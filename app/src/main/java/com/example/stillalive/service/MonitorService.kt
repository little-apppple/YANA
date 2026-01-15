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
import com.example.stillalive.worker.CheckInManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class MonitorService : Service() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var checkInManager: CheckInManager
    private var isAutoMode = false

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
