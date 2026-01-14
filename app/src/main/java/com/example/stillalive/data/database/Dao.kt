package com.example.stillalive.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SignDao {
    @Query("SELECT * FROM sign_records ORDER BY timestamp DESC")
    fun getAll(): Flow<List<SignRecord>>

    @Query("SELECT * FROM sign_records WHERE date = :date LIMIT 1")
    suspend fun getByDate(date: String): SignRecord?

    @Query("SELECT * FROM sign_records ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastSign(): SignRecord?

    @Insert
    suspend fun insert(record: SignRecord)
}

@Dao
interface SmsDao {
    @Query("SELECT * FROM sms_records ORDER BY timestamp DESC")
    fun getAll(): Flow<List<SmsRecord>>

    @Insert
    suspend fun insert(record: SmsRecord)
}
