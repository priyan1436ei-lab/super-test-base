package com.example.data.local

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE userId = :userId LIMIT 1")
    fun getUser(userId: String): Flow<User?>

    @Query("SELECT * FROM users WHERE userId = :userId LIMIT 1")
    suspend fun getUserSync(userId: String): User?

    @Transaction
    @Query("SELECT * FROM users WHERE userId = :userId LIMIT 1")
    fun getUserWithWorkouts(userId: String): Flow<UserWithWorkouts?>

    @Transaction
    @Query("SELECT * FROM users WHERE userId = :userId LIMIT 1")
    fun getUserWithNutritionLogs(userId: String): Flow<UserWithNutritionLogs?>

    @Transaction
    @Query("SELECT * FROM users WHERE userId = :userId LIMIT 1")
    fun getUserWithWeightLogs(userId: String): Flow<UserWithWeightLogs?>

    @Transaction
    @Query("SELECT * FROM users WHERE userId = :userId LIMIT 1")
    fun getUserWithFullProfile(userId: String): Flow<UserWithFullFitnessProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Update
    suspend fun updateUser(user: User)
}

@Dao
interface ExerciseDao {
    @Query("SELECT * FROM exercises ORDER BY muscleGroup ASC, name ASC")
    fun getAllExercises(): Flow<List<Exercise>>

    @Query("SELECT * FROM exercises WHERE exerciseId = :id LIMIT 1")
    suspend fun getExerciseById(id: String): Exercise?

    @Query("SELECT * FROM exercises WHERE muscleGroup = :muscleGroup")
    fun getExercisesByMuscle(muscleGroup: String): Flow<List<Exercise>>

    @Transaction
    @Query("SELECT * FROM exercises WHERE exerciseId = :id LIMIT 1")
    fun getExerciseWithSets(id: String): Flow<ExerciseWithSets?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercises(exercises: List<Exercise>)
}

@Dao
interface WorkoutDao {
    @Query("SELECT * FROM workout_plans WHERE userId = :userId ORDER BY createdAt DESC")
    fun getWorkoutPlans(userId: String): Flow<List<WorkoutPlan>>

    @Query("SELECT * FROM workout_plans WHERE planId = :planId LIMIT 1")
    suspend fun getWorkoutPlan(planId: String): WorkoutPlan?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutPlan(plan: WorkoutPlan)

    @Query("SELECT * FROM workout_sessions WHERE userId = :userId ORDER BY startedAt DESC")
    fun getWorkoutSessions(userId: String): Flow<List<WorkoutSession>>

    @Transaction
    @Query("SELECT * FROM workout_sessions WHERE sessionId = :sessionId LIMIT 1")
    fun getWorkoutWithSets(sessionId: String): Flow<WorkoutWithSets?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutSession(session: WorkoutSession)

    @Query("SELECT * FROM workout_sets WHERE workoutSessionId = :sessionId ORDER BY setNumber ASC")
    fun getWorkoutSets(sessionId: String): Flow<List<WorkoutSet>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutSet(set: WorkoutSet)

    @Query("SELECT * FROM workout_sets ORDER BY weightKg DESC LIMIT 10")
    fun getTopWeightSets(): Flow<List<WorkoutSet>>

    @Query("SELECT * FROM workout_sets ORDER BY reps DESC LIMIT 10")
    fun getTopRepSets(): Flow<List<WorkoutSet>>
}

@Dao
interface NutritionDao {
    @Query("SELECT * FROM food_items ORDER BY isIndianFood DESC, name ASC")
    fun getAllFoodItems(): Flow<List<FoodItem>>

    @Query("SELECT * FROM food_items WHERE isIndianFood = 1")
    fun getIndianFoodItems(): Flow<List<FoodItem>>

    @Query("""
        SELECT * FROM food_items 
        WHERE name LIKE '%' || :query || '%' 
           OR tamilName LIKE '%' || :query || '%' 
           OR category LIKE '%' || :query || '%' 
           OR description LIKE '%' || :query || '%' 
        ORDER BY 
          CASE WHEN name LIKE :query || '%' THEN 1 
               WHEN tamilName LIKE :query || '%' THEN 2 
               ELSE 3 END, 
          name ASC
    """)
    fun searchFoodByName(query: String): Flow<List<FoodItem>>

