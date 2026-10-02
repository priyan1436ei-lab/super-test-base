package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.User
import com.example.data.model.WaterLog
import com.example.data.model.WeightLog
import com.example.data.model.WorkoutSession
import com.example.data.model.WorkoutSet
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun ProgressAnalyticsScreen(
    user: User?,
    weightLogs: List<WeightLog>,
    waterLogs: List<WaterLog>,
    workoutSessions: List<WorkoutSession>,
    topWeightSets: List<WorkoutSet>,
    onLogWeight: (Float, String) -> Unit,
    onDeleteWeight: (WeightLog) -> Unit,
    onAddWater: (Int) -> Unit,
    onUndoWater: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Week", "Month", "3 Months", "Year")

    var showLogWeightModal by remember { mutableStateOf(false) }

    val currentWeight = user?.weightKg ?: 72.4f
    val targetWeight = user?.targetWeightKg ?: 68.0f
    val totalChange = -0.4f

    // Hydration metrics
    val todayWaterMl = waterLogs.sumOf { it.amountMl }
    val waterTargetMl = 3000
    val waterPercentage = (todayWaterMl.toFloat() / waterTargetMl).coerceIn(0f, 1f)

    // Total Volume
    val totalVolumeAllTime = workoutSessions.sumOf { it.totalVolumeKg.toDouble() }.toInt()
    val maxWeightLifted = topWeightSets.maxOfOrNull { it.weightKg } ?: 80f

    AmbientLiquidMeshBackground {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "PROGRESS & ANALYTICS",
                            style = Typography.headlineMedium.copy(fontWeight = FontWeight.Black, color = TextPrimary)
                        )
                        Text(
                            text = "Biometric telemetry and strength adaptation",
                            style = Typography.bodySmall.copy(color = TextSecondary)
                        )
                    }

                    LiquidSecondaryButton(
                        text = "+ Log Weight",
                        onClick = { showLogWeightModal = true },
                        borderColor = NeonViolet.copy(alpha = 0.5f),
                        textColor = NeonViolet
                    )
                }
            }

            // Time Horizon Segment Control
            item {
                LiquidSegmentControl(
                    options = tabs,
                    selectedIndex = selectedTab,
                    onOptionSelected = { selectedTab = it }
                )
            }

            // 1. BODY WEIGHT HERO CARD & TREND CHART
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
                                    text = "BODY WEIGHT TELEMETRY",
                                    style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = NeonViolet)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text(
                                        text = "$currentWeight",
                                        style = Typography.displayMedium.copy(fontWeight = FontWeight.Black, color = TextPrimary)
                                    )
                                    Text(
                                        text = " KG",
                                        style = Typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = TextMuted),
                                        modifier = Modifier.padding(bottom = 4.dp, start = 2.dp)
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(SuccessGreen.copy(alpha = 0.15f))
                                    .border(1.dp, SuccessGreen.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.AutoMirrored.Filled.TrendingDown, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "$totalChange KG (30d)",
                                        style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = SuccessGreen)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Custom Vector Trend Graph
                        WeightTrendCanvas(
                            weightLogs = weightLogs,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Target: $targetWeight KG", style = Typography.bodySmall.copy(color = TextSecondary))
                            Text(text = "Weekly Avg: 72.6 KG", style = Typography.bodySmall.copy(color = TextMuted))
                        }
                    }
                }
            }

            // 2. HYDRATION TRACKING CARD
            item {
                LiquidGlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(ElectricCyan.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.WaterDrop, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(22.dp))
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(text = "HYDRATION TRACKER", style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = TextMuted))
                                    Text(
                                        text = "${String.format("%.1f", todayWaterMl / 1000f)} / ${(waterTargetMl / 1000f)} L",
                                        style = Typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                                    )
                                }
                            }

                            Text(
                                text = "${(waterPercentage * 100).toInt()}% Done",
                                style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = ElectricCyan)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        ProgressBar(
                            progress = waterPercentage,
                            color = ElectricCyan,
                            modifier = Modifier.fillMaxWidth(),
                            height = 8.dp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Quick Water Logging Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            LiquidSecondaryButton(
                                text = "+250 ML",
                                onClick = { onAddWater(250) },
                                modifier = Modifier.weight(1f),
                                borderColor = ElectricCyan.copy(alpha = 0.4f),
                                textColor = ElectricCyan
                            )
                            LiquidSecondaryButton(
                                text = "+500 ML",
                                onClick = { onAddWater(500) },
                                modifier = Modifier.weight(1f),
                                borderColor = ElectricCyan.copy(alpha = 0.4f),
                                textColor = ElectricCyan
                            )
                            LiquidIconButton(
                                onClick = onUndoWater,
                                contentDescription = "Undo"
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = null, tint = TextMuted, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }

            // 3. PERSONAL RECORDS (PRs) HERO SECTION
            item {
                Column {
                    Text(
                        text = "PERSONAL RECORDS (PRs)",
                        style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = TextMuted, letterSpacing = 0.5.sp),
                        modifier = Modifier.padding(bottom = 10.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        PrCardItem(
                            title = "HEAVIEST LIFT",
                            value = "${maxWeightLifted.toInt()} KG",
                            exercise = "Barbell Bench Press",
                            modifier = Modifier.weight(1f)
                        )
                        PrCardItem(
                            title = "MAX VOLUME",
                            value = "${totalVolumeAllTime} KG",
                            exercise = "Lifetime Tonnage",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        PrCardItem(
                            title = "BEST STREAK",
                            value = "${user?.currentStreak ?: 6} Days",
                            exercise = "Unbroken Consistency",
                            modifier = Modifier.weight(1f)
                        )
                        PrCardItem(
                            title = "SESSIONS DONE",
                            value = "${user?.workoutsCompletedCount ?: 28}",
                            exercise = "Completed Workouts",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // 4. Weight Logs History List
            item {
                Column {
                    Text(
                        text = "RECENT WEIGHT ENTRIES",
                        style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = TextMuted, letterSpacing = 0.5.sp),
                        modifier = Modifier.padding(bottom = 10.dp)
                    )

                    LiquidGlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            if (weightLogs.isEmpty()) {
                                Text("No weight entries recorded yet.", style = Typography.bodyMedium.copy(color = TextMuted))
                            } else {
                                weightLogs.takeLast(5).reversed().forEach { log ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "${log.weightKg} ${log.unit}",
                                                style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                                            )
                                            Text(
                                                text = "${log.date} ${if (log.note.isNotBlank()) "• ${log.note}" else ""}",
                                                style = Typography.bodySmall.copy(color = TextSecondary)
                                            )
                                        }

                                        IconButton(onClick = { onDeleteWeight(log) }, modifier = Modifier.size(28.dp)) {
                                            Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = TextMuted, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Log Weight Modal
        if (showLogWeightModal) {
            LogWeightDialog(
                currentWeight = currentWeight,
                onDismiss = { showLogWeightModal = false },
                onConfirm = { w, n ->
                    onLogWeight(w, n)
                    showLogWeightModal = false
                }
            )
        }
    }
    }
}

@Composable
fun PrCardItem(
    title: String,
    value: String,
    exercise: String,
    modifier: Modifier = Modifier
) {
    LiquidGlassCard(modifier = modifier, cornerRadius = 18.dp) {
        Column {
            Text(text = title, style = Typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ElectricLime))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, style = Typography.headlineMedium.copy(fontWeight = FontWeight.Black, color = TextPrimary))
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = exercise, style = Typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp))
        }
    }
}

@Composable
fun WeightTrendCanvas(
    weightLogs: List<WeightLog>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val points = if (weightLogs.size >= 2) {
            weightLogs.map { it.weightKg }
        } else {
            listOf(73.8f, 73.2f, 72.8f, 72.4f)
        }

        val minW = points.minOrNull() ?: 70f
        val maxW = points.maxOrNull() ?: 75f
        val range = (maxW - minW).coerceAtLeast(1f)

        val wStep = size.width / (points.size - 1).coerceAtLeast(1)
        val path = Path()

        points.forEachIndexed { idx, w ->
            val x = idx * wStep
            val y = size.height - ((w - minW) / range) * (size.height - 20.dp.toPx()) - 10.dp.toPx()
            if (idx == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }

        // Draw smooth neon path
        drawPath(
            path = path,
            color = NeonViolet,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )

        // Draw points
        points.forEachIndexed { idx, w ->
            val x = idx * wStep
            val y = size.height - ((w - minW) / range) * (size.height - 20.dp.toPx()) - 10.dp.toPx()
            drawCircle(
                color = ElectricLime,
                radius = 4.dp.toPx(),
                center = Offset(x, y)
            )
        }
    }
}

@Composable
fun LogWeightDialog(
    currentWeight: Float,
    onDismiss: () -> Unit,
    onConfirm: (Float, String) -> Unit
) {
    var weightInput by remember { mutableStateOf(currentWeight.toString()) }
    var noteInput by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        LiquidGlassCard(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .clickable(enabled = false) {},
            cornerRadius = 24.dp
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(text = "Log Today's Weight", style = Typography.headlineSmall.copy(fontWeight = FontWeight.Bold, color = TextPrimary))
                Spacer(modifier = Modifier.height(16.dp))

                LiquidInput(
                    value = weightInput,
                    onValueChange = { weightInput = it },
                    label = "Weight (KG)",
                    placeholder = "72.4"
                )

                Spacer(modifier = Modifier.height(12.dp))

                LiquidInput(
                    value = noteInput,
                    onValueChange = { noteInput = it },
                    label = "Note (Optional)",
                    placeholder = "e.g., Fasted morning weigh-in"
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    LiquidSecondaryButton(text = "Cancel", onClick = onDismiss, modifier = Modifier.weight(1f))
                    LiquidPrimaryButton(
                        text = "SAVE ENTRY",
                        onClick = {
                            val w = weightInput.toFloatOrNull() ?: currentWeight
                            onConfirm(w, noteInput)
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
