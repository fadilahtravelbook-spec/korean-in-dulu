package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.HaneulBlue
import com.example.ui.theme.HaneulContainer
import com.example.ui.theme.InkBlack
import com.example.ui.theme.NunbitYellow
import com.example.ui.theme.NunbitYellowContainer
import com.example.ui.theme.SaessakGreen
import com.example.ui.theme.SarangRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import kotlin.math.roundToInt

// Model untuk materi latihan mengetik keyboard
data class TypingExerciseItem(
    val targetText: String,
    val romanization: String,
    val meaningId: String,
    val note: String = ""
)

// Helper dekomposisi Hangul ke urutan tuts keyboard 2-beol-sik
object HangulKeyboardUtil {
    private val CHOSEONG = listOf(
        'ㄱ', 'ㄲ', 'ㄴ', 'ㄷ', 'ㄸ', 'ㄹ', 'ㅁ', 'ㅂ', 'ㅃ', 'ㅅ',
        'ㅆ', 'ㅇ', 'ㅈ', 'ㅉ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ'
    )
    private val JUNGSEONG = listOf(
        'ㅏ', 'ㅐ', 'ㅑ', 'ㅒ', 'ㅓ', 'ㅔ', 'ㅕ', 'ㅖ', 'ㅗ', 'ㅘ',
        'ㅙ', 'ㅚ', 'ㅛ', 'ㅜ', 'ㅝ', 'ㅞ', 'ㅟ', 'ㅠ', 'ㅡ', 'ㅢ', 'ㅣ'
    )
    private val JONGSEONG = listOf(
        "", "ㄱ", "ㄲ", "ㄳ", "ㄴ", "ㄵ", "ㄶ", "ㄷ", "ㄹ", "ㄺ",
        "ㄻ", "ㄼ", "ㄽ", "ㄾ", "ㄿ", "ㅀ", "ㅁ", "ㅂ", "ㅄ", "ㅅ",
        "ㅆ", "ㅇ", "ㅈ", "ㅊ", "ㅋ", "ㅌ", "ㅍ", "ㅎ"
    )

    // Dekomposisi vokal gabungan ke tombol tuts individual
    private fun expandJungseong(j: Char): List<Char> = when (j) {
        'ㅘ' -> listOf('ㅗ', 'ㅏ')
        'ㅙ' -> listOf('ㅗ', 'ㅐ')
        'ㅚ' -> listOf('ㅗ', 'ㅣ')
        'ㅝ' -> listOf('ㅜ', 'ㅓ')
        'ㅞ' -> listOf('ㅜ', 'ㅔ')
        'ㅟ' -> listOf('ㅜ', 'ㅣ')
        'ㅢ' -> listOf('ㅡ', 'ㅣ')
        else -> listOf(j)
    }

    // Dekomposisi batchim ganda ke tombol tuts individual
    private fun expandJongseong(j: String): List<Char> = when (j) {
        "" -> emptyList()
        "ㄳ" -> listOf('ㄱ', 'ㅅ')
        "ㄵ" -> listOf('ㄴ', 'ㅈ')
        "ㄶ" -> listOf('ㄴ', 'ㅎ')
        "ㄺ" -> listOf('ㄹ', 'ㄱ')
        "ㄻ" -> listOf('ㄹ', 'ㅁ')
        "ㄼ" -> listOf('ㄹ', 'ㅂ')
        "ㄽ" -> listOf('ㄹ', 'ㅅ')
        "ㄾ" -> listOf('ㄹ', 'ㅌ')
        "ㄿ" -> listOf('ㄹ', 'ㅍ')
        "ㅀ" -> listOf('ㄹ', 'ㅎ')
        "ㅄ" -> listOf('ㅂ', 'ㅅ')
        else -> j.toList()
    }

    fun decompose(text: String): List<Char> {
        val result = mutableListOf<Char>()
        for (ch in text) {
            val code = ch.code
            if (code in 0xAC00..0xD7A3) {
                val syllableIndex = code - 0xAC00
                val choIdx = syllableIndex / (21 * 28)
                val jungIdx = (syllableIndex % (21 * 28)) / 28
                val jongIdx = syllableIndex % 28

                result.add(CHOSEONG[choIdx])
                result.addAll(expandJungseong(JUNGSEONG[jungIdx]))
                result.addAll(expandJongseong(JONGSEONG[jongIdx]))
            } else {
                result.add(ch)
            }
        }
        return result
    }

    // Compound vowel combinations (e.g. ㅗ + ㅏ -> ㅘ)
    val COMPOUND_VOWELS = mapOf(
        ('ㅗ' to 'ㅏ') to 'ㅘ',
        ('ㅗ' to 'ㅐ') to 'ㅙ',
        ('ㅗ' to 'ㅣ') to 'ㅚ',
        ('ㅜ' to 'ㅓ') to 'ㅝ',
        ('ㅜ' to 'ㅔ') to 'ㅞ',
        ('ㅜ' to 'ㅣ') to 'ㅟ',
        ('ㅡ' to 'ㅣ') to 'ㅢ'
    )

    // Compound batchim combinations (e.g. ㄱ + ㅅ -> ㄳ)
    val COMPOUND_JONGSEONG = mapOf(
        ("ㄱ" to 'ㅅ') to "ㄳ",
        ("ㄴ" to 'ㅈ') to "ㄵ",
        ("ㄴ" to 'ㅎ') to "ㄶ",
        ("ㄹ" to 'ㄱ') to "ㄺ",
        ("ㄹ" to 'ㅁ') to "ㄻ",
        ("ㄹ" to 'ㅂ') to "ㄼ",
        ("ㄹ" to 'ㅅ') to "ㄽ",
        ("ㄹ" to 'ㅌ') to "ㄾ",
        ("ㄹ" to 'ㅍ') to "ㄿ",
        ("ㄹ" to 'ㅎ') to "ㅀ",
        ("ㅂ" to 'ㅅ') to "ㅄ"
    )

    // Split compound batchim if followed by vowel
    val SPLIT_COMPOUND_JONGSEONG = mapOf(
        "ㄳ" to ("ㄱ" to 'ㅅ'),
        "ㄵ" to ("ㄴ" to 'ㅈ'),
        "ㄶ" to ("ㄴ" to 'ㅎ'),
        "ㄺ" to ("ㄹ" to 'ㄱ'),
        "ㄻ" to ("ㄹ" to 'ㅁ'),
        "ㄼ" to ("ㄹ" to 'ㅂ'),
        "ㄽ" to ("ㄹ" to 'ㅅ'),
        "ㄾ" to ("ㄹ" to 'ㅌ'),
        "ㄿ" to ("ㄹ" to 'ㅍ'),
        "ㅀ" to ("ㄹ" to 'ㅎ'),
        "ㅄ" to ("ㅂ" to 'ㅅ')
    )

