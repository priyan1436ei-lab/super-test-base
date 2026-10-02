package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Exercise
import com.example.data.model.WorkoutPlan
import com.example.ui.components.*
import com.example.ui.navigation.Screen
import com.example.ui.theme.*

@Composable
fun WorkoutLibraryScreen(
    exercises: List<Exercise>,
    workoutPlans: List<WorkoutPlan>,
    onNavigate: (Screen) -> Unit,
    onStartWorkout: (String, List<Exercise>) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var selectedDifficulty by remember { mutableStateOf("All") }
    var selectedExerciseForDetail by remember { mutableStateOf<Exercise?>(null) }

    val categories = listOf(
        "All", "Chest", "Back", "Shoulders", "Arms", "Legs", "Abs",
        "Full Body", "Cardio", "HIIT", "Strength", "Mobility"
    )

    val filteredExercises = exercises.filter { ex ->
        val matchesSearch = searchQuery.isBlank() ||
                ex.name.contains(searchQuery, ignoreCase = true) ||
                ex.muscleGroup.contains(searchQuery, ignoreCase = true) ||
                ex.equipment.contains(searchQuery, ignoreCase = true)

        val matchesCategory = selectedCategory == "All" || ex.muscleGroup.equals(selectedCategory, ignoreCase = true)
        val matchesDifficulty = selectedDifficulty == "All" || ex.difficulty.equals(selectedDifficulty, ignoreCase = true)

        matchesSearch && matchesCategory && matchesDifficulty
    }

    AmbientLiquidMeshBackground {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
            // Header: Title & AI Workout Generator CTA
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "WORKOUT LIBRARY",
                            style = Typography.headlineMedium.copy(
                                fontWeight = FontWeight.Black,
                                color = TextPrimary
                            )
                        )
                        Text(
                            text = "${exercises.size} science-backed exercises & splits",
                            style = Typography.bodySmall.copy(color = TextSecondary)
                        )
                    }

                    LiquidSecondaryButton(
                        text = "AI Generator",
                        onClick = { onNavigate(Screen.AiGenerator) },
                        borderColor = ElectricLime.copy(alpha = 0.5f),
                        textColor = ElectricLime,
                        leadingIcon = {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = ElectricLime, modifier = Modifier.size(16.dp))
                        }
                    )
                }
            }

            // AI Generated Protocols Banner Card
            item {
                LiquidGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = NeonViolet.copy(alpha = 0.6f),
                    ambientGlowBrush = VioletCyanGradient,
                    onClick = { onNavigate(Screen.AiGenerator) }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "AI PROTOCOL GENERATOR",
                                style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = ElectricCyan)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Build Custom Adaptive Split",
                                style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Calibrated to your equipment, frequency & recovery.",
                                style = Typography.bodySmall.copy(color = TextSecondary)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color(0x3319E3FF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(24.dp))
                        }
                    }
                }
            }

            // Search Bar
            item {
                LiquidInput(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = "Search exercises, muscles, equipment...",
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = TextMuted)
                            }
                        } else {
                            Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted)
                        }
                    }
                )
            }

            // Muscle Group Category Filter Chips
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { cat ->
                        LiquidChip(
                            text = cat,
                            isSelected = selectedCategory == cat,
                            onClick = { selectedCategory = cat }
                        )
                    }
                }
            }

            // Difficulty Filter Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("All", "Beginner", "Intermediate", "Advanced").forEach { diff ->
                        val isSelected = selectedDifficulty == diff
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSelected) GlassSurfaceLevel3 else Color.Transparent)
                                .border(1.dp, if (isSelected) ElectricLime.copy(alpha = 0.5f) else GlassBorder, RoundedCornerShape(14.dp))
                                .clickable { selectedDifficulty = diff }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = diff,
                                style = Typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) ElectricLime else TextMuted
                                )
                            )
                        }
                    }
                }
            }

            // Exercise List
            if (filteredExercises.isEmpty()) {
                item {
                    EmptyState(
                        title = "No Exercises Found",
                        message = "Try changing your search keywords or muscle filter.",
                        ctaText = "Clear Filters",
                        onCtaClick = {
                            searchQuery = ""
                            selectedCategory = "All"
                            selectedDifficulty = "All"
                        },
                        icon = {
                            Icon(Icons.Default.SearchOff, contentDescription = null, tint = TextMuted, modifier = Modifier.size(28.dp))
                        }
                    )
                }
            } else {
                items(filteredExercises) { exercise ->
                    ExerciseCardItem(
                        exercise = exercise,
                        onClick = { selectedExerciseForDetail = exercise },
                        onStart = {
                            onStartWorkout(exercise.name, listOf(exercise))
                        }
                    )
                }
            }
        }

        // Exercise Detail Modal Sheet
        if (selectedExerciseForDetail != null) {
            val ex = selectedExerciseForDetail!!
            ExerciseDetailDialog(
                exercise = ex,
                onDismiss = { selectedExerciseForDetail = null },
                onStartExercise = {
                    selectedExerciseForDetail = null
                    onStartWorkout(ex.name, listOf(ex))
                }
            )
        }
    }
    }
}

