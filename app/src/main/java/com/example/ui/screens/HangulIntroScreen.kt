package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AppSubHeader
import com.example.ui.components.AudioSpeakerButton
import com.example.ui.components.HannieMascotCard
import com.example.ui.theme.HaneulBlue
import com.example.ui.theme.HaneulContainer
import com.example.ui.theme.InkBlack
import com.example.ui.theme.NunbitYellow
import com.example.ui.theme.NunbitYellowContainer
import com.example.ui.theme.SaessakGreen
import com.example.ui.theme.SarangRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import kotlin.random.Random

// Master Catalog of 40 Hangul items (already defined previously)
data class HangulFlashcardItem(
    val character: String,
    val name: String,
    val romanization: String,
    val type: HangulType,
    val category: String,
    val pronunciationTip: String,
    val mnemonicTip: String,
    val strokeCount: Int,
    val strokeDirections: List<String>,
    val exampleWordKo: String,
    val exampleWordRo: String,
    val exampleWordMeaning: String,
    val audioQuery: String = character
)

enum class HangulType(val label: String) {
    VOWEL_BASIC("Vokal Dasar"),
    CONSONANT_BASIC("Konsonan Dasar"),
    VOWEL_COMPOUND("Vokal Ganda"),
    CONSONANT_DOUBLE("Konsonan Ganda")
}

