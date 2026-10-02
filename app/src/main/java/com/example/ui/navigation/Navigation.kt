package com.example.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Auth : Screen("auth")
    object Onboarding : Screen("onboarding")
    object Home : Screen("home")
    object Workout : Screen("workout")
    object Nutrition : Screen("nutrition")
    object Progress : Screen("progress")
    object Profile : Screen("profile")
    object ActiveWorkout : Screen("active_workout")
    object WorkoutComplete : Screen("workout_complete")
    object AiGenerator : Screen("ai_generator")
    object AiCoach : Screen("ai_coach")
    object GoalsCalendar : Screen("goals_calendar")
    object AiFoodScanner : Screen("ai_food_scanner")
    object CameraCapture : Screen("camera_capture")
    object Alarms : Screen("alarms")
}

data class BottomNavItem(
    val screen: Screen,
    val title: String,
    val icon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Home, "Home", Icons.Default.Home),
    BottomNavItem(Screen.Workout, "Workout", Icons.Default.FitnessCenter),
    BottomNavItem(Screen.Nutrition, "Nutrition", Icons.Default.Restaurant),
    BottomNavItem(Screen.Progress, "Progress", Icons.AutoMirrored.Filled.TrendingUp),
    BottomNavItem(Screen.Profile, "Profile", Icons.Default.Person)
)

/**
 * Floating Liquid Glass Bottom Navigation Bar
 * Features:
 * - Electric Lime active indicator
 * - Slight icon lift with spring animation
 * - Subtle ambient glass glow and 28dp pill shape
 * - Respects WindowInsets.navigationBars
 */
@Composable
fun FloatingLiquidNavBar(
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
                .clip(RoundedCornerShape(35.dp))
                .background(Color(0xE613131A))
                .border(1.2.dp, GlassBorderBrush, RoundedCornerShape(35.dp))
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            // Top specular shine
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(26.dp)
                    .align(Alignment.TopCenter)
                    .clip(RoundedCornerShape(topStart = 35.dp, topEnd = 35.dp))
                    .background(GlassSurfaceSheenBrush)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                bottomNavItems.forEach { item ->
                    val isSelected = currentScreen.route == item.screen.route
                    val scale by animateFloatAsState(
                        targetValue = if (isSelected) 1.06f else 1f,
                        animationSpec = spring(dampingRatio = 0.75f, stiffness = 500f),
                        label = "nav_icon_scale"
                    )

                    Column(
                        modifier = Modifier
                            .scale(scale)
                            .clip(RoundedCornerShape(22.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = false, radius = 28.dp, color = ElectricLime.copy(alpha = 0.25f)),
                                onClick = { onNavigate(item.screen) }
                            )
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(if (isSelected) 38.dp else 28.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    if (isSelected) ElectricLime.copy(alpha = 0.18f) else Color.Transparent
                                )
                                .border(
                                    width = if (isSelected) 1.dp else 0.dp,
                                    color = if (isSelected) ElectricLime.copy(alpha = 0.4f) else Color.Transparent,
                                    shape = RoundedCornerShape(16.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title,
                                tint = if (isSelected) ElectricLime else TextMuted,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = item.title,
                            style = Typography.labelSmall.copy(
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                color = if (isSelected) ElectricLime else TextMuted
                            )
                        )
                    }
                }
            }
        }
    }
}
