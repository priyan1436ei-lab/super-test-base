package com.example.data.network

import com.example.data.local.NutritionDao
import com.example.data.model.FoodItem
import com.example.data.network.model.toFoodItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

class NutritionNetworkRepository(
    private val nutritionDao: NutritionDao? = null,
    private val apiService: com.example.data.network.api.NutritionApiService = NutritionApiClient.apiService
) {

    /**
     * Queries Open Food Facts public database for accurate laboratory nutritional data.
     * Maps products to FoodItem domain entities and caches them into Room SQLite if dao is available.
     */
    suspend fun searchFoodApi(query: String): Result<List<FoodItem>> = withContext(Dispatchers.IO) {
        val cleanQuery = query.trim()
        if (cleanQuery.isBlank()) {
            return@withContext Result.success(emptyList())
        }

        try {
            val response = apiService.searchFood(query = cleanQuery)
            val products = response.products.orEmpty()

            val foodItems = products
                .filter { it.nutriments != null && (it.productName != null || it.genericName != null) }
                .map { it.toFoodItem() }

            // Cache fetched food items locally in Room for offline access
            if (foodItems.isNotEmpty() && nutritionDao != null) {
                try {
                    nutritionDao.insertFoodItems(foodItems)
                } catch (_: Exception) {}
            }

            Result.success(foodItems)
        } catch (e: IOException) {
            Result.failure(Exception("Network error while contacting Nutrition Database: ${e.localizedMessage ?: "Connection timed out"}", e))
        } catch (e: Exception) {
            Result.failure(Exception("Failed to parse nutritional data: ${e.localizedMessage}", e))
        }
    }

    /**
     * Look up exact barcode item from Open Food Facts API
     */
    suspend fun getFoodByBarcode(barcode: String): Result<FoodItem?> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getProductByBarcode(barcode.trim())
            val product = response.product
            if (product != null) {
                val foodItem = product.toFoodItem()
                nutritionDao?.insertFoodItems(listOf(foodItem))
                Result.success(foodItem)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