val hangulCatalog: List<HangulFlashcardItem> = listOf(
    // 10 VOKAL DASAR
    HangulFlashcardItem("ㅏ", "A (아)", "a", HangulType.VOWEL_BASIC, "Vokal Dasar", "Bunyi vokal 'a' bulat jernih seperti kata 'ayah'.", "Pohon tegak dengan satu cabang bertunas ke kanan.", 2, listOf("1. Garis vertikal atas ke bawah", "2. Garis pendek ke kanan di tengah"), "아이", "a-i", "Anak / Bayi", "아"),
    HangulFlashcardItem("ㅑ", "Ya (야)", "ya", HangulType.VOWEL_BASIC, "Vokal Dasar", "Bunyi luncuran 'ya' seperti kata 'yakin'.", "Dua cabang ke kanan menandakan luncuran Y.", 3, listOf("1. Garis vertikal panjang", "2. Garis pendek atas", "3. Garis pendek bawah"), "야구", "ya-gu", "Bisbol", "야"),
    HangulFlashcardItem("ㅓ", "Eo (어)", "eo", HangulType.VOWEL_BASIC, "Vokal Dasar", "Bunyi vokal 'o' terbuka santai, mirip kata 'botol'.", "Cabang mengarah ke kiri (ke dalam diri).", 2, listOf("1. Garis pendek horizontal ke kanan", "2. Garis vertikal melintasi ujung kanan"), "어머니", "eo-meo-ni", "Ibu", "어"),
    HangulFlashcardItem("ㅕ", "Yeo (여)", "yeo", HangulType.VOWEL_BASIC, "Vokal Dasar", "Bunyi luncuran 'yo' terbuka seperti 'yogurt'.", "Dua cabang mengarah ke kiri menyatu ke tiang tegak.", 3, listOf("1. Garis horizontal atas", "2. Garis horizontal bawah", "3. Garis vertikal panjang"), "여자", "yeo-ja", "Wanita / Perempuan", "여"),
    HangulFlashcardItem("ㅗ", "O (오)", "o", HangulType.VOWEL_BASIC, "Vokal Dasar", "Bunyi 'o' bulat tertutup sempurna seperti 'soto'.", "Tunas bertumbuh ke atas tanah menghadap matahari langit.", 2, listOf("1. Garis pendek vertikal ke bawah", "2. Garis horizontal panjang"), "오이", "o-i", "Mentimun", "오"),
    HangulFlashcardItem("ㅛ", "Yo (요)", "yo", HangulType.VOWEL_BASIC, "Vokal Dasar", "Bunyi luncuran 'yo' bulat tertutup seperti 'yoyo'.", "Dua tunas vertikal kembar bermekaran ke atas tanah.", 3, listOf("1. Tunas vertikal kiri", "2. Tunas vertikal kanan", "3. Garis horizontal"), "요리", "yo-ri", "Masakan / Memasak", "요"),
    HangulFlashcardItem("ㅜ", "U (우)", "u", HangulType.VOWEL_BASIC, "Vokal Dasar", "Bunyi vokal 'u' maju seperti kata 'buku'.", "Akar menembus ke bawah tanah (unsur bumi teduh).", 2, listOf("1. Garis horizontal panjang", "2. Garis vertikal turun di tengah"), "우유", "u-yu", "Susu", "우"),
    HangulFlashcardItem("ㅠ", "Yu (유)", "yu", HangulType.VOWEL_BASIC, "Vokal Dasar", "Bunyi luncuran 'yu' seperti kata 'payung'.", "Dua akar menjulur ke bawah tanah.", 3, listOf("1. Garis horizontal panjang", "2. Akar vertikal kiri", "3. Akar vertikal kanan"), "유리", "yu-ri", "Kaca / Gelas", "유"),
    HangulFlashcardItem("ㅡ", "Eu (으)", "eu", HangulType.VOWEL_BASIC, "Vokal Dasar", "Bunyi vokal mendatar tanpa membulatkan bibir, mirip 'e' pada 'sedap'.", "Garis lurus mendatar merepresentasikan bumi luas.", 1, listOf("1. Satu garis horizontal dari kiri ke kanan"), "구름", "gu-reum", "Awan", "으"),
    HangulFlashcardItem("ㅣ", "I (이)", "i", HangulType.VOWEL_BASIC, "Vokal Dasar", "Bunyi vokal 'i' tersenyum lebar seperti 'ibu'.", "Garis vertikal lurus mewakili manusia berdiri tegak.", 1, listOf("1. Satu garis vertikal tegak dari atas ke bawah"), "이름", "i-reum", "Nama", "이"),

    // 14 KONSONAN DASAR
    HangulFlashcardItem("ㄱ", "Giyeok (기역)", "g / k", HangulType.CONSONANT_BASIC, "Konsonan Dasar", "Bunyi 'g' lembut di awal kata, 'k' di akhir (batchim).", "Pangkal lidah menempel di tenggorokan, mirip pistol (Gun).", 1, listOf("1. Dari kiri ke kanan langsung tekuk ke bawah"), "가방", "ga-bang", "Tas", "기역"),
    HangulFlashcardItem("ㄴ", "Nieun (니은)", "n", HangulType.CONSONANT_BASIC, "Konsonan Dasar", "Bunyi sengau 'n' lembut seperti kata 'nasi'.", "Ujung lidah menempel gusi atas (Nose/Hidung).", 1, listOf("1. Dari atas turun lalu tekuk ke kanan"), "나무", "na-mu", "Pohon", "니은"),
    HangulFlashcardItem("ㄷ", "Digeut (디귿)", "d / t", HangulType.CONSONANT_BASIC, "Konsonan Dasar", "Bunyi 'd' di awal kata, 't' di akhir suku kata.", "Bentuk pintu (Door) kotak terbuka ke kanan.", 2, listOf("1. Garis horizontal atas", "2. Siku bawah dari kiri ke kanan"), "다리", "da-ri", "Kaki / Jembatan", "디귿"),
    HangulFlashcardItem("ㄹ", "Rieul (리을)", "r / l", HangulType.CONSONANT_BASIC, "Konsonan Dasar", "Bunyi getar 'r' di antara dua vokal, 'l' di akhir suku kata.", "Ular berkelok (Rattlesnake) atau angka dua bersudut.", 3, listOf("1. Siku atas (ㄱ)", "2. Garis tengah", "3. Siku bawah (ㄴ)"), "라디오", "ra-di-o", "Radio", "리을"),
    HangulFlashcardItem("ㅁ", "Mieum (미음)", "m", HangulType.CONSONANT_BASIC, "Konsonan Dasar", "Bunyi 'm' bilabial dengan kedua bibir tertutup rapat.", "Kotak tertutup menyerupai bentuk mulut (Mouth).", 3, listOf("1. Vertikal kiri", "2. Atas ke kanan lalu turun", "3. Garis penutup bawah"), "모자", "mo-ja", "Topi", "미음"),
    HangulFlashcardItem("ㅂ", "Bieup (비읍)", "b / p", HangulType.CONSONANT_BASIC, "Konsonan Dasar", "Bunyi 'b' lembut di awal kata, 'p' di akhir kata.", "Wadah ember terbuka (Bucket) penampung air.", 4, listOf("1. Tiang kiri", "2. Tiang kanan", "3. Jembatan tengah", "4. Dasar penutup"), "바지", "ba-ji", "Celana", "비읍"),
    HangulFlashcardItem("ㅅ", "Siot (시옷)", "s", HangulType.CONSONANT_BASIC, "Konsonan Dasar", "Bunyi desis 's' lembut ('sh' jika bertemu vokal ㅣ).", "Atap rumah atau dua kaki orang berdiri (Stand).", 2, listOf("1. Garis miring ke kiri bawah", "2. Garis miring ke kanan bawah"), "사자", "sa-ja", "Singa", "시옷"),
    HangulFlashcardItem("ㅇ", "Ieung (이응)", "silent / ng", HangulType.CONSONANT_BASIC, "Konsonan Dasar", "Hening di awal suku kata; berbunyi 'ng' jika di akhir suku kata.", "Lingkaran angka nol (Zero) yang hening di awal kata.", 1, listOf("1. Satu lingkaran penuh berlawanan jarum jam"), "안녕", "an-nyeong", "Halo / Damai", "이응"),
    HangulFlashcardItem("ㅈ", "Jieut (지읒)", "j", HangulType.CONSONANT_BASIC, "Konsonan Dasar", "Bunyi afrikat 'j' lembut seperti kata 'jalan'.", "Orang memakai topi datar atau kendi air (Jug).", 2, listOf("1. Garis atas belok miring kiri", "2. Coretan miring ke kanan"), "지도", "ji-do", "Peta", "지읒"),
    HangulFlashcardItem("ㅊ", "Chieut (치읓)", "ch", HangulType.CONSONANT_BASIC, "Konsonan Dasar", "Bunyi afrikat 'ch' aspirasi kuat seperti 'champion'.", "Huruf Jieut bermahkota coretan di atas kepala (Cheetah).", 3, listOf("1. Titik atas", "2. Garis atas cabang kiri", "3. Cabang kanan"), "차", "cha", "Teh / Mobil", "치읓"),
    HangulFlashcardItem("ㅋ", "Kieuk (키읔)", "k", HangulType.CONSONANT_BASIC, "Konsonan Dasar", "Bunyi velar 'k' aspirasi dengan embusan udara kuat keluar.", "Giyeok dengan satu gerigi kunci tambahan (Key).", 2, listOf("1. Garis atas siku ke bawah", "2. Garis tengah horizontal"), "커피", "keo-pi", "Kopi", "키읔"),
    HangulFlashcardItem("ㅌ", "Tieut (티읕)", "t", HangulType.CONSONANT_BASIC, "Konsonan Dasar", "Bunyi alveolar 't' aspirasi dengan embusan udara kuat sela lidah.", "Digeut dengan lidah di tengah, mirip gigi (Teeth).", 3, listOf("1. Garis atas", "2. Garis tengah", "3. Siku bawah"), "토마토", "to-ma-to", "Tomat", "티읕"),
    HangulFlashcardItem("ㅍ", "Pieup (피읖)", "p", HangulType.CONSONANT_BASIC, "Konsonan Dasar", "Bunyi bilabial 'p' aspirasi dengan ledakan napas dari bibir.", "Pilar kuil megah atau simbol pi (Pillar/Parthenon).", 4, listOf("1. Atap horizontal", "2. Tiang kiri", "3. Tiang kanan", "4. Dasar"), "피자", "pi-ja", "Pizza", "피읖"),
    HangulFlashcardItem("ㅎ", "Hieut (히읗)", "h", HangulType.CONSONANT_BASIC, "Konsonan Dasar", "Bunyi hembusan napas 'h' lembut dari tenggorokan.", "Orang mengenakan topi bundar bergaris (Hat).", 3, listOf("1. Coretan pendek atas", "2. Garis tengah", "3. Lingkaran bawah"), "하늘", "ha-neul", "Langit", "히읗"),

    // 11 VOKAL GANDA / DIFTONG
    HangulFlashcardItem("ㅐ", "Ae (애)", "ae", HangulType.VOWEL_COMPOUND, "Vokal Ganda", "Kombinasi ㅏ + ㅣ, menghasilkan bunyi 'e' terbuka seperti 'apel'.", "Huruf A dan I berdiri berdampingan saling merangkul.", 3, listOf("1. Vertikal kiri", "2. Coretan tengah", "3. Vertikal kanan"), "새", "sae", "Burung", "애"),
    HangulFlashcardItem("ㅒ", "Yae (얘)", "yae", HangulType.VOWEL_COMPOUND, "Vokal Ganda", "Kombinasi ㅑ + ㅣ, menghasilkan bunyi 'ye' terbuka.", "Dua cabang di kiri menandakan awalan luncuran Y.", 4, listOf("1. Vertikal kiri", "2. Coretan atas", "3. Coretan bawah", "4. Vertikal kanan"), "얘기", "yae-gi", "Cerita / Obrolan", "얘"),
    HangulFlashcardItem("ㅔ", "E (에)", "e", HangulType.VOWEL_COMPOUND, "Vokal Ganda", "Kombinasi ㅓ + ㅣ, menghasilkan bunyi 'e' seperti 'meja'.", "Coretan horizontal di kiri mengalir ke tiang kanan.", 3, listOf("1. Coretan pendek", "2. Vertikal pertama", "3. Vertikal kedua"), "베개", "be-gae", "Bantal", "에"),
    HangulFlashcardItem("ㅖ", "Ye (예)", "ye", HangulType.VOWEL_COMPOUND, "Vokal Ganda", "Kombinasi ㅕ + ㅣ, menghasilkan bunyi 'ye' seperti 'yes'.", "Dua cabang horizontal mengalir ke tiang tegak.", 4, listOf("1. Coretan atas", "2. Coretan bawah", "3. Vertikal tengah", "4. Vertikal kanan"), "시계", "si-gye", "Jam tangan", "예"),
    HangulFlashcardItem("ㅘ", "Wa (와)", "wa", HangulType.VOWEL_COMPOUND, "Vokal Ganda", "Kombinasi vokal ㅗ + ㅏ diucapkan cepat menjadi 'wa'.", "Matahari di atas memancarkan energi ke huruf A.", 4, listOf("1. Vertikal ㅗ", "2. Horizontal ㅗ", "3. Vertikal ㅏ", "4. Coretan kanan ㅏ"), "사과", "sa-gwa", "Apel", "와"),
    HangulFlashcardItem("ㅙ", "Wae (왜)", "wae", HangulType.VOWEL_COMPOUND, "Vokal Ganda", "Kombinasi vokal ㅗ + ㅐ diucapkan 'wae' seperti 'kenapa'.", "Huruf 'wae' yang terkenal untuk menanyakan alasan.", 5, listOf("1. Bentuk ㅗ", "2. Bentuk ㅐ di sampingnya"), "돼지", "dwae-ji", "Babi", "왜"),
    HangulFlashcardItem("ㅚ", "Oe (외)", "oe / we", HangulType.VOWEL_COMPOUND, "Vokal Ganda", "Kombinasi ㅗ + ㅣ, diucapkan mirip 'we' seperti kata 'web'.", "Huruf O dan I bersatu membentuk bunyi luncuran khas.", 3, listOf("1. Vertikal ㅗ", "2. Horizontal ㅗ", "3. Vertikal panjang ㅣ"), "회사", "hoe-sa", "Perusahaan / Kantor", "외"),
    HangulFlashcardItem("ㅝ", "Wo (워)", "wo", HangulType.VOWEL_COMPOUND, "Vokal Ganda", "Kombinasi vokal ㅜ + ㅓ diucapkan cepat meluncur menjadi 'wo'.", "Akar bawah tanah bersambung dengan vokal terbuka Eo.", 4, listOf("1. Horizontal ㅜ", "2. Vertikal ㅜ", "3. Coretan ㅓ", "4. Vertikal ㅓ"), "뭐", "mwo", "Apa?", "워"),
    HangulFlashcardItem("ㅞ", "We (웨)", "we", HangulType.VOWEL_COMPOUND, "Vokal Ganda", "Kombinasi vokal ㅜ + ㅔ diucapkan 'we' seperti 'wedding'.", "Sering digunakan untuk serapan kata asing.", 5, listOf("1. Bentuk ㅜ", "2. Bentuk ㅔ di sampingnya"), "웨딩", "we-ding", "Pernikahan", "웨"),
    HangulFlashcardItem("ㅟ", "Wi (위)", "wi", HangulType.VOWEL_COMPOUND, "Vokal Ganda", "Kombinasi vokal ㅜ + ㅣ diucapkan 'wi' seperti kata 'wind'.", "Akar tanah bertemu tiang manusia.", 3, listOf("1. Horizontal ㅜ", "2. Vertikal ㅜ", "3. Vertikal ㅣ"), "귀", "gwi", "Telinga", "위"),
    HangulFlashcardItem("ㅢ", "Ui (의)", "ui", HangulType.VOWEL_COMPOUND, "Vokal Ganda", "Kombinasi mendatar ㅡ + tegak ㅣ diucapkan meluncur 'eu-i' cepat.", "Bumi datar menyatu dengan tiang manusia.", 2, listOf("1. Garis datar ㅡ", "2. Garis tegak vertikal ㅣ"), "의사", "ui-sa", "Dokter", "의"),

    // 5 KONSONAN GANDA
    HangulFlashcardItem("ㄲ", "Ssang-giyeok (쌍기역)", "kk", HangulType.CONSONANT_DOUBLE, "Konsonan Ganda", "Dua Giyeok kembar, bunyi 'k' tajam tegang tanpa desis.", "Dua pistol Giyeok berjajar kokoh.", 2, listOf("1. Tulis Giyeok pertama (ㄱ)", "2. Tulis Giyeok kedua (ㄱ) di sampingnya"), "꽃", "kkot", "Bunga", "쌍기역"),
    HangulFlashcardItem("ㄸ", "Ssang-digeut (쌍디귿)", "tt", HangulType.CONSONANT_DOUBLE, "Konsonan Ganda", "Dua Digeut kembar, bunyi 't' tegas dan tegang tanpa desah.", "Dua pintu kembar berdiri sejajar.", 4, listOf("1. Digeut pertama di kiri", "2. Digeut kedua di sampingnya"), "딸기", "ttal-gi", "Stroberi", "쌍디귿"),
    HangulFlashcardItem("ㅃ", "Ssang-bieup (쌍비읍)", "pp", HangulType.CONSONANT_DOUBLE, "Konsonan Ganda", "Dua Bieup kembar, bunyi 'p' tajam dan padat dari bibir.", "Dua ember air berdampingan penuh energi.", 8, listOf("1. Tulis Bieup kiri", "2. Tulis Bieup kanan"), "빵", "ppang", "Roti", "쌍비읍"),
    HangulFlashcardItem("ㅆ", "Ssang-siot (쌍시옷)", "ss", HangulType.CONSONANT_DOUBLE, "Konsonan Ganda", "Dua Siot kembar, bunyi desis 'ss' tajam dan tegas.", "Dua orang berdiri berdampingan saling mendukung.", 4, listOf("1. Siot pertama di kiri", "2. Siot kedua di kanan"), "싸다", "ssa-da", "Murah / Membungkus", "쌍시옷"),
    HangulFlashcardItem("ㅉ", "Ssang-jieut (쌍지읒)", "jj", HangulType.CONSONANT_DOUBLE, "Konsonan Ganda", "Dua Jieut kembar, bunyi 'j' tajam dan tegang.", "Dua kendi kembar berdampingan.", 4, listOf("1. Jieut pertama di kiri", "2. Jieut kedua di kanan"), "찌개", "jji-gae", "Sup / Rebusan Korea", "쌍지읒")
)

