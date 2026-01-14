package com.example.stillalive.utils

import android.content.Context
import android.telephony.SmsManager
import android.util.Log
import com.example.stillalive.StillAliveApp
import com.example.stillalive.data.database.SmsRecord
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmsHelper(private val context: Context) {
    fun sendSms(phoneNumber: String, message: String, contactName: String = ""): Boolean {
        return try {
            val smsManager = context.getSystemService(SmsManager::class.java)
            val parts = smsManager.divideMessage(message)
            if (parts.size > 1) {
                smsManager.sendMultipartTextMessage(phoneNumber, null, parts, null, null)
            } else {
                smsManager.sendTextMessage(phoneNumber, null, message, null, null)
            }
            
            // Log to database
            val app = context.applicationContext as StillAliveApp
            CoroutineScope(Dispatchers.IO).launch {
                app.database.smsDao().insert(
                    SmsRecord(
                        timestamp = System.currentTimeMillis(),
                        contactName = contactName.ifEmpty { "Unknown" },
                        phoneNumber = phoneNumber,
                        content = message,
                        status = "SENT"
                    )
                )
            }
            
            true
        } catch (e: Exception) {
            Log.e("SmsHelper", "Failed to send SMS", e)
            // Log failure to database
             val app = context.applicationContext as StillAliveApp
            CoroutineScope(Dispatchers.IO).launch {
                app.database.smsDao().insert(
                    SmsRecord(
                        timestamp = System.currentTimeMillis(),
                        contactName = contactName.ifEmpty { "Unknown" },
                        phoneNumber = phoneNumber,
                        content = message,
                        status = "FAILED"
                    )
                )
            }
            false
        }
    }
}
