package com.example.alarmmath

import android.content.Context
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class AlarmRepository private constructor(
    private val dao: AlarmDao,
    private val context: Context,
    private val io: CoroutineDispatcher = Dispatchers.IO
) {
    val alarms: Flow<List<AlarmEntity>> = dao.observeAll()

    suspend fun add(timeMillis: Long, difficulty: Int = 0) = withContext(io) {
        val id = dao.insert(AlarmEntity(timeMillis = timeMillis, difficulty = difficulty))
        schedule(id.toInt(), timeMillis, difficulty)
    }

    suspend fun toggle(alarm: AlarmEntity, enabled: Boolean) = withContext(io) {
        val updated = alarm.copy(enabled = enabled)
        dao.update(updated)
        if (enabled) {
            schedule(updated.id, updated.timeMillis, updated.difficulty)
        } else {
            cancel(updated.id)
        }
    }

    suspend fun delete(alarm: AlarmEntity) = withContext(io) {
        dao.delete(alarm)
        cancel(alarm.id)
    }

    private fun schedule(requestCode: Int, time: Long, difficulty: Int) {
        AlarmScheduler.scheduleExactRtc(context, time, requestCode, difficulty)
    }

    private fun cancel(requestCode: Int) {
        AlarmScheduler.cancel(context, requestCode)
    }

    companion object {
        @Volatile private var INSTANCE: AlarmRepository? = null
        fun get(context: Context): AlarmRepository = INSTANCE ?: synchronized(this) {
            val db = AlarmDatabase.get(context)
            AlarmRepository(db.alarmDao(), context).also { INSTANCE = it }
        }
    }
}
