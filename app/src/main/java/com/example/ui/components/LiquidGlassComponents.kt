package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

/**
 * Liquid Glass Card - Glass Level 1 & 2
 * Translucent liquid surface, inner refractive highlight, subtle border, 22-26dp rounded corners.
 */
@Composable
fun LiquidGlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    borderColor: Color = GlassBorder,
    borderBrush: Brush? = null,
    borderWidth: Dp = 1.dp,
    backgroundColor: Color = GlassSurfaceLevel2,
    ambientGlowBrush: Brush? = null,
    showSpecularSheen: Boolean = true,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed && onClick != null) 0.98f else 1f,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 400f),
        label = "card_scale"
    )

    val effectiveBorderBrush = borderBrush ?: if (borderColor == GlassBorder) GlassBorderBrush else SolidColor(borderColor)

    Box(
        modifier = modifier
            .scale(scale)
            .clip(shape)
            .then(
                if (ambientGlowBrush != null) {
                    Modifier.background(ambientGlowBrush)
                } else {
                    Modifier
                }
            )
            .background(backgroundColor)
            .border(borderWidth, effectiveBorderBrush, shape)
            .then(
                if (onClick != null) {
                    Modifier
                        .pointerInput(Unit) {
                            awaitEachGesture {
                                awaitFirstDown(requireUnconsumed = false)
                                isPressed = true
                                waitForUpOrCancellation()
                                isPressed = false
                            }
                        }
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(color = ElectricLime.copy(alpha = 0.2f)),
                            onClick = onClick
                        )
                } else Modifier
            )
            .padding(18.dp)
    ) {
        if (showSpecularSheen) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp)
                    .align(Alignment.TopCenter)
                    .clip(RoundedCornerShape(topStart = cornerRadius, topEnd = cornerRadius))
                    .background(GlassSurfaceSheenBrush)
            )
        }
        content()
    }
}

/**
 * Liquid Hero Card - For High-Impact Sections (Daily Fitness Score, Active Workout)
 */
@Composable
fun LiquidHeroCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 26.dp,
    content: @Composable BoxScope.() -> Unit
) {
    LiquidGlassCard(
        modifier = modifier,
        cornerRadius = cornerRadius,
        backgroundColor = Color(0x38191922),
        borderColor = GlassBorderHighlight,
        ambientGlowBrush = HeroCardGradient,
        content = content
    )
}

/**
 * Liquid Primary Button - Tactile Electric Lime Physical Button
 */
@Composable
fun LiquidPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 500f),
        label = "button_scale"
    )

    Box(
        modifier = modifier
            .heightIn(min = 52.dp)
            .scale(scale)
            .clip(RoundedCornerShape(26.dp))
            .background(
                if (enabled) ElectricLime else Color(0x33C6FF3D)
            )
            .pointerInput(enabled) {
                if (enabled) {
                    awaitEachGesture {
                        awaitFirstDown(requireUnconsumed = false)
                        isPressed = true
                        waitForUpOrCancellation()
                        isPressed = false
                    }
                }
            }
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = Color.Black.copy(alpha = 0.3f)),
                onClick = onClick
            )
            .padding(horizontal = 24.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            leadingIcon?.invoke()
            if (leadingIcon != null) Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                style = Typography.labelLarge.copy(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (enabled) BgPrimary else TextMuted
                )
            )
            if (trailingIcon != null) Spacer(modifier = Modifier.width(8.dp))
            trailingIcon?.invoke()
        }
    }
}

/**
 * Liquid Secondary Button - Glass Level 3 Translucent Action
 */
@Composable
fun LiquidSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    borderColor: Color = GlassBorder,
    textColor: Color = TextPrimary,
    leadingIcon: (@Composable () -> Unit)? = null
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 500f),
        label = "sec_button_scale"
    )

    Box(
        modifier = modifier
            .heightIn(min = 48.dp)
            .scale(scale)
            .clip(RoundedCornerShape(24.dp))
            .background(GlassSurfaceLevel3)
            .border(1.dp, borderColor, RoundedCornerShape(24.dp))
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    isPressed = true
                    waitForUpOrCancellation()
                    isPressed = false
                }
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = ElectricLime.copy(alpha = 0.15f)),
                onClick = onClick
            )
            .padding(horizontal = 20.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            leadingIcon?.invoke()
            if (leadingIcon != null) Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                style = Typography.labelLarge.copy(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor
                )
            )
        }
    }
}