/**
 * Standard Unicode Korean Syllable Tables
 */
val CHOSUNG_INITIALS = listOf(
    "ㄱ", "ㄲ", "ㄴ", "ㄷ", "ㄸ", "ㄹ", "ㅁ", "ㅂ", "ㅃ", "ㅅ", "ㅆ", "ㅇ", "ㅈ", "ㅉ", "ㅊ", "ㅋ", "ㅌ", "ㅍ", "ㅎ"
)
val JUNGSUNG_MEDIALS = listOf(
    "ㅏ", "ㅐ", "ㅑ", "ㅒ", "ㅓ", "ㅔ", "ㅕ", "ㅖ", "ㅗ", "ㅘ", "ㅙ", "ㅚ", "ㅛ", "ㅜ", "ㅝ", "ㅞ", "ㅟ", "ㅠ", "ㅡ", "ㅢ", "ㅣ"
)
val JONGSUNG_FINALS = listOf(
    "(Tanpa)", "ㄱ", "ㄲ", "ㄳ", "ㄴ", "ㄵ", "ㄶ", "ㄷ", "ㄹ", "ㄺ", "ㄻ", "ㄼ", "ㄽ", "ㄾ", "ㄿ", "ㅀ", "ㅁ", "ㅂ", "ㅄ", "ㅅ", "ㅆ", "ㅇ", "ㅈ", "ㅊ", "ㅋ", "ㅌ", "ㅍ", "ㅎ"
)

// Pre-set words for the Syllable Builder
data class SyllablePreset(
    val initial: String,
    val medial: String,
    val finalBatchim: String,
    val meaning: String
)

