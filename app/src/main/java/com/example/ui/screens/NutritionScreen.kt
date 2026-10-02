package com.example.ui.screens

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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FoodItem
import com.example.data.model.FoodLog
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun NutritionScreen(
    foodLogs: List<FoodLog>,
    foodDatabase: List<FoodItem>,
    onLogMeal: (mealType: String, food: FoodItem?, customName: String, qty: Float, cal: Int, p: Float, c: Float, f: Float) -> Unit,
    onDeleteMeal: (FoodLog) -> Unit,
    onOpenScanner: () -> Unit = {}
) {
    var showAddMealDialog by remember { mutableStateOf(false) }
    var targetMealType by remember { mutableStateOf("BREAKFAST") }

    // Macros computation
    val totalCalories = foodLogs.sumOf { it.calories }
    val targetCalories = 2200
    val totalProtein = foodLogs.sumOf { it.protein.toDouble() }.toFloat()
    val targetProtein = 140f
    val totalCarbs = foodLogs.sumOf { it.carbs.toDouble() }.toFloat()
    val targetCarbs = 240f
    val totalFat = foodLogs.sumOf { it.fat.toDouble() }.toFloat()
    val targetFat = 70f

    val mealTypes = listOf("BREAKFAST", "LUNCH", "DINNER", "SNACKS")

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
            // Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "NUTRITION & MACROS",
                            style = Typography.headlineMedium.copy(fontWeight = FontWeight.Black, color = TextPrimary)
                        )
                        Text(
                            text = "Real-time caloric and protein adherence",
                            style = Typography.bodySmall.copy(color = TextSecondary)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        LiquidIconButton(
                            onClick = onOpenScanner,
                            contentDescription = "Scan Plate",
                            backgroundColor = Color(0x33C6FF3D)
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, tint = ElectricLime, modifier = Modifier.size(20.dp))
                        }

                        LiquidPrimaryButton(
                            text = "+ ADD MEAL",
                            onClick = {
                                targetMealType = "LUNCH"
                                showAddMealDialog = true
                            }
                        )
                    }
                }
            }

            // AI FOOD VISION SCANNER BANNER
            item {
                LiquidGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = ElectricCyan.copy(alpha = 0.6f),
                    ambientGlowBrush = VioletCyanGradient,
                    onClick = onOpenScanner
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(ElectricCyan))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "AI FOOD SCANNER (உணவு ஸ்கேனர்)",
                                    style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = ElectricCyan)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Snap Food Plate & Detect Nutrients",
                                style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Volumetric portion estimation, calories, protein & micronutrients at 20-30cm distance.",
                                style = Typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color(0x3319E3FF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(24.dp))
                        }
                    }
                }
            }

            // Daily Target Calories & Macros Hero Card
            item {
                LiquidHeroCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "CALORIC INTAKE",
                                    style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = ElectricLime)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text(
                                        text = "$totalCalories",
                                        style = Typography.headlineLarge.copy(fontWeight = FontWeight.Black, color = TextPrimary)
                                    )
                                    Text(
                                        text = " / $targetCalories kcal",
                                        style = Typography.titleMedium.copy(color = TextMuted),
                                        modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x33C6FF3D)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = ElectricLime, modifier = Modifier.size(26.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        ProgressBar(
                            progress = totalCalories.toFloat() / targetCalories,
                            color = ElectricLime,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        // Three Macros: Protein, Carbs, Fat
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            MacroBarItem(
                                title = "Protein",
                                current = totalProtein.toInt(),
                                target = targetProtein.toInt(),
                                unit = "g",
                                color = ElectricCyan,
                                modifier = Modifier.weight(1f)
                            )
                            MacroBarItem(
                                title = "Carbs",
                                current = totalCarbs.toInt(),
                                target = targetCarbs.toInt(),
                                unit = "g",
                                color = WarningAmber,
                                modifier = Modifier.weight(1f)
                            )
                            MacroBarItem(
                                title = "Fat",
                                current = totalFat.toInt(),
                                target = targetFat.toInt(),
                                unit = "g",
                                color = NeonViolet,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Meals Sections: Breakfast, Lunch, Dinner, Snacks
            items(mealTypes) { meal ->
                val logsForMeal = foodLogs.filter { it.mealType.equals(meal, ignoreCase = true) }
                val mealCalories = logsForMeal.sumOf { it.calories }
                val mealProtein = logsForMeal.sumOf { it.protein.toDouble() }.toInt()

                LiquidGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 20.dp
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = meal,
                                    style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                                )
                                Text(
                                    text = "$mealCalories kcal • ${mealProtein}g protein",
                                    style = Typography.bodySmall.copy(color = TextSecondary)
                                )
                            }

                            LiquidIconButton(
                                onClick = {
                                    targetMealType = meal
                                    showAddMealDialog = true
                                },
                                contentDescription = "Add to $meal"
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = ElectricLime, modifier = Modifier.size(20.dp))
                            }
                        }

                        if (logsForMeal.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = GlassBorder, thickness = 0.8.dp)
                            Spacer(modifier = Modifier.height(10.dp))

                            logsForMeal.forEach { logItem ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = logItem.customFoodName,
                                            style = Typography.bodyMedium.copy(fontWeight = FontWeight.Medium, color = TextPrimary)
                                        )
                                        Text(
                                            text = "P: ${logItem.protein.toInt()}g • C: ${logItem.carbs.toInt()}g • F: ${logItem.fat.toInt()}g",
                                            style = Typography.bodySmall.copy(color = TextMuted)
                                        )
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "${logItem.calories} kcal",
                                            style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = ElectricLime)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        IconButton(
                                            onClick = { onDeleteMeal(logItem) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = TextMuted, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Add Meal Dialog / Sheet
        if (showAddMealDialog) {
            AddFoodDialog(
                mealType = targetMealType,
                foodDatabase = foodDatabase,
                onDismiss = { showAddMealDialog = false },
                onAdd = { food, customName, qty, cal, p, c, f ->
                    onLogMeal(targetMealType, food, customName, qty, cal, p, c, f)
                    showAddMealDialog = false
                }
            )
        }
    }
}
}