/**
 * Liquid Icon Button - 48dp tactile circular glass button
 */
@Composable
fun LiquidIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = GlassSurfaceLevel2,
    borderColor: Color = GlassBorder,
    contentDescription: String? = null,
    content: @Composable () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 600f),
        label = "icon_btn_scale"
    )

    Box(
        modifier = modifier
            .size(48.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(backgroundColor)
            .border(1.dp, borderColor, CircleShape)
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    isPressed = true
                    waitForUpOrCancellation()
                    isPressed = false
                }
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = ElectricLime.copy(alpha = 0.25f)),
                onClick = onClick
            )
            .semantics {
                if (contentDescription != null) {
                    this.contentDescription = contentDescription
                }
            },
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

/**
 * Liquid Chip - Selectable filter pill with Neon Electric Accent
 */
@Composable
fun LiquidChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null
) {
    val targetBg = if (isSelected) ElectricLime else GlassSurfaceLevel2
    val targetText = if (isSelected) BgPrimary else TextSecondary
    val targetBorder = if (isSelected) ElectricLime else GlassBorder

    Box(
        modifier = modifier
            .heightIn(min = 38.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(targetBg)
            .border(1.dp, targetBorder, RoundedCornerShape(20.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = ElectricLime.copy(alpha = 0.2f)),
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                icon()
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = text,
                style = Typography.labelMedium.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = targetText
                )
            )
        }
    }
}

/**
 * Liquid Segment Control - Multi-tab switcher with fluid indicator
 */
@Composable
fun LiquidSegmentControl(
    options: List<String>,
    selectedIndex: Int,
    onOptionSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(GlassSurfaceLevel1)
            .border(1.dp, GlassBorder, RoundedCornerShape(18.dp))
            .padding(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            options.forEachIndexed { index, title ->
                val isSelected = index == selectedIndex
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isSelected) GlassSurfaceLevel3 else Color.Transparent)
                        .border(
                            width = if (isSelected) 1.dp else 0.dp,
                            color = if (isSelected) ElectricLime.copy(alpha = 0.5f) else Color.Transparent,
                            shape = RoundedCornerShape(14.dp)
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(color = ElectricLime.copy(alpha = 0.2f)),
                            onClick = { onOptionSelected(index) }
                        )
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        style = Typography.labelMedium.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) ElectricLime else TextMuted
                        )
                    )
                }
            }
        }
    }
}

/**
 * Liquid Input - Translucent dark input with glow border on focus
 */
@Composable
fun LiquidInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    label: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: androidx.compose.ui.text.input.VisualTransformation = androidx.compose.ui.text.input.VisualTransformation.None,
    trailingIcon: (@Composable () -> Unit)? = null
) {
    var isFocused by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        if (label != null) {
            Text(
                text = label,
                style = Typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary
                ),
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(GlassSurfaceLevel2)
                .border(
                    width = 1.dp,
                    color = if (isFocused) ElectricLime else GlassBorder,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 16.dp, vertical = 14.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = Typography.bodyMedium.copy(color = TextMuted)
                        )
                    }
                    BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = Typography.bodyLarge.copy(
                            color = TextPrimary,
                            fontWeight = FontWeight.Medium
                        ),
                        cursorBrush = SolidColor(ElectricLime),
                        keyboardOptions = keyboardOptions,
                        keyboardActions = keyboardActions,
                        visualTransformation = visualTransformation,
                        singleLine = true
                    )
                }
                trailingIcon?.invoke()
            }
        }
    }
}

/**
 * Metric Bento Card - Reusable for Calories, Steps, Water, Weight, Workout
 */
