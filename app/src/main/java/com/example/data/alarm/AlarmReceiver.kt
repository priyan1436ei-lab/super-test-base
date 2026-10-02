package com.example.data.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.data.model.FitnessAlarm
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class AlarmReceiver : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "fittrack_fitness_alarms"
        const val CHANNEL_NAME = "FitTrack AI Alarms & Reminders"

        // Global SharedFlow for in-app real-time alarm ringing popups
        private val _alarmTriggerFlow = MutableSharedFlow<FitnessAlarm>(extraBufferCapacity = 5)
        val alarmTriggerFlow = _alarmTriggerFlow.asSharedFlow()
    }

    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getStringExtra(AlarmScheduler.EXTRA_ALARM_ID) ?: "alarm_unknown"
        val title = intent.getStringExtra(AlarmScheduler.EXTRA_TITLE) ?: "Fitness Reminder"
        val tamilLabel = intent.getStringExtra(AlarmScheduler.EXTRA_TAMIL_LABEL) ?: ""
        val category = intent.getStringExtra(AlarmScheduler.EXTRA_CATEGORY) ?: "WORKOUT"
        val targetValue = intent.getStringExtra(AlarmScheduler.EXTRA_TARGET_VALUE) ?: ""
        val soundTone = intent.getStringExtra(AlarmScheduler.EXTRA_SOUND_TONE) ?: "Cyber Chime"
        val vibrate = intent.getBooleanExtra(AlarmScheduler.EXTRA_VIBRATE, true)

        Log.d("AlarmReceiver", "Alarm fired! Id=$alarmId, Title=$title, Category=$category")

        val triggeredAlarm = FitnessAlarm(
            alarmId = alarmId,
            title = title,
            labelTamil = tamilLabel,
            category = category,
            targetValue = targetValue,
            soundTone = soundTone,
            vibrate = vibrate,
            lastTriggeredAt = System.currentTimeMillis()
        )

        // 1. Emit to in-app real-time flow
        _alarmTriggerFlow.tryEmit(triggeredAlarm)

        // 2. Play vibration if enabled
        if (vibrate) {
            triggerVibration(context)
        }

        // 3. Post system notification
        postNotification(context, triggeredAlarm)
    }

    private fun triggerVibration(context: Context) {
        try {
            val vibrator = ContextCompat.getSystemService(context, Vibrator::class.java)
            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val timings = longArrayOf(0, 400, 200, 400, 200, 600)
                    val amplitudes = intArrayOf(0, 255, 0, 255, 0, 255)
                    vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(1000)
                }
            }
        } catch (e: Exception) {
            Log.e("AlarmReceiver", "Error vibrating: ${e.message}")
        }
    }

    private fun postNotification(context: Context, alarm: FitnessAlarm) {
        val notificationManager = ContextCompat.getSystemService(context, NotificationManager::class.java)
            ?: return

        // Create Channel on Android O+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Real-time alerts for workouts, hydration, meals, and sleep routines"
                enableLights(true)
                lightColor = Color.GREEN
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 400, 200, 400, 200, 600)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Tap action -> Open MainActivity
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("OPEN_SCREEN", "alarms")
            putExtra("TRIGGERED_ALARM_ID", alarm.alarmId)
        }
        val pendingOpenIntent = PendingIntent.getActivity(
            context,
            alarm.alarmId.hashCode(),
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val categoryIconEmoji = when (alarm.category.uppercase()) {
            "WORKOUT" -> "⚡"
            "HYDRATION" -> "💧"
            "MEAL" -> "🥗"
            "SUPPLEMENT" -> "🥤"
            "SLEEP" -> "🌙"
            else -> "⏰"
        }

        val subText = if (alarm.labelTamil.isNotBlank()) {
            "${alarm.labelTamil} • ${alarm.targetValue}"
        } else {
            alarm.targetValue.ifBlank { "FitTrack Daily Routine" }
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("$categoryIconEmoji ${alarm.title}")
            .setContentText(subText)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("${alarm.title}\n${if (alarm.labelTamil.isNotBlank()) "${alarm.labelTamil}\n" else ""}Target: ${alarm.targetValue.ifBlank { "Stay consistent with your fitness routine!" }}")
            )
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setSound(soundUri)
            .setContentIntent(pendingOpenIntent)
            .addAction(
                android.R.drawable.ic_menu_today,
                "OPEN FITTRACK",
                pendingOpenIntent
            )
            .build()

        try {
            notificationManager.notify(alarm.alarmId.hashCode(), notification)
        } catch (e: SecurityException) {
            Log.e("AlarmReceiver", "Notification permission denied: ${e.message}")
        }
    }
}
