package com.example.budgetnyuku

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.app.DatePickerDialog
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class CategoryReportActivity : AppCompatActivity() {


    private lateinit var db: DatabaseHelper
    private var userId: Int = -1
    private lateinit var reportAdapter: CategoryReportAdapter
    private val categoryTotals = mutableListOf<CategoryTotal>()
    private var currentStartDate = ""
    private var currentEndDate = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category_report)

        db = DatabaseHelper(this)
        userId = intent.getIntExtra("USER_ID", -1)

        if (userId == -1) {
            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val recyclerView = findViewById<RecyclerView>(R.id.rvCategoryTotals)
        recyclerView.layoutManager = LinearLayoutManager(this)
        reportAdapter = CategoryReportAdapter(categoryTotals)
        recyclerView.adapter = reportAdapter

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<Button>(R.id.btnFilterDate).setOnClickListener { showDateRangePicker() }

        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        currentEndDate = dateFormat.format(calendar.time)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        currentStartDate = dateFormat.format(calendar.time)

        loadReport()
    }

    private fun showDateRangePicker() {
        val calendar = Calendar.getInstance()

        val startDatePicker = DatePickerDialog(this, { _, startYear, startMonth, startDay ->
            val startDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", startYear, startMonth + 1, startDay)

            DatePickerDialog(this, { _, endYear, endMonth, endDay ->
                val endDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", endYear, endMonth + 1, endDay)
                currentStartDate = startDate
                currentEndDate = endDate
                loadReport()
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))

        startDatePicker.show()
    }

    private fun loadReport() {
        categoryTotals.clear()
        categoryTotals.addAll(db.getCategoryTotals(userId, currentStartDate, currentEndDate))
        reportAdapter.notifyDataSetChanged()

        val totalAmount = categoryTotals.sumOf { it.totalAmount }
        val currencySymbol = db.getCurrencySymbol(userId)
        findViewById<TextView>(R.id.tvTotalAmount).text = String.format(Locale.getDefault(), "Total: %s%.2f", currencySymbol, totalAmount)
        findViewById<TextView>(R.id.tvDateRange).text = String.format(Locale.getDefault(), "%s to %s", currentStartDate, currentEndDate)

        if (categoryTotals.isEmpty()) {
            findViewById<TextView>(R.id.tvNoData).visibility = android.view.View.VISIBLE
        } else {
            findViewById<TextView>(R.id.tvNoData).visibility = android.view.View.GONE
        }
    }
}