    @Query("""
        SELECT * FROM food_items 
        WHERE (:query IS NULL OR :query = '' OR name LIKE '%' || :query || '%' OR tamilName LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%')
          AND (:minProtein IS NULL OR protein >= :minProtein)
          AND (:maxCalories IS NULL OR calories <= :maxCalories)
          AND (:maxCarbs IS NULL OR carbs <= :maxCarbs)
          AND (:maxFat IS NULL OR fat <= :maxFat)
          AND (:minFiber IS NULL OR fibre >= :minFiber)
          AND (:category IS NULL OR :category = '' OR category = :category)
        ORDER BY 
          CASE WHEN :sortBy = 'PROTEIN' THEN protein END DESC,
          CASE WHEN :sortBy = 'CALORIES_ASC' THEN calories END ASC,
          CASE WHEN :sortBy = 'CALORIES_DESC' THEN calories END DESC,
          CASE WHEN :sortBy = 'FIBER' THEN fibre END DESC,
          name ASC
    """)
    fun searchByNutritionalProfile(
        query: String? = null,
        minProtein: Float? = null,
        maxCalories: Int? = null,
        maxCarbs: Float? = null,
        maxFat: Float? = null,
        minFiber: Float? = null,
        category: String? = null,
        sortBy: String? = null
    ): Flow<List<FoodItem>>

    @Query("SELECT * FROM food_items WHERE protein >= :minProtein ORDER BY protein DESC")
    fun getHighProteinFoods(minProtein: Float = 15f): Flow<List<FoodItem>>

    @Query("SELECT * FROM food_items WHERE calories <= :maxCalories ORDER BY calories ASC")
    fun getLowCalorieFoods(maxCalories: Int = 200): Flow<List<FoodItem>>

    @Query("SELECT * FROM food_items WHERE carbs <= :maxCarbs ORDER BY carbs ASC")
    fun getLowCarbFoods(maxCarbs: Float = 10f): Flow<List<FoodItem>>

    @Query("SELECT * FROM food_items WHERE fibre >= :minFiber ORDER BY fibre DESC")
    fun getHighFiberFoods(minFiber: Float = 4f): Flow<List<FoodItem>>

    @Query("SELECT DISTINCT category FROM food_items ORDER BY category ASC")
    fun getAllCategories(): Flow<List<String>>

    @Query("SELECT COUNT(*) FROM food_items")
    suspend fun getFoodItemCount(): Int

    @Query("DELETE FROM food_items")
    suspend fun clearFoodItems()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoodItems(items: List<FoodItem>)

    @Query("SELECT * FROM food_logs WHERE userId = :userId AND date = :date ORDER BY createdAt ASC")
    fun getFoodLogsForDate(userId: String, date: String): Flow<List<FoodLog>>

    @Query("SELECT * FROM food_logs WHERE userId = :userId AND date = :date ORDER BY createdAt ASC")
    suspend fun getFoodLogsForDateSync(userId: String, date: String): List<FoodLog>

    @Transaction
    @Query("SELECT * FROM food_logs WHERE userId = :userId AND date = :date ORDER BY createdAt ASC")
    fun getFoodLogsWithItemsForDate(userId: String, date: String): Flow<List<NutritionLogWithFoodItem>>

    @Query("SELECT * FROM food_logs WHERE userId = :userId ORDER BY date DESC")
    fun getAllFoodLogs(userId: String): Flow<List<FoodLog>>

    @Transaction
    @Query("SELECT * FROM food_logs WHERE userId = :userId ORDER BY date DESC")
    fun getAllFoodLogsWithItems(userId: String): Flow<List<NutritionLogWithFoodItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoodLog(log: FoodLog)

    @Delete
    suspend fun deleteFoodLog(log: FoodLog)
}

@Dao
interface WaterDao {
    @Query("SELECT * FROM water_logs WHERE userId = :userId AND date = :date ORDER BY loggedAt ASC")
    fun getWaterLogsForDate(userId: String, date: String): Flow<List<WaterLog>>