    fun compose(jamoList: List<Char>): String {
        if (jamoList.isEmpty()) return ""

        val sb = java.lang.StringBuilder()
        var choseong: Char? = null
        var jungseong: Char? = null
        var jongseong: String = ""

        fun flush() {
            if (choseong != null && jungseong != null) {
                val choIdx = CHOSEONG.indexOf(choseong!!)
                val jungIdx = JUNGSEONG.indexOf(jungseong!!)
                val jongIdx = JONGSEONG.indexOf(jongseong)
                if (choIdx != -1 && jungIdx != -1 && jongIdx != -1) {
                    val code = 0xAC00 + (choIdx * 21 * 28) + (jungIdx * 28) + jongIdx
                    sb.append(code.toChar())
                } else {
                    sb.append(choseong)
                    sb.append(jungseong)
                    if (jongseong.isNotEmpty()) sb.append(jongseong)
                }
            } else if (choseong != null) {
                sb.append(choseong)
            } else if (jungseong != null) {
                sb.append(jungseong)
            }
            choseong = null
            jungseong = null
            jongseong = ""
        }

        for (ch in jamoList) {
            val isConsonant = isLeftHandConsonant(ch)
            val isVowel = JUNGSEONG.contains(ch)

            // Karakter spasi, tanda baca (. , ! ? ~ dll) atau non-Hangul langsung di-append
            if (!isConsonant && !isVowel) {
                flush()
                sb.append(ch)
                continue
            }

            if (isConsonant) {
                if (choseong == null) {
                    choseong = ch
                } else if (jungseong == null) {
                    sb.append(choseong)
                    choseong = ch
                } else if (jongseong.isEmpty()) {
                    if (JONGSEONG.contains(ch.toString())) {
                        jongseong = ch.toString()
                    } else {
                        flush()
                        choseong = ch
                    }
                } else {
                    val compoundKey = jongseong to ch
                    val compound = COMPOUND_JONGSEONG[compoundKey]
                    if (compound != null) {
                        jongseong = compound
                    } else {
                        flush()
                        choseong = ch
                    }
                }
            } else {
                // Vowel
                if (choseong == null) {
                    if (jungseong == null) {
                        jungseong = ch
                    } else {
                        val compoundVowel = COMPOUND_VOWELS[jungseong!! to ch]
                        if (compoundVowel != null) {
                            jungseong = compoundVowel
                        } else {
                            flush()
                            jungseong = ch
                        }
                    }
                } else if (jungseong == null) {
                    jungseong = ch
                } else if (jongseong.isEmpty()) {
                    val compoundVowel = COMPOUND_VOWELS[jungseong!! to ch]
                    if (compoundVowel != null) {
                        jungseong = compoundVowel
                    } else {
                        flush()
                        jungseong = ch
                    }
                } else {
                    val split = SPLIT_COMPOUND_JONGSEONG[jongseong]
                    if (split != null) {
                        jongseong = split.first
                        flush()
                        choseong = split.second
                        jungseong = ch
                    } else {
                        val singleFinal = jongseong.firstOrNull()
                        jongseong = ""
                        flush()
                        choseong = singleFinal
                        jungseong = ch
                    }
                }
            }
        }

        flush()
        return sb.toString()
    }

    // Cek apakah tuts berada di tangan kiri (Konsonan) atau tangan kanan (Vokal)
    fun isLeftHandConsonant(c: Char): Boolean {
        return c in listOf('ㄱ', 'ㄲ', 'ㄴ', 'ㄷ', 'ㄸ', 'ㄹ', 'ㅁ', 'ㅂ', 'ㅃ', 'ㅅ', 'ㅆ', 'ㅇ', 'ㅈ', 'ㅉ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ')
    }

    // Mapping tombol fisik QWERTY untuk tuts Korea
    fun getQwertyKeyHint(c: Char): String = when (c) {
        'ㅂ' -> "Q"
        'ㅃ' -> "Shift+Q"
        'ㅈ' -> "W"
        'ㅉ' -> "Shift+W"
        'ㄷ' -> "E"
        'ㄸ' -> "Shift+E"
        'ㄱ' -> "R"
        'ㄲ' -> "Shift+R"
        'ㅅ' -> "T"
        'ㅆ' -> "Shift+T"
        'ㅛ' -> "Y"
        'ㅕ' -> "U"
        'ㅑ' -> "I"
        'ㅐ' -> "O"
        'ㅒ' -> "Shift+O"
        'ㅔ' -> "P"
        'ㅖ' -> "Shift+P"
        'ㅁ' -> "A"
        'ㄴ' -> "S"
        'ㅇ' -> "D"
        'ㄹ' -> "F"
        'ㅎ' -> "G"
        'ㅗ' -> "H"
        'ㅓ' -> "J"
        'ㅏ' -> "K"
        'ㅣ' -> "L"
        'ㅋ' -> "Z"
        'ㅌ' -> "X"
        'ㅊ' -> "C"
        'ㅍ' -> "V"
        'ㅠ' -> "B"
        'ㅜ' -> "N"
        'ㅡ' -> "M"
        ' ' -> "Space"
        '.' -> "."
        ',' -> ","
        '!' -> "!"
        '?' -> "?"
        '~' -> "~"
        '-' -> "-"
        in '0'..'9' -> c.toString()
        else -> c.toString()
    }

    // Mapping tombol QWERTY fisik ke tuts 2-Beol-Sik Korea
    fun mapQwertyToHangul(keyChar: Char, isShift: Boolean): Char? {
        val upper = keyChar.uppercaseChar()
        return when (upper) {
            'Q' -> if (isShift) 'ㅃ' else 'ㅂ'
            'W' -> if (isShift) 'ㅉ' else 'ㅈ'
            'E' -> if (isShift) 'ㄸ' else 'ㄷ'
            'R' -> if (isShift) 'ㄲ' else 'ㄱ'
            'T' -> if (isShift) 'ㅆ' else 'ㅅ'
            'Y' -> 'ㅛ'
            'U' -> 'ㅕ'
            'I' -> 'ㅑ'
            'O' -> if (isShift) 'ㅒ' else 'ㅐ'
            'P' -> if (isShift) 'ㅖ' else 'ㅔ'
            'A' -> 'ㅁ'
            'S' -> 'ㄴ'
            'D' -> 'ㅇ'
            'F' -> 'ㄹ'
            'G' -> 'ㅎ'
            'H' -> 'ㅗ'
            'J' -> 'ㅓ'
            'K' -> 'ㅏ'
            'L' -> 'ㅣ'
            'Z' -> 'ㅋ'
            'X' -> 'ㅌ'
            'C' -> 'ㅊ'
            'V' -> 'ㅍ'
            'B' -> 'ㅠ'
            'N' -> 'ㅜ'
            'M' -> 'ㅡ'
            ' ' -> ' '
            '.' -> '.'
            ',' -> ','
            '!' -> '!'
            '?' -> '?'
            '~' -> '~'
            '-' -> '-'
            in '0'..'9' -> keyChar
            else -> null
        }
    }
}

