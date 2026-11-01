package com.example.util

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

data class SyllableScore(
    val syllable: String,
    val score: Int, // 0 to 100
    val isMatched: Boolean
)

data class SpeechAssessment(
    val recognizedText: String,
    val targetText: String,
    val accuracy: Int,
    val fluency: Int,
    val feedback: String,
    val syllableBreakdown: List<SyllableScore>
)

class SpeechRecognitionHelper(private val context: Context) {

    private var speechRecognizer: SpeechRecognizer? = null

    fun isAvailable(): Boolean = SpeechRecognizer.isRecognitionAvailable(context)

    fun startListening(
        languageCode: String = "ko-KR",
        onReady: () -> Unit = {},
        onRmsChanged: (Float) -> Unit = {},
        onResult: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        if (android.os.Looper.myLooper() != android.os.Looper.getMainLooper()) {
            android.os.Handler(android.os.Looper.getMainLooper()).post {
                startListening(languageCode, onReady, onRmsChanged, onResult, onError)
            }
            return
        }

        stopListening()

        if (!isAvailable()) {
            onError("Pengenal suara tidak tersedia pada perangkat/emulator ini. Anda dapat menggunakan tombol Simulasi Evaluasi untuk mencoba latihan berbicara.")
            return
        }

        try {
            val recognizer = SpeechRecognizer.createSpeechRecognizer(context.applicationContext)
            if (recognizer == null) {
                onError("Gagal menginisialisasi modul perekam suara. Silakan coba kembali.")
                return
            }
            speechRecognizer = recognizer

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageCode)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, languageCode)
                putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, languageCode)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            }

            speechRecognizer?.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    onReady()
                }

                override fun onBeginningOfSpeech() {}

                override fun onRmsChanged(rmsdB: Float) {
                    onRmsChanged(rmsdB)
                }

                override fun onBufferReceived(buffer: ByteArray?) {}

                override fun onEndOfSpeech() {}

                override fun onError(error: Int) {
                    val msg = when (error) {
                        SpeechRecognizer.ERROR_AUDIO -> "Gangguan perekaman audio"
                        SpeechRecognizer.ERROR_CLIENT -> "Klien pengenal suara tidak merespon"
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Izin mikrofon belum diberikan"
                        SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Koneksi jaringan diperlukan untuk speech recognizer"
                        SpeechRecognizer.ERROR_NO_MATCH -> "Suara tidak terdengar jelas, silakan coba lagi"
                        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Layanan pengenal suara sedang sibuk"
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Waktu bicara habis, belum terdengar suara"
                        else -> "Tidak dapat mengenali suara (Kode: $error)"
                    }
                    stopListening()
                    onError(msg)
                }

                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val spoken = matches?.firstOrNull() ?: ""
                    stopListening()
                    onResult(spoken)
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    matches?.firstOrNull()?.let {
                        // partial result update if needed
                    }
                }

                override fun onEvent(eventType: Int, params: Bundle?) {}
            })

            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            stopListening()
            onError("Gagal memulai microphone: ${e.localizedMessage}")
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.cancel()
            speechRecognizer?.destroy()
        } catch (_: Exception) {
        } finally {
            speechRecognizer = null
        }
    }

    companion object {
        fun evaluatePronunciation(targetKorean: String, spokenKorean: String): SpeechAssessment {
            val cleanTarget = targetKorean.replace(Regex("[.,?!~\\s]"), "")
            val cleanSpoken = spokenKorean.replace(Regex("[.,?!~\\s]"), "")

            if (cleanSpoken.isBlank()) {
                val breakdown = cleanTarget.map { SyllableScore(it.toString(), 0, false) }
                return SpeechAssessment(
                    recognizedText = "(Tidak terdeteksi)",
                    targetText = targetKorean,
                    accuracy = 45,
                    fluency = 50,
                    feedback = "Coba ucapkan lebih dekat ke mikrofon dengan intonasi jelas.",
                    syllableBreakdown = breakdown
                )
            }

            // Calculate similarity Levenshtein distance
            val dist = levenshteinDistance(cleanTarget, cleanSpoken)
            val maxLen = max(cleanTarget.length, cleanSpoken.length)
            val similarity = if (maxLen > 0) ((maxLen - dist).toFloat() / maxLen.toFloat()) else 0f
            val baseAccuracy = (similarity * 100).toInt().coerceIn(40, 100)

            // Syllable by syllable matching
            val breakdown = cleanTarget.mapIndexed { idx, char ->
                val targetCharStr = char.toString()
                val isExactMatch = cleanSpoken.contains(targetCharStr)
                val isPositionMatch = cleanSpoken.getOrNull(idx) == char
                val score = when {
                    isPositionMatch -> (94..100).random()
                    isExactMatch -> (82..90).random()
                    else -> (50..70).random()
                }
                SyllableScore(
                    syllable = targetCharStr,
                    score = score,
                    isMatched = isExactMatch || isPositionMatch
                )
            }

            val fluency = (baseAccuracy * 0.95f + (88..98).random() * 0.05f).toInt().coerceIn(50, 100)

            val feedback = when {
                baseAccuracy >= 90 -> "🎉 Sangat fasih! Pengucapan suku kata dan intonasi persis seperti penutur asli Korea."
                baseAccuracy >= 75 -> "👍 Bagus sekali! Sudah sangat jelas, latih lagi sedikit pada ritme dan akhiran kata."
                else -> "💪 Usaha yang bagus! Dengarkan tombol audio sekali lagi lalu tirukan perlahan suku kata per suku kata."
            }

            return SpeechAssessment(
                recognizedText = spokenKorean,
                targetText = targetKorean,
                accuracy = baseAccuracy,
                fluency = fluency,
                feedback = feedback,
                syllableBreakdown = breakdown
            )
        }

        private fun levenshteinDistance(lhs: CharSequence, rhs: CharSequence): Int {
            val len0 = lhs.length + 1
            val len1 = rhs.length + 1
            var cost = IntArray(len0) { it }
            var newcost = IntArray(len0)

            for (i in 1 until len1) {
                newcost[0] = i
                for (j in 1 until len0) {
                    val match = if (lhs[j - 1] == rhs[i - 1]) 0 else 1
                    val costReplace = cost[j - 1] + match
                    val costInsert = cost[j] + 1
                    val costDelete = newcost[j - 1] + 1
                    newcost[j] = min(min(costInsert, costDelete), costReplace)
                }
                val swap = cost
                cost = newcost
                newcost = swap
            }
            return cost[len0 - 1]
        }
    }
}
