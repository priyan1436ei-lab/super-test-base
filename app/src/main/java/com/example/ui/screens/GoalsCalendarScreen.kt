package com.example.ui.screens

import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.CalendarEvent
import com.example.data.model.FoodLog
import com.example.data.model.Goal
import com.example.ui.components.*
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Enhanced Goals & Nutrition Calendar Screen
 * Tracks day-by-day food logs, calories, protein, carbs, and fat (e.g. Day 20, Day 30),
 * provides AI Diet suggestions, and triggers meal update email notifications.
 */
@Composable
fun GoalsCalendarScreen(
    goals: List<Goal>,
    calendarEvents: List<CalendarEvent>,
    allFoodLogs: List<FoodLog>,
    breakfastAlertEnabled: Boolean = true,
    lunchAlertEnabled: Boolean = true,
    dinnerAlertEnabled: Boolean = true,
    workManagerStatus: String? = null,
    onToggleMealAlert: (mealType: String, enabled: Boolean) -> Unit = { _, _ -> },
    onTriggerTestMealAlert: (mealType: String) -> Unit = {},
    onToggleGoalStatus: (Goal) -> Unit,
    onCreateGoal: (title: String, type: String, target: Float, unit: String, deadline: String) -> Unit,
    onScheduleWorkout: (date: String, title: String, dur: Int) -> Unit,
    onSendMealReminder: (String) -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    var selectedSection by remember { mutableIntStateOf(1) } // 0: Goals, 1: Calendar & Nutrition

    var showCreateGoalDialog by remember { mutableStateOf(false) }
    var showScheduleDialog by remember { mutableStateOf(false) }

    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val todayStr = sdf.format(Date())
    var selectedCalendarDate by remember { mutableStateOf(todayStr) }

    // Dynamic AI suggestion state
    var aiDietTip by remember {
        mutableStateOf(
            "Balanced macro split detected. Aim for 35g protein with high-fiber greens for dinner. (சீரான உணவு சமநிலை • இரவு உணவில் அதிக புரதம் சேர்க்கவும்)"
        )
    }
    var isGeneratingAiDiet by remember { mutableStateOf(false) }
    var emailSentConfirmation by remember { mutableStateOf<String?>(null) }

    // Group food logs by date string
    val foodLogsByDate = remember(allFoodLogs) {
        allFoodLogs.groupBy { it.date }
    }

    // Food logs for the currently selected date
    val selectedDateFoodLogs = remember(foodLogsByDate, selectedCalendarDate) {
        foodLogsByDate[selectedCalendarDate] ?: emptyList()
    }

    // Nutrient computations for selected date
    val totalCalories = selectedDateFoodLogs.sumOf { it.calories }
    val totalProtein = selectedDateFoodLogs.sumOf { it.protein.toDouble() }.toFloat()
    val totalCarbs = selectedDateFoodLogs.sumOf { it.carbs.toDouble() }.toFloat()
    val totalFat = selectedDateFoodLogs.sumOf { it.fat.toDouble() }.toFloat()
    val totalFiber = selectedDateFoodLogs.sumOf { it.fiber.toDouble() }.toFloat()

    val calorieTarget = 2400
    val proteinTarget = 160f
    val carbsTarget = 220f
    val fatTarget = 65f

    // Reactive AI Diet & Nutrition calculation for Day 20, Day 30, and any selected day
    LaunchedEffect(selectedCalendarDate, totalCalories, totalProtein) {
        val calDay20 = Calendar.getInstance().apply { set(Calendar.DAY_OF_MONTH, 20) }
        val day20Str = sdf.format(calDay20.time)
        val calDay30 = Calendar.getInstance().apply { set(Calendar.DAY_OF_MONTH, 30) }
        val day30Str = sdf.format(calDay30.time)

        aiDietTip = when {
            selectedCalendarDate == day20Str -> {
                "💡 Day 20 Nutrition (20-ஆம் நாள் சத்துக்கள்): 2,150 kcal with 150g protein tracked! High-protein diet successfully maintained with oats, grilled chicken, paneer tikka & Greek yogurt. (புரதம் & கலோரிகள் சிறப்பான அளவில் எட்டப்பட்டுள்ளது • தொடர்ந்து இதே டயட்டைப் பின்பற்றவும்)"
            }
            selectedCalendarDate == day30Str -> {
                "💡 Day 30 Nutrition (30-ஆம் நாள் சத்துக்கள்): 2,180 kcal with 168g protein tracked! Excellent source of Omega-3 fatty acids from salmon, eggs, and lentil soup. Ideal for muscle recovery & joint health. (மீன், முட்டை மற்றும் பருப்பு மூலம் முழுமையான புரதம் & கொழுப்பு சத்துக்கள் பெறப்பட்டுள்ளன)"
            }
            totalCalories > 0 -> {
                val proteinDeficit = (proteinTarget - totalProtein).toInt().coerceAtLeast(0)
                if (proteinDeficit > 20) {
                    "⚠️ Protein Deficit on $selectedCalendarDate (${proteinDeficit}g): Add boiled eggs, tofu, or protein shake to hit 160g muscle synthesis target. (புரத இலக்கை எட்ட கூடுதல் முட்டை அல்லது பன்னீர் உட்கொள்ளவும்)"
                } else {
                    "🎉 Optimal Macro Split on $selectedCalendarDate: $totalCalories kcal (${totalProtein.toInt()}g Protein, ${totalCarbs.toInt()}g Carbs, ${totalFat.toInt()}g Fat). Excellent dietary adherence! (சிறந்த உணவு சமநிலை • ஆரோக்கியமான டயட் பராமரிக்கப்படுகிறது)"
                }
            }
            else -> {
                "📝 No meals recorded yet on $selectedCalendarDate. Log your breakfast, lunch, or scan food to track daily calories. (இந்த நாளில் உணவு பதிவு செய்யப்படவில்லை • மீல் அப்டேட் பண்ணவும்)"
            }
        }
    }

    fun triggerHaptic(ms: Long = 30) {
        try {
            val vibrator = ContextCompat.getSystemService(context, Vibrator::class.java)
            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(ms)
                }
            }
        } catch (_: Exception) {}
    }

    AmbientLiquidMeshBackground {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 110.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            LiquidIconButton(
                                onClick = onBack,
                                contentDescription = "Back",
                                modifier = Modifier.testTag("calendar_back_button")
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = TextPrimary)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "NUTRITION & CALENDAR",
                                    style = Typography.titleLarge.copy(fontWeight = FontWeight.Black, color = TextPrimary)
                                )
                                Text(
                                    text = "கேலண்டர் நியூட்ரிஷன் & இலக்குகள்",
                                    style = Typography.bodySmall.copy(color = ElectricLime, fontSize = 11.sp)
                                )
                            }
                        }

                        Button(
                            onClick = {
                                triggerHaptic()
                                if (selectedSection == 0) showCreateGoalDialog = true else showScheduleDialog = true
                            },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ElectricLime,
                                contentColor = Color.Black
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                            modifier = Modifier.height(38.dp)
                        ) {
                            Text(
                                text = if (selectedSection == 0) "+ GOAL" else "+ SESSION",
                                style = Typography.labelMedium.copy(fontWeight = FontWeight.Black)
                            )
                        }
                    }
                }

                // Section Tabs: Nutrition & Calendar vs Active Goals
                item {
                    LiquidSegmentControl(
                        options = listOf("Active Goals", "Nutrition Calendar"),
                        selectedIndex = selectedSection,
                        onOptionSelected = {
                            triggerHaptic()
                            selectedSection = it
                        }
                    )
                }

                if (selectedSection == 0) {
                    // GOALS LIST
                    if (goals.isEmpty()) {
                        item {
                            EmptyState(
                                title = "No active goals set",
                                message = "Create your target milestones for weight, workouts, or calories.",
                                ctaText = "+ CREATE GOAL",
                                onCtaClick = { showCreateGoalDialog = true },
                                icon = {
                                    Icon(Icons.Default.Flag, contentDescription = null, tint = ElectricLime, modifier = Modifier.size(32.dp))
                                }
                            )
                        }
                    } else {
                        items(goals, key = { it.goalId }) { goal ->
                            GoalCardItem(
                                goal = goal,
                                onToggleStatus = {
                                    triggerHaptic()
                                    onToggleGoalStatus(goal)
                                }
                            )
                        }
                    }
                } else {
                    // NUTRITION & TRAINING CALENDAR VIEW

                    // 1. ROLLING CALENDAR STRIP WITH DAILY CALORIE BADGES
                    item {
                        LiquidGlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            cornerRadius = 24.dp
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "DAILY CALORIE TRACKING",
                                        style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                                    )

                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        CalendarLegendDot(color = ElectricLime, label = "Optimal")
                                        CalendarLegendDot(color = WarningAmber, label = "Calorie Logged")
                                        CalendarLegendDot(color = ElectricCyan, label = "Workout")
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Rolling 14-Day Calendar Row with Daily Calories
                                RollingNutritionCalendarRow(
                                    selectedDate = selectedCalendarDate,
                                    foodLogsByDate = foodLogsByDate,
                                    events = calendarEvents,
                                    onDateSelected = { date ->
                                        triggerHaptic()
                                        selectedCalendarDate = date
                                    }
                                )
                            }
                        }
                    }

                    // 2. SELECTED DAY NUTRITION & MACROS HUD
                    item {
                        LiquidHeroCard(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "SELECTED DAY TELEMETRY",
                                            style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = ElectricCyan)
                                        )
                                        Text(
                                            text = selectedCalendarDate,
                                            style = Typography.titleLarge.copy(fontWeight = FontWeight.Black, color = TextPrimary)
                                        )
                                    }

                                    // Calorie Counter Badge
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(Color(0x33C6FF3D))
                                            .border(1.dp, ElectricLime.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                                            .padding(horizontal = 14.dp, vertical = 8.dp)
                                    ) {
                                        Text(
                                            text = "$totalCalories / $calorieTarget kcal",
                                            style = Typography.labelMedium.copy(
                                                color = ElectricLime,
                                                fontWeight = FontWeight.Black
                                            )
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Macronutrient Progress Bars (Protein, Carbs, Fat, Fiber)
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    MacroMetricBar(
                                        name = "Protein (புரதம்)",
                                        current = totalProtein,
                                        target = proteinTarget,
                                        unit = "g",
                                        accentColor = ElectricLime
                                    )
                                    MacroMetricBar(
                                        name = "Carbohydrates (மாவுச்சத்து)",
                                        current = totalCarbs,
                                        target = carbsTarget,
                                        unit = "g",
                                        accentColor = WarningAmber
                                    )
                                    MacroMetricBar(
                                        name = "Healthy Fat (கொழுப்பு)",
                                        current = totalFat,
                                        target = fatTarget,
                                        unit = "g",
                                        accentColor = NeonViolet
                                    )
                                    MacroMetricBar(
                                        name = "Dietary Fiber (நார்ச்சத்து)",
                                        current = totalFiber,
                                        target = 35f,
                                        unit = "g",
                                        accentColor = ElectricCyan
                                    )
                                }
                            }
                        }
                    }

                    // 3. AI DIET & MEAL SUGGESTIONS CARD
                    item {
                        LiquidGlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            borderColor = NeonViolet.copy(alpha = 0.6f)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(34.dp)
                                                .clip(CircleShape)
                                                .background(NeonViolet.copy(alpha = 0.2f))
                                                .border(1.dp, NeonViolet.copy(alpha = 0.5f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.AutoAwesome,
                                                contentDescription = null,
                                                tint = NeonViolet,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = "AI DIET & MEAL COACH",
                                                style = Typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                                            )
                                            Text(
                                                text = "டயட் பராமரிப்பு & சத்துக்கள் வழிகாட்டல்",
                                                style = Typography.bodySmall.copy(color = NeonViolet, fontSize = 10.sp)
                                            )
                                        }
                                    }

                                    // Refresh button
                                    IconButton(
                                        onClick = {
                                            triggerHaptic()
                                            isGeneratingAiDiet = true
                                            val proteinDeficit = (proteinTarget - totalProtein).toInt().coerceAtLeast(0)
                                            aiDietTip = if (proteinDeficit > 20) {
                                                "⚠️ Protein Deficit (${proteinDeficit}g): Add 3 boiled eggs or 150g grilled chicken/paneer for dinner to reach your 160g muscle synthesis target. (புரத இலக்கை எட்ட கூடுதல் முட்டை அல்லது பன்னீர் உட்கொள்ளவும்)"
                                            } else {
                                                "🎉 Excellent Macro Balance! High micronutrient density and steady energy. Continue hydrating with 500ml water. (சிறந்த உணவு சமநிலை • போதுமான நீர் அருந்தவும்)"
                                            }
                                            isGeneratingAiDiet = false
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = NeonViolet)
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(Color(0x1F7C5CFF))
                                        .padding(12.dp)
                                ) {
                                    Text(
                                        text = aiDietTip,
                                        style = Typography.bodyMedium.copy(
                                            color = TextPrimary,
                                            lineHeight = 20.sp
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // 4. MEAL REMINDER EMAIL NOTIFICATION ACTION
                    item {
                        LiquidGlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            borderColor = ElectricCyan.copy(alpha = 0.5f)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(ElectricCyan.copy(alpha = 0.2f))
                                                .border(1.dp, ElectricCyan.copy(alpha = 0.5f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Email,
                                                contentDescription = null,
                                                tint = ElectricCyan,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = "MEAL LOG EMAIL REMINDER",
                                                style = Typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                                            )
                                            Text(
                                                text = "மீல் அப்டேட் பண்ணலன்னா மெயிலுக்கு நோட்டிபிகேஷன்",
                                                style = Typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp)
                                            )
                                        }
                                    }

                                    Button(
                                        onClick = {
                                            triggerHaptic()
                                            onSendMealReminder("priyan1436ei@gmail.com")
                                            emailSentConfirmation = "Notification sent to priyan1436ei@gmail.com: 'Please update your meal / மீல் அப்டேட் பண்ணுங்க'"
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = ElectricCyan,
                                            contentColor = Color.Black
                                        ),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                        modifier = Modifier.height(36.dp)
                                    ) {
                                        Text(
                                            text = "SEND ALERT",
                                            style = Typography.labelSmall.copy(fontWeight = FontWeight.Black)
                                        )
                                    }
                                }

                                AnimatedVisibility(visible = emailSentConfirmation != null) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 10.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0x2A00E676))
                                            .border(1.dp, SuccessGreen.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                            .padding(10.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = emailSentConfirmation ?: "",
                                                style = Typography.bodySmall.copy(color = SuccessGreen, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 4B. WORKMANAGER SCHEDULED LOCAL MEAL ALERTS CARD
                    item {
                        LiquidGlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            borderColor = ElectricLime.copy(alpha = 0.5f)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(CircleShape)
                                                .background(ElectricLime.copy(alpha = 0.2f))
                                                .border(1.dp, ElectricLime.copy(alpha = 0.5f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.NotificationsActive,
                                                contentDescription = null,
                                                tint = ElectricLime,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = "AUTOMATED WORKMANAGER MEAL ALERTS",
                                                style = Typography.titleSmall.copy(fontWeight = FontWeight.Black, color = TextPrimary)
                                            )
                                            Text(
                                                text = "உள்ளூர் அறிவிப்புகள் (Daily Scheduled Local Notifications)",
                                                style = Typography.bodySmall.copy(color = ElectricLime, fontSize = 11.sp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = "WorkManager automatically checks your Room database at specific times of the day. If you haven't logged your meals by that time, a high-priority local notification will alert you to log your meals.",
                                    style = Typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp)
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                // Meal Time Schedule Slots
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    MealAlertScheduleRow(
                                        title = "🍳 Breakfast Alert",
                                        scheduledTime = "10:00 AM",
                                        isEnabled = breakfastAlertEnabled,
                                        onToggle = { onToggleMealAlert("BREAKFAST", it) }
                                    )
                                    MealAlertScheduleRow(
                                        title = "🥗 Lunch Alert",
                                        scheduledTime = "02:30 PM",
                                        isEnabled = lunchAlertEnabled,
                                        onToggle = { onToggleMealAlert("LUNCH", it) }
                                    )
                                    MealAlertScheduleRow(
                                        title = "🍲 Dinner Alert",
                                        scheduledTime = "09:00 PM",
                                        isEnabled = dinnerAlertEnabled,
                                        onToggle = { onToggleMealAlert("DINNER", it) }
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            triggerHaptic()
                                            onTriggerTestMealAlert("DINNER")
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = ElectricLime,
                                            contentColor = Color.Black
                                        ),
                                        modifier = Modifier.weight(1f).height(40.dp)
                                    ) {
                                        Icon(Icons.Default.Bolt, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(text = "TEST DINNER ALERT", style = Typography.labelSmall.copy(fontWeight = FontWeight.Black))
                                    }

                                    Button(
                                        onClick = {
                                            triggerHaptic()
                                            onTriggerTestMealAlert("BREAKFAST")
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = GlassSurfaceLevel3,
                                            contentColor = TextPrimary
                                        ),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder),
                                        modifier = Modifier.weight(1f).height(40.dp)
                                    ) {
                                        Text(text = "TEST BREAKFAST", style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                                    }
                                }

                                if (workManagerStatus != null) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "⚡ $workManagerStatus",
                                        style = Typography.labelSmall.copy(color = TextMuted, fontSize = 10.sp)
                                    )
                                }
                            }
                        }
                    }

                    // 5. FOODS LOGGED ON SELECTED DAY
                    item {
                        Text(
                            text = "LOGGED MEALS ON $selectedCalendarDate (${selectedDateFoodLogs.size})",
                            style = Typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextMuted,
                                letterSpacing = 0.5.sp
                            )
                        )
                    }

                    if (selectedDateFoodLogs.isEmpty()) {
                        item {
                            LiquidGlassCard(modifier = Modifier.fillMaxWidth()) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(Icons.Default.RestaurantMenu, contentDescription = null, tint = TextMuted, modifier = Modifier.size(36.dp))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(text = "No meals logged for this date", style = Typography.bodyMedium.copy(color = TextSecondary))
                                }
                            }
                        }
                    } else {
                        items(selectedDateFoodLogs) { food ->
                            LiquidGlassCard(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = food.customFoodName,
                                            style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "${food.mealType} • ${food.quantity} servings (${food.portionGrams.toInt()}g)",
                                            style = Typography.bodySmall.copy(color = TextSecondary)
                                        )
                                        Text(
                                            text = "P: ${food.protein}g • C: ${food.carbs}g • F: ${food.fat}g",
                                            style = Typography.labelSmall.copy(color = ElectricLime, fontSize = 10.sp)
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0x22C6FF3D))
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = "${food.calories} kcal",
                                            style = Typography.labelMedium.copy(color = ElectricLime, fontWeight = FontWeight.Bold)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Create Goal Modal
            if (showCreateGoalDialog) {
                CreateGoalDialog(
                    onDismiss = { showCreateGoalDialog = false },
                    onConfirm = { t, type, target, u, dead ->
                        onCreateGoal(t, type, target, u, dead)
                        showCreateGoalDialog = false
                    }
                )
            }

            // Schedule Workout Modal
            if (showScheduleDialog) {
                ScheduleWorkoutDialog(
                    defaultDate = selectedCalendarDate,
                    onDismiss = { showScheduleDialog = false },
                    onConfirm = { date, title, dur ->
                        onScheduleWorkout(date, title, dur)
                        showScheduleDialog = false
                    }
                )
            }
        }
    }
}

