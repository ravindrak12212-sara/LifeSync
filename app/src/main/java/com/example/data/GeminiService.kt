package com.example.data

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiService {
    private const val TAG = "GeminiService"
    
    // We use 'gemini-3.5-flash' as specified by the Gemini API guidelines for basic/standard text tasks
    private const val MODEL_NAME = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun getCoachingSuggestion(
        username: String,
        habits: List<Habit>,
        logs: List<HabitLog>,
        tasks: List<PlannerTask>,
        goals: List<UserGoal>,
        journals: List<DailyJournal>,
        healthLogs: List<HealthLog>
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "Please configure your GEMINI_API_KEY in the Secrets panel in AI Studio to unlock full real-time AI Coaching suggestions!"
        }

        // Build a highly descriptive prompt about the user's active life stats
        val prompt = StringBuilder()
        prompt.append("You are the LifeSync AI Coach, an expert in personal growth, habit building, productivity, and mental wellness.\n\n")
        prompt.append("Analyze the following real-time data for user '$username' and provide brief, punchy, highly actionable recommendations (3-4 bullet points) covering routine improvement, streak preservation, productivity score, and positive encouragement. Keep it engaging, direct, and under 150 words.\n\n")
        
        prompt.append("--- USER STATS ---\n")
        prompt.append("Active Habits:\n")
        if (habits.isEmpty()) {
            prompt.append("- None created yet.\n")
        } else {
            habits.forEach { prompt.append("- ${it.emoji} ${it.name} (Category: ${it.category}, Priority: ${it.priority}, Difficulty: ${it.difficulty})\n") }
        }

        prompt.append("\nRecent Habit Logs (completions):\n")
        if (logs.isEmpty()) {
            prompt.append("- No completions logged yet.\n")
        } else {
            logs.take(10).forEach { prompt.append("- Habit ID: ${it.habitId} completed on ${it.date}\n") }
        }

        prompt.append("\nToday's Timetable & Tasks:\n")
        if (tasks.isEmpty()) {
            prompt.append("- No tasks scheduled for today.\n")
        } else {
            tasks.forEach { prompt.append("- ${it.title} (Time: ${it.startTime ?: "Anytime"}, Priority: ${it.priority}, Status: ${if (it.isCompleted) "Completed" else "Pending"})\n") }
        }

        prompt.append("\nPersonal Goals:\n")
        if (goals.isEmpty()) {
            prompt.append("- No personal goals defined.\n")
        } else {
            goals.forEach { prompt.append("- ${it.title} (Progress: ${it.currentValue}/${it.targetValue}, Category: ${it.category})\n") }
        }

        prompt.append("\nRecent Journals & Mood:\n")
        if (journals.isEmpty()) {
            prompt.append("- No mood logged recently.\n")
        } else {
            journals.take(3).forEach { prompt.append("- ${it.date}: Mood was ${it.mood}. Log: ${it.text}\n") }
        }

        prompt.append("\nRecent Health Logs:\n")
        if (healthLogs.isEmpty()) {
            prompt.append("- No hydration/steps logged recently.\n")
        } else {
            healthLogs.take(3).forEach { prompt.append("- Date: ${it.date}: Water: ${it.waterIntakeMl}/${it.waterGoalMl}ml, Steps: ${it.steps}, Sleep: ${it.sleepHours}h, Heart Rate: ${it.heartRate ?: "N/A"} bpm\n") }
        }

        prompt.append("\nResponse format:\n")
        prompt.append("💡 **AI Daily Highlight**: <One punchy insight>\n")
        prompt.append("📈 **Routine Suggestion**: <Actionable tweak to improve daily flow>\n")
        prompt.append("🔥 **Streak & Gamification Guide**: <A quick tip to prevent streak breaks and earn XP>\n")
        prompt.append("🌱 **Health & Wellness Tip**: <Brief advice based on sleep/steps/water>")

        try {
            // Build Gemini Request JSON
            val requestJson = JSONObject()
            val contentsArray = JSONArray()
            val contentObj = JSONObject()
            val partsArray = JSONArray()
            val partObj = JSONObject()
            
            partObj.put("text", prompt.toString())
            partsArray.put(partObj)
            contentObj.put("parts", partsArray)
            contentsArray.put(contentObj)
            requestJson.put("contents", contentsArray)

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = requestJson.toString().toRequestBody(mediaType)

            val url = "$BASE_URL?key=$apiKey"
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                val errorMsg = response.body?.string() ?: "Unknown error"
                Log.e(TAG, "Gemini API call failed: $errorMsg")
                return@withContext "AI Coach is sleeping right now. (Server code: ${response.code}). Check your internet connection and API key!"
            }

            val responseBody = response.body?.string() ?: ""
            val responseJson = JSONObject(responseBody)
            val candidates = responseJson.getJSONArray("candidates")
            if (candidates.length() > 0) {
                val firstCandidate = candidates.getJSONObject(0)
                val content = firstCandidate.getJSONObject("content")
                val parts = content.getJSONArray("parts")
                if (parts.length() > 0) {
                    return@withContext parts.getJSONObject(0).getString("text")
                }
            }
            "AI Coach could not synthesize stats right now. Please log more activities!"
        } catch (e: Exception) {
            Log.e(TAG, "Error in Gemini coaching call", e)
            "Could not connect to AI Coach: ${e.localizedMessage}. Please make sure you are online and have added a GEMINI_API_KEY."
        }
    }

    suspend fun generateChatResponse(
        history: List<ChatMessage>,
        roleInstruction: String,
        modelName: String,
        useThinking: Boolean
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "Please configure your GEMINI_API_KEY in the Secrets panel in AI Studio to unlock full Chatbot capabilities!"
        }

        val baseUrl = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent"

        try {
            val requestJson = JSONObject()
            
            // Build contents array for multi-turn history
            val contentsArray = JSONArray()
            history.forEach { msg ->
                val contentObj = JSONObject()
                contentObj.put("role", msg.role)
                val partsArray = JSONArray()
                val partObj = JSONObject()
                partObj.put("text", msg.text)
                partsArray.put(partObj)
                contentObj.put("parts", partsArray)
                contentsArray.put(contentObj)
            }
            requestJson.put("contents", contentsArray)

            // Include system instruction for specific roles
            if (roleInstruction.isNotEmpty()) {
                val systemObj = JSONObject()
                val partsArray = JSONArray()
                val partObj = JSONObject()
                partObj.put("text", roleInstruction)
                partsArray.put(partObj)
                systemObj.put("parts", partsArray)
                requestJson.put("systemInstruction", systemObj)
            }

            // Include generationConfig
            val generationConfigObj = JSONObject()
            if (modelName == "gemini-3.1-pro-preview" && useThinking) {
                val thinkingConfigObj = JSONObject()
                thinkingConfigObj.put("thinkingLevel", "HIGH")
                generationConfigObj.put("thinkingConfig", thinkingConfigObj)
                // DO NOT set maxOutputTokens for HIGH thinking level
            }
            if (generationConfigObj.length() > 0) {
                requestJson.put("generationConfig", generationConfigObj)
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = requestJson.toString().toRequestBody(mediaType)

            val url = "$baseUrl?key=$apiKey"
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                val errorMsg = response.body?.string() ?: "Unknown error"
                Log.e(TAG, "Gemini Chat API call failed: $errorMsg")
                return@withContext "Error: Gemini Chat API returned error code ${response.code}."
            }

            val responseBody = response.body?.string() ?: ""
            val responseJson = JSONObject(responseBody)
            val candidates = responseJson.getJSONArray("candidates")
            if (candidates.length() > 0) {
                val firstCandidate = candidates.getJSONObject(0)
                val content = firstCandidate.getJSONObject("content")
                val parts = content.getJSONArray("parts")
                if (parts.length() > 0) {
                    return@withContext parts.getJSONObject(0).getString("text")
                }
            }
            "Error: Received empty response from Gemini."
        } catch (e: Exception) {
            Log.e(TAG, "Error in Gemini chat API call", e)
            "Connection Error: ${e.localizedMessage}. Please make sure you are online and have added a GEMINI_API_KEY."
        }
    }
}

data class ChatMessage(
    val role: String, // "user" or "model"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)
