package com.example.ui.screens

import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.FitnessAlarm
import com.example.ui.components.*
import com.example.ui.theme.*

/**
 * Real-Time Alarms & Fitness Reminders Screen
 * Manages exact scheduled alarms and real-time triggers for Workouts, Hydration,
 * Nutrition/Meals, Sleep, and Supplements with bilingual English/Tamil descriptions.
 */
@Composable
fun AlarmsScreen(
    alarms: List<FitnessAlarm>,
    nextAlarm: FitnessAlarm?,
    nextAlarmCountdown: String,
    onToggleAlarm: (FitnessAlarm) -> Unit,
    onSaveAlarm: (
        title: String,
        labelTamil: String,
        category: String,
        hour: Int,
        minute: Int,
        daysOfWeek: String,
        soundTone: String,
        vibrate: Boolean,
        targetValue: String,
        existingId: String?
    ) -> Unit,
    onDeleteAlarm: (FitnessAlarm) -> Unit,
    onTriggerTestAlarm: (FitnessAlarm) -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    var selectedCategoryFilter by remember { mutableStateOf("ALL") }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingAlarm by remember { mutableStateOf<FitnessAlarm?>(null) }

    fun vibrateShort() {
        try {
            val vibrator = ContextCompat.getSystemService(context, Vibrator::class.java)
            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(35, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(35)
                }
            }
        } catch (_: Exception) {}
    }

    val categories = listOf("ALL", "WORKOUT", "HYDRATION", "MEAL", "SUPPLEMENT", "SLEEP")

    val filteredAlarms = remember(alarms, selectedCategoryFilter) {
        if (selectedCategoryFilter == "ALL") {
            alarms
        } else {
            alarms.filter { it.category.equals(selectedCategoryFilter, ignoreCase = true) }
        }
    }

    AmbientLiquidMeshBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                // Liquid Glass Top Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.statusBars)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            LiquidIconButton(
                                onClick = onBack,
                                contentDescription = "Back",
                                modifier = Modifier.testTag("alarms_back_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = null,
                                    tint = TextPrimary
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = "REAL-TIME ALARMS",
                                    style = Typography.titleMedium.copy(
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 0.5.sp
                                    )
                                )
                                Text(
                                    text = "உடற்பயிற்சி & நீர் அருந்தும் நினைவூட்டல்",
                                    style = Typography.bodySmall.copy(
                                        color = ElectricLime,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }

                        // Add New Alarm Button
                        Button(
                            onClick = {
                                vibrateShort()
                                editingAlarm = null
                                showAddDialog = true
                            },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ElectricLime,
                                contentColor = Color.Black
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                            modifier = Modifier
                                .height(38.dp)
                                .testTag("add_alarm_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddAlarm,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "ADD",
                                style = Typography.labelMedium.copy(fontWeight = FontWeight.Black)
                            )
                        }
                    }
                }
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. HERO CARD: NEXT TRIGGERING ALARM & LIVE TICKER
                item {
                    NextAlarmHeroCard(
                        nextAlarm = nextAlarm,
                        countdown = nextAlarmCountdown,
                        onTestRing = { alarm ->
                            vibrateShort()
                            onTriggerTestAlarm(alarm)
                        }
                    )
                }

                // 2. QUICK PRESETS TRAY
                item {
                    Text(
                        text = "QUICK ALARM PRESETS",
                        style = Typography.labelSmall.copy(
                            color = TextSecondary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            QuickPresetChip(
                                title = "💧 +2-Hr Hydrate",
                                sub = "Every 2 hrs",
                                color = ElectricCyan,
                                onClick = {
                                    vibrateShort()
                                    onSaveAlarm(
                                        "Hydration Break",
                                        "தண்ணீர் அருந்தும் நேரம்",
                                        "HYDRATION",
                                        11,
                                        0,
                                        "1,2,3,4,5,6,7",
                                        "Cyber Chime",
                                        true,
                                        "500 ml Water",
                                        null
                                    )
                                }
                            )
                        }
                        item {
                            QuickPresetChip(
                                title = "⚡ +7 AM Cardio",
                                sub = "HIIT Morning",
                                color = ElectricLime,
                                onClick = {
                                    vibrateShort()
                                    onSaveAlarm(
                                        "HIIT Morning Cardio",
                                        "காலை அதிதீவிர உடற்பயிற்சி",
                                        "WORKOUT",
                                        7,
                                        0,
                                        "1,2,3,4,5,6",
                                        "High Energy",
                                        true,
                                        "30 min HIIT Run",
                                        null
                                    )
                                }
                            )
                        }
                        item {
                            QuickPresetChip(
                                title = "🥗 +1 PM Lunch Log",
                                sub = "Macro Check",
                                color = WarningAmber,
                                onClick = {
                                    vibrateShort()
                                    onSaveAlarm(
                                        "Lunch Nutrition & Macros",
                                        "மதிய உணவு & சத்துக்கள் பதிவு",
                                        "MEAL",
                                        13,
                                        0,
                                        "1,2,3,4,5,6,7",
                                        "Zen Bell",
                                        true,
                                        "High Protein Meal",
                                        null
                                    )
                                }
                            )
                        }
                        item {
                            QuickPresetChip(
                                title = "🥤 +Protein Shake",
                                sub = "Post Workout",
                                color = NeonViolet,
                                onClick = {
                                    vibrateShort()
                                    onSaveAlarm(
                                        "Post-Workout Whey Protein",
                                        "உடற்பயிற்சி புரத பானம்",
                                        "SUPPLEMENT",
                                        18,
                                        30,
                                        "1,2,3,4,5",
                                        "Cyber Chime",
                                        true,
                                        "30g Whey + 5g Creatine",
                                        null
                                    )
                                }
                            )
                        }
                        item {
                            QuickPresetChip(
                                title = "🌙 +10:30 PM Sleep",
                                sub = "8h Recovery",
                                color = Color(0xFF818CF8),
                                onClick = {
                                    vibrateShort()
                                    onSaveAlarm(
                                        "Sleep & Recovery Call",
                                        "இரவு உறக்கம் & தசை மீட்பு",
                                        "SLEEP",
                                        22,
                                        30,
                                        "1,2,3,4,5,6,7",
                                        "Gentle Rise",
                                        true,
                                        "8 Hours Rest Target",
                                        null
                                    )
                                }
                            )
                        }
                    }
                }

                // 3. CATEGORY FILTER CHIPS
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SCHEDULED ALARMS (${filteredAlarms.size})",
                            style = Typography.labelSmall.copy(
                                color = TextSecondary,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(ElectricLime)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "REAL-TIME SYNC",
                                style = Typography.labelSmall.copy(
                                    color = ElectricLime,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(categories) { cat ->
                            val isSelected = selectedCategoryFilter == cat
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(
                                        if (isSelected) ElectricLime.copy(alpha = 0.2f) else Color(0x1F191922)
                                    )
                                    .border(
                                        1.dp,
                                        if (isSelected) ElectricLime else Color(0x2EFFFFFF),
                                        RoundedCornerShape(20.dp)
                                    )
                                    .clickable {
                                        vibrateShort()
                                        selectedCategoryFilter = cat
                                    }
                                    .padding(horizontal = 14.dp, vertical = 7.dp)
                            ) {
                                Text(
                                    text = cat,
                                    style = Typography.labelSmall.copy(
                                        color = if (isSelected) ElectricLime else TextSecondary,
                                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Normal
                                    )
                                )
                            }
                        }
                    }
                }

                // 4. ALARMS LIST
                if (filteredAlarms.isEmpty()) {
                    item {
                        LiquidGlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AlarmOff,
                                    contentDescription = null,
                                    tint = TextMuted,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "No Alarms in this category",
                                    style = Typography.titleSmall.copy(color = TextPrimary)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Tap '+ ADD' to set up a new real-time fitness reminder",
                                    style = Typography.bodySmall.copy(color = TextSecondary)
                                )
                            }
                        }
                    }
                } else {
                    items(filteredAlarms, key = { it.alarmId }) { alarm ->
                        AlarmItemCard(
                            alarm = alarm,
                            onToggle = {
                                vibrateShort()
                                onToggleAlarm(alarm)
                            },
                            onEdit = {
                                vibrateShort()
                                editingAlarm = alarm
                                showAddDialog = true
                            },
                            onDelete = {
                                vibrateShort()
                                onDeleteAlarm(alarm)
                            },
                            onTestRing = {
                                vibrateShort()
                                onTriggerTestAlarm(alarm)
                            }
                        )
                    }
                }
            }
        }
    }

    // Add or Edit Alarm Bottom Sheet / Dialog
    if (showAddDialog) {
        AlarmEditDialog(
            alarmToEdit = editingAlarm,
            onDismiss = { showAddDialog = false },
            onSave = { title, labelTamil, category, hour, minute, days, tone, vibrate, target ->
                onSaveAlarm(
                    title,
                    labelTamil,
                    category,
                    hour,
                    minute,
                    days,
                    tone,
                    vibrate,
                    target,
                    editingAlarm?.alarmId
                )
                showAddDialog = false
            }
        )
    }
}

