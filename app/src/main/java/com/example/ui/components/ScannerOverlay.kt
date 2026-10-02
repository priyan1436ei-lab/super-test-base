package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

/**
 * Scanner Overlay UI Component for CameraCaptureScreen
 *
 * Features:
 * - Dynamic animated scanning laser beam with cyber glow trail
 * - High-tech corner reticle brackets with pulsing center target HUD
 * - Distance indicator & real-time detection tag (English + Tamil)
 * - Interactive Capture & Analyze Button that triggers food recognition
 * - Shutter flash animation and camera lens/torch overlay controls
 */
@Composable
fun ScannerOverlay(
    modifier: Modifier = Modifier,
    isScanning: Boolean = true,
    isAnalyzing: Boolean = false,
    distanceCm: Int = 24,
    detectedFoodName: String = "",
    detectedFoodTamilName: String = "",
    detectedCalories: Int = 0,
    confidencePct: Float = 98.4f,
    showFlash: Boolean = false,
    isTorchOn: Boolean = false,
    onCaptureClick: () -> Unit,
    onTorchToggleClick: (() -> Unit)? = null,
    onSwitchLensClick: (() -> Unit)? = null,
    onQuickFoodCycleClick: (() -> Unit)? = null
) {
    // Laser scanning animation
    val infiniteTransition = rememberInfiniteTransition(label = "scanner_laser_loop")
    val laserYRatio by infiniteTransition.animateFloat(
        initialValue = 0.06f,
        targetValue = 0.94f,
        animationSpec = infiniteRepeatable(
            animation = tween(1350, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_y_ratio"
    )

    // Subtle pulsing for corner reticles and center target
    val reticlePulse by infiniteTransition.animateFloat(
        initialValue = 0.97f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "reticle_pulse"
    )

    // Radar scan rotation for analyzing state
    val radarRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radar_rotation"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(22.dp))
    ) {
        // 1. DYNAMIC CANVAS: CORNER BRACKETS, TARGET RETICLE & LASER BEAM
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val pad = 20.dp.toPx()
            val bracketLen = 30.dp.toPx()
            val bracketStroke = 3.5.dp.toPx()
            val cornerColor = if (isAnalyzing) ElectricCyan else ElectricLime

            // --- Corner Reticle Brackets ---
            // Top-Left
            drawLine(cornerColor, Offset(pad, pad), Offset(pad + bracketLen, pad), bracketStroke, StrokeCap.Round)
            drawLine(cornerColor, Offset(pad, pad), Offset(pad, pad + bracketLen), bracketStroke, StrokeCap.Round)

            // Top-Right
            drawLine(cornerColor, Offset(w - pad, pad), Offset(w - pad - bracketLen, pad), bracketStroke, StrokeCap.Round)
            drawLine(cornerColor, Offset(w - pad, pad), Offset(w - pad, pad + bracketLen), bracketStroke, StrokeCap.Round)

            // Bottom-Left
            drawLine(cornerColor, Offset(pad, h - pad), Offset(pad + bracketLen, h - pad), bracketStroke, StrokeCap.Round)
            drawLine(cornerColor, Offset(pad, h - pad), Offset(pad, h - pad - bracketLen), bracketStroke, StrokeCap.Round)

            // Bottom-Right
            drawLine(cornerColor, Offset(w - pad, h - pad), Offset(w - pad - bracketLen, h - pad), bracketStroke, StrokeCap.Round)
            drawLine(cornerColor, Offset(w - pad, h - pad), Offset(w - pad, h - pad - bracketLen), bracketStroke, StrokeCap.Round)

            // --- Center Target Circle HUD ---
            val center = Offset(w / 2f, h / 2f)
            val ringRadius = 36.dp.toPx() * reticlePulse
            drawCircle(
                color = cornerColor.copy(alpha = 0.35f),
                radius = ringRadius,
                center = center,
                style = Stroke(width = 1.5.dp.toPx())
            )
            // Center crosshair ticks
            val crosshairLen = 8.dp.toPx()
            drawLine(cornerColor.copy(alpha = 0.6f), Offset(center.x - ringRadius - crosshairLen, center.y), Offset(center.x - ringRadius + crosshairLen, center.y), 1.5.dp.toPx())
            drawLine(cornerColor.copy(alpha = 0.6f), Offset(center.x + ringRadius - crosshairLen, center.y), Offset(center.x + ringRadius + crosshairLen, center.y), 1.5.dp.toPx())
            drawLine(cornerColor.copy(alpha = 0.6f), Offset(center.x, center.y - ringRadius - crosshairLen), Offset(center.x, center.y - ringRadius + crosshairLen), 1.5.dp.toPx())
            drawLine(cornerColor.copy(alpha = 0.6f), Offset(center.x, center.y + ringRadius - crosshairLen), Offset(center.x, center.y + ringRadius + crosshairLen), 1.5.dp.toPx())

            // --- Active Laser Scanning Beam with Trail ---
            if (isScanning && !isAnalyzing) {
                val currentLaserY = h * laserYRatio
                // Glow trail above/below
                val trailHeight = 16.dp.toPx()
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            ElectricLime.copy(alpha = 0.15f),
                            ElectricCyan.copy(alpha = 0.25f),
                            Color.Transparent
                        ),
                        startY = currentLaserY - trailHeight,
                        endY = currentLaserY + trailHeight
                    ),
                    topLeft = Offset(pad, currentLaserY - trailHeight),
                    size = androidx.compose.ui.geometry.Size(w - (pad * 2), trailHeight * 2)
                )

                // Main sharp laser beam
                drawLine(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            ElectricLime.copy(alpha = 0.85f),
                            ElectricCyan,
                            ElectricLime.copy(alpha = 0.85f),
                            Color.Transparent
                        )
                    ),
                    start = Offset(pad, currentLaserY),
                    end = Offset(w - pad, currentLaserY),
                    strokeWidth = 3.5.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }

        // 2. SHUTTER FLASH OVERLAY
        if (showFlash) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White.copy(alpha = 0.85f))
            )
        }

        // 3. TOP VIEWPORT HUD: DISTANCE SENSOR & QUICK CAMERA CONTROLS
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Distance Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xCC08080D))
                    .border(1.dp, GlassBorderHighlight, RoundedCornerShape(16.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(if (distanceCm in 20..30) SuccessGreen else WarningAmber)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "RANGE: ${distanceCm}cm • ${if (distanceCm in 20..30) "FOCUS LOCKED" else "ADJUST DISTANCE"}",
                        style = Typography.labelSmall.copy(color = TextPrimary, fontWeight = FontWeight.Black, fontSize = 9.5.sp)
                    )
                }
            }

            // Quick Camera Toggles (Torch & Lens)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (onTorchToggleClick != null) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (isTorchOn) Color(0x33C6FF3D) else Color(0xCC08080D))
                            .border(1.dp, if (isTorchOn) ElectricLime else GlassBorder, CircleShape)
                            .clickable { onTorchToggleClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isTorchOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                            contentDescription = "Torch Toggle",
                            tint = if (isTorchOn) ElectricLime else TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                if (onSwitchLensClick != null) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xCC08080D))
                            .border(1.dp, GlassBorder, CircleShape)
                            .clickable { onSwitchLensClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FlipCameraAndroid,
                            contentDescription = "Flip Lens",
                            tint = TextPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // 4. BOTTOM VIEWPORT HUD: LIVE FOOD TAG & DEDICATED CAPTURE/ANALYZE BUTTON
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color(0xCC07070B), Color(0xF207070B))
                    )
                )
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Live Food Detection Pill
            if (detectedFoodName.isNotBlank()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = detectedFoodName,
                                style = Typography.bodyMedium.copy(fontWeight = FontWeight.ExtraBold, color = TextPrimary),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (detectedCalories > 0) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "• ~$detectedCalories kcal",
                                    style = Typography.labelSmall.copy(color = WarningAmber, fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                        if (detectedFoodTamilName.isNotBlank()) {
                            Text(
                                text = detectedFoodTamilName,
                                style = Typography.labelSmall.copy(color = ElectricCyan, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Quick food cycle icon
                        if (onQuickFoodCycleClick != null) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x3319E3FF))
                                    .border(1.dp, ElectricCyan.copy(alpha = 0.5f), CircleShape)
                                    .clickable { onQuickFoodCycleClick() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Shuffle, contentDescription = "Cycle Food", tint = ElectricCyan, modifier = Modifier.size(16.dp))
                            }
                        }

                        // Confidence Match Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0x33C6FF3D))
                                .border(1.dp, ElectricLime, RoundedCornerShape(8.dp))
                                .padding(horizontal = 7.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "$confidencePct% MATCH",
                                style = Typography.labelSmall.copy(color = ElectricLime, fontWeight = FontWeight.Black, fontSize = 9.sp)
                            )
                        }
                    }
                }
            }

            // 5. INTEGRATED CAPTURE & ANALYZE BUTTON (Triggers food analysis)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Interactive Capture Button
                Button(
                    onClick = onCaptureClick,
                    enabled = !isAnalyzing,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isAnalyzing) ElectricCyan else ElectricLime,
                        contentColor = BgPrimary
                    ),
                    shape = RoundedCornerShape(24.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
                    modifier = Modifier
                        .testTag("scanner_capture_button")
                        .height(48.dp)
                        .scale(if (isAnalyzing) 0.95f else 1f)
                ) {
                    if (isAnalyzing) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Analyzing",
                            tint = BgPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ANALYZING FOOD...",
                            style = Typography.labelMedium.copy(fontWeight = FontWeight.Black, color = BgPrimary, letterSpacing = 0.5.sp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Camera,
                            contentDescription = "Capture",
                            tint = BgPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "CAPTURE & ANALYZE",
                            style = Typography.labelMedium.copy(fontWeight = FontWeight.Black, color = BgPrimary, letterSpacing = 0.5.sp)
                        )
                    }
                }
            }
        }
    }
}
