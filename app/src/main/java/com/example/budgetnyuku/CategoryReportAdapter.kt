package com.example.budgetnyuku

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.NumberFormat
import java.util.*

class CategoryReportAdapter(
    private val categoryTotals: List<CategoryTotal>
) : RecyclerView.Adapter<CategoryReportAdapter.ReportViewHolder>() {

    class ReportViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val colorIndicator: View = itemView.findViewById(R.id.viewColorIndicator)
        val tvCategoryName: TextView = itemView.findViewById(R.id.tvCategoryName)
        val tvAmount: TextView = itemView.findViewById(R.id.tvAmount)
        val progressBar: ProgressBar = itemView.findViewById(R.id.progressBar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category_report, parent, false)
        return ReportViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        val item = categoryTotals[position]
        holder.tvCategoryName.text = item.categoryName
        holder.colorIndicator.setBackgroundColor(Color.parseColor(item.categoryColor))

        val format = NumberFormat.getCurrencyInstance(Locale.US)
        holder.tvAmount.text = format.format(item.totalAmount)

        val maxTotal = categoryTotals.maxOfOrNull { it.totalAmount } ?: 1.0
        val progress = ((item.totalAmount / maxTotal) * 100).toInt()
        holder.progressBar.progress = progress
    }

    override fun getItemCount() = categoryTotals.size
}