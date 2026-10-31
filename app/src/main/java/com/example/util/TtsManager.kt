package com.example.util

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

class TtsManager(context: Context) : TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = TextToSpeech(context.applicationContext, this)
    private var isInitialized = false
    private var pendingSpeech: Pair<String, Float>? = null

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.KOREAN)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                // Fallback to default locale if Korean voice data not downloaded
                tts?.setLanguage(Locale.getDefault())
                Log.w("TtsManager", "Korean language voice data not present; fallback configured.")
            }
            isInitialized = true
            pendingSpeech?.let { (text, speed) ->
                speak(text, speed)
                pendingSpeech = null
            }
        } else {
            Log.e("TtsManager", "Failed to initialize TTS engine. Status: $status")
        }
    }

    fun speak(text: String, speed: Float = 0.95f) {
        if (!isInitialized) {
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
        try {
            tts?.stop()
        } catch (e: Exception) {
            Log.e("TtsManager", "Error stopping TTS", e)
        }
    }

    fun shutdown() {
        try {
            tts?.stop()
            tts?.shutdown()
            tts = null
            isInitialized = false
        } catch (e: Exception) {
            Log.e("TtsManager", "Error shutting down TTS", e)
        }
    }
}