@Composable
fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    progress: Float? = null,
    accentColor: Color = ElectricLime,
    trend: String? = null,
    onClick: (() -> Unit)? = null
) {
    LiquidGlassCard(
        modifier = modifier,
        cornerRadius = 20.dp,
        onClick = onClick
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    icon()
                }

                if (trend != null) {
                    Text(
                        text = trend,
                        style = Typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (trend.startsWith("-")) SuccessGreen else accentColor
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = value,
                style = Typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary
                )
            )

            Text(
                text = title.uppercase(),
                style = Typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    letterSpacing = 0.5.sp
                )
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = subtitle,
                style = Typography.bodySmall.copy(color = TextSecondary)
            )

            if (progress != null) {
                Spacer(modifier = Modifier.height(10.dp))
                ProgressBar(
                    progress = progress.coerceIn(0f, 1f),
                    color = accentColor,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * Animated Circular Progress Ring
 */
@Composable
fun ProgressRing(
    progress: Float, // 0.0 to 1.0
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 10.dp,
    progressColor: Color = ElectricLime,
    trackColor: Color = Color(0x33282836)
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f),
        label = "ring_progress"
    )

    Canvas(modifier = modifier) {
        val stroke = strokeWidth.toPx()
        val diameter = size.minDimension - stroke
        val topLeft = Offset((size.width - diameter) / 2, (size.height - diameter) / 2)
        val arcSize = Size(diameter, diameter)

        // Track
        drawArc(
            color = trackColor,
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = stroke, cap = StrokeCap.Round)
        )

        // Glowing progress arc
        drawArc(
            color = progressColor,
            startAngle = -90f,
            sweepAngle = 360f * animatedProgress,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = stroke, cap = StrokeCap.Round)
        )
    }
}

/**
 * Animated Horizontal Progress Bar
 */
@Composable
fun ProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    height: Dp = 6.dp,
    color: Color = ElectricLime,
    trackColor: Color = Color(0x33282836)
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = spring(dampingRatio = 0.85f, stiffness = 400f),
        label = "bar_progress"
    )

    Box(
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(height / 2))
            .background(trackColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(animatedProgress)
                .clip(RoundedCornerShape(height / 2))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(color.copy(alpha = 0.7f), color)
                    )
                )
        )
    }
}

/**
 * Empty State Card
 */
@Composable
fun EmptyState(
    title: String,
    message: String,
    ctaText: String,
    onCtaClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit
) {
    LiquidGlassCard(
        modifier = modifier.fillMaxWidth(),
        cornerRadius = 24.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(GlassSurfaceLevel3)
                    .border(1.dp, GlassBorder, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                icon()
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = title,
                style = Typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = TextPrimary),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = message,
                style = Typography.bodyMedium.copy(color = TextSecondary),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(18.dp))

            LiquidPrimaryButton(
                text = ctaText,
                onClick = onCtaClick
            )
        }
    }
}

/**
 * Ambient Liquid Mesh Background
 * Dynamic GPU-accelerated radial atmospheric glow blobs that create the signature Liquid Glass refraction.
 */
@Composable
fun AmbientLiquidMeshBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BgPrimary)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Top-left cyber violet glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x387C5CFF), Color(0x127C5CFF), Color.Transparent),
                    center = Offset(w * 0.15f, h * 0.12f),
                    radius = w * 0.70f
                )
            )

            // Center-right electric lime glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x28C6FF3D), Color(0x0CC6FF3D), Color.Transparent),
                    center = Offset(w * 0.88f, h * 0.38f),
                    radius = w * 0.58f
                )
            )

            // Bottom-left electric cyan glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x2E19E3FF), Color(0x0C19E3FF), Color.Transparent),
                    center = Offset(w * 0.22f, h * 0.80f),
                    radius = w * 0.65f
                )
            )
        }
        content()
    }
}

/**
 * Concentric Performance Triple Ring HUD
 * Outer Ring: Workout Activity (Electric Lime)
 * Middle Ring: Caloric Balance (Warning Amber / Sunset Orange)
 * Inner Ring: Hydration Level (Electric Cyan)
 */
