package com.example.budgetnyuku

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

class AddEditExpense : AppCompatActivity() {


    private lateinit var db: DatabaseHelper
    private var userId: Int = -1
    private var selectedCategoryId: Int = -1
    private var selectedPhoto: ByteArray? = null
    private val categories = mutableListOf<Category>()

    private lateinit var spinnerCategory: Spinner
    private lateinit var etAmount: EditText
    private lateinit var etDate: EditText
    private lateinit var etStartTime: EditText
    private lateinit var etEndTime: EditText
    private lateinit var etDescription: EditText
    private lateinit var btnTakePhoto: MaterialButton
    private lateinit var btnSelectPhoto: MaterialButton
    private lateinit var ivPhotoPreview: ImageView
    private lateinit var btnRemovePhoto: Button

    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val imageBitmap = result.data?.extras?.getParcelable<android.graphics.Bitmap>("data")
            if (imageBitmap != null) {
                val stream = ByteArrayOutputStream()
                imageBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 80, stream)
                selectedPhoto = stream.toByteArray()
                ivPhotoPreview.setImageBitmap(imageBitmap)
                ivPhotoPreview.visibility = android.view.View.VISIBLE
                btnRemovePhoto.visibility = android.view.View.VISIBLE
            }
        }
    }

    private val selectPhotoLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK && result.data?.data != null) {
            val uri = result.data?.data!!
            val inputStream = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            val stream = ByteArrayOutputStream()
            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 80, stream)
            selectedPhoto = stream.toByteArray()
            ivPhotoPreview.setImageBitmap(bitmap)
            ivPhotoPreview.visibility = android.view.View.VISIBLE
            btnRemovePhoto.visibility = android.view.View.VISIBLE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_expense)

        db = DatabaseHelper(this)
        userId = intent.getIntExtra("USER_ID", -1)

        if (userId == -1) {
            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        initViews()
        loadCategories()
        setupDateAndTimePickers()
        setupPhotoButtons()

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<Button>(R.id.btnSaveExpense).setOnClickListener { saveExpense() }
    }

    private fun initViews() {
        spinnerCategory = findViewById(R.id.spinnerCategory)
        etAmount = findViewById(R.id.etAmount)
        etDate = findViewById(R.id.etDate)
        etStartTime = findViewById(R.id.etStartTime)
        etEndTime = findViewById(R.id.etEndTime)
        etDescription = findViewById(R.id.etDescription)
        btnTakePhoto = findViewById(R.id.btnTakePhoto)
        btnSelectPhoto = findViewById(R.id.btnSelectPhoto)
        ivPhotoPreview = findViewById(R.id.ivPhotoPreview)
        btnRemovePhoto = findViewById(R.id.btnRemovePhoto)

        btnRemovePhoto.setOnClickListener {
            selectedPhoto = null
            ivPhotoPreview.setImageDrawable(null)
            ivPhotoPreview.visibility = android.view.View.GONE
            btnRemovePhoto.visibility = android.view.View.GONE
        }
    }

    private fun loadCategories() {
        categories.clear()
        categories.addAll(db.getCategories(userId))

        if (categories.isEmpty()) {
            Toast.makeText(this, "Please create categories first", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        val categoryNames = categories.map { it.name }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categoryNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = adapter

        spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: android.view.View?, position: Int, id: Long) {
                selectedCategoryId = categories[position].id
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun setupDateAndTimePickers() {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        etDate.setText(dateFormat.format(calendar.time))
        etDate.setOnClickListener {
            val datePicker = android.app.DatePickerDialog(this, { _, year, month, day ->
                calendar.set(year, month, day)
                etDate.setText(dateFormat.format(calendar.time))
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
            datePicker.show()
        }

        etStartTime.setText(timeFormat.format(calendar.time))
        etStartTime.setOnClickListener {
            val timePicker = android.app.TimePickerDialog(this, { _, hour, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hour)
                calendar.set(Calendar.MINUTE, minute)
                etStartTime.setText(timeFormat.format(calendar.time))
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true)
            timePicker.show()
        }

        etEndTime.setText(timeFormat.format(calendar.time))
        etEndTime.setOnClickListener {
            val timePicker = android.app.TimePickerDialog(this, { _, hour, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hour)
                calendar.set(Calendar.MINUTE, minute)
                etEndTime.setText(timeFormat.format(calendar.time))
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true)
            timePicker.show()
        }
    }

    private fun setupPhotoButtons() {
        btnTakePhoto.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                launchCamera()
            } else {
                requestCameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
            }
        }

        btnSelectPhoto.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            selectPhotoLauncher.launch(intent)
        }
    }

    private val requestCameraPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            launchCamera()
        } else {
            Toast.makeText(this, "Camera permission is required to take photos", Toast.LENGTH_SHORT).show()
        }
    }

    private fun launchCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(packageManager) != null) {
            takePictureLauncher.launch(intent)
        } else {
            Toast.makeText(this, "No camera app found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveExpense() {
        val amountText = etAmount.text.toString()
        if (amountText.isEmpty()) {
            Toast.makeText(this, "Please enter an amount", Toast.LENGTH_SHORT).show()
            return
        }

        val amount = amountText.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedCategoryId == -1) {
            Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show()
            return
        }

        val expense = Expense(
            id = 0,
            userId = userId,
            categoryId = selectedCategoryId,
            amount = amount,
            date = etDate.text.toString(),
            startTime = etStartTime.text.toString(),
            endTime = etEndTime.text.toString(),
            description = etDescription.text.toString(),
            photo = selectedPhoto
        )

        if (db.addExpense(expense)) {
            Toast.makeText(this, "Expense added successfully", Toast.LENGTH_SHORT).show()
            setResult(RESULT_OK)
            finish()
        } else {
            Toast.makeText(this, "Failed to add expense", Toast.LENGTH_SHORT).show()
        }
    }
}