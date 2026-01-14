package com.example.stillalive.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {

    companion object {
        val USER_NAME = stringPreferencesKey("user_name")
        val WELCOME_MESSAGE = stringPreferencesKey("welcome_message")
        // Stored as JSON string list: ["Alice|123456", "Bob|987654"]
        val EMERGENCY_CONTACTS = stringPreferencesKey("emergency_contacts")
        val ALERT_THRESHOLD_DAYS = intPreferencesKey("alert_threshold_days")
        val IS_AUTO_MODE = booleanPreferencesKey("is_auto_mode")
        val CONTACT_ADDRESS = stringPreferencesKey("contact_address")
    }

    val userName: Flow<String> = context.dataStore.data.map { it[USER_NAME] ?: "User" }
    val welcomeMessage: Flow<String> = context.dataStore.data.map { it[WELCOME_MESSAGE] ?: "又活了一天，我真棒！！" }
    val emergencyContacts: Flow<List<String>> = context.dataStore.data.map { 
        val set = it[EMERGENCY_CONTACTS]
        if (set.isNullOrEmpty()) emptyList() else set.split(",")
    }
    val alertThresholdDays: Flow<Int> = context.dataStore.data.map { it[ALERT_THRESHOLD_DAYS] ?: 3 }
    val isAutoMode: Flow<Boolean> = context.dataStore.data.map { it[IS_AUTO_MODE] ?: false }
    val contactAddress: Flow<String> = context.dataStore.data.map { it[CONTACT_ADDRESS] ?: "" }

    suspend fun saveUserName(name: String) {
        context.dataStore.edit { it[USER_NAME] = name }
    }
    
    suspend fun saveWelcomeMessage(msg: String) {
        context.dataStore.edit { it[WELCOME_MESSAGE] = msg }
    }

    suspend fun saveEmergencyContacts(contacts: List<String>) {
        context.dataStore.edit { 
            it[EMERGENCY_CONTACTS] = contacts.joinToString(",")
        }
    }

    suspend fun saveAlertThreshold(days: Int) {
        context.dataStore.edit { it[ALERT_THRESHOLD_DAYS] = days }
    }

    suspend fun saveAutoMode(isAuto: Boolean) {
        context.dataStore.edit { it[IS_AUTO_MODE] = isAuto }
    }

    suspend fun saveContactAddress(address: String) {
        context.dataStore.edit { it[CONTACT_ADDRESS] = address }
    }
}
