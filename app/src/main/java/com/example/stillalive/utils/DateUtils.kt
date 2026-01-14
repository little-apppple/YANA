package com.example.stillalive.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateUtils {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    fun getCurrentDate(): String {
        return dateFormat.format(Date())
    }

    fun formatDateTime(timestamp: Long): String {
        return timeFormat.format(Date(timestamp))
    }
    
    fun getDaysDifference(lastTimestamp: Long): Int {
        val diff = System.currentTimeMillis() - lastTimestamp
        return (diff / (1000 * 60 * 60 * 24)).toInt()
    }
}
