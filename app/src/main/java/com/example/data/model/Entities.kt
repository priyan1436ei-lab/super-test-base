package com.example.data.model

import androidx.room.*

/**
 * User Entity - Core Athlete Profile
 * Includes indices on email and username for fast lookups and unique constraint enforcement.
 */
@Entity(
    tableName = "users",
    indices = [
        Index(value = ["email"], unique = true),
        Index(value = ["username"], unique = true)
    ]
)
data class User(
    @PrimaryKey val userId: String = "user_default",
    val fullName: String = "Priyan",
    val username: String = "priyan_fit",
    val email: String = "priyan1436ei@gmail.com",
    val phone: String = "+1 (555) 382-9102",
    val age: Int = 26,
    val gender: String = "Male",
    val heightCm: Float = 178f,
    val weightKg: Float = 72.4f,
    val targetWeightKg: Float = 68.0f,
    val fitnessGoal: String = "Build Muscle",
    val activityLevel: String = "Moderately Active",
    val experienceLevel: String = "Intermediate",
    val workoutDaysPerWeek: Int = 5,
    val equipment: String = "Dumbbells, Barbell, Bench, Cable Machine",
    val preferredWorkoutDuration: Int = 50,
    val dietaryPreference: String = "Non-Vegetarian",
    val measurementUnits: String = "METRIC", // METRIC or IMPERIAL
    val isOnboarded: Boolean = true,
    val subscriptionTier: String = "FREE", // FREE, PRO, PREMIUM
    val currentStreak: Int = 6,
    val workoutsCompletedCount: Int = 28,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Exercise Entity - Exercise Library & Taxonomy
 * Indexed on muscleGroup, difficulty, equipment, and name for instantaneous filtering.
 */
@Entity(
    tableName = "exercises",
    indices = [
        Index(value = ["muscleGroup"]),
        Index(value = ["difficulty"]),
        Index(value = ["equipment"]),
        Index(value = ["name"])
    ]
)
data class Exercise(
    @PrimaryKey val exerciseId: String,
    val name: String,
    val slug: String,
    val description: String,
    val muscleGroup: String, // Chest, Back, Shoulders, Arms, Legs, Abs, Full Body, Cardio, HIIT, Strength, Mobility
    val secondaryMuscles: String,
    val difficulty: String, // Beginner, Intermediate, Advanced
    val equipment: String, // Barbell, Dumbbell, Bodyweight, Cable, Machine
    val instructions: String,
    val safetyNotes: String,
    val defaultSets: Int = 3,
    val defaultReps: String = "10-12",
    val defaultRestSeconds: Int = 90,
    val estimatedCalories: Int = 85,
    val isPremium: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Workout Plan Entity
 * Links to User via Foreign Key with cascading deletion.
 */
@Entity(
    tableName = "workout_plans",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["userId"])
    ]
)
data class WorkoutPlan(
    @PrimaryKey val planId: String,
    val userId: String = "user_default",
    val planName: String,
    val goal: String,
    val daysJson: String, // JSON string of days & exercise lists
    val isAiGenerated: Boolean = false,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Workout (Session) Entity - Completed or In-Progress Training Session
 * Links to User via Foreign Key with cascade deletion.
 * Indexed on userId and startedAt for fast timeline and streak queries.
 */
@Entity(
    tableName = "workout_sessions",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["userId"]),
        Index(value = ["startedAt"]),
        Index(value = ["workoutName"])
    ]
)
data class Workout(
    @PrimaryKey val sessionId: String,
    val userId: String = "user_default",
    val workoutPlanId: String? = null,
    val workoutName: String,
    val startedAt: Long,
    val completedAt: Long,
    val durationSeconds: Long,
    val caloriesEstimated: Int,
    val totalVolumeKg: Float,
    val completedExercisesCount: Int,
    val totalSetsCount: Int = 0,
    val notes: String = "",
    val completionPercentage: Int = 100
) {
    val workoutId: String get() = sessionId
}

