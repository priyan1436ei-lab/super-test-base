package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun SubscriptionPaywallDialog(
    lockedFeature: String,
    onDismiss: () -> Unit,
    onUpgrade: (String) -> Unit
) {
    var selectedTier by remember { mutableStateOf("PRO") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        LiquidGlassCard(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clickable(enabled = false) {},
            cornerRadius = 28.dp,
            borderColor = ElectricLime.copy(alpha = 0.6f),
            ambientGlowBrush = HeroCardGradient
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0x33C6FF3D))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = ElectricLime, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "MEMBERSHIP PASS", style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = ElectricLime))
                        }
                    }

                    LiquidIconButton(onClick = onDismiss, contentDescription = "Close") {
                        Icon(Icons.Default.Close, contentDescription = null, tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Unlock $lockedFeature",
                    style = Typography.headlineMedium.copy(fontWeight = FontWeight.Black, color = TextPrimary)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Elevate your training with personalized AI coaching, Gemini 3.1 Pro high thinking, and automated adaptive periodization.",
                    style = Typography.bodySmall.copy(color = TextSecondary),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Tier Selection Cards: PRO vs PREMIUM
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    PaywallTierCard(
                        tierName = "PRO",
                        price = "$9.99 / mo",
                        features = listOf("AI Fitness Coach", "AI Split Generator", "Advanced Telemetry"),
                        isSelected = selectedTier == "PRO",
                        onClick = { selectedTier = "PRO" },
                        accentColor = ElectricLime,
                        modifier = Modifier.weight(1f)
                    )

                    PaywallTierCard(
                        tierName = "PREMIUM",
                        price = "$14.99 / mo",
                        features = listOf("Everything in PRO", "Unlimited High Thinking", "Biometric Synergy"),
                        isSelected = selectedTier == "PREMIUM",
                        onClick = { selectedTier = "PREMIUM" },
                        accentColor = ElectricCyan,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                LiquidPrimaryButton(
                    text = "UPGRADE TO $selectedTier",
                    onClick = { onUpgrade(selectedTier) },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = BgPrimary)
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "NOT NOW",
                    style = Typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        letterSpacing = 1.sp
                    ),
                    modifier = Modifier
                        .clickable(onClick = onDismiss)
                        .padding(8.dp)
                )
            }
        }
    }
}

@Composable
fun PaywallTierCard(
    tierName: String,
    price: String,
    features: List<String>,
    isSelected: Boolean,
    onClick: () -> Unit,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(if (isSelected) Color(0x2EC6FF3D) else GlassSurfaceLevel2)
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) accentColor else GlassBorder,
                shape = RoundedCornerShape(18.dp)
            )
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Column {
            Text(
                text = tierName,
                style = Typography.titleMedium.copy(
                    fontWeight = FontWeight.Black,
                    color = if (isSelected) accentColor else TextPrimary
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = price,
                style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = TextSecondary)
            )

            Spacer(modifier = Modifier.height(10.dp))

            features.forEach { feat ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = feat,
                        style = Typography.bodySmall.copy(fontSize = 10.sp, color = TextPrimary)
                    )
                }
            }
        }
    }
}
