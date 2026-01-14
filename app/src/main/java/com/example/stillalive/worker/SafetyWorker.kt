package com.example.stillalive.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.stillalive.StillAliveApp
import com.example.stillalive.utils.DateUtils
import com.example.stillalive.utils.LocationHelper
import com.example.stillalive.utils.SmsHelper
import kotlinx.coroutines.flow.first

import kotlinx.coroutines.withTimeout

class SafetyWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val app = applicationContext as StillAliveApp
        val signDao = app.database.signDao()
        val settings = app.settingsRepository

        val lastSign = signDao.getLastSign() ?: return Result.success()
        val thresholdDays = settings.alertThresholdDays.first()
        val daysDiff = DateUtils.getDaysDifference(lastSign.timestamp)

        if (daysDiff >= thresholdDays) {
            val contacts = settings.emergencyContacts.first()
            val userName = settings.userName.first()
            val contactAddress = settings.contactAddress.first()
            val smsHelper = SmsHelper(applicationContext)
            
            // Try to get location (SafetyWorker will rely on LocationHelper's internal timeout/fallback)
            val currentLocation = LocationHelper.getCurrentLocation(applicationContext)
            
            contacts.forEach { contactStr ->
                val parts = contactStr.split("|")
                var name = "联系人"
                var phone = ""
                
                if (parts.size >= 2) {
                    name = parts[0]
                    phone = parts[1]
                } else if (parts.isNotEmpty()) {
                    // Fallback: assume the whole string is the phone number
                    phone = parts[0]
                }
                
                if (phone.isNotBlank()) {
                    val finalUserName = if (userName.isBlank()) "您的好友" else userName
                    val finalAddress = if (contactAddress.isBlank()) "未填写" else contactAddress
                    
                    val message = "你的好友：$finalUserName 已经连续 ${daysDiff}天未使用手机，建议回拨或其他方式尽快确认其身体状态。填写的联系地址是 ：$finalAddress，实时定位：$currentLocation"
                    smsHelper.sendSms(phone, message, name)
                }
            }
        }
        return Result.success()
    }
}
