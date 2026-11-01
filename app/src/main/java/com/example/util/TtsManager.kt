package com.example.util

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

class TtsManager(context: Context) : TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private var pendingSpeech: Pair<String, Float>? = null

    init {
        try {
            tts = TextToSpeech(context.applicationContext, this)
        } catch (e: Exception) {
            Log.e("TtsManager", "Failed to initialize TextToSpeech instance", e)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val currentTts = tts ?: return
            val result = currentTts.setLanguage(Locale.KOREAN)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                // Secondary fallback
                val fallbackUs = currentTts.setLanguage(Locale.US)
                if (fallbackUs == TextToSpeech.LANG_MISSING_DATA || fallbackUs == TextToSpeech.LANG_NOT_SUPPORTED) {
                    currentTts.setLanguage(Locale.getDefault())
                }
                Log.w("TtsManager", "Korean language voice data not present; fallback configured.")
            }
            isInitialized = true
            pendingSpeech?.let { (text, speed) ->
                speak(text, speed)
                pendingSpeech = null
            }
        } else {
            isInitialized = false
            pendingSpeech = null
            Log.e("TtsManager", "Failed to initialize TTS engine. Status: $status")
        }
    }

    fun speak(text: String, speed: Float = 0.95f) {
        if (text.isBlank()) return
        if (!isInitialized || tts == null) {
            pendingSpeech = text to speed
            return
        }
        try {
            tts?.setSpeechRate(speed)
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "KOREAN_IN_DULU_TTS")
        } catch (e: Exception) {
            Log.e("TtsManager", "Error speaking text", e)
        }
    }

    fun stop() {
        pendingSpeech = null
        try {
            tts?.stop()
        } catch (e: Exception) {
            Log.e("TtsManager", "Error stopping TTS", e)
        }
    }

    fun shutdown() {
        pendingSpeech = null
        isInitialized = false
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (e: Exception) {
            Log.e("TtsManager", "Error shutting down TTS", e)
        } finally {
            tts = null
        }
    }
}
