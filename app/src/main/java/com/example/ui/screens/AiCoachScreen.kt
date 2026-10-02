package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AIMessage
import com.example.ui.components.*
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun AiCoachScreen(
    messages: List<AIMessage>,
    isThinking: Boolean,
    onSendMessage: (String) -> Unit,
    onClearHistory: () -> Unit,
    onBack: () -> Unit
) {
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    val suggestedPrompts = listOf(
        "Build today's workout",
        "How much protein should I eat?",
        "Replace this exercise",
        "What should I train today?",
        "Give me a 30-minute workout",
        "Explain progressive overload"
    )

    LaunchedEffect(messages.size, isThinking) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    AmbientLiquidMeshBackground {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header: Status "Ready" with Glowing Green/Lime Pulse & Clear Chat Option
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LiquidIconButton(onClick = onBack, contentDescription = "Back") {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = TextPrimary)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "FITTRACK AI COACH",
                            style = Typography.titleMedium.copy(fontWeight = FontWeight.Black, color = TextPrimary)
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(if (isThinking) WarningAmber else ElectricLime)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isThinking) "Thinking (Gemini 3.1 Pro)..." else "Ready • High Intelligence Active",
                                style = Typography.labelSmall.copy(
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isThinking) WarningAmber else ElectricLime
                                )
                            )
                        }
                    }
                }

                LiquidIconButton(
                    onClick = onClearHistory,
                    contentDescription = "Clear Chat"
                ) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = TextMuted)
                }
            }

            // Message List
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (messages.isEmpty()) {
                    item {
                        EmptyCoachState()
                    }
                } else {
                    items(messages) { msg ->
                        CoachMessageBubble(message = msg)
                    }
                }

                if (isThinking) {
                    item {
                        ThinkingBubble()
                    }
                }
            }

            // Quick Prompt Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(suggestedPrompts) { prompt ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(18.dp))
                            .background(GlassSurfaceLevel2)
                            .border(1.dp, GlassBorder, RoundedCornerShape(18.dp))
                            .clickable {
                                onSendMessage(prompt)
                            }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = prompt,
                            style = Typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = ElectricCyan
                            )
                        )
                    }
                }
            }

            // Bottom Input Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        LiquidInput(
                            value = inputText,
                            onValueChange = { inputText = it },
                            placeholder = "Ask your coach anything about workouts, diet..."
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    LiquidIconButton(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                val text = inputText
                                inputText = ""
                                onSendMessage(text)
                            }
                        },
                        backgroundColor = ElectricLime,
                        borderColor = ElectricLime,
                        contentDescription = "Send Message"
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = null,
                            tint = BgPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
    }
}

@Composable
fun CoachMessageBubble(message: AIMessage) {
    val isUser = message.role == "USER"
    var showThinking by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 320.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 20.dp,
                        topEnd = 20.dp,
                        bottomStart = if (isUser) 20.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 20.dp
                    )
                )
                .background(
                    if (isUser) ElectricLime else Color(0x38191922)
                )
                .border(
                    width = 1.dp,
                    color = if (isUser) ElectricLime else GlassBorderHighlight,
                    shape = RoundedCornerShape(
                        topStart = 20.dp,
                        topEnd = 20.dp,
                        bottomStart = if (isUser) 20.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 20.dp
                    )
                )
                .padding(16.dp)
        ) {
            Column {
                Text(
                    text = message.message,
                    style = Typography.bodyMedium.copy(
                        color = if (isUser) BgPrimary else TextPrimary,
                        lineHeight = 22.sp,
                        fontWeight = if (isUser) FontWeight.Medium else FontWeight.Normal
                    )
                )

                if (!isUser && !message.thinkingProcess.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable { showThinking = !showThinking }
                            .padding(vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (showThinking) "Hide Thinking Process" else "View Thinking Process",
                            style = Typography.labelSmall.copy(color = ElectricCyan, fontWeight = FontWeight.Bold)
                        )
                    }

                    AnimatedVisibility(visible = showThinking) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0x2219E3FF))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = message.thinkingProcess ?: "",
                                style = Typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ThinkingBubble() {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0x38191922))
            .border(1.dp, GlassBorder, RoundedCornerShape(18.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Psychology, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Coach is reasoning with high thinking...",
                style = Typography.bodySmall.copy(color = TextSecondary)
            )
        }
    }
}

@Composable
fun EmptyCoachState() {
    LiquidGlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 24.dp
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color(0x3319E3FF)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.SmartToy, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(30.dp))
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Ask FITTRACK AI Coach",
                style = Typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "I have full context of your fitness goal, recent workouts, volume load, and daily nutrition. Tap any suggested prompt or type below.",
                style = Typography.bodyMedium.copy(color = TextSecondary),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}
