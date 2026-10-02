package com.example.data.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.MainActivity
import com.example.data.local.FitTrackDatabase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * WorkManager task to check if user has logged their meals for specific times of the day,
 * and post high-priority local notifications if they haven't logged them.
 */
class MealReminderWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val TAG = "MealReminderWorker"
        const val KEY_MEAL_TYPE = "key_meal_type" // "BREAKFAST", "LUNCH", "DINNER", "ANY"
        const val KEY_CHECK_NAME = "key_check_name"
        const val KEY_CUSTOM_TITLE = "key_custom_title"

        const val CHANNEL_ID = "fittrack_meal_reminders_channel"
        const val CHANNEL_NAME = "Meal Logging Reminders"

        const val NOTIF_ID_BREAKFAST = 8001
        const val NOTIF_ID_LUNCH = 8002
        const val NOTIF_ID_DINNER = 8003
        const val NOTIF_ID_GENERIC = 8004
    }

    override suspend fun doWork(): Result {
        val mealType = inputData.getString(KEY_MEAL_TYPE) ?: "ANY"
        val checkName = inputData.getString(KEY_CHECK_NAME) ?: "$mealType Log Check"

        Log.d(TAG, "Executing scheduled WorkManager meal check for: $mealType ($checkName)")

        val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        return try {
            val database = FitTrackDatabase.getInstance(context)
            val todayLogs = database.nutritionDao().getFoodLogsForDateSync("user_default", todayDate)

            val hasLogged = when (mealType.uppercase(Locale.getDefault())) {
                "BREAKFAST" -> todayLogs.any { it.mealType.contains("BREAKFAST", ignoreCase = true) }
                "LUNCH" -> todayLogs.any { it.mealType.contains("LUNCH", ignoreCase = true) }
                "DINNER" -> todayLogs.any { it.mealType.contains("DINNER", ignoreCase = true) }
                "ANY" -> todayLogs.isNotEmpty()
                else -> todayLogs.any { it.mealType.contains(mealType, ignoreCase = true) }
            }

            if (!hasLogged) {
                Log.w(TAG, "No $mealType meal logged for $todayDate. Triggering local notification.")
                sendMealReminderNotification(mealType)
            } else {
                Log.i(TAG, "$mealType meal is already logged for $todayDate (${todayLogs.size} logs found). Notification skipped.")
            }

            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error executing meal reminder check: ${e.message}", e)
            Result.retry()
        }
    }

    private fun sendMealReminderNotification(mealType: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create high-importance notification channel on Android 8.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Daily local alerts to remind you to log breakfast, lunch, and dinner."
                enableVibration(true)
                enableLights(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // PendingIntent to launch app directly to Nutrition screen
        val openNutritionIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("OPEN_SCREEN", "nutrition")
            putExtra("EXTRA_MEAL_TYPE", mealType)
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            mealType.hashCode(),
            openNutritionIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // PendingIntent for Quick Camera Scanner action
        val openCameraIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("OPEN_SCREEN", "camera_capture")
            putExtra("EXTRA_MEAL_TYPE", mealType)
        }
        val cameraPendingIntent = PendingIntent.getActivity(
            context,
            mealType.hashCode() + 100,
            openCameraIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val (title, text, notifId) = when (mealType.uppercase(Locale.getDefault())) {
            "BREAKFAST" -> Triple(
                "🍳 Breakfast Not Logged! (காலை உணவு)",
                "You haven't logged your breakfast yet today. Keep your nutrition goals on track! (காலை உணவைப் பதிவு செய்யவும்)",
                NOTIF_ID_BREAKFAST
            )
            "LUNCH" -> Triple(
                "🥗 Lunch Reminder: No Meal Logged (மதிய உணவு)",
                "Power your afternoon workouts! Don't forget to track your lunch macros. (மதிய உணவு கலோரிகள் பதிவு செய்யவும்)",
                NOTIF_ID_LUNCH
            )
            "DINNER" -> Triple(
                "🍲 Dinner Log Check (இரவு உணவு)",
                "Finish your day strong! Track your dinner to complete your daily protein target. (இரவு உணவை பதிவு செய்யவும்)",
                NOTIF_ID_DINNER
            )
            else -> Triple(
                "🍽️ Meal Reminder: Track Your Diet (மீல் அப்டேட்)",
                "You haven't updated your meals today. Tap here to scan or log your food now! (மீல் அப்டேட் பண்ணுங்க)",
                NOTIF_ID_GENERIC
            )
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(contentPendingIntent)
            .setAutoCancel(true)
            .addAction(android.R.drawable.ic_menu_agenda, "LOG MEAL", contentPendingIntent)
            .addAction(android.R.drawable.ic_menu_camera, "SCAN FOOD", cameraPendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notifId, notification)
        } catch (e: SecurityException) {
            Log.e(TAG, "Notification permission missing: ${e.message}")
        }
    }
}
