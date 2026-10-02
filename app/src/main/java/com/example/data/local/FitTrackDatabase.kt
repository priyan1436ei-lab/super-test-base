package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.*

@Database(
    entities = [
        User::class,
        Exercise::class,
        WorkoutPlan::class,
        Workout::class,
        WorkoutSet::class,
        FoodItem::class,
        NutritionLog::class,
        WaterLog::class,
        WeightLog::class,
        Goal::class,
        CalendarEvent::class,
        AIMessage::class,
        Subscription::class,
        FitnessAlarm::class
    ],
    version = 5,
    exportSchema = false
)
abstract class FitTrackDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun exerciseDao(): ExerciseDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun nutritionDao(): NutritionDao
    abstract fun waterDao(): WaterDao
    abstract fun weightDao(): WeightDao
    abstract fun goalDao(): GoalDao
    abstract fun calendarDao(): CalendarDao
    abstract fun aiChatDao(): AiChatDao
    abstract fun subscriptionDao(): SubscriptionDao
    abstract fun alarmDao(): AlarmDao

    companion object {
        @Volatile
        private var INSTANCE: FitTrackDatabase? = null

        fun getInstance(context: Context): FitTrackDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FitTrackDatabase::class.java,
                    "fittrack_ai.db"
                )
                .fallbackToDestructiveMigration(true)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
