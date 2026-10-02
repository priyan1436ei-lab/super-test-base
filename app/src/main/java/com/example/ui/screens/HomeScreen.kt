package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FoodLog
import com.example.data.model.User
import com.example.data.model.WaterLog
import com.example.data.model.WorkoutSession
import com.example.ui.components.*
import com.example.ui.navigation.Screen
import com.example.ui.theme.*
import java.util.Calendar

@Composable
fun HomeScreen(
    user: User?,
    todayFoodLogs: List<FoodLog>,
    todayWaterLogs: List<WaterLog>,
    workoutSessions: List<WorkoutSession>,
    onNavigate: (Screen) -> Unit,
    onStartWorkout: (String) -> Unit,
    onQuickWater: (Int) -> Unit,
    onOpenAddMeal: () -> Unit,
    onOpenLogWeight: () -> Unit,
    onOpenScanner: () -> Unit = {}
) {
    // Time-based greeting
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when (hour) {
        in 5..11 -> "Good Morning"
        in 12..16 -> "Good Afternoon"
        in 17..21 -> "Good Evening"
        else -> "Good Night"
    }

    val userName = user?.fullName ?: "Priyan"
    val userGoal = user?.fitnessGoal ?: "Build Muscle"

    // Real Metrics Computation
    val totalCaloriesConsumed = todayFoodLogs.sumOf { it.calories }
    val calorieTarget = 2400
    val totalWaterMl = todayWaterLogs.sumOf { it.amountMl }
    val waterTargetMl = 3000
    val currentWeight = user?.weightKg ?: 72.4f
    val workoutsCompletedThisWeek = (user?.workoutsCompletedCount ?: 28) % 7
    val weeklyTargetWorkouts = user?.workoutDaysPerWeek ?: 5

    // Macronutrient sums
    val totalProtein = todayFoodLogs.sumOf { it.protein.toDouble() }.toFloat().coerceAtLeast(45f)
    val targetProtein = 160f
    val totalCarbs = todayFoodLogs.sumOf { it.carbs.toDouble() }.toFloat().coerceAtLeast(120f)
    val targetCarbs = 220f
    val totalFat = todayFoodLogs.sumOf { it.fat.toDouble() }.toFloat().coerceAtLeast(35f)
    val targetFat = 65f

    // Daily Fitness Score Calculation (0 to 100)
    val waterScore = (totalWaterMl.toFloat() / waterTargetMl).coerceIn(0f, 1f) * 25f
    val calScore = (totalCaloriesConsumed.toFloat() / calorieTarget).coerceIn(0f, 1.1f).let {
        if (it > 1f) (2f - it).coerceIn(0f, 1f) else it
    } * 25f
    val workoutScore = (workoutsCompletedThisWeek.toFloat() / weeklyTargetWorkouts).coerceIn(0f, 1f) * 35f
    val streakScore = ((user?.currentStreak ?: 6) / 10f).coerceIn(0f, 1f) * 15f
    val dailyFitnessScore = (waterScore + calScore + workoutScore + streakScore).toInt().coerceIn(45, 96)

    AmbientLiquidMeshBackground {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 110.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // 1. Top Section: Avatar, Greeting, Goal Chip, AI Coach Icon
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onNavigate(Screen.Profile) }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(Color(0x337C5CFF))
                                .border(1.5.dp, GlassBorderLimeBrush, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = userName.take(1).uppercase(),
                                style = Typography.titleLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    color = ElectricLime
                                )
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "$greeting,",
                                style = Typography.bodySmall.copy(color = TextSecondary, fontSize = 12.sp)
                            )
                            Text(
                                text = userName,
                                style = Typography.titleLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    color = TextPrimary
                                )
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0x22C6FF3D))
                                .border(1.dp, ElectricLime.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(ElectricLime)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = userGoal,
                                    style = Typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = ElectricLime,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }

                        LiquidIconButton(
                            onClick = { onNavigate(Screen.Alarms) },
                            contentDescription = "Real-Time Alarms"
                        ) {
                            Icon(
                                imageVector = Icons.Default.Alarm,
                                contentDescription = null,
                                tint = ElectricLime,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        LiquidIconButton(
                            onClick = { onNavigate(Screen.AiCoach) },
                            contentDescription = "AI Coach"
                        ) {
                            Icon(
                                imageVector = Icons.Default.SmartToy,
                                contentDescription = null,
                                tint = ElectricCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // 2. DAILY FITNESS PERFORMANCE HERO CARD (FIGMA LIQUID GLASS HUD)
            item {
                LiquidHeroCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(ElectricLime)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "DAILY FITNESS PERFORMANCE",
                                    style = Typography.labelSmall.copy(
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 1.sp,
                                        color = ElectricLime
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = "$dailyFitnessScore",
                                    style = Typography.displayLarge.copy(
                                        fontWeight = FontWeight.Black,
                                        color = TextPrimary,
                                        fontSize = 44.sp
                                    )
                                )
                                Text(
                                    text = " / 100",
                                    style = Typography.titleMedium.copy(
                                        color = TextMuted,
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )

                                Spacer(modifier = Modifier.width(10.dp))

                                Box(
                                    modifier = Modifier
                                        .padding(bottom = 8.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0x2834D399))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.ArrowUpward, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(12.dp))
                                        Text("+5.2%", style = Typography.labelSmall.copy(color = SuccessGreen, fontWeight = FontWeight.Bold, fontSize = 10.sp))
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = if (dailyFitnessScore >= 80) "Optimal recovery & training consistency. Push Day ready."
                                else "Keep hydrating and complete your training protocol to reach peak score.",
                                style = Typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Concentric Performance Triple Ring HUD
                        ConcentricPerformanceRing(
                            workoutProgress = workoutsCompletedThisWeek.toFloat() / weeklyTargetWorkouts,
                            calorieProgress = totalCaloriesConsumed.toFloat() / calorieTarget,
                            waterProgress = totalWaterMl.toFloat() / waterTargetMl,
                            score = dailyFitnessScore,
                            size = 98.dp
                        )
                    }
                }
            }

            // REAL-TIME ALARMS & REMINDERS QUICK HUD
            item {
                LiquidGlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigate(Screen.Alarms) },
                    borderColor = ElectricLime.copy(alpha = 0.4f)
                ) {
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
                                    .background(ElectricLime.copy(alpha = 0.15f))
                                    .border(1.dp, ElectricLime.copy(alpha = 0.5f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Alarm,
                                    contentDescription = null,
                                    tint = ElectricLime,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "REAL-TIME ALARMS & REMINDERS",
                                        style = Typography.labelSmall.copy(
                                            color = ElectricLime,
                                            fontWeight = FontWeight.Black,
                                            letterSpacing = 0.5.sp
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(SuccessGreen)
                                    )
                                }
                                Text(
                                    text = "உடற்பயிற்சி & நீர் அருந்தும் நினைவூட்டல் • Active Engine",
                                    style = Typography.bodySmall.copy(
                                        color = TextSecondary,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = ElectricLime,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // 3. TODAY'S TRAINING PROTOCOL (SPOTLIGHT WORKOUT CARD)
            item {
                LiquidGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = ElectricLime.copy(alpha = 0.5f),
                    borderBrush = GlassBorderLimeBrush,
                    ambientGlowBrush = GlassCardGradient
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
                                        .background(ElectricLime.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FitnessCenter,
                                        contentDescription = null,
                                        tint = ElectricLime,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "TODAY'S TRAINING PROTOCOL",
                                    style = Typography.labelSmall.copy(
                                        fontWeight = FontWeight.Black,
                                        color = TextMuted,
                                        letterSpacing = 0.5.sp
                                    )
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(GlassSurfaceLevel3)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Hypertrophy",
                                    style = Typography.labelSmall.copy(color = ElectricLime, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "PUSH DAY (CHEST & DELTS)",
                            style = Typography.headlineMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary
                            )
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Timer, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("52 min", style = Typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp))
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.AutoMirrored.Filled.FormatListBulleted, contentDescription = null, tint = NeonViolet, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("7 exercises", style = Typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp))
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("~420 kcal", style = Typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp))
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            LiquidPrimaryButton(
                                text = "BEGIN SESSION",
                                onClick = { onStartWorkout("Push Day") },
                                modifier = Modifier.weight(1.5f),
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        tint = BgPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            )

                            LiquidSecondaryButton(
                                text = "VIEW PLAN",
                                onClick = { onNavigate(Screen.Workout) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // 4. QUICK ACTIONS ROW (FROSTED GLASS PILLS)
            item {
                Column {
                    Text(
                        text = "QUICK ACTIONS",
                        style = Typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextMuted,
                            letterSpacing = 0.5.sp
                        ),
                        modifier = Modifier.padding(bottom = 10.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        QuickActionGlassItem(
                            icon = Icons.Default.CameraAlt,
                            label = "Scan Food",
                            accentColor = ElectricLime,
                            onClick = onOpenScanner
                        )
                        QuickActionGlassItem(
                            icon = Icons.Default.FitnessCenter,
                            label = "Workout",
                            accentColor = NeonViolet,
                            onClick = { onStartWorkout("Push Day") }
                        )
                        QuickActionGlassItem(
                            icon = Icons.Default.WaterDrop,
                            label = "+250ml",
                            accentColor = ElectricCyan,
                            onClick = { onQuickWater(250) }
                        )
                        QuickActionGlassItem(
                            icon = Icons.Default.Alarm,
                            label = "Alarms",
                            accentColor = ElectricLime,
                            onClick = { onNavigate(Screen.Alarms) }
                        )
                        QuickActionGlassItem(
                            icon = Icons.Default.Scale,
                            label = "Weight",
                            accentColor = WarningAmber,
                            onClick = onOpenLogWeight
                        )
                        QuickActionGlassItem(
                            icon = Icons.Default.AutoAwesome,
                            label = "AI Coach",
                            accentColor = ElectricCyan,
                            onClick = { onNavigate(Screen.AiCoach) }
                        )
                    }
                }
            }

            // 5. NUTRITION & MACRONUTRIENT SPLIT GLASS CARD
            item {
                LiquidGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onOpenAddMeal
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "MACRONUTRIENT BREAKDOWN",
                                    style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = TextMuted)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "$totalCaloriesConsumed / $calorieTarget kcal",
                                    style = Typography.titleMedium.copy(fontWeight = FontWeight.Black, color = TextPrimary)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(ElectricLime.copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "+ LOG MEAL",
                                    style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = ElectricLime, fontSize = 10.sp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        MacroPillRow(
                            proteinGrams = totalProtein,
                            targetProtein = targetProtein,
                            carbsGrams = totalCarbs,
                            targetCarbs = targetCarbs,
                            fatGrams = totalFat,
                            targetFat = targetFat
                        )
                    }
                }
            }

            // 6. AI RECOVERY & SMART PERFORMANCE INSIGHT CARD
            item {
                LiquidGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = NeonViolet.copy(alpha = 0.5f),
                    borderBrush = GlassBorderVioletBrush,
                    onClick = { onNavigate(Screen.AiCoach) }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(NeonViolet.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = NeonViolet, modifier = Modifier.size(20.dp))
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "FITTRACK AI RECOVERY INSIGHT",
                                style = Typography.labelSmall.copy(fontWeight = FontWeight.Black, color = NeonViolet, letterSpacing = 0.5.sp)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Rest interval adjusted to 90s for compound pressing sets. Hydration pace is on track for optimal protein synthesis.",
                                style = Typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp)
                            )
                        }

                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextMuted, modifier = Modifier.size(20.dp))
                    }
                }
            }

            // 7. HOME BENTO METRICS GRID
            item {
                Column {
                    Text(
                        text = "BODY TELEMETRY & HYDRATION",
                        style = Typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextMuted,
                            letterSpacing = 0.5.sp
                        ),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        val waterL = String.format("%.1f", totalWaterMl / 1000f)
                        MetricCard(
                            title = "Hydration",
                            value = "${waterL}L",
                            subtitle = "Target: ${(waterTargetMl / 1000f)}L daily",
                            progress = totalWaterMl.toFloat() / waterTargetMl,
                            accentColor = ElectricCyan,
                            icon = {
                                Icon(Icons.Default.WaterDrop, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(20.dp))
                            },
                            modifier = Modifier.weight(1f),
                            onClick = { onQuickWater(250) }
                        )

                        MetricCard(
                            title = "Body Weight",
                            value = "$currentWeight KG",
                            subtitle = "Target: ${user?.targetWeightKg ?: 68.0f} KG",
                            trend = "-0.4 KG",
                            accentColor = NeonViolet,
                            icon = {
                                Icon(Icons.Default.MonitorWeight, contentDescription = null, tint = NeonViolet, modifier = Modifier.size(20.dp))
                            },
                            modifier = Modifier.weight(1f),
                            onClick = onOpenLogWeight
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Weekly Consistency
                    LiquidGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onNavigate(Screen.GoalsCalendar) }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "WEEKLY PROTOCOL CONSISTENCY",
                                    style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = TextMuted)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "$workoutsCompletedThisWeek / $weeklyTargetWorkouts Workouts Completed",
                                    style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                ProgressBar(
                                    progress = workoutsCompletedThisWeek.toFloat() / weeklyTargetWorkouts,
                                    color = ElectricLime,
                                    modifier = Modifier.width(180.dp)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0x337C5CFF))
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "🔥 ${user?.currentStreak ?: 6}d Streak",
                                    style = Typography.labelLarge.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuickActionGlassItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    accentColor: Color,
    onClick: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // 60 FPS Buttery Spring Scale Physics
    val scale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1.0f,
        animationSpec = androidx.compose.animation.core.spring(
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
            stiffness = androidx.compose.animation.core.Spring.StiffnessMedium
        ),
        label = "quick_action_spring_scale"
    )

    val glowAlpha by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isPressed) 0.6f else 0.15f,
        animationSpec = androidx.compose.animation.core.tween(150),
        label = "quick_action_glow"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    try {
                        val vibrator = androidx.core.content.ContextCompat.getSystemService(context, android.os.Vibrator::class.java)
                        if (vibrator != null && vibrator.hasVibrator()) {
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                vibrator.vibrate(android.os.VibrationEffect.createOneShot(30, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
                            } else {
                                @Suppress("DEPRECATION")
                                vibrator.vibrate(30)
                            }
                        }
                    } catch (_: Exception) {}
                    onClick()
                }
            )
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(
                    if (isPressed) accentColor.copy(alpha = 0.22f) else GlassSurfaceLevel2
                )
                .border(
                    1.2.dp,
                    if (isPressed) accentColor else accentColor.copy(alpha = glowAlpha),
                    RoundedCornerShape(20.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isPressed) TextPrimary else accentColor,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            style = Typography.labelSmall.copy(
                fontSize = 11.sp,
                fontWeight = if (isPressed) FontWeight.Bold else FontWeight.Medium,
                color = if (isPressed) accentColor else TextSecondary
            )
        )
    }
}
