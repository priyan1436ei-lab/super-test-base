package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.RealTimeAlarmRingingDialog
import com.example.ui.navigation.FloatingLiquidNavBar
import com.example.ui.navigation.Screen
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.FitTrackViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: FitTrackViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        when (intent?.getStringExtra("OPEN_SCREEN")) {
            "alarms" -> viewModel.navigateTo(Screen.Alarms)
            "nutrition" -> viewModel.navigateTo(Screen.Nutrition)
            "camera_capture" -> viewModel.navigateTo(Screen.CameraCapture)
            "goals_calendar" -> viewModel.navigateTo(Screen.GoalsCalendar)
        }

        setContent {
            MyApplicationTheme {
                FitTrackApp(viewModel = viewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        when (intent.getStringExtra("OPEN_SCREEN")) {
            "alarms" -> viewModel.navigateTo(Screen.Alarms)
            "nutrition" -> viewModel.navigateTo(Screen.Nutrition)
            "camera_capture" -> viewModel.navigateTo(Screen.CameraCapture)
            "goals_calendar" -> viewModel.navigateTo(Screen.GoalsCalendar)
        }
    }
}

@Composable
fun FitTrackApp(viewModel: FitTrackViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val user by viewModel.user.collectAsStateWithLifecycle()
    val subscription by viewModel.subscription.collectAsStateWithLifecycle()
    val exercises by viewModel.exercises.collectAsStateWithLifecycle()
    val workoutPlans by viewModel.workoutPlans.collectAsStateWithLifecycle()
    val workoutSessions by viewModel.workoutSessions.collectAsStateWithLifecycle()
    val topWeightSets by viewModel.topWeightSets.collectAsStateWithLifecycle()
    val foodItems by viewModel.foodItems.collectAsStateWithLifecycle()
    val todayFoodLogs by viewModel.todayFoodLogs.collectAsStateWithLifecycle()
    val allFoodLogs by viewModel.allFoodLogs.collectAsStateWithLifecycle()
    val todayWaterLogs by viewModel.todayWaterLogs.collectAsStateWithLifecycle()
    val weightLogs by viewModel.weightLogs.collectAsStateWithLifecycle()
    val goals by viewModel.goals.collectAsStateWithLifecycle()
    val calendarEvents by viewModel.calendarEvents.collectAsStateWithLifecycle()
    val aiMessages by viewModel.aiMessages.collectAsStateWithLifecycle()

    // Real-Time Alarms & Reminders state
    val alarms by viewModel.alarms.collectAsStateWithLifecycle()
    val nextAlarm by viewModel.nextAlarm.collectAsStateWithLifecycle()
    val nextAlarmCountdown by viewModel.nextAlarmCountdown.collectAsStateWithLifecycle()
    val activeRingingAlarm by viewModel.activeRingingAlarm.collectAsStateWithLifecycle()

    // WorkManager Meal Alert Notification states
    val breakfastAlertEnabled by viewModel.breakfastReminderEnabled.collectAsStateWithLifecycle()
    val lunchAlertEnabled by viewModel.lunchReminderEnabled.collectAsStateWithLifecycle()
    val dinnerAlertEnabled by viewModel.dinnerReminderEnabled.collectAsStateWithLifecycle()
    val workManagerStatus by viewModel.mealReminderStatus.collectAsStateWithLifecycle()

    // Active Workout state
    val workoutElapsedSeconds by viewModel.workoutElapsedSeconds.collectAsStateWithLifecycle()
    val isWorkoutPaused by viewModel.isWorkoutPaused.collectAsStateWithLifecycle()
    val activeExercises by viewModel.activeExercises.collectAsStateWithLifecycle()
    val currentExerciseIndex by viewModel.currentExerciseIndex.collectAsStateWithLifecycle()
    val activeSets by viewModel.activeSets.collectAsStateWithLifecycle()
    val restTimerSeconds by viewModel.restTimerSeconds.collectAsStateWithLifecycle()
    val isRestTimerRunning by viewModel.isRestTimerRunning.collectAsStateWithLifecycle()
    val lastCompletedSession by viewModel.lastCompletedSession.collectAsStateWithLifecycle()

    // AI state
    val isCoachThinking by viewModel.isCoachThinking.collectAsStateWithLifecycle()
    val isGeneratingPlan by viewModel.isGeneratingPlan.collectAsStateWithLifecycle()
    val generatedPlanJson by viewModel.generatedPlanJson.collectAsStateWithLifecycle()

    // Paywall & Toast state
    val showPaywall by viewModel.showPaywall.collectAsStateWithLifecycle()
    val paywallFeature by viewModel.paywallFeature.collectAsStateWithLifecycle()
    val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()

    // Back handling
    BackHandler(enabled = currentScreen != Screen.Home && currentScreen != Screen.Splash && currentScreen != Screen.Auth) {
        val handled = viewModel.navigateBack()
        if (!handled) {
            viewModel.navigateTo(Screen.Home)
        }
    }

    // Determine if bottom bar should be shown
    val showBottomBar = currentScreen in listOf(
        Screen.Home,
        Screen.Workout,
        Screen.Nutrition,
        Screen.Progress,
        Screen.Profile
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgPrimary)
    ) {
        // Main Screen Host with 60 FPS Buttery Transitions
        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = {
                (fadeIn(animationSpec = tween(220, easing = FastOutSlowInEasing)) +
                    scaleIn(initialScale = 0.96f, animationSpec = tween(220, easing = FastOutSlowInEasing)))
                    .togetherWith(fadeOut(animationSpec = tween(180, easing = FastOutSlowInEasing)))
            },
            label = "screen_transition"
        ) { targetScreen ->
            when (targetScreen) {
                Screen.Splash -> {
                    SplashScreen(
                        user = user,
                        onNavigate = { viewModel.navigateTo(it) }
                    )
                }

                Screen.Auth -> {
                    AuthScreen(
                        supabaseAuth = viewModel.supabaseAuth,
                        onLoginSuccess = { email, name ->
                            viewModel.login(email, name)
                        },
                        onStartOnboarding = {
                            viewModel.navigateTo(Screen.Onboarding)
                        }
                    )
                }

                Screen.Onboarding -> {
                    OnboardingScreen(
                        onComplete = { name, age, gender, h, w, tw, goal, exp, freq, eq, diet ->
                            viewModel.completeOnboarding(
                                name, age, gender, h, w, tw, goal, exp, freq, eq, diet
                            )
                        },
                        onBackToAuth = { viewModel.navigateTo(Screen.Auth) }
                    )
                }

                Screen.Home -> {
                    HomeScreen(
                        user = user,
                        todayFoodLogs = todayFoodLogs,
                        todayWaterLogs = todayWaterLogs,
                        workoutSessions = workoutSessions,
                        onNavigate = { viewModel.navigateTo(it) },
                        onStartWorkout = { name ->
                            viewModel.startWorkout(name)
                        },
                        onQuickWater = { amount ->
                            viewModel.addWater(amount)
                        },
                        onOpenAddMeal = {
                            viewModel.navigateTo(Screen.Nutrition)
                        },
                        onOpenLogWeight = {
                            viewModel.navigateTo(Screen.Progress)
                        },
                        onOpenScanner = {
                            viewModel.navigateTo(Screen.CameraCapture)
                        }
                    )
                }

                Screen.Workout -> {
                    WorkoutLibraryScreen(
                        exercises = exercises,
                        workoutPlans = workoutPlans,
                        onNavigate = { viewModel.navigateTo(it) },
                        onStartWorkout = { name, exList ->
                            viewModel.startWorkout(name, presetExercises = exList)
                        }
                    )
                }

                Screen.ActiveWorkout -> {
                    ActiveWorkoutScreen(
                        workoutName = viewModel.activeWorkoutName.value,
                        elapsedSeconds = workoutElapsedSeconds,
                        isPaused = isWorkoutPaused,
                        exercises = activeExercises,
                        currentExerciseIndex = currentExerciseIndex,
                        sets = activeSets,
                        restTimerSeconds = restTimerSeconds,
                        isRestTimerRunning = isRestTimerRunning,
                        onTogglePause = { viewModel.toggleWorkoutPause() },
                        onCompleteSet = { idx, w, r -> viewModel.completeSet(idx, w, r) },
                        onAddSet = { viewModel.addSetToCurrentExercise() },
                        onRemoveSet = { viewModel.removeSet(it) },
                        onNextExercise = { viewModel.nextExercise() },
                        onPreviousExercise = { viewModel.previousExercise() },
                        onAddRestTime = { viewModel.addRestTime(it) },
                        onSkipRestTime = { viewModel.skipRestTimer() },
                        onFinishWorkout = { notes -> viewModel.finishWorkout(notes) },
                        onBack = { viewModel.navigateBack() }
                    )
                }

                Screen.WorkoutComplete -> {
                    WorkoutCompletionScreen(
                        session = lastCompletedSession,
                        onNavigate = { viewModel.navigateTo(it) }
                    )
                }

                Screen.AiGenerator -> {
                    AiWorkoutGeneratorScreen(
                        user = user,
                        isGenerating = isGeneratingPlan,
                        generatedPlanJson = generatedPlanJson,
                        onGenerate = { g, exp, eq, d, dur, pref ->
                            viewModel.generateAIWorkoutPlan(g, exp, eq, d, dur, pref)
                        },
                        onSavePlan = { viewModel.saveGeneratedPlan() },
                        onBack = { viewModel.navigateBack() }
                    )
                }

                Screen.AiCoach -> {
                    AiCoachScreen(
                        messages = aiMessages,
                        isThinking = isCoachThinking,
                        onSendMessage = { viewModel.sendCoachMessage(it) },
                        onClearHistory = { viewModel.clearCoachHistory() },
                        onBack = { viewModel.navigateBack() }
                    )
                }

                Screen.Nutrition -> {
                    NutritionScreen(
                        foodLogs = todayFoodLogs,
                        foodDatabase = foodItems,
                        onLogMeal = { type, food, customName, qty, cal, p, c, f ->
                            viewModel.logMeal(type, food, customName, qty, cal, p, c, f)
                        },
                        onDeleteMeal = { viewModel.deleteFood(it) },
                        onOpenScanner = { viewModel.navigateTo(Screen.CameraCapture) }
                    )
                }

                Screen.CameraCapture -> {
                    CameraCaptureScreen(
                        foodDatabase = foodItems,
                        onLogScannedMeal = { mealType, food, customName, qty, cal, p, c, f, portionGrams ->
                            viewModel.logMeal(
                                mealType = mealType,
                                foodItem = food,
                                customName = customName,
                                quantity = qty,
                                calories = cal,
                                protein = p,
                                carbs = c,
                                fat = f,
                                portionGrams = portionGrams,
                                scannedViaCamera = true
                            )
                        },
                        onNavigateToDetailedScanner = { viewModel.navigateTo(Screen.AiFoodScanner) },
                        onBack = { viewModel.navigateBack() },
                        onFetchOnlineNutrition = { query -> viewModel.nutritionNetworkRepo.searchFoodApi(query) }
                    )
                }

                Screen.AiFoodScanner -> {
                    AiFoodScannerScreen(
                        foodDatabase = foodItems,
                        onLogScannedMeal = { mealType, food, customName, qty, cal, p, c, f, portionGrams ->
                            viewModel.logMeal(
                                mealType = mealType,
                                foodItem = food,
                                customName = customName,
                                quantity = qty,
                                calories = cal,
                                protein = p,
                                carbs = c,
                                fat = f,
                                portionGrams = portionGrams,
                                scannedViaCamera = true
                            )
                            viewModel.navigateTo(Screen.Nutrition)
                        },
                        onOpenCameraCapture = { viewModel.navigateTo(Screen.CameraCapture) },
                        onBack = { viewModel.navigateBack() },
                        onReingestDataset = { viewModel.reingestNutritionDataset() }
                    )
                }

                Screen.Progress -> {
                    ProgressAnalyticsScreen(
                        user = user,
                        weightLogs = weightLogs,
                        waterLogs = todayWaterLogs,
                        workoutSessions = workoutSessions,
                        topWeightSets = topWeightSets,
                        onLogWeight = { w, n -> viewModel.logWeight(w, n) },
                        onDeleteWeight = { viewModel.deleteWeight(it) },
                        onAddWater = { viewModel.addWater(it) },
                        onUndoWater = { viewModel.undoWater() }
                    )
                }

                Screen.GoalsCalendar -> {
                    GoalsCalendarScreen(
                        goals = goals,
                        calendarEvents = calendarEvents,
                        allFoodLogs = allFoodLogs,
                        breakfastAlertEnabled = breakfastAlertEnabled,
                        lunchAlertEnabled = lunchAlertEnabled,
                        dinnerAlertEnabled = dinnerAlertEnabled,
                        workManagerStatus = workManagerStatus,
                        onToggleMealAlert = { mealType, enabled ->
                            viewModel.toggleMealReminderWorker(mealType, enabled)
                        },
                        onTriggerTestMealAlert = { mealType ->
                            viewModel.triggerTestMealReminderWorker(mealType)
                        },
                        onToggleGoalStatus = { viewModel.toggleGoalStatus(it) },
                        onCreateGoal = { t, type, target, u, dead ->
                            viewModel.createGoal(t, type, target, u, dead)
                        },
                        onScheduleWorkout = { d, title, dur ->
                            viewModel.scheduleWorkout(d, title, dur)
                        },
                        onSendMealReminder = { email ->
                            viewModel.sendMealReminderEmail(email)
                        },
                        onBack = { viewModel.navigateBack() }
                    )
                }

                Screen.Profile -> {
                    ProfileScreen(
                        user = user,
                        subscription = subscription,
                        onOpenPaywall = { viewModel.openPaywall("Premium Features") },
                        onLogout = { viewModel.logout() },
                        onNavigate = { viewModel.navigateTo(it) },
                        onExportData = {
                            viewModel.showToast("Local data export prepared: fittrack_backup.json")
                        }
                    )
                }

                Screen.Alarms -> {
                    AlarmsScreen(
                        alarms = alarms,
                        nextAlarm = nextAlarm,
                        nextAlarmCountdown = nextAlarmCountdown,
                        onToggleAlarm = { viewModel.toggleAlarm(it) },
                        onSaveAlarm = { title, labelTamil, category, hour, minute, days, tone, vibrate, target, existingId ->
                            viewModel.saveAlarm(
                                title = title,
                                labelTamil = labelTamil,
                                category = category,
                                hour = hour,
                                minute = minute,
                                daysOfWeek = days,
                                soundTone = tone,
                                vibrate = vibrate,
                                targetValue = target,
                                existingAlarmId = existingId
                            )
                        },
                        onDeleteAlarm = { viewModel.deleteAlarm(it) },
                        onTriggerTestAlarm = { viewModel.triggerTestAlarm(it) },
                        onBack = { viewModel.navigateBack() }
                    )
                }
            }
        }

        // Real-Time Ringing Alarm Alert Dialog
        activeRingingAlarm?.let { ringingAlarm ->
            RealTimeAlarmRingingDialog(
                alarm = ringingAlarm,
                onDismiss = { viewModel.dismissRingingAlarm() },
                onSnooze = { mins -> viewModel.snoozeRingingAlarm(ringingAlarm, mins) }
            )
        }

        // Floating Liquid Glass Bottom Navigation Bar
        if (showBottomBar) {
            FloatingLiquidNavBar(
                currentScreen = currentScreen,
                onNavigate = { viewModel.navigateTo(it) },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }

        // Subscription Paywall Modal Dialog
        if (showPaywall) {
            SubscriptionPaywallDialog(
                lockedFeature = paywallFeature,
                onDismiss = { viewModel.closePaywall() },
                onUpgrade = { tier ->
                    viewModel.upgradeSubscription(tier)
                }
            )
        }

        // Global Toast Notification Banner
        AnimatedVisibility(
            visible = toastMessage != null,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(top = 12.dp, start = 20.dp, end = 20.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xF2191922))
                    .border(1.dp, ElectricLime.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Text(
                    text = toastMessage ?: "",
                    style = Typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )
            }
        }
    }
}
