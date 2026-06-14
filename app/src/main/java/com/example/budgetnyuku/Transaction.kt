package com.example.budgetnyuku

data class Transaction(
    val id: Int = 0,
    val userId: Int,
    val amount: Double,
    val category: String,
    val subcategory: String = "",
    val description: String = "",
    val date: String,
    val type: String,
    val isRecurring: Boolean = false,
    val recurringFrequency: String? = null
)