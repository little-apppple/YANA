package com.example.stillalive.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import androidx.core.app.NotificationCompat
import android.graphics.BitmapFactory
import com.example.stillalive.MainActivity
import com.example.stillalive.R
import com.example.stillalive.StillAliveApp
import com.example.stillalive.utils.DateUtils
import com.example.stillalive.utils.LocationHelper
import com.example.stillalive.utils.SmsHelper
import com.example.stillalive.worker.CheckInManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class MonitorService : Service() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var checkInManager: CheckInManager
    private var isAutoMode = false
    private var periodicJob: Job? = null
    // Cache for location to use in SMS
    private var lastLocation: String = "正在获取位置..."

    private val screenReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_USER_PRESENT) {
                if (isAutoMode) {
                    serviceScope.launch {
                        checkInManager.checkIn("AUTO")
                    }
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        val app = application as StillAliveApp
        checkInManager = CheckInManager(app.database.signDao())
        
        // Observe settings
        serviceScope.launch {
            app.settingsRepository.isAutoMode.collect { 
                isAutoMode = it
            }
        }

        val filter = IntentFilter(Intent.ACTION_USER_PRESENT)
        registerReceiver(screenReceiver, filter)
        startForegroundService()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startPeriodicTasks()
        return START_STICKY
    }

    private fun startPeriodicTasks() {
        if (periodicJob?.isActive == true) return

        periodicJob = serviceScope.launch {
            while (true) {
                if (isAutoMode) {
                    try {
                        // 1. Update Location (Every hour)
                        updateLocation()
                        
                        // 2. Check Safety Status
                        checkSafety()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                // Check every hour
                delay(60 * 60 * 1000L)
            }
        }
    }

    private suspend fun updateLocation() {
        try {
            // Update local cache
            lastLocation = LocationHelper.getCurrentLocation(applicationContext)
        } catch (e: Exception) {
            lastLocation = "定位获取失败: ${e.message}"
        }
    }

    private suspend fun checkSafety() {
        val app = application as StillAliveApp
        val signDao = app.database.signDao()
        val settings = app.settingsRepository

        val lastSign = signDao.getLastSign() ?: return
        val thresholdDays = settings.alertThresholdDays.first()
        val daysDiff = DateUtils.getDaysDifference(lastSign.timestamp)

        if (daysDiff >= thresholdDays) {
            val contacts = settings.emergencyContacts.first()
            val userName = settings.userName.first()
            val contactAddress = settings.contactAddress.first()
            val smsHelper = SmsHelper(applicationContext)
            
            // Check if we already sent SMS today to avoid spamming? 
            // For now, simple implementation as per requirement. 
            // Ideally should check last SMS sent time.
            
            contacts.forEach { contactStr ->
                val parts = contactStr.split("|")
                var name = "联系人"
                var phone = ""
                
                if (parts.size >= 2) {
                    name = parts[0]
                    phone = parts[1]
                } else if (parts.isNotEmpty()) {
                    phone = parts[0]
                }
                
                if (phone.isNotBlank()) {
                    val finalUserName = if (userName.isBlank()) "您的好友" else userName
                    val finalAddress = if (contactAddress.isBlank()) "未填写" else contactAddress
                    
                    val message = "你的好友：$finalUserName 已经连续 ${daysDiff}天未使用手机，建议回拨或其他方式尽快确认其身体状态。填写的联系地址是 ：$finalAddress，实时定位：$lastLocation"
                    smsHelper.sendSms(phone, message, name)
                }
            }
        }
    }

    private fun startForegroundService() {
        val channelId = "MonitorServiceChannel"
        val channel = NotificationChannel(
            channelId,
            "Safety Monitor Service",
            NotificationManager.IMPORTANCE_LOW
        )
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE
        )

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("呀呐YANA - 安全守护中")
            .setContentText("正在监测手机使用状态以自动签到")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .build()

        startForeground(1, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(screenReceiver)
        } catch (e: Exception) {
            // Receiver might not be registered
        }
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
