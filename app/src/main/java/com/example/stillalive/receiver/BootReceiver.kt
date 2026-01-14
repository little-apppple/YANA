package com.example.stillalive.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.stillalive.service.MonitorService

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val serviceIntent = Intent(context, MonitorService::class.java)
            context.startForegroundService(serviceIntent)
        }
    }
}
