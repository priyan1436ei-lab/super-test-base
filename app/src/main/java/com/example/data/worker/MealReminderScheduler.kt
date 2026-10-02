package com.example.data.worker

import android.content.Context
import android.util.Log
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * MealReminderScheduler handles scheduling, cancelling, and triggering WorkManager
 * periodic and one-time tasks for local meal alert notifications.
 */
class MealReminderScheduler(private val context: Context) {

    companion object {
        const val TAG = "MealReminderScheduler"

        const val WORK_BREAKFAST = "work_meal_reminder_breakfast"
        const val WORK_LUNCH = "work_meal_reminder_lunch"
        const val WORK_DINNER = "work_meal_reminder_dinner"
        const val WORK_IMMEDIATE = "work_meal_reminder_immediate"

        const val DEFAULT_BREAKFAST_HOUR = 10
        const val DEFAULT_BREAKFAST_MIN = 0

        const val DEFAULT_LUNCH_HOUR = 14
        const val DEFAULT_LUNCH_MIN = 30

        const val DEFAULT_DINNER_HOUR = 21
        const val DEFAULT_DINNER_MIN = 0
    }

    private val prefs = context.getSharedPreferences("meal_reminder_prefs", Context.MODE_PRIVATE)
    private val workManager = WorkManager.getInstance(context)

    private val _breakfastEnabled = MutableStateFlow(prefs.getBoolean("breakfast_enabled", true))
    val breakfastEnabled: StateFlow<Boolean> = _breakfastEnabled.asStateFlow()

    private val _lunchEnabled = MutableStateFlow(prefs.getBoolean("lunch_enabled", true))
    val lunchEnabled: StateFlow<Boolean> = _lunchEnabled.asStateFlow()

    private val _dinnerEnabled = MutableStateFlow(prefs.getBoolean("dinner_enabled", true))
    val dinnerEnabled: StateFlow<Boolean> = _dinnerEnabled.asStateFlow()

    private val _lastActionStatus = MutableStateFlow<String?>("WorkManager Engine: Ready")
    val lastActionStatus: StateFlow<String?> = _lastActionStatus.asStateFlow()

    /**
     * Initializes and schedules all enabled default daily meal reminders.
     */
    fun scheduleDefaultReminders() {
        if (_breakfastEnabled.value) {
            scheduleDailyMealCheck(
                mealType = "BREAKFAST",
                hour = DEFAULT_BREAKFAST_HOUR,
                minute = DEFAULT_BREAKFAST_MIN,
                workName = WORK_BREAKFAST
            )
        }
        if (_lunchEnabled.value) {
            scheduleDailyMealCheck(
                mealType = "LUNCH",
                hour = DEFAULT_LUNCH_HOUR,
                minute = DEFAULT_LUNCH_MIN,
                workName = WORK_LUNCH
            )
        }
        if (_dinnerEnabled.value) {
            scheduleDailyMealCheck(
                mealType = "DINNER",
                hour = DEFAULT_DINNER_HOUR,
                minute = DEFAULT_DINNER_MIN,
                workName = WORK_DINNER
            )
        }
        _lastActionStatus.value = "WorkManager: Active for Breakfast (10:00 AM), Lunch (02:30 PM), Dinner (09:00 PM)"
    }

    /**
     * Schedules a 24-hour repeating WorkManager task with initial delay matching the target hour and minute.
     */
    fun scheduleDailyMealCheck(
        mealType: String,
        hour: Int,
        minute: Int,
        workName: String
    ) {
        val initialDelayMs = calculateDelayToNext(hour, minute)

        val inputData = Data.Builder()
            .putString(MealReminderWorker.KEY_MEAL_TYPE, mealType)
            .putString(MealReminderWorker.KEY_CHECK_NAME, "$mealType at ${String.format("%02d:%02d", hour, minute)}")
            .build()

        val periodicWork = PeriodicWorkRequestBuilder<MealReminderWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(initialDelayMs, TimeUnit.MILLISECONDS)
            .setInputData(inputData)
            .addTag("meal_reminder")
            .addTag("meal_$mealType")
            .build()

        workManager.enqueueUniquePeriodicWork(
            workName,
            ExistingPeriodicWorkPolicy.UPDATE,
            periodicWork
        )

        val delayHours = TimeUnit.MILLISECONDS.toHours(initialDelayMs)
        val delayMinutes = TimeUnit.MILLISECONDS.toMinutes(initialDelayMs) % 60
        Log.i(TAG, "Scheduled WorkManager $mealType reminder in $delayHours h $delayMinutes m (Daily repeat)")
    }