@Composable
fun KoreanKeyboardPracticeView(
    ttsSpeed: Float = 0.9f,
    onSpeak: (String, Float) -> Unit,
    onAddXp: (Int) -> Unit,
    onRecordProgress: (moduleId: String, lessonId: String, title: String, score: Int) -> Unit
) {
    // Kategori Latihan Ketik
    val categories = listOf(
        "🖐️ Vokal Kanan",
        "🖐️ Konsonan Kiri",
        "⇧ Shift & Ganda",
        "🧩 Suku Kata",
        "📚 Kata Esensial",
        "⚡ Kalimat Kilat",
        "⌨️ Mode Bebas"
    )
    var selectedCategoryIndex by remember { mutableIntStateOf(0) }

    // Bank Soal Latihan Ketik
    val exercises = remember(selectedCategoryIndex) {
        when (selectedCategoryIndex) {
            0 -> listOf(
                TypingExerciseItem("ㅏ", "a", "Vokal Dasar (Tombol K - Jari Tengah Kanan)", "Bentuk vertikal tegak dengan garis pendek ke kanan."),
                TypingExerciseItem("ㅓ", "eo", "Vokal Dasar (Tombol J - Telunjuk Kanan)", "Garis vertikal tegak dengan garis pendek ke kiri."),
                TypingExerciseItem("ㅗ", "o", "Vokal Dasar (Tombol H - Telunjuk Kanan)", "Garis horizontal datar dengan garis pendek ke atas."),
                TypingExerciseItem("ㅜ", "u", "Vokal Dasar (Tombol N - Telunjuk Kanan)", "Garis horizontal datar dengan garis pendek ke bawah."),
                TypingExerciseItem("ㅡ", "eu", "Vokal Dasar (Tombol M - Telunjuk/Tengah Kanan)", "Vokal datar senyum rileks."),
                TypingExerciseItem("ㅣ", "i", "Vokal Dasar (Tombol L - Jari Manis Kanan)", "Garis vertikal tegak."),
                TypingExerciseItem("ㅐ", "ae", "Vokal Dasar (Tombol O - Jari Manis Kanan Atas)", "Kombinasi ㅏ + ㅣ."),
                TypingExerciseItem("ㅔ", "e", "Vokal Dasar (Tombol P - Kelingking Kanan Atas)", "Kombinasi ㅓ + ㅣ."),
                TypingExerciseItem("ㅑ", "ya", "Vokal Iotated (Tombol I - Jari Tengah Kanan Atas)", "Dua garis ke kanan menghasilkan bunyi Y-."),
                TypingExerciseItem("ㅕ", "yeo", "Vokal Iotated (Tombol U - Telunjuk Kanan Atas)", "Dua garis ke kiri menghasilkan bunyi Y-."),
                TypingExerciseItem("ㅛ", "yo", "Vokal Iotated (Tombol Y - Telunjuk Kanan Atas)", "Dua garis ke atas menghasilkan bunyi Y-."),
                TypingExerciseItem("ㅠ", "yu", "Vokal Iotated (Tombol B - Telunjuk Kanan Bawah)", "Dua garis ke bawah menghasilkan bunyi Y-.")
            )
            1 -> listOf(
                TypingExerciseItem("ㄱ", "g/k", "Konsonan Dasar (Tombol R - Telunjuk Kiri Atas)", "G-konsonan pangkal lidah."),
                TypingExerciseItem("ㄴ", "n", "Konsonan Dasar (Tombol S - Jari Manis Kiri Tengah)", "N-ujung lidah menempel langit-langit."),
                TypingExerciseItem("ㄷ", "d/t", "Konsonan Dasar (Tombol E - Jari Tengah Kiri Atas)", "D-ujung lidah menyentuh gusi."),
                TypingExerciseItem("ㄹ", "r/l", "Konsonan Dasar (Tombol F - Telunjuk Kiri Tengah)", "R/L lentur."),
                TypingExerciseItem("ㅁ", "m", "Konsonan Dasar (Tombol A - Kelingking Kiri Tengah)", "Kotak tertutup rapat (M)."),
                TypingExerciseItem("ㅂ", "b/p", "Konsonan Dasar (Tombol Q - Kelingking Kiri Atas)", "Ember terbuka (B/P)."),
                TypingExerciseItem("ㅅ", "s", "Konsonan Dasar (Tombol T - Telunjuk Kiri Atas)", "Garis atap rumah (S)."),
                TypingExerciseItem("ㅇ", "ng / senyap", "Konsonan Dasar (Tombol D - Jari Tengah Kiri Tengah)", "Lingkaran lembut."),
                TypingExerciseItem("ㅈ", "j", "Konsonan Dasar (Tombol W - Jari Manis Kiri Atas)", "Atap bergaris mendatar (J)."),
                TypingExerciseItem("ㅊ", "ch", "Konsonan Aspirasi (Tombol C - Jari Tengah Kiri Bawah)", "J dengan topi napas (CH)."),
                TypingExerciseItem("ㅋ", "k (aspirasi)", "Konsonan Aspirasi (Tombol Z - Kelingking Kiri Bawah)", "ㄱ dengan garis napas tengah."),
                TypingExerciseItem("ㅌ", "t (aspirasi)", "Konsonan Aspirasi (Tombol X - Jari Manis Kiri Bawah)", "ㄷ dengan garis napas tengah."),
                TypingExerciseItem("ㅍ", "p (aspirasi)", "Konsonan Aspirasi (Tombol V - Telunjuk Kiri Bawah)", "Pintu ganda (P kuat)."),
                TypingExerciseItem("ㅎ", "h", "Konsonan Dasar (Tombol G - Telunjuk Kiri Tengah)", "Topi dan wajah (H).")
            )
            2 -> listOf(
                TypingExerciseItem("ㄲ", "kk", "Konsonan Ganda (Shift + R)", "Tahan Shift lalu tekan R."),
                TypingExerciseItem("ㄸ", "tt", "Konsonan Ganda (Shift + E)", "Tahan Shift lalu tekan E."),
                TypingExerciseItem("ㅃ", "pp", "Konsonan Ganda (Shift + Q)", "Tahan Shift lalu tekan Q."),
                TypingExerciseItem("ㅆ", "ss", "Konsonan Ganda (Shift + T)", "Tahan Shift lalu tekan T."),
                TypingExerciseItem("ㅉ", "jj", "Konsonan Ganda (Shift + W)", "Tahan Shift lalu tekan W."),
                TypingExerciseItem("ㅒ", "yae", "Vokal Ganda (Shift + O)", "Tahan Shift lalu tekan O."),
                TypingExerciseItem("ㅖ", "ye", "Vokal Ganda (Shift + P)", "Tahan Shift lalu tekan P.")
            )
            3 -> listOf(
                TypingExerciseItem("가", "ga", "ㄱ (kiri) + ㅏ (kanan)", "Ritme ketik: Kiri lalu Kanan."),
                TypingExerciseItem("나", "na", "ㄴ (kiri) + ㅏ (kanan)", "Konsonan S + Vokal K."),
                TypingExerciseItem("다", "da", "ㄷ (kiri) + ㅏ (kanan)", "Konsonan E + Vokal K."),
                TypingExerciseItem("라", "ra", "ㄹ (kiri) + ㅏ (kanan)", "Konsonan F + Vokal K."),
                TypingExerciseItem("마", "ma", "ㅁ (kiri) + ㅏ (kanan)", "Konsonan A + Vokal K."),
                TypingExerciseItem("바", "ba", "ㅂ (kiri) + ㅏ (kanan)", "Konsonan Q + Vokal K."),
                TypingExerciseItem("사", "sa", "ㅅ (kiri) + ㅏ (kanan)", "Konsonan T + Vokal K."),
                TypingExerciseItem("아", "a", "ㅇ (kiri) + ㅏ (kanan)", "Konsonan D + Vokal K."),
                TypingExerciseItem("자", "ja", "ㅈ (kiri) + ㅏ (kanan)", "Konsonan W + Vokal K."),
                TypingExerciseItem("차", "cha", "ㅊ (kiri) + ㅏ (kanan)", "Konsonan C + Vokal K."),
                TypingExerciseItem("카", "ka", "ㅋ (kiri) + ㅏ (kanan)", "Konsonan Z + Vokal K."),
                TypingExerciseItem("타", "ta", "ㅌ (kiri) + ㅏ (kanan)", "Konsonan X + Vokal K."),
                TypingExerciseItem("파", "pa", "ㅍ (kiri) + ㅏ (kanan)", "Konsonan V + Vokal K."),
                TypingExerciseItem("하", "ha", "ㅎ (kiri) + ㅏ (kanan)", "Konsonan G + Vokal K."),
                TypingExerciseItem("고", "go", "ㄱ (kiri) + ㅗ (kanan)", "Konsonan R + Vokal H."),
                TypingExerciseItem("노", "no", "ㄴ (kiri) + ㅗ (kanan)", "Konsonan S + Vokal H."),
                TypingExerciseItem("도", "do", "ㄷ (kiri) + ㅗ (kanan)", "Konsonan E + Vokal H."),
                TypingExerciseItem("로", "ro", "ㄹ (kiri) + ㅗ (kanan)", "Konsonan F + Vokal H."),
                TypingExerciseItem("모", "mo", "ㅁ (kiri) + ㅗ (kanan)", "Konsonan A + Vokal H."),
                TypingExerciseItem("보", "bo", "ㅂ (kiri) + ㅗ (kanan)", "Konsonan Q + Vokal H."),
                TypingExerciseItem("우", "u", "ㅇ (kiri) + ㅜ (kanan)", "Konsonan D + Vokal N.")
            )
            4 -> listOf(
                TypingExerciseItem("한국", "han-guk", "Korea", "Urutan tuts: ㅎ(G) + ㅏ(K) + ㄴ(S) + ㄱ(R) + ㅜ(N) + ㄱ(R)"),
                TypingExerciseItem("사랑", "sa-rang", "Cinta / Sayang", "Urutan tuts: ㅅ(T) + ㅏ(K) + ㄹ(F) + ㅏ(K) + ㅇ(D)"),
                TypingExerciseItem("친구", "chin-gu", "Teman / Sahabat", "Urutan tuts: ㅊ(C) + ㅣ(L) + ㄴ(S) + ㄱ(R) + ㅜ(N)"),
                TypingExerciseItem("학교", "hak-gyo", "Sekolah", "Urutan tuts: ㅎ(G) + ㅏ(K) + ㄱ(R) + ㄱ(R) + ㅛ(Y)"),
                TypingExerciseItem("김치", "gim-chi", "Kimchi", "Makanan fermentasi sawi putih khas Korea."),
                TypingExerciseItem("물", "mul", "Air", "Konsonan ㅁ(A) + Vokal ㅜ(N) + Batchim ㄹ(F)"),
                TypingExerciseItem("밥", "bap", "Nasi / Makanan", "Konsonan ㅂ(Q) + Vokal ㅏ(K) + Batchim ㅂ(Q)"),
                TypingExerciseItem("책", "chaek", "Buku", "Konsonan ㅊ(C) + Vokal ㅐ(O) + Batchim ㄱ(R)"),
                TypingExerciseItem("서울", "seo-ul", "Seoul (Ibu kota Korea)", "Urutan tuts: ㅅ(T) + ㅓ(J) + ㅇ(D) + ㅜ(N) + ㄹ(F)"),
                TypingExerciseItem("사람", "sa-ram", "Orang / Manusia", "Urutan tuts: ㅅ(T) + ㅏ(K) + ㄹ(F) + ㅏ(K) + ㅁ(A)"),
                TypingExerciseItem("커피", "keo-pi", "Kopi", "Urutan tuts: ㅋ(Z) + ㅓ(J) + ㅍ(V) + ㅣ(L)"),
                TypingExerciseItem("오늘", "o-neul", "Hari Ini", "Urutan tuts: ㅇ(D) + ㅗ(H) + ㄴ(S) + ㅡ(M) + ㄹ(F)"),
                TypingExerciseItem("시간", "si-gan", "Waktu / Jam", "Urutan tuts: ㅅ(T) + ㅣ(L) + ㄱ(R) + ㅏ(K) + ㄴ(S)"),
                TypingExerciseItem("이름", "i-reum", "Nama", "Urutan tuts: ㅇ(D) + ㅣ(L) + ㄹ(F) + ㅡ(M) + ㅁ(A)"),
                TypingExerciseItem("나라", "na-ra", "Negara", "Urutan tuts: ㄴ(S) + ㅏ(K) + ㄹ(F) + ㅏ(K)")
            )
            5 -> listOf(
                TypingExerciseItem("안녕하세요", "an-nyeong-ha-se-yo", "Halo / Salam sejahtera", "Salam perjumpaan paling umum dan sopan."),
                TypingExerciseItem("감사합니다", "gam-sa-ham-ni-da", "Terima kasih (Formal)", "Ungkapan terima kasih penuh rasa hormat."),
                TypingExerciseItem("반갑습니다", "ban-gap-seum-ni-da", "Senang bertemu Anda", "Diucapkan saat pertama kali berkenalan."),
                TypingExerciseItem("죄송합니다", "joe-song-ham-ni-da", "Mohon maaf", "Permohonan maaf formal sopan."),
                TypingExerciseItem("맛있어요", "mas-iss-eo-yo", "Enak / Lezat", "Pujian makanan lezat di restoran."),
                TypingExerciseItem("좋아요", "jo-a-yo", "Bagus / Saya suka", "Ungkapan persetujuan atau rasa senang."),
                TypingExerciseItem("잘 가요", "jal ga-yo", "Selamat jalan", "Dilepaskan untuk orang yang pergi."),
                TypingExerciseItem("사랑해요", "sa-rang-hae-yo", "Aku mencintaimu", "Ungkapan kasih sayang tulus."),
                TypingExerciseItem("행복하세요", "haeng-bok-ha-se-yo", "Semoga selalu bahagia", "Doa berkat untuk sahabat dan keluarga.")
            )
            else -> emptyList()
        }
    }

    var currentItemIndex by remember(selectedCategoryIndex) { mutableIntStateOf(0) }
    val currentExercise = exercises.getOrElse(currentItemIndex) { exercises.firstOrNull() }

    // Target sequence of characters to type
    val targetKeystrokes = remember(currentExercise) {
        if (currentExercise != null) {
            HangulKeyboardUtil.decompose(currentExercise.targetText)
        } else emptyList()
    }

    // State pengetikan
    var currentKeystrokeIndex by remember(currentExercise) { mutableIntStateOf(0) }
    val typedBuffer = remember(currentExercise) { mutableStateListOf<Char>() }
    var isShiftActive by remember { mutableStateOf(false) }
    var streakCount by remember { mutableIntStateOf(0) }
    var totalStrokes by remember { mutableIntStateOf(0) }
    var correctStrokes by remember { mutableIntStateOf(0) }
    var typingStartTime by remember { mutableLongStateOf(0L) }
    var showCompletedBanner by remember { mutableStateOf(false) }

    // Free Typing State (Mode Bebas)
    val freeTypedJamos = remember { mutableStateListOf<Char>() }
    var freeTypedText by remember { mutableStateOf("") }

    // Target karakter berikutnya yang harus ditekan
    val nextExpectedChar = if (currentExercise != null && currentKeystrokeIndex < targetKeystrokes.size) {
        targetKeystrokes[currentKeystrokeIndex]
    } else null

    // Handler penekanan tuts keyboard
    fun onKeyPress(ch: Char) {
        if (selectedCategoryIndex == 6) {
            // Mode Bebas: kumpulkan jamo lalu susun otomatis menjadi suku kata Korea asli
            freeTypedJamos.add(ch)
            freeTypedText = HangulKeyboardUtil.compose(freeTypedJamos.toList())
            if (isShiftActive) isShiftActive = false
            return
        }

        if (currentExercise == null) return

        if (typingStartTime == 0L) {
            typingStartTime = System.currentTimeMillis()
        }

        totalStrokes++

        if (nextExpectedChar != null && ch == nextExpectedChar) {
            // Benar
            correctStrokes++
            streakCount++
            typedBuffer.add(ch)
            val nextIdx = currentKeystrokeIndex + 1
            currentKeystrokeIndex = nextIdx

            // Reset shift setelah sekali pakai jika aktif
            if (isShiftActive) isShiftActive = false

            // Cek apakah selesai
            if (nextIdx >= targetKeystrokes.size) {
                showCompletedBanner = true
                onAddXp(15)
                onSpeak(currentExercise.targetText, ttsSpeed)
                onRecordProgress(
                    "keyboard_typing",
                    "drill_${selectedCategoryIndex}_$currentItemIndex",
                    "Ketik Keyboard: ${currentExercise.targetText}",
                    100
                )
            }
        } else {
            // Salah ketik
            streakCount = 0
        }
    }

    fun onBackspace() {
        if (selectedCategoryIndex == 6) {
            if (freeTypedJamos.isNotEmpty()) {
                freeTypedJamos.removeAt(freeTypedJamos.lastIndex)
                freeTypedText = HangulKeyboardUtil.compose(freeTypedJamos.toList())
            }
            return
        }

        if (typedBuffer.isNotEmpty()) {
            typedBuffer.removeAt(typedBuffer.size - 1)
            currentKeystrokeIndex = maxOf(0, currentKeystrokeIndex - 1)
            showCompletedBanner = false
        }
    }

    fun onResetDrill() {
        typedBuffer.clear()
        currentKeystrokeIndex = 0
        showCompletedBanner = false
    }

    fun onNextExercise() {
        if (exercises.isNotEmpty()) {
            currentItemIndex = (currentItemIndex + 1) % exercises.size
            typedBuffer.clear()
            currentKeystrokeIndex = 0
            showCompletedBanner = false
        }
    }

    // Hitung akurasi
    val accuracy = if (totalStrokes > 0) {
        ((correctStrokes.toFloat() / totalStrokes.toFloat()) * 100).roundToInt()
    } else 100

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp)
    ) {
        // Tab Navigasi Kategori Pengetikan
        ScrollableTabRow(
            selectedTabIndex = selectedCategoryIndex,
            containerColor = Color.White,
            contentColor = HaneulBlue,
            edgePadding = 12.dp,
            divider = {}
        ) {
            categories.forEachIndexed { idx, label ->
                Tab(
                    selected = selectedCategoryIndex == idx,
                    onClick = {
                        selectedCategoryIndex = idx
                        currentItemIndex = 0
                        typedBuffer.clear()
                        currentKeystrokeIndex = 0
                        showCompletedBanner = false
                    },
                    text = {
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            fontWeight = if (selectedCategoryIndex == idx) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Banner Penjelasan Tata Letak 2-Beol-Sik
        Surface(
            color = Color(0xFFEFF6FF),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp),
            shape = RoundedCornerShape(12.dp),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.horizontalGradient(listOf(Color(0xFFBFDBFE), Color(0xFF93C5FD)))
            )
        ) {
            Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Keyboard, contentDescription = null, tint = HaneulBlue, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Prinsip Keyboard Korea (두벌식 2-Beol-Sik)",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.sp,
                        color = InkBlack
                    )
                    Text(
                        text = "🖐️ Tangan Kiri = Konsonan (자음) • 🖐️ Tangan Kanan = Vokal (모음). Huruf tersusun otomatis menjadi suku kata!",
                        fontSize = 10.sp,
                        color = Color(0xFF1E3A8A),
                        lineHeight = 14.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Bar Statistik Pengetikan (Akurasi & Streak)
        Surface(
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp),
            shape = RoundedCornerShape(12.dp),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.horizontalGradient(listOf(Color(0xFFE2E8F0), Color(0xFFCBD5E1)))
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Speed, contentDescription = null, tint = HaneulBlue, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Akurasi: ", fontSize = 11.sp, color = TextSecondary)
                    Text("$accuracy%", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = if (accuracy >= 90) SaessakGreen else HaneulBlue)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = NunbitYellow, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Streak Tuts: ", fontSize = 11.sp, color = TextSecondary)
                    Text("$streakCount", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFB45309))
                }

                if (currentExercise != null) {
                    Text(
                        text = "${currentItemIndex + 1}/${exercises.size}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Konten Latihan Ketik / Mode Bebas
        if (selectedCategoryIndex == 6) {
            // Mode Bebas
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.horizontalGradient(listOf(Color(0xFFE2E8F0), Color(0xFFCBD5E1)))
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Papan Ketik Bebas (자유 타자 연습)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = InkBlack
                    )
                    Text(
                        text = "Tekan tombol tuts keyboard Hangul di bawah ini untuk merangkai kata apa pun, lalu dengarkan suaranya!",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF8FAFC),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.horizontalGradient(listOf(Color(0xFFCBD5E1), Color(0xFF94A3B8)))
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (freeTypedText.isEmpty()) {
                                Text(
                                    text = "Ketik sesuatu menggunakan keyboard di bawah...",
                                    color = TextMuted,
                                    fontSize = 14.sp
                                )
                            } else {
                                Text(
                                    text = freeTypedText,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = InkBlack
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                if (freeTypedText.isNotEmpty()) {
                                    onSpeak(freeTypedText, ttsSpeed)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = HaneulBlue),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Dengarkan Lafal", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                freeTypedJamos.clear()
                                freeTypedText = ""
                            },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Bersihkan", fontSize = 12.sp, color = TextSecondary)
                        }
                    }
                }
            }
        } else if (currentExercise != null) {
            // Target Card Latihan
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header Target & Tombol TTS
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = HaneulContainer
                        ) {
                            Text(
                                text = "Target Kata #${currentItemIndex + 1}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = HaneulBlue,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        IconButton(
                            onClick = { onSpeak(currentExercise.targetText, ttsSpeed) },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Dengar Target",
                                tint = HaneulBlue
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Target Hangul Besar
                    Text(
                        text = currentExercise.targetText,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Black,
                        color = InkBlack,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "[${currentExercise.romanization}]",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = HaneulBlue
                    )

                    Text(
                        text = currentExercise.meaningId,
                        fontSize = 12.sp,
                        color = Color(0xFF475569),
                        textAlign = TextAlign.Center
                    )

                    if (currentExercise.note.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = currentExercise.note,
                            fontSize = 11.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Visual Roadmap Keystroke (Urutan tombol yang harus ditekan)
                    Text(
                        text = "Urutan Tombol Tuts:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = InkBlack,
                        modifier = Modifier.align(Alignment.Start)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        itemsIndexed(targetKeystrokes) { idx, charKey ->
                            val isCompleted = idx < currentKeystrokeIndex
                            val isCurrent = idx == currentKeystrokeIndex
                            val isLeftHand = HangulKeyboardUtil.isLeftHandConsonant(charKey)
                            val qwertyHint = HangulKeyboardUtil.getQwertyKeyHint(charKey)

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = when {
                                    isCompleted -> Color(0xFFDCFCE7)
                                    isCurrent -> NunbitYellowContainer
                                    isLeftHand -> Color(0xFFEFF6FF)
                                    else -> Color(0xFFFFFBEB)
                                },
                                border = CardDefaults.outlinedCardBorder().copy(
                                    brush = Brush.horizontalGradient(
                                        when {
                                            isCompleted -> listOf(Color(0xFF86EFAC), Color(0xFF4ADE80))
                                            isCurrent -> listOf(NunbitYellow, Color(0xFFD97706))
                                            isLeftHand -> listOf(Color(0xFFBFDBFE), Color(0xFF93C5FD))
                                            else -> listOf(Color(0xFFFDE68A), Color(0xFFFBBF24))
                                        }
                                    )
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = charKey.toString(),
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = when {
                                                isCompleted -> Color(0xFF15803D)
                                                isCurrent -> Color(0xFF92400E)
                                                isLeftHand -> Color(0xFF1E40AF)
                                                else -> Color(0xFFB45309)
                                            }
                                        )
                                        if (isCompleted) {
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Icon(
                                                Icons.Default.Check,
                                                contentDescription = null,
                                                tint = Color(0xFF15803D),
                                                modifier = Modifier.size(13.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = qwertyHint,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Kotak Teks yang Sudah Diketik
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF8FAFC),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.horizontalGradient(
                                if (showCompletedBanner) listOf(SaessakGreen, Color(0xFF059669))
                                else listOf(Color(0xFFE2E8F0), Color(0xFFCBD5E1))
                            )
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 14.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (typedBuffer.isEmpty()) {
                                Text(
                                    text = "Tekan tombol di bawah untuk mengetik...",
                                    color = TextMuted,
                                    fontSize = 13.sp
                                )
                            } else {
                                val assembled = HangulKeyboardUtil.compose(typedBuffer.toList())
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = assembled,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Black,
                                        color = if (showCompletedBanner) Color(0xFF15803D) else InkBlack
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "(${typedBuffer.joinToString(" + ")})",
                                        fontSize = 12.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }

                    // Banner Sukses Selesai Kata Ini
                    AnimatedVisibility(visible = showCompletedBanner) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFDCFCE7),
                                border = CardDefaults.outlinedCardBorder().copy(
                                    brush = Brush.horizontalGradient(listOf(Color(0xFF86EFAC), Color(0xFF4ADE80)))
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SaessakGreen, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Tepat sekali! +15 XP",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Color(0xFF15803D)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = { onNextExercise() },
                                colors = ButtonDefaults.buttonColors(containerColor = HaneulBlue),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("next_typing_exercise_btn")
                            ) {
                                Text("Lanjut Kata Berikutnya", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(15.dp))
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // VIRTUAL 2-BEOL-SIK KEYBOARD COMPONENT
        VirtualKorean2BeolSikKeyboard(
            isShiftActive = isShiftActive,
            highlightChar = nextExpectedChar,
            onKeyPress = { onKeyPress(it) },
            onShiftToggle = { isShiftActive = !isShiftActive },
            onBackspace = { onBackspace() },
            onReset = { onResetDrill() },
            onSpeak = {
                currentExercise?.let { onSpeak(it.targetText, ttsSpeed) }
            },
            onEnter = {
                if (showCompletedBanner) {
                    onNextExercise()
                }
            }
        )
    }
}

enum class VirtualKeyboardMode {
    HANGUL,    // 🇰🇷 한글 두벌식 (2-Beol-Sik)
    ENGLISH,   // 🔤 영문 (English QWERTY)
    SYMBOLS    // 🔢!#1 (숫자 & 기호)
}

/**
 * Komponen Virtual Keyboard Korea Standar Otentik (두벌식 2-Beol-Sik + 한/영 + !#1)
 */
@Composable
fun VirtualKorean2BeolSikKeyboard(
    isShiftActive: Boolean = false,
    highlightChar: Char? = null,
    onKeyPress: (Char) -> Unit,
    onShiftToggle: () -> Unit = {},
    onBackspace: () -> Unit = {},
    onReset: () -> Unit = {},
    onSpeak: () -> Unit = {},
    onEnter: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var keyboardMode by remember { mutableStateOf(VirtualKeyboardMode.HANGUL) }
    var englishShiftActive by remember { mutableStateOf(false) }
    var symbolsPage by remember { mutableIntStateOf(1) }

    // Baris Angka Lengkap dengan Simbol Sekunder khas Keyboard Korea (1..0)
    val numberRow = listOf(
        Pair('1', "!"),
        Pair('2', "@"),
        Pair('3', "#"),
        Pair('4', "$"),
        Pair('5', "%"),
        Pair('6', "^"),
        Pair('7', "&"),
        Pair('8', "*"),
        Pair('9', "("),
        Pair('0', ")")
    )

    // Shift detection untuk konsonan ganda Hangul (ㅃ ㅉ ㄸ ㄲ ㅆ ㅒ ㅖ)
    val shiftedJamos = setOf('ㅃ', 'ㅉ', 'ㄸ', 'ㄲ', 'ㅆ', 'ㅒ', 'ㅖ')
    val isShiftNeeded = highlightChar in shiftedJamos
    val shouldHighlightShift = isShiftActive || isShiftNeeded

    // Baris 1-3 Hangul (Standard 2-Beol-Sik)
    val hangulRow1 = listOf(
        Pair('ㅂ', 'ㅃ'),
        Pair('ㅈ', 'ㅉ'),
        Pair('ㄷ', 'ㄸ'),
        Pair('ㄱ', 'ㄲ'),
        Pair('ㅅ', 'ㅆ'),
        Pair('ㅛ', 'ㅛ'),
        Pair('ㅕ', 'ㅕ'),
        Pair('ㅑ', 'ㅑ'),
        Pair('ㅐ', 'ㅒ'),
        Pair('ㅔ', 'ㅖ')
    )

    val hangulRow2 = listOf(
        Pair('ㅁ', 'ㅁ'),
        Pair('ㄴ', 'ㄴ'),
        Pair('ㅇ', 'ㅇ'),
        Pair('ㄹ', 'ㄹ'),
        Pair('ㅎ', 'ㅎ'),
        Pair('ㅗ', 'ㅗ'),
        Pair('ㅓ', 'ㅓ'),
        Pair('ㅏ', 'ㅏ'),
        Pair('ㅣ', 'ㅣ')
    )

    val hangulRow3 = listOf(
        Pair('ㅋ', 'ㅋ'),
        Pair('ㅌ', 'ㅌ'),
        Pair('ㅊ', 'ㅊ'),
        Pair('ㅍ', 'ㅍ'),
        Pair('ㅠ', 'ㅠ'),
        Pair('ㅜ', 'ㅜ'),
        Pair('ㅡ', 'ㅡ')
    )

    // Baris 1-3 English QWERTY
    val englishRow1 = listOf('q', 'w', 'e', 'r', 't', 'y', 'u', 'i', 'o', 'p')
    val englishRow2 = listOf('a', 's', 'd', 'f', 'g', 'h', 'j', 'k', 'l')
    val englishRow3 = listOf('z', 'x', 'c', 'v', 'b', 'n', 'm')

    // Halaman 1 Simbol (Simbol percakapan esensial, mata uang Won ₩, tanda tanya, tanda seru, tilde)
    val symbolsPage1Row1 = listOf('1', '2', '3', '4', '5', '6', '7', '8', '9', '0')
    val symbolsPage1Row2 = listOf('@', '#', '₩', '%', '&', '-', '+', '(', ')', '/')
    val symbolsPage1Row3 = listOf('*', '"', '\'', ':', ';', '!', '?', '~', '^')

    // Halaman 2 Simbol (Kurung siku, kurung kurawal, simbol matematika & internasional)
    val symbolsPage2Row1 = listOf('1', '2', '3', '4', '5', '6', '7', '8', '9', '0')
    val symbolsPage2Row2 = listOf('[', ']', '{', '}', '<', '>', '=', '_', '\\', '|')
    val symbolsPage2Row3 = listOf('$', '€', '£', '¥', '°', '•', '¿', '¡', '§')

    // Auto-detect & switch ke halaman simbol jika target karakter ada di simbol sekunder
    LaunchedEffect(highlightChar) {
        if (highlightChar != null) {
            if (highlightChar in symbolsPage2Row2 || highlightChar in symbolsPage2Row3) {
                keyboardMode = VirtualKeyboardMode.SYMBOLS
                symbolsPage = 2
            } else if (highlightChar in symbolsPage1Row2 || highlightChar in symbolsPage1Row3) {
                if (highlightChar !in setOf(',', '.', '?', '!', '~', ' ')) {
                    keyboardMode = VirtualKeyboardMode.SYMBOLS
                    symbolsPage = 1
                }
            }
        }
    }

    Surface(
        color = Color(0xFFF1F5F9),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
            .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Bar Keyboard (Mode Indikator + Pintasan Lafal & Reset)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Badge Mode Saat Ini
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = when (keyboardMode) {
                        VirtualKeyboardMode.HANGUL -> HaneulContainer
                        VirtualKeyboardMode.ENGLISH -> Color(0xFFEDE9FE)
                        VirtualKeyboardMode.SYMBOLS -> NunbitYellowContainer
                    }
                ) {
                    Text(
                        text = when (keyboardMode) {
                            VirtualKeyboardMode.HANGUL -> "🇰🇷 한글 2-Beol-Sik"
                            VirtualKeyboardMode.ENGLISH -> "🔤 English QWERTY"
                            VirtualKeyboardMode.SYMBOLS -> "🔢!#1 Simbol ($symbolsPage/2)"
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (keyboardMode) {
                            VirtualKeyboardMode.HANGUL -> HaneulBlue
                            VirtualKeyboardMode.ENGLISH -> Color(0xFF6B21A8)
                            VirtualKeyboardMode.SYMBOLS -> Color(0xFF92400E)
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }

                // Panduan Target (Jika ada highlightChar)
                if (highlightChar != null) {
                    val qwerty = HangulKeyboardUtil.getQwertyKeyHint(highlightChar)
                    val displayChar = if (highlightChar == ' ') "[SPASI]" else highlightChar.toString()
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = NunbitYellowContainer
                    ) {
                        Text(
                            text = "Target: $displayChar ($qwerty)",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF78350F),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                // Tombol Reset & Lafal Ringkas
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFE2E8F0),
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { onReset() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Reset", tint = InkBlack, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("Reset", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = InkBlack)
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = HaneulContainer,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { onSpeak() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Lafal", tint = HaneulBlue, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("Lafal", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = HaneulBlue)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // ====================== BARIS 0 (Angka & Simbol Sekunder 1..0) ======================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(3.dp, Alignment.CenterHorizontally)
            ) {
                numberRow.forEach { (numChar, subSym) ->
                    val isHighlighted = highlightChar == numChar
                    KeyboardNumberKeyButton(
                        char = numChar,
                        subSymbol = subSym,
                        isHighlighted = isHighlighted,
                        modifier = Modifier.weight(1f),
                        onClick = { onKeyPress(numChar) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(3.dp))

            // ====================== BARIS 1 ======================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(3.dp, Alignment.CenterHorizontally)
            ) {
                when (keyboardMode) {
                    VirtualKeyboardMode.HANGUL -> {
                        hangulRow1.forEach { (normal, shifted) ->
                            val charToEmit = if (isShiftActive) shifted else normal
                            val isHighlighted = highlightChar == charToEmit || (!isShiftActive && highlightChar == shifted)
                            val isLeftHand = HangulKeyboardUtil.isLeftHandConsonant(charToEmit)

                            KeyboardKeyButton(
                                char = charToEmit,
                                subChar = if (!isShiftActive && shifted != normal) shifted.toString() else "",
                                qwertyHint = HangulKeyboardUtil.getQwertyKeyHint(normal),
                                isLeftHand = isLeftHand,
                                isHighlighted = isHighlighted,
                                modifier = Modifier.weight(1f),
                                onClick = { onKeyPress(charToEmit) }
                            )
                        }
                    }
                    VirtualKeyboardMode.ENGLISH -> {
                        englishRow1.forEach { rawChar ->
                            val charToEmit = if (englishShiftActive) rawChar.uppercaseChar() else rawChar
                            val isHighlighted = highlightChar?.lowercaseChar() == rawChar
                            KeyboardSimpleKeyButton(
                                text = charToEmit.toString(),
                                isHighlighted = isHighlighted,
                                modifier = Modifier.weight(1f),
                                onClick = { onKeyPress(charToEmit) }
                            )
                        }
                    }
                    VirtualKeyboardMode.SYMBOLS -> {
                        val row1Symbols = if (symbolsPage == 1) symbolsPage1Row1 else symbolsPage2Row1
                        row1Symbols.forEach { sym ->
                            val isHighlighted = highlightChar == sym
                            KeyboardSimpleKeyButton(
                                text = sym.toString(),
                                isHighlighted = isHighlighted,
                                modifier = Modifier.weight(1f),
                                onClick = { onKeyPress(sym) }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // ====================== BARIS 2 ======================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(3.dp, Alignment.CenterHorizontally)
            ) {
                when (keyboardMode) {
                    VirtualKeyboardMode.HANGUL -> {
                        hangulRow2.forEach { (normal, shifted) ->
                            val charToEmit = if (isShiftActive) shifted else normal
                            val isHighlighted = highlightChar == charToEmit
                            val isLeftHand = HangulKeyboardUtil.isLeftHandConsonant(charToEmit)

                            KeyboardKeyButton(
                                char = charToEmit,
                                subChar = "",
                                qwertyHint = HangulKeyboardUtil.getQwertyKeyHint(normal),
                                isLeftHand = isLeftHand,
                                isHighlighted = isHighlighted,
                                modifier = Modifier.weight(1f),
                                onClick = { onKeyPress(charToEmit) }
                            )
                        }
                    }
                    VirtualKeyboardMode.ENGLISH -> {
                        englishRow2.forEach { rawChar ->
                            val charToEmit = if (englishShiftActive) rawChar.uppercaseChar() else rawChar
                            val isHighlighted = highlightChar?.lowercaseChar() == rawChar
                            KeyboardSimpleKeyButton(
                                text = charToEmit.toString(),
                                isHighlighted = isHighlighted,
                                modifier = Modifier.weight(1f),
                                onClick = { onKeyPress(charToEmit) }
                            )
                        }
                    }
                    VirtualKeyboardMode.SYMBOLS -> {
                        val row2Symbols = if (symbolsPage == 1) symbolsPage1Row2 else symbolsPage2Row2
                        row2Symbols.forEach { sym ->
                            val isHighlighted = highlightChar == sym
                            KeyboardSimpleKeyButton(
                                text = sym.toString(),
                                isHighlighted = isHighlighted,
                                modifier = Modifier.weight(1f),
                                onClick = { onKeyPress(sym) }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // ====================== BARIS 3 (Shift / Page Switcher, Karakter Tengah, Backspace) ======================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(3.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tombol Kiri Baris 3: Shift (Hangul/English) atau Page Switcher (Symbols)
                if (keyboardMode == VirtualKeyboardMode.SYMBOLS) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = NunbitYellowContainer,
                        modifier = Modifier
                            .weight(1.3f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                symbolsPage = if (symbolsPage == 1) 2 else 1
                            }
                            .testTag("keyboard_symbols_page_toggle_btn"),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.horizontalGradient(listOf(NunbitYellow, Color(0xFFD97706)))
                        )
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = if (symbolsPage == 1) "1/2 ➔" else "2/2 ➔",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF78350F)
                            )
                        }
                    }
                } else {
                    val shiftActive = if (keyboardMode == VirtualKeyboardMode.HANGUL) shouldHighlightShift else englishShiftActive
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (shiftActive) NunbitYellow else Color(0xFFE2E8F0),
                        modifier = Modifier
                            .weight(1.3f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                if (keyboardMode == VirtualKeyboardMode.HANGUL) {
                                    onShiftToggle()
                                } else {
                                    englishShiftActive = !englishShiftActive
                                }
                            }
                            .testTag("keyboard_shift_btn"),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.horizontalGradient(
                                if (shiftActive) listOf(Color(0xFFD97706), Color(0xFFB45309))
                                else listOf(Color(0xFFCBD5E1), Color(0xFF94A3B8))
                            )
                        )
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.KeyboardArrowUp,
                                    contentDescription = "Shift",
                                    tint = if (shiftActive) Color(0xFF78350F) else InkBlack,
                                    modifier = Modifier.size(16.dp)
                                )
                                if (keyboardMode == VirtualKeyboardMode.HANGUL && isShiftNeeded && !isShiftActive) {
                                    Text(
                                        "⇧ Shift!",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF78350F)
                                    )
                                }
                            }
                        }
                    }
                }

                // Tombol Karakter Tengah Baris 3
                when (keyboardMode) {
                    VirtualKeyboardMode.HANGUL -> {
                        hangulRow3.forEach { (normal, shifted) ->
                            val charToEmit = if (isShiftActive) shifted else normal
                            val isHighlighted = highlightChar == charToEmit
                            val isLeftHand = HangulKeyboardUtil.isLeftHandConsonant(charToEmit)

                            KeyboardKeyButton(
                                char = charToEmit,
                                subChar = "",
                                qwertyHint = HangulKeyboardUtil.getQwertyKeyHint(normal),
                                isLeftHand = isLeftHand,
                                isHighlighted = isHighlighted,
                                modifier = Modifier.weight(1f),
                                onClick = { onKeyPress(charToEmit) }
                            )
                        }
                    }
                    VirtualKeyboardMode.ENGLISH -> {
                        englishRow3.forEach { rawChar ->
                            val charToEmit = if (englishShiftActive) rawChar.uppercaseChar() else rawChar
                            val isHighlighted = highlightChar?.lowercaseChar() == rawChar
                            KeyboardSimpleKeyButton(
                                text = charToEmit.toString(),
                                isHighlighted = isHighlighted,
                                modifier = Modifier.weight(1f),
                                onClick = { onKeyPress(charToEmit) }
                            )
                        }
                    }
                    VirtualKeyboardMode.SYMBOLS -> {
                        val row3Symbols = if (symbolsPage == 1) symbolsPage1Row3 else symbolsPage2Row3
                        row3Symbols.forEach { sym ->
                            val isHighlighted = highlightChar == sym
                            KeyboardSimpleKeyButton(
                                text = sym.toString(),
                                isHighlighted = isHighlighted,
                                modifier = Modifier.weight(1f),
                                onClick = { onKeyPress(sym) }
                            )
                        }
                    }
                }

                // Tombol Backspace Kanan
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFE2E8F0),
                    modifier = Modifier
                        .weight(1.3f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onBackspace() }
                        .testTag("keyboard_backspace_btn"),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.horizontalGradient(listOf(Color(0xFFCBD5E1), Color(0xFF94A3B8)))
                    )
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.AutoMirrored.Filled.Backspace,
                            contentDescription = "Hapus Tuts",
                            tint = SarangRed,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // ====================== BARIS 4 (Bawah: !#1, 한/영, Koma, Spacebar, Titik, ?, !, ~, Enter) ======================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(3.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tombol !#1 / Mode Simbol
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (keyboardMode == VirtualKeyboardMode.SYMBOLS) NunbitYellowContainer else Color(0xFFE2E8F0),
                    modifier = Modifier
                        .weight(1.1f)
                        .height(42.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            keyboardMode = if (keyboardMode == VirtualKeyboardMode.SYMBOLS) VirtualKeyboardMode.HANGUL else VirtualKeyboardMode.SYMBOLS
                        }
                        .testTag("keyboard_symbols_mode_btn")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = if (keyboardMode == VirtualKeyboardMode.SYMBOLS) "한글" else "!#1",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (keyboardMode == VirtualKeyboardMode.SYMBOLS) Color(0xFF78350F) else InkBlack
                        )
                    }
                }

                // Tombol 한/영 (Hangul / English Switch)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (keyboardMode == VirtualKeyboardMode.ENGLISH) HaneulContainer else Color(0xFFE2E8F0),
                    modifier = Modifier
                        .weight(1.0f)
                        .height(42.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            keyboardMode = if (keyboardMode == VirtualKeyboardMode.ENGLISH) VirtualKeyboardMode.HANGUL else VirtualKeyboardMode.ENGLISH
                        }
                        .testTag("keyboard_kor_eng_toggle_btn")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "한/영",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (keyboardMode == VirtualKeyboardMode.ENGLISH) HaneulBlue else InkBlack
                        )
                    }
                }

                // Tuts Koma (,)
                KeyboardPunctuationKeyButton(
                    char = ',',
                    hint = "",
                    isHighlighted = highlightChar == ',',
                    modifier = Modifier.weight(0.75f),
                    onClick = { onKeyPress(',') }
                )

                // Spacebar (한국어 / English / Space)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (highlightChar == ' ') NunbitYellowContainer else Color.White,
                    modifier = Modifier
                        .weight(2.3f)
                        .height(42.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onKeyPress(' ') }
                        .testTag("keyboard_space_btn"),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.horizontalGradient(
                            if (highlightChar == ' ') listOf(NunbitYellow, Color(0xFFD97706))
                            else listOf(Color(0xFFCBD5E1), Color(0xFFCBD5E1))
                        )
                    )
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = when {
                                highlightChar == ' ' -> "👉 [SPASI] 👈"
                                keyboardMode == VirtualKeyboardMode.HANGUL -> "한국어 Space"
                                keyboardMode == VirtualKeyboardMode.ENGLISH -> "English"
                                else -> "Space"
                            },
                            fontSize = 10.sp,
                            fontWeight = if (highlightChar == ' ') FontWeight.ExtraBold else FontWeight.Medium,
                            color = if (highlightChar == ' ') Color(0xFF92400E) else TextSecondary
                        )
                    }
                }

                // Tuts Titik (.)
                KeyboardPunctuationKeyButton(
                    char = '.',
                    hint = "",
                    isHighlighted = highlightChar == '.',
                    modifier = Modifier.weight(0.75f),
                    onClick = { onKeyPress('.') }
                )

                // Tuts Tanda Tanya (?)
                KeyboardPunctuationKeyButton(
                    char = '?',
                    hint = "",
                    isHighlighted = highlightChar == '?',
                    modifier = Modifier.weight(0.8f),
                    onClick = { onKeyPress('?') }
                )

                // Tuts Tanda Seru (!)
                KeyboardPunctuationKeyButton(
                    char = '!',
                    hint = "",
                    isHighlighted = highlightChar == '!',
                    modifier = Modifier.weight(0.8f),
                    onClick = { onKeyPress('!') }
                )

                // Tuts Tilde (~)
                KeyboardPunctuationKeyButton(
                    char = '~',
                    hint = "",
                    isHighlighted = highlightChar == '~',
                    modifier = Modifier.weight(0.8f),
                    onClick = { onKeyPress('~') }
                )

                // Tombol Enter / Selesai (↵)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = HaneulBlue,
                    modifier = Modifier
                        .weight(1.15f)
                        .height(42.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            if (onEnter != null) onEnter()
                            else onKeyPress('\n')
                        }
                        .testTag("keyboard_enter_btn")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "↵ Enter",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