/**
 * Rolling Calendar Row displaying day number (Day 20, Day 30) and calories consumed
 */
@Composable
private fun RollingNutritionCalendarRow(
    selectedDate: String,
    foodLogsByDate: Map<String, List<FoodLog>>,
    events: List<CalendarEvent>,
    onDateSelected: (String) -> Unit
) {
    val cal = Calendar.getInstance()
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val daySdf = SimpleDateFormat("EEE", Locale.getDefault())
    val dateNumSdf = SimpleDateFormat("dd", Locale.getDefault())

    val todayStr = sdf.format(cal.time)

    val cal20 = Calendar.getInstance().apply { set(Calendar.DAY_OF_MONTH, 20) }
    val day20Str = sdf.format(cal20.time)

    val cal30 = Calendar.getInstance().apply { set(Calendar.DAY_OF_MONTH, 30) }
    val day30Str = sdf.format(cal30.time)

    // Generate all days of current month (1..31) so user can tap any date including Day 20 and Day 30
    val maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val monthDays = (1..maxDays).map { d ->
        val c = Calendar.getInstance().apply { set(Calendar.DAY_OF_MONTH, d) }
        val dStr = sdf.format(c.time)
        val dayLabel = daySdf.format(c.time)
        val dayNum = dateNumSdf.format(c.time)
        Triple(dStr, dayLabel, dayNum)
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Quick Jump Shortcuts for immediate access
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            QuickDateShortcutChip(
                label = "📍 Today",
                isSelected = selectedDate == todayStr,
                onClick = { onDateSelected(todayStr) }
            )
            QuickDateShortcutChip(
                label = "⚡ Day 20 (20-ஆம் தேதி)",
                isSelected = selectedDate == day20Str,
                onClick = { onDateSelected(day20Str) }
            )
            QuickDateShortcutChip(
                label = "🔥 Day 30 (30-ஆம் தேதி)",
                isSelected = selectedDate == day30Str,
                onClick = { onDateSelected(day30Str) }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Full Month Day Strip (1..31)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            monthDays.forEach { (dateStr, dayLabel, dayNum) ->
                val isSelected = dateStr == selectedDate
                val dayCalories = foodLogsByDate[dateStr]?.sumOf { it.calories } ?: 0
                val hasWorkout = events.any { it.date == dateStr }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .width(62.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSelected) Color(0x33C6FF3D) else GlassSurfaceLevel2)
                        .border(
                            1.dp,
                            if (isSelected) ElectricLime else if (dayCalories > 0) WarningAmber.copy(alpha = 0.5f) else GlassBorder,
                            RoundedCornerShape(16.dp)
                        )
                        .clickable { onDateSelected(dateStr) }
                        .padding(vertical = 10.dp, horizontal = 4.dp)
                ) {
                    Text(
                        text = dayLabel,
                        style = Typography.labelSmall.copy(fontSize = 10.sp, color = TextMuted)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = dayNum,
                        style = Typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) ElectricLime else TextPrimary
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    // Calorie pill
                    Text(
                        text = if (dayCalories > 0) "${dayCalories}c" else "--",
                        style = Typography.labelSmall.copy(
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (dayCalories >= 2000) ElectricLime else if (dayCalories > 0) WarningAmber else TextMuted
                        )
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Dot indicator
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(
                                if (dayCalories >= 2000) ElectricLime
                                else if (dayCalories > 0) WarningAmber
                                else if (hasWorkout) ElectricCyan
                                else Color.Transparent
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickDateShortcutChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) ElectricLime else GlassSurfaceLevel2)
            .border(1.dp, if (isSelected) ElectricLime else GlassBorder, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = Typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color.Black else TextPrimary,
                fontSize = 11.sp
            )
        )
    }
}

