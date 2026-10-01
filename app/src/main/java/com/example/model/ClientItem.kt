package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clients")
data class ClientItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val companyOrChannel: String = "",
    val notes: String = "",
    val email: String = "",
    val phone: String = "",
    val defaultRate: Double? = null,
    val createdAt: Long = System.currentTimeMillis()
)