@Composable
fun MacroBarItem(
    title: String,
    current: Int,
    target: Int,
    unit: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(GlassSurfaceLevel2)
            .border(1.dp, GlassBorder, RoundedCornerShape(14.dp))
            .padding(10.dp)
    ) {
        Column {
            Text(text = title.uppercase(), style = Typography.labelSmall.copy(fontSize = 10.sp, color = TextMuted))
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "$current / $target$unit",
                style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
            )
            Spacer(modifier = Modifier.height(6.dp))
            ProgressBar(
                progress = current.toFloat() / target,
                color = color,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun AddFoodDialog(
    mealType: String,
    foodDatabase: List<FoodItem>,
    onDismiss: () -> Unit,
    onAdd: (food: FoodItem?, customName: String, qty: Float, cal: Int, p: Float, c: Float, f: Float) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedMacroFilter by remember { mutableStateOf("ALL") }
    var selectedFood by remember { mutableStateOf<FoodItem?>(null) }
    var quantityStr by remember { mutableStateOf("1") }

    var isCustomEntry by remember { mutableStateOf(false) }
    var customName by remember { mutableStateOf("") }
    var customCalStr by remember { mutableStateOf("") }
    var customProteinStr by remember { mutableStateOf("") }
    var customCarbsStr by remember { mutableStateOf("") }
    var customFatStr by remember { mutableStateOf("") }

    val filteredFoods = remember(foodDatabase, searchQuery, selectedMacroFilter) {
        val q = searchQuery.trim().lowercase()
        foodDatabase.filter { food ->
            val matchesQuery = q.isEmpty() ||
                food.name.lowercase().contains(q) ||
                food.tamilName.lowercase().contains(q) ||
                food.category.lowercase().contains(q)

            val matchesMacro = when (selectedMacroFilter) {
                "HIGH_PROTEIN" -> food.protein >= 20f
                "LOW_CALORIE" -> food.calories <= 200
                "LOW_CARB" -> food.carbs <= 10f
                "HIGH_FIBER" -> food.fibre >= 4f
                "SOUTH_INDIAN" -> food.isIndianFood || food.category.contains("South Indian", ignoreCase = true) || food.tamilName.isNotBlank()
                else -> true
            }

            matchesQuery && matchesMacro
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.BottomCenter
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(BgCard)
                .border(1.dp, GlassBorderHighlight, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .clickable(enabled = false) {}
                .padding(20.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Log $mealType",
                            style = Typography.headlineSmall.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                        )
                        Text(
                            text = "Search Indian & global nutritional database",
                            style = Typography.bodySmall.copy(color = TextSecondary)
                        )
                    }

                    LiquidIconButton(onClick = onDismiss, contentDescription = "Close") {
                        Icon(Icons.Default.Close, contentDescription = null, tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                LiquidSegmentControl(
                    options = listOf("Database Search", "Custom Manual"),
                    selectedIndex = if (!isCustomEntry) 0 else 1,
                    onOptionSelected = { isCustomEntry = it == 1 }
                )

                Spacer(modifier = Modifier.height(14.dp))

                if (!isCustomEntry) {
                    LiquidInput(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = "Search Idli, Dosa, Chicken, Dal, Whey...",
                        trailingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted)
                        }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Macro filter chips
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            Pair("ALL", "All (${filteredFoods.size})"),
                            Pair("HIGH_PROTEIN", "⚡ High Protein"),
                            Pair("LOW_CALORIE", "🥗 Low Cal"),
                            Pair("LOW_CARB", "🥑 Low Carb"),
                            Pair("HIGH_FIBER", "🌾 High Fiber"),
                            Pair("SOUTH_INDIAN", "🍛 South Indian")
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

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredFoods) { food ->
                            val isSelected = selectedFood?.foodId == food.foodId
                            LiquidGlassCard(
                                modifier = Modifier.fillMaxWidth(),
                                cornerRadius = 14.dp,
                                borderColor = if (isSelected) ElectricLime else GlassBorder,
                                backgroundColor = if (isSelected) Color(0x33C6FF3D) else GlassSurfaceLevel2,
                                onClick = { selectedFood = food }
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = food.name,
                                            style = Typography.titleSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) ElectricLime else TextPrimary
                                            )
                                        )
                                        Text(
                                            text = "Serving: ${food.servingSize.toInt()} ${food.servingUnit} • P: ${food.protein}g • C: ${food.carbs}g • F: ${food.fat}g",
                                            style = Typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp)
                                        )
                                    }
                                    Text(
                                        text = "${food.calories} kcal",
                                        style = Typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) ElectricLime else WarningAmber
                                        )
                                    )
                                }
                            }
                        }
                    }

                    if (selectedFood != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                LiquidInput(
                                    value = quantityStr,
                                    onValueChange = { quantityStr = it },
                                    label = "Servings / Quantity"
                                )
                            }
                            LiquidPrimaryButton(
                                text = "LOG MEAL",
                                onClick = {
                                    val qty = quantityStr.toFloatOrNull() ?: 1f
                                    onAdd(selectedFood, selectedFood!!.name, qty, selectedFood!!.calories, selectedFood!!.protein, selectedFood!!.carbs, selectedFood!!.fat)
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                } else {
                    // Custom manual food entry
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            LiquidInput(value = customName, onValueChange = { customName = it }, label = "Food / Meal Name", placeholder = "e.g. Protein Smoothie")
                        }
                        item {
                            LiquidInput(value = customCalStr, onValueChange = { customCalStr = it }, label = "Calories (kcal)", placeholder = "350")
                        }
                        item {
                            LiquidInput(value = customProteinStr, onValueChange = { customProteinStr = it }, label = "Protein (g)", placeholder = "30")
                        }
                        item {
                            LiquidInput(value = customCarbsStr, onValueChange = { customCarbsStr = it }, label = "Carbs (g)", placeholder = "40")
                        }
                        item {
                            LiquidInput(value = customFatStr, onValueChange = { customFatStr = it }, label = "Fat (g)", placeholder = "8")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    LiquidPrimaryButton(
                        text = "ADD CUSTOM MEAL",
                        onClick = {
                            val cal = customCalStr.toIntOrNull() ?: 0
                            val p = customProteinStr.toFloatOrNull() ?: 0f
                            val c = customCarbsStr.toFloatOrNull() ?: 0f
                            val f = customFatStr.toFloatOrNull() ?: 0f
                            onAdd(null, customName.ifBlank { "Custom Meal" }, 1f, cal, p, c, f)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
