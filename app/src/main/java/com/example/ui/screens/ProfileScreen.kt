package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Subscription
import com.example.data.model.User
import com.example.ui.components.*
import com.example.ui.navigation.Screen
import com.example.ui.theme.*

@Composable
fun ProfileScreen(
    user: User?,
    subscription: Subscription?,
    onOpenPaywall: () -> Unit,
    onLogout: () -> Unit,
    onNavigate: (Screen) -> Unit,
    onExportData: () -> Unit
) {
    var notificationsEnabled by remember { mutableStateOf(true) }
    var metricUnits by remember { mutableStateOf(true) }

    val tier = subscription?.plan ?: user?.subscriptionTier ?: "FREE"

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
            // User Header Card
            item {
                LiquidHeroCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(68.dp)
                                .clip(CircleShape)
                                .background(Color(0x337C5CFF))
                                .border(2.dp, ElectricLime, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = (user?.fullName ?: "P").take(1).uppercase(),
                                style = Typography.displayMedium.copy(fontWeight = FontWeight.Black, color = ElectricLime)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = user?.fullName ?: "Priyan",
                                    style = Typography.headlineSmall.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(ElectricLime)
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = tier,
                                        style = Typography.labelSmall.copy(fontWeight = FontWeight.Black, color = BgPrimary)
                                    )
                                }
                            }

                            Text(
                                text = user?.email ?: "priyan1436ei@gmail.com",
                                style = Typography.bodySmall.copy(color = TextSecondary),
                                modifier = Modifier.padding(top = 2.dp)
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "🎯 ${user?.fitnessGoal ?: "Build Muscle"} • ${user?.weightKg ?: 72.4f} KG",
                                style = Typography.labelMedium.copy(color = ElectricCyan, fontWeight = FontWeight.SemiBold)
                            )
                        }
                    }
                }
            }

            // Stats Ticker
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ProfileMiniStat(label = "Streak", value = "${user?.currentStreak ?: 6} Days", modifier = Modifier.weight(1f))
                    ProfileMiniStat(label = "Completed", value = "${user?.workoutsCompletedCount ?: 28}", modifier = Modifier.weight(1f))
                    ProfileMiniStat(label = "Target Wt", value = "${user?.targetWeightKg ?: 68.0f} KG", modifier = Modifier.weight(1f))
                }
            }

            // Subscription Upgrade Card
            item {
                LiquidGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = if (tier == "FREE") ElectricLime else NeonViolet,
                    ambientGlowBrush = if (tier == "FREE") LimeVioletGradient else VioletCyanGradient,
                    onClick = onOpenPaywall
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "MEMBERSHIP TIER",
                                style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = ElectricLime)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (tier == "PREMIUM") "FITTRACK PREMIUM ACTIVE" else "FITTRACK $tier PLAN",
                                style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (tier == "PREMIUM") "All AI models, high thinking, and telemetry unlocked." else "Unlock high thinking AI Coach, custom generator & analytics.",
                                style = Typography.bodySmall.copy(color = TextSecondary)
                            )
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = ElectricLime,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Settings & Preferences Section
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "PREFERENCES & TELEMETRY",
                        style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = TextMuted, letterSpacing = 0.5.sp),
                        modifier = Modifier.padding(bottom = 10.dp)
                    )

                    LiquidGlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            SettingToggleRow(
                                title = "Training & Hydration Reminders",
                                subtitle = "Daily smart notifications for workouts and water",
                                isChecked = notificationsEnabled,
                                onCheckedChange = { notificationsEnabled = it }
                            )

                            HorizontalDivider(color = GlassBorder, thickness = 0.8.dp, modifier = Modifier.padding(vertical = 10.dp))

                            SettingToggleRow(
                                title = "Metric Measurement Units",
                                subtitle = if (metricUnits) "Kilograms (KG) & Centimeters (cm)" else "Pounds (LB) & Feet (ft)",
                                isChecked = metricUnits,
                                onCheckedChange = { metricUnits = it }
                            )

                            HorizontalDivider(color = GlassBorder, thickness = 0.8.dp, modifier = Modifier.padding(vertical = 10.dp))

                            SettingActionRow(
                                icon = Icons.Default.CloudDownload,
                                title = "Export Biometric Data (JSON)",
                                subtitle = "Download local Room database backup",
                                onClick = onExportData
                            )
                        }
                    }
                }
            }

            // Account & Logout
            item {
                LiquidGlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        SettingActionRow(
                            icon = Icons.Default.PrivacyTip,
                            title = "Privacy & Data Retention",
                            subtitle = "Local-first storage with zero telemetry leakage",
                            onClick = {}
                        )

                        HorizontalDivider(color = GlassBorder, thickness = 0.8.dp, modifier = Modifier.padding(vertical = 10.dp))

                        SettingActionRow(
                            icon = Icons.AutoMirrored.Filled.Logout,
                            title = "Log Out of Session",
                            subtitle = "Safely end current session",
                            titleColor = ErrorRed,
                            onClick = onLogout
                        )
                    }
                }
            }
        }
    }
    }
}

@Composable
fun ProfileMiniStat(label: String, value: String, modifier: Modifier = Modifier) {
    LiquidGlassCard(modifier = modifier, cornerRadius = 16.dp) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text(text = label.uppercase(), style = Typography.labelSmall.copy(fontSize = 10.sp, color = TextMuted))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary))
        }
    }
}

@Composable
fun SettingToggleRow(
    title: String,
    subtitle: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = Typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = TextPrimary))
            Text(text = subtitle, style = Typography.bodySmall.copy(color = TextSecondary))
        }
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = BgPrimary,
                checkedTrackColor = ElectricLime,
                uncheckedThumbColor = TextMuted,
                uncheckedTrackColor = GlassSurfaceLevel3
            )
        )
    }
}

@Composable
fun SettingActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    titleColor: Color = TextPrimary,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Icon(imageVector = icon, contentDescription = null, tint = titleColor, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = title, style = Typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = titleColor))
                Text(text = subtitle, style = Typography.bodySmall.copy(color = TextSecondary))
            }
        }
        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = TextMuted, modifier = Modifier.size(18.dp))
    }
}
