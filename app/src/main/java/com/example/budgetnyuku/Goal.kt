package com.example.budgetnyuku

data class Goal(
    val id: Int = 0,
    val userId: Int,
    val name: String,
    val targetAmount: Double,
    var currentAmount: Double = 0.0,
    val deadline: String
)
