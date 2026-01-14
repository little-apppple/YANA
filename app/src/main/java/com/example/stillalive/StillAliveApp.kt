package com.example.stillalive

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.stillalive.data.database.AppDatabase
import com.example.stillalive.data.repository.SettingsRepository
import com.example.stillalive.worker.SafetyWorker
import java.util.concurrent.TimeUnit

class StillAliveApp : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val settingsRepository by lazy { SettingsRepository(this) }
    
    override fun onCreate() {
        super.onCreate()
        setupWorker()
    }

    private fun setupWorker() {
        val workRequest = PeriodicWorkRequestBuilder<SafetyWorker>(1, TimeUnit.DAYS)
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "SafetyCheck",
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }
}
