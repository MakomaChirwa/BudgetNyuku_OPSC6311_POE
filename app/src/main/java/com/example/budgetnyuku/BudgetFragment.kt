package com.example.budgetnyuku

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class BudgetFragment : Fragment() {

    private lateinit var db: DatabaseHelper
    private var userId: Int = -1
    private lateinit var expenseAdapter: ExpenseAdapter
    private val expenses = mutableListOf<Expense>()
    private var currentStartDate = ""
    private var currentEndDate = ""

    companion object {
        fun newInstance(userId: Int): BudgetFragment {
            val fragment = BudgetFragment()
            val args = Bundle()
            args.putInt("USER_ID", userId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_budget, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = DatabaseHelper(requireContext())
        userId = arguments?.getInt("USER_ID") ?: -1

        if (userId == -1) {
            Toast.makeText(requireContext(), "Error", Toast.LENGTH_SHORT).show()
            return
        }

        setupViews(view)
        loadBudgetSettings()
        setupDateRange()
        loadExpenses()

        view.findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            (requireActivity() as? MainActivity)?.findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_navigation)?.selectedItemId = R.id.nav_dashboard
        }

        view.findViewById<Button>(R.id.btnSaveBudget).setOnClickListener { saveBudgetSettings() }
        view.findViewById<Button>(R.id.btnFilterDate).setOnClickListener { showDateRangePicker() }
    }

    private fun setupViews(view: View) {
        val recyclerView = view.findViewById<RecyclerView>(R.id.rvExpenses)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        val currencySymbol = db.getCurrencySymbol(userId)
        
        view.findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.tilMinGoal).prefixText = currencySymbol
        view.findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.tilMaxGoal).prefixText = currencySymbol
        
        expenseAdapter = ExpenseAdapter(expenses, currencySymbol) { expense ->
            showExpenseDetails(expense, currencySymbol)
        }
        recyclerView.adapter = expenseAdapter
    }

    private fun loadBudgetSettings() {
        val etMinGoal = view?.findViewById<EditText>(R.id.etMinGoal)
        val etMaxGoal = view?.findViewById<EditText>(R.id.etMaxGoal)

        val settings = db.getBudgetSettings(userId)
        if (settings != null) {
            etMinGoal?.setText(settings.first.toString())
            etMaxGoal?.setText(settings.second.toString())
        }
    }

    private fun saveBudgetSettings() {
        val etMinGoal = view?.findViewById<EditText>(R.id.etMinGoal)
        val etMaxGoal = view?.findViewById<EditText>(R.id.etMaxGoal)

        val minGoal = etMinGoal?.text.toString().toDoubleOrNull()
        val maxGoal = etMaxGoal?.text.toString().toDoubleOrNull()

        if (minGoal == null || maxGoal == null) {
            Toast.makeText(requireContext(), "Please enter valid amounts", Toast.LENGTH_SHORT).show()
            return
        }

        if (minGoal >= maxGoal) {
            Toast.makeText(requireContext(), "Minimum goal must be less than maximum goal", Toast.LENGTH_SHORT).show()
            return
        }

        if (db.saveBudgetSettings(userId, minGoal, maxGoal)) {
            Toast.makeText(requireContext(), "Budget settings saved! 🎯", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), "Failed to save settings", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupDateRange() {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        currentEndDate = dateFormat.format(calendar.time)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        currentStartDate = dateFormat.format(calendar.time)
    }

    private fun showDateRangePicker() {
        val calendar = Calendar.getInstance()

        DatePickerDialog(requireContext(), { _, startYear, startMonth, startDay ->
            val startDate = String.format("%04d-%02d-%02d", startYear, startMonth + 1, startDay)

            DatePickerDialog(requireContext(), { _, endYear, endMonth, endDay ->
                val endDate = String.format("%04d-%02d-%02d", endYear, endMonth + 1, endDay)
                currentStartDate = startDate
                currentEndDate = endDate
                loadExpenses()
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun loadExpenses() {
        expenses.clear()
        expenses.addAll(db.getExpensesByDateRange(userId, currentStartDate, currentEndDate))
        expenseAdapter.notifyDataSetChanged()

        val totalAmount = expenses.sumOf { it.amount }
        val currencySymbol = db.getCurrencySymbol(userId)
        view?.findViewById<TextView>(R.id.tvTotalAmount)?.text = String.format(Locale.getDefault(), "Total: %s%.2f", currencySymbol, totalAmount)
        view?.findViewById<TextView>(R.id.tvDateRange)?.text = String.format(Locale.getDefault(), "%s to %s", currentStartDate, currentEndDate)

        updateBudgetProgress(totalAmount, currencySymbol)

        if (expenses.isEmpty()) {
            view?.findViewById<TextView>(R.id.tvNoExpenses)?.visibility = View.VISIBLE
        } else {
            view?.findViewById<TextView>(R.id.tvNoExpenses)?.visibility = View.GONE
        }
    }

    private fun updateBudgetProgress(totalSpent: Double, currencySymbol: String) {
        val settings = db.getBudgetSettings(userId)
        if (settings != null) {
            val minGoal = settings.first
            val maxGoal = settings.second

            val progressBar = view?.findViewById<ProgressBar>(R.id.progressBudget)
            val tvProgressText = view?.findViewById<TextView>(R.id.tvProgressText)

            val progress = if (maxGoal > 0) ((totalSpent / maxGoal) * 100).toInt().coerceIn(0, 100) else 0
            progressBar?.progress = progress

            val status = when {
                totalSpent < minGoal -> String.format(Locale.getDefault(), "⚠️ Below minimum goal (Need: %s%.2f more)", currencySymbol, minGoal - totalSpent)
                totalSpent > maxGoal -> String.format(Locale.getDefault(), "⚠️ Exceeded maximum goal by %s%.2f", currencySymbol, totalSpent - maxGoal)
                else -> "✓ On track! You're within your budget range"
            }
            tvProgressText?.text = status
        }
    }

    private fun showExpenseDetails(expense: Expense, currencySymbol: String) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Expense Details")
            .setMessage(
                String.format(
                    Locale.getDefault(),
                    "Category: %s\nAmount: %s%.2f\nDate: %s\nTime: %s - %s\nDescription: %s",
                    expense.categoryName,
                    currencySymbol,
                    expense.amount,
                    expense.date,
                    expense.startTime,
                    expense.endTime,
                    expense.description
                )
            )
            .setPositiveButton("Close", null)
            .setNeutralButton("Delete") { _, _ ->
                androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("Delete Expense")
                    .setMessage("Are you sure you want to delete this expense?")
                    .setPositiveButton("Delete") { _, _ ->
                        if (db.deleteExpense(expense.id)) {
                            Toast.makeText(requireContext(), "Expense deleted", Toast.LENGTH_SHORT).show()
                            loadExpenses()
                        }
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
            .show()
    }
}