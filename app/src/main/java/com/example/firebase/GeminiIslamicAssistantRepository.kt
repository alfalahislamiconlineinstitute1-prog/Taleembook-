package com.example.firebase

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Content
import com.google.firebase.ai.type.content
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeout

sealed interface AiStreamEvent {
    data object Loading : AiStreamEvent
    data class Streaming(val text: String) : AiStreamEvent
    data class Success(val text: String) : AiStreamEvent
    data class Error(val message: String) : AiStreamEvent
}

data class AiHistoryTurn(
    val sender: String,
    val text: String
)

class GeminiIslamicAssistantRepository {

    companion object {
        private const val TAG = "GeminiStudyAssistant"
        private const val MODEL_NAME = "gemini-3.8-flash"
        private const val TIMEOUT_MS = 60_000L
    }

    private val studyModel by lazy {
        Firebase.ai.generativeModel(
            modelName = MODEL_NAME,
            systemInstruction = content {
                text(
                    "You are the Al Falah Islamic Online Institute Study Assistant. " +
                        "Help students, teachers, and learners understand Noorani Qaida, Quran Tajweed, " +
                        "Hifz revision strategies, daily Duas & Salah, Seerah, Stories of the Prophets, " +
                        "Islamic Adab & Akhlaaq, and Daily Hadith explanations. " +
                        "Respond warmly, respectfully, clearly, and concisely in English (and include brief " +
                        "Arabic or Roman Urdu phrases when helpful). For complex Fiqh rulings, kindly remind " +
                        "the student to verify with a qualified scholar or institute Mufti."
                )
            }
        )
    }

    fun streamStudyReply(
        userPrompt: String,
        recentHistory: List<AiHistoryTurn> = emptyList()
    ): Flow<AiStreamEvent> = flow {
        emit(AiStreamEvent.Loading)
        val accumulated = StringBuilder()
        try {
            val chatHistory: List<Content> = recentHistory
                .takeLast(8)
                .map { turn ->
                    content(if (turn.sender == "assistant") "model" else "user") {
                        text(turn.text)
                    }
                }
            val chat = studyModel.startChat(history = chatHistory)
            withTimeout(TIMEOUT_MS) {
                chat.sendMessageStream(userPrompt).collect { chunk ->
                    val chunkText = chunk.text.orEmpty()
                    if (chunkText.isNotEmpty()) {
                        accumulated.append(chunkText)
                        emit(AiStreamEvent.Streaming(accumulated.toString()))
                    }
                }
            }
            val finalText = accumulated.toString().trim()
            if (finalText.isNotEmpty()) {
                emit(AiStreamEvent.Success(finalText))
            } else {
                emit(AiStreamEvent.Error("Gemini returned an empty response. Please try again."))
            }
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Gemini streaming timed out", e)
            emit(AiStreamEvent.Error("The request timed out after 60 seconds. Please try again."))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Firebase AI Logic generation failed", e)
            emit(
                AiStreamEvent.Error(
                    e.localizedMessage ?: "Unable to reach Gemini right now. Please try again."
                )
            )
        }
    }
}