    /**
     * Triggers an immediate one-time WorkManager evaluation to test notification delivery right now.
     */
    fun triggerImmediateTestCheck(mealType: String = "BREAKFAST") {
        val inputData = Data.Builder()
            .putString(MealReminderWorker.KEY_MEAL_TYPE, mealType)
            .putString(MealReminderWorker.KEY_CHECK_NAME, "Immediate Test Check ($mealType)")
            .build()

        val oneTimeWork = OneTimeWorkRequestBuilder<MealReminderWorker>()
            .setInputData(inputData)
            .addTag("meal_reminder_test")
            .build()

        workManager.enqueueUniqueWork(
            WORK_IMMEDIATE + "_" + mealType,
            ExistingWorkPolicy.REPLACE,
            oneTimeWork
        )

        _lastActionStatus.value = "⚡ Triggered immediate WorkManager check for $mealType!"
        Log.i(TAG, "Triggered immediate WorkManager meal check task for $mealType")
    }

    fun toggleMealReminder(mealType: String, enabled: Boolean) {
        when (mealType.uppercase()) {
            "BREAKFAST" -> {
                _breakfastEnabled.value = enabled
                prefs.edit().putBoolean("breakfast_enabled", enabled).apply()
                if (enabled) {
                    scheduleDailyMealCheck("BREAKFAST", DEFAULT_BREAKFAST_HOUR, DEFAULT_BREAKFAST_MIN, WORK_BREAKFAST)
                } else {
                    workManager.cancelUniqueWork(WORK_BREAKFAST)
                }
            }
            "LUNCH" -> {
                _lunchEnabled.value = enabled
                prefs.edit().putBoolean("lunch_enabled", enabled).apply()
                if (enabled) {
                    scheduleDailyMealCheck("LUNCH", DEFAULT_LUNCH_HOUR, DEFAULT_LUNCH_MIN, WORK_LUNCH)
                } else {
                    workManager.cancelUniqueWork(WORK_LUNCH)
                }
            }
            "DINNER" -> {
                _dinnerEnabled.value = enabled
                prefs.edit().putBoolean("dinner_enabled", enabled).apply()
                if (enabled) {
                    scheduleDailyMealCheck("DINNER", DEFAULT_DINNER_HOUR, DEFAULT_DINNER_MIN, WORK_DINNER)
                } else {
                    workManager.cancelUniqueWork(WORK_DINNER)
                }
            }
        }
        _lastActionStatus.value = "$mealType reminder ${if (enabled) "Enabled (Scheduled)" else "Disabled (Cancelled)"}"
    }

    /**
     * Computes the millisecond duration until the specified hour and minute.
     * If the time today has already elapsed, computes delay until tomorrow at that time.
     */
    private fun calculateDelayToNext(hour: Int, minute: Int): Long {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (target.before(now)) {
            target.add(Calendar.DAY_OF_YEAR, 1)
        }

        return (target.timeInMillis - now.timeInMillis).coerceAtLeast(0L)
    }

    fun cancelAllMealReminders() {
        workManager.cancelUniqueWork(WORK_BREAKFAST)
        workManager.cancelUniqueWork(WORK_LUNCH)
        workManager.cancelUniqueWork(WORK_DINNER)
        _lastActionStatus.value = "All WorkManager meal reminders paused."
    }
}
