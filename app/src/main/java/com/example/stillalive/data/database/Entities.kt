package com.example.stillalive.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sign_records")
data class SignRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long,
    val date: String, // YYYY-MM-DD
    val type: String // "MANUAL" or "AUTO"
)

@Entity(tableName = "sms_records")
data class SmsRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long,
    val contactName: String,
    val phoneNumber: String,
    val content: String,
    val status: String // "SENT" or "FAILED"
)
