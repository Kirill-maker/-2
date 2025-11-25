package com.example.alarmmath

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timeMillis: Long,
    val enabled: Boolean = true,
    val difficulty: Int = 0  // 0 = EASY, 1 = MEDIUM, 2 = HARD
)
