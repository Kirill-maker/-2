package com.example.alarmmath

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.SystemClock
import android.provider.Settings
import android.util.Log

object AlarmScheduler {
    private const val TAG = "AlarmScheduler"
    const val EXTRA_DIFFICULTY = "extra_difficulty"

    fun scheduleExact(context: Context, delayMillis: Long, requestCode: Int = 0, difficulty: Int = 0) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (!canScheduleExact(am)) {
            Log.w(TAG, "Cannot schedule exact alarms, requesting permission")
            requestExactAlarmPermission(context)
            return
        }
        val pi = PendingIntent.getBroadcast(
            context,
            requestCode,
            Intent(context, AlarmReceiver::class.java).apply {
                putExtra(EXTRA_DIFFICULTY, difficulty)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val triggerAt = SystemClock.elapsedRealtime() + delayMillis
        am.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAt, pi)
        Log.d(TAG, "Scheduled alarm $requestCode at elapsed +$delayMillis, difficulty=$difficulty")
    }

    /**
     * Schedule an exact alarm at the specified wall-clock time (RTC_WAKEUP).
     * @param triggerAtMillis epoch millis when the alarm should fire.
     * @param difficulty 0=EASY, 1=MEDIUM, 2=HARD
     */
    fun scheduleExactRtc(context: Context, triggerAtMillis: Long, requestCode: Int = 0, difficulty: Int = 0) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (!canScheduleExact(am)) {
            Log.w(TAG, "Cannot schedule exact alarms, requesting permission")
            requestExactAlarmPermission(context)
            return
        }
        val pi = PendingIntent.getBroadcast(
            context,
            requestCode,
            Intent(context, AlarmReceiver::class.java).apply {
                putExtra(EXTRA_DIFFICULTY, difficulty)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pi)
        Log.d(TAG, "Scheduled alarm $requestCode at $triggerAtMillis, difficulty=$difficulty")
    }

    fun cancel(context: Context, requestCode: Int = 0) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pi = PendingIntent.getBroadcast(
            context,
            requestCode,
            Intent(context, AlarmReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        am.cancel(pi)
        Log.d(TAG, "Cancelled alarm $requestCode")
    }

    private fun canScheduleExact(am: AlarmManager): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            am.canScheduleExactAlarms()
        } else {
            true
        }
    }

    private fun requestExactAlarmPermission(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }
}