/**
 * Tombol Tuts Simpel untuk Mode Alfabet Inggris & Simbol
 */
@Composable
fun KeyboardSimpleKeyButton(
    text: String,
    isHighlighted: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = if (isHighlighted) NunbitYellowContainer else Color.White,
        animationSpec = tween(150),
        label = "simpleKeyBg"
    )
    val textColor = if (isHighlighted) Color(0xFF78350F) else InkBlack

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = bgColor,
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .testTag("key_$text"),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(
                if (isHighlighted) listOf(NunbitYellow, Color(0xFFD97706))
                else listOf(Color(0xFFCBD5E1), Color(0xFFCBD5E1))
            )
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontSize = 15.sp,
                fontWeight = if (isHighlighted) FontWeight.Black else FontWeight.Bold,
                color = textColor
            )
        }
    }
}

/**
 * Tombol Tuts Angka & Simbol Keyboard (Number/Symbol Key)
 */
@Composable
fun KeyboardNumberKeyButton(
    char: Char,
    subSymbol: String = "",
    isHighlighted: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = if (isHighlighted) NunbitYellowContainer else Color.White,
        animationSpec = tween(150),
        label = "numKeyBg"
    )

    val borderColor = if (isHighlighted) NunbitYellow else Color(0xFFCBD5E1)
    val textColor = if (isHighlighted) Color(0xFF78350F) else InkBlack

    Surface(
        shape = RoundedCornerShape(6.dp),
        color = bgColor,
        modifier = modifier
            .height(34.dp)
            .clip(RoundedCornerShape(6.dp))
            .clickable { onClick() }
            .testTag("key_$char"),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(
                if (isHighlighted) listOf(NunbitYellow, Color(0xFFD97706))
                else listOf(borderColor, borderColor)
            )
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = char.toString(),
                fontSize = 12.sp,
                fontWeight = if (isHighlighted) FontWeight.Black else FontWeight.Bold,
                color = textColor,
                lineHeight = 13.sp
            )
            if (subSymbol.isNotEmpty()) {
                Text(
                    text = subSymbol,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isHighlighted) Color(0xFF92400E) else TextMuted,
                    lineHeight = 9.sp
                )
            }
        }
    }
}

