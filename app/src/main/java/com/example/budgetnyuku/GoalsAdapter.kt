package com.example.budgetnyuku

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.*

class GoalsAdapter(
    private val goals: List<Goal>,
    private val currencySymbol: String,
    private val onAddFundsClick: (Goal) -> Unit,
    private val onDeleteClick: (Goal) -> Unit
) : RecyclerView.Adapter<GoalsAdapter.GoalViewHolder>() {

    class GoalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvGoalName)
        val tvDeadline: TextView = itemView.findViewById(R.id.tvDeadline)
        val pbGoal: ProgressBar = itemView.findViewById(R.id.pbGoal)
        val tvProgress: TextView = itemView.findViewById(R.id.tvProgress)
        val tvPercent: TextView = itemView.findViewById(R.id.tvPercent)
        val btnAddFunds: Button = itemView.findViewById(R.id.btnAddFunds)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GoalViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_goal, parent, false)
        return GoalViewHolder(view)
    }

    override fun onBindViewHolder(holder: GoalViewHolder, position: Int) {
        val goal = goals[position]
        holder.tvName.text = goal.name
        holder.tvDeadline.text = String.format(Locale.getDefault(), "By: %s", goal.deadline)
        
        val percent = if (goal.targetAmount > 0) ((goal.currentAmount / goal.targetAmount) * 100).toInt().coerceAtMost(100) else 0
        holder.pbGoal.progress = percent
        holder.tvPercent.text = String.format(Locale.getDefault(), "%d%%", percent)
        
        holder.tvProgress.text = String.format(Locale.getDefault(), "%s%.2f / %s%.2f", currencySymbol, goal.currentAmount, currencySymbol, goal.targetAmount)
        
        holder.btnAddFunds.setOnClickListener { onAddFundsClick(goal) }
        holder.btnDelete.setOnClickListener { onDeleteClick(goal) }
    }

    override fun getItemCount() = goals.size
}
