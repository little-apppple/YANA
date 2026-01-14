package com.example.stillalive.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.stillalive.StillAliveApp
import com.example.stillalive.data.database.SignDao
import com.example.stillalive.data.repository.SettingsRepository
import com.example.stillalive.utils.DateUtils
import com.example.stillalive.worker.CheckInManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class HomeUiState(
    val userName: String = "",
    val welcomeMessage: String = "",
    val isCheckedIn: Boolean = false,
    val lastCheckInTime: Long = 0,
    val totalCheckInDays: Int = 0,
    val consecutiveCheckInDays: Int = 0
)

class HomeViewModel(
    private val signDao: SignDao,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    private val checkInManager = CheckInManager(signDao)

    init {
        viewModelScope.launch {
            combine(
                settingsRepository.userName,
                settingsRepository.welcomeMessage,
                signDao.getAll()
            ) { userName, message, records ->
                val todayStr = DateUtils.getCurrentDate()
                val todayRecord = records.find { it.date == todayStr }
                val lastRecord = records.firstOrNull()
                
                // Calculate stats
                val uniqueDates = records.map { it.date }.distinct().sortedDescending()
                val totalDays = uniqueDates.size
                
                var consecutiveDays = 0
                if (uniqueDates.isNotEmpty()) {
                    // Use java.time for date calculation (MinSDK 26+)
                    val today = java.time.LocalDate.now()
                    val isTodayCheckedIn = todayRecord != null
                    
                    // Start checking from today if checked in, otherwise yesterday
                    var checkDate = if (isTodayCheckedIn) today else today.minusDays(1)
                    
                    for (i in 0 until uniqueDates.size) { // safe limit
                        val dateStr = checkDate.toString()
                        if (uniqueDates.contains(dateStr)) {
                            consecutiveDays++
                            checkDate = checkDate.minusDays(1)
                        } else {
                            break
                        }
                    }
                }
                
                HomeUiState(
                    userName = userName,
                    welcomeMessage = message,
                    isCheckedIn = todayRecord != null,
                    lastCheckInTime = lastRecord?.timestamp ?: 0,
                    totalCheckInDays = totalDays,
                    consecutiveCheckInDays = consecutiveDays
                )
            }.collect {
                _uiState.value = it
            }
        }
    }

    fun checkIn() {
        viewModelScope.launch {
            checkInManager.checkIn("MANUAL")
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as StillAliveApp)
                HomeViewModel(
                    signDao = app.database.signDao(),
                    settingsRepository = app.settingsRepository
                )
            }
        }
    }
}
