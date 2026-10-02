package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun OnboardingScreen(
    onComplete: (
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
    ) -> Unit,
    onBackToAuth: () -> Unit
) {
    var step by remember { mutableIntStateOf(1) }
    val totalSteps = 10

    var name by remember { mutableStateOf("Priyan") }
    var ageStr by remember { mutableStateOf("26") }
    var gender by remember { mutableStateOf("Male") }

    var isMetricHeight by remember { mutableStateOf(true) }
    var heightCmStr by remember { mutableStateOf("178") }

    var isMetricWeight by remember { mutableStateOf(true) }
    var weightKgStr by remember { mutableStateOf("72.4") }
    var targetWeightKgStr by remember { mutableStateOf("68.0") }

    var goal by remember { mutableStateOf("Build Muscle") }
    var experience by remember { mutableStateOf("Intermediate") }
    var frequency by remember { mutableIntStateOf(5) }

    val selectedEquipment = remember {
        mutableStateListOf("Dumbbells", "Barbell", "Bench", "Cable Machine")
    }

    var diet by remember { mutableStateOf("Non-Vegetarian") }

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
            // Header: Back button + Progress bar + Step counter
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LiquidIconButton(
                        onClick = {
                            if (step > 1) step-- else onBackToAuth()
                        },
                        contentDescription = "Back"
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            tint = TextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Text(
                        text = "Step $step of $totalSteps",
                        style = Typography.labelLarge.copy(
                            color = ElectricLime,
                            fontWeight = FontWeight.Bold
                        )
                    )

                    // Spacer placeholder to balance header
                    Spacer(modifier = Modifier.size(48.dp))
                }

                Spacer(modifier = Modifier.height(14.dp))

                ProgressBar(
                    progress = step.toFloat() / totalSteps,
                    color = ElectricLime,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Step Content
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 16.dp)
            ) {
                AnimatedContent(
                    targetState = step,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "step_content"
                ) { currentStep ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.Center
                    ) {
                        when (currentStep) {
                            1 -> {
                                Text("What is your name?", style = Typography.headlineMedium, color = TextPrimary)
                                Text("We personalize your fitness telemetry and training coach.", style = Typography.bodyMedium, color = TextSecondary, modifier = Modifier.padding(top = 6.dp))
                                Spacer(modifier = Modifier.height(24.dp))
                                LiquidInput(
                                    value = name,
                                    onValueChange = { name = it },
                                    label = "First Name",
                                    placeholder = "e.g., Priyan"
                                )
                            }
                            2 -> {
                                Text("How old are you?", style = Typography.headlineMedium, color = TextPrimary)
                                Text("Used to compute metabolic expenditure and recovery indexes.", style = Typography.bodyMedium, color = TextSecondary, modifier = Modifier.padding(top = 6.dp))
                                Spacer(modifier = Modifier.height(24.dp))
                                LiquidInput(
                                    value = ageStr,
                                    onValueChange = { ageStr = it.filter { ch -> ch.isDigit() } },
                                    label = "Age (Years)",
                                    placeholder = "26"
                                )
                            }
                            3 -> {
                                Text("Select your gender", style = Typography.headlineMedium, color = TextPrimary)
                                Text("Helps calibrate hormonal baselines and caloric algorithms.", style = Typography.bodyMedium, color = TextSecondary, modifier = Modifier.padding(top = 6.dp))
                                Spacer(modifier = Modifier.height(24.dp))
                                listOf("Male", "Female", "Prefer not to say").forEach { g ->
                                    SelectableOptionCard(
                                        title = g,
                                        isSelected = gender == g,
                                        onClick = { gender = g }
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                }
                            }
                            4 -> {
                                Text("What is your height?", style = Typography.headlineMedium, color = TextPrimary)
                                Spacer(modifier = Modifier.height(16.dp))
                                LiquidSegmentControl(
                                    options = listOf("Centimeters (cm)", "Feet / Inches"),
                                    selectedIndex = if (isMetricHeight) 0 else 1,
                                    onOptionSelected = { isMetricHeight = it == 0 }
                                )
                                Spacer(modifier = Modifier.height(20.dp))
                                LiquidInput(
                                    value = heightCmStr,
                                    onValueChange = { heightCmStr = it },
                                    label = if (isMetricHeight) "Height in cm" else "Height (e.g., 5.10)",
                                    placeholder = if (isMetricHeight) "178" else "5.10"
                                )
                            }
                            5 -> {
                                Text("Current & target body weight", style = Typography.headlineMedium, color = TextPrimary)
                                Spacer(modifier = Modifier.height(16.dp))
                                LiquidSegmentControl(
                                    options = listOf("Kilograms (KG)", "Pounds (LB)"),
                                    selectedIndex = if (isMetricWeight) 0 else 1,
                                    onOptionSelected = { isMetricWeight = it == 0 }
                                )
                                Spacer(modifier = Modifier.height(20.dp))
                                LiquidInput(
                                    value = weightKgStr,
                                    onValueChange = { weightKgStr = it },
                                    label = "Current Weight",
                                    placeholder = "72.4"
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                LiquidInput(
                                    value = targetWeightKgStr,
                                    onValueChange = { targetWeightKgStr = it },
                                    label = "Target Weight",
                                    placeholder = "68.0"
                                )
                            }
                            6 -> {
                                Text("What is your primary goal?", style = Typography.headlineMedium, color = TextPrimary)
                                Spacer(modifier = Modifier.height(20.dp))
                                listOf(
                                    "Build Muscle" to "Maximize hypertrophy and lean muscular mass",
                                    "Lose Weight" to "Burn body fat while preserving lean tissue",
                                    "Stay Fit" to "Maintain longevity, cardiovascular health and posture",
                                    "Improve Endurance" to "High-stamina conditioning and VO2 max",
                                    "Gain Strength" to "Heavy compound powerlifting and neural drive"
                                ).forEach { (opt, sub) ->
                                    SelectableOptionCard(
                                        title = opt,
                                        subtitle = sub,
                                        isSelected = goal == opt,
                                        onClick = { goal = opt }
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                }
                            }
                            7 -> {
                                Text("Training experience", style = Typography.headlineMedium, color = TextPrimary)
                                Spacer(modifier = Modifier.height(20.dp))
                                listOf(
                                    "Beginner" to "< 1 year of consistent lifting",
                                    "Intermediate" to "1 - 3 years of structured progressive overload",
                                    "Advanced" to "3+ years with mastery of compound mechanics"
                                ).forEach { (exp, desc) ->
                                    SelectableOptionCard(
                                        title = exp,
                                        subtitle = desc,
                                        isSelected = experience == exp,
                                        onClick = { experience = exp }
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                }
                            }
                            8 -> {
                                Text("Workout frequency per week", style = Typography.headlineMedium, color = TextPrimary)
                                Text("How many days can you commit to training?", style = Typography.bodyMedium, color = TextSecondary, modifier = Modifier.padding(top = 6.dp))
                                Spacer(modifier = Modifier.height(20.dp))
                                listOf(2, 3, 4, 5, 6).forEach { days ->
                                    SelectableOptionCard(
                                        title = "$days Days per week",
                                        subtitle = when (days) {
                                            2 -> "Full Body Split (Efficient)"
                                            3 -> "Push / Pull / Legs Classic"
                                            4 -> "Upper / Lower Split"
                                            5 -> "Pro Split (Push, Pull, Legs, Upper, Core)"
                                            else -> "Advanced High-Volume Split"
                                        },
                                        isSelected = frequency == days,
                                        onClick = { frequency = days }
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                }
                            }
                            9 -> {
                                Text("Available equipment", style = Typography.headlineMedium, color = TextPrimary)
                                Text("Select all that apply to tailor workout generation.", style = Typography.bodyMedium, color = TextSecondary, modifier = Modifier.padding(top = 6.dp))
                                Spacer(modifier = Modifier.height(20.dp))
                                listOf(
                                    "No Equipment", "Dumbbells", "Barbell", "Bench",
                                    "Resistance Bands", "Cable Machine", "Full Gym"
                                ).forEach { eq ->
                                    val isSelected = selectedEquipment.contains(eq)
                                    SelectableOptionCard(
                                        title = eq,
                                        isSelected = isSelected,
                                        onClick = {
                                            if (isSelected) {
                                                if (selectedEquipment.size > 1) selectedEquipment.remove(eq)
                                            } else {
                                                selectedEquipment.add(eq)
                                            }
                                        }
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                }
                            }
                            10 -> {
                                Text("Dietary preference", style = Typography.headlineMedium, color = TextPrimary)
                                Text("Aligns macro recommendations with authentic meal logs.", style = Typography.bodyMedium, color = TextSecondary, modifier = Modifier.padding(top = 6.dp))
                                Spacer(modifier = Modifier.height(20.dp))
                                listOf("Non-Vegetarian", "Vegetarian", "Vegan", "Eggetarian").forEach { d ->
                                    SelectableOptionCard(
                                        title = d,
                                        isSelected = diet == d,
                                        onClick = { diet = d }
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                }
                            }
                        }
                    }
                }
            }

            // Bottom Navigation CTA
            Column {
                if (step < totalSteps) {
                    LiquidPrimaryButton(
                        text = "Continue",
                        onClick = { step++ },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = BgPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )
                } else {
                    LiquidPrimaryButton(
                        text = "BUILD MY PLAN",
                        onClick = {
                            val age = ageStr.toIntOrNull() ?: 26
                            val h = heightCmStr.toFloatOrNull() ?: 178f
                            val w = weightKgStr.toFloatOrNull() ?: 72.4f
                            val tw = targetWeightKgStr.toFloatOrNull() ?: 68.0f
                            onComplete(
                                name, age, gender, h, w, tw,
                                goal, experience, frequency,
                                selectedEquipment.joinToString(", "), diet
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
    }
}

@Composable
fun SelectableOptionCard(
    title: String,
    subtitle: String? = null,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    LiquidGlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 18.dp,
        borderColor = if (isSelected) ElectricLime else GlassBorder,
        backgroundColor = if (isSelected) Color(0x2EC6FF3D) else GlassSurfaceLevel2,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = Typography.titleMedium.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                        color = if (isSelected) ElectricLime else TextPrimary
                    )
                )
                if (subtitle != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = Typography.bodySmall.copy(color = TextSecondary)
                    )
                }
            }

            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(ElectricLime),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = BgPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
