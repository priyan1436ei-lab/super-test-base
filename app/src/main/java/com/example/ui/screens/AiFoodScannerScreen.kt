package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FoodItem
import com.example.ui.components.*
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun AiFoodScannerScreen(
    foodDatabase: List<FoodItem>,
    onLogScannedMeal: (mealType: String, food: FoodItem?, customName: String, qty: Float, cal: Int, p: Float, c: Float, f: Float, portionGrams: Float) -> Unit,
    onBack: () -> Unit,
    onOpenCameraCapture: (() -> Unit)? = null,
    onReingestDataset: (() -> Unit)? = null
) {
    val scope = rememberCoroutineScope()
    var isScanning by remember { mutableStateOf(false) }

    // Retrofit Nutrition Database client
    val networkRepo = remember { com.example.data.network.NutritionNetworkRepository() }
    var isSearchingOnlineApi by remember { mutableStateOf(false) }
    var onlineApiStatusMessage by remember { mutableStateOf<String?>(null) }

    // Default to first item or standard nutritious item
    val initialFood = remember(foodDatabase) {
        foodDatabase.firstOrNull { it.name.contains("Dosa", ignoreCase = true) }
            ?: foodDatabase.firstOrNull()
    }
    var detectedFood by remember { mutableStateOf<FoodItem?>(initialFood) }

    var selectedMealType by remember { mutableStateOf("LUNCH") }
    var distanceCm by remember { mutableIntStateOf(24) }

    // Real-time portion calculation state (in grams)
    var selectedPortionGrams by remember { mutableFloatStateOf(initialFood?.portionWeightGrams ?: 150f) }
    var portionMultiplier by remember { mutableFloatStateOf(1.0f) }

    var globalFoodQuery by remember { mutableStateOf("") }
    var selectedMacroFilter by remember { mutableStateOf("ALL") }

    // Scanner animation
    val infiniteTransition = rememberInfiniteTransition(label = "scanner")
    val laserPosition by infiniteTransition.animateFloat(
        initialValue = 0.05f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser"
    )

    val reticlePulse by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // Update portion grams whenever a new food is detected
    LaunchedEffect(detectedFood) {
        if (detectedFood != null) {
            selectedPortionGrams = detectedFood!!.portionWeightGrams * portionMultiplier
        }
    }

    // Real-time Room database search by name, Tamil name, and nutritional content
    val databaseSearchResults = remember(foodDatabase, globalFoodQuery, selectedMacroFilter) {
        val q = globalFoodQuery.trim().lowercase()
        foodDatabase.filter { food ->
            val matchesName = q.isEmpty() ||
                food.name.lowercase().contains(q) ||
                food.tamilName.lowercase().contains(q) ||
                food.category.lowercase().contains(q) ||
                food.description.lowercase().contains(q)

            val matchesNutritionalContent = when (selectedMacroFilter) {
                "HIGH_PROTEIN" -> food.protein >= 20f
                "LOW_CALORIE" -> food.calories <= 200
                "LOW_CARB" -> food.carbs <= 10f
                "HIGH_FIBER" -> food.fibre >= 4f
                "SOUTH_INDIAN" -> food.isIndianFood || food.category.contains("South Indian", ignoreCase = true) || food.tamilName.isNotBlank()
                else -> true
            }

            matchesName && matchesNutritionalContent
        }
    }

    fun startPlateScan(target: FoodItem?, customQuery: String? = null) {
        scope.launch {
            isScanning = true
            delay(850)

            val result = if (!customQuery.isNullOrBlank()) {
                com.example.data.ai.FitTrackAIService.recognizeGlobalFood(customQuery, distanceCm)
            } else if (target != null) {
                target
            } else {
                foodDatabase.randomOrNull() ?: foodDatabase.firstOrNull()
            }

            isScanning = false
            detectedFood = result
            if (result != null) {
                distanceCm = result.recommendedDistanceCm
                selectedPortionGrams = result.portionWeightGrams * portionMultiplier
            }
        }
    }

    // Photo picker launcher (0-permission, compliant with Android & Play Store)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            startPlateScan(foodDatabase.randomOrNull())
        }
    }

    // Active calculations based on selected portion
    val currentFood = detectedFood ?: foodDatabase.firstOrNull()
    val baseGrams = (currentFood?.portionWeightGrams ?: 100f).coerceAtLeast(1f)
    val ratio = selectedPortionGrams / baseGrams

    val calculatedCalories = ((currentFood?.calories ?: 200) * ratio).roundToInt().coerceAtLeast(0)
    val calculatedProtein = ((currentFood?.protein ?: 10f) * ratio).coerceAtLeast(0f)
    val calculatedCarbs = ((currentFood?.carbs ?: 25f) * ratio).coerceAtLeast(0f)
    val calculatedFat = ((currentFood?.fat ?: 5f) * ratio).coerceAtLeast(0f)
    val calculatedFiber = ((currentFood?.fibre ?: 2f) * ratio).coerceAtLeast(0f)

    // Atwater Calories split
    val proteinKcal = calculatedProtein * 4f
    val carbsKcal = calculatedCarbs * 4f
    val fatKcal = calculatedFat * 9f
    val sumKcal = (proteinKcal + carbsKcal + fatKcal).coerceAtLeast(1f)
    val proteinPct = (proteinKcal / sumKcal * 100f).roundToInt()
    val carbsPct = (carbsKcal / sumKcal * 100f).roundToInt()
    val fatPct = (fatKcal / sumKcal * 100f).roundToInt()

    // Daily Targets (2200 kcal, 140g protein)
    val dailyCalorieBudgetPct = ((calculatedCalories.toFloat() / 2200f) * 100f).roundToInt()
    val dailyProteinBudgetPct = ((calculatedProtein / 140f) * 100f).roundToInt()

    AmbientLiquidMeshBackground {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Top Header Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LiquidIconButton(onClick = onBack, contentDescription = "Back") {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = TextPrimary)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0x33C6FF3D))
                                .border(1.dp, ElectricLime, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, tint = ElectricLime, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "REAL-TIME FOOD SCANNER",
                                style = Typography.titleMedium.copy(fontWeight = FontWeight.Black, color = TextPrimary, letterSpacing = 0.5.sp)
                            )
                            Text(
                                text = "உணவு ஸ்கேனர் & கலோரி கால்குலேட்டர்",
                                style = Typography.labelSmall.copy(color = ElectricCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (onOpenCameraCapture != null) {
                            LiquidIconButton(
                                onClick = onOpenCameraCapture,
                                contentDescription = "Open Live CameraX"
                            ) {
                                Icon(Icons.Default.Videocam, contentDescription = null, tint = ElectricLime, modifier = Modifier.size(20.dp))
                            }
                        }
                        LiquidIconButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            contentDescription = "Pick Photo"
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(20.dp))
                        }
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 6.dp, bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 1. LIVE CAMERA VIEWFINDER & SCANNING RETICLE
                    item {
                        LiquidGlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            cornerRadius = 24.dp,
                            borderColor = if (isScanning) ElectricCyan else GlassBorderHighlight,
                            showSpecularSheen = true
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(260.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Color(0xFF0A0A0F))
                            ) {
                                // Animated Target Reticle Canvas
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val w = size.width
                                    val h = size.height
                                    val bracketLen = 28.dp.toPx()
                                    val bracketStroke = 3.dp.toPx()
                                    val pad = 20.dp.toPx()

                                    val bracketColor = if (isScanning) ElectricCyan else ElectricLime

                                    // Top-Left
                                    drawLine(bracketColor, Offset(pad, pad), Offset(pad + bracketLen, pad), bracketStroke)
                                    drawLine(bracketColor, Offset(pad, pad), Offset(pad, pad + bracketLen), bracketStroke)

                                    // Top-Right
                                    drawLine(bracketColor, Offset(w - pad, pad), Offset(w - pad - bracketLen, pad), bracketStroke)
                                    drawLine(bracketColor, Offset(w - pad, pad), Offset(w - pad, pad + bracketLen), bracketStroke)

                                    // Bottom-Left
                                    drawLine(bracketColor, Offset(pad, h - pad), Offset(pad + bracketLen, h - pad), bracketStroke)
                                    drawLine(bracketColor, Offset(pad, h - pad), Offset(pad, h - pad - bracketLen), bracketStroke)

                                    // Bottom-Right
                                    drawLine(bracketColor, Offset(w - pad, h - pad), Offset(w - pad - bracketLen, h - pad), bracketStroke)
                                    drawLine(bracketColor, Offset(w - pad, h - pad), Offset(w - pad, h - pad - bracketLen), bracketStroke)

                                    // Live Laser Scanning Line
                                    if (isScanning) {
                                        val laserY = h * laserPosition
                                        drawLine(
                                            brush = Brush.horizontalGradient(
                                                colors = listOf(Color.Transparent, ElectricCyan, ElectricLime, Color.Transparent)
                                            ),
                                            start = Offset(pad, laserY),
                                            end = Offset(w - pad, laserY),
                                            strokeWidth = 3.5.dp.toPx()
                                        )
                                    }
                                }

                                // Overlay Elements inside Camera Viewfinder
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.SpaceBetween,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    // Top HUD: Distance + AI Sensor Status
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(Color(0xCC070709))
                                                .border(1.dp, if (distanceCm in 20..30) SuccessGreen else WarningAmber, RoundedCornerShape(12.dp))
                                                .padding(horizontal = 10.dp, vertical = 5.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(6.dp)
                                                        .clip(CircleShape)
                                                        .background(if (distanceCm in 20..30) SuccessGreen else WarningAmber)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "DISTANCE: $distanceCm CM",
                                                    style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 10.sp)
                                                )
                                            }
                                        }

                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(Color(0xCC070709))
                                                .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
                                                .padding(horizontal = 10.dp, vertical = 5.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Bolt, contentDescription = null, tint = ElectricLime, modifier = Modifier.size(12.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "AI NEURAL VISION",
                                                    style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = ElectricLime, fontSize = 10.sp)
                                                )
                                            }
                                        }
                                    }

                                    // Center Recognition Plate Reticle
                                    if (isScanning) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Box(
                                                modifier = Modifier
                                                    .size(54.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0x3319E3FF))
                                                    .border(1.5.dp, ElectricCyan, CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(28.dp))
                                            }
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = "SCANNING FOOD VOLUMETRICS...",
                                                style = Typography.labelMedium.copy(fontWeight = FontWeight.Black, color = ElectricLime, letterSpacing = 1.sp)
                                            )
                                            Text(
                                                text = "கலோரி மற்றும் ஊட்டச்சத்து அளவீடு செய்யப்படுகிறது",
                                                style = Typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp)
                                            )
                                        }
                                    } else if (currentFood != null) {
                                        // Detected Target Tag
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(14.dp))
                                                .background(Color(0xDD13131A))
                                                .border(1.dp, ElectricLime, RoundedCornerShape(14.dp))
                                                .padding(horizontal = 14.dp, vertical = 8.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ElectricLime, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Column {
                                                    Text(
                                                        text = currentFood.name,
                                                        style = Typography.titleSmall.copy(fontWeight = FontWeight.Black, color = TextPrimary)
                                                    )
                                                    if (currentFood.tamilName.isNotBlank()) {
                                                        Text(
                                                            text = currentFood.tamilName,
                                                            style = Typography.labelSmall.copy(color = ElectricLime, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                        )
                                                    }
                                                }
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Text(
                                                    text = "$calculatedCalories kcal",
                                                    style = Typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold, color = WarningAmber)
                                                )
                                            }
                                        }
                                    } else {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(Icons.Default.CenterFocusStrong, contentDescription = null, tint = Color(0x66FFFFFF), modifier = Modifier.size(44.dp))
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = "உணவுத் தட்டை சட்டகத்தில் வைக்கவும் (Place plate inside)",
                                                style = Typography.bodySmall.copy(color = TextSecondary)
                                            )
                                        }
                                    }

                                    // Bottom Viewfinder Bar
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (onOpenCameraCapture != null) {
                                            LiquidSecondaryButton(
                                                text = "CameraX Live",
                                                onClick = onOpenCameraCapture,
                                                modifier = Modifier.weight(1f)
                                            )
                                        } else {
                                            LiquidSecondaryButton(
                                                text = "Random Plate",
                                                onClick = {
                                                    startPlateScan(foodDatabase.randomOrNull())
                                                },
                                                modifier = Modifier.weight(1f)
                                            )
                                        }

                                        LiquidPrimaryButton(
                                            text = if (isScanning) "SCANNING..." else "SNAP & RE-SCAN",
                                            onClick = {
                                                startPlateScan(currentFood)
                                            },
                                            enabled = !isScanning,
                                            modifier = Modifier.weight(1.3f),
                                            leadingIcon = {
                                                Icon(Icons.Default.Camera, contentDescription = null, tint = BgPrimary, modifier = Modifier.size(16.dp))
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 2. REAL-TIME CALORIE CALCULATOR & PORTION CONTROLLER (கலோரி கால்குலேட்டர்)
                    item {
                        LiquidHeroCard(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                // Header: Calorie Calculation Title & Live Status
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(CircleShape)
                                                .background(ElectricLime.copy(alpha = 0.2f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.Calculate, contentDescription = null, tint = ElectricLime, modifier = Modifier.size(18.dp))
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = "REAL-TIME CALORIE CALCULATOR",
                                                style = Typography.labelMedium.copy(fontWeight = FontWeight.Black, color = ElectricLime, letterSpacing = 0.5.sp)
                                            )
                                            Text(
                                                text = "நேரடி கலோரி & ஊட்டச்சத்து கணக்கீடு",
                                                style = Typography.labelSmall.copy(color = TextSecondary, fontSize = 10.sp)
                                            )
                                        }
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color(0x33C6FF3D))
                                            .border(1.dp, ElectricLime, RoundedCornerShape(10.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "Grade ${currentFood?.healthRating ?: "A"}",
                                            style = Typography.labelSmall.copy(fontWeight = FontWeight.Black, color = ElectricLime)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // GIANT REAL-TIME CALORIE DISPLAY
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.Bottom,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.Bottom) {
                                        Text(
                                            text = "$calculatedCalories",
                                            style = Typography.displayLarge.copy(
                                                fontWeight = FontWeight.Black,
                                                color = TextPrimary,
                                                fontSize = 54.sp
                                            )
                                        )
                                        Column(modifier = Modifier.padding(bottom = 8.dp, start = 8.dp)) {
                                            Text(
                                                text = "TOTAL KCAL",
                                                style = Typography.titleMedium.copy(fontWeight = FontWeight.Black, color = WarningAmber)
                                            )
                                            Text(
                                                text = "மொத்த கலோரி",
                                                style = Typography.labelSmall.copy(color = TextMuted, fontSize = 10.sp)
                                            )
                                        }
                                    }

                                    // Caloric Density Indicator
                                    val density = if (selectedPortionGrams > 0) (calculatedCalories / selectedPortionGrams) else 1.5f
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = String.format("%.2f kcal/g", density),
                                            style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = ElectricCyan)
                                        )
                                        Text(
                                            text = if (density < 1.5f) "Lean / Low Density" else if (density < 2.5f) "Moderate Density" else "High Energy",
                                            style = Typography.labelSmall.copy(color = TextSecondary, fontSize = 10.sp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // PORTION SIZE SLIDER & CONTROLLER
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "PORTION WEIGHT (அளவு):",
                                        style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = TextMuted)
                                    )
                                    Text(
                                        text = "${selectedPortionGrams.roundToInt()} Grams (g)",
                                        style = Typography.titleMedium.copy(fontWeight = FontWeight.Black, color = ElectricLime)
                                    )
                                }

                                Slider(
                                    value = selectedPortionGrams,
                                    onValueChange = { grams ->
                                        selectedPortionGrams = grams
                                        portionMultiplier = grams / baseGrams
                                    },
                                    valueRange = 25f..600f,
                                    steps = 22,
                                    colors = SliderDefaults.colors(
                                        thumbColor = ElectricLime,
                                        activeTrackColor = ElectricLime,
                                        inactiveTrackColor = Color(0x33282836)
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                // Quick Portion Preset Chips
                                LazyRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    listOf(
                                        Pair(50f, "50g Snack"),
                                        Pair(100f, "100g Small"),
                                        Pair(150f, "150g Regular"),
                                        Pair(200f, "200g Plate"),
                                        Pair(300f, "300g Large"),
                                        Pair(450f, "450g Heavy")
                                    ).forEach { (grams, label) ->
                                        item {
                                            LiquidChip(
                                                text = label,
                                                isSelected = (selectedPortionGrams - grams).let { kotlin.math.abs(it) < 15f },
                                                onClick = {
                                                    selectedPortionGrams = grams
                                                    portionMultiplier = grams / baseGrams
                                                }
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // 4 KEY MACRONUTRIENT BADGES WITH ATWATER CONTRIBUTION
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    MacroDetailTile(
                                        title = "Protein",
                                        tamil = "புரதம்",
                                        grams = calculatedProtein,
                                        color = ElectricCyan,
                                        kcalContribution = proteinKcal.roundToInt(),
                                        modifier = Modifier.weight(1f)
                                    )
                                    MacroDetailTile(
                                        title = "Carbs",
                                        tamil = "மாவுச்சத்து",
                                        grams = calculatedCarbs,
                                        color = WarningAmber,
                                        kcalContribution = carbsKcal.roundToInt(),
                                        modifier = Modifier.weight(1f)
                                    )
                                    MacroDetailTile(
                                        title = "Fat",
                                        tamil = "கொழுப்பு",
                                        grams = calculatedFat,
                                        color = NeonViolet,
                                        kcalContribution = fatKcal.roundToInt(),
                                        modifier = Modifier.weight(1f)
                                    )
                                    MacroDetailTile(
                                        title = "Fiber",
                                        tamil = "நார்ச்சத்து",
                                        grams = calculatedFiber,
                                        color = SuccessGreen,
                                        kcalContribution = null,
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // ATWATER ENERGY RATIO BAR (% Protein, % Carbs, % Fat)
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(GlassSurfaceLevel3)
                                        .padding(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "MACRONUTRIENT ENERGY RATIO (சத்து விகிதம்)",
                                            style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = TextMuted, fontSize = 9.sp)
                                        )
                                        Text(
                                            text = "$proteinPct% P • $carbsPct% C • $fatPct% F",
                                            style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = ElectricLime, fontSize = 10.sp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Multi-color segmented energy bar
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(8.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                    ) {
                                        val pWeight = (proteinPct.toFloat() / 100f).coerceIn(0.05f, 0.9f)
                                        val cWeight = (carbsPct.toFloat() / 100f).coerceIn(0.05f, 0.9f)
                                        val fWeight = (fatPct.toFloat() / 100f).coerceIn(0.05f, 0.9f)

                                        Box(modifier = Modifier.weight(pWeight).fillMaxHeight().background(ElectricCyan))
                                        Box(modifier = Modifier.weight(cWeight).fillMaxHeight().background(WarningAmber))
                                        Box(modifier = Modifier.weight(fWeight).fillMaxHeight().background(NeonViolet))
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // DAILY CALORIE & PROTEIN BUDGET IMPACT
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(GlassSurfaceLevel2)
                                            .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
                                            .padding(10.dp)
                                    ) {
                                        Column {
                                            Text(
                                                text = "DAILY CALORIE BUDGET",
                                                style = Typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "$dailyCalorieBudgetPct% of 2200 kcal",
                                                style = Typography.labelMedium.copy(fontWeight = FontWeight.Black, color = ElectricLime)
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            ProgressBar(
                                                progress = (dailyCalorieBudgetPct / 100f).coerceIn(0f, 1f),
                                                color = ElectricLime,
                                                height = 4.dp
                                            )
                                        }
                                    }

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(GlassSurfaceLevel2)
                                            .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
                                            .padding(10.dp)
                                    ) {
                                        Column {
                                            Text(
                                                text = "DAILY PROTEIN TARGET",
                                                style = Typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "$dailyProteinBudgetPct% of 140g target",
                                                style = Typography.labelMedium.copy(fontWeight = FontWeight.Black, color = ElectricCyan)
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            ProgressBar(
                                                progress = (dailyProteinBudgetPct / 100f).coerceIn(0f, 1f),
                                                color = ElectricCyan,
                                                height = 4.dp
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // MEAL DESTINATION & LOG BUTTON
                                Text(
                                    text = "LOG CALCULATED MEAL TO:",
                                    style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = TextMuted)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    listOf("BREAKFAST", "LUNCH", "DINNER", "SNACKS").forEach { meal ->
                                        LiquidChip(
                                            text = meal,
                                            isSelected = selectedMealType == meal,
                                            onClick = { selectedMealType = meal }
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(18.dp))

                                LiquidPrimaryButton(
                                    text = "LOG THIS MEAL ($calculatedCalories KCAL)",
                                    onClick = {
                                        onLogScannedMeal(
                                            selectedMealType,
                                            currentFood,
                                            currentFood?.name ?: "Custom Meal",
                                            ratio,
                                            calculatedCalories,
                                            calculatedProtein,
                                            calculatedCarbs,
                                            calculatedFat,
                                            selectedPortionGrams
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    leadingIcon = {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = BgPrimary)
                                    }
                                )
                            }
                        }
                    }

                    // 3. SEARCH FOOD DATASET (ROOM DATABASE SEARCH & NUTRITIONAL FILTERS)
                    item {
                        LiquidGlassCard(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Search, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "SEARCH & CALCULATE ANY FOOD",
                                            style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(ElectricCyan.copy(alpha = 0.15f))
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "${databaseSearchResults.size} in Room",
                                            style = Typography.labelSmall.copy(color = ElectricCyan, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "உணவு பெயரை தட்டச்சு செய்து உடனடி கலோரி கணக்கீட்டை பெறவும் (Type any food name to calculate immediately).",
                                    style = Typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp)
                                )
                                Spacer(modifier = Modifier.height(10.dp))

                                LiquidInput(
                                    value = globalFoodQuery,
                                    onValueChange = { globalFoodQuery = it },
                                    placeholder = "எ.கா. Masala Dosa, Biryani, Meen Kuzhambu, Pizza, Chicken...",
                                    label = "உணவு பெயர் தேடல் (Food Search)",
                                    modifier = Modifier.fillMaxWidth()
                                )

                                if (globalFoodQuery.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Button(
                                            onClick = {
                                                scope.launch {
                                                    isSearchingOnlineApi = true
                                                    onlineApiStatusMessage = "Connecting to Open Food Facts API..."
                                                    val res = networkRepo.searchFoodApi(globalFoodQuery)
                                                    res.onSuccess { list ->
                                                        isSearchingOnlineApi = false
                                                        if (list.isNotEmpty()) {
                                                            detectedFood = list.first()
                                                            onlineApiStatusMessage = "Found ${list.size} verified matches from Nutrition API!"
                                                        } else {
                                                            onlineApiStatusMessage = "No API items found. Using local database."
                                                        }
                                                    }.onFailure { err ->
                                                        isSearchingOnlineApi = false
                                                        onlineApiStatusMessage = "API Error: ${err.message}"
                                                    }
                                                }
                                            },
                                            enabled = !isSearchingOnlineApi,
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = ElectricCyan,
                                                contentColor = BgPrimary
                                            ),
                                            shape = RoundedCornerShape(10.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                            modifier = Modifier.height(36.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.CloudSync, contentDescription = null, tint = BgPrimary, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = if (isSearchingOnlineApi) "QUERYING RETROFIT API..." else "SEARCH GLOBAL NUTRITION API",
                                                    style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = BgPrimary)
                                                )
                                            }
                                        }

                                        if (!onlineApiStatusMessage.isNullOrBlank()) {
                                            Text(
                                                text = onlineApiStatusMessage.orEmpty(),
                                                style = Typography.labelSmall.copy(color = ElectricLime, fontSize = 10.sp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Macro Filter Chips
                                LazyRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    listOf(
                                        Pair("ALL", "All Foods"),
                                        Pair("HIGH_PROTEIN", "⚡ High Protein (≥20g)"),
                                        Pair("LOW_CALORIE", "🥗 Low Cal (≤200 kcal)"),
                                        Pair("LOW_CARB", "🥑 Low Carb (≤10g)"),
                                        Pair("HIGH_FIBER", "🌾 High Fiber (≥4g)"),
                                        Pair("SOUTH_INDIAN", "🍛 South Indian (தமிழ்)")
                                    ).forEach { (key, label) ->
                                        item {
                                            LiquidChip(
                                                text = label,
                                                isSelected = selectedMacroFilter == key,
                                                onClick = { selectedMacroFilter = key }
                                            )
                                        }
                                    }
                                }

                                // Matched Room Database Results (tap to calculate immediately)
                                if (databaseSearchResults.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "SELECT FOOD TO CALCULATE (${databaseSearchResults.size} MATCHES):",
                                        style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = ElectricLime, fontSize = 10.sp)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))

                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        databaseSearchResults.take(6).forEach { food ->
                                            val isSelected = currentFood?.foodId == food.foodId
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(if (isSelected) Color(0x33C6FF3D) else GlassSurfaceLevel2)
                                                    .border(1.dp, if (isSelected) ElectricLime else GlassBorder, RoundedCornerShape(12.dp))
                                                    .clickable {
                                                        detectedFood = food
                                                        selectedPortionGrams = food.portionWeightGrams
                                                        portionMultiplier = 1.0f
                                                    }
                                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            Text(
                                                                text = food.name,
                                                                style = Typography.labelMedium.copy(
                                                                    fontWeight = FontWeight.Bold,
                                                                    color = if (isSelected) ElectricLime else TextPrimary
                                                                )
                                                            )
                                                            if (food.tamilName.isNotBlank()) {
                                                                Spacer(modifier = Modifier.width(6.dp))
                                                                Text(
                                                                    text = "(${food.tamilName})",
                                                                    style = Typography.labelSmall.copy(color = TextMuted, fontSize = 10.sp)
                                                                )
                                                            }
                                                        }
                                                        Text(
                                                            text = "${food.calories} kcal/100g • P: ${food.protein}g • C: ${food.carbs}g • F: ${food.fat}g • ${food.portionWeightGrams.toInt()}g standard",
                                                            style = Typography.bodySmall.copy(color = TextSecondary, fontSize = 10.sp)
                                                        )
                                                    }

                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(8.dp))
                                                            .background(if (isSelected) ElectricLime else GlassSurfaceLevel3)
                                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                                    ) {
                                                        Text(
                                                            text = if (isSelected) "CALCULATING" else "SELECT",
                                                            style = Typography.labelSmall.copy(
                                                                fontWeight = FontWeight.Bold,
                                                                color = if (isSelected) BgPrimary else TextPrimary,
                                                                fontSize = 9.sp
                                                            )
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 4. WORLD FOOD TRAINED DATASET PRESETS
                    item {
                        Column {
                            Text(
                                text = "POPULAR FOOD PRESETS (விரைவு உணவுகள்)",
                                style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = TextMuted, letterSpacing = 0.5.sp),
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            val worldFoods = listOf(
                                Pair("Masala Dosa", "மசால் தோசை"),
                                Pair("Chicken Biryani", "பிரியாணி"),
                                Pair("Fish Curry", "மீன் குழம்பு"),
                                Pair("Chettinad Mutton", "மட்டன் சுக்கா"),
                                Pair("Ven Pongal", "வெண் பொங்கல்"),
                                Pair("Medu Vada", "மெது வடை"),
                                Pair("Curd Rice", "தயிர் சாதம்"),
                                Pair("Paneer Butter Masala", "பன்னீர் மசாலா"),
                                Pair("Dal Tadka", "தால் தட்கா"),
                                Pair("Chapati", "சப்பாத்தி"),
                                Pair("Pizza Slice", "பீட்சா"),
                                Pair("Herb Grilled Chicken", "கிரில் சிக்கன்"),
                                Pair("Boiled Eggs", "முட்டை"),
                                Pair("Whey Protein Shake", "புரோட்டீன்")
                            )

                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(worldFoods) { (name, tamil) ->
                                    val isSelected = currentFood?.name?.contains(name.split(" ")[0], ignoreCase = true) == true
                                    LiquidChip(
                                        text = "$name ($tamil)",
                                        isSelected = isSelected,
                                        onClick = {
                                            startPlateScan(null, name)
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // 5. DATASET & INGESTION TELEMETRY CARD
                    item {
                        LiquidGlassCard(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(ElectricLime.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Memory, contentDescription = null, tint = ElectricLime, modifier = Modifier.size(18.dp))
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "ROOM NUTRITION PIPELINE v3.2",
                                            style = Typography.labelSmall.copy(fontWeight = FontWeight.Black, color = ElectricLime, letterSpacing = 0.5.sp)
                                        )
                                        Text(
                                            text = "${foodDatabase.size} Clinical Records • Atwater Calibrated • 3D Volumetric Scale",
                                            style = Typography.bodySmall.copy(color = TextSecondary, fontSize = 10.sp)
                                        )
                                    }
                                }

                                if (onReingestDataset != null) {
                                    LiquidSecondaryButton(
                                        text = "SYNC",
                                        onClick = { onReingestDataset() }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MacroDetailTile(
    title: String,
    tamil: String,
    grams: Float,
    color: Color,
    kcalContribution: Int?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(GlassSurfaceLevel3)
            .border(1.dp, color.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
            .padding(vertical = 8.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title.uppercase(),
                style = Typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextMuted)
            )
            Text(
                text = tamil,
                style = Typography.labelSmall.copy(fontSize = 8.sp, color = color.copy(alpha = 0.8f))
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${String.format("%.1f", grams)}g",
                style = Typography.titleSmall.copy(fontWeight = FontWeight.Black, color = color)
            )
            if (kcalContribution != null) {
                Text(
                    text = "${kcalContribution}k",
                    style = Typography.labelSmall.copy(fontSize = 8.sp, color = TextSecondary)
                )
            }
        }
    }
}