/**
 * Tombol Tuts Tanda Baca Keyboard (Punctuation Key)
 */
@Composable
fun KeyboardPunctuationKeyButton(
    char: Char,
    hint: String = "",
    isHighlighted: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = if (isHighlighted) NunbitYellowContainer else Color.White,
        animationSpec = tween(150),
        label = "punctKeyBg"
    )

    val borderColor = if (isHighlighted) NunbitYellow else Color(0xFFCBD5E1)
    val textColor = if (isHighlighted) Color(0xFF78350F) else InkBlack

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = bgColor,
        modifier = modifier
            .height(42.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .testTag("key_$char"),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(
                if (isHighlighted) listOf(NunbitYellow, Color(0xFFD97706))
                else listOf(borderColor, borderColor)
            )
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = char.toString(),
                fontSize = 14.sp,
                fontWeight = if (isHighlighted) FontWeight.Black else FontWeight.Bold,
                color = textColor
            )
            if (hint.isNotEmpty() && hint != char.toString()) {
                Text(
                    text = hint,
                    fontSize = 7.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextMuted
                )
            }
        }
    }
}

/**
 * Tombol Tuts Tunggal Keyboard
 */
@Composable
fun KeyboardKeyButton(
    char: Char,
    subChar: String,
    qwertyHint: String,
    isLeftHand: Boolean,
    isHighlighted: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = when {
            isHighlighted -> NunbitYellowContainer
            isLeftHand -> Color.White
            else -> Color(0xFFFFFDF5)
        },
        animationSpec = tween(150),
        label = "keyBg"
    )

    val borderColor = when {
        isHighlighted -> NunbitYellow
        isLeftHand -> Color(0xFFCBD5E1)
        else -> Color(0xFFFDE68A)
    }

    val textColor = when {
        isHighlighted -> Color(0xFF78350F)
        isLeftHand -> Color(0xFF1E3A8A)
        else -> Color(0xFF92400E)
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = bgColor,
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .testTag("key_$char"),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(
                if (isHighlighted) listOf(NunbitYellow, Color(0xFFD97706))
                else listOf(borderColor, borderColor)
            )
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = char.toString(),
                    fontSize = 17.sp,
                    fontWeight = if (isHighlighted) FontWeight.Black else FontWeight.Bold,
                    color = textColor
                )

                if (subChar.isNotEmpty()) {
                    Text(
                        text = subChar,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(end = 1.dp)
                    )
                }
            }

            if (qwertyHint.isNotEmpty() && qwertyHint.length == 1) {
                Text(
                    text = qwertyHint,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextMuted
                )
            }
        }
    }
}