@Composable
fun ExerciseCardItem(
    exercise: Exercise,
    onClick: () -> Unit,
    onStart: () -> Unit
) {
    LiquidGlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 20.dp,
        onClick = onClick
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = exercise.name,
                        style = Typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${exercise.muscleGroup} • Secondary: ${exercise.secondaryMuscles}",
                        style = Typography.bodySmall.copy(color = TextSecondary)
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            when (exercise.difficulty) {
                                "Beginner" -> SuccessGreen.copy(alpha = 0.18f)
                                "Advanced" -> ErrorRed.copy(alpha = 0.18f)
                                else -> WarningAmber.copy(alpha = 0.18f)
                            }
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = exercise.difficulty,
                        style = Typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = when (exercise.difficulty) {
                                "Beginner" -> SuccessGreen
                                "Advanced" -> ErrorRed
                                else -> WarningAmber
                            }
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "${exercise.defaultSets} sets × ${exercise.defaultReps}",
                        style = Typography.labelSmall.copy(fontWeight = FontWeight.SemiBold, color = ElectricLime)
                    )
                    Text(
                        text = "⏱ ${exercise.defaultRestSeconds}s rest",
                        style = Typography.labelSmall.copy(color = TextMuted)
                    )
                    Text(
                        text = "🏋️ ${exercise.equipment}",
                        style = Typography.labelSmall.copy(color = TextMuted)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(GlassSurfaceLevel3)
                        .clickable(onClick = onStart),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Start",
                        tint = ElectricLime,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ExerciseDetailDialog(
    exercise: Exercise,
    onDismiss: () -> Unit,
    onStartExercise: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.75f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.BottomCenter
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(BgCard)
                .border(1.dp, GlassBorderHighlight, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .clickable(enabled = false) {}
                .padding(24.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header Drag pill
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .width(44.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(0x44FFFFFF))
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = exercise.name,
                            style = Typography.headlineMedium.copy(fontWeight = FontWeight.Black, color = TextPrimary)
                        )
                        Text(
                            text = "${exercise.muscleGroup} • ${exercise.equipment}",
                            style = Typography.bodyMedium.copy(color = ElectricLime),
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    LiquidIconButton(onClick = onDismiss, contentDescription = "Close") {
                        Icon(Icons.Default.Close, contentDescription = null, tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Prescription chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricChipItem(label = "Sets", value = "${exercise.defaultSets}")
                    MetricChipItem(label = "Reps", value = exercise.defaultReps)
                    MetricChipItem(label = "Rest", value = "${exercise.defaultRestSeconds}s")
                    MetricChipItem(label = "Difficulty", value = exercise.difficulty)
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "INSTRUCTIONS",
                    style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = TextMuted, letterSpacing = 0.5.sp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = exercise.instructions,
                    style = Typography.bodyMedium.copy(color = TextPrimary, lineHeight = 22.sp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "SAFETY NOTES",
                    style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = WarningAmber, letterSpacing = 0.5.sp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = exercise.safetyNotes,
                    style = Typography.bodySmall.copy(color = TextSecondary, lineHeight = 20.sp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                LiquidPrimaryButton(
                    text = "START THIS EXERCISE",
                    onClick = onStartExercise,
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = BgPrimary)
                    }
                )
            }
        }
    }
}

@Composable
fun MetricChipItem(label: String, value: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(GlassSurfaceLevel2)
            .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = label.uppercase(), style = Typography.labelSmall.copy(fontSize = 9.sp, color = TextMuted))
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = value, style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary))
        }
    }
}
