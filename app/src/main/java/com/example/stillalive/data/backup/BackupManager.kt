package com.example.stillalive.data.backup

import android.content.Context
import android.net.Uri
import com.example.stillalive.data.database.AppDatabase
import com.example.stillalive.data.database.SignRecord
import com.example.stillalive.data.database.SmsRecord
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

data class BackupData(
    val signRecords: List<SignRecord>,
    val smsRecords: List<SmsRecord>
)

class BackupManager(private val context: Context, private val database: AppDatabase) {
    private val gson = Gson()

    suspend fun exportData(uri: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            val signRecords = database.signDao().getAll().first()
            val smsRecords = database.smsDao().getAll().first()
            val backupData = BackupData(signRecords, smsRecords)
            val json = gson.toJson(backupData)

            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(json.toByteArray())
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun importData(uri: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            val stringBuilder = StringBuilder()
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    var line: String? = reader.readLine()
                    while (line != null) {
                        stringBuilder.append(line)
                        line = reader.readLine()
                    }
                }
            }
            val json = stringBuilder.toString()
            val backupData = gson.fromJson(json, BackupData::class.java)

            // Restore data
            backupData.signRecords.forEach { 
                // Reset ID to 0 to auto-generate new ID and avoid conflict
                database.signDao().insert(it.copy(id = 0)) 
            }
            backupData.smsRecords.forEach { 
                database.smsDao().insert(it.copy(id = 0)) 
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
