package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val FitTrackDarkColorScheme = darkColorScheme(
    primary = ElectricLime,
    onPrimary = BgPrimary,
    primaryContainer = Color(0x33C6FF3D),
    onPrimaryContainer = ElectricLime,
    secondary = NeonViolet,
    onSecondary = TextPrimary,
    secondaryContainer = Color(0x337C5CFF),
    onSecondaryContainer = Color(0xFFD6C8FF),
    tertiary = ElectricCyan,
    onTertiary = BgPrimary,
    tertiaryContainer = Color(0x3319E3FF),
    onTertiaryContainer = ElectricCyan,
    background = BgPrimary,
    onBackground = TextPrimary,
    surface = BgCard,
    onSurface = TextPrimary,
    surfaceVariant = BgElevated,
    onSurfaceVariant = TextSecondary,
    outline = GlassBorder,
    outlineVariant = GlassBorderHighlight,
    error = ErrorRed,
    onError = BgPrimary
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = FitTrackDarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = BgPrimary.toArgb()
                window.navigationBarColor = BgPrimary.toArgb()
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = false
                controller.isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
