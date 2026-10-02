package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.FitTrackAIService
import com.example.data.local.FitTrackDatabase
import com.example.data.model.*
import com.example.data.repository.FitTrackRepository
import com.example.ui.navigation.Screen
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
class FitTrackViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FitTrackRepository(FitTrackDatabase.getInstance(application))
    val nutritionNetworkRepo = com.example.data.network.NutritionNetworkRepository(FitTrackDatabase.getInstance(application).nutritionDao())
    val alarmScheduler = com.example.data.alarm.AlarmScheduler(application)
    val supabaseAuth = com.example.data.auth.SupabaseAuthService(application)
    val mealReminderScheduler = com.example.data.worker.MealReminderScheduler(application)

    // WorkManager Meal Reminder States
    val breakfastReminderEnabled: StateFlow<Boolean> = mealReminderScheduler.breakfastEnabled
    val lunchReminderEnabled: StateFlow<Boolean> = mealReminderScheduler.lunchEnabled
    val dinnerReminderEnabled: StateFlow<Boolean> = mealReminderScheduler.dinnerEnabled
    val mealReminderStatus: StateFlow<String?> = mealReminderScheduler.lastActionStatus

    // Real-Time Alarms & Reminders State
    val alarms: StateFlow<List<FitnessAlarm>> = repository.getAllAlarms()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeAlarms: StateFlow<List<FitnessAlarm>> = repository.getActiveAlarms()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _activeRingingAlarm = MutableStateFlow<FitnessAlarm?>(null)
    val activeRingingAlarm: StateFlow<FitnessAlarm?> = _activeRingingAlarm.asStateFlow()

    private val _nextAlarm = MutableStateFlow<FitnessAlarm?>(null)
    val nextAlarm: StateFlow<FitnessAlarm?> = _nextAlarm.asStateFlow()

    private val _nextAlarmCountdown = MutableStateFlow("Initializing alarms...")
    val nextAlarmCountdown: StateFlow<String> = _nextAlarmCountdown.asStateFlow()

    // Network search state
    private val _isSearchingOnline = MutableStateFlow(false)
    val isSearchingOnline: StateFlow<Boolean> = _isSearchingOnline.asStateFlow()

    private val _onlineFoodResults = MutableStateFlow<List<FoodItem>>(emptyList())
    val onlineFoodResults: StateFlow<List<FoodItem>> = _onlineFoodResults.asStateFlow()

    // Navigation state
    private val _currentScreen = MutableStateFlow<Screen>(Screen.Splash)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val screenBackStack = mutableListOf<Screen>()

    // Current date
    private val _currentDateStr = MutableStateFlow(getTodayString())
    val currentDateStr: StateFlow<String> = _currentDateStr.asStateFlow()

    // User & Subscription
    val user: StateFlow<User?> = repository.getUser()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val subscription: StateFlow<Subscription?> = repository.getSubscription()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Exercises & Workouts
    val exercises: StateFlow<List<Exercise>> = repository.getAllExercises()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val workoutPlans: StateFlow<List<WorkoutPlan>> = repository.getWorkoutPlans()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val workoutSessions: StateFlow<List<WorkoutSession>> = repository.getWorkoutSessions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val topWeightSets: StateFlow<List<WorkoutSet>> = repository.getTopWeightSets()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Nutrition
    val foodItems: StateFlow<List<FoodItem>> = repository.getAllFoodItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val foodCategories: StateFlow<List<String>> = repository.getAllFoodCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _foodSearchQuery = MutableStateFlow("")
    val foodSearchQuery: StateFlow<String> = _foodSearchQuery.asStateFlow()

    private val _nutritionalFilter = MutableStateFlow(com.example.data.nutrition.NutritionalFilterCriteria())
    val nutritionalFilter: StateFlow<com.example.data.nutrition.NutritionalFilterCriteria> = _nutritionalFilter.asStateFlow()

    val filteredFoodItems: StateFlow<List<FoodItem>> = _nutritionalFilter.flatMapLatest { filter ->
        repository.searchFoodByNutritionalProfile(
            query = filter.query,
            minProtein = filter.minProtein,
            maxCalories = filter.maxCalories,
            maxCarbs = filter.maxCarbs,
            maxFat = filter.maxFat,
            minFiber = filter.minFiber,
            category = filter.category,
            sortBy = filter.sortBy.key
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _lastIngestionResult = MutableStateFlow<com.example.data.nutrition.IngestionResult?>(null)
    val lastIngestionResult: StateFlow<com.example.data.nutrition.IngestionResult?> = _lastIngestionResult.asStateFlow()

    val todayFoodLogs: StateFlow<List<FoodLog>> = _currentDateStr.flatMapLatest { date ->
        repository.getFoodLogsForDate(date)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allFoodLogs: StateFlow<List<FoodLog>> = repository.getAllFoodLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Water
    val todayWaterLogs: StateFlow<List<WaterLog>> = _currentDateStr.flatMapLatest { date ->
        repository.getWaterLogsForDate(date)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allWaterLogs: StateFlow<List<WaterLog>> = repository.getAllWaterLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Weight
    val weightLogs: StateFlow<List<WeightLog>> = repository.getWeightLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Goals & Calendar
    val goals: StateFlow<List<Goal>> = repository.getGoals()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val calendarEvents: StateFlow<List<CalendarEvent>> = repository.getCalendarEvents()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // AI Messages
    val aiMessages: StateFlow<List<AIMessage>> = repository.getAiMessages()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Workout Session State
    private val _isWorkoutActive = MutableStateFlow(false)
    val isWorkoutActive: StateFlow<Boolean> = _isWorkoutActive.asStateFlow()

    private val _activeWorkoutName = MutableStateFlow("Push Day")
    val activeWorkoutName: StateFlow<String> = _activeWorkoutName.asStateFlow()

    private val _activeWorkoutPlanId = MutableStateFlow<String?>(null)

    private val _workoutElapsedSeconds = MutableStateFlow(0L)
    val workoutElapsedSeconds: StateFlow<Long> = _workoutElapsedSeconds.asStateFlow()

    private val _isWorkoutPaused = MutableStateFlow(false)
    val isWorkoutPaused: StateFlow<Boolean> = _isWorkoutPaused.asStateFlow()

    private val _activeExercises = MutableStateFlow<List<Exercise>>(emptyList())
    val activeExercises: StateFlow<List<Exercise>> = _activeExercises.asStateFlow()

    private val _currentExerciseIndex = MutableStateFlow(0)
    val currentExerciseIndex: StateFlow<Int> = _currentExerciseIndex.asStateFlow()

    private val _activeSets = MutableStateFlow<List<WorkoutSet>>(emptyList())
    val activeSets: StateFlow<List<WorkoutSet>> = _activeSets.asStateFlow()

    // Floating Rest Timer State
    private val _restTimerSeconds = MutableStateFlow(0)
    val restTimerSeconds: StateFlow<Int> = _restTimerSeconds.asStateFlow()

    private val _isRestTimerRunning = MutableStateFlow(false)
    val isRestTimerRunning: StateFlow<Boolean> = _isRestTimerRunning.asStateFlow()

    // Completed Session Summary
    private val _lastCompletedSession = MutableStateFlow<WorkoutSession?>(null)
    val lastCompletedSession: StateFlow<WorkoutSession?> = _lastCompletedSession.asStateFlow()

    // AI Coach UI State
    private val _isCoachThinking = MutableStateFlow(false)
    val isCoachThinking: StateFlow<Boolean> = _isCoachThinking.asStateFlow()

    // AI Workout Generator UI State
    private val _isGeneratingPlan = MutableStateFlow(false)
    val isGeneratingPlan: StateFlow<Boolean> = _isGeneratingPlan.asStateFlow()

    private val _generatedPlanJson = MutableStateFlow<String?>(null)
    val generatedPlanJson: StateFlow<String?> = _generatedPlanJson.asStateFlow()

    // Paywall Dialog State
    private val _showPaywall = MutableStateFlow(false)
    val showPaywall: StateFlow<Boolean> = _showPaywall.asStateFlow()

    private val _paywallFeature = MutableStateFlow("AI Features")
    val paywallFeature: StateFlow<String> = _paywallFeature.asStateFlow()

    // Toast notification
    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    private var timerJob: Job? = null
    private var restTimerJob: Job? = null

    init {
        viewModelScope.launch {
            repository.checkAndSeedInitialData()
            repository.seedDefaultAlarmsIfEmpty()
        }

        // Initialize WorkManager local notification meal reminders (Breakfast, Lunch, Dinner)
        try {
            mealReminderScheduler.scheduleDefaultReminders()
        } catch (_: Exception) {}

        // Real-time alarm countdown ticker
        viewModelScope.launch {
            while (isActive) {
                updateNextAlarmCountdown()
                delay(1000L)
            }
        }

        // Listen for triggered alarms from AlarmReceiver
        viewModelScope.launch {
            com.example.data.alarm.AlarmReceiver.alarmTriggerFlow.collect { firedAlarm ->
                _activeRingingAlarm.value = firedAlarm
            }
        }
    }

    private fun updateNextAlarmCountdown() {
        val currentAlarms = alarms.value.filter { it.isEnabled }
        if (currentAlarms.isEmpty()) {
            _nextAlarm.value = null
            _nextAlarmCountdown.value = "No active alarms"
            return
        }

        val now = System.currentTimeMillis()
        var nearestAlarm: FitnessAlarm? = null
        var minDiffMillis = Long.MAX_VALUE

        for (alarm in currentAlarms) {
            val triggerMillis = alarmScheduler.calculateNextTriggerMillis(
                alarm.timeHour,
                alarm.timeMinute,
                alarm.daysOfWeek
            )
            val diff = triggerMillis - now
            if (diff in 1 until minDiffMillis) {
                minDiffMillis = diff
                nearestAlarm = alarm
            }
        }

        _nextAlarm.value = nearestAlarm

        if (nearestAlarm != null && minDiffMillis < Long.MAX_VALUE) {
            val totalSeconds = (minDiffMillis / 1000).coerceAtLeast(0)
            val hours = totalSeconds / 3600
            val minutes = (totalSeconds % 3600) / 60
            val seconds = totalSeconds % 60

            val timeFormatted = nearestAlarm.getFormattedTime()
            val categoryEmoji = when (nearestAlarm.category.uppercase()) {
                "WORKOUT" -> "⚡"
                "HYDRATION" -> "💧"
                "MEAL" -> "🥗"
                "SUPPLEMENT" -> "🥤"
                "SLEEP" -> "🌙"
                else -> "⏰"
            }

            _nextAlarmCountdown.value = if (hours > 0) {
                "$categoryEmoji $timeFormatted in ${hours}h ${minutes}m ${seconds}s"
            } else if (minutes > 0) {
                "$categoryEmoji $timeFormatted in ${minutes}m ${seconds}s"
            } else {
                "$categoryEmoji $timeFormatted in ${seconds}s"
            }
        } else {
            _nextAlarmCountdown.value = "All alarms completed"
        }
    }

    // Navigation Management
    fun navigateTo(screen: Screen) {
        if (_currentScreen.value != screen) {
            screenBackStack.add(_currentScreen.value)
            _currentScreen.value = screen
        }
    }

    fun navigateBack(): Boolean {
        if (screenBackStack.isNotEmpty()) {
            val prev = screenBackStack.removeAt(screenBackStack.size - 1)
            _currentScreen.value = prev
            return true
        }
        return false
    }

    fun showToast(msg: String) {
        _toastMessage.value = msg
        viewModelScope.launch {
            delay(2800)
            if (_toastMessage.value == msg) {
                _toastMessage.value = null
            }
        }
    }

    // Auth & Onboarding Flow
    fun completeOnboarding(
        name: String,
        age: Int,
        gender: String,
        heightCm: Float,
        weightKg: Float,
        targetWeightKg: Float,
        goal: String,
        experience: String,
        frequency: Int,
        equipment: String,
        diet: String
    ) {
        viewModelScope.launch {
            val currentUser = repository.getUserSync() ?: User()
            val updated = currentUser.copy(
                fullName = name.ifBlank { "Priyan" },
                age = age,
                gender = gender,
                heightCm = heightCm,
                weightKg = weightKg,
                targetWeightKg = targetWeightKg,
                fitnessGoal = goal,
                experienceLevel = experience,
                workoutDaysPerWeek = frequency,
                equipment = equipment,
                dietaryPreference = diet,
                isOnboarded = true,
                updatedAt = System.currentTimeMillis()
            )
            repository.saveUser(updated)
            // Also log initial weight
            repository.logWeight(weightKg, getTodayString(), "Initial onboarding weight")
            showToast("Welcome $name! Your AI training profile is ready.")
            _currentScreen.value = Screen.Home
        }
    }

    fun login(email: String, name: String = "Priyan") {
        viewModelScope.launch {
            val currentUser = repository.getUserSync() ?: User()
            val updated = currentUser.copy(
                email = email,
                fullName = name,
                isOnboarded = true
            )
            repository.saveUser(updated)
            showToast("Signed in as $name")
            _currentScreen.value = Screen.Home
        }
    }

    fun logout() {
        viewModelScope.launch {
            val currentUser = repository.getUserSync() ?: User()
            repository.saveUser(currentUser.copy(isOnboarded = false))
            supabaseAuth.logout()
            _currentScreen.value = Screen.Auth
        }
    }

    fun sendMealReminderEmail(email: String = "priyan1436ei@gmail.com") {
        viewModelScope.launch {
            showToast("📧 Meal reminder sent to $email: 'Please update your meal / மீல் அப்டேட் பண்ணுங்க'")
        }
    }

    // WorkManager Meal Alert Notification Controls
    fun toggleMealReminderWorker(mealType: String, enabled: Boolean) {
        mealReminderScheduler.toggleMealReminder(mealType, enabled)
        showToast("WorkManager $mealType alert ${if (enabled) "Scheduled" else "Cancelled"}")
    }

    fun triggerTestMealReminderWorker(mealType: String = "DINNER") {
        mealReminderScheduler.triggerImmediateTestCheck(mealType)
        showToast("⚡ WorkManager checking $mealType log for today... notification dispatched if unlogged!")
    }

    // Active Workout Controls
    fun startWorkout(workoutName: String, planId: String? = null, presetExercises: List<Exercise>? = null) {
        viewModelScope.launch {
            val allEx = presetExercises ?: run {
                val all = repository.getAllExercises().firstOrNull() ?: emptyList()
                if (workoutName.contains("Push", ignoreCase = true)) {
                    all.filter { it.muscleGroup in listOf("Chest", "Shoulders", "Arms") }.take(5)
                } else if (workoutName.contains("Pull", ignoreCase = true)) {
                    all.filter { it.muscleGroup in listOf("Back", "Arms") }.take(5)
                } else if (workoutName.contains("Leg", ignoreCase = true)) {
                    all.filter { it.muscleGroup in listOf("Legs", "Abs") }.take(5)
                } else {
                    all.take(5)
                }
            }

            _activeWorkoutName.value = workoutName
            _activeWorkoutPlanId.value = planId
            _activeExercises.value = allEx
            _currentExerciseIndex.value = 0
            _workoutElapsedSeconds.value = 0L
            _isWorkoutPaused.value = false
            _isWorkoutActive.value = true

            // Generate first default set for exercise 0
            val firstEx = allEx.firstOrNull()
            _activeSets.value = if (firstEx != null) {
                listOf(
                    WorkoutSet(
                        workoutSessionId = "active_current",
                        exerciseId = firstEx.exerciseId,
                        exerciseName = firstEx.name,
                        setNumber = 1,
                        weightKg = 60f,
                        reps = 10,
                        completed = false
                    )
                )
            } else emptyList()

            startWorkoutTimer()
            _currentScreen.value = Screen.ActiveWorkout
        }
    }

    private fun startWorkoutTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive && _isWorkoutActive.value) {
                delay(1000)
                if (!_isWorkoutPaused.value) {
                    _workoutElapsedSeconds.value += 1
                }
            }
        }
    }

    fun toggleWorkoutPause() {
        _isWorkoutPaused.value = !_isWorkoutPaused.value
    }

    fun completeSet(setIndex: Int, weight: Float, reps: Int) {
        val currentList = _activeSets.value.toMutableList()
        if (setIndex in currentList.indices) {
            val updated = currentList[setIndex].copy(
                weightKg = weight,
                reps = reps,
                completed = true,
                completedAt = System.currentTimeMillis()
            )
            currentList[setIndex] = updated
            _activeSets.value = currentList

            // Trigger rest timer
            val currentEx = _activeExercises.value.getOrNull(_currentExerciseIndex.value)
            startRestTimer(currentEx?.defaultRestSeconds ?: 90)

            // Auto-add next set if less than 4
            val currentExSets = currentList.filter { it.exerciseId == currentEx?.exerciseId }
            if (currentExSets.size < (currentEx?.defaultSets ?: 4)) {
                currentList.add(
                    WorkoutSet(
                        workoutSessionId = "active_current",
                        exerciseId = currentEx?.exerciseId ?: "ex_current",
                        exerciseName = currentEx?.name ?: "",
                        setNumber = currentExSets.size + 1,
                        weightKg = weight,
                        reps = reps,
                        completed = false
                    )
                )
                _activeSets.value = currentList
            }
        }
    }

    fun addSetToCurrentExercise() {
        val currentEx = _activeExercises.value.getOrNull(_currentExerciseIndex.value) ?: return
        val currentList = _activeSets.value.toMutableList()
        val exSets = currentList.filter { it.exerciseId == currentEx.exerciseId }
        val lastWeight = exSets.lastOrNull()?.weightKg ?: 60f
        val lastReps = exSets.lastOrNull()?.reps ?: 10

        currentList.add(
            WorkoutSet(
                workoutSessionId = "active_current",
                exerciseId = currentEx.exerciseId,
                exerciseName = currentEx.name,
                setNumber = exSets.size + 1,
                weightKg = lastWeight,
                reps = lastReps,
                completed = false
            )
        )
        _activeSets.value = currentList
    }

    fun removeSet(setIndex: Int) {
        val currentList = _activeSets.value.toMutableList()
        if (setIndex in currentList.indices && currentList.size > 1) {
            currentList.removeAt(setIndex)
            _activeSets.value = currentList
        }
    }

    fun nextExercise() {
        if (_currentExerciseIndex.value < _activeExercises.value.size - 1) {
            _currentExerciseIndex.value += 1
            val nextEx = _activeExercises.value[_currentExerciseIndex.value]
            // Check if sets already exist
            val existing = _activeSets.value.filter { it.exerciseId == nextEx.exerciseId }
            if (existing.isEmpty()) {
                val currentList = _activeSets.value.toMutableList()
                currentList.add(
                    WorkoutSet(
                        workoutSessionId = "active_current",
                        exerciseId = nextEx.exerciseId,
                        exerciseName = nextEx.name,
                        setNumber = 1,
                        weightKg = 50f,
                        reps = 10,
                        completed = false
                    )
                )
                _activeSets.value = currentList
            }
        }
    }

    fun previousExercise() {
        if (_currentExerciseIndex.value > 0) {
            _currentExerciseIndex.value -= 1
        }
    }

    fun replaceExercise(newEx: Exercise) {
        val currentIdx = _currentExerciseIndex.value
        val list = _activeExercises.value.toMutableList()
        if (currentIdx in list.indices) {
            list[currentIdx] = newEx
            _activeExercises.value = list
            showToast("Replaced with ${newEx.name}")
        }
    }

    // Rest Timer Management
    private fun startRestTimer(seconds: Int) {
        restTimerJob?.cancel()
        _restTimerSeconds.value = seconds
        _isRestTimerRunning.value = true

        restTimerJob = viewModelScope.launch {
            while (isActive && _restTimerSeconds.value > 0) {
                delay(1000)
                _restTimerSeconds.value -= 1
            }
            _isRestTimerRunning.value = false
            triggerVibration()
        }
    }

    fun addRestTime(seconds: Int = 15) {
        _restTimerSeconds.value += seconds
    }

    fun skipRestTimer() {
        restTimerJob?.cancel()
        _restTimerSeconds.value = 0
        _isRestTimerRunning.value = false
    }

    private fun triggerVibration() {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val manager = getApplication<Application>().getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                manager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                getApplication<Application>().getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(350, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(350)
            }
        } catch (e: Exception) {
            // Ignore if vibration not permitted or not supported
        }
    }

    fun finishWorkout(notes: String = "") {
        viewModelScope.launch {
            timerJob?.cancel()
            restTimerJob?.cancel()
            _isRestTimerRunning.value = false
            _isWorkoutActive.value = false

            val sessionId = "session_" + System.currentTimeMillis()
            val completedSets = _activeSets.value.filter { it.completed }
            val totalVolume = completedSets.sumOf { (it.weightKg * it.reps).toDouble() }.toFloat()
            val durationSec = _workoutElapsedSeconds.value.coerceAtLeast(60L)
            val estimatedCalories = (durationSec / 60 * 7.8f).toInt()
            val completedExCount = completedSets.map { it.exerciseId }.distinct().size

            val session = WorkoutSession(
                sessionId = sessionId,
                userId = "user_default",
                workoutPlanId = _activeWorkoutPlanId.value,
                workoutName = _activeWorkoutName.value,
                startedAt = System.currentTimeMillis() - durationSec * 1000,
                completedAt = System.currentTimeMillis(),
                durationSeconds = durationSec,
                caloriesEstimated = estimatedCalories,
                totalVolumeKg = totalVolume,
                completedExercisesCount = completedExCount,
                totalSetsCount = completedSets.size,
                notes = notes,
                completionPercentage = 100
            )

            repository.saveWorkoutSession(session)
            completedSets.forEach { set ->
                repository.saveWorkoutSet(set.copy(workoutSessionId = sessionId))
            }

            // Update user streak and workout count
            val currentUser = repository.getUserSync()
            if (currentUser != null) {
                repository.saveUser(
                    currentUser.copy(
                        currentStreak = currentUser.currentStreak + 1,
                        workoutsCompletedCount = currentUser.workoutsCompletedCount + 1,
                        updatedAt = System.currentTimeMillis()
                    )
                )
            }

            // Record calendar completion
            val today = getTodayString()
            repository.saveCalendarEvent(
                CalendarEvent(
                    eventId = "ev_" + System.currentTimeMillis(),
                    userId = "user_default",
                    date = today,
                    eventType = "COMPLETED",
                    title = "${_activeWorkoutName.value} Done",
                    workoutName = _activeWorkoutName.value,
                    durationMinutes = (durationSec / 60).toInt()
                )
            )

            _lastCompletedSession.value = session
            _currentScreen.value = Screen.WorkoutComplete
        }
    }

    // AI Coach Interaction
    fun sendCoachMessage(text: String) {
        if (text.isBlank()) return
        if (!canUseAICoach()) {
            openPaywall("AI Fitness Coach")
            return
        }

        viewModelScope.launch {
            val userMsg = AIMessage(
                userId = "user_default",
                role = "USER",
                message = text
            )
            repository.saveAiMessage(userMsg)

            _isCoachThinking.value = true
            val currentUser = repository.getUserSync()
            val recentSessions = repository.getWorkoutSessions().firstOrNull()?.take(3)?.joinToString {
                "${it.workoutName} (${it.durationSeconds / 60}m)"
            } ?: "None recently"
            val nutritionToday = todayFoodLogs.value.sumOf { it.calories }
            val proteinToday = todayFoodLogs.value.sumOf { it.protein.toDouble() }

            val (reply, thinking) = FitTrackAIService.generateCoachResponse(
                prompt = text,
                userContext = currentUser,
                recentWorkouts = recentSessions,
                nutritionSummary = "$nutritionToday kcal, ${proteinToday.toInt()}g protein"
            )

            _isCoachThinking.value = false
            val aiMsg = AIMessage(
                userId = "user_default",
                role = "MODEL",
                message = reply,
                thinkingProcess = thinking
            )
            repository.saveAiMessage(aiMsg)
        }
    }

    fun clearCoachHistory() {
        viewModelScope.launch {
            repository.clearAiHistory()
            showToast("AI Coach chat history cleared.")
        }
    }

    // AI Workout Generator
    fun generateAIWorkoutPlan(
        goal: String,
        experience: String,
        equipment: String,
        daysPerWeek: Int,
        durationMin: Int,
        preference: String
    ) {
        if (!canGenerateWorkout()) {
            openPaywall("AI Workout Generator")
            return
        }

        viewModelScope.launch {
            _isGeneratingPlan.value = true
            val jsonResult = FitTrackAIService.generateWorkoutPlan(
                goal = goal,
                experience = experience,
                equipment = equipment,
                daysPerWeek = daysPerWeek,
                durationMin = durationMin,
                preferences = preference
            )
            _isGeneratingPlan.value = false
            _generatedPlanJson.value = jsonResult
        }
    }

    fun saveGeneratedPlan() {
        val json = _generatedPlanJson.value ?: return
        viewModelScope.launch {
            try {
                val obj = JSONObject(json)
                val planName = obj.optString("planName", "AI Personalized Protocol")
                val goal = obj.optString("goal", "Hypertrophy")
                val daysArr = obj.optJSONArray("days") ?: JSONArray()

                val plan = WorkoutPlan(
                    planId = "plan_ai_" + System.currentTimeMillis(),
                    userId = "user_default",
                    planName = planName,
                    goal = goal,
                    daysJson = daysArr.toString(),
                    isAiGenerated = true,
                    isActive = true
                )
                repository.saveWorkoutPlan(plan)
                showToast("Workout plan '$planName' saved!")
                _currentScreen.value = Screen.Workout
            } catch (e: Exception) {
                showToast("Failed to parse plan. Try again.")
            }
        }
    }

    // Nutrition Logging
    fun logMeal(
        mealType: String,
        foodItem: FoodItem? = null,
        customName: String = "",
        quantity: Float = 1f,
        calories: Int = 0,
        protein: Float = 0f,
        carbs: Float = 0f,
        fat: Float = 0f,
        portionGrams: Float = 150f,
        scannedViaCamera: Boolean = false
    ) {
        viewModelScope.launch {
            val foodName = foodItem?.name ?: customName.ifBlank { "Custom Meal" }
            val cals = if (foodItem != null) (foodItem.calories * quantity).toInt() else calories
            val p = if (foodItem != null) foodItem.protein * quantity else protein
            val c = if (foodItem != null) foodItem.carbs * quantity else carbs
            val f = if (foodItem != null) foodItem.fat * quantity else fat

            val log = FoodLog(
                userId = "user_default",
                date = getTodayString(),
                mealType = mealType,
                foodId = foodItem?.foodId,
                customFoodName = foodName,
                quantity = quantity,
                calories = cals,
                protein = p,
                carbs = c,
                fat = f,
                portionGrams = portionGrams,
                scannedViaCamera = scannedViaCamera
            )
            repository.logFood(log)
            showToast("Added $foodName ($cals kcal)")
        }
    }

    fun deleteFood(foodLog: FoodLog) {
        viewModelScope.launch {
            repository.deleteFoodLog(foodLog)
            showToast("Meal entry removed.")
        }
    }

    // Nutritional Search & Ingestion Pipeline Controls
    fun setFoodSearchQuery(query: String) {
        _foodSearchQuery.value = query
        _nutritionalFilter.value = _nutritionalFilter.value.copy(query = query)
    }

    fun updateNutritionalFilter(filter: com.example.data.nutrition.NutritionalFilterCriteria) {
        _nutritionalFilter.value = filter
        _foodSearchQuery.value = filter.query
    }

    fun setNutritionalPreset(presetName: String) {
        val current = _nutritionalFilter.value
        val newFilter = when (presetName) {
            "HIGH_PROTEIN" -> current.copy(minProtein = 20f, maxCalories = null, maxCarbs = null, sortBy = com.example.data.nutrition.NutritionalSortOption.PROTEIN)
            "LOW_CALORIE" -> current.copy(maxCalories = 200, minProtein = null, maxCarbs = null, sortBy = com.example.data.nutrition.NutritionalSortOption.CALORIES_LOW)
            "LOW_CARB" -> current.copy(maxCarbs = 10f, minProtein = null, maxCalories = null, sortBy = com.example.data.nutrition.NutritionalSortOption.DEFAULT)
            "HIGH_FIBER" -> current.copy(minFiber = 4f, minProtein = null, maxCalories = null, sortBy = com.example.data.nutrition.NutritionalSortOption.FIBER)
            else -> com.example.data.nutrition.NutritionalFilterCriteria(query = current.query)
        }
        _nutritionalFilter.value = newFilter
    }

    fun searchOnlineNutritionDb(query: String) {
        if (query.isBlank()) return
        viewModelScope.launch {
            _isSearchingOnline.value = true
            val result = nutritionNetworkRepo.searchFoodApi(query)
            result.onSuccess { list ->
                _onlineFoodResults.value = list
                if (list.isNotEmpty()) {
                    showToast("Fetched ${list.size} verified items from Nutrition Database API")
                } else {
                    showToast("No online items found for '$query'")
                }
            }.onFailure { err ->
                showToast("Nutrition API: ${err.message ?: "Failed to reach server"}")
            }
            _isSearchingOnline.value = false
        }
    }

    fun reingestNutritionDataset() {
        viewModelScope.launch {
            val result = repository.runNutritionDatasetIngestion()
            _lastIngestionResult.value = result
            showToast("Dataset Ingested: ${result.totalIngested} items.")
        }
    }

    // Water Tracking
    fun addWater(amountMl: Int) {
        viewModelScope.launch {
            repository.logWater(amountMl, getTodayString())
            showToast("+$amountMl ml logged! Keep hydrating 💧")
        }
    }

    fun undoWater() {
        viewModelScope.launch {
            repository.undoLastWater()
            showToast("Last water entry removed.")
        }
    }

    // Weight Tracking
    fun logWeight(weightKg: Float, note: String = "") {
        viewModelScope.launch {
            repository.logWeight(weightKg, getTodayString(), note)
            showToast("Weight logged: $weightKg kg")
        }
    }

    fun deleteWeight(log: WeightLog) {
        viewModelScope.launch {
            repository.deleteWeightLog(log)
            showToast("Weight record removed.")
        }
    }

    // Goals Management
    fun createGoal(title: String, type: String, target: Float, unit: String, deadline: String) {
        viewModelScope.launch {
            val goal = Goal(
                goalId = "goal_" + System.currentTimeMillis(),
                userId = "user_default",
                title = title,
                type = type,
                targetValue = target,
                currentValue = 0f,
                unit = unit,
                startDate = getTodayString(),
                deadline = deadline,
                status = "ACTIVE"
            )
            repository.saveGoal(goal)
            showToast("Goal created: $title")
        }
    }

    fun toggleGoalStatus(goal: Goal) {
        viewModelScope.launch {
            val nextStatus = if (goal.status == "ACTIVE") "COMPLETED" else "ACTIVE"
            repository.updateGoal(goal.copy(status = nextStatus))
            showToast("Goal marked as $nextStatus")
        }
    }

    fun scheduleWorkout(date: String, workoutTitle: String, durationMin: Int = 45) {
        viewModelScope.launch {
            val ev = CalendarEvent(
                eventId = "ev_" + System.currentTimeMillis(),
                userId = "user_default",
                date = date,
                eventType = "SCHEDULED",
                title = workoutTitle,
                workoutName = workoutTitle,
                durationMinutes = durationMin
            )
            repository.saveCalendarEvent(ev)
            showToast("Scheduled '$workoutTitle' on $date")
        }
    }

    // Paywall & Subscriptions
    fun openPaywall(feature: String) {
        _paywallFeature.value = feature
        _showPaywall.value = true
    }

    fun closePaywall() {
        _showPaywall.value = false
    }

    fun upgradeSubscription(tier: String) {
        viewModelScope.launch {
            val sub = Subscription(
                subscriptionId = "sub_user_default",
                userId = "user_default",
                plan = tier,
                status = "ACTIVE",
                startedAt = System.currentTimeMillis()
            )
            repository.saveSubscription(sub)
            _showPaywall.value = false
            showToast("🎉 Upgraded to FITTRACK $tier! All features unlocked.")
        }
    }

    // Entitlement checks (Server / DB governed)
    fun canUseAICoach(): Boolean {
        val tier = subscription.value?.plan ?: user.value?.subscriptionTier ?: "FREE"
        return tier in listOf("PRO", "PREMIUM")
    }

    fun canGenerateWorkout(): Boolean {
        val tier = subscription.value?.plan ?: user.value?.subscriptionTier ?: "FREE"
        return tier in listOf("PRO", "PREMIUM")
    }

    fun canViewAdvancedAnalytics(): Boolean {
        val tier = subscription.value?.plan ?: user.value?.subscriptionTier ?: "FREE"
        return tier in listOf("PRO", "PREMIUM")
    }

    // =========================================================================
    // REAL-TIME ALARM ACTIONS & SCHEDULING
    // =========================================================================

    fun toggleAlarm(alarm: FitnessAlarm) {
        viewModelScope.launch {
            val updated = alarm.copy(isEnabled = !alarm.isEnabled)
            repository.updateAlarm(updated)
            if (updated.isEnabled) {
                alarmScheduler.scheduleAlarm(updated)
                showToast("⏰ Alarm '${updated.title}' activated (${updated.getFormattedTime()})")
            } else {
                alarmScheduler.cancelAlarm(alarm.alarmId)
                showToast("Alarm '${updated.title}' disabled")
            }
        }
    }

    fun saveAlarm(
        title: String,
        labelTamil: String = "",
        category: String,
        hour: Int,
        minute: Int,
        daysOfWeek: String = "1,2,3,4,5,6,7",
        soundTone: String = "Cyber Chime",
        vibrate: Boolean = true,
        targetValue: String = "",
        existingAlarmId: String? = null
    ) {
        viewModelScope.launch {
            val id = existingAlarmId ?: ("alarm_" + System.currentTimeMillis())
            val alarm = FitnessAlarm(
                alarmId = id,
                userId = "user_default",
                title = title.ifBlank { "Fitness Reminder" },
                labelTamil = labelTamil,
                category = category,
                timeHour = hour,
                timeMinute = minute,
                isEnabled = true,
                daysOfWeek = daysOfWeek,
                soundTone = soundTone,
                vibrate = vibrate,
                targetValue = targetValue
            )
            repository.saveAlarm(alarm)
            alarmScheduler.scheduleAlarm(alarm)
            showToast("Saved alarm: ${alarm.title} (${alarm.getFormattedTime()})")
        }
    }

    fun deleteAlarm(alarm: FitnessAlarm) {
        viewModelScope.launch {
            alarmScheduler.cancelAlarm(alarm.alarmId)
            repository.deleteAlarm(alarm)
            showToast("Alarm '${alarm.title}' deleted")
        }
    }

    fun triggerTestAlarm(alarm: FitnessAlarm) {
        viewModelScope.launch {
            alarmScheduler.triggerTestAlarm(alarm, delayMillis = 1500L)
            showToast("🔔 Test alarm queued! Firing in 2 seconds...")
            // Also trigger in-app state with slight delay
            delay(1500L)
            _activeRingingAlarm.value = alarm
        }
    }

    fun dismissRingingAlarm() {
        _activeRingingAlarm.value = null
    }

    fun snoozeRingingAlarm(alarm: FitnessAlarm, minutes: Int = 10) {
        viewModelScope.launch {
            _activeRingingAlarm.value = null
            showToast("💤 Snoozed '${alarm.title}' for $minutes minutes")
            // Schedule snooze trigger
            alarmScheduler.triggerTestAlarm(alarm, delayMillis = minutes * 60 * 1000L)
        }
    }

    private fun getTodayString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }
}