@Composable
fun ConcentricPerformanceRing(
    workoutProgress: Float,
    calorieProgress: Float,
    waterProgress: Float,
    score: Int,
    modifier: Modifier = Modifier,
    size: Dp = 104.dp
) {
    val animWorkout by animateFloatAsState(
        targetValue = workoutProgress.coerceIn(0f, 1f),
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f),
        label = "anim_workout"
    )
    val animCalorie by animateFloatAsState(
        targetValue = calorieProgress.coerceIn(0f, 1f),
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f),
        label = "anim_cal"
    )
    val animWater by animateFloatAsState(
        targetValue = waterProgress.coerceIn(0f, 1f),
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f),
        label = "anim_water"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerOffset = Offset(this.size.width / 2, this.size.height / 2)
            val strokeOuter = 7.dp.toPx()
            val strokeMid = 6.dp.toPx()
            val strokeInner = 5.dp.toPx()

            val dOuter = this.size.minDimension - strokeOuter - 2.dp.toPx()
            val dMid = dOuter - strokeOuter * 2 - 4.dp.toPx()
            val dInner = dMid - strokeMid * 2 - 4.dp.toPx()

            // 1. Outer Ring: Workout (Electric Lime)
            val topOuter = Offset((this.size.width - dOuter) / 2, (this.size.height - dOuter) / 2)
            drawArc(
                color = Color(0x26C6FF3D),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topOuter,
                size = Size(dOuter, dOuter),
                style = Stroke(width = strokeOuter, cap = StrokeCap.Round)
            )
            drawArc(
                color = ElectricLime,
                startAngle = -90f,
                sweepAngle = 360f * animWorkout,
                useCenter = false,
                topLeft = topOuter,
                size = Size(dOuter, dOuter),
                style = Stroke(width = strokeOuter, cap = StrokeCap.Round)
            )

            // 2. Middle Ring: Calories (Warning Amber)
            val topMid = Offset((this.size.width - dMid) / 2, (this.size.height - dMid) / 2)
            drawArc(
                color = Color(0x26FBBF24),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topMid,
                size = Size(dMid, dMid),
                style = Stroke(width = strokeMid, cap = StrokeCap.Round)
            )
            drawArc(
                color = WarningAmber,
                startAngle = -90f,
                sweepAngle = 360f * animCalorie,
                useCenter = false,
                topLeft = topMid,
                size = Size(dMid, dMid),
                style = Stroke(width = strokeMid, cap = StrokeCap.Round)
            )

            // 3. Inner Ring: Hydration (Electric Cyan)
            val topInner = Offset((this.size.width - dInner) / 2, (this.size.height - dInner) / 2)
            drawArc(
                color = Color(0x2619E3FF),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topInner,
                size = Size(dInner, dInner),
                style = Stroke(width = strokeInner, cap = StrokeCap.Round)
            )
            drawArc(
                color = ElectricCyan,
                startAngle = -90f,
                sweepAngle = 360f * animWater,
                useCenter = false,
                topLeft = topInner,
                size = Size(dInner, dInner),
                style = Stroke(width = strokeInner, cap = StrokeCap.Round)
            )
        }

        // Center Score Badge
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$score",
                style = Typography.titleLarge.copy(
                    fontWeight = FontWeight.Black,
                    color = TextPrimary,
                    fontSize = 20.sp
                )
            )
            Text(
                text = "INDEX",
                style = Typography.labelSmall.copy(
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = ElectricLime,
                    letterSpacing = 0.5.sp
                )
            )
        }
    }
}

/**
 * Macro Pill Row for Protein, Carbs, Fat Breakdown
 */
@Composable
fun MacroPillRow(
    proteinGrams: Float,
    targetProtein: Float,
    carbsGrams: Float,
    targetCarbs: Float,
    fatGrams: Float,
    targetFat: Float,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        MacroPillItem(
            label = "Protein",
            current = proteinGrams.toInt(),
            target = targetProtein.toInt(),
            color = ElectricCyan,
            modifier = Modifier.weight(1f)
        )
        MacroPillItem(
            label = "Carbs",
            current = carbsGrams.toInt(),
            target = targetCarbs.toInt(),
            color = ElectricLime,
            modifier = Modifier.weight(1f)
        )
        MacroPillItem(
            label = "Fat",
            current = fatGrams.toInt(),
            target = targetFat.toInt(),
            color = NeonViolet,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun MacroPillItem(
    label: String,
    current: Int,
    target: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    val progress = (current.toFloat() / target).coerceIn(0f, 1f)
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(GlassSurfaceLevel3)
            .border(1.dp, GlassBorder, RoundedCornerShape(14.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    style = Typography.labelSmall.copy(color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = Typography.labelSmall.copy(color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${current}g",
                style = Typography.titleSmall.copy(color = TextPrimary, fontWeight = FontWeight.Black)
            )
            Spacer(modifier = Modifier.height(6.dp))
            ProgressBar(
                progress = progress,
                color = color,
                height = 4.dp,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