    @Query("SELECT * FROM water_logs WHERE userId = :userId ORDER BY loggedAt DESC")
    fun getAllWaterLogs(userId: String): Flow<List<WaterLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWaterLog(log: WaterLog)

    @Query("DELETE FROM water_logs WHERE waterLogId = (SELECT waterLogId FROM water_logs WHERE userId = :userId ORDER BY loggedAt DESC LIMIT 1)")
    suspend fun undoLastWaterLog(userId: String)
}

@Dao
interface WeightDao {
    @Query("SELECT * FROM weight_logs WHERE userId = :userId ORDER BY date ASC, loggedAt ASC")
    fun getWeightLogs(userId: String): Flow<List<WeightLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeightLog(log: WeightLog)

    @Delete
    suspend fun deleteWeightLog(log: WeightLog)
}

@Dao
interface GoalDao {
    @Query("SELECT * FROM goals WHERE userId = :userId ORDER BY createdAt DESC")
    fun getGoals(userId: String): Flow<List<Goal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: Goal)

    @Update
    suspend fun updateGoal(goal: Goal)

    @Delete
    suspend fun deleteGoal(goal: Goal)
}

@Dao
interface CalendarDao {
    @Query("SELECT * FROM calendar_events WHERE userId = :userId ORDER BY date ASC")
    fun getCalendarEvents(userId: String): Flow<List<CalendarEvent>>

    @Query("SELECT * FROM calendar_events WHERE userId = :userId AND date = :date")
    fun getEventsForDate(userId: String, date: String): Flow<List<CalendarEvent>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCalendarEvent(event: CalendarEvent)

    @Update
    suspend fun updateCalendarEvent(event: CalendarEvent)

    @Delete
    suspend fun deleteCalendarEvent(event: CalendarEvent)
}

@Dao
interface AiChatDao {
    @Query("SELECT * FROM ai_messages WHERE userId = :userId ORDER BY createdAt ASC")
    fun getMessages(userId: String): Flow<List<AIMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: AIMessage)

    @Query("DELETE FROM ai_messages WHERE userId = :userId")
    suspend fun clearHistory(userId: String)
}

@Dao
interface SubscriptionDao {
    @Query("SELECT * FROM subscriptions WHERE userId = :userId LIMIT 1")
    fun getSubscription(userId: String): Flow<Subscription?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubscription(subscription: Subscription)

    @Update
    suspend fun updateSubscription(subscription: Subscription)
}

@Dao
interface AlarmDao {
    @Query("SELECT * FROM fitness_alarms WHERE userId = :userId ORDER BY timeHour ASC, timeMinute ASC")
    fun getAllAlarms(userId: String = "user_default"): Flow<List<FitnessAlarm>>

    @Query("SELECT * FROM fitness_alarms WHERE userId = :userId AND isEnabled = 1 ORDER BY timeHour ASC, timeMinute ASC")
    fun getActiveAlarms(userId: String = "user_default"): Flow<List<FitnessAlarm>>

    @Query("SELECT * FROM fitness_alarms WHERE userId = :userId AND category = :category ORDER BY timeHour ASC, timeMinute ASC")
    fun getAlarmsByCategory(userId: String = "user_default", category: String): Flow<List<FitnessAlarm>>

    @Query("SELECT * FROM fitness_alarms WHERE alarmId = :id LIMIT 1")
    suspend fun getAlarmById(id: String): FitnessAlarm?

    @Query("SELECT COUNT(*) FROM fitness_alarms WHERE userId = :userId")
    suspend fun getAlarmCount(userId: String = "user_default"): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlarm(alarm: FitnessAlarm)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlarms(alarms: List<FitnessAlarm>)

    @Update
    suspend fun updateAlarm(alarm: FitnessAlarm)

    @Delete
    suspend fun deleteAlarm(alarm: FitnessAlarm)

    @Query("DELETE FROM fitness_alarms WHERE alarmId = :id")
    suspend fun deleteAlarmById(id: String)
}
