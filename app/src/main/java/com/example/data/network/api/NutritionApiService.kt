package com.example.data.network.api

import com.example.data.network.model.BarcodeProductResponse
import com.example.data.network.model.OpenFoodFactsSearchResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface NutritionApiService {

    /**
     * Search global and regional food items in the Open Food Facts database
     */
    @GET("cgi/search.pl")
    suspend fun searchFood(
        @Query("search_terms") query: String,
        @Query("search_simple") searchSimple: Int = 1,
        @Query("action") action: String = "process",
        @Query("json") json: Int = 1,
        @Query("page_size") pageSize: Int = 25,
        @Query("fields") fields: String = "code,product_name,generic_name,brands,nutriments,serving_size,image_url,categories,nutrition_grades"
    ): OpenFoodFactsSearchResponse

    /**
     * Fetch exact barcode verified food entry
     */
    @GET("api/v2/product/{barcode}.json")
    suspend fun getProductByBarcode(
        @Path("barcode") barcode: String
    ): BarcodeProductResponse
}
