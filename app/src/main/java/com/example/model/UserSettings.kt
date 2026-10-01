package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_settings")
data class UserSettings(
    @PrimaryKey
    val id: Int = 1,
    val editorName: String = "Freelance Video Editor",
    val editorEmail: String = "editor@example.com",
    val editorPhone: String = "+880 1700-000000",
    val editorAddress: String = "Dhaka, Bangladesh",
    val defaultClientName: String = "",
    val defaultClientEmail: String = "",
    val defaultClientAddress: String = "",
    val defaultRatePerMinute: Double = 60.0,
    val currencySymbol: String = "৳", // BDT ৳ default
    val currencyCode: String = "BDT",
    val showMoney: Boolean = true, // Settings -> Display & Privacy -> Show Money (ON/OFF)
    val reducedMotion: Boolean = false, // Settings -> Display & Privacy -> Reduced Motion (ON/OFF)
    val themeMode: String = "SYSTEM", // "LIGHT", "DARK", "SYSTEM"
    val dateFormat: String = "YYYY-MM-DD",
    val startOfWeek: String = "MONDAY",
    val invoiceTitle: String = "VIDEO EDITING INVOICE",
    val invoicePrefix: String = "INV-",
    val paymentInfo: String = "bKash / Nagad: 01700-000000 (Personal)\nBank: City Bank / Dutch-Bangla Bank\nAccount: 110-234-567890\nRouting: 225272345",
    val invoiceNotes: String = "Thank you for your business! Please settle the payment within 7 days of invoice issuance.",
    val customFieldsSchemaJson: String = "[]"
)
