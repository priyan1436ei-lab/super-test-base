package com.example.data.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.data.model.FitnessAlarm
import java.util.Calendar

class AlarmScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    companion object {
        const val ACTION_TRIGGER_ALARM = "com.example.fittrack.ACTION_TRIGGER_ALARM"
        const val EXTRA_ALARM_ID = "extra_alarm_id"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_TAMIL_LABEL = "extra_tamil_label"
        const val EXTRA_CATEGORY = "extra_category"
        const val EXTRA_TARGET_VALUE = "extra_target_value"
        const val EXTRA_SOUND_TONE = "extra_sound_tone"
        const val EXTRA_VIBRATE = "extra_vibrate"
    }

    /**
     * Schedules the next occurrence for a given FitnessAlarm
     */
    fun scheduleAlarm(alarm: FitnessAlarm) {
        if (!alarm.isEnabled) {
            cancelAlarm(alarm.alarmId)
            return
        }

        val triggerMillis = calculateNextTriggerMillis(alarm.timeHour, alarm.timeMinute, alarm.daysOfWeek)

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_TRIGGER_ALARM
            putExtra(EXTRA_ALARM_ID, alarm.alarmId)
            putExtra(EXTRA_TITLE, alarm.title)
            putExtra(EXTRA_TAMIL_LABEL, alarm.labelTamil)
            putExtra(EXTRA_CATEGORY, alarm.category)
            putExtra(EXTRA_TARGET_VALUE, alarm.targetValue)
            putExtra(EXTRA_SOUND_TONE, alarm.soundTone)
            putExtra(EXTRA_VIBRATE, alarm.vibrate)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.alarmId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerMillis,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerMillis,
                    pendingIntent
                )
            }
            Log.d("AlarmScheduler", "Alarm scheduled: ${alarm.title} at $triggerMillis")
        } catch (e: SecurityException) {
            Log.e("AlarmScheduler", "Security exception scheduling alarm: ${e.message}")
            try {
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
            } catch (err: Exception) {
                Log.e("AlarmScheduler", "Failed fallback alarm: ${err.message}")
            }
        }
    }

    /**
     * Cancels an existing scheduled alarm
     */
    fun cancelAlarm(alarmId: String) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_TRIGGER_ALARM
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmId.hashCode(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
            Log.d("AlarmScheduler", "Alarm cancelled: $alarmId")
        }
    }

    /**
     * Immediately or shortly (e.g. 2s) triggers a test alarm for real-time demonstration
     */
    fun triggerTestAlarm(alarm: FitnessAlarm, delayMillis: Long = 2000L) {
        val triggerMillis = System.currentTimeMillis() + delayMillis

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_TRIGGER_ALARM
            putExtra(EXTRA_ALARM_ID, alarm.alarmId)
            putExtra(EXTRA_TITLE, alarm.title)
            putExtra(EXTRA_TAMIL_LABEL, alarm.labelTamil)
            putExtra(EXTRA_CATEGORY, alarm.category)
            putExtra(EXTRA_TARGET_VALUE, alarm.targetValue)
            putExtra(EXTRA_SOUND_TONE, alarm.soundTone)
            putExtra(EXTRA_VIBRATE, alarm.vibrate)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ("test_" + alarm.alarmId).hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerMillis,
                pendingIntent
            )
            Log.d("AlarmScheduler", "Test alarm queued in ${delayMillis}ms")
        } catch (e: Exception) {
            Log.e("AlarmScheduler", "Error triggering test alarm: ${e.message}")
        }
    }

    /**
     * Calculates the exact next epoch millis for an alarm given hour, minute, and active days.
     * Active days: 1=Mon, 2=Tue, 3=Wed, 4=Thu, 5=Fri, 6=Sat, 7=Sun
     */
    fun calculateNextTriggerMillis(hour: Int, minute: Int, daysOfWeek: String): Long {
        val activeDays = daysOfWeek.split(",")
            .mapNotNull { it.trim().toIntOrNull() }
            .ifEmpty { listOf(1, 2, 3, 4, 5, 6, 7) }

        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // If target time is earlier today or right now, jump to next day
        if (target.timeInMillis <= now.timeInMillis) {
            target.add(Calendar.DAY_OF_YEAR, 1)
        }

        // Advance until day of week is matched
        for (i in 0..7) {
            val calDay = target.get(Calendar.DAY_OF_WEEK)
            // Convert Calendar.DAY_OF_WEEK (Sun=1, Mon=2..Sat=7) to ISO (Mon=1..Sun=7)
            val isoDay = if (calDay == Calendar.SUNDAY) 7 else calDay - 1
            if (activeDays.contains(isoDay)) {
                break
            }
            target.add(Calendar.DAY_OF_YEAR, 1)
        }

        return target.timeInMillis
    }
}
