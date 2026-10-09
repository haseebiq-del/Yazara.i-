package com.example.data.ai

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
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
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

class SchoolAiService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun generateSchoolImage(
        prompt: String,
        imageSize: String = "1K", // 1K, 2K, 4K
        aspectRatio: String = "1:1"
    ): Result<Bitmap> = withContext(Dispatchers.IO) {
        runCatching {
            val apiKey = BuildConfig.GEMINI_API_KEY
            if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
                error("Gemini API key is not configured. Please set GEMINI_API_KEY in the Secrets panel.")
            }

            // Using gemini-3-pro-image-preview as required
            val model = "gemini-3-pro-image-preview"
            val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"

            val requestJson = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", prompt)
                            })
                        })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("responseModalities", JSONArray().apply {
                        put("TEXT")
                        put("IMAGE")
                    })
                    put("imageConfig", JSONObject().apply {
                        put("imageSize", imageSize)
                        put("aspectRatio", aspectRatio)
                    })
                })
            }

            val body = requestJson.toString().toRequestBody(jsonMediaType)
            val request = Request.Builder().url(url).post(body).build()
            val response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                val errorBody = response.body?.string() ?: "Unknown error"
                Log.e("SchoolAiService", "Image generation failed: ${response.code} $errorBody")
                error("Failed to generate image (${response.code}): $errorBody")
            }

            val responseString = response.body?.string() ?: error("Empty response from AI")
            val root = JSONObject(responseString)
            val candidates = root.optJSONArray("candidates") ?: error("No candidates returned")
            val firstCandidate = candidates.getJSONObject(0)
            val content = firstCandidate.getJSONObject("content")
            val parts = content.getJSONArray("parts")

            var foundBitmap: Bitmap? = null
            for (i in 0 until parts.length()) {
                val part = parts.getJSONObject(i)
                val inlineData = part.optJSONObject("inlineData")
                if (inlineData != null) {
                    val base64Data = inlineData.optString("data")
                    if (base64Data.isNotEmpty()) {
                        val decodedBytes = Base64.decode(base64Data, Base64.DEFAULT)
                        foundBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                        break
                    }
                }
            }

            foundBitmap ?: error("No image data in AI response")
        }
    }

    suspend fun animateImageToVideo(
        prompt: String,
        aspectRatio: String = "16:9", // 16:9 or 9:16
        inputBitmap: Bitmap? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val apiKey = BuildConfig.GEMINI_API_KEY
            if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
                error("Gemini API key is not configured. Please set GEMINI_API_KEY in the Secrets panel.")
            }

            // Using veo-3.1-fast-generate-preview
            val model = "veo-3.1-fast-generate-preview"
            val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateVideos?key=$apiKey"

            val requestJson = JSONObject().apply {
                put("prompt", prompt.ifEmpty { "Animate this school illustration into motion with cinematic lighting" })
                put("config", JSONObject().apply {
                    put("numberOfVideos", 1)
                    put("resolution", "720p")
                    put("aspectRatio", if (aspectRatio == "9:16") "9:16" else "16:9")
                })
                if (inputBitmap != null) {
                    val stream = ByteArrayOutputStream()
                    inputBitmap.compress(Bitmap.CompressFormat.JPEG, 85, stream)
                    val base64 = Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)
                    put("image", JSONObject().apply {
                        put("imageBytes", base64)
                        put("mimeType", "image/jpeg")
                    })
                }
            }

            val body = requestJson.toString().toRequestBody(jsonMediaType)
            val request = Request.Builder().url(url).post(body).build()
            val response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                val errorBody = response.body?.string() ?: "Unknown error"
                Log.e("SchoolAiService", "Video generation failed: ${response.code} $errorBody")
                error("Video generation failed (${response.code}): $errorBody")
            }

            val responseString = response.body?.string() ?: error("Empty response from Veo")
            val root = JSONObject(responseString)
            val operationName = root.optString("name")
            "Video generation queued successfully (Operation: $operationName, Model: $model)"
        }
    }
}
