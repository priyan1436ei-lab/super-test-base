package com.example.data.network

import android.content.Context
import android.util.Log
import com.example.data.auth.SupabaseAuthService
import com.example.data.model.FoodLog
import com.example.data.model.User
import com.example.data.model.WorkoutSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Supabase Data Service (PostgREST API Client)
 * Synchronizes FitTrack AI athlete profiles, workout sessions, and nutrition logs
 * directly to Supabase PostgreSQL database via PostgREST.
 */
class SupabaseDataService(
    private val context: Context,
    private val authService: SupabaseAuthService
) {

    companion object {
        private const val TAG = "SupabaseDataService"
    }

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(12, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private fun buildRequest(endpoint: String, method: String = "GET", bodyJson: String? = null): Request {
        val url = "${authService.supabaseUrl}/rest/v1/$endpoint"
        val token = authService.getAccessToken() ?: authService.supabaseAnonKey

        val builder = Request.Builder()
            .url(url)
            .addHeader("apikey", authService.supabaseAnonKey)
            .addHeader("Authorization", "Bearer $token")
            .addHeader("Content-Type", "application/json")
            // Upsert header: merge on primary key conflict
            .addHeader("Prefer", "resolution=merge-duplicates,return=representation")

        when (method.uppercase()) {
            "POST" -> builder.post((bodyJson ?: "{}").toRequestBody(jsonMediaType))
            "PUT" -> builder.put((bodyJson ?: "{}").toRequestBody(jsonMediaType))
            "PATCH" -> builder.patch((bodyJson ?: "{}").toRequestBody(jsonMediaType))
            "DELETE" -> builder.delete()
            else -> builder.get()
        }

        return builder.build()
    }

    /**
     * Upserts User profile to Supabase 'users' table
     */
    suspend fun syncUserProfile(user: User): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val payload = JSONObject().apply {
                put("user_id", user.userId)
                put("full_name", user.fullName)
                put("username", user.username)
                put("email", user.email)
                put("phone", user.phone)
                put("age", user.age)
                put("gender", user.gender)
                put("height_cm", user.heightCm.toDouble())
                put("weight_kg", user.weightKg.toDouble())
                put("target_weight_kg", user.targetWeightKg.toDouble())
                put("fitness_goal", user.fitnessGoal)
                put("activity_level", user.activityLevel)
                put("subscription_tier", user.subscriptionTier)
                put("current_streak", user.currentStreak)
                put("workouts_completed_count", user.workoutsCompletedCount)
                put("updated_at", System.currentTimeMillis())
            }

            val request = buildRequest("users", method = "POST", bodyJson = payload.toString())
            httpClient.newCall(request).execute().use { response ->
                Log.d(TAG, "Sync user response code: ${response.code}")
                if (response.isSuccessful || response.code == 201) {
                    Result.success(true)
                } else {
                    Result.failure(Exception("Supabase sync user returned ${response.code}: ${response.message}"))
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Offline or sync user profile failed: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Upserts a WorkoutSession to Supabase 'workout_sessions' table
     */
    suspend fun syncWorkoutSession(session: WorkoutSession): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val payload = JSONObject().apply {
                put("session_id", session.sessionId)
                put("user_id", session.userId)
                put("plan_name", session.planName)
                put("start_time", session.startTime)
                put("end_time", session.endTime)
                put("duration_minutes", session.durationMinutes)
                put("total_calories_burned", session.totalCaloriesBurned)
                put("notes", session.notes)
                put("completed_date", session.completedDate)
            }

            val request = buildRequest("workout_sessions", method = "POST", bodyJson = payload.toString())
            httpClient.newCall(request).execute().use { response ->
                Log.d(TAG, "Sync workout session response code: ${response.code}")
                if (response.isSuccessful || response.code == 201) {
                    Result.success(true)
                } else {
                    Result.failure(Exception("Supabase sync workout returned ${response.code}"))
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Offline or sync workout session failed: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Upserts a FoodLog to Supabase 'food_logs' table
     */
    suspend fun syncFoodLog(food: FoodLog): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val payload = JSONObject().apply {
                put("log_id", food.logId)
                put("user_id", food.userId)
                put("food_name", food.foodName)
                put("calories", food.calories)
                put("protein_g", food.proteinG.toDouble())
                put("carbs_g", food.carbsG.toDouble())
                put("fats_g", food.fatsG.toDouble())
                put("meal_type", food.mealType)
                put("log_date", food.logDate)
                put("serving_size", food.servingSize)
            }

            val request = buildRequest("food_logs", method = "POST", bodyJson = payload.toString())
            httpClient.newCall(request).execute().use { response ->
                Log.d(TAG, "Sync food log response code: ${response.code}")
                if (response.isSuccessful || response.code == 201) {
                    Result.success(true)
                } else {
                    Result.failure(Exception("Supabase sync food log returned ${response.code}"))
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Offline or sync food log failed: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Fetches recent WorkoutSessions for a user from Supabase
     */
    suspend fun fetchWorkoutSessions(userId: String = authService.getUserId()): Result<List<JSONObject>> = withContext(Dispatchers.IO) {
        try {
            val endpoint = "workout_sessions?user_id=eq.$userId&order=completed_date.desc&limit=50"
            val request = buildRequest(endpoint, method = "GET")
            httpClient.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: "[]"
                if (response.isSuccessful) {
                    val jsonArray = JSONArray(body)
                    val list = mutableListOf<JSONObject>()
                    for (i in 0 until jsonArray.length()) {
                        list.add(jsonArray.getJSONObject(i))
                    }
                    Result.success(list)
                } else {
                    Result.failure(Exception("Supabase fetch returned ${response.code}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
