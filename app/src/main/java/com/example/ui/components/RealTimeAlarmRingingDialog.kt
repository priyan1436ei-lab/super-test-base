package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.FitnessAlarm
import com.example.ui.theme.*

/**
 * Real-Time Alarm Ringing Dialog
 * Pops up whenever a scheduled or test alarm triggers in real-time.
 * Features glowing radar pulse rings, bilingual Tamil/English copy, and action targets.
 */
@Composable
fun RealTimeAlarmRingingDialog(
    alarm: FitnessAlarm,
    onDismiss: () -> Unit,
    onSnooze: (minutes: Int) -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val radarScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "radarScale"
    )
    val radarAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "radarAlpha"
    )

    val categoryColor = when (alarm.category.uppercase()) {
        "WORKOUT" -> ElectricLime
        "HYDRATION" -> ElectricCyan
        "MEAL" -> WarningAmber
        "SUPPLEMENT" -> NeonViolet
        "SLEEP" -> Color(0xFF818CF8)
        else -> ElectricLime
    }

    val categoryIcon = when (alarm.category.uppercase()) {
        "WORKOUT" -> Icons.Default.FitnessCenter
        "HYDRATION" -> Icons.Default.WaterDrop
        "MEAL" -> Icons.Default.Restaurant
        "SUPPLEMENT" -> Icons.Default.Medication
        "SLEEP" -> Icons.Default.Bedtime
        else -> Icons.Default.NotificationsActive
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.85f))
                .testTag("alarm_ringing_dialog")
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(32.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF1B1B26), Color(0xFF0F0F16))
                        )
                    )
                    .border(
                        1.5.dp,
                        Brush.linearGradient(
                            listOf(
                                categoryColor.copy(alpha = 0.8f),
                                Color(0x33FFFFFF),
                                categoryColor.copy(alpha = 0.3f)
                            )
                        ),
                        RoundedCornerShape(32.dp)
                    )
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Radar Glowing Pulse Icon
                Box(
                    modifier = Modifier.size(110.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .scale(radarScale)
                            .clip(CircleShape)
                            .background(categoryColor.copy(alpha = radarAlpha))
                    )
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(categoryColor.copy(alpha = 0.2f))
                            .border(1.5.dp, categoryColor.copy(alpha = 0.6f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = categoryIcon,
                            contentDescription = null,
                            tint = categoryColor,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Time Display
                Text(
                    text = alarm.getFormattedTime(),
                    style = Typography.displaySmall.copy(
                        color = TextPrimary,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Category Tag
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(categoryColor.copy(alpha = 0.15f))
                        .border(1.dp, categoryColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "● REAL-TIME ${alarm.category.uppercase()} ALARM",
                        style = Typography.labelSmall.copy(
                            color = categoryColor,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Alarm Title
                Text(
                    text = alarm.title,
                    style = Typography.titleLarge.copy(
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                )

                // Tamil Translation / Subtitle
                if (alarm.labelTamil.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = alarm.labelTamil,
                        style = Typography.bodyMedium.copy(
                            color = categoryColor,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        )
                    )
                }

                // Target Goal (e.g., 500ml water, 45m workout)
                if (alarm.targetValue.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0x22FFFFFF))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Target: ${alarm.targetValue}",
                            style = Typography.bodySmall.copy(
                                color = TextSecondary,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Action Buttons: Snooze & Complete
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Snooze Button
                    Button(
                        onClick = { onSnooze(alarm.snoozeMinutes) },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("alarm_snooze_button"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0x33252535),
                            contentColor = TextPrimary
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x44FFFFFF))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Snooze,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Snooze ${alarm.snoozeMinutes}m",
                            style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    // Complete / Dismiss Button
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1.2f)
                            .height(50.dp)
                            .testTag("alarm_dismiss_button"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = categoryColor,
                            contentColor = Color.Black
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Dismiss",
                            style = Typography.labelMedium.copy(fontWeight = FontWeight.Black)
                        )
                    }
                }
            }
        }
    }
}
