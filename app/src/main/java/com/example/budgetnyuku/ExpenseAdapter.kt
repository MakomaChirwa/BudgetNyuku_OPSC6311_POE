package com.example.budgetnyuku

import androidx.core.graphics.toColorInt
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import java.util.*

class ExpenseAdapter(
    private val expenses: List<Expense>,
    private val currencySymbol: String = "R",
    private val onItemClick: (Expense) -> Unit
) : RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {

    class ExpenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardView: CardView = itemView.findViewById(R.id.cardExpense)
        val colorIndicator: View = itemView.findViewById(R.id.viewColorIndicator)
        val tvCategory: TextView = itemView.findViewById(R.id.tvCategory)
        val tvAmount: TextView = itemView.findViewById(R.id.tvAmount)
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_expense, parent, false)
        return ExpenseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val expense = expenses[position]
        holder.tvCategory.text = expense.categoryName
        holder.colorIndicator.setBackgroundColor(expense.categoryColor.toColorInt())

        holder.tvAmount.text = String.format(Locale.getDefault(), "%s%.2f", currencySymbol, expense.amount)
        holder.tvDate.text = expense.date
        holder.tvDescription.text = expense.description.ifEmpty { "No description" }

        holder.cardView.setOnClickListener { onItemClick(expense) }
    }

    override fun getItemCount() = expenses.size
}