/**
 * Hero Card displaying the upcoming real-time alarm and live countdown
 */
@Composable
private fun NextAlarmHeroCard(
    nextAlarm: FitnessAlarm?,
    countdown: String,
    onTestRing: (FitnessAlarm) -> Unit
) {
    val categoryColor = when (nextAlarm?.category?.uppercase()) {
        "WORKOUT" -> ElectricLime
        "HYDRATION" -> ElectricCyan
        "MEAL" -> WarningAmber
        "SUPPLEMENT" -> NeonViolet
        "SLEEP" -> Color(0xFF818CF8)
        else -> ElectricLime
    }

    LiquidHeroCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(categoryColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "NEXT REAL-TIME ALARM",
                        style = Typography.labelSmall.copy(
                            color = categoryColor,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    )
                }

                // Live Ticker Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x22FFFFFF))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = countdown,
                        style = Typography.labelSmall.copy(
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (nextAlarm != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = nextAlarm.getFormattedTime(),
                            style = Typography.displaySmall.copy(
                                color = TextPrimary,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = nextAlarm.title,
                            style = Typography.titleMedium.copy(
                                color = categoryColor,
                                fontWeight = FontWeight.Bold
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (nextAlarm.labelTamil.isNotBlank()) {
                            Text(
                                text = nextAlarm.labelTamil,
                                style = Typography.bodySmall.copy(
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Test Ring Button
                    Button(
                        onClick = { onTestRing(nextAlarm) },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0x33252535),
                            contentColor = TextPrimary
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, categoryColor.copy(alpha = 0.5f)),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        modifier = Modifier.testTag("test_ring_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = categoryColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "TEST RING",
                            style = Typography.labelSmall.copy(
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            } else {
                Text(
                    text = "No upcoming alarms scheduled",
                    style = Typography.bodyMedium.copy(color = TextSecondary)
                )
            }
        }
    }
}

/**
 * Individual Alarm Card with toggle switch, day chips, and action buttons
 */
@Composable
private fun AlarmItemCard(
    alarm: FitnessAlarm,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onTestRing: () -> Unit
) {
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
        else -> Icons.Default.Alarm
    }

    LiquidGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() }
            .testTag("alarm_card_${alarm.alarmId}")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Category Icon Circle
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(categoryColor.copy(alpha = if (alarm.isEnabled) 0.2f else 0.08f))
                            .border(1.dp, categoryColor.copy(alpha = if (alarm.isEnabled) 0.5f else 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = categoryIcon,
                            contentDescription = null,
                            tint = if (alarm.isEnabled) categoryColor else TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = alarm.getFormattedTime(),
                            style = Typography.titleLarge.copy(
                                color = if (alarm.isEnabled) TextPrimary else TextMuted,
                                fontWeight = FontWeight.Black,
                                fontSize = 22.sp
                            )
                        )
                        Text(
                            text = alarm.title,
                            style = Typography.bodyMedium.copy(
                                color = if (alarm.isEnabled) categoryColor else TextMuted,
                                fontWeight = FontWeight.Bold
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (alarm.labelTamil.isNotBlank()) {
                            Text(
                                text = alarm.labelTamil,
                                style = Typography.bodySmall.copy(
                                    color = TextSecondary.copy(alpha = if (alarm.isEnabled) 1f else 0.6f),
                                    fontSize = 11.sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Switch Toggle
                Switch(
                    checked = alarm.isEnabled,
                    onCheckedChange = { onToggle() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.Black,
                        checkedTrackColor = categoryColor,
                        uncheckedThumbColor = TextMuted,
                        uncheckedTrackColor = Color(0x33252535)
                    ),
                    modifier = Modifier.testTag("alarm_switch_${alarm.alarmId}")
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Active Day of Week Chips (Mon to Sun)
            val dayLetters = listOf("M", "T", "W", "T", "F", "S", "S")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    dayLetters.forEachIndexed { index, letter ->
                        val dayIndex = index + 1 // 1=Mon .. 7=Sun
                        val isActive = alarm.isDayActive(dayIndex)
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isActive && alarm.isEnabled) categoryColor.copy(alpha = 0.25f)
                                    else Color(0x1F191922)
                                )
                                .border(
                                    1.dp,
                                    if (isActive && alarm.isEnabled) categoryColor else Color(0x22FFFFFF),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = letter,
                                style = Typography.labelSmall.copy(
                                    color = if (isActive && alarm.isEnabled) categoryColor else TextMuted,
                                    fontSize = 10.sp,
                                    fontWeight = if (isActive) FontWeight.Black else FontWeight.Normal
                                )
                            )
                        }
                    }
                }

                // Action Icons: Quick Test & Delete
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Test Ring
                    IconButton(
                        onClick = onTestRing,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Test Alarm",
                            tint = categoryColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Delete
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete Alarm",
                            tint = TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Quick Preset Chip in horizontal scroll
 */
@Composable
private fun QuickPresetChip(
    title: String,
    sub: String,
    color: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0x1F191922))
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Column {
            Text(
                text = title,
                style = Typography.labelSmall.copy(
                    color = color,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = sub,
                style = Typography.bodySmall.copy(
                    color = TextSecondary,
                    fontSize = 10.sp
                )
            )
        }
    }
}

/**
 * Dialog for adding or editing an alarm
 */
@Composable
private fun AlarmEditDialog(
    alarmToEdit: FitnessAlarm?,
    onDismiss: () -> Unit,
    onSave: (
        title: String,
        labelTamil: String,
        category: String,
        hour: Int,
        minute: Int,
        days: String,
        soundTone: String,
        vibrate: Boolean,
        targetValue: String
    ) -> Unit
) {
    var title by remember { mutableStateOf(alarmToEdit?.title ?: "Morning Cardio") }
    var labelTamil by remember { mutableStateOf(alarmToEdit?.labelTamil ?: "காலை உடற்பயிற்சி") }
    var category by remember { mutableStateOf(alarmToEdit?.category ?: "WORKOUT") }
    var hour by remember { mutableIntStateOf(alarmToEdit?.timeHour ?: 6) }
    var minute by remember { mutableIntStateOf(alarmToEdit?.timeMinute ?: 30) }
    var isPm by remember { mutableStateOf(hour >= 12) }
    var displayHour by remember { mutableIntStateOf(if (hour == 0) 12 else if (hour > 12) hour - 12 else hour) }
    var targetValue by remember { mutableStateOf(alarmToEdit?.targetValue ?: "45 mins") }
    var soundTone by remember { mutableStateOf(alarmToEdit?.soundTone ?: "Cyber Chime") }
    var vibrate by remember { mutableStateOf(alarmToEdit?.vibrate ?: true) }

    val initialDays = remember(alarmToEdit) {
        val days = alarmToEdit?.daysOfWeek ?: "1,2,3,4,5,6,7"
        days.split(",").mapNotNull { it.trim().toIntOrNull() }.toSet().ifEmpty { setOf(1, 2, 3, 4, 5, 6, 7) }
    }
    var activeDays by remember { mutableStateOf(initialDays) }

    val categories = listOf("WORKOUT", "HYDRATION", "MEAL", "SUPPLEMENT", "SLEEP")
    val sounds = listOf("Cyber Chime", "High Energy", "Zen Bell", "Pulse Alert", "Gentle Rise")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (alarmToEdit == null) "NEW REAL-TIME ALARM" else "EDIT ALARM",
                style = Typography.titleMedium.copy(
                    color = TextPrimary,
                    fontWeight = FontWeight.Black
                )
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Time Selector Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Hour Stepper
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                displayHour = if (displayHour == 1) 12 else displayHour - 1
                            }
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Hour down", tint = ElectricLime)
                        }
                        Text(
                            text = String.format("%02d", displayHour),
                            style = Typography.displaySmall.copy(fontWeight = FontWeight.Black, color = TextPrimary)
                        )
                        IconButton(
                            onClick = {
                                displayHour = if (displayHour == 12) 1 else displayHour + 1
                            }
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Hour up", tint = ElectricLime)
                        }
                    }

                    Text(":", style = Typography.displaySmall.copy(fontWeight = FontWeight.Bold, color = TextPrimary))

                    // Minute Stepper
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                minute = if (minute == 0) 55 else (minute - 5).coerceAtLeast(0)
                            }
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Minute down", tint = ElectricLime)
                        }
                        Text(
                            text = String.format("%02d", minute),
                            style = Typography.displaySmall.copy(fontWeight = FontWeight.Black, color = TextPrimary)
                        )
                        IconButton(
                            onClick = {
                                minute = if (minute >= 55) 0 else minute + 5
                            }
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Minute up", tint = ElectricLime)
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // AM / PM Toggle
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0x33252535))
                            .border(1.dp, ElectricLime.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .clickable { isPm = !isPm }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = if (isPm) "PM" else "AM",
                            style = Typography.titleSmall.copy(
                                color = ElectricLime,
                                fontWeight = FontWeight.Black
                            )
                        )
                    }
                }

                // Category Chips
                Text(
                    text = "CATEGORY",
                    style = Typography.labelSmall.copy(color = TextSecondary, fontWeight = FontWeight.Bold)
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categories) { cat ->
                        val isSelected = category == cat
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) ElectricLime.copy(alpha = 0.25f) else Color(0x1F191922))
                                .border(1.dp, if (isSelected) ElectricLime else Color(0x33FFFFFF), RoundedCornerShape(12.dp))
                                .clickable { category = cat }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = cat,
                                style = Typography.labelSmall.copy(
                                    color = if (isSelected) ElectricLime else TextSecondary,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        }
                    }
                }

                // Title Input
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Alarm Title (English)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = ElectricLime,
                        unfocusedBorderColor = Color(0x44FFFFFF)
                    )
                )

                // Tamil Translation Input
                OutlinedTextField(
                    value = labelTamil,
                    onValueChange = { labelTamil = it },
                    label = { Text("Tamil Subtitle (தமிழ் விளக்கம்)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = ElectricLime,
                        unfocusedBorderColor = Color(0x44FFFFFF)
                    )
                )

                // Target Goal Input
                OutlinedTextField(
                    value = targetValue,
                    onValueChange = { targetValue = it },
                    label = { Text("Target (e.g. 500ml / 45m workout)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = ElectricLime,
                        unfocusedBorderColor = Color(0x44FFFFFF)
                    )
                )

                // Repeat Days Chips
                Text(
                    text = "REPEAT DAYS",
                    style = Typography.labelSmall.copy(color = TextSecondary, fontWeight = FontWeight.Bold)
                )
                val dayNames = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    dayNames.forEachIndexed { index, name ->
                        val dayNum = index + 1
                        val active = activeDays.contains(dayNum)
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(if (active) ElectricLime else Color(0x1F191922))
                                .border(1.dp, if (active) ElectricLime else Color(0x33FFFFFF), CircleShape)
                                .clickable {
                                    if (active) {
                                        if (activeDays.size > 1) {
                                            activeDays = activeDays - dayNum
                                        }
                                    } else {
                                        activeDays = activeDays + dayNum
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = name.take(1),
                                style = Typography.labelSmall.copy(
                                    color = if (active) Color.Black else TextSecondary,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalHour = if (isPm) {
                        if (displayHour == 12) 12 else displayHour + 12
                    } else {
                        if (displayHour == 12) 0 else displayHour
                    }
                    val daysString = activeDays.sorted().joinToString(",")
                    onSave(title, labelTamil, category, finalHour, minute, daysString, soundTone, vibrate, targetValue)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricLime,
                    contentColor = Color.Black
                )
            ) {
                Text("SAVE & SCHEDULE", fontWeight = FontWeight.Black)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        },
        containerColor = Color(0xFF13131D)
    )
}
