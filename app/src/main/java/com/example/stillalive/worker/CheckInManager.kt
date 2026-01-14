package com.example.stillalive.worker

import com.example.stillalive.data.database.SignDao
import com.example.stillalive.data.database.SignRecord
import com.example.stillalive.utils.DateUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CheckInManager(private val signDao: SignDao) {
    suspend fun checkIn(type: String) {
        val today = DateUtils.getCurrentDate()
        val existing = signDao.getByDate(today)
        if (existing == null) {
            val record = SignRecord(
                timestamp = System.currentTimeMillis(),
                date = today,
                type = type
            )
            signDao.insert(record)
        }
    }
}
