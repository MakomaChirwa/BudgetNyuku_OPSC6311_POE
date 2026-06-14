package com.example.budgetnyuku

import java.util.Arrays

data class Expense(
    val id: Int,
    val userId: Int,
    val categoryId: Int,
    val amount: Double,
    val date: String,
    val startTime: String,
    val endTime: String,
    val description: String,
    val photo: ByteArray? = null,
    val categoryName: String = "",
    val categoryColor: String = ""
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Expense

        if (id != other.id) return false
        if (userId != other.userId) return false
        if (categoryId != other.categoryId) return false
        if (amount != other.amount) return false
        if (date != other.date) return false
        if (startTime != other.startTime) return false
        if (endTime != other.endTime) return false
        if (description != other.description) return false
        if (photo != null) {
            if (other.photo == null) return false
            if (!photo.contentEquals(other.photo)) return false
        } else if (other.photo != null) return false
        if (categoryName != other.categoryName) return false
        if (categoryColor != other.categoryColor) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + userId
        result = 31 * result + categoryId
        result = 31 * result + amount.hashCode()
        result = 31 * result + date.hashCode()
        result = 31 * result + startTime.hashCode()
        result = 31 * result + endTime.hashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + (photo?.contentHashCode() ?: 0)
        result = 31 * result + categoryName.hashCode()
        result = 31 * result + categoryColor.hashCode()
        return result
    }
}
