package com.example.util

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import android.util.Log
import java.util.Locale

/**
 * TtsManager yang dioptimalkan untuk pembelajaran bahasa Korea:
 * - Sanitasi teks Hangul agar pengucapan tidak tersangkut pada huruf alfabet Latin, simbol kurung, atau garis miring.
 * - Konfigurasi AudioAttributes (Speech/Media) untuk pemrosesan sinyal audio yang jernih tanpa distorsi.
 * - Pemilihan voice berkualitas tinggi (High Quality Korean Voice).
 * - Penyesuaian Pitch (1.05f) dan Speech Rate (0.92f) natural agar intonasi terdengar hangat, ramah, dan tidak kaku (robotik).
 */
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

            // 1. Atur bahasa ke Korean (ko-KR)
            val result = currentTts.setLanguage(Locale.KOREAN)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                // Secondary fallback
                val fallbackUs = currentTts.setLanguage(Locale.US)
                if (fallbackUs == TextToSpeech.LANG_MISSING_DATA || fallbackUs == TextToSpeech.LANG_NOT_SUPPORTED) {
                    currentTts.setLanguage(Locale.getDefault())
                }
                Log.w("TtsManager", "Korean language voice data not present; fallback configured.")
            }

            // 2. Konfigurasi AudioAttributes agar output jernih & terarah ke stream Media/Speech
            try {
                val audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
                currentTts.setAudioAttributes(audioAttributes)
            } catch (e: Exception) {
                Log.w("TtsManager", "Could not set AudioAttributes: ${e.message}")
            }

            // 3. Pilih voice Korea beresolusi tinggi (High Quality Natural Voice) jika tersedia
            try {
                val voices = currentTts.voices
                if (!voices.isNullOrEmpty()) {
                    val koVoices = voices.filter { v ->
                        v.locale.language.equals("ko", ignoreCase = true) ||
                        v.locale.toLanguageTag().startsWith("ko", ignoreCase = true)
                    }

                    if (koVoices.isNotEmpty()) {
                        // Cari voice terbaik: lokal tanpa latency tinggi, berkualitas tinggi
                        val bestVoice = koVoices.find { v ->
                            !v.isNetworkConnectionRequired &&
                            (v.quality == Voice.QUALITY_VERY_HIGH || v.quality == Voice.QUALITY_HIGH)
                        } ?: koVoices.find { v ->
                            v.name.contains("kof", ignoreCase = true) ||
                            v.name.contains("ism", ignoreCase = true) ||
                            v.name.contains("natural", ignoreCase = true)
                        } ?: koVoices.firstOrNull()

                        if (bestVoice != null) {
                            currentTts.voice = bestVoice
                            Log.d("TtsManager", "Selected high-clarity voice: ${bestVoice.name}")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w("TtsManager", "Voice optimization skipped: ${e.message}")
            }

            // 4. Intonasi bersahabat: Pitch 1.05f menghilangkan kesan robotik datar
            currentTts.setPitch(1.05f)

            // 5. Utterance listener untuk pelacakan selesai bicara
            currentTts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    Log.d("TtsManager", "TTS speech started: $utteranceId")
                }

                override fun onDone(utteranceId: String?) {
                    Log.d("TtsManager", "TTS speech finished: $utteranceId")
                }

                @Deprecated("Deprecated in Java", ReplaceWith("onError(utteranceId, -1)"))
                override fun onError(utteranceId: String?) {
                    Log.w("TtsManager", "TTS error on: $utteranceId")
                }

                override fun onError(utteranceId: String?, errorCode: Int) {
                    Log.w("TtsManager", "TTS error ($errorCode) on: $utteranceId")
                }
            })

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

    /**
     * Membersihkan teks masukan sebelum dibunyikan oleh TTS agar artikulasi Korea murni,
     * tidak mengeja romanisasi latin, tanda kurung, garis miring, atau simbol yang merusak artikulasi.
     */
    fun sanitizeKoreanForTts(raw: String): String {
        if (raw.isBlank()) return ""

        var cleaned = raw

        // Hapus teks latin dalam tanda kurung siku [romanization]
        cleaned = cleaned.replace(Regex("\\[[^\\]]*[a-zA-Z]+[^\\]]*\\]"), "")
        // Hapus teks latin dalam tanda kurung biasa (penjelasan bahasa Indonesia/Inggris)
        cleaned = cleaned.replace(Regex("\\([^\\)]*[a-zA-Z]+[^\\)]*\\)"), "")

        // Ganti garis miring "하나 / 한" menjadi jeda koma lembut "하나, 한" agar tidak dieja "빗금/슬래시"
        cleaned = cleaned.replace(Regex("\\s*/\\s*"), ", ")

        // Hapus simbol yang memicu pembacaan aneh pada TTS
        cleaned = cleaned.replace(Regex("[~#*^•_|\"\"'']"), " ")

        // Jika terdapat karakter Hangul, hilangkan kata-kata latin yang berdiri sendiri
        val hasHangul = cleaned.any { it in '\uAC00'..'\uD7A3' || it in '\u3131'..'\u318E' }
        if (hasHangul) {
            cleaned = cleaned.replace(Regex("\\b[a-zA-Z]+\\b"), " ")
        }

        // Rapikan spasi dan koma beruntun
        cleaned = cleaned.replace(Regex("\\s*,\\s*"), ", ")
        cleaned = cleaned.replace(Regex(",\\s*,"), ",")
        cleaned = cleaned.replace(Regex("\\s+"), " ").trim()

        return if (cleaned.isNotBlank()) cleaned else raw
    }

    fun speak(text: String, speed: Float = 0.92f) {
        if (text.isBlank()) return
        val cleanText = sanitizeKoreanForTts(text)
        if (cleanText.isBlank()) return

        if (!isInitialized || tts == null) {
            pendingSpeech = cleanText to speed
            return
        }

        try {
            // Speech rate yang stabil dan jelas untuk belajar (default 0.92f)
            val effectiveRate = speed.coerceIn(0.6f, 1.4f)
            tts?.setSpeechRate(effectiveRate)
            // Pitch 1.05f untuk intonasi yang cerah dan bersahabat
            tts?.setPitch(1.05f)

            val params = Bundle().apply {
                putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1.0f)
                putInt(TextToSpeech.Engine.KEY_PARAM_STREAM, AudioManager.STREAM_MUSIC)
            }

            val utteranceId = "KOREAN_IN_DULU_${System.currentTimeMillis()}"
            tts?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
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
