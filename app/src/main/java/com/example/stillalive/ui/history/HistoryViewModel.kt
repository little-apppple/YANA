package com.example.stillalive.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.stillalive.StillAliveApp
import com.example.stillalive.data.database.SignDao

class HistoryViewModel(
    signDao: SignDao,
    smsDao: com.example.stillalive.data.database.SmsDao
) : ViewModel() {
    val signRecords = signDao.getAll()
    val smsRecords = smsDao.getAll()

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as StillAliveApp)
                HistoryViewModel(
                    app.database.signDao(),
                    app.database.smsDao()
                )
            }
        }
    }
}
