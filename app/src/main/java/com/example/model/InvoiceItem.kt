package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "invoices")
data class InvoiceItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val invoiceNumber: String,
    val invoiceDate: String, // Format: YYYY-MM-DD
    val clientName: String,
    val channelName: String = "",
    val billingPeriod: String, // e.g. "September 2026"
    val subtotal: Double = 0.0,
    val totalAmount: Double = 0.0,
    val totalMinutes: Double = 0.0,
    val projectCount: Int = 0,
    val paymentStatus: String = STATUS_UNPAID, // "Unpaid", "Partially Paid", "Paid"
    val notes: String = "",
    val itemsJson: String = "[]", // JSON array of billed work items
    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val STATUS_UNPAID = "Unpaid"
        const val STATUS_PARTIALLY_PAID = "Partially Paid"
        const val STATUS_PAID = "Paid"
    }
}