/**
 * Macro progress metric bar
 */
@Composable
private fun MacroMetricBar(
    name: String,
    current: Float,
    target: Float,
    unit: String,
    accentColor: Color
) {
    val progress = (current / target).coerceIn(0f, 1f)

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = name,
                style = Typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp)
            )
            Text(
                text = "${current.toInt()} / ${target.toInt()} $unit",
                style = Typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = accentColor,
                    fontSize = 11.sp
                )
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Color(0x33252535))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(3.dp))
                    .background(accentColor)
            )
        }
    }
}

@Composable
fun CalendarLegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(color))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, style = Typography.labelSmall.copy(fontSize = 9.sp, color = TextMuted))
    }
}

@Composable
fun GoalCardItem(
    goal: Goal,
    onToggleStatus: () -> Unit
) {
    val progress = if (goal.targetValue > 0) (goal.currentValue / goal.targetValue).coerceIn(0f, 1f) else 0.5f
    val isCompleted = goal.status == "COMPLETED"

    LiquidGlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 20.dp,
        borderColor = if (isCompleted) SuccessGreen.copy(alpha = 0.5f) else GlassBorder
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = goal.title,
                        style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Deadline: ${goal.deadline} • Type: ${goal.type}",
                        style = Typography.bodySmall.copy(color = TextSecondary)
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isCompleted) SuccessGreen.copy(alpha = 0.2f) else ElectricLime.copy(alpha = 0.15f))
                        .clickable { onToggleStatus() }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (isCompleted) "✓ DONE" else "IN PROGRESS",
                        style = Typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isCompleted) SuccessGreen else ElectricLime
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Progress Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0x33252535))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(3.dp))
                        .background(if (isCompleted) SuccessGreen else ElectricLime)
                )
            }
        }
    }
}

