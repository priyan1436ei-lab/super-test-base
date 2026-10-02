package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// FITTRACK AI - Dark Liquid Glass + Neon Fitness UI Color Palette
val BgPrimary = Color(0xFF070709)
val BgSecondary = Color(0xFF0D0D12)
val BgCard = Color(0xFF13131A)
val BgElevated = Color(0xFF191922)

// Accent Colors
val ElectricLime = Color(0xFFC6FF3D)
val NeonViolet = Color(0xFF7C5CFF)
val ElectricCyan = Color(0xFF19E3FF)

// Status Colors
val SuccessGreen = Color(0xFF34D399)
val WarningAmber = Color(0xFFFBBF24)
val ErrorRed = Color(0xFFF87171)

// Typography & Text
val TextPrimary = Color(0xFFF7F7F8)
val TextSecondary = Color(0xFFB6B6C1)
val TextMuted = Color(0xFF92929D)

// Liquid Glass Translucent Tints & Borders
val GlassSurfaceLevel1 = Color(0x1F191922)
val GlassSurfaceLevel2 = Color(0x33191922)
val GlassSurfaceLevel3 = Color(0x4D191922)
val GlassSurfaceReflective = Color(0x2820202F)

val GlassBorder = Color(0x2EFFFFFF)
val GlassBorderHighlight = Color(0x52FFFFFF)
val GlassBorderLime = Color(0x66C6FF3D)
val GlassBorderViolet = Color(0x667C5CFF)
val GlassBorderCyan = Color(0x6619E3FF)

// Dual-tone Glass Bevel Gradient Brushes for realistic liquid glass refraction
val GlassBorderBrush = Brush.linearGradient(
    colors = listOf(
        Color(0x66FFFFFF),
        Color(0x28FFFFFF),
        Color(0x10FFFFFF)
    )
)

val GlassBorderLimeBrush = Brush.linearGradient(
    colors = listOf(
        Color(0x99C6FF3D),
        Color(0x44C6FF3D),
        Color(0x15FFFFFF)
    )
)

val GlassBorderVioletBrush = Brush.linearGradient(
    colors = listOf(
        Color(0x997C5CFF),
        Color(0x447C5CFF),
        Color(0x15FFFFFF)
    )
)

val GlassBorderCyanBrush = Brush.linearGradient(
    colors = listOf(
        Color(0x9919E3FF),
        Color(0x4419E3FF),
        Color(0x15FFFFFF)
    )
)

val GlassSurfaceSheenBrush = Brush.verticalGradient(
    colors = listOf(
        Color(0x2EFFFFFF),
        Color(0x0AFFFFFF),
        Color(0x00FFFFFF)
    )
)

// Ambient Gradients
val LimeVioletGradient = Brush.linearGradient(
    colors = listOf(ElectricLime, NeonViolet)
)

val LimeCyanGradient = Brush.linearGradient(
    colors = listOf(ElectricLime, ElectricCyan)
)

val VioletCyanGradient = Brush.linearGradient(
    colors = listOf(NeonViolet, ElectricCyan)
)

val GlassCardGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0x33282836),
        Color(0x1A13131A),
        Color(0x120D0D12)
    )
)

val HeroCardGradient = Brush.linearGradient(
    colors = listOf(
        Color(0x3D7C5CFF),
        Color(0x24C6FF3D),
        Color(0x1A19E3FF)
    )
)

val AmbientBackgroundBrush = Brush.radialGradient(
    colors = listOf(
        Color(0x267C5CFF),
        Color(0x140D0D12),
        Color(0xFF070709)
    )
)
