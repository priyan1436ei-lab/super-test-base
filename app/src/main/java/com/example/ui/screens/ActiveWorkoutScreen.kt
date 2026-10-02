package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Exercise
import com.example.data.model.WorkoutSession
import com.example.data.model.WorkoutSet
import com.example.ui.components.*
import com.example.ui.navigation.Screen
import com.example.ui.theme.*

@Composable
fun ActiveWorkoutScreen(
    workoutName: String,
    elapsedSeconds: Long,
    isPaused: Boolean,
    exercises: List<Exercise>,
    currentExerciseIndex: Int,
    sets: List<WorkoutSet>,
    restTimerSeconds: Int,
    isRestTimerRunning: Boolean,
    onTogglePause: () -> Unit,
    onCompleteSet: (Int, Float, Int) -> Unit,
    onAddSet: () -> Unit,
    onRemoveSet: (Int) -> Unit,
    onNextExercise: () -> Unit,
    onPreviousExercise: () -> Unit,
    onAddRestTime: (Int) -> Unit,
    onSkipRestTime: () -> Unit,
    onFinishWorkout: (String) -> Unit,
    onBack: () -> Unit
) {
    val currentExercise = exercises.getOrNull(currentExerciseIndex)
    val exerciseSets = sets.filter { it.exerciseId == (currentExercise?.exerciseId ?: "") }

    var showEndDialog by remember { mutableStateOf(false) }
    var workoutNotes by remember { mutableStateOf("") }

    // Format timer hh:mm:ss
    val hours = elapsedSeconds / 3600
    val minutes = (elapsedSeconds % 3600) / 60
    val seconds = elapsedSeconds % 60
    val timerStr = if (hours > 0) {
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }

    AmbientLiquidMeshBackground {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Top Bar: Back, Workout Name, Elapsed Timer, Pause & End Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LiquidIconButton(
                    onClick = { showEndDialog = true },
                    contentDescription = "Back"
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        tint = TextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = workoutName.uppercase(),
                        style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = TextMuted)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isPaused) WarningAmber else ElectricLime)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = timerStr,
                            style = Typography.headlineMedium.copy(
                                fontWeight = FontWeight.Black,
                                color = if (isPaused) WarningAmber else TextPrimary
                            )
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    LiquidIconButton(
                        onClick = onTogglePause,
                        contentDescription = if (isPaused) "Resume" else "Pause",
                        backgroundColor = if (isPaused) WarningAmber.copy(alpha = 0.2f) else GlassSurfaceLevel2
                    ) {
                        Icon(
                            imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                            contentDescription = null,
                            tint = if (isPaused) WarningAmber else TextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    LiquidIconButton(
                        onClick = { showEndDialog = true },
                        contentDescription = "Finish Workout",
                        backgroundColor = Color(0x33F87171)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = ElectricLime,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Current Exercise Header Card
            if (currentExercise != null) {
                LiquidHeroCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "EXERCISE ${currentExerciseIndex + 1} OF ${exercises.size}",
                                style = Typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = ElectricLime
                                )
                            )

                            Text(
                                text = currentExercise.muscleGroup,
                                style = Typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = ElectricCyan
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = currentExercise.name,
                            style = Typography.headlineSmall.copy(
                                fontWeight = FontWeight.Black,
                                color = TextPrimary
                            )
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Target: ${currentExercise.defaultSets} sets • ${currentExercise.defaultReps} reps • ${currentExercise.defaultRestSeconds}s rest",
                            style = Typography.bodySmall.copy(color = TextSecondary)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sets Table Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "SET", style = Typography.labelSmall.copy(color = TextMuted), modifier = Modifier.weight(0.7f))
                Text(text = "PREVIOUS", style = Typography.labelSmall.copy(color = TextMuted), modifier = Modifier.weight(1.2f))
                Text(text = "KG", style = Typography.labelSmall.copy(color = TextMuted), modifier = Modifier.weight(1f))
                Text(text = "REPS", style = Typography.labelSmall.copy(color = TextMuted), modifier = Modifier.weight(1f))
                Text(text = "LOG", style = Typography.labelSmall.copy(color = TextMuted), modifier = Modifier.weight(0.8f))
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Sets List
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                itemsIndexed(exerciseSets) { index, setItem ->
                    val globalIndex = sets.indexOf(setItem)
                    ActiveSetRowItem(
                        setIndex = index + 1,
                        setItem = setItem,
                        onComplete = { w, r ->
                            if (globalIndex != -1) {
                                onCompleteSet(globalIndex, w, r)
                            }
                        },
                        onDelete = {
                            if (globalIndex != -1) {
                                onRemoveSet(globalIndex)
                            }
                        }
                    )
                }

                item {
                    // Add Set Button
                    LiquidSecondaryButton(
                        text = "+ ADD SET",
                        onClick = onAddSet,
                        modifier = Modifier.fillMaxWidth(),
                        borderColor = ElectricLime.copy(alpha = 0.35f),
                        textColor = ElectricLime
                    )
                }

                item {
                    // Exercise Navigation (Previous / Next)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        LiquidSecondaryButton(
                            text = "Prev Exercise",
                            onClick = onPreviousExercise,
                            modifier = Modifier.weight(1f)
                        )
                        LiquidPrimaryButton(
                            text = if (currentExerciseIndex < exercises.size - 1) "Next Exercise" else "Finish Session",
                            onClick = {
                                if (currentExerciseIndex < exercises.size - 1) {
                                    onNextExercise()
                                } else {
                                    showEndDialog = true
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Floating Rest Timer Overlay Card
        if (isRestTimerRunning && restTimerSeconds > 0) {
            FloatingRestTimerCard(
                remainingSeconds = restTimerSeconds,
                onAdd15Sec = { onAddRestTime(15) },
                onSkip = onSkipRestTime,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp)
            )
        }

        // End Workout Confirmation Dialog
        if (showEndDialog) {
            EndWorkoutConfirmationDialog(
                onDismiss = { showEndDialog = false },
                onConfirm = {
                    showEndDialog = false
                    onFinishWorkout(workoutNotes)
                },
                notes = workoutNotes,
                onNotesChange = { workoutNotes = it }
            )
        }
    }
    }
}

@Composable
fun ActiveSetRowItem(
    setIndex: Int,
    setItem: WorkoutSet,
    onComplete: (Float, Int) -> Unit,
    onDelete: () -> Unit
) {
    var weightInput by remember(setItem.weightKg) { mutableStateOf(setItem.weightKg.toString()) }
    var repsInput by remember(setItem.reps) { mutableStateOf(setItem.reps.toString()) }

    LiquidGlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 16.dp,
        backgroundColor = if (setItem.completed) Color(0x2834D399) else GlassSurfaceLevel2,
        borderColor = if (setItem.completed) SuccessGreen.copy(alpha = 0.5f) else GlassBorder
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Set number
            Text(
                text = "$setIndex",
                style = Typography.titleMedium.copy(
                    fontWeight = FontWeight.Black,
                    color = if (setItem.completed) SuccessGreen else ElectricLime
                ),
                modifier = Modifier.weight(0.7f)
            )

            // Previous set reference
            Text(
                text = "${setItem.weightKg}kg × ${setItem.reps}",
                style = Typography.bodySmall.copy(color = TextMuted),
                modifier = Modifier.weight(1.2f)
            )

            // Weight input
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(GlassSurfaceLevel3)
                    .border(1.dp, GlassBorder, RoundedCornerShape(10.dp))
                    .padding(horizontal = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.foundation.text.BasicTextField(
                    value = weightInput,
                    onValueChange = { weightInput = it },
                    textStyle = Typography.bodyMedium.copy(
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    ),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Reps input
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(GlassSurfaceLevel3)
                    .border(1.dp, GlassBorder, RoundedCornerShape(10.dp))
                    .padding(horizontal = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.foundation.text.BasicTextField(
                    value = repsInput,
                    onValueChange = { repsInput = it },
                    textStyle = Typography.bodyMedium.copy(
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    ),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Checkmark button
            Box(
                modifier = Modifier
                    .weight(0.8f)
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (setItem.completed) SuccessGreen else GlassSurfaceLevel3)
                    .clickable {
                        val w = weightInput.toFloatOrNull() ?: setItem.weightKg
                        val r = repsInput.toIntOrNull() ?: setItem.reps
                        onComplete(w, r)
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Log Set",
                    tint = if (setItem.completed) BgPrimary else TextMuted,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun FloatingRestTimerCard(
    remainingSeconds: Int,
    onAdd15Sec: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    val mins = remainingSeconds / 60
    val secs = remainingSeconds % 60
    val timeFormatted = String.format("%02d:%02d", mins, secs)

    Box(
        modifier = modifier
            .fillMaxWidth(0.92f)
            .clip(RoundedCornerShape(28.dp))
            .background(Color(0xF013131A))
            .border(1.5.dp, ElectricCyan, RoundedCornerShape(28.dp))
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0x3319E3FF)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        tint = ElectricCyan,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = "REST INTERVAL",
                        style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = TextMuted)
                    )
                    Text(
                        text = timeFormatted,
                        style = Typography.headlineMedium.copy(fontWeight = FontWeight.Black, color = ElectricCyan)
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LiquidSecondaryButton(
                    text = "+15s",
                    onClick = onAdd15Sec,
                    borderColor = ElectricCyan.copy(alpha = 0.5f),
                    textColor = ElectricCyan
                )

                LiquidPrimaryButton(
                    text = "SKIP",
                    onClick = onSkip
                )
            }
        }
    }
}

@Composable
fun EndWorkoutConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    notes: String,
    onNotesChange: (String) -> Unit
) {
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
                Text(
                    text = "Finish Workout?",
                    style = Typography.headlineSmall.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "All completed sets, total training volume, and estimated calories will be logged to your telemetry history.",
                    style = Typography.bodyMedium.copy(color = TextSecondary)
                )

                Spacer(modifier = Modifier.height(16.dp))

                LiquidInput(
                    value = notes,
                    onValueChange = onNotesChange,
                    label = "Session Notes (Optional)",
                    placeholder = "e.g., Hit 80kg bench PR! Good energy."
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    LiquidSecondaryButton(
                        text = "Resume",
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    )

                    LiquidPrimaryButton(
                        text = "Complete",
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