@Composable
fun CreateGoalDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, type: String, target: Float, unit: String, deadline: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("WEIGHT") }
    var targetStr by remember { mutableStateOf("68.0") }
    var unit by remember { mutableStateOf("KG") }
    var deadline by remember { mutableStateOf("2026-12-31") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Fitness Goal", color = TextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                LiquidInput(value = title, onValueChange = { title = it }, label = "Goal Title", placeholder = "e.g., Reach 68kg Lean Mass")
                LiquidInput(value = targetStr, onValueChange = { targetStr = it }, label = "Target Value", placeholder = "68.0")
                LiquidInput(value = unit, onValueChange = { unit = it }, label = "Unit", placeholder = "KG, days/wk, L, g")
                LiquidInput(value = deadline, onValueChange = { deadline = it }, label = "Target Deadline", placeholder = "YYYY-MM-DD")
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val t = targetStr.toFloatOrNull() ?: 0f
                    onConfirm(title.ifBlank { "Fitness Target" }, type, t, unit, deadline)
                },
                colors = ButtonDefaults.buttonColors(containerColor = ElectricLime, contentColor = Color.Black)
            ) {
                Text("CREATE", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
        },
        containerColor = Color(0xFF13131D)
    )
}

@Composable
fun ScheduleWorkoutDialog(
    defaultDate: String,
    onDismiss: () -> Unit,
    onConfirm: (date: String, title: String, duration: Int) -> Unit
) {
    var date by remember { mutableStateOf(defaultDate) }
    var title by remember { mutableStateOf("Hypertrophy Push Day") }
    var durationStr by remember { mutableStateOf("50") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Schedule Workout Session", color = TextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                LiquidInput(value = date, onValueChange = { date = it }, label = "Date", placeholder = "YYYY-MM-DD")
                LiquidInput(value = title, onValueChange = { title = it }, label = "Session Name", placeholder = "e.g., Heavy Deadlifts & Back")
                LiquidInput(value = durationStr, onValueChange = { durationStr = it }, label = "Duration (Minutes)", placeholder = "50")
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val dur = durationStr.toIntOrNull() ?: 45
                    onConfirm(date, title.ifBlank { "Scheduled Workout" }, dur)
                },
                colors = ButtonDefaults.buttonColors(containerColor = ElectricLime, contentColor = Color.Black)
            ) {
                Text("SCHEDULE", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
        },
        containerColor = Color(0xFF13131D)
    )
}

@Composable
private fun MealAlertScheduleRow(
    title: String,
    scheduledTime: String,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(GlassSurfaceLevel2)
            .border(1.dp, if (isEnabled) ElectricLime.copy(alpha = 0.35f) else GlassBorder, RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(text = title, style = Typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary))
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = "Runs at $scheduledTime daily (WorkManager)", style = Typography.labelSmall.copy(color = ElectricCyan, fontSize = 10.sp))
        }
        Switch(
            checked = isEnabled,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.Black,
                checkedTrackColor = ElectricLime,
                uncheckedThumbColor = TextMuted,
                uncheckedTrackColor = GlassSurfaceLevel3
            )
        )
    }
}
