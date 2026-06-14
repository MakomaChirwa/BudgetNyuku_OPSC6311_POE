package com.example.budgetnyuku

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.ChipGroup
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

class AddTransactionFragment : Fragment() {

    private lateinit var db: DatabaseHelper
    private var userId: Int = -1
    private var selectedType: String = "Expense"
    private var selectedCategoryId: Int = -1
    private var selectedPhoto: ByteArray? = null

    private lateinit var chipGroupType: ChipGroup
    private lateinit var etAmount: EditText
    private lateinit var spinnerCategory: Spinner
    private lateinit var etDescription: EditText
    private lateinit var tvDate: TextView
    private lateinit var etStartTime: EditText
    private lateinit var etEndTime: EditText
    private lateinit var btnSubmit: Button
    private lateinit var btnTakePhoto: MaterialButton
    private lateinit var btnSelectPhoto: MaterialButton
    private lateinit var ivPhotoPreview: ImageView

    private val categories = mutableListOf<Category>()

    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageBitmap = result.data?.extras?.get("data") as? Bitmap
            imageBitmap?.let {
                val stream = ByteArrayOutputStream()
                it.compress(Bitmap.CompressFormat.JPEG, 80, stream)
                selectedPhoto = stream.toByteArray()
                ivPhotoPreview.setImageBitmap(it)
                ivPhotoPreview.visibility = View.VISIBLE
            }
        }
    }

    private val selectPictureLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageUri = result.data?.data
            imageUri?.let {
                val inputStream = requireContext().contentResolver.openInputStream(it)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                val stream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
                selectedPhoto = stream.toByteArray()
                ivPhotoPreview.setImageBitmap(bitmap)
                ivPhotoPreview.visibility = View.VISIBLE
            }
        }
    }

    companion object {
        fun newInstance(userId: Int): AddTransactionFragment {
            val fragment = AddTransactionFragment()
            val args = Bundle()
            args.putInt("USER_ID", userId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_add_transaction, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = DatabaseHelper(requireContext())
        userId = arguments?.getInt("USER_ID") ?: -1

        if (userId == -1) {
            Toast.makeText(requireContext(), "Error: User not found", Toast.LENGTH_SHORT).show()
            return
        }

        initViews(view)
        setupTypeSelection()
        setupDateAndTimePickers()
        loadCategories()
        setupPhotoButtons()

        view.findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            (requireActivity() as? MainActivity)?.findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_navigation)?.selectedItemId = R.id.nav_dashboard
        }

        btnSubmit.setOnClickListener { saveTransaction() }
    }

    private fun initViews(view: View) {
        chipGroupType = view.findViewById(R.id.chipGroupType)
        etAmount = view.findViewById(R.id.etAmount)
        spinnerCategory = view.findViewById(R.id.spinnerCategory)
        etDescription = view.findViewById(R.id.etDescription)
        tvDate = view.findViewById(R.id.tvDate)
        etStartTime = view.findViewById(R.id.etStartTime)
        etEndTime = view.findViewById(R.id.etEndTime)
        btnSubmit = view.findViewById(R.id.btnSubmit)
        btnTakePhoto = view.findViewById(R.id.btnTakePhoto)
        btnSelectPhoto = view.findViewById(R.id.btnSelectPhoto)
        ivPhotoPreview = view.findViewById(R.id.ivPhotoPreview)
    }

    private fun setupPhotoButtons() {
        btnTakePhoto.setOnClickListener {
            if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                launchCamera()
            } else {
                requestCameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
            }
        }

        btnSelectPhoto.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            selectPictureLauncher.launch(intent)
        }
    }

    private val requestCameraPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            launchCamera()
        } else {
            Toast.makeText(requireContext(), "Camera permission is required to take photos", Toast.LENGTH_SHORT).show()
        }
    }

    private fun launchCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(requireContext().packageManager) != null) {
            takePictureLauncher.launch(intent)
        } else {
            Toast.makeText(requireContext(), "No camera app found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupTypeSelection() {
        chipGroupType.setOnCheckedStateChangeListener { _, checkedIds ->
            when (checkedIds.firstOrNull()) {
                R.id.chipExpense -> {
                    selectedType = "Expense"
                    btnSubmit.text = getString(R.string.add_expense)
                    btnSubmit.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark))
                }
                R.id.chipIncome -> {
                    selectedType = "Income"
                    btnSubmit.text = getString(R.string.add_income)
                    btnSubmit.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark))
                }
            }
        }
    }

    private fun loadCategories() {
        categories.clear()
        categories.addAll(db.getCategories(userId))

        if (categories.isEmpty()) {
            Toast.makeText(requireContext(), "Please create categories first in Categories tab", Toast.LENGTH_LONG).show()
            return
        }

        val categoryNames = categories.map { it.name }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categoryNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = adapter

        spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedCategoryId = categories[position].id
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun setupDateAndTimePickers() {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        tvDate.text = dateFormat.format(calendar.time)
        tvDate.setOnClickListener {
            DatePickerDialog(requireContext(), { _, year, month, day ->
                calendar.set(year, month, day)
                tvDate.text = dateFormat.format(calendar.time)
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        etStartTime.setText(timeFormat.format(calendar.time))
        etStartTime.setOnClickListener {
            TimePickerDialog(requireContext(), { _, hour, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hour)
                calendar.set(Calendar.MINUTE, minute)
                etStartTime.setText(timeFormat.format(calendar.time))
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
        }

        etEndTime.setText(timeFormat.format(calendar.time))
        etEndTime.setOnClickListener {
            TimePickerDialog(requireContext(), { _, hour, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hour)
                calendar.set(Calendar.MINUTE, minute)
                etEndTime.setText(timeFormat.format(calendar.time))
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
        }
    }

    private fun saveTransaction() {
        val amountText = etAmount.text.toString()
        if (amountText.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter an amount", Toast.LENGTH_SHORT).show()
            return
        }

        val amount = amountText.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            Toast.makeText(requireContext(), "Please enter a valid amount", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedCategoryId == -1) {
            Toast.makeText(requireContext(), "Please select a category", Toast.LENGTH_SHORT).show()
            return
        }

        val expense = Expense(
            id = 0,
            userId = userId,
            categoryId = selectedCategoryId,
            amount = amount,
            date = tvDate.text.toString(),
            startTime = etStartTime.text.toString(),
            endTime = etEndTime.text.toString(),
            description = etDescription.text.toString(),
            photo = selectedPhoto
        )

        if (db.addExpense(expense)) {
            val message = if (selectedType == "Expense") "Expense added successfully! 🎉" else "Income added successfully! 🎉"
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            clearForm()
        } else {
            Toast.makeText(requireContext(), "Failed to add ${selectedType.lowercase()}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun clearForm() {
        etAmount.text.clear()
        etDescription.text.clear()
        selectedPhoto = null
        ivPhotoPreview.visibility = View.GONE
        ivPhotoPreview.setImageDrawable(null)
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        tvDate.text = dateFormat.format(calendar.time)
        etStartTime.setText(timeFormat.format(calendar.time))
        etEndTime.setText(timeFormat.format(calendar.time))
        // Reset to first category
        if (categories.isNotEmpty()) {
            spinnerCategory.setSelection(0)
        }
    }
}