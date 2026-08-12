package com.example.data

import android.graphics.Bitmap
import android.util.Base64
import com.example.BuildConfig
import com.example.model.MathSolution
import com.example.model.SolutionStep
import com.example.util.LocalMathSolver
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

class GeminiMathService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private fun Bitmap.toBase64(): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        compress(Bitmap.CompressFormat.JPEG, 85, byteArrayOutputStream)
        return Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.NO_WRAP)
    }

    suspend fun solveMathQuestion(
        promptText: String,
        imageBitmap: Bitmap? = null,
        followUpContext: String? = null
    ): Result<MathSolution> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY

        // If API key is missing or default placeholder, fallback gracefully to LocalMathSolver
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            val localSol = LocalMathSolver.solve(promptText, imageBitmap)
            return@withContext Result.success(localSol)
        }

        // Try gemini-2.5-flash first, fallback to gemini-1.5-flash
        val modelsToTry = listOf("gemini-2.5-flash", "gemini-1.5-flash")

        val systemPrompt = """
            You are an expert Math Tutor and Solver.
            Solve the given math problem accurately with step-by-step explanations.
            
            Strictly respond in pure JSON format matching this schema:
            {
              "category": "Algebra / Calculus / Geometry / Trigonometry / Statistics / Arithmetic / Word Problem",
              "summary": "Short 1-sentence recap of the problem and approach",
              "finalAnswer": "The precise final answer with key units or simplified expression",
              "steps": [
                {
                  "stepNumber": 1,
                  "title": "Title of step (e.g. Identify Given Values / Set Up Equation / Integrate)",
                  "explanation": "Clear, encouraging explanation of why and how this step is performed.",
                  "mathExpression": "LaTeX or plain text equation for this step (e.g. 2x + 5 = 15)",
                  "keyFormula": "Relevant formula used, if applicable (e.g. Quadratic Formula: x = (-b ± √(b²-4ac))/(2a))"
                }
              ],
              "keyConcepts": ["Concept 1", "Concept 2"],
              "similarPracticeQuestions": ["Practice problem 1", "Practice problem 2"]
            }
            Do NOT enclose the output in markdown backticks like ```json. Return ONLY valid raw JSON text.
        """.trimIndent()

        for (modelName in modelsToTry) {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent?key=$apiKey"

            try {
                val partsArray = JSONArray()

                val fullPrompt = StringBuilder()
                if (followUpContext != null) {
                    fullPrompt.append("Previous Context: $followUpContext\n\n")
                }
                if (promptText.isNotBlank()) {
                    fullPrompt.append("Solve this question: $promptText")
                } else if (imageBitmap != null) {
                    fullPrompt.append("Analyze and solve the handwritten or printed math problem shown in this image.")
                } else {
                    fullPrompt.append("Solve the math problem.")
                }

                partsArray.put(JSONObject().apply {
                    put("text", fullPrompt.toString())
                })

                if (imageBitmap != null) {
                    val base64Data = imageBitmap.toBase64()
                    partsArray.put(JSONObject().apply {
                        put("inlineData", JSONObject().apply {
                            put("mimeType", "image/jpeg")
                            put("data", base64Data)
                        })
                    })
                }

                val contentsArray = JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", partsArray)
                    })
                }

                val requestJson = JSONObject().apply {
                    put("contents", contentsArray)
                    put("systemInstruction", JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply { put("text", systemPrompt) })
                        })
                    })
                    put("generationConfig", JSONObject().apply {
                        put("responseMimeType", "application/json")
                        put("temperature", 0.2)
                    })
                }

                val mediaType = "application/json; charset=utf-8".toMediaType()
                val requestBody = requestJson.toString().toRequestBody(mediaType)

                val request = Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build()

                val response = client.newCall(request).execute()
                val responseBodyString = response.body?.string() ?: ""

                if (response.isSuccessful) {
                    val parsedResponse = JSONObject(responseBodyString)
                    val candidates = parsedResponse.optJSONArray("candidates")
                    if (candidates != null && candidates.length() > 0) {
                        val candidateObj = candidates.getJSONObject(0)
                        val contentObj = candidateObj.optJSONObject("content")
                        val parts = contentObj?.optJSONArray("parts")
                        if (parts != null && parts.length() > 0) {
                            val rawText = parts.getJSONObject(0).optString("text", "").trim()

                            val cleanJsonStr = rawText
                                .replace("^```json".toRegex(RegexOption.MULTILINE), "")
                                .replace("^```".toRegex(RegexOption.MULTILINE), "")
                                .replace("```$".toRegex(RegexOption.MULTILINE), "")
                                .trim()

                            val jsonSolution = JSONObject(cleanJsonStr)

                            val category = jsonSolution.optString("category", "General Math")
                            val summary = jsonSolution.optString("summary", "Step-by-step solution")
                            val finalAnswer = jsonSolution.optString("finalAnswer", "No final answer extracted.")

                            val stepsList = mutableListOf<SolutionStep>()
                            val stepsArray = jsonSolution.optJSONArray("steps")
                            if (stepsArray != null) {
                                for (i in 0 until stepsArray.length()) {
                                    val stepObj = stepsArray.getJSONObject(i)
                                    stepsList.add(
                                        SolutionStep(
                                            stepNumber = stepObj.optInt("stepNumber", i + 1),
                                            title = stepObj.optString("title", "Step ${i + 1}"),
                                            explanation = stepObj.optString("explanation", ""),
                                            mathExpression = stepObj.optString("mathExpression", ""),
                                            keyFormula = stepObj.optString("keyFormula", "")
                                        )
                                    )
                                }
                            }

                            val keyConceptsList = mutableListOf<String>()
                            val keyConceptsArray = jsonSolution.optJSONArray("keyConcepts")
                            if (keyConceptsArray != null) {
                                for (i in 0 until keyConceptsArray.length()) {
                                    keyConceptsList.add(keyConceptsArray.getString(i))
                                }
                            }

                            val practiceList = mutableListOf<String>()
                            val practiceArray = jsonSolution.optJSONArray("similarPracticeQuestions")
                            if (practiceArray != null) {
                                for (i in 0 until practiceArray.length()) {
                                    practiceList.add(practiceArray.getString(i))
                                }
                            }

                            val solution = MathSolution(
                                questionText = if (promptText.isNotBlank()) promptText else "Handwritten / Image Problem",
                                category = category,
                                summary = summary,
                                finalAnswer = finalAnswer,
                                steps = stepsList,
                                keyConcepts = keyConceptsList,
                                similarPracticeQuestions = practiceList
                            )

                            return@withContext Result.success(solution)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // If Gemini remote requests failed or were blocked, use LocalMathSolver
        val localSol = LocalMathSolver.solve(promptText, imageBitmap)
        Result.success(localSol)
    }

    suspend fun chatWithAssistant(
        userPrompt: String,
        recentHistory: List<com.example.model.ChatMessage> = emptyList()
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY

        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
            val modelsToTry = listOf("gemini-2.5-flash", "gemini-1.5-flash")
            val systemPrompt = """
                You are MathLens AI, a friendly, highly intelligent AI Math Tutor and Study Assistant.
                Your job is to explain math concepts clearly, provide step-by-step guidance, help with homework questions, explain formulas, and break down complex equations into simple intuitive steps.
                Use clear text formatting, markdown, and formulas where appropriate.
                Keep explanations engaging, concise, and easy to follow.
            """.trimIndent()

            for (modelName in modelsToTry) {
                val url = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent?key=$apiKey"
                try {
                    val contentsArray = JSONArray()

                    // Add recent context
                    recentHistory.takeLast(6).forEach { msg ->
                        val role = if (msg.sender == com.example.model.ChatSender.USER) "user" else "model"
                        contentsArray.put(JSONObject().apply {
                            put("role", role)
                            put("parts", JSONArray().apply {
                                put(JSONObject().apply { put("text", msg.text) })
                            })
                        })
                    }

                    // Add current user prompt
                    contentsArray.put(JSONObject().apply {
                        put("role", "user")
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply { put("text", userPrompt) })
                        })
                    })

                    val requestJson = JSONObject().apply {
                        put("contents", contentsArray)
                        put("systemInstruction", JSONObject().apply {
                            put("parts", JSONArray().apply {
                                put(JSONObject().apply { put("text", systemPrompt) })
                            })
                        })
                        put("generationConfig", JSONObject().apply {
                            put("temperature", 0.3)
                        })
                    }

                    val mediaType = "application/json; charset=utf-8".toMediaType()
                    val requestBody = requestJson.toString().toRequestBody(mediaType)

                    val request = Request.Builder()
                        .url(url)
                        .post(requestBody)
                        .build()

                    val response = client.newCall(request).execute()
                    val responseBodyString = response.body?.string() ?: ""

                    if (response.isSuccessful) {
                        val parsedResponse = JSONObject(responseBodyString)
                        val candidates = parsedResponse.optJSONArray("candidates")
                        if (candidates != null && candidates.length() > 0) {
                            val candidateObj = candidates.getJSONObject(0)
                            val contentObj = candidateObj.optJSONObject("content")
                            val parts = contentObj?.optJSONArray("parts")
                            if (parts != null && parts.length() > 0) {
                                val replyText = parts.getJSONObject(0).optString("text", "").trim()
                                if (replyText.isNotBlank()) return@withContext replyText
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        // Local Assistant Fallback
        val solution = LocalMathSolver.solve(userPrompt)
        buildString {
            append("💡 **MathLens Assistant Guidance:**\n\n")
            append("${solution.summary}\n\n")
            append("🎯 **Final Answer / Result:**\n${solution.finalAnswer}\n\n")
            append("📝 **Step-by-Step Breakdown:**\n")
            solution.steps.forEach { step ->
                append("${step.stepNumber}. **${step.title}**: ${step.explanation}\n")
                if (step.mathExpression.isNotBlank()) {
                    append("   *Formula/Math:* `${step.mathExpression}`\n")
                }
            }
            if (solution.keyConcepts.isNotEmpty()) {
                append("\n🔑 **Key Concepts:** ${solution.keyConcepts.joinToString(", ")}")
            }
        }
    }
}


