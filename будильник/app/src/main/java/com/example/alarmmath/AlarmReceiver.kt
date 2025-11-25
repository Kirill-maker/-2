package com.example.alarmmath

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val difficulty = intent?.getIntExtra(AlarmScheduler.EXTRA_DIFFICULTY, 0) ?: 0
        
        // Запускаем foreground service со звуком
        AlarmService.start(context)
        
        // Запускаем Activity для решения интегралов
        val activityIntent = Intent(context, AlarmActivity::class.java).apply {
            addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_SINGLE_TOP
            )
            putExtra(AlarmScheduler.EXTRA_DIFFICULTY, difficulty)
        }
        context.startActivity(activityIntent)
    }
}
