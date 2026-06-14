package com.example.budgetnyuku

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.app.DatePickerDialog
import android.graphics.BitmapFactory
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class ExpenseListActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private var userId: Int = -1
    private lateinit var expenseAdapter: ExpenseAdapter
    private val expenses = mutableListOf<Expense>()
    private var currentStartDate = ""
    private var currentEndDate = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expense_list)

        db = DatabaseHelper(this)
        userId = intent.getIntExtra("USER_ID", -1)

        if (userId == -1) {
            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val recyclerView = findViewById<RecyclerView>(R.id.rvExpenses)
        recyclerView.layoutManager = LinearLayoutManager(this)
        val currencySymbol = db.getCurrencySymbol(userId)
        expenseAdapter = ExpenseAdapter(expenses, currencySymbol) { expense ->
            showExpenseDetails(expense)
        }
        recyclerView.adapter = expenseAdapter

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<Button>(R.id.btnFilterDate).setOnClickListener { showDateRangePicker() }

        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        currentEndDate = dateFormat.format(calendar.time)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        currentStartDate = dateFormat.format(calendar.time)

        loadExpenses()
    }

    private fun showDateRangePicker() {
        val calendar = Calendar.getInstance()

        val startDatePicker = DatePickerDialog(this, { _, startYear, startMonth, startDay ->
            val startDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", startYear, startMonth + 1, startDay)

            DatePickerDialog(this, { _, endYear, endMonth, endDay ->
                val endDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", endYear, endMonth + 1, endDay)
                currentStartDate = startDate
                currentEndDate = endDate
                loadExpenses()
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))

        startDatePicker.show()
    }

    private fun loadExpenses() {
        expenses.clear()
        expenses.addAll(db.getExpensesByDateRange(userId, currentStartDate, currentEndDate))
        expenseAdapter.notifyDataSetChanged()

        val totalAmount = expenses.sumOf { it.amount }
        val currencySymbol = db.getCurrencySymbol(userId)
        findViewById<TextView>(R.id.tvTotalAmount).text = String.format(Locale.getDefault(), "Total: %s%.2f", currencySymbol, totalAmount)
        findViewById<TextView>(R.id.tvDateRange).text = String.format(Locale.getDefault(), "%s to %s", currentStartDate, currentEndDate)

        if (expenses.isEmpty()) {
            findViewById<TextView>(R.id.tvNoExpenses).visibility = android.view.View.VISIBLE
        } else {
            findViewById<TextView>(R.id.tvNoExpenses).visibility = android.view.View.GONE
        }
    }

    private fun showExpenseDetails(expense: Expense) {
        val view = layoutInflater.inflate(R.layout.dialog_expense_details, null)

        val currencySymbol = db.getCurrencySymbol(userId)
        view.findViewById<TextView>(R.id.tvCategory).text = expense.categoryName
        view.findViewById<TextView>(R.id.tvAmount).text = String.format(Locale.getDefault(), "%s%.2f", currencySymbol, expense.amount)
        view.findViewById<TextView>(R.id.tvDate).text = expense.date
        view.findViewById<TextView>(R.id.tvTime).text = String.format(Locale.getDefault(), "%s - %s", expense.startTime, expense.endTime)
        view.findViewById<TextView>(R.id.tvDescription).text = expense.description.ifEmpty { "No description" }

        val ivPhoto = view.findViewById<ImageView>(R.id.ivPhoto)
        if (expense.photo != null && expense.photo.isNotEmpty()) {
            val bitmap = BitmapFactory.decodeByteArray(expense.photo, 0, expense.photo.size)
            ivPhoto.setImageBitmap(bitmap)
            ivPhoto.visibility = android.view.View.VISIBLE
            ivPhoto.setOnClickListener {
                showFullScreenImage(bitmap)
            }
        } else {
            ivPhoto.visibility = android.view.View.GONE
        }

        AlertDialog.Builder(this)
            .setTitle("Expense Details")
            .setView(view)
            .setPositiveButton("Close", null)
            .setNeutralButton("Delete") { _, _ ->
                AlertDialog.Builder(this)
                    .setTitle("Delete Expense")
                    .setMessage("Are you sure you want to delete this expense?")
                    .setPositiveButton("Delete") { _, _ ->
                        if (db.deleteExpense(expense.id)) {
                            Toast.makeText(this, "Expense deleted", Toast.LENGTH_SHORT).show()
                            loadExpenses()
                        }
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
            .show()
    }

    private fun showFullScreenImage(bitmap: android.graphics.Bitmap) {
        val imageView = ImageView(this)
        imageView.setImageBitmap(bitmap)
        imageView.scaleType = ImageView.ScaleType.FIT_CENTER
        imageView.setOnClickListener { (it.parent as? AlertDialog)?.dismiss() }

        AlertDialog.Builder(this)
            .setView(imageView)
            .setPositiveButton("Close", null)
            .show()
    }
}