typealias WorkoutSession = Workout

/**
 * Workout Set Entity - Individual Sets within a Workout
 * Dual Foreign Keys: links to parent Workout and referenced Exercise.
 */
@Entity(
    tableName = "workout_sets",
    foreignKeys = [
        ForeignKey(
            entity = Workout::class,
            parentColumns = ["sessionId"],
            childColumns = ["workoutSessionId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Exercise::class,
            parentColumns = ["exerciseId"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["workoutSessionId"]),
        Index(value = ["exerciseId"]),
        Index(value = ["workoutSessionId", "setNumber"])
    ]
)
data class WorkoutSet(
    @PrimaryKey(autoGenerate = true) val setId: Long = 0,
    val workoutSessionId: String,
    val exerciseId: String,
    val exerciseName: String = "",
    val setNumber: Int,
    val weightKg: Float,
    val reps: Int,
    val completed: Boolean = true,
    val startedAt: Long = System.currentTimeMillis(),
    val completedAt: Long = System.currentTimeMillis()
)

/**
 * Food Item Entity - Global & Regional Nutrient Taxonomy
 */
@Entity(
    tableName = "food_items",
    indices = [
        Index(value = ["name"]),
        Index(value = ["tamilName"]),
        Index(value = ["category"]),
        Index(value = ["calories"]),
        Index(value = ["protein"]),
        Index(value = ["carbs"]),
        Index(value = ["fat"]),
        Index(value = ["isIndianFood"])
    ]
)
data class FoodItem(
    @PrimaryKey val foodId: String,
    val name: String,
    val servingSize: Float,
    val servingUnit: String, // g, ml, piece, cup
    val calories: Int,
    val protein: Float,
    val carbs: Float,
    val fat: Float,
    val fibre: Float = 0f,
    val sugar: Float = 0f,
    val sodiumMg: Float = 0f,
    val potassiumMg: Float = 0f,
    val calciumMg: Float = 0f,
    val ironMg: Float = 0f,
    val glycemicIndex: Int = 50,
    val healthRating: String = "A", // A+, A, B+, B, C
    val portionWeightGrams: Float = 150f,
    val recommendedDistanceCm: Int = 25, // optimal photo distance for volumetric accuracy
    val category: String, // Indian, Protein, Grains, Dairy, Snacks
    val isIndianFood: Boolean = false,
    val tamilName: String = "",
    val description: String = ""
)

/**
 * NutritionLog Entity (FoodLog) - Daily Caloric & Macronutrient Meal Entry
 * Linked to User (CASCADE) and optional FoodItem (SET_NULL).
 * Indices on userId, date, composite [userId, date], and mealType for rapid daily macro calculation.
 */
@Entity(
    tableName = "food_logs",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = FoodItem::class,
            parentColumns = ["foodId"],
            childColumns = ["foodId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["userId"]),
        Index(value = ["foodId"]),
        Index(value = ["date"]),
        Index(value = ["userId", "date"]),
        Index(value = ["mealType"])
    ]
)
data class NutritionLog(
    @PrimaryKey(autoGenerate = true) val foodLogId: Long = 0,
    val userId: String = "user_default",
    val date: String, // YYYY-MM-DD
    val mealType: String, // BREAKFAST, LUNCH, DINNER, SNACKS
    val foodId: String? = null,
    val customFoodName: String,
    val quantity: Float,
    val calories: Int,
    val protein: Float,
    val carbs: Float,
    val fat: Float,
    val fiber: Float = 0f,
    val scannedViaCamera: Boolean = false,
    val portionGrams: Float = 150f,
    val scanConfidence: Float = 0.95f,
    val createdAt: Long = System.currentTimeMillis()
) {
    val nutritionLogId: Long get() = foodLogId
}

typealias FoodLog = NutritionLog

/**
 * Water Log Entity - Daily Hydration Records
 */
@Entity(
    tableName = "water_logs",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["userId"]),
        Index(value = ["date"])
    ]
)
data class WaterLog(
    @PrimaryKey(autoGenerate = true) val waterLogId: Long = 0,
    val userId: String = "user_default",
    val amountMl: Int,
    val date: String, // YYYY-MM-DD
    val loggedAt: Long = System.currentTimeMillis()
)

