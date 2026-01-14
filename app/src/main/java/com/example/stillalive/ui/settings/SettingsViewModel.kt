package com.example.stillalive.ui.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.stillalive.StillAliveApp
import com.example.stillalive.data.backup.BackupManager
import com.example.stillalive.data.repository.SettingsRepository
import com.example.stillalive.utils.LocationHelper
import com.example.stillalive.utils.SmsHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class SettingsUiState(
    val userName: String = "",
    val welcomeMessage: String = "",
    val emergencyContacts: List<String> = emptyList(), // Format: "Name|Phone"
    val alertThresholdDays: Int = 3,
    val isAutoMode: Boolean = false,
    val contactAddress: String = "",
    val backupMessage: String? = null
)

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val backupManager: BackupManager,
    private val smsHelper: SmsHelper,
    private val context: android.content.Context
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                combine(
                    settingsRepository.userName,
                    settingsRepository.welcomeMessage,
                    settingsRepository.emergencyContacts
                ) { name, welcome, contacts -> Triple(name, welcome, contacts) },
                combine(
                    settingsRepository.alertThresholdDays,
                    settingsRepository.isAutoMode,
                    settingsRepository.contactAddress
                ) { days, auto, address -> Triple(days, auto, address) }
            ) { (name, welcome, contacts), (days, auto, address) ->
                val current = _uiState.value
                SettingsUiState(name, welcome, contacts, days, auto, address, current.backupMessage)
            }.collect {
                _uiState.value = it
            }
        }
    }

    fun updateContactAddress(address: String) = viewModelScope.launch {
        settingsRepository.saveContactAddress(address)
    }

    fun updateUserName(name: String) = viewModelScope.launch {
        settingsRepository.saveUserName(name)
    }

    fun updateWelcomeMessage(msg: String) = viewModelScope.launch {
        settingsRepository.saveWelcomeMessage(msg)
    }

    fun addEmergencyContact(name: String, phone: String) = viewModelScope.launch {
        val currentContacts = _uiState.value.emergencyContacts.toMutableList()
        currentContacts.add("$name|$phone")
        settingsRepository.saveEmergencyContacts(currentContacts)
    }

    fun removeEmergencyContact(index: Int) = viewModelScope.launch {
        val currentContacts = _uiState.value.emergencyContacts.toMutableList()
        if (index in currentContacts.indices) {
            currentContacts.removeAt(index)
            settingsRepository.saveEmergencyContacts(currentContacts)
        }
    }

    fun updateAlertThreshold(days: Int) = viewModelScope.launch {
        settingsRepository.saveAlertThreshold(days)
    }

    fun updateAutoMode(isAuto: Boolean) = viewModelScope.launch {
        settingsRepository.saveAutoMode(isAuto)
    }

    fun exportData(uri: Uri) = viewModelScope.launch {
        val success = backupManager.exportData(uri)
        _uiState.value = _uiState.value.copy(
            backupMessage = if (success) "导出成功" else "导出失败"
        )
    }

    fun importData(uri: Uri) = viewModelScope.launch {
        val success = backupManager.importData(uri)
        _uiState.value = _uiState.value.copy(
            backupMessage = if (success) "导入成功" else "导入失败"
        )
    }
    
    fun clearBackupMessage() {
        _uiState.value = _uiState.value.copy(backupMessage = null)
    }

    fun updateBackupMessage(message: String) {
        _uiState.value = _uiState.value.copy(backupMessage = message)
    }

    fun sendTestSms(userName: String, phone: String, contactName: String) {
        val message = "【测试】这是来自 $userName 的测试短信，用于验证紧急联系人配置是否正常。"
        viewModelScope.launch {
            val success = smsHelper.sendSms(phone, message, contactName)
            _uiState.value = _uiState.value.copy(
                backupMessage = if (success) "测试短信已发送" else "发送失败，请检查权限和余额"
            )
        }
    }
    
    fun simulateEmergency() {
        viewModelScope.launch {
            val contacts = _uiState.value.emergencyContacts
            val userName = _uiState.value.userName
            val contactAddress = _uiState.value.contactAddress
            var sentCount = 0
            
            // Get current location (simulated environment, but try to get real)
            val currentLocation = LocationHelper.getCurrentLocation(context)

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
                    
                    val message = "你的好友：$finalUserName 已经连续 3天未使用手机，建议回拨或其他方式尽快确认其身体状态。填写的联系地址是 ：$finalAddress，实时定位：$currentLocation"
                    val success = smsHelper.sendSms(phone, message, name)
                    if (success) sentCount++
                }
            }
            
            _uiState.value = _uiState.value.copy(
                backupMessage = "已模拟3天未签到，向 ${sentCount} 位联系人发送了报警短信"
            )
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as StillAliveApp)
                SettingsViewModel(
                    app.settingsRepository,
                    BackupManager(app, app.database),
                    SmsHelper(app),
                    app.applicationContext
                )
            }
        }
    }
}
