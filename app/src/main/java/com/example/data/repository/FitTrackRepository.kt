package com.example.data.repository

import android.content.Context
import com.example.data.local.FitTrackDatabase
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class FitTrackRepository(private val db: FitTrackDatabase) {

    private val userDao = db.userDao()
    private val exerciseDao = db.exerciseDao()
    private val workoutDao = db.workoutDao()
    private val nutritionDao = db.nutritionDao()
    private val waterDao = db.waterDao()
    private val weightDao = db.weightDao()
    private val goalDao = db.goalDao()
    private val calendarDao = db.calendarDao()
    private val aiChatDao = db.aiChatDao()
    private val subscriptionDao = db.subscriptionDao()
    private val alarmDao = db.alarmDao()

    fun getUser(userId: String = "user_default"): Flow<User?> = userDao.getUser(userId)
    suspend fun getUserSync(userId: String = "user_default"): User? = userDao.getUserSync(userId)
    suspend fun saveUser(user: User) = userDao.insertUser(user)

    // Exercises
    fun getAllExercises(): Flow<List<Exercise>> = exerciseDao.getAllExercises()
    fun getExercisesByMuscle(muscle: String): Flow<List<Exercise>> = exerciseDao.getExercisesByMuscle(muscle)
    suspend fun getExerciseById(id: String): Exercise? = exerciseDao.getExerciseById(id)

    // Workouts
    fun getWorkoutPlans(userId: String = "user_default"): Flow<List<WorkoutPlan>> = workoutDao.getWorkoutPlans(userId)
    suspend fun saveWorkoutPlan(plan: WorkoutPlan) = workoutDao.insertWorkoutPlan(plan)
    fun getWorkoutSessions(userId: String = "user_default"): Flow<List<WorkoutSession>> = workoutDao.getWorkoutSessions(userId)
    suspend fun saveWorkoutSession(session: WorkoutSession) = workoutDao.insertWorkoutSession(session)
    fun getWorkoutSets(sessionId: String): Flow<List<WorkoutSet>> = workoutDao.getWorkoutSets(sessionId)
    suspend fun saveWorkoutSet(set: WorkoutSet) = workoutDao.insertWorkoutSet(set)
    fun getTopWeightSets(): Flow<List<WorkoutSet>> = workoutDao.getTopWeightSets()
    fun getTopRepSets(): Flow<List<WorkoutSet>> = workoutDao.getTopRepSets()

    // Nutrition
    fun getAllFoodItems(): Flow<List<FoodItem>> = nutritionDao.getAllFoodItems()
    fun getFoodLogsForDate(date: String, userId: String = "user_default"): Flow<List<FoodLog>> =
        nutritionDao.getFoodLogsForDate(userId, date)
    fun getAllFoodLogs(userId: String = "user_default"): Flow<List<FoodLog>> = nutritionDao.getAllFoodLogs(userId)
    suspend fun logFood(foodLog: FoodLog) = nutritionDao.insertFoodLog(foodLog)
    suspend fun deleteFoodLog(foodLog: FoodLog) = nutritionDao.deleteFoodLog(foodLog)

    // Water
    fun getWaterLogsForDate(date: String, userId: String = "user_default"): Flow<List<WaterLog>> =
        waterDao.getWaterLogsForDate(userId, date)
    fun getAllWaterLogs(userId: String = "user_default"): Flow<List<WaterLog>> = waterDao.getAllWaterLogs(userId)
    suspend fun logWater(amountMl: Int, date: String, userId: String = "user_default") {
        waterDao.insertWaterLog(WaterLog(userId = userId, amountMl = amountMl, date = date))
    }
    suspend fun undoLastWater(userId: String = "user_default") = waterDao.undoLastWaterLog(userId)

    // Weight
    fun getWeightLogs(userId: String = "user_default"): Flow<List<WeightLog>> = weightDao.getWeightLogs(userId)
    suspend fun logWeight(weightKg: Float, date: String, note: String = "", userId: String = "user_default") {
        weightDao.insertWeightLog(WeightLog(userId = userId, weightKg = weightKg, date = date, note = note))
        val current = userDao.getUserSync(userId) ?: User(userId = userId)
        userDao.insertUser(current.copy(weightKg = weightKg, updatedAt = System.currentTimeMillis()))
    }
    suspend fun deleteWeightLog(log: WeightLog) = weightDao.deleteWeightLog(log)

    // Goals
    fun getGoals(userId: String = "user_default"): Flow<List<Goal>> = goalDao.getGoals(userId)
    suspend fun saveGoal(goal: Goal) = goalDao.insertGoal(goal)
    suspend fun updateGoal(goal: Goal) = goalDao.updateGoal(goal)
    suspend fun deleteGoal(goal: Goal) = goalDao.deleteGoal(goal)

    // Calendar
    fun getCalendarEvents(userId: String = "user_default"): Flow<List<CalendarEvent>> = calendarDao.getCalendarEvents(userId)
    fun getEventsForDate(date: String, userId: String = "user_default"): Flow<List<CalendarEvent>> = calendarDao.getEventsForDate(userId, date)
    suspend fun saveCalendarEvent(event: CalendarEvent) = calendarDao.insertCalendarEvent(event)
    suspend fun updateCalendarEvent(event: CalendarEvent) = calendarDao.updateCalendarEvent(event)
    suspend fun deleteCalendarEvent(event: CalendarEvent) = calendarDao.deleteCalendarEvent(event)

    // AI Chat
    fun getAiMessages(userId: String = "user_default"): Flow<List<AIMessage>> = aiChatDao.getMessages(userId)
    suspend fun saveAiMessage(msg: AIMessage) = aiChatDao.insertMessage(msg)
    suspend fun clearAiHistory(userId: String = "user_default") = aiChatDao.clearHistory(userId)

    // Subscriptions
    fun getSubscription(userId: String = "user_default"): Flow<Subscription?> = subscriptionDao.getSubscription(userId)
    suspend fun saveSubscription(sub: Subscription) {
        subscriptionDao.insertSubscription(sub)
        val user = userDao.getUserSync(sub.userId)
        if (user != null) {
            userDao.insertUser(user.copy(subscriptionTier = sub.plan, updatedAt = System.currentTimeMillis()))
        }
    }

    suspend fun checkAndSeedInitialData() = withContext(Dispatchers.IO) {
        val existingUser = userDao.getUserSync("user_default")
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        if (existingUser == null) {
            userDao.insertUser(
                User(
                    userId = "user_default",
                    fullName = "Priyan",
                    username = "priyan_fit",
                    email = "priyan1436ei@gmail.com",
                    age = 26,
                    heightCm = 178f,
                    weightKg = 72.4f,
                    targetWeightKg = 68.0f,
                    fitnessGoal = "Build Muscle",
                    activityLevel = "Moderately Active",
                    experienceLevel = "Intermediate",
                    workoutDaysPerWeek = 5,
                    equipment = "Dumbbells, Barbell, Bench, Cable Machine",
                    preferredWorkoutDuration = 52,
                    dietaryPreference = "Non-Vegetarian",
                    isOnboarded = true,
                    subscriptionTier = "PRO",
                    currentStreak = 6,
                    workoutsCompletedCount = 28
                )
            )
        }

        val existingSub = subscriptionDao.getSubscription("user_default").firstOrNull()
        if (existingSub == null) {
            subscriptionDao.insertSubscription(
                Subscription(
                    subscriptionId = "sub_user_default",
                    userId = "user_default",
                    plan = "PRO",
                    status = "ACTIVE"
                )
            )
        }

        // Seed Exercises
        seedExercises()

        // Seed Comprehensive Nutrient Food Database
        seedFoodItems()

        // Seed Default Workout Plan
        val plans = workoutDao.getWorkoutPlans("user_default").firstOrNull()
        if (plans.isNullOrEmpty()) {
            workoutDao.insertWorkoutPlan(
                WorkoutPlan(
                    planId = "plan_push_pull_legs",
                    userId = "user_default",
                    planName = "Hypertrophy 5-Day Split",
                    goal = "Build Muscle",
                    daysJson = """
                    [
                        {"day": "Monday", "title": "Push Day - Chest, Shoulders, Triceps", "exercises": ["ex_bench_press", "ex_incline_dumbbell", "ex_overhead_press", "ex_lateral_raise", "ex_tricep_pushdown"]},
                        {"day": "Tuesday", "title": "Pull Day - Back & Biceps", "exercises": ["ex_deadlift", "ex_lat_pulldown", "ex_barbell_row", "ex_bicep_curl", "ex_face_pull"]},
                        {"day": "Wednesday", "title": "Legs & Core Power", "exercises": ["ex_squat", "ex_romanian_deadlift", "ex_bulgarian_split", "ex_leg_raise", "ex_plank"]},
                        {"day": "Thursday", "title": "Active Recovery & Mobility", "exercises": ["ex_hip_stretch", "ex_cat_cow"]},
                        {"day": "Friday", "title": "Upper Body Hypertrophy", "exercises": ["ex_bench_press", "ex_pullup", "ex_arnold_press", "ex_hammer_curl", "ex_dips"]},
                        {"day": "Saturday", "title": "Lower Body & HIIT", "exercises": ["ex_squat", "ex_kettlebell_swing", "ex_burpees"]},
                        {"day": "Sunday", "title": "Rest & Meal Prep", "exercises": []}
                    ]
                    """.trimIndent(),
                    isAiGenerated = true,
                    isActive = true
                )
            )
        }

        // Seed Initial Water Logs for today if empty
        val waterLogs = waterDao.getWaterLogsForDate("user_default", todayStr).firstOrNull()
        if (waterLogs.isNullOrEmpty()) {
            waterDao.insertWaterLog(WaterLog(userId = "user_default", amountMl = 500, date = todayStr, loggedAt = System.currentTimeMillis() - 4 * 3600 * 1000))
            waterDao.insertWaterLog(WaterLog(userId = "user_default", amountMl = 750, date = todayStr, loggedAt = System.currentTimeMillis() - 2 * 3600 * 1000))
            waterDao.insertWaterLog(WaterLog(userId = "user_default", amountMl = 850, date = todayStr, loggedAt = System.currentTimeMillis() - 30 * 60 * 1000))
        }

        // Seed Initial Food Logs for today if empty
        val foodLogs = nutritionDao.getFoodLogsForDate("user_default", todayStr).firstOrNull()
        if (foodLogs.isNullOrEmpty()) {
            nutritionDao.insertFoodLog(
                FoodLog(userId = "user_default", date = todayStr, mealType = "BREAKFAST", customFoodName = "Idli with Sambar & Boiled Eggs", quantity = 1f, calories = 380, protein = 21f, carbs = 46f, fat = 10f, portionGrams = 220f, scannedViaCamera = true)
            )
            nutritionDao.insertFoodLog(
                FoodLog(userId = "user_default", date = todayStr, mealType = "LUNCH", customFoodName = "Chicken Curry with 2 Chapatis & Dal", quantity = 1f, calories = 520, protein = 40f, carbs = 58f, fat = 16f, portionGrams = 320f, scannedViaCamera = true)
            )
            nutritionDao.insertFoodLog(
                FoodLog(userId = "user_default", date = todayStr, mealType = "SNACKS", customFoodName = "Whey Protein Shake with Greek Yogurt", quantity = 1f, calories = 250, protein = 35f, carbs = 9f, fat = 5.5f, portionGrams = 200f)
            )
        }

        // Seed Day 20 and Day 30 specifically for calendar nutrition tracking
        val calMonth = Calendar.getInstance()
        calMonth.set(Calendar.DAY_OF_MONTH, 20)
        val day20Date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calMonth.time)
        val day20Logs = nutritionDao.getFoodLogsForDate("user_default", day20Date).firstOrNull()
        if (day20Logs.isNullOrEmpty()) {
            nutritionDao.insertFoodLog(
                FoodLog(userId = "user_default", date = day20Date, mealType = "BREAKFAST", customFoodName = "Oatmeal with Blueberries & Whey", quantity = 1f, calories = 440, protein = 32f, carbs = 60f, fat = 8f, portionGrams = 250f, scannedViaCamera = true)
            )
            nutritionDao.insertFoodLog(
                FoodLog(userId = "user_default", date = day20Date, mealType = "LUNCH", customFoodName = "Grilled Chicken Breast with Brown Rice & Broccoli", quantity = 1f, calories = 680, protein = 58f, carbs = 72f, fat = 14f, portionGrams = 380f, scannedViaCamera = true)
            )
            nutritionDao.insertFoodLog(
                FoodLog(userId = "user_default", date = day20Date, mealType = "SNACKS", customFoodName = "Greek Yogurt with Mixed Almonds", quantity = 1f, calories = 310, protein = 24f, carbs = 18f, fat = 16f, portionGrams = 180f)
            )
            nutritionDao.insertFoodLog(
                FoodLog(userId = "user_default", date = day20Date, mealType = "DINNER", customFoodName = "Paneer Tikka Salad with Quinoa", quantity = 1f, calories = 520, protein = 36f, carbs = 42f, fat = 22f, portionGrams = 320f, scannedViaCamera = true)
            )
        }

        calMonth.set(Calendar.DAY_OF_MONTH, 30)
        val day30Date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calMonth.time)
        val day30Logs = nutritionDao.getFoodLogsForDate("user_default", day30Date).firstOrNull()
        if (day30Logs.isNullOrEmpty()) {
            nutritionDao.insertFoodLog(
                FoodLog(userId = "user_default", date = day30Date, mealType = "BREAKFAST", customFoodName = "Scrambled Eggs with Avocado Toast & Spinach", quantity = 1f, calories = 510, protein = 34f, carbs = 38f, fat = 24f, portionGrams = 260f, scannedViaCamera = true)
            )
            nutritionDao.insertFoodLog(
                FoodLog(userId = "user_default", date = day30Date, mealType = "LUNCH", customFoodName = "Salmon Fillet with Sweet Potato & Asparagus", quantity = 1f, calories = 720, protein = 54f, carbs = 64f, fat = 26f, portionGrams = 350f, scannedViaCamera = true)
            )
            nutritionDao.insertFoodLog(
                FoodLog(userId = "user_default", date = day30Date, mealType = "SNACKS", customFoodName = "Peanut Butter Banana Protein Shake", quantity = 1f, calories = 380, protein = 32f, carbs = 36f, fat = 12f, portionGrams = 300f)
            )
            nutritionDao.insertFoodLog(
                FoodLog(userId = "user_default", date = day30Date, mealType = "DINNER", customFoodName = "Grilled Chicken Breast with Lentil Soup & Vegetables", quantity = 1f, calories = 570, protein = 48f, carbs = 52f, fat = 14f, portionGrams = 340f, scannedViaCamera = true)
            )
        }

        // Seed Initial Weight Logs
        val weightLogs = weightDao.getWeightLogs("user_default").firstOrNull()
        if (weightLogs.isNullOrEmpty()) {
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -21)
            weightDao.insertWeightLog(WeightLog(userId = "user_default", weightKg = 73.8f, date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)))
            cal.add(Calendar.DAY_OF_YEAR, 7)
            weightDao.insertWeightLog(WeightLog(userId = "user_default", weightKg = 73.2f, date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)))
            cal.add(Calendar.DAY_OF_YEAR, 7)
            weightDao.insertWeightLog(WeightLog(userId = "user_default", weightKg = 72.8f, date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)))
            cal.add(Calendar.DAY_OF_YEAR, 7)
            weightDao.insertWeightLog(WeightLog(userId = "user_default", weightKg = 72.4f, date = todayStr, note = "Post morning workout"))
        }

        // Seed Initial Goals
        val goals = goalDao.getGoals("user_default").firstOrNull()
        if (goals.isNullOrEmpty()) {
            goalDao.insertGoal(
                Goal(
                    goalId = "goal_weight_target",
                    userId = "user_default",
                    title = "Reach 68.0 KG Lean Muscle",
                    type = "WEIGHT",
                    targetValue = 68.0f,
                    currentValue = 72.4f,
                    unit = "KG",
                    startDate = todayStr,
                    deadline = "2026-12-31",
                    status = "ACTIVE"
                )
            )
            goalDao.insertGoal(
                Goal(
                    goalId = "goal_workout_freq",
                    userId = "user_default",
                    title = "Complete 5 Workouts Weekly",
                    type = "WORKOUT_FREQ",
                    targetValue = 5.0f,
                    currentValue = 4.0f,
                    unit = "days/wk",
                    startDate = todayStr,
                    deadline = "Ongoing",
                    status = "ACTIVE"
                )
            )
            goalDao.insertGoal(
                Goal(
                    goalId = "goal_daily_water",
                    userId = "user_default",
                    title = "Daily Hydration Target",
                    type = "WATER",
                    targetValue = 3.0f,
                    currentValue = 2.1f,
                    unit = "L",
                    startDate = todayStr,
                    deadline = "Daily",
                    status = "ACTIVE"
                )
            )
            goalDao.insertGoal(
                Goal(
                    goalId = "goal_protein_target",
                    userId = "user_default",
                    title = "Daily Protein Intake",
                    type = "PROTEIN",
                    targetValue = 140.0f,
                    currentValue = 96.0f,
                    unit = "g",
                    startDate = todayStr,
                    deadline = "Daily",
                    status = "ACTIVE"
                )
            )
        }

        // Seed Calendar Events
        val calEvents = calendarDao.getCalendarEvents("user_default").firstOrNull()
        if (calEvents.isNullOrEmpty()) {
            val cal = Calendar.getInstance()
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            
            cal.add(Calendar.DAY_OF_YEAR, -3)
            calendarDao.insertCalendarEvent(CalendarEvent(eventId = "ev_1", userId = "user_default", date = sdf.format(cal.time), eventType = "COMPLETED", title = "Pull Day Done", workoutName = "Pull Day", durationMinutes = 48))
            
            cal.add(Calendar.DAY_OF_YEAR, 1)
            calendarDao.insertCalendarEvent(CalendarEvent(eventId = "ev_2", userId = "user_default", date = sdf.format(cal.time), eventType = "COMPLETED", title = "Legs & Core Done", workoutName = "Leg Day", durationMinutes = 55))
            
            cal.add(Calendar.DAY_OF_YEAR, 1)
            calendarDao.insertCalendarEvent(CalendarEvent(eventId = "ev_3", userId = "user_default", date = sdf.format(cal.time), eventType = "REST", title = "Active Recovery & Mobility", workoutName = "Mobility", durationMinutes = 25))
            
            cal.add(Calendar.DAY_OF_YEAR, 1) // Today
            calendarDao.insertCalendarEvent(CalendarEvent(eventId = "ev_today", userId = "user_default", date = sdf.format(cal.time), eventType = "SCHEDULED", title = "Push Day (Chest & Shoulders)", workoutName = "Push Day", durationMinutes = 52))

            cal.add(Calendar.DAY_OF_YEAR, 1) // Tomorrow
            calendarDao.insertCalendarEvent(CalendarEvent(eventId = "ev_next", userId = "user_default", date = sdf.format(cal.time), eventType = "SCHEDULED", title = "Pull Day (Back & Biceps)", workoutName = "Pull Day", durationMinutes = 50))
        }

        // Seed Initial Workout Sessions
        val sessions = workoutDao.getWorkoutSessions("user_default").firstOrNull()
        if (sessions.isNullOrEmpty()) {
            val session1 = WorkoutSession(
                sessionId = "sess_demo_1",
                userId = "user_default",
                workoutName = "Push Day",
                startedAt = System.currentTimeMillis() - 2 * 24 * 3600 * 1000,
                completedAt = System.currentTimeMillis() - 2 * 24 * 3600 * 1000 + 3120000,
                durationSeconds = 3120,
                caloriesEstimated = 420,
                totalVolumeKg = 4850f,
                completedExercisesCount = 6,
                totalSetsCount = 18,
                notes = "Felt strong on bench press! Hit 80kg PR."
            )
            workoutDao.insertWorkoutSession(session1)
            workoutDao.insertWorkoutSet(WorkoutSet(workoutSessionId = "sess_demo_1", exerciseId = "ex_bench_press", exerciseName = "Barbell Bench Press", setNumber = 1, weightKg = 60f, reps = 12))
            workoutDao.insertWorkoutSet(WorkoutSet(workoutSessionId = "sess_demo_1", exerciseId = "ex_bench_press", exerciseName = "Barbell Bench Press", setNumber = 2, weightKg = 70f, reps = 10))
            workoutDao.insertWorkoutSet(WorkoutSet(workoutSessionId = "sess_demo_1", exerciseId = "ex_bench_press", exerciseName = "Barbell Bench Press", setNumber = 3, weightKg = 80f, reps = 8))
        }
    }

    private suspend fun seedExercises() {
        val list = listOf(
            Exercise("ex_bench_press", "Barbell Bench Press", "bench-press", "Core compound movement targeting pectoralis major, anterior deltoids and triceps.", "Chest", "Triceps, Shoulders", "Intermediate", "Barbell", "1. Lie flat on bench with eyes under bar.\n2. Grip barbell slightly wider than shoulder width.\n3. Lower bar smoothly to mid-chest, keeping elbows at 45 degrees.\n4. Drive feet into ground and press barbell up explosively.", "Keep wrists straight and retract scapula to protect shoulder joints.", 4, "8-12", 90, 95),
            Exercise("ex_incline_dumbbell", "Incline Dumbbell Press", "incline-dumbbell", "Hypertrophy lift emphasizing the clavicular upper chest head.", "Chest", "Front Delts, Triceps", "Intermediate", "Dumbbells", "1. Set bench to 30-degree incline.\n2. Bring dumbbells to shoulders, core braced.\n3. Press dumbbells upward together without clanking at top.\n4. Lower with 3-second eccentric control.", "Do not arch lower back excessively.", 3, "10-12", 75, 80),
            Exercise("ex_cable_fly", "Cable Chest Flyes", "cable-fly", "Isolation movement providing continuous tension through peak contraction.", "Chest", "Anterior Delts", "Beginner", "Cable Machine", "1. Position pulleys at chest height.\n2. Take slight step forward with soft bend in elbows.\n3. Bring hands together in hugging arc.\n4. Squeeze chest at peak contraction for 1 second.", "Avoid bending and extending elbows during movement.", 3, "12-15", 60, 65),
            Exercise("ex_pushups", "Diamond / Standard Push-ups", "pushups", "Classic calisthenic chest and core strengthening movement.", "Chest", "Triceps, Core", "Beginner", "Bodyweight", "1. High plank position, hands under shoulders.\n2. Keep rigid spine, hips level.\n3. Lower until chest touches 1 inch above floor.\n4. Press back to top.", "Do not let hips sag.", 3, "15-20", 45, 55),
            Exercise("ex_deadlift", "Barbell Conventional Deadlift", "deadlift", "King of posterior chain strength, working hamstrings, glutes, erectors, and lats.", "Back", "Glutes, Hamstrings, Traps", "Advanced", "Barbell", "1. Stand midfoot under bar, hip-width stance.\n2. Grip bar just outside legs.\n3. Pull chest up, pack lats, take slack out of bar.\n4. Push the floor away through midfoot until locked out tall.", "Never round your lumbar spine under load.", 4, "5-6", 120, 140),
            Exercise("ex_pullup", "Wide Grip Pull-ups", "pullups", "Ultimate vertical pulling test for lat width and scapular stabilizers.", "Back", "Biceps, Rear Delts", "Intermediate", "Bodyweight", "1. Grip pull-up bar with overhand grip wider than shoulders.\n2. Depress scapula and pull chest toward bar.\n3. Pause briefly at chin-over-bar height.\n4. Lower smoothly with full control.", "Avoid swinging or kicking legs for momentum.", 3, "8-10", 90, 80),
            Exercise("ex_lat_pulldown", "Wide Grip Lat Pulldown", "lat-pulldown", "Targeted vertical pull focusing on latissimus dorsi engagement.", "Back", "Biceps, Mid Back", "Beginner", "Cable Machine", "1. Secure thighs under pads.\n2. Grasp wide bar with pronated grip.\n3. Pull bar down toward upper chest while leaning back 10-15 degrees.\n4. Control eccentric return to full stretch.", "Pull through elbows, not your forearms.", 3, "10-12", 75, 75),
            Exercise("ex_barbell_row", "Bent-over Barbell Row", "barbell-row", "Horizontal pulling compound building back thickness and spinal density.", "Back", "Rear Delts, Biceps", "Intermediate", "Barbell", "1. Hinge at hips to 45-degree torso angle.\n2. Pull barbell toward lower ribs/navel.\n3. Squeeze shoulder blades together firmly at top.\n4. Lower smoothly under tension.", "Maintain neutral neck and braced spine throughout.", 3, "8-10", 90, 90),
            Exercise("ex_overhead_press", "Standing Barbell Military Press", "overhead-press", "Gold standard functional vertical push for shoulder boulder development.", "Shoulders", "Triceps, Upper Chest", "Intermediate", "Barbell", "1. Rack bar at collarbone height, grip just outside shoulders.\n2. Squeeze glutes and brace core.\n3. Press bar straight up, pulling head back slightly then through.\n4. Lock out directly over midfoot.", "Keep ribcage locked down; avoid leaning back.", 4, "6-8", 90, 85),
            Exercise("ex_lateral_raise", "Dumbbell Lateral Raises", "lateral-raise", "Strict medial delt isolation for visual shoulder width.", "Shoulders", "Traps", "Beginner", "Dumbbells", "1. Stand tall, dumbbells at sides.\n2. Raise arms out to sides with slight forward angle (scaption plane).\n3. Lift to parallel with shoulder height, leading with elbows.\n4. Lower with 2-second tempo.", "Avoid swinging weight or shrugging shoulders up.", 4, "12-15", 60, 50),
            Exercise("ex_face_pull", "Cable Face Pulls", "face-pull", "Bulletproof postural movement strengthening rotator cuffs and rear delts.", "Shoulders", "Rear Delts, Rhomboids", "Beginner", "Cable Machine", "1. Attach rope to eye level pulley.\n2. Grasp ends with thumbs facing back.\n3. Pull rope toward bridge of nose, pulling elbows back and rotating hands outward.\n4. Squeeze rear delts for 2 seconds.", "Keep posture upright; do not lean backwards.", 3, "15-20", 60, 50),
            Exercise("ex_arnold_press", "Seated Arnold Press", "arnold-press", "Multi-angle rotational press hitting anterior and lateral delts.", "Shoulders", "Triceps", "Intermediate", "Dumbbells", "1. Sit on upright bench holding dumbbells at chest, palms facing in.\n2. Press upward while rotating wrists outward so palms face forward at top.\n3. Reverse motion smoothly on descent.", "Move smoothly through rotation without sudden jerks.", 3, "10-12", 75, 75),
            Exercise("ex_bicep_curl", "Standing Barbell Bicep Curl", "barbell-curl", "Heavy bicep mass builder with strict forearm supination.", "Arms", "Forearms", "Beginner", "Barbell", "1. Grip bar shoulder-width apart.\n2. Pin elbows to ribs.\n3. Curl barbell upward contracting biceps tightly at top.\n4. Lower with complete eccentric control.", "Do not swing hips back and forth.", 3, "10-12", 60, 60),
            Exercise("ex_hammer_curl", "Dumbbell Hammer Curls", "hammer-curl", "Neutral grip curl developing brachialis and forearm thickness.", "Arms", "Brachioradialis", "Beginner", "Dumbbells", "1. Hold dumbbells with palms facing each other.\n2. Curl upward keeping thumbs facing up.\n3. Peak contraction at top, then lower smoothly.", "Keep upper arms completely stationary.", 3, "12-15", 60, 55),
            Exercise("ex_tricep_pushdown", "Cable Tricep Rope Pushdown", "tricep-pushdown", "Continuous tension movement for lateral and medial triceps heads.", "Arms", "Forearms", "Beginner", "Cable Machine", "1. Grab rope attachment at chest level, elbows tucked.\n2. Push down extending arms fully.\n3. Flare rope outward at bottom for peak contraction.\n4. Return smoothly to 90 degrees.", "Do not flare elbows away from ribs.", 4, "12-15", 60, 60),
            Exercise("ex_dips", "Parallel Bar Dips", "dips", "Calisthenic upper body power builder for chest and triceps.", "Arms", "Chest, Shoulders", "Intermediate", "Bodyweight", "1. Mount parallel bars, arms locked out.\n2. Lower torso by bending elbows until upper arms parallel floor.\n3. Press back up to lockout.", "Avoid going below 90 degrees if you have shoulder discomfort.", 3, "8-12", 75, 75),
            Exercise("ex_squat", "Barbell Back Squat", "back-squat", "Foundational lower-body compound for quad, glute, and spinal strength.", "Legs", "Glutes, Hamstrings, Core", "Advanced", "Barbell", "1. Set bar across upper traps/rear delts.\n2. Feet shoulder width, toes angled slightly out.\n3. Break at hips and knees simultaneously, descending past parallel.\n4. Drive through midfoot back to standing.", "Keep knees tracking in line with toes.", 4, "6-10", 120, 130),
            Exercise("ex_romanian_deadlift", "Romanian Deadlift (RDL)", "rdl", "Supreme hip hinge movement for hamstring and glute hypertrophy.", "Legs", "Glutes, Lower Back", "Intermediate", "Barbell", "1. Stand holding bar at hip height.\n2. Push hips back with slight knee bend.\n3. Lower bar along shins until hamstrings feel full stretch.\n4. Drive hips forward and contract glutes at top.", "Keep back flat as a table throughout.", 3, "8-12", 90, 95),
            Exercise("ex_bulgarian_split", "Bulgarian Split Squat", "bulgarian-split-squat", "Unilateral leg builder eliminating imbalances and firing glutes.", "Legs", "Quads, Glutes", "Intermediate", "Dumbbells", "1. Place rear foot on bench behind you.\n2. Lower hips until front thigh is parallel to ground.\n3. Drive through front heel to return to top.", "Keep torso upright or slightly inclined for glute focus.", 3, "10-12", 75, 85),
            Exercise("ex_leg_raise", "Hanging Leg Raises", "hanging-leg-raise", "Strict anterior pelvic tilt exercise for deep lower abdominals.", "Abs", "Hip Flexors", "Intermediate", "Bodyweight", "1. Hang from pull-up bar with relaxed shoulders.\n2. Contract abs and raise legs up to 90 degrees or higher.\n3. Lower with controlled speed without swinging.", "Avoid using momentum; initiate pull from lower abs.", 3, "12-15", 60, 45),
            Exercise("ex_plank", "Weighted or Standard Plank", "plank", "Isometric core pillar stability drill strengthening transverse abdominis.", "Abs", "Shoulders, Glutes", "Beginner", "Bodyweight", "1. Forearms on ground, elbows under shoulders.\n2. Squeeze glutes, quads, and draw navel toward spine.\n3. Hold perfectly straight line from crown to heels.", "Breathe steadily while maintaining abdominal tension.", 3, "45-60s", 45, 40),
            Exercise("ex_kettlebell_swing", "Russian Kettlebell Swing", "kettlebell-swing", "Explosive hip hinge conditioning for cardio and posterior chain power.", "HIIT", "Glutes, Hamstrings, Shoulders", "Intermediate", "Kettlebell", "1. Stand with feet slightly wider than shoulder width.\n2. Hinge at hips to swing kettlebell between legs.\n3. Snap hips forward explosively to propel bell to chest level.", "Power comes from hips, not lifting with arms.", 4, "15-20", 45, 90),
            Exercise("ex_burpees", "Full Chest-to-Floor Burpees", "burpees", "Total body metabolic conditioning and cardio endurance test.", "Full Body", "Chest, Legs, Core", "Intermediate", "Bodyweight", "1. Drop into squat, place hands on floor.\n2. Kick feet back to pushup position, lower chest to floor.\n3. Press up, jump feet to hands.\n4. Jump straight up with hands overhead.", "Pace your breathing rhythm to sustain sets.", 3, "12-15", 60, 110),
            Exercise("ex_hip_stretch", "World's Greatest Stretch", "greatest-stretch", "Dynamic multi-joint mobility movement for hips, thoracic spine and ankles.", "Mobility", "Hips, Thoracic Spine", "Beginner", "Bodyweight", "1. Step forward into deep lunge.\n2. Place inside elbow near front instep.\n3. Rotate chest and reach hand toward ceiling.\n4. Return and stretch hamstring.", "Take slow deep breaths into each stretch position.", 3, "5 per side", 30, 25),
            Exercise("ex_cat_cow", "Cat-Cow Spinal Flow", "cat-cow", "Gentle spinal flexion and extension restoring vertebral mobility.", "Mobility", "Spine, Core", "Beginner", "Bodyweight", "1. Tabletop position on hands and knees.\n2. Inhale: drop belly, arch back, gaze upward (Cow).\n3. Exhale: round spine, tuck chin, tuck pelvis (Cat).", "Move synchronously with slow deep breaths.", 2, "10 cycles", 30, 20)
        )
        exerciseDao.insertExercises(list)
    }

    private suspend fun seedFoodItems() {
        com.example.data.nutrition.NutritionDatasetPipeline.ingestCuratedDataset(nutritionDao)
    }

    // =========================================================================
    // NUTRITION DATASET PIPELINE & SEARCH APIS
    // =========================================================================

    fun searchFoodByName(query: String): Flow<List<FoodItem>> {
        return if (query.isBlank()) {
            nutritionDao.getAllFoodItems()
        } else {
            nutritionDao.searchFoodByName(query.trim())
        }
    }

    fun searchFoodByNutritionalProfile(
        query: String? = null,
        minProtein: Float? = null,
        maxCalories: Int? = null,
        maxCarbs: Float? = null,
        maxFat: Float? = null,
        minFiber: Float? = null,
        category: String? = null,
        sortBy: String? = null
    ): Flow<List<FoodItem>> {
        return nutritionDao.searchByNutritionalProfile(
            query = query?.trim()?.ifEmpty { null },
            minProtein = minProtein,
            maxCalories = maxCalories,
            maxCarbs = maxCarbs,
            maxFat = maxFat,
            minFiber = minFiber,
            category = category?.ifEmpty { null },
            sortBy = sortBy
        )
    }

    fun getHighProteinFoods(minProtein: Float = 15f): Flow<List<FoodItem>> =
        nutritionDao.getHighProteinFoods(minProtein)

    fun getLowCalorieFoods(maxCalories: Int = 200): Flow<List<FoodItem>> =
        nutritionDao.getLowCalorieFoods(maxCalories)

    fun getLowCarbFoods(maxCarbs: Float = 10f): Flow<List<FoodItem>> =
        nutritionDao.getLowCarbFoods(maxCarbs)

    fun getHighFiberFoods(minFiber: Float = 4f): Flow<List<FoodItem>> =
        nutritionDao.getHighFiberFoods(minFiber)

    fun getAllFoodCategories(): Flow<List<String>> =
        nutritionDao.getAllCategories()

    suspend fun runNutritionDatasetIngestion(
        additionalRecords: List<com.example.data.nutrition.NutritionRawRecord>? = null
    ): com.example.data.nutrition.IngestionResult {
        return com.example.data.nutrition.NutritionDatasetPipeline.ingestCuratedDataset(nutritionDao, additionalRecords)
    }

    // =========================================================================
    // REAL-TIME ALARM & REMINDER DATA OPERATIONS
    // =========================================================================

    fun getAllAlarms(userId: String = "user_default"): Flow<List<FitnessAlarm>> =
        alarmDao.getAllAlarms(userId)

    fun getActiveAlarms(userId: String = "user_default"): Flow<List<FitnessAlarm>> =
        alarmDao.getActiveAlarms(userId)

    fun getAlarmsByCategory(category: String, userId: String = "user_default"): Flow<List<FitnessAlarm>> =
        alarmDao.getAlarmsByCategory(userId, category)

    suspend fun getAlarmById(id: String): FitnessAlarm? = alarmDao.getAlarmById(id)

    suspend fun saveAlarm(alarm: FitnessAlarm) = alarmDao.insertAlarm(alarm)

    suspend fun updateAlarm(alarm: FitnessAlarm) = alarmDao.updateAlarm(alarm)

    suspend fun deleteAlarm(alarm: FitnessAlarm) = alarmDao.deleteAlarm(alarm)

    suspend fun deleteAlarmById(id: String) = alarmDao.deleteAlarmById(id)

    suspend fun seedDefaultAlarmsIfEmpty() {
        withContext(Dispatchers.IO) {
            val count = alarmDao.getAlarmCount("user_default")
            if (count == 0) {
                val defaults = listOf(
                    FitnessAlarm(
                        alarmId = "alarm_morning_workout",
                        userId = "user_default",
                        title = "Morning Cardio & HIIT",
                        labelTamil = "காலை உடற்பயிற்சி & கார்டியோ",
                        category = "WORKOUT",
                        timeHour = 6,
                        timeMinute = 30,
                        isEnabled = true,
                        daysOfWeek = "1,2,3,4,5,6", // Mon-Sat
                        soundTone = "High Energy",
                        vibrate = true,
                        targetValue = "45 mins HIIT"
                    ),
                    FitnessAlarm(
                        alarmId = "alarm_morning_water",
                        userId = "user_default",
                        title = "Morning Hydration Kickstart",
                        labelTamil = "காலை தண்ணீர் அருந்தும் நேரம்",
                        category = "HYDRATION",
                        timeHour = 9,
                        timeMinute = 0,
                        isEnabled = true,
                        daysOfWeek = "1,2,3,4,5,6,7", // Daily
                        soundTone = "Cyber Chime",
                        vibrate = true,
                        targetValue = "500 ml Water"
                    ),
                    FitnessAlarm(
                        alarmId = "alarm_lunch_nutrition",
                        userId = "user_default",
                        title = "Healthy Lunch & Macro Log",
                        labelTamil = "மதிய உணவு & கலோரி பதிவு",
                        category = "MEAL",
                        timeHour = 13,
                        timeMinute = 0,
                        isEnabled = true,
                        daysOfWeek = "1,2,3,4,5,6,7",
                        soundTone = "Zen Bell",
                        vibrate = true,
                        targetValue = "High Protein Lunch"
                    ),
                    FitnessAlarm(
                        alarmId = "alarm_afternoon_water",
                        userId = "user_default",
                        title = "Afternoon Hydration",
                        labelTamil = "மதிய நீர் அருந்துதல்",
                        category = "HYDRATION",
                        timeHour = 15,
                        timeMinute = 30,
                        isEnabled = true,
                        daysOfWeek = "1,2,3,4,5,6,7",
                        soundTone = "Cyber Chime",
                        vibrate = true,
                        targetValue = "500 ml Water"
                    ),
                    FitnessAlarm(
                        alarmId = "alarm_evening_workout",
                        userId = "user_default",
                        title = "Evening Strength & Hypertrophy",
                        labelTamil = "மாலை தசை வலிமை பயிற்சி",
                        category = "WORKOUT",
                        timeHour = 17,
                        timeMinute = 45,
                        isEnabled = true,
                        daysOfWeek = "1,2,3,4,5",
                        soundTone = "Pulse Alert",
                        vibrate = true,
                        targetValue = "Heavy Push/Pull Session"
                    ),
                    FitnessAlarm(
                        alarmId = "alarm_post_workout_shake",
                        userId = "user_default",
                        title = "Post-Workout Whey Protein",
                        labelTamil = "உடற்பயிற்சிக்கு பின் புரத பானம்",
                        category = "SUPPLEMENT",
                        timeHour = 19,
                        timeMinute = 15,
                        isEnabled = true,
                        daysOfWeek = "1,2,3,4,5",
                        soundTone = "Cyber Chime",
                        vibrate = true,
                        targetValue = "30g Whey + 5g Creatine"
                    ),
                    FitnessAlarm(
                        alarmId = "alarm_night_sleep",
                        userId = "user_default",
                        title = "Deep Sleep & Muscle Recovery",
                        labelTamil = "இரவு ஆழ்ந்த உறக்கம் & உடல் மீட்பு",
                        category = "SLEEP",
                        timeHour = 22,
                        timeMinute = 30,
                        isEnabled = true,
                        daysOfWeek = "1,2,3,4,5,6,7",
                        soundTone = "Gentle Rise",
                        vibrate = true,
                        targetValue = "8 Hours Sleep Target"
                    )
                )
                alarmDao.insertAlarms(defaults)
            }
        }
    }
}