val PRESET_WORDS = listOf(
    SyllablePreset("ㄱ", "ㅏ", "ㅇ", "강 (Sungai)"),
    SyllablePreset("ㅂ", "ㅏ", "ㅂ", "밥 (Nasi / Makanan)"),
    SyllablePreset("ㅁ", "ㅜ", "ㄹ", "물 (Air)"),
    SyllablePreset("ㅈ", "ㅣ", "ㅂ", "집 (Rumah)"),
    SyllablePreset("ㅅ", "ㅏ", "ㄴ", "산 (Gunung)"),
    SyllablePreset("ㄷ", "ㅏ", "ㄹ", "달 (Bulan)"),
    SyllablePreset("ㅂ", "ㅕ", "ㄹ", "별 (Bintang)"),
    SyllablePreset("ㄲ", "ㅗ", "ㅊ", "꽃 (Bunga)"),
    SyllablePreset("ㄴ", "ㅜ", "ㄴ", "눈 (Mata / Salju)"),
    SyllablePreset("ㅎ", "ㅏ", "(Tanpa)", "하 (Ha)")
)

/**
 * Main HangulIntroScreen with 4 interactive tabs:
 * 0: Katalog Kartu (Flashcard Grid)
 * 1: Blok Suku Kata (Syllable Combiner)
 * 2: Latihan Deck (Sequential Flip Practice)
 * 3: Kuis Interaktif (Recognition & Audio Quiz Game)
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HangulIntroScreen(
    onBack: () -> Unit,
    onSpeak: (String) -> Unit,
    onNavigateToWriting: ((String) -> Unit)? = null,
    onAddXp: ((Int) -> Unit)? = null
) {
    // 0: Katalog, 1: Kombinasi Blok, 2: Latihan Deck, 3: Kuis
    var activeTab by remember { mutableIntStateOf(0) }
    var showRomanization by remember { mutableStateOf(true) }

    // Filter categories for catalog
    val filterCategories = listOf("Semua (40)", "Vokal Dasar (10)", "Konsonan Dasar (14)", "Vokal Ganda (11)", "Konsonan Ganda (5)")
    var selectedCategoryIndex by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }

    // Detail dialog item
    var detailDialogLetter by remember { mutableStateOf<HangulFlashcardItem?>(null) }

    // Filtered letters list
    val filteredLetters = remember(selectedCategoryIndex, searchQuery) {
        hangulCatalog.filter { item ->
            val matchCategory = when (selectedCategoryIndex) {
                1 -> item.type == HangulType.VOWEL_BASIC
                2 -> item.type == HangulType.CONSONANT_BASIC
                3 -> item.type == HangulType.VOWEL_COMPOUND
                4 -> item.type == HangulType.CONSONANT_DOUBLE
                else -> true
            }
            val matchSearch = if (searchQuery.isBlank()) {
                true
            } else {
                val q = searchQuery.trim().lowercase()
                item.character.lowercase().contains(q) ||
                    item.romanization.lowercase().contains(q) ||
                    item.name.lowercase().contains(q) ||
                    item.exampleWordKo.lowercase().contains(q) ||
                    item.exampleWordMeaning.lowercase().contains(q)
            }
            matchCategory && matchSearch
        }
    }

    // Modal dialog
    detailDialogLetter?.let { letter ->
        HangulDetailModal(
            item = letter,
            onDismiss = { detailDialogLetter = null },
            onSpeak = onSpeak,
            onPracticeWrite = { char ->
                detailDialogLetter = null
                onNavigateToWriting?.invoke(char)
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .testTag("hangul_intro_screen")
    ) {
        // Sub-Header
        AppSubHeader(
            title = "Pengenalan Hangul (한글)",
            subtitle = "Kuasai 40 huruf, kombinasi blok suku kata & kuis interaktif",
            onBack = onBack,
            trailingAction = {
                // Romanization toggle
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (showRomanization) HaneulContainer else Color(0xFFF1F5F9),
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { showRomanization = !showRomanization }
                        .testTag("toggle_romanization_btn")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (showRomanization) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Toggle Romanisasi",
                            tint = if (showRomanization) HaneulBlue else TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (showRomanization) "Rom: ON" else "Rom: OFF",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (showRomanization) HaneulBlue else TextSecondary
                        )
                    }
                }
            }
        )

        // 4 Primary Feature Tabs
        TabRow(
            selectedTabIndex = activeTab,
            containerColor = Color.White,
            contentColor = HaneulBlue,
            divider = { Box(modifier = Modifier.height(1.dp).fillMaxWidth().background(Color(0xFFE2E8F0))) }
        ) {
            Tab(
                selected = activeTab == 0,
                onClick = { activeTab = 0 },
                text = { Text("Katalog (40)", fontSize = 12.sp, fontWeight = if (activeTab == 0) FontWeight.Bold else FontWeight.Medium) },
                icon = { Icon(Icons.Default.ViewModule, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
            Tab(
                selected = activeTab == 1,
                onClick = { activeTab = 1 },
                text = { Text("Kombinasi Blok", fontSize = 12.sp, fontWeight = if (activeTab == 1) FontWeight.Bold else FontWeight.Medium) },
                icon = { Icon(Icons.Default.Extension, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
            Tab(
                selected = activeTab == 2,
                onClick = { activeTab = 2 },
                text = { Text("Latihan Deck", fontSize = 12.sp, fontWeight = if (activeTab == 2) FontWeight.Bold else FontWeight.Medium) },
                icon = { Icon(Icons.Default.Style, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
            Tab(
                selected = activeTab == 3,
                onClick = { activeTab = 3 },
                text = { Text("Kuis Cepat", fontSize = 12.sp, fontWeight = if (activeTab == 3) FontWeight.Bold else FontWeight.Medium) },
                icon = { Icon(Icons.Default.Quiz, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
        }

        when (activeTab) {
            0 -> {
                // ==========================================
                // TAB 0: KATALOG KARTU FLASHCARD LENGKAP
                // ==========================================
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        HangulStoryBanner(onSpeak = onSpeak)
                    }

                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text("Cari karakter, nama, atau romanisasi...", fontSize = 13.sp) },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp)) },
                                trailingIcon = {
                                    if (searchQuery.isNotEmpty()) {
                                        IconButton(onClick = { searchQuery = "" }) {
                                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = TextSecondary, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White,
                                    focusedBorderColor = HaneulBlue,
                                    unfocusedBorderColor = Color(0xFFE2E8F0)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("hangul_catalog_search")
                            )

                            ScrollableTabRow(
                                selectedTabIndex = selectedCategoryIndex,
                                edgePadding = 0.dp,
                                containerColor = Color.Transparent,
                                divider = {},
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                filterCategories.forEachIndexed { idx, label ->
                                    val isSelected = selectedCategoryIndex == idx
                                    Tab(
                                        selected = isSelected,
                                        onClick = { selectedCategoryIndex = idx },
                                        text = {
                                            Surface(
                                                shape = RoundedCornerShape(20.dp),
                                                color = if (isSelected) HaneulBlue else Color.White,
                                                border = if (isSelected) null else CardDefaults.outlinedCardBorder().copy(
                                                    brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFE2E8F0))
                                                ),
                                                modifier = Modifier.padding(vertical = 4.dp)
                                            ) {
                                                Text(
                                                    text = label,
                                                    fontSize = 12.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                    color = if (isSelected) Color.White else TextSecondary,
                                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                                )
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFEFF6FF),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFBFDBFE))
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.FlipCameraAndroid, contentDescription = null, tint = HaneulBlue, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Ketuk tombol kartu untuk membalik & melihat tips mnemonik + contoh kata. Tekan speaker untuk mendengarkan pelafalan asli.",
                                    fontSize = 11.sp,
                                    color = HaneulBlue,
                                    lineHeight = 15.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Menampilkan ${filteredLetters.size} Huruf",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = InkBlack
                            )
                            Text(
                                text = "Sentuh untuk membalik ↺",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    // 2-column grid
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            for (i in filteredLetters.indices step 2) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    HangulFlashcardButton(
                                        item = filteredLetters[i],
                                        showRomanization = showRomanization,
                                        onSpeak = onSpeak,
                                        onOpenDetail = { detailDialogLetter = it },
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (i + 1 < filteredLetters.size) {
                                        HangulFlashcardButton(
                                            item = filteredLetters[i + 1],
                                            showRomanization = showRomanization,
                                            onSpeak = onSpeak,
                                            onOpenDetail = { detailDialogLetter = it },
                                            modifier = Modifier.weight(1f)
                                        )
                                    } else {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }

                    item {
                        HannieMascotCard(
                            quote = "Raja Sejong mendesain vokal Hangul berdasarkan 3 pilar alam: • Langit (titik), ㅡ Bumi (datar), dan ㅣ Manusia (tegak). Sangat filosofis dan ilmiah!",
                            title = "Pesan Belajar dari Hannie"
                        )
                    }
                }
            }

            1 -> {
                // ==========================================
                // TAB 1: INTERACTIVE SYLLABLE COMBINER (블록 조합기)
                // ==========================================
                HangulSyllableBuilderView(
                    onSpeak = onSpeak,
                    onNavigateToWriting = onNavigateToWriting
                )
            }

            2 -> {
                // ==========================================
                // TAB 2: LATIHAN DECK FLASHCARD SWIPE
                // ==========================================
                var deckIdx by remember { mutableIntStateOf(0) }
                var isDeckFlipped by remember { mutableStateOf(false) }

                HangulDeckPracticeView(
                    letters = filteredLetters.ifEmpty { hangulCatalog },
                    currentIndex = deckIdx,
                    isFlipped = isDeckFlipped,
                    onFlip = { isDeckFlipped = !isDeckFlipped },
                    onNext = {
                        if (deckIdx < filteredLetters.size - 1) {
                            deckIdx++
                            isDeckFlipped = false
                        }
                    },
                    onPrev = {
                        if (deckIdx > 0) {
                            deckIdx--
                            isDeckFlipped = false
                        }
                    },
                    onMarkKnown = {
                        if (deckIdx < filteredLetters.size - 1) {
                            deckIdx++
                            isDeckFlipped = false
                        }
                    },
                    onShuffle = {
                        deckIdx = 0
                        isDeckFlipped = false
                    },
                    onSpeak = onSpeak,
                    onOpenDetail = { detailDialogLetter = it }
                )
            }

            3 -> {
                // ==========================================
                // TAB 3: KUIS INTERAKTIF HANGUL
                // ==========================================
                HangulQuizView(
                    onSpeak = onSpeak,
                    onAwardXp = { xp -> onAddXp?.invoke(xp) }
                )
            }
        }
    }
}

/**
 * Interactive Syllable Builder Composable:
 * Dynamically synthesizes Korean syllables using the real Unicode equation:
 * Unicode = 0xAC00 + (Chosung * 21 + Jungsung) * 28 + Jongsung
 */
