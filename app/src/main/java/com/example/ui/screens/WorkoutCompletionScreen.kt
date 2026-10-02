package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.WorkoutSession
import com.example.ui.components.*
import com.example.ui.navigation.Screen
import com.example.ui.theme.*

@Composable
fun WorkoutCompletionScreen(
    session: WorkoutSession?,
    onNavigate: (Screen) -> Unit
) {
    val durationMin = ((session?.durationSeconds ?: 3120L) / 60).toInt()
    val calories = session?.caloriesEstimated ?: 420
    val volumeKg = session?.totalVolumeKg?.toInt() ?: 4850
    val exercisesCount = session?.completedExercisesCount ?: 6
    val setsCount = session?.totalSetsCount ?: 18
    val earnedXp = 350 + (volumeKg / 50)

    // Confetti particles animation
    val infiniteTransition = rememberInfiniteTransition(label = "confetti")
    val particleOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "confetti_anim"
    )

    AmbientLiquidMeshBackground {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(20.dp)
        ) {
        // Minimal particle effect
        Canvas(modifier = Modifier.fillMaxSize()) {
            val colors = listOf(ElectricLime, NeonViolet, ElectricCyan, WarningAmber)
            val randomOffsets = listOf(
                Offset(size.width * 0.2f, (size.height * particleOffset * 1.2f) % size.height),
                Offset(size.width * 0.45f, (size.height * (particleOffset + 0.3f) * 1.1f) % size.height),
                Offset(size.width * 0.75f, (size.height * (particleOffset + 0.6f) * 1.3f) % size.height),
                Offset(size.width * 0.85f, (size.height * (particleOffset + 0.15f) * 1.2f) % size.height),
                Offset(size.width * 0.15f, (size.height * (particleOffset + 0.8f) * 1.1f) % size.height)
            )
            randomOffsets.forEachIndexed { idx, offset ->
                drawCircle(
                    color = colors[idx % colors.size].copy(alpha = 0.6f),
                    radius = 5.dp.toPx(),
                    center = offset
                )
            }
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(20.dp))

                // Trophy badge
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color(0x33C6FF3D))
                        .border(2.dp, ElectricLime, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = "Trophy",
                        tint = ElectricLime,
                        modifier = Modifier.size(42.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "WORKOUT COMPLETE!",
                    style = Typography.headlineMedium.copy(
                        fontWeight = FontWeight.Black,
                        color = TextPrimary,
                        letterSpacing = 1.sp
                    )
                )

                Text(
                    text = "${session?.workoutName ?: "Push Day"} • Performance Logged",
                    style = Typography.bodyMedium.copy(color = ElectricLime),
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Stats Bento Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CompletionStatCard(
                        title = "TOTAL TIME",
                        value = "${durationMin}m",
                        accentColor = ElectricCyan,
                        modifier = Modifier.weight(1f)
                    )
                    CompletionStatCard(
                        title = "CALORIES",
                        value = "$calories",
                        accentColor = WarningAmber,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CompletionStatCard(
                        title = "VOLUME",
                        value = "${volumeKg} kg",
                        accentColor = NeonViolet,
                        modifier = Modifier.weight(1f)
                    )
                    CompletionStatCard(
                        title = "SETS DONE",
                        value = "$setsCount ($exercisesCount ex)",
                        accentColor = SuccessGreen,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // PR & XP Card
                LiquidHeroCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = ElectricLime, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "PERSONAL RECORD",
                                    style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = ElectricLime)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Highest Bench Volume +40kg",
                                style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0x33C6FF3D))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "+$earnedXp XP",
                                style = Typography.labelLarge.copy(fontWeight = FontWeight.Black, color = ElectricLime)
                            )
                        }
                    }
                }
            }

            // CTAs
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                LiquidPrimaryButton(
                    text = "SAVE & RETURN TO HOME",
                    onClick = { onNavigate(Screen.Home) },
                    modifier = Modifier.fillMaxWidth()
                )

                LiquidSecondaryButton(
                    text = "View Progress Analytics",
                    onClick = { onNavigate(Screen.Progress) },
                    modifier = Modifier.fillMaxWidth(),
                    textColor = TextPrimary
                )
            }
        }
    }
    }
}

@Composable
fun CompletionStatCard(
    title: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    LiquidGlassCard(
        modifier = modifier,
        cornerRadius = 18.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = title,
                style = Typography.labelSmall.copy(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    letterSpacing = 0.5.sp
                )
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = Typography.headlineMedium.copy(
                    fontWeight = FontWeight.Black,
                    color = accentColor
                )
            )
        }
    }
}
