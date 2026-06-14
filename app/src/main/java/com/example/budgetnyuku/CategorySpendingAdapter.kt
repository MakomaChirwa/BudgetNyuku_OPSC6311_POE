package com.example.budgetnyuku

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.RecyclerView
import java.util.*

class CategorySpendingAdapter(
    private val categoryTotals: List<CategoryTotal>,
    private val currencySymbol: String
) : RecyclerView.Adapter<CategorySpendingAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val viewColor: View = itemView.findViewById(R.id.viewColor)
        val tvName: TextView = itemView.findViewById(R.id.tvCategoryName)
        val tvAmount: TextView = itemView.findViewById(R.id.tvAmount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_category_spending, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = categoryTotals[position]
        holder.tvName.text = item.categoryName
        holder.tvAmount.text = String.format(Locale.getDefault(), "%s%.2f", currencySymbol, item.totalAmount)
        holder.viewColor.setBackgroundColor(item.categoryColor.toColorInt())
    }

    override fun getItemCount() = categoryTotals.size
}