@Composable
fun HangulSyllableBuilderView(
    onSpeak: (String) -> Unit,
    onNavigateToWriting: ((String) -> Unit)? = null
) {
    var selectedChosungIndex by remember { mutableIntStateOf(0) } // ㄱ
    var selectedJungsungIndex by remember { mutableIntStateOf(0) } // ㅏ
    var selectedJongsungIndex by remember { mutableIntStateOf(0) } // (Tanpa)

    // Calculate Unicode syllable dynamically
    val synthesizedSyllable = remember(selectedChosungIndex, selectedJungsungIndex, selectedJongsungIndex) {
        val code = 0xAC00 + (selectedChosungIndex * 21 + selectedJungsungIndex) * 28 + selectedJongsungIndex
        code.toChar().toString()
    }

    val chosungChar = CHOSUNG_INITIALS.getOrElse(selectedChosungIndex) { CHOSUNG_INITIALS.first() }
    val jungsungChar = JUNGSUNG_MEDIALS.getOrElse(selectedJungsungIndex) { JUNGSUNG_MEDIALS.first() }
    val jongsungLabel = JONGSUNG_FINALS.getOrElse(selectedJongsungIndex) { JONGSUNG_FINALS.first() }
    val hasBatchim = selectedJongsungIndex > 0

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Explainer Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFE2E8F0))
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Laboratorium Kombinasi Suku Kata (음절)",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = InkBlack
                )
                Text(
                    text = "Hangul tidak ditulis berjejer seperti Latin, melainkan dirangkai menjadi satu blok suku kata kotak.",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Synthesized Character Big Preview
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = HaneulContainer,
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF93C5FD))
                    ),
                    modifier = Modifier.size(110.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = synthesizedSyllable,
                            fontSize = 62.sp,
                            fontWeight = FontWeight.Black,
                            color = HaneulBlue
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Breakdown pills
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFFFF7ED)) {
                        Text("Awal: $chosungChar", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEA580C), modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                    }
                    Text("+", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                    Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFEFF6FF)) {
                        Text("Vokal: $jungsungChar", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2563EB), modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                    }
                    if (hasBatchim) {
                        Text("+", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                        Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFF0FDF4)) {
                            Text("Batchim: $jongsungLabel", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF059669), modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AudioSpeakerButton(
                        onClick = { onSpeak(synthesizedSyllable) },
                        label = "Dengarkan: $synthesizedSyllable"
                    )

                    Button(
                        onClick = { onNavigateToWriting?.invoke(synthesizedSyllable) },
                        colors = ButtonDefaults.buttonColors(containerColor = HaneulBlue),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Latih Tulis", fontSize = 12.sp)
                    }
                }
            }
        }

        // Quick Preset Words Chips
        Column {
            Text(
                text = "Pintasan Kosakata Populer:",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = InkBlack
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(PRESET_WORDS) { preset ->
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color.White,
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFE2E8F0))
                        ),
                        modifier = Modifier
                            .clickable {
                                selectedChosungIndex = CHOSUNG_INITIALS.indexOf(preset.initial).coerceAtLeast(0)
                                selectedJungsungIndex = JUNGSUNG_MEDIALS.indexOf(preset.medial).coerceAtLeast(0)
                                selectedJongsungIndex = JONGSUNG_FINALS.indexOf(preset.finalBatchim).coerceAtLeast(0)
                                onSpeak(preset.meaning.substringBefore(" "))
                            }
                            .testTag("preset_word_${preset.meaning}")
                    ) {
                        Text(
                            text = preset.meaning,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = HaneulBlue,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // 1. Selector Chosung (Konsonan Awal)
        Column {
            Text(
                text = "1. Pilih Konsonan Awal (초성):",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = InkBlack
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                itemsIndexed(CHOSUNG_INITIALS) { idx, char ->
                    val isSelected = selectedChosungIndex == idx
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) Color(0xFFEA580C) else Color.White,
                        border = if (isSelected) null else CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFFED7AA))
                        ),
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                selectedChosungIndex = idx
                                onSpeak(char)
                            }
                            .testTag("chosung_pick_$char")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = char,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else Color(0xFFC2410C)
                            )
                        }
                    }
                }
            }
        }

        // 2. Selector Jungsung (Vokal Tengah)
        Column {
            Text(
                text = "2. Pilih Vokal Tengah (중성):",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = InkBlack
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                itemsIndexed(JUNGSUNG_MEDIALS) { idx, char ->
                    val isSelected = selectedJungsungIndex == idx
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) Color(0xFF2563EB) else Color.White,
                        border = if (isSelected) null else CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFBFDBFE))
                        ),
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                selectedJungsungIndex = idx
                                onSpeak(char)
                            }
                            .testTag("jungsung_pick_$char")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = char,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else Color(0xFF1D4ED8)
                            )
                        }
                    }
                }
            }
        }

        // 3. Selector Jongsung (Konsonan Akhir / Batchim)
        Column {
            Text(
                text = "3. Pilih Konsonan Akhir / Batchim (종성):",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = InkBlack
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                itemsIndexed(JONGSUNG_FINALS) { idx, char ->
                    val isSelected = selectedJongsungIndex == idx
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) Color(0xFF059669) else Color.White,
                        border = if (isSelected) null else CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFBBF7D0))
                        ),
                        modifier = Modifier
                            .size(if (idx == 0) 64.dp else 44.dp, 44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { selectedJongsungIndex = idx }
                            .testTag("jongsung_pick_$char")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = char,
                                fontSize = if (idx == 0) 11.sp else 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else Color(0xFF047857)
                            )
                        }
                    }
                }
            }
        }

        // Anatomy diagram card
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFFF1F5F9),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lightbulb, contentDescription = null, tint = NunbitYellow, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Rumus Anatomi Blok Hangul:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = InkBlack)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "• Blok 2 Bagian: Konsonan + Vokal (Contoh: ㄱ + ㅏ = 가)\n• Blok 3 Bagian: Konsonan + Vokal + Batchim (Contoh: ㄱ + ㅏ + ㅇ = 강)\n• Vokal tegak (ㅏ, ㅓ, ㅣ) diletakkan di samping kanan, vokal datar (ㅗ, ㅜ, ㅡ) diletakkan di bawah konsonan awal.",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

