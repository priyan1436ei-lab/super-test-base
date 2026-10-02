package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.User
import com.example.ui.components.*
import com.example.ui.theme.*
import org.json.JSONArray
import org.json.JSONObject

@Composable
fun AiWorkoutGeneratorScreen(
    user: User?,
    isGenerating: Boolean,
    generatedPlanJson: String?,
    onGenerate: (goal: String, exp: String, eq: String, days: Int, dur: Int, pref: String) -> Unit,
    onSavePlan: () -> Unit,
    onBack: () -> Unit
) {
    var goal by remember { mutableStateOf(user?.fitnessGoal ?: "Build Muscle") }
    var experience by remember { mutableStateOf(user?.experienceLevel ?: "Intermediate") }
    var equipment by remember { mutableStateOf(user?.equipment ?: "Dumbbells, Barbell, Bench") }
    var daysPerWeek by remember { mutableIntStateOf(user?.workoutDaysPerWeek ?: 5) }
    var durationMin by remember { mutableIntStateOf(user?.preferredWorkoutDuration ?: 50) }
    var preference by remember { mutableStateOf("Hypertrophy focus, emphasize upper chest and lats") }

    // Neon glowing border animation for generation state
    val infiniteTransition = rememberInfiniteTransition(label = "glow_anim")
    val glowPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "glow_phase"
    )

    AmbientLiquidMeshBackground {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(20.dp)
        ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LiquidIconButton(onClick = onBack, contentDescription = "Back") {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = TextPrimary)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = ElectricLime, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "AI WORKOUT GENERATOR",
                        style = Typography.titleMedium.copy(fontWeight = FontWeight.Black, color = TextPrimary)
                    )
                }

                Spacer(modifier = Modifier.size(48.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Body
            if (isGenerating) {
                // High-Thinking Liquid Neon Generation Loading Card
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    LiquidGlassCard(
                        modifier = Modifier
                            .fillMaxWidth(0.92f)
                            .border(
                                width = 2.dp,
                                brush = Brush.sweepGradient(
                                    colors = listOf(ElectricLime, NeonViolet, ElectricCyan, ElectricLime)
                                ),
                                shape = RoundedCornerShape(26.dp)
                            ),
                        cornerRadius = 26.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x3319E3FF)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Psychology,
                                    contentDescription = null,
                                    tint = ElectricCyan,
                                    modifier = Modifier.size(36.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Text(
                                text = "CREATING YOUR PLAN",
                                style = Typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Black,
                                    color = ElectricLime,
                                    letterSpacing = 1.sp
                                )
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "High Thinking Active • Calibrating volume load, fatigue dissipation, and progressive overload sequences for $goal.",
                                style = Typography.bodyMedium.copy(color = TextSecondary),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            ProgressBar(
                                progress = 0.72f,
                                color = ElectricLime,
                                modifier = Modifier.fillMaxWidth(0.8f)
                            )
                        }
                    }
                }
            } else if (generatedPlanJson != null) {
                // Generated Plan Preview
                val planDetails = remember(generatedPlanJson) { parsePlan(generatedPlanJson) }

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        LiquidHeroCard(modifier = Modifier.fillMaxWidth()) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ElectricLime, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "GENERATED PROTOCOL",
                                        style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = ElectricLime)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = planDetails.first,
                                    style = Typography.headlineSmall.copy(fontWeight = FontWeight.Black, color = TextPrimary)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Goal: $goal • ${planDetails.second.size} Training Days",
                                    style = Typography.bodySmall.copy(color = TextSecondary)
                                )
                            }
                        }
                    }

                    items(planDetails.second) { day ->
                        LiquidGlassCard(modifier = Modifier.fillMaxWidth()) {
                            Column {
                                Text(
                                    text = day.first,
                                    style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = ElectricLime)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = day.second,
                                    style = Typography.bodyMedium.copy(color = TextSecondary)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    LiquidSecondaryButton(
                        text = "Regenerate",
                        onClick = {
                            onGenerate(goal, experience, equipment, daysPerWeek, durationMin, preference)
                        },
                        modifier = Modifier.weight(1f)
                    )

                    LiquidPrimaryButton(
                        text = "SAVE TO MY PLANS",
                        onClick = onSavePlan,
                        modifier = Modifier.weight(1.5f)
                    )
                }
            } else {
                // Form input to configure plan
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Text(
                            text = "Configure Training Parameters",
                            style = Typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                        )
                        Text(
                            text = "Our Gemini 3.1 Pro AI engine synthesizes a customized workout protocol.",
                            style = Typography.bodySmall.copy(color = TextSecondary),
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    item {
                        LiquidInput(
                            value = goal,
                            onValueChange = { goal = it },
                            label = "Primary Fitness Goal",
                            placeholder = "e.g. Build Muscle, Strength, Fat Loss"
                        )
                    }

                    item {
                        LiquidInput(
                            value = equipment,
                            onValueChange = { equipment = it },
                            label = "Available Equipment",
                            placeholder = "e.g. Full Gym, Dumbbells only, Barbell & Bench"
                        )
                    }

                    item {
                        Text(text = "Days Per Week ($daysPerWeek Days)", style = Typography.labelMedium.copy(color = TextSecondary))
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(3, 4, 5, 6).forEach { d ->
                                LiquidChip(
                                    text = "$d Days",
                                    isSelected = daysPerWeek == d,
                                    onClick = { daysPerWeek = d }
                                )
                            }
                        }
                    }

                    item {
                        LiquidInput(
                            value = preference,
                            onValueChange = { preference = it },
                            label = "Focus & Special Preferences",
                            placeholder = "e.g. Prioritize upper chest and lats"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                LiquidPrimaryButton(
                    text = "GENERATE AI WORKOUT PLAN",
                    onClick = {
                        onGenerate(goal, experience, equipment, daysPerWeek, durationMin, preference)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = BgPrimary)
                    }
                )
            }
        }
    }
    }
}

private fun parsePlan(json: String): Pair<String, List<Pair<String, String>>> {
    return try {
        val obj = JSONObject(json)
        val name = obj.optString("planName", "Custom AI Plan")
        val days = obj.optJSONArray("days") ?: JSONArray()
        val list = mutableListOf<Pair<String, String>>()
        for (i in 0 until days.length()) {
            val d = days.getJSONObject(i)
            val dayName = d.optString("day", "Day ${i + 1}")
            val title = d.optString("title", "")
            val exercises = d.optJSONArray("exercises")
            val exCount = exercises?.length() ?: 0
            list.add(Pair(dayName, "$title ($exCount Exercises)"))
        }
        Pair(name, list)
    } catch (e: Exception) {
        Pair("AI Workout Protocol", listOf(Pair("Full Body Split", "Compound Movements")))
    }
}
