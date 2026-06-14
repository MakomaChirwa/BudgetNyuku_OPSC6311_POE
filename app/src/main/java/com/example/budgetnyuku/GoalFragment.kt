package com.example.budgetnyuku

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.*

class GoalFragment : Fragment() {

    private lateinit var db: DatabaseHelper
    private var userId: Int = -1
    private lateinit var adapter: GoalsAdapter
    private val goals = mutableListOf<Goal>()
    private var currencySymbol = "R"

    companion object {
        fun newInstance(userId: Int): GoalFragment {
            val fragment = GoalFragment()
            val args = Bundle()
            args.putInt("USER_ID", userId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_goals, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = DatabaseHelper(requireContext())
        userId = arguments?.getInt("USER_ID") ?: -1
        currencySymbol = db.getCurrencySymbol(userId)

        val rvGoals = view.findViewById<RecyclerView>(R.id.rvGoals)
        rvGoals.layoutManager = LinearLayoutManager(requireContext())
        adapter = GoalsAdapter(goals, currencySymbol, { goal ->
            showAddFundsDialog(goal)
        }, { goal ->
            showDeleteGoalDialog(goal)
        })
        rvGoals.adapter = adapter

        view.findViewById<FloatingActionButton>(R.id.fabAddGoal).setOnClickListener {
            showAddGoalDialog()
        }

        view.findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            (requireActivity() as? MainActivity)?.findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_navigation)?.selectedItemId = R.id.nav_dashboard
        }

        loadGoals()
    }

    private fun loadGoals() {
        goals.clear()
        goals.addAll(db.getGoals(userId))
        adapter.notifyDataSetChanged()
        
        view?.findViewById<TextView>(R.id.tvNoGoals)?.visibility = if (goals.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun showAddGoalDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_goal, null)
        val etName = dialogView.findViewById<EditText>(R.id.etGoalName)
        val etAmount = dialogView.findViewById<EditText>(R.id.etTargetAmount)
        val etDeadline = dialogView.findViewById<EditText>(R.id.etDeadline)

        val calendar = Calendar.getInstance()
        etDeadline.setOnClickListener {
            DatePickerDialog(requireContext(), { _, year, month, day ->
                val deadline = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, day)
                etDeadline.setText(deadline)
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        AlertDialog.Builder(requireContext())
            .setTitle("New Savings Goal")
            .setView(dialogView)
            .setPositiveButton("Create") { _, _ ->
                val name = etName.text.toString().trim()
                val amount = etAmount.text.toString().toDoubleOrNull() ?: 0.0
                val deadline = etDeadline.text.toString()

                if (name.isNotEmpty() && amount > 0) {
                    if (db.addGoal(userId, name, amount, deadline)) {
                        loadGoals()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showAddFundsDialog(goal: Goal) {
        val etAmount = EditText(requireContext())
        etAmount.hint = "Amount to add"
        etAmount.inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL

        AlertDialog.Builder(requireContext())
            .setTitle("Add Funds to ${goal.name}")
            .setView(etAmount)
            .setPositiveButton("Add") { _, _ ->
                val addAmount = etAmount.text.toString().toDoubleOrNull() ?: 0.0
                if (addAmount > 0) {
                    db.updateGoalProgress(goal.id, goal.currentAmount + addAmount)
                    loadGoals()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDeleteGoalDialog(goal: Goal) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Goal")
            .setMessage("Are you sure you want to delete '${goal.name}'?")
            .setPositiveButton("Delete") { _, _ ->
                if (db.deleteGoal(goal.id)) {
                    loadGoals()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