/**
 * Interactive Hangul Quiz Mini-game:
 * Tests reading recognition and audio listening with instant feedback, streaks, and XP reward.
 */
@Composable
fun HangulQuizView(
    onSpeak: (String) -> Unit,
    onAwardXp: (Int) -> Unit
) {
    // Generate 10 quiz questions
    var currentQuestionIndex by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }
    var streak by remember { mutableIntStateOf(0) }
    var selectedAnswerIndex by remember { mutableStateOf<Int?>(null) }
    var isSubmitted by remember { mutableStateOf(false) }
    var isFinished by remember { mutableStateOf(false) }

    // Quiz dataset (10 questions)
    val questions = remember {
        listOf(
            QuizQuestion("Huruf manakah yang berbunyi 'a'?", "ㅏ", listOf("ㅏ", "ㅓ", "ㅗ", "ㅣ"), 0, "ㅏ berbunyi 'a' dengan cabang ke kanan."),
            QuizQuestion("Dengarkan audio. Huruf apakah ini?", "ㄱ", listOf("ㄴ", "ㄱ", "ㄷ", "ㄹ"), 1, "ㄱ (Giyeok) berbentuk siku seperti pistol.", isAudioQuestion = true),
            QuizQuestion("Huruf 'ㅁ' (Mieum) memiliki romanisasi...", "m", listOf("n", "b", "m", "s"), 2, "ㅁ berbentuk kotak mulut (Mouth) mewakili bunyi M."),
            QuizQuestion("Dengarkan audio. Huruf apakah ini?", "ㅗ", listOf("ㅜ", "ㅗ", "ㅛ", "ㅠ"), 1, "ㅗ (O) berbentuk tunas ke atas tanah.", isAudioQuestion = true),
            QuizQuestion("Huruf 'ㅂ' (Bieup) memiliki bentuk yang mirip...", "Wadah ember air (Bucket)", listOf("Ular meliuk", "Wadah ember air (Bucket)", "Pohon bercabang", "Pintu"), 1, "ㅂ menyerupai ember terbuka penampung air."),
            QuizQuestion("Dengarkan audio suku kata ini:", "강", listOf("강", "방", "공", "감"), 0, "강 (gang) terbentuk dari ㄱ + ㅏ + ㅇ.", isAudioQuestion = true),
            QuizQuestion("Manakah yang merupakan vokal 'u' (seperti dalam buku)?", "ㅜ", listOf("ㅗ", "ㅡ", "ㅜ", "ㅏ"), 2, "ㅜ memiliki akar yang menjulur ke bawah tanah."),
            QuizQuestion("Dengarkan audio huruf aspirasi ini:", "ㅋ", listOf("ㄱ", "ㅋ", "ㄲ", "ㅌ"), 1, "ㅋ (Kieuk) adalah konsonan letup berhembus K.", isAudioQuestion = true),
            QuizQuestion("Huruf 'ㅇ' (Ieung) jika berada di akhir suku kata berbunyi...", "ng", listOf("h", "silent", "ng", "m"), 2, "ㅇ hening di awal kata, namun berbunyi 'ng' di posisi batchim."),
            QuizQuestion("Dengarkan kata contoh berikut:", "사과", listOf("사과", "시계", "오이", "우유"), 0, "사과 (sagwa) berarti apel.", isAudioQuestion = true)
        )
    }

    val currentQ = questions.getOrElse(currentQuestionIndex) { questions.first() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!isFinished) {
            // Top Stats: Question counter, Streak, and Score
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Soal ${currentQuestionIndex + 1} / ${questions.size}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = InkBlack
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFFEF3C7)) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Streak: $streak 🔥", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF92400E))
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Skor: $score", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = HaneulBlue)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                LinearProgressIndicator(
                    progress = { (currentQuestionIndex + 1).toFloat() / questions.size.toFloat() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = HaneulBlue,
                    trackColor = Color(0xFFE2E8F0)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Question Card
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFE2E8F0))
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (currentQ.isAudioQuestion) {
                        Surface(
                            shape = CircleShape,
                            color = HaneulContainer,
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .clickable { onSpeak(currentQ.target) }
                                .testTag("quiz_audio_btn")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = "Putar Audio Soal",
                                    tint = HaneulBlue,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Tekan ikon untuk mendengarkan audio",
                            fontSize = 12.sp,
                            color = HaneulBlue,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = currentQ.question,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = InkBlack
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // 4 Options (2x2 grid)
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        for (row in 0 until 2) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                for (col in 0 until 2) {
                                    val idx = row * 2 + col
                                    val option = currentQ.options[idx]
                                    val isSelected = selectedAnswerIndex == idx
                                    val isCorrectOption = idx == currentQ.correctIndex

                                    val bgColor by animateColorAsState(
                                        targetValue = when {
                                            isSubmitted && isCorrectOption -> Color(0xFFD1FAE5)
                                            isSubmitted && isSelected && !isCorrectOption -> Color(0xFFFEE2E2)
                                            isSelected -> HaneulContainer
                                            else -> Color(0xFFF8FAFC)
                                        },
                                        label = "btn_bg"
                                    )

                                    val textColor = when {
                                        isSubmitted && isCorrectOption -> Color(0xFF065F46)
                                        isSubmitted && isSelected && !isCorrectOption -> Color(0xFF991B1B)
                                        isSelected -> HaneulBlue
                                        else -> InkBlack
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = bgColor,
                                        border = CardDefaults.outlinedCardBorder().copy(
                                            brush = androidx.compose.ui.graphics.SolidColor(
                                                when {
                                                    isSubmitted && isCorrectOption -> Color(0xFF10B981)
                                                    isSubmitted && isSelected && !isCorrectOption -> Color(0xFFEF4444)
                                                    isSelected -> HaneulBlue
                                                    else -> Color(0xFFE2E8F0)
                                                }
                                            )
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(56.dp)
                                            .clip(RoundedCornerShape(14.dp))
                                            .clickable(enabled = !isSubmitted) {
                                                selectedAnswerIndex = idx
                                            }
                                            .testTag("quiz_opt_$idx")
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = option,
                                                fontSize = if (option.length == 1) 22.sp else 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = textColor,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (isSubmitted) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (selectedAnswerIndex == currentQ.correctIndex) Color(0xFFF0FDF4) else Color(0xFFFEF2F2),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = (if (selectedAnswerIndex == currentQ.correctIndex) "Benar! 🎉 " else "Kurang tepat! 💡 ") + currentQ.explanation,
                                fontSize = 11.sp,
                                color = if (selectedAnswerIndex == currentQ.correctIndex) Color(0xFF047857) else Color(0xFFB91C1C),
                                modifier = Modifier.padding(10.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bottom Action Button
            if (!isSubmitted) {
                Button(
                    onClick = {
                        if (selectedAnswerIndex != null) {
                            isSubmitted = true
                            if (selectedAnswerIndex == currentQ.correctIndex) {
                                score += 10 + (streak * 2)
                                streak++
                            } else {
                                streak = 0
                            }
                        }
                    },
                    enabled = selectedAnswerIndex != null,
                    colors = ButtonDefaults.buttonColors(containerColor = HaneulBlue),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("quiz_submit_btn")
                ) {
                    Text("Periksa Jawaban", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = {
                        if (currentQuestionIndex < questions.size - 1) {
                            currentQuestionIndex++
                            selectedAnswerIndex = null
                            isSubmitted = false
                        } else {
                            isFinished = true
                            onAwardXp(score)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SaessakGreen),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("quiz_next_btn")
                ) {
                    Text(if (currentQuestionIndex < questions.size - 1) "Soal Berikutnya ➔" else "Lihat Hasil Akhir 🎉", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        } else {
            // FINISHED CELEBRATION VIEW
            Card(
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "🎉", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Selamat, Kuis Selesai!",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = InkBlack
                    )
                    Text(
                        text = "Kamu telah menyelesaikan 10 soal pengenalan huruf Hangul.",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = NunbitYellowContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Total Skor Akhir:", fontSize = 12.sp, color = Color(0xFF78350F))
                            Text("$score Poin", fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color(0xFF92400E))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("+${score} XP ditambahkan ke profilmu!", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SaessakGreen)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            currentQuestionIndex = 0
                            score = 0
                            streak = 0
                            selectedAnswerIndex = null
                            isSubmitted = false
                            isFinished = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = HaneulBlue),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Coba Kuis Lagi", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

data class QuizQuestion(
    val question: String,
    val target: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String,
    val isAudioQuestion: Boolean = false
)

/**
 * Flashcard button on catalog
 */
@Composable
fun HangulFlashcardButton(
    item: HangulFlashcardItem,
    showRomanization: Boolean,
    onSpeak: (String) -> Unit,
    onOpenDetail: (HangulFlashcardItem) -> Unit,
    modifier: Modifier = Modifier
) {
    var isFlipped by remember { mutableStateOf(false) }

    val (primaryColor, containerColor, borderColor) = when (item.type) {
        HangulType.VOWEL_BASIC -> Triple(Color(0xFF2563EB), Color(0xFFEFF6FF), Color(0xFFBFDBFE))
        HangulType.CONSONANT_BASIC -> Triple(Color(0xFFEA580C), Color(0xFFFFF7ED), Color(0xFFFED7AA))
        HangulType.VOWEL_COMPOUND -> Triple(Color(0xFF7C3AED), Color(0xFFF5F3FF), Color(0xFFDDD6FE))
        HangulType.CONSONANT_DOUBLE -> Triple(Color(0xFF059669), Color(0xFFF0FDF4), Color(0xFFBBF7D0))
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .border(1.5.dp, borderColor, RoundedCornerShape(20.dp))
            .clickable { isFlipped = !isFlipped }
            .testTag("flashcard_button_${item.character}")
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (showRomanization) {
                        Surface(shape = RoundedCornerShape(8.dp), color = containerColor) {
                            Text(
                                text = item.romanization,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = primaryColor,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                            )
                        }
                    } else {
                        Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFF1F5F9)) {
                            Text(
                                text = "•••",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = containerColor,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .clickable { onSpeak(item.audioQuery) }
                                .testTag("audio_btn_${item.character}")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = "Audio ${item.name}",
                                    tint = primaryColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFF1F5F9),
                            modifier = Modifier.size(30.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.FlipCameraAndroid,
                                    contentDescription = "Balik",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (!isFlipped) {
                    // FRONT FACE
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = item.character,
                            fontSize = 42.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = InkBlack,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = item.name,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryColor
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = item.pronunciationTip,
                            fontSize = 11.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 14.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFF8FAFC), modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Sentuh untuk balik kartu ↺",
                                fontSize = 10.sp,
                                color = TextMuted,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }
                } else {
                    // BACK FACE
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFFFEF3C7), modifier = Modifier.fillMaxWidth()) {
                            Row(modifier = Modifier.padding(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Lightbulb, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Mnemonik:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF92400E))
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = item.mnemonicTip,
                            fontSize = 11.sp,
                            color = InkBlack,
                            lineHeight = 15.sp,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = containerColor,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSpeak(item.exampleWordKo) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "${item.exampleWordKo}" + if (showRomanization) " (${item.exampleWordRo})" else "",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = primaryColor
                                    )
                                    Text(text = item.exampleWordMeaning, fontSize = 10.sp, color = TextSecondary)
                                }
                                Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, tint = primaryColor, modifier = Modifier.size(14.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Buka Detail & Stroke ➔",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryColor,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenDetail(item) }
                                .padding(vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Deck Practice View for swipeable / sequential flashcard study.
 */
@Composable
fun HangulDeckPracticeView(
    letters: List<HangulFlashcardItem>,
    currentIndex: Int,
    isFlipped: Boolean,
    onFlip: () -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    onMarkKnown: () -> Unit,
    onShuffle: () -> Unit,
    onSpeak: (String) -> Unit,
    onOpenDetail: (HangulFlashcardItem) -> Unit
) {
    val currentItem = letters.getOrElse(currentIndex) { letters.first() }
    val progress = (currentIndex + 1).toFloat() / letters.size.toFloat()

    val (primaryColor, containerColor) = when (currentItem.type) {
        HangulType.VOWEL_BASIC -> Color(0xFF2563EB) to Color(0xFFEFF6FF)
        HangulType.CONSONANT_BASIC -> Color(0xFFEA580C) to Color(0xFFFFF7ED)
        HangulType.VOWEL_COMPOUND -> Color(0xFF7C3AED) to Color(0xFFF5F3FF)
        HangulType.CONSONANT_DOUBLE -> Color(0xFF059669) to Color(0xFFF0FDF4)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color.White,
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFE2E8F0))
                    )
                ) {
                    Text(
                        text = "Kartu ${currentIndex + 1} dari ${letters.size}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = InkBlack,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = containerColor
                ) {
                    Text(
                        text = currentItem.category,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryColor,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = primaryColor,
                trackColor = Color(0xFFE2E8F0)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        ElevatedCard(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clickable { onFlip() }
                .testTag("big_flashcard_deck_item")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AudioSpeakerButton(
                        onClick = { onSpeak(currentItem.audioQuery) },
                        label = "Dengar"
                    )

                    OutlinedButton(
                        onClick = onFlip,
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.FlipCameraAndroid, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isFlipped) "Tutup" else "Balik", fontSize = 12.sp)
                    }
                }

                AnimatedContent(
                    targetState = isFlipped,
                    transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(200)) },
                    label = "deck_flip_anim"
                ) { flipped ->
                    if (!flipped) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = currentItem.character,
                                fontSize = 84.sp,
                                fontWeight = FontWeight.Black,
                                color = InkBlack
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = currentItem.name,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = primaryColor
                            )

                            Text(
                                text = "Pengucapan: \"${currentItem.romanization}\"",
                                fontSize = 15.sp,
                                color = TextSecondary,
                                fontWeight = FontWeight.Medium
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color(0xFFF8FAFC),
                                border = CardDefaults.outlinedCardBorder().copy(
                                    brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFE2E8F0))
                                )
                            ) {
                                Text(
                                    text = "Sentuh kartu untuk melihat tips mengingat & contoh kosakata",
                                    fontSize = 11.sp,
                                    color = TextMuted,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                        }
                    } else {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFFEF3C7)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Lightbulb, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Cara Cepat Mengingat (Mnemonik)",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF92400E)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = currentItem.mnemonicTip,
                                fontSize = 14.sp,
                                color = InkBlack,
                                textAlign = TextAlign.Center,
                                lineHeight = 20.sp
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = containerColor,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSpeak(currentItem.exampleWordKo) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "Contoh Kata:",
                                            fontSize = 11.sp,
                                            color = TextSecondary
                                        )
                                        Text(
                                            text = "${currentItem.exampleWordKo} [${currentItem.exampleWordRo}]",
                                            fontSize = 17.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = primaryColor
                                        )
                                        Text(
                                            text = "= ${currentItem.exampleWordMeaning}",
                                            fontSize = 13.sp,
                                            color = InkBlack,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                    Icon(
                                        Icons.AutoMirrored.Filled.VolumeUp,
                                        contentDescription = "Dengarkan",
                                        tint = primaryColor,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                OutlinedButton(
                    onClick = { onOpenDetail(currentItem) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Panduan Goresan & Detail Lengkap", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = onPrev,
                enabled = currentIndex > 0,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("deck_prev_button")
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Sebelumnya")
                Spacer(modifier = Modifier.width(4.dp))
                Text("Sebelum")
            }

            Button(
                onClick = onMarkKnown,
                colors = ButtonDefaults.buttonColors(containerColor = SaessakGreen),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .weight(1.2f)
                    .height(48.dp)
                    .testTag("deck_known_button")
            ) {
                Icon(Icons.Default.Check, contentDescription = "Sudah Paham")
                Spacer(modifier = Modifier.width(4.dp))
                Text("Sudah Paham", fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = onNext,
                enabled = currentIndex < letters.size - 1,
                colors = ButtonDefaults.buttonColors(containerColor = HaneulBlue),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("deck_next_button")
            ) {
                Text("Lanjut")
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Lanjut")
            }
        }
    }
}

/**
 * Story & Historical Background Banner of Hangul by King Sejong the Great.
 */
@Composable
fun HangulStoryBanner(onSpeak: (String) -> Unit) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = HaneulBlue),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = NunbitYellow
                ) {
                    Text(
                        text = "한글 (Hangul)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = InkBlack,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White.copy(alpha = 0.2f),
                    modifier = Modifier.clickable { onSpeak("세종대왕 한글 창제") }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Dengar", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Alfabet Terhebat Ciptaan Raja Sejong",
                fontSize = 17.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Diciptakan pada tahun 1443 oleh Raja Sejong yang Agung agar seluruh rakyat dapat membaca dan menulis dengan mudah. Hangul terdiri dari 40 huruf: 10 Vokal Dasar, 14 Konsonan Dasar, 11 Vokal Ganda, dan 5 Konsonan Ganda.",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.9f),
                lineHeight = 17.sp
            )
        }
    }
}

/**
 * Modal dialog showing detailed stroke guide and phonetics for a Hangul character.
 */
@Composable
fun HangulDetailModal(
    item: HangulFlashcardItem,
    onDismiss: () -> Unit,
    onSpeak: (String) -> Unit,
    onPracticeWrite: (String) -> Unit
) {
    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = { onPracticeWrite(item.character) },
                colors = ButtonDefaults.buttonColors(containerColor = HaneulBlue),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("modal_practice_write_btn")
            ) {
                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Latihan Tulis di Kanvas")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Tutup")
            }
        },
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.character,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = InkBlack
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = item.name,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = HaneulBlue
                        )
                        Text(
                            text = item.category,
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }

                Surface(
                    shape = CircleShape,
                    color = HaneulContainer,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .clickable { onSpeak(item.audioQuery) }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Dengarkan",
                            tint = HaneulBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF8FAFC),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFE2E8F0))
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Cara Pengucapan:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = HaneulBlue
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = item.pronunciationTip,
                            fontSize = 12.sp,
                            color = InkBlack,
                            lineHeight = 16.sp
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFEF3C7)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lightbulb, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Tips Mnemonik Mengingat:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF92400E)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = item.mnemonicTip,
                            fontSize = 12.sp,
                            color = Color(0xFF78350F),
                            lineHeight = 16.sp
                        )
                    }
                }

                Column {
                    Text(
                        text = "Urutan Goresan (${item.strokeCount} Goresan):",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = InkBlack
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    item.strokeDirections.forEach { step ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = HaneulContainer,
                                modifier = Modifier.size(8.dp)
                            ) {}
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = step,
                                fontSize = 11.sp,
                                color = TextSecondary,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFF0FDF4),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFBBF7D0))
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Kosakata Contoh:", fontSize = 11.sp, color = Color(0xFF047857))
                            Text(
                                text = "${item.exampleWordKo} [${item.exampleWordRo}]",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF065F46)
                            )
                            Text(text = "= ${item.exampleWordMeaning}", fontSize = 12.sp, color = InkBlack)
                        }
                        IconButton(
                            onClick = { onSpeak(item.exampleWordKo) }
                        ) {
                            Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Dengarkan", tint = Color(0xFF047857))
                        }
                    }
                }
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White
    )
}
