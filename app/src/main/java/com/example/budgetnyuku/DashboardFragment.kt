package com.example.budgetnyuku

import androidx.core.graphics.toColorInt
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.chip.ChipGroup
import java.text.SimpleDateFormat
import java.util.*

class DashboardFragment : Fragment() {

    private lateinit var db: DatabaseHelper
    private var userId: Int = -1
    private lateinit var barChart: BarChart
    private lateinit var tvMonthlyTotal: TextView
    private lateinit var tvBudgetStatus: TextView
    private lateinit var tvBudgetInfo: TextView
    private lateinit var tvWelcome: TextView
    private lateinit var tvPoints: TextView
    private lateinit var tvBadges: TextView
    private lateinit var chipGroupPeriod: ChipGroup
    private lateinit var rvRecentExpenses: RecyclerView
    private lateinit var rvCategorySpending: RecyclerView
    private lateinit var recentExpensesAdapter: ExpenseAdapter
    private lateinit var categorySpendingAdapter: CategorySpendingAdapter
    private val recentExpenses = mutableListOf<Expense>()
    private val categoryTotals = mutableListOf<CategoryTotal>()

    private var userPoints = 0
    private val userBadges = mutableListOf<String>()
    private var currencySymbol = "R"

    companion object {
        fun newInstance(userId: Int): DashboardFragment {
            val fragment = DashboardFragment()
            val args = Bundle()
            args.putInt("USER_ID", userId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = DatabaseHelper(requireContext())
        userId = arguments?.getInt("USER_ID") ?: -1

        if (userId == -1) {
            Toast.makeText(requireContext(), "Error loading user", Toast.LENGTH_SHORT).show()
            return
        }

        initViews(view)
        currencySymbol = db.getCurrencySymbol(userId)
        setupRecyclerView()
        loadDashboardData()
        setupPeriodSelector()
        loadUserPointsAndBadges()

        view.findViewById<View>(R.id.btnSettings).setOnClickListener {
            val intent = Intent(requireContext(), SettingsActivity::class.java)
            intent.putExtra("USER_ID", userId)
            startActivity(intent)
        }

        view.findViewById<View>(R.id.btnBack).setOnClickListener {
            requireActivity().finish()
        }
    }

    private fun initViews(view: View) {
        barChart = view.findViewById(R.id.barChart)
        tvMonthlyTotal = view.findViewById(R.id.tvMonthlyTotal)
        tvBudgetStatus = view.findViewById(R.id.tvBudgetStatus)
        tvBudgetInfo = view.findViewById(R.id.tvBudgetInfo)
        tvWelcome = view.findViewById(R.id.tvWelcome)
        tvPoints = view.findViewById(R.id.tvPoints)
        tvBadges = view.findViewById(R.id.tvBadges)
        chipGroupPeriod = view.findViewById(R.id.chipGroupPeriod)
        rvRecentExpenses = view.findViewById(R.id.rvRecentExpenses)
        rvCategorySpending = view.findViewById(R.id.rvCategorySpending)

        val userName = db.getUserName(userId)
        tvWelcome.text = "Welcome, $userName!"
    }

    private fun setupRecyclerView() {
        rvRecentExpenses.layoutManager = LinearLayoutManager(requireContext())
        recentExpensesAdapter = ExpenseAdapter(recentExpenses, currencySymbol) { expense ->
            showExpenseDetails(expense)
        }
        rvRecentExpenses.adapter = recentExpensesAdapter

        rvCategorySpending.layoutManager = LinearLayoutManager(requireContext())
        categorySpendingAdapter = CategorySpendingAdapter(categoryTotals, currencySymbol)
        rvCategorySpending.adapter = categorySpendingAdapter
    }

    private fun setupPeriodSelector() {
        chipGroupPeriod.setOnCheckedStateChangeListener { _, checkedIds ->
            when (checkedIds.firstOrNull()) {
                R.id.chipWeek -> loadChartData("week")
                R.id.chipMonth -> loadChartData("month")
                R.id.chipYear -> loadChartData("year")
            }
        }
        loadChartData("month")
    }

    private fun loadDashboardData() {
        val calendar = Calendar.getInstance()
        val endDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
        calendar.add(Calendar.MONTH, -1)
        val startDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)

        val expenses = db.getExpensesByDateRange(userId, startDate, endDate)
        recentExpenses.clear()
        recentExpenses.addAll(expenses.take(5))
        recentExpensesAdapter.notifyDataSetChanged()

        val currentMonth = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
        val monthlyTotal = db.getMonthlyTotal(userId, currentMonth)
        val budgetSettings = db.getBudgetSettings(userId)

        tvMonthlyTotal.text = String.format(Locale.getDefault(), "%s%.2f", currencySymbol, monthlyTotal)

        if (budgetSettings != null) {
            val minGoal = budgetSettings.first
            val maxGoal = budgetSettings.second

            tvBudgetInfo.text = String.format(Locale.getDefault(), "Budget: %s%.2f - %s%.2f", currencySymbol, minGoal, currencySymbol, maxGoal)

            when {
                monthlyTotal < minGoal -> {
                    tvBudgetStatus.text = "⚠️ Below minimum goal!"
                    tvBudgetStatus.setTextColor("#FF9800".toColorInt())
                }
                monthlyTotal > maxGoal -> {
                    tvBudgetStatus.text = "⚠️ Exceeded maximum goal!"
                    tvBudgetStatus.setTextColor("#F44336".toColorInt())
                }
                else -> {
                    tvBudgetStatus.text = "✓ On track! Great job!"
                    tvBudgetStatus.setTextColor("#4CAF50".toColorInt())
                    if (!userBadges.contains("Budget Hero")) {
                        db.addUserPoints(userId, 50)
                        db.saveUserBadge(userId, "🏆 Budget Hero")
                        loadUserPointsAndBadges()
                        Toast.makeText(requireContext(), "🎉 +50 points! You earned the Budget Hero badge!", Toast.LENGTH_LONG).show()
                    }
                }
            }
        } else {
            tvBudgetInfo.text = "No budget set"
            tvBudgetStatus.text = "Set a budget to track goals"
        }

        if ((recentExpenses.size >= 5) && !userBadges.contains("Consistent Logger")) {
            db.addUserPoints(userId, 20)
            db.saveUserBadge(userId, "📝 Consistent Logger")
            loadUserPointsAndBadges()
            Toast.makeText(requireContext(), "🎉 +20 points! You earned the Consistent Logger badge!", Toast.LENGTH_LONG).show()
        }
    }

    private fun loadChartData(period: String) {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val endDate = dateFormat.format(calendar.time)

        val startDate = when (period) {
            "week" -> {
                calendar.add(Calendar.DAY_OF_MONTH, -7)
                dateFormat.format(calendar.time)
            }
            "year" -> {
                calendar.add(Calendar.YEAR, -1)
                dateFormat.format(calendar.time)
            }
            else -> { // month
                calendar.add(Calendar.MONTH, -1)
                dateFormat.format(calendar.time)
            }
        }

        val currentCategoryTotals = db.getCategoryTotals(userId, startDate, endDate)
        categoryTotals.clear()
        categoryTotals.addAll(currentCategoryTotals)
        categorySpendingAdapter.notifyDataSetChanged()

        if (categoryTotals.isEmpty()) {
            barChart.clear()
            barChart.invalidate()
            barChart.setNoDataText("No data for selected period")
            return
        }

        setupBarChart(categoryTotals)

        val budgetSettings = db.getBudgetSettings(userId)
        if (budgetSettings != null) {
            val totalSpent = categoryTotals.sumOf { it.totalAmount }
            if (totalSpent > budgetSettings.second) {
                Toast.makeText(requireContext(), "⚠️ Warning: You've exceeded your maximum budget!", Toast.LENGTH_SHORT).show()
            } else if (totalSpent < budgetSettings.first) {
                Toast.makeText(requireContext(), "💡 Tip: You're below your minimum spending goal.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupBarChart(categoryTotals: List<CategoryTotal>) {
        val entries = ArrayList<BarEntry>()
        val labels = ArrayList<String>()

        categoryTotals.forEachIndexed { index, total ->
            entries.add(BarEntry(index.toFloat(), total.totalAmount.toFloat()))
            labels.add(total.categoryName)
        }

        val dataSet = BarDataSet(entries, "Spending by Category")
        val colors = listOf(
            ContextCompat.getColor(requireContext(), android.R.color.holo_blue_dark),
            ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark),
            ContextCompat.getColor(requireContext(), android.R.color.holo_orange_dark),
            ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark),
            ContextCompat.getColor(requireContext(), android.R.color.holo_purple),
        )
        dataSet.colors = colors
        dataSet.valueTextSize = 12f

        val barData = BarData(dataSet)
        barChart.data = barData
        barChart.description.isEnabled = false
        barChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        barChart.xAxis.labelRotationAngle = -45f
        barChart.xAxis.textSize = 10f
        barChart.axisLeft.textSize = 12f
        barChart.axisRight.isEnabled = false
        barChart.invalidate()
    }

    private fun loadUserPointsAndBadges() {
        userPoints = db.getUserPoints(userId)
        userBadges.clear()
        userBadges.addAll(db.getUserBadges(userId))

        tvPoints.text = "⭐ $userPoints points"
        tvBadges.text = if (userBadges.isEmpty()) "No badges yet" else userBadges.joinToString(" | ")
    }

    private fun showExpenseDetails(expense: Expense) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Expense Details")
            .setMessage(
                """
                Category: ${expense.categoryName}
                Amount: ${currencySymbol}${String.format(Locale.getDefault(), "%.2f", expense.amount)}
                Date: ${expense.date}
                Time: ${expense.startTime} - ${expense.endTime}
                Description: ${expense.description}
                """.trimIndent()
            )
            .setPositiveButton("OK", null)
            .show()
    }
}