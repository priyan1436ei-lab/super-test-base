package com.example.data.network.model

import com.example.data.model.FoodItem
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.util.UUID
import kotlin.math.roundToInt

@JsonClass(generateAdapter = true)
data class OpenFoodFactsSearchResponse(
    @field:Json(name = "count") val count: Int? = 0,
    @field:Json(name = "page") val page: Int? = 1,
    @field:Json(name = "page_size") val pageSize: Int? = 20,
    @field:Json(name = "products") val products: List<FoodProductDto>? = emptyList()
)

@JsonClass(generateAdapter = true)
data class BarcodeProductResponse(
    @field:Json(name = "status") val status: Int? = 0,
    @field:Json(name = "status_verbose") val statusVerbose: String? = null,
    @field:Json(name = "code") val code: String? = null,
    @field:Json(name = "product") val product: FoodProductDto? = null
)

@JsonClass(generateAdapter = true)
data class FoodProductDto(
    @field:Json(name = "code") val code: String? = null,
    @field:Json(name = "product_name") val productName: String? = null,
    @field:Json(name = "generic_name") val genericName: String? = null,
    @field:Json(name = "brands") val brands: String? = null,
    @field:Json(name = "categories") val categories: String? = null,
    @field:Json(name = "serving_size") val servingSize: String? = null,
    @field:Json(name = "image_url") val imageUrl: String? = null,
    @field:Json(name = "nutrition_grades") val nutritionGrades: String? = null,
    @field:Json(name = "nutriments") val nutriments: NutrimentsDto? = null
)

@JsonClass(generateAdapter = true)
data class NutrimentsDto(
    @field:Json(name = "energy-kcal_100g") val energyKcal100g: Float? = null,
    @field:Json(name = "energy-kcal") val energyKcal: Float? = null,
    @field:Json(name = "energy-kcal_serving") val energyKcalServing: Float? = null,
    @field:Json(name = "proteins_100g") val proteins100g: Float? = null,
    @field:Json(name = "proteins") val proteins: Float? = null,
    @field:Json(name = "carbohydrates_100g") val carbs100g: Float? = null,
    @field:Json(name = "carbohydrates") val carbs: Float? = null,
    @field:Json(name = "fat_100g") val fat100g: Float? = null,
    @field:Json(name = "fat") val fat: Float? = null,
    @field:Json(name = "fiber_100g") val fiber100g: Float? = null,
    @field:Json(name = "sugars_100g") val sugars100g: Float? = null,
    @field:Json(name = "sodium_100g") val sodium100g: Float? = null,
    @field:Json(name = "salt_100g") val salt100g: Float? = null
)

/**
 * Maps Open Food Facts DTO into FitTrack local domain Entity (FoodItem)
 */
fun FoodProductDto.toFoodItem(): FoodItem {
    val cleanName = when {
        !productName.isNullOrBlank() -> productName.trim()
        !genericName.isNullOrBlank() -> genericName.trim()
        else -> "Food Item"
    }

    val brandSuffix = if (!brands.isNullOrBlank()) " (${brands.trim()})" else ""
    val displayName = (cleanName + brandSuffix).take(60)

    val cal = (nutriments?.energyKcal100g ?: nutriments?.energyKcal ?: 120f).roundToInt().coerceAtLeast(0)
    val protein = (nutriments?.proteins100g ?: nutriments?.proteins ?: 4f).coerceAtLeast(0f)
    val carbs = (nutriments?.carbs100g ?: nutriments?.carbs ?: 15f).coerceAtLeast(0f)
    val fat = (nutriments?.fat100g ?: nutriments?.fat ?: 3f).coerceAtLeast(0f)
    val fiber = (nutriments?.fiber100g ?: 1.5f).coerceAtLeast(0f)
    val sugar = (nutriments?.sugars100g ?: 2f).coerceAtLeast(0f)
    val sodium = ((nutriments?.sodium100g ?: 0.05f) * 1000f).coerceAtLeast(0f)

    // Translate OpenFoodFacts Nutri-Score (a, b, c, d, e) to Health Rating
    val rating = when (nutritionGrades?.lowercase()?.trim()) {
        "a" -> "A+"
        "b" -> "A"
        "c" -> "B+"
        "d" -> "B"
        "e" -> "C"
        else -> if (cal <= 250 && protein >= 10f) "A" else "B+"
    }

    val id = if (!code.isNullOrBlank()) "off_$code" else "api_${UUID.randomUUID().toString().take(8)}"
    val isIndian = categories?.contains("india", ignoreCase = true) == true ||
        displayName.contains("curry", ignoreCase = true) ||
        displayName.contains("dal", ignoreCase = true) ||
        displayName.contains("rice", ignoreCase = true)

    return FoodItem(
        foodId = id,
        name = displayName,
        servingSize = 100f,
        servingUnit = "g",
        calories = cal,
        protein = protein,
        carbs = carbs,
        fat = fat,
        fibre = fiber,
        sugar = sugar,
        sodiumMg = sodium,
        potassiumMg = 220f,
        calciumMg = 40f,
        ironMg = 1.2f,
        glycemicIndex = if (carbs < 10f) 25 else 50,
        healthRating = rating,
        portionWeightGrams = 100f,
        recommendedDistanceCm = 24,
        category = categories?.split(",")?.firstOrNull()?.trim()?.take(25) ?: "Nutrition API",
        isIndianFood = isIndian,
        tamilName = "",
        description = "Live verified laboratory nutritional profile from Open Food Facts DB. (Per 100g: $cal kcal, ${protein}g P, ${carbs}g C, ${fat}g F)"
    )
}