/**
 * WeightLog Entity - User Body Composition History
 * Linked to User with cascade deletion and indices on userId and date.
 */
@Entity(
    tableName = "weight_logs",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["userId"]),
        Index(value = ["date"]),
        Index(value = ["userId", "date"])
    ]
)
data class WeightLog(
    @PrimaryKey(autoGenerate = true) val weightLogId: Long = 0,
    val userId: String = "user_default",
    val weightKg: Float,
    val unit: String = "KG",
    val date: String, // YYYY-MM-DD
    val note: String = "",
    val loggedAt: Long = System.currentTimeMillis()
)

/**
 * Goal Entity - User Targets & Milestones
 */
@Entity(
    tableName = "goals",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["userId"]),
        Index(value = ["type"])
    ]
)
data class Goal(
    @PrimaryKey val goalId: String,
    val userId: String = "user_default",
    val title: String,
    val type: String, // WEIGHT, WORKOUT_FREQ, WATER, CALORIES, PROTEIN, STEPS, CUSTOM
    val targetValue: Float,
    val currentValue: Float,
    val unit: String,
    val startDate: String,
    val deadline: String,
    val status: String = "ACTIVE", // ACTIVE, COMPLETED, PAUSED, EXPIRED
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Calendar Event Entity - Scheduled and Completed Sessions
 */
@Entity(
    tableName = "calendar_events",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["userId"]),
        Index(value = ["date"])
    ]
)
data class CalendarEvent(
    @PrimaryKey val eventId: String,
    val userId: String = "user_default",
    val date: String, // YYYY-MM-DD
    val eventType: String, // COMPLETED, MISSED, REST, SCHEDULED
    val title: String,
    val workoutName: String = "",
    val durationMinutes: Int = 45,
    val notes: String = ""
)

/**
 * AI Message Entity - Persistent AI Coach Conversations
 */
@Entity(
    tableName = "ai_messages",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["userId"]),
        Index(value = ["conversationId"])
    ]
)
data class AIMessage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val conversationId: String = "default_convo",
    val userId: String = "user_default",
    val role: String, // USER, MODEL, SYSTEM
    val message: String,
    val thinkingProcess: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Subscription Entity - Billing & Tier Management
 */
