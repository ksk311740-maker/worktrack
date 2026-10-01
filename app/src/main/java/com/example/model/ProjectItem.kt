package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String, // Format: YYYY-MM-DD
    val name: String,
    val category: String,
    val minutes: Double,
    val itemsCount: Int = 1,
    val ratePerMinute: Double = 60.0,
    val payment: Double = 0.0,
    val isPaymentOverridden: Boolean = false,
    val startTime: String = "", // e.g., "09:00"
    val endTime: String = "",   // e.g., "11:30"
    val workingHours: Double = 0.0,
    val clientName: String = "",
    val channel: String = "",   // YouTube channel
    val notes: String = "",
    val status: String = STATUS_COMPLETED, // "Completed", "Pending", "Cancelled"
    val paymentStatus: String = PAYMENT_UNPAID, // "Unpaid", "Partially Paid", "Paid"
    val customFieldsJson: String = "{}", // JSON map of key-value pairs
    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val STATUS_COMPLETED = "Completed"
        const val STATUS_PENDING = "Pending"
        const val STATUS_CANCELLED = "Cancelled"

        const val PAYMENT_UNPAID = "Unpaid"
        const val PAYMENT_PARTIALLY_PAID = "Partially Paid"
        const val PAYMENT_PAID = "Paid"
    }
}
