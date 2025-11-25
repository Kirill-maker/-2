package com.example.alarmmath

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AlarmDao {
    @Query("SELECT * FROM alarms ORDER BY timeMillis")
    fun observeAll(): Flow<List<AlarmEntity>>

    @Insert
    suspend fun insert(alarm: AlarmEntity): Long

    @Update
    suspend fun update(alarm: AlarmEntity): Int

    @Delete
    suspend fun delete(alarm: AlarmEntity): Int
}
