package com.example.budgetnyuku

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.SimpleDateFormat
import java.util.*

class Dashboard : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private var userId: Int = -1
    private lateinit var userName: String
    private lateinit var recentExpensesAdapter: ExpenseAdapter
    private val recentExpenses = mutableListOf<Expense>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            setContentView(R.layout.activity_dashboard)

            db = DatabaseHelper(this)
            userId = intent.getIntExtra("USER_ID", -1)

            Log.d("Dashboard", "UserId received: $userId")

            if (userId == -1) {
                Toast.makeText(this, "Session expired", Toast.LENGTH_SHORT).show()
                finish()
                return
            }

            userName = db.getUserName(userId)
            Log.d("Dashboard", "UserName: $userName")

            setupUI()
            setupRecyclerView() // Initialize RecyclerView and Adapter BEFORE loading data
            setupClickListeners()
            loadDashboardData()

        } catch (e: Exception) {
            Log.e("Dashboard", "Error in onCreate", e)
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupUI() {
        findViewById<TextView>(R.id.tvWelcome).text = "Welcome, $userName!"
    }

    private fun setupClickListeners() {
        findViewById<androidx.cardview.widget.CardView>(R.id.cardCategories).setOnClickListener {
            startActivity(Intent(this, ManageCategories::class.java).putExtra("USER_ID", userId))
        }

        findViewById<androidx.cardview.widget.CardView>(R.id.cardAddExpense).setOnClickListener {
            startActivityForResult(Intent(this, AddEditExpense::class.java).putExtra("USER_ID", userId), 1)
        }

        findViewById<androidx.cardview.widget.CardView>(R.id.cardViewExpenses).setOnClickListener {
            startActivity(Intent(this, ExpenseListActivity::class.java).putExtra("USER_ID", userId))
        }

        findViewById<androidx.cardview.widget.CardView>(R.id.cardCategoryReport).setOnClickListener {
            startActivity(Intent(this, CategoryReportActivity::class.java).putExtra("USER_ID", userId))
        }

        findViewById<androidx.cardview.widget.CardView>(R.id.cardBudgetSettings).setOnClickListener {
            startActivity(Intent(this, BudgetSettingsActivity::class.java).putExtra("USER_ID", userId))
        }

        findViewById<FloatingActionButton>(R.id.fabLogout).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes") { _, _ ->
                    startActivity(Intent(this, NyukuLauncher::class.java))
                    finish()
                }
                .setNegativeButton("No", null)
                .show()
        }
    }

    private fun setupRecyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.rvRecentExpenses)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recentExpensesAdapter = ExpenseAdapter(recentExpenses) { expense ->
            showExpenseDetails(expense)
        }
        recyclerView.adapter = recentExpensesAdapter
    }

    private fun loadDashboardData() {
        try {
            val calendar = Calendar.getInstance()
            val endDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
            calendar.add(Calendar.MONTH, -1)
            val startDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)

            Log.d("Dashboard", "Loading expenses from $startDate to $endDate")

            val expenses = db.getExpensesByDateRange(userId, startDate, endDate)
            recentExpenses.clear()
            recentExpenses.addAll(expenses.take(5))
            
            // Check if adapter is initialized before notifying
            if (::recentExpensesAdapter.isInitialized) {
                recentExpensesAdapter.notifyDataSetChanged()
            }

            if (recentExpenses.isEmpty()) {
                findViewById<TextView>(R.id.tvNoRecentExpenses).visibility = android.view.View.VISIBLE
            } else {
                findViewById<TextView>(R.id.tvNoRecentExpenses).visibility = android.view.View.GONE
            }

            val currentMonth = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
            val monthlyTotal = db.getMonthlyTotal(userId, currentMonth)
            val budgetSettings = db.getBudgetSettings(userId)

            findViewById<TextView>(R.id.tvMonthlyTotal).text = String.format("R%.2f", monthlyTotal)

            if (budgetSettings != null) {
                findViewById<TextView>(R.id.tvBudgetInfo).text = String.format(
                    "Budget: R%.2f - R%.2f",
                    budgetSettings.first,
                    budgetSettings.second
                )

                when {
                    monthlyTotal < budgetSettings.first -> {
                        findViewById<TextView>(R.id.tvBudgetStatus).text = "⚠️ Below minimum goal!"
                        findViewById<TextView>(R.id.tvBudgetStatus).setTextColor(
                            resources.getColor(android.R.color.holo_orange_dark)
                        )
                    }
                    monthlyTotal > budgetSettings.second -> {
                        findViewById<TextView>(R.id.tvBudgetStatus).text = "⚠️ Exceeded maximum goal!"
                        findViewById<TextView>(R.id.tvBudgetStatus).setTextColor(
                            resources.getColor(android.R.color.holo_red_dark)
                        )
                    }
                    else -> {
                        findViewById<TextView>(R.id.tvBudgetStatus).text = "✓ On track!"
                        findViewById<TextView>(R.id.tvBudgetStatus).setTextColor(
                            resources.getColor(android.R.color.holo_green_dark)
                        )
                    }
                }
            } else {
                findViewById<TextView>(R.id.tvBudgetInfo).text = "No budget set"
                findViewById<TextView>(R.id.tvBudgetStatus).text = "Set a budget to track goals"
                findViewById<TextView>(R.id.tvBudgetStatus).setTextColor(
                    resources.getColor(android.R.color.darker_gray)
                )
            }
        } catch (e: Exception) {
            Log.e("Dashboard", "Error loading data", e)
            Toast.makeText(this, "Error loading data: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showExpenseDetails(expense: Expense) {
        AlertDialog.Builder(this)
            .setTitle("Expense Details")
            .setMessage("""
                Category: ${expense.categoryName}
                Amount: R${String.format("%.2f", expense.amount)}
                Date: ${expense.date}
                Time: ${expense.startTime} - ${expense.endTime}
                Description: ${expense.description}
            """.trimIndent())
            .setPositiveButton("OK", null)
            .show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            loadDashboardData()
        }
    }

    override fun onResume() {
        super.onResume()
        loadDashboardData()
    }
}