@Entity(
    tableName = "subscriptions",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["userId"])
    ]
)
data class Subscription(
    @PrimaryKey val subscriptionId: String = "sub_user_default",
    val userId: String = "user_default",
    val plan: String = "FREE", // FREE, PRO, PREMIUM
    val platform: String = "GOOGLE_PLAY",
    val status: String = "ACTIVE", // ACTIVE, TRIAL, EXPIRED, CANCELLED
    val startedAt: Long = System.currentTimeMillis(),
    val renewsAt: Long = System.currentTimeMillis() + 30L * 24 * 3600 * 1000,
    val expiresAt: Long? = null,
    val externalTransactionId: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

// =========================================================================
// ROOM RELATIONSHIPS & AGGREGATE POJOS (1-to-Many & Nested Modeling)
// =========================================================================

/**
 * 1-to-Many relationship between User and Workouts
 */
data class UserWithWorkouts(
    @Embedded val user: User,
    @Relation(
        parentColumn = "userId",
        entityColumn = "userId"
    )
    val workouts: List<Workout>
)

/**
 * 1-to-Many relationship between User and NutritionLogs
 */
data class UserWithNutritionLogs(
    @Embedded val user: User,
    @Relation(
        parentColumn = "userId",
        entityColumn = "userId"
    )
    val nutritionLogs: List<NutritionLog>
)

/**
 * 1-to-Many relationship between User and WeightLogs
 */
data class UserWithWeightLogs(
    @Embedded val user: User,
    @Relation(
        parentColumn = "userId",
        entityColumn = "userId"
    )
    val weightLogs: List<WeightLog>
)

/**
 * 1-to-Many relationship between Workout and WorkoutSets
 */
data class WorkoutWithSets(
    @Embedded val workout: Workout,
    @Relation(
        parentColumn = "sessionId",
        entityColumn = "workoutSessionId"
    )
    val sets: List<WorkoutSet>
)

/**
 * Relationship between WorkoutSet and referenced Exercise
 */
data class WorkoutSetWithExercise(
    @Embedded val workoutSet: WorkoutSet,
    @Relation(
        parentColumn = "exerciseId",
        entityColumn = "exerciseId"
    )
    val exercise: Exercise?
)

/**
 * 1-to-Many relationship between Exercise and WorkoutSets across all sessions
 */
data class ExerciseWithSets(
    @Embedded val exercise: Exercise,
    @Relation(
        parentColumn = "exerciseId",
        entityColumn = "exerciseId"
    )
    val sets: List<WorkoutSet>
)

/**
 * Relationship between NutritionLog and referenced FoodItem
 */
data class NutritionLogWithFoodItem(
    @Embedded val nutritionLog: NutritionLog,
    @Relation(
        parentColumn = "foodId",
        entityColumn = "foodId"
    )
    val foodItem: FoodItem?
)

/**
 * Full Fitness Profile Aggregate - Combines User with all primary telemetry streams
 */
data class UserWithFullFitnessProfile(
    @Embedded val user: User,
    @Relation(
        parentColumn = "userId",
        entityColumn = "userId"
    )
    val workouts: List<Workout>,
    @Relation(
        parentColumn = "userId",
        entityColumn = "userId"
    )
    val nutritionLogs: List<NutritionLog>,
    @Relation(
        parentColumn = "userId",
        entityColumn = "userId"
    )
    val weightLogs: List<WeightLog>,
    @Relation(
        parentColumn = "userId",
        entityColumn = "userId"
    )
    val goals: List<Goal>
)

/**
 * Real-Time Fitness Alarm Entity
 * Used for scheduled notifications, alerts, and timers for Workouts, Hydration, Nutrition, Sleep, and Supplements.
 */
@Entity(
    tableName = "fitness_alarms",
    indices = [
        Index(value = ["userId"]),
        Index(value = ["category"]),
        Index(value = ["isEnabled"])
    ]
)
data class FitnessAlarm(
    @PrimaryKey val alarmId: String,
    val userId: String = "user_default",
    val title: String,
    val labelTamil: String = "",
    val category: String = "WORKOUT", // WORKOUT, HYDRATION, MEAL, SUPPLEMENT, SLEEP, STRETCH
    val timeHour: Int = 7, // 0..23
    val timeMinute: Int = 0, // 0..59
    val isEnabled: Boolean = true,
    val daysOfWeek: String = "1,2,3,4,5,6,7", // 1=Mon, 2=Tue, 3=Wed, 4=Thu, 5=Fri, 6=Sat, 7=Sun
    val soundTone: String = "Cyber Chime", // Cyber Chime, Zen Bell, High Energy, Gentle Rise
    val vibrate: Boolean = true,
    val snoozeMinutes: Int = 10,
    val targetValue: String = "", // e.g. "500ml", "45 mins", "30g protein"
    val createdAt: Long = System.currentTimeMillis(),
    val lastTriggeredAt: Long = 0L
) {
    fun getFormattedTime(): String {
        val h = if (timeHour == 0) 12 else if (timeHour > 12) timeHour - 12 else timeHour
        val m = String.format("%02d", timeMinute)
        val amPm = if (timeHour >= 12) "PM" else "AM"
        return String.format("%02d:%s %s", h, m, amPm)
    }

    fun isDayActive(dayIndex: Int): Boolean {
        return daysOfWeek.split(",").mapNotNull { it.trim().toIntOrNull() }.contains(dayIndex)
    }
}
