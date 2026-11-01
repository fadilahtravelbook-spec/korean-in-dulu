package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Style
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HangulLetter
import com.example.ui.components.AppSubHeader
import com.example.ui.components.BatchimGuideView
import com.example.ui.components.HangulKeyboardUtil
import com.example.ui.components.KoreanDocumentPracticeView
import com.example.ui.components.KoreanKeyboardPracticeView
import com.example.ui.components.VirtualKorean2BeolSikKeyboard
import com.example.ui.theme.HaneulBlue
import com.example.ui.theme.HaneulContainer
import com.example.ui.theme.InkBlack
import com.example.ui.theme.NunbitYellow
import com.example.ui.theme.NunbitYellowContainer
import com.example.ui.theme.SaessakGreen
import com.example.ui.theme.SarangRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class HangulWordPractice(
    val word: String,
    val romanization: String,
    val meaningId: String,
    val category: String,
    val iconEmoji: String,
    val syllables: List<String>
)

@Composable
fun WritingScreen(
    letters: List<HangulLetter>,
    initialLetter: String? = null,
    onBack: () -> Unit,
    onSpeak: (String, Float) -> Unit,
    onStopSpeak: () -> Unit = {},
    onAddXp: (Int) -> Unit = {},
    onRecordProgress: (moduleId: String, lessonId: String, title: String, score: Int) -> Unit,
    onNavigateToHangulIntro: (() -> Unit)? = null
) {
    // Mode Utama Menulis:
    // 0: Coretan Huruf (Stroke Order & Auto-play Berurutan)
    // 1: Gabung Suku Kata (Choseong + Jungseong + Jongseong)
    // 2: Kata Dasar (Praktik menulis kata bermakna)
    // 3: Kuis Dikte (Blind Test pendengaran & menulis)
    // 4: Aturan Batchim (Konsonan Akhir)
    var mainWritingTab by remember { mutableIntStateOf(0) }

    // Global Audio Speed: 0.6x (Sangat Jelas), 0.8x (Santai), 1.0x (Normal), 1.2x (Cepat)
    var ttsSpeed by remember { mutableFloatStateOf(0.9f) }

    DisposableEffect(Unit) {
        onDispose {
            onStopSpeak()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        // App Header
        AppSubHeader(
            title = "Ketik & Latihan Hangul (한글 타자 & 쓰기)",
            subtitle = "Latihan ketik 2-Beol-Sik, susun suku kata, kuis dikte audio & aturan batchim",
            onBack = onBack,
            trailingAction = if (onNavigateToHangulIntro != null) {
                {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = HaneulContainer,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onNavigateToHangulIntro() }
                            .testTag("writing_to_hangul_intro_btn")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Style, contentDescription = null, tint = HaneulBlue, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Flashcard", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = HaneulBlue)
                        }
                    }
                }
            } else null
        )

        // Tab Navigasi Fitur Utama
        ScrollableTabRow(
            selectedTabIndex = mainWritingTab,
            containerColor = Color.White,
            contentColor = HaneulBlue,
            edgePadding = 12.dp,
            divider = {}
        ) {
            val tabs = listOf(
                "⌨️ Ketik Huruf",
                "🚀 Tantangan Ketik",
                "🧩 Gabung Suku Kata",
                "📖 Kata Dasar",
                "📜 Dokumen & Surat",
                "🎧 Kuis Dikte Audio",
                "📚 Aturan Batchim"
            )
            tabs.forEachIndexed { idx, label ->
                Tab(
                    selected = mainWritingTab == idx,
                    onClick = {
                        onStopSpeak()
                        mainWritingTab = idx
                    },
                    text = {
                        Text(
                            text = label,
                            fontWeight = if (mainWritingTab == idx) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 12.sp
                        )
                    }
                )
            }
        }

        // Toolbar Pengaturan Kecepatan Suara Global (0.6x - 1.2x)
        Surface(
            color = Color(0xFFF1F5F9),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Speed, contentDescription = null, tint = HaneulBlue, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Kecepatan Suara:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = InkBlack
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf(
                        0.6f to "0.6x (Jelas)",
                        0.8f to "0.8x",
                        1.0f to "1.0x",
                        1.2f to "1.2x"
                    ).forEach { (spd, label) ->
                        val isSel = ttsSpeed == spd
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSel) HaneulBlue else Color.White,
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = androidx.compose.ui.graphics.SolidColor(if (isSel) HaneulBlue else Color(0xFFCBD5E1))
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { ttsSpeed = spd }
                        ) {
                            Text(
                                text = label,
                                fontSize = 10.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSel) Color.White else InkBlack,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }
        }

        when (mainWritingTab) {
            0 -> {
                LetterWritingPracticeView(
                    letters = letters,
                    initialLetter = initialLetter,
                    ttsSpeed = ttsSpeed,
                    onSpeak = onSpeak,
                    onStopSpeak = onStopSpeak,
                    onAddXp = onAddXp,
                    onRecordProgress = onRecordProgress
                )
            }
            1 -> {
                KoreanKeyboardPracticeView(
                    ttsSpeed = ttsSpeed,
                    onSpeak = onSpeak,
                    onAddXp = onAddXp,
                    onRecordProgress = onRecordProgress
                )
            }
            2 -> {
                SyllableCombinationWritingView(
                    ttsSpeed = ttsSpeed,
                    onSpeak = onSpeak,
                    onAddXp = onAddXp,
                    onRecordProgress = onRecordProgress
                )
            }
            3 -> {
                WordWritingPracticeView(
                    ttsSpeed = ttsSpeed,
                    onSpeak = onSpeak,
                    onAddXp = onAddXp,
                    onRecordProgress = onRecordProgress
                )
            }
            4 -> {
                KoreanDocumentPracticeView(
                    ttsSpeed = ttsSpeed,
                    onSpeak = onSpeak,
                    onAddXp = onAddXp,
                    onRecordProgress = onRecordProgress
                )
            }
            5 -> {
                BlindDictationQuizView(
                    letters = letters,
                    ttsSpeed = ttsSpeed,
                    onSpeak = onSpeak,
                    onAddXp = onAddXp,
                    onRecordProgress = onRecordProgress
                )
            }
            6 -> {
                BatchimGuideView(
                    onSpeak = { text -> onSpeak(text, ttsSpeed) },
                    onRecordProgress = onRecordProgress
                )
            }
        }
    }
}

/**
 * Tab 0: Latihan Menulis Huruf dengan Stroke Order & Auto-Play Audio Berurutan
 */
@Composable
fun LetterWritingPracticeView(
    letters: List<HangulLetter>,
    initialLetter: String? = null,
    ttsSpeed: Float,
    onSpeak: (String, Float) -> Unit,
    onStopSpeak: () -> Unit,
    onAddXp: (Int) -> Unit,
    onRecordProgress: (moduleId: String, lessonId: String, title: String, score: Int) -> Unit
) {
    val categories = listOf("Vokal Dasar", "Konsonan Dasar", "Vokal Ganda", "Konsonan Ganda")
    val initialCatIndex = remember(initialLetter) {
        val found = letters.firstOrNull { it.character == initialLetter }
        if (found != null) {
            val idx = categories.indexOf(found.category)
            if (idx >= 0) idx else 0
        } else 0
    }
    var selectedCategoryIndex by remember(initialCatIndex) { mutableIntStateOf(initialCatIndex) }
    val currentCategory = categories[selectedCategoryIndex]
    val filteredLetters = letters.filter { it.category == currentCategory }

    val initialLetterIdx = remember(initialLetter, selectedCategoryIndex) {
        val idx = filteredLetters.indexOfFirst { it.character == initialLetter }
        if (idx >= 0) idx else 0
    }
    var selectedLetterIndex by remember(selectedCategoryIndex, initialLetterIdx) { mutableIntStateOf(initialLetterIdx) }
    val safeLetterIndex = selectedLetterIndex.coerceIn(0, maxOf(0, filteredLetters.size - 1))
    val currentLetter = filteredLetters.getOrElse(safeLetterIndex) { filteredLetters.firstOrNull() ?: letters.first() }

    var wrongKeyWarning by remember(currentLetter.character) { mutableStateOf<String?>(null) }
    var latestScore by remember { mutableStateOf<Int?>(null) }
    var isShiftActive by remember { mutableStateOf(false) }
    var typedLetterJamos by remember(currentLetter.character) { mutableStateOf(listOf<Char>()) }
    var letterCompleted by remember(currentLetter.character) { mutableStateOf(false) }
    val letterTargetJamos = remember(currentLetter.character) {
        val decomp = HangulKeyboardUtil.decompose(currentLetter.character)
        if (decomp.isNotEmpty()) decomp else listOf(currentLetter.character.firstOrNull() ?: ' ')
    }
    val nextLetterKey = letterTargetJamos.getOrNull(typedLetterJamos.size)
    val composedLiveLetter = remember(typedLetterJamos) {
        HangulKeyboardUtil.compose(typedLetterJamos)
    }

    // Auto-Play Sequential Audio States
    var isAutoPlaying by remember { mutableStateOf(false) }
    var autoPlayProgress by remember { mutableFloatStateOf(0f) }
    var isLoopingEnabled by remember { mutableStateOf(false) }
    var autoPlayJob by remember { mutableStateOf<Job?>(null) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(selectedCategoryIndex) {
        autoPlayJob?.cancel()
        isAutoPlaying = false
        onStopSpeak()
    }

    DisposableEffect(Unit) {
        onDispose {
            autoPlayJob?.cancel()
            onStopSpeak()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Sub-tabs Kategori Huruf
        ScrollableTabRow(
            selectedTabIndex = selectedCategoryIndex,
            containerColor = Color.Transparent,
            contentColor = HaneulBlue,
            edgePadding = 0.dp,
            divider = {}
        ) {
            categories.forEachIndexed { idx, cat ->
                Tab(
                    selected = selectedCategoryIndex == idx,
                    onClick = {
                        autoPlayJob?.cancel()
                        onStopSpeak()
                        isAutoPlaying = false
                        selectedCategoryIndex = idx
                        selectedLetterIndex = 0
                        latestScore = null
                    },
                    text = {
                        Text(
                            text = cat,
                            fontWeight = if (selectedCategoryIndex == idx) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 12.sp
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // HERO BANNER: PUTAR SUARA BERURUTAN OTOMATIS (Sequential Chanting)
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isAutoPlaying) NunbitYellowContainer else Color.White
            ),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(
                    if (isAutoPlaying) NunbitYellow else Color(0xFFBFDBFE)
                )
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                if (isAutoPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = if (isAutoPlaying) Color(0xFF92400E) else HaneulBlue,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (selectedCategoryIndex == 0) "Lantunan 10 Vokal Dasar Berurutan" else "Putar $currentCategory Berurutan",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (isAutoPlaying) Color(0xFF78350F) else InkBlack
                            )
                        }
                        Text(
                            text = if (isAutoPlaying) "Melantunkan: ${currentLetter.character} [${currentLetter.romanization}] (${selectedLetterIndex + 1}/${filteredLetters.size})"
                            else "Putar semua bunyi urut otomatis (ㅏ ㅑ ㅓ ㅕ ㅗ...) untuk hafalan cepat",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Toggle Ulangi (Loop)
                        IconButton(
                            onClick = { isLoopingEnabled = !isLoopingEnabled },
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(if (isLoopingEnabled) NunbitYellow else Color(0xFFF1F5F9))
                        ) {
                            Icon(
                                Icons.Default.Loop,
                                contentDescription = "Ulangi terus",
                                tint = if (isLoopingEnabled) InkBlack else Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        Button(
                            onClick = {
                                if (isAutoPlaying) {
                                    autoPlayJob?.cancel()
                                    onStopSpeak()
                                    isAutoPlaying = false
                                } else {
                                    isAutoPlaying = true
                                    autoPlayJob = coroutineScope.launch {
                                        do {
                                            for (i in filteredLetters.indices) {
                                                if (!isActive) break
                                                selectedLetterIndex = i
                                                autoPlayProgress = (i + 1).toFloat() / filteredLetters.size
                                                val ltr = filteredLetters[i]
                                                onSpeak(ltr.soundSample, ttsSpeed)
                                                // Durasi jeda sesuai kecepatan audio
                                                val pauseMillis = (1300 / ttsSpeed).toLong()
                                                delay(pauseMillis)
                                            }
                                        } while (isLoopingEnabled && isActive)
                                        isAutoPlaying = false
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isAutoPlaying) SarangRed else HaneulBlue
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("auto_play_sequence_button")
                        ) {
                            Text(
                                text = if (isAutoPlaying) "Berhenti ⏹️" else "Putar Otomatis ▶️",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color.White
                            )
                        }
                    }
                }

                if (isAutoPlaying) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { autoPlayProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = HaneulBlue,
                        trackColor = Color(0xFFE2E8F0)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Pilihan Huruf Horizontal (LazyRow)
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 2.dp)
        ) {
            itemsIndexed(filteredLetters) { idx, letter ->
                val isSelected = letter.character == currentLetter.character
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isSelected) HaneulBlue else Color.White,
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(
                            if (isSelected) HaneulBlue else Color(0xFFE2E8F0)
                        )
                    ),
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable {
                            autoPlayJob?.cancel()
                            onStopSpeak()
                            isAutoPlaying = false
                            selectedLetterIndex = idx
                            latestScore = null
                            onSpeak(letter.soundSample, ttsSpeed)
                        }
                        .testTag("letter_choice_${letter.character}")
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = letter.character,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else InkBlack
                        )
                        Text(
                            text = letter.romanization,
                            fontSize = 10.sp,
                            color = if (isSelected) NunbitYellow else TextSecondary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Kartu Detail Huruf & Bunyi Pengucapan
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(20.dp))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = HaneulContainer,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = currentLetter.character,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = HaneulBlue
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "Huruf ${currentLetter.name} [${currentLetter.romanization}]",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = InkBlack
                        )
                        Text(
                            text = "Tuts [${HangulKeyboardUtil.getQwertyKeyHint(currentLetter.character.firstOrNull() ?: ' ')}] • ${if (HangulKeyboardUtil.isLeftHandConsonant(currentLetter.character.firstOrNull() ?: ' ')) "Konsonan Kiri" else "Vokal Kanan"} • ${currentLetter.description}",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            lineHeight = 15.sp
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Tombol Bunyi Huruf
                    Button(
                        onClick = { onSpeak(currentLetter.soundSample, ttsSpeed) },
                        colors = ButtonDefaults.buttonColors(containerColor = HaneulBlue),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Bunyi Huruf",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Bunyi 🔊", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Spacer(modifier = Modifier.height(10.dp))

        // AREA LATIHAN PENGETIKAN KEYBOARD 2-BEOL-SIK (MENGGANTIKAN KANVAS GAMBAR)
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(
                    if (letterCompleted) SaessakGreen else Color(0xFFE2E8F0)
                )
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Baris Target Huruf & Live Input Pengetikan
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Box Target Huruf
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Target Huruf",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = HaneulContainer,
                            modifier = Modifier.size(68.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = currentLetter.character,
                                    fontSize = 38.sp,
                                    fontWeight = FontWeight.Black,
                                    color = HaneulBlue
                                )
                            }
                        }
                    }

                    // Tanda Panah Ketik
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Keyboard,
                            contentDescription = "Ketik",
                            tint = if (letterCompleted) SaessakGreen else HaneulBlue,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = if (letterCompleted) "Selesai ✓" else "Ketik 👉",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (letterCompleted) SaessakGreen else HaneulBlue
                        )
                    }

                    // Box Hasil Pengetikan Pengguna
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Hasil Ketikan Anda",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (letterCompleted) Color(0xFFD1FAE5) else Color(0xFFF8FAFC),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = androidx.compose.ui.graphics.SolidColor(
                                    if (letterCompleted) SaessakGreen else Color(0xFFCBD5E1)
                                )
                            ),
                            modifier = Modifier.size(68.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = composedLiveLetter.ifEmpty { "..." },
                                    fontSize = 38.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (letterCompleted) Color(0xFF065F46) else InkBlack
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Roadmap Urutan Tuts Keyboard
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Urutan Tuts: ",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary
                    )
                    letterTargetJamos.forEachIndexed { idx, ch ->
                        val isDone = idx < typedLetterJamos.size
                        val isCurrent = idx == typedLetterJamos.size
                        val qwertyHint = HangulKeyboardUtil.getQwertyKeyHint(ch)
                        val isLeft = HangulKeyboardUtil.isLeftHandConsonant(ch)

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = when {
                                isDone -> Color(0xFFD1FAE5)
                                isCurrent -> NunbitYellowContainer
                                else -> Color(0xFFF1F5F9)
                            },
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = androidx.compose.ui.graphics.SolidColor(
                                    when {
                                        isDone -> SaessakGreen
                                        isCurrent -> NunbitYellow
                                        else -> Color(0xFFCBD5E1)
                                    }
                                )
                            ),
                            modifier = Modifier.padding(horizontal = 3.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${idx + 1}. $ch [$qwertyHint]",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = when {
                                        isDone -> Color(0xFF065F46)
                                        isCurrent -> Color(0xFF78350F)
                                        else -> InkBlack
                                    }
                                )
                                if (isDone) {
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        tint = SaessakGreen,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }

                        if (idx < letterTargetJamos.size - 1) {
                            Text(
                                text = "➔",
                                fontSize = 11.sp,
                                color = TextMuted,
                                modifier = Modifier.padding(horizontal = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Banner Status Keberhasilan / Panduan
                AnimatedVisibility(visible = letterCompleted) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFECFDF5),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFA7F3D0))
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = SaessakGreen,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Luar Biasa! Huruf '${currentLetter.character}' tuntas diketik (+10 XP) 🎉",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF065F46)
                            )
                        }
                    }
                }

                if (wrongKeyWarning != null) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFFEE2E2),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFFCA5A5))
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Clear, contentDescription = null, tint = SarangRed, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = wrongKeyWarning ?: "",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF991B1B),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else if (!letterCompleted) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFF8FAFC),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val activeKey = nextLetterKey
                        val activeQwerty = activeKey?.let { HangulKeyboardUtil.getQwertyKeyHint(it) } ?: ""
                        val isLeft = activeKey?.let { HangulKeyboardUtil.isLeftHandConsonant(it) } ?: false
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = NunbitYellow,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (activeKey != null) "Tekan tuts berkedip '$activeKey' (tombol QWERTY [$activeQwerty], tangan ${if (isLeft) "kiri konsonan" else "kanan vokal"})"
                                else "Ketik huruf di keyboard",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Virtual Keyboard 2-Beol-Sik Interaktif
        VirtualKorean2BeolSikKeyboard(
            isShiftActive = isShiftActive,
            highlightChar = if (!letterCompleted) nextLetterKey else null,
            onKeyPress = { pressedChar ->
                if (!letterCompleted && nextLetterKey != null) {
                    if (pressedChar == nextLetterKey) {
                        wrongKeyWarning = null
                        val updated = typedLetterJamos + pressedChar
                        typedLetterJamos = updated
                        if (updated.size >= letterTargetJamos.size) {
                            letterCompleted = true
                            latestScore = 100
                            onSpeak(currentLetter.soundSample, ttsSpeed)
                            onAddXp(10)
                            onRecordProgress("writing", currentLetter.character, "Ketik ${currentLetter.character}", 100)
                        }
                    } else {
                        val activeQwerty = HangulKeyboardUtil.getQwertyKeyHint(nextLetterKey)
                        wrongKeyWarning = "Salah tombol '$pressedChar'. Tekan tuts '$nextLetterKey' (tombol QWERTY [$activeQwerty])"
                    }
                }
            },
            onShiftToggle = { isShiftActive = !isShiftActive },
            onBackspace = {
                wrongKeyWarning = null
                if (typedLetterJamos.isNotEmpty()) {
                    typedLetterJamos = typedLetterJamos.dropLast(1)
                    letterCompleted = false
                }
            },
            onReset = {
                wrongKeyWarning = null
                typedLetterJamos = emptyList()
                letterCompleted = false
            },
            onSpeak = { onSpeak(currentLetter.soundSample, ttsSpeed) }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Navigasi Huruf Sebelumnya / Berikutnya
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = {
                    if (selectedLetterIndex > 0) {
                        selectedLetterIndex--
                        latestScore = null
                        val prevLetter = filteredLetters.getOrElse(selectedLetterIndex) { currentLetter }
                        onSpeak(prevLetter.soundSample, ttsSpeed)
                    }
                },
                enabled = selectedLetterIndex > 0,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE2E8F0)),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text("⬅️ Huruf Sebelumnya", color = if (selectedLetterIndex > 0) InkBlack else Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            Text(
                text = "${(selectedLetterIndex + 1).coerceAtMost(filteredLetters.size)} / ${filteredLetters.size}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary
            )

            Button(
                onClick = {
                    if (selectedLetterIndex < filteredLetters.size - 1) {
                        selectedLetterIndex++
                        latestScore = null
                        val nextLetter = filteredLetters.getOrElse(selectedLetterIndex) { currentLetter }
                        onSpeak(nextLetter.soundSample, ttsSpeed)
                    }
                },
                enabled = selectedLetterIndex < filteredLetters.size - 1,
                colors = ButtonDefaults.buttonColors(containerColor = HaneulBlue),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text("Huruf Berikutnya ➡️", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Kartu Prinsip Keyboard Korea 2-Beol-Sik
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Keyboard, contentDescription = null, tint = HaneulBlue, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Prinsip Keyboard 2-Beol-Sik (두벌식):",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = InkBlack
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = HaneulContainer
                    ) {
                        Text(
                            text = "Standar Korea",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = HaneulBlue,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFEFF6FF),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFBFDBFE))
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("🖐️ Tangan Kiri", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF1E40AF))
                            Text("자음 (Konsonan)", fontSize = 10.sp, color = TextSecondary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("ㄱ ㄴ ㄷ ㄹ ㅁ ㅂ ㅅ ㅇ ㅈ ㅊ ㅋ ㅌ ㅍ ㅎ", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = InkBlack)
                            Text("Q W E R T A S D F G Z X C V", fontSize = 9.sp, color = TextMuted)
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFFFFBEB),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFFDE68A))
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("🖐️ Tangan Kanan", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFFB45309))
                            Text("모음 (Vokal)", fontSize = 10.sp, color = TextSecondary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("ㅏ ㅑ ㅓ ㅕ ㅗ ㅛ ㅜ ㅠ ㅡ ㅣ ㅐ ㅔ", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = InkBlack)
                            Text("Y U I O P H J K L B N M", fontSize = 9.sp, color = TextMuted)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "💡 Tips: Gunakan tombol Shift (⇧) untuk mengetik konsonan kembar (ㄲ, ㄸ, ㅃ, ㅆ, ㅉ) dan vokal ganda (ㅒ, ㅖ).",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    lineHeight = 15.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))
    }
}

/**
 * Tab 1: Pelatihan Menyusun & Menulis Blok Suku Kata (Choseong + Jungseong + Jongseong)
 */
@Composable
fun SyllableCombinationWritingView(
    ttsSpeed: Float,
    onSpeak: (String, Float) -> Unit,
    onAddXp: (Int) -> Unit,
    onRecordProgress: (moduleId: String, lessonId: String, title: String, score: Int) -> Unit
) {
    val consonants = listOf("ㄱ", "ㄴ", "ㄷ", "ㄹ", "ㅁ", "ㅂ", "ㅅ", "ㅇ", "ㅈ", "ㅎ")
    val vowels = listOf("ㅏ", "ㅑ", "ㅓ", "ㅕ", "ㅗ", "ㅛ", "ㅜ", "ㅠ", "ㅡ", "ㅣ")
    val batchims = listOf(
        "" to "Tanpa Batchim",
        "ㄱ" to "ㄱ (k)",
        "ㄴ" to "ㄴ (n)",
        "ㄷ" to "ㄷ (t)",
        "ㄹ" to "ㄹ (l)",
        "ㅁ" to "ㅁ (m)",
        "ㅂ" to "ㅂ (p)",
        "ㅇ" to "ㅇ (ng)"
    )

    var selConsonant by remember { mutableStateOf("ㄱ") }
    var selVowel by remember { mutableStateOf("ㅏ") }
    var selBatchim by remember { mutableStateOf("") }

    // Hitung blok suku kata Hangul gabungan secara real-time
    val formedChar = remember(selConsonant, selVowel, selBatchim) {
        combineHangul(selConsonant, selVowel, selBatchim)
    }
    var wrongSyllableWarning by remember(formedChar) { mutableStateOf<String?>(null) }

    var isShiftActive by remember { mutableStateOf(false) }
    var typedSyllableJamos by remember(formedChar) { mutableStateOf(listOf<Char>()) }
    var syllableCompleted by remember(formedChar) { mutableStateOf(false) }
    val syllableTargetJamos = remember(formedChar) {
        HangulKeyboardUtil.decompose(formedChar)
    }
    val nextSyllableKey = syllableTargetJamos.getOrNull(typedSyllableJamos.size)
    val liveComposedSyllable = remember(typedSyllableJamos) {
        HangulKeyboardUtil.compose(typedSyllableJamos)
    }

    val comboMeaning = when (formedChar) {
        "가" -> "Ga (Pergi / Partikel Subjek) • Contoh: 가게 (toko), 가방 (tas)"
        "나" -> "Na (Aku / Saya santai) • Contoh: 나무 (pohon), 나비 (kupu-kupu)"
        "다" -> "Da (Semua / Akhiran kalimat) • Contoh: 다시 (lagi), 다리 (jembatan/kaki)"
        "라" -> "Ra/La • Contoh: 라면 (ramyeon), 라디오 (radio)"
        "마" -> "Ma • Contoh: 마음 (hati), 마늘 (bawang putih)"
        "바" -> "Ba • Contoh: 바다 (laut), 바나나 (pisang)"
        "사" -> "Sa (Empat / Membeli) • Contoh: 사과 (apel), 사람 (orang)"
        "아" -> "A (Ah / Anak) • Contoh: 아이 (anak), 아침 (pagi)"
        "자" -> "Ja (Ayo / Tidur) • Contoh: 자동차 (mobil), 모자 (topi)"
        "하" -> "Ha (Melakukan) • Contoh: 하늘 (langit), 하루 (sehari)"
        "고" -> "Go (Tinggi) • Contoh: 고기 (daging), 고양이 (kucing)"
        "노" -> "No (Kuning) • Contoh: 노래 (lagu), 노란색 (warna kuning)"
        "구" -> "Gu (Sembilan) • Contoh: 구름 (awan), 구두 (sepatu)"
        "무" -> "Mu (Lobak) • Contoh: 무료 (gratis), 무지개 (pelangi)"
        "비" -> "Bi (Hujan) • Contoh: 비누 (sabun), 비행기 (pesawat)"
        "한" -> "Han (Korea / Satu) • Contoh: 한국 (Korea), 한글 (Hangul)"
        "글" -> "Geul (Tulisan) • Contoh: 한글 (Aksara Korea)"
        "강" -> "Gang (Sungai) • Contoh: 한강 (Sungai Han)"
        "달" -> "Dal (Bulan) • Contoh: 달력 (kalender)"
        "봄" -> "Bom (Musim Semi) • Contoh: 봄날 (hari musim semi)"
        "밥" -> "Bap (Nasi / Makanan) • Contoh: 비빔밥 (bibimbap)"
        "산" -> "San (Gunung) • Contoh: 부산 (Busan), 설악산 (Gunung Seorak)"
        "집" -> "Jip (Rumah) • Contoh: 우리집 (rumah kami)"
        "물" -> "Mul (Air) • Contoh: 생수 (air mineral)"
        "불" -> "Bul (Api) • Contoh: 불고기 (bulgogi)"
        else -> "Suku kata $formedChar dibaca [$formedChar]"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Kartu Penjelasan Blok Hangul
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFE2E8F0))
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Konsep Blok Kotak Suku Kata Hangul (자모 결합):",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = HaneulBlue
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Aksara Korea tidak ditulis memanjang ke samping seperti latin, melainkan dirangkai dalam satu kesatuan kotak kuadran: Konsonan Awal (초성) + Vokal (중성) + Batchim Bawah (종성).",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    lineHeight = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 1. Pilih Konsonan Awal
        Text(
            text = "1. Pilih Konsonan Awal (초성 - Choseong):",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = InkBlack,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(6.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(consonants) { c ->
                val isSel = selConsonant == c
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSel) HaneulBlue else Color.White,
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(if (isSel) HaneulBlue else Color(0xFFCBD5E1))
                    ),
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable {
                            selConsonant = c
                            val ch = combineHangul(c, selVowel, selBatchim)
                            onSpeak(ch, ttsSpeed)
                        }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = c, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = if (isSel) Color.White else InkBlack)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 2. Pilih Vokal
        Text(
            text = "2. Pilih Vokal (중성 - Jungseong):",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = InkBlack,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(6.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(vowels) { v ->
                val isSel = selVowel == v
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSel) SaessakGreen else Color.White,
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(if (isSel) SaessakGreen else Color(0xFFCBD5E1))
                    ),
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable {
                            selVowel = v
                            val ch = combineHangul(selConsonant, v, selBatchim)
                            onSpeak(ch, ttsSpeed)
                        }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = v, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = if (isSel) Color.White else InkBlack)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 3. Pilih Batchim Bawah
        Text(
            text = "3. Pilih Konsonan Akhir Batchim (종성 - Jongseong / Opsional):",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = InkBlack,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(6.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(batchims) { (b, label) ->
                val isSel = selBatchim == b
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSel) NunbitYellowContainer else Color.White,
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(if (isSel) Color(0xFFCA8A04) else Color(0xFFCBD5E1))
                    ),
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable {
                            selBatchim = b
                            val ch = combineHangul(selConsonant, selVowel, b)
                            onSpeak(ch, ttsSpeed)
                        }
                ) {
                    Text(
                        text = label,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = if (isSel) Color(0xFF854D0E) else InkBlack,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Kartu Hasil Gabungan Suku Kata
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFFBFDBFE), RoundedCornerShape(20.dp))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = HaneulContainer,
                        modifier = Modifier.size(64.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(text = formedChar, fontSize = 34.sp, fontWeight = FontWeight.ExtraBold, color = HaneulBlue)
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = "$selConsonant + $selVowel ${if (selBatchim.isNotEmpty()) "+ $selBatchim" else ""} = $formedChar",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = InkBlack
                        )
                        Text(
                            text = comboMeaning,
                            fontSize = 11.sp,
                            color = TextSecondary,
                            lineHeight = 15.sp
                        )
                    }
                }

                Button(
                    onClick = { onSpeak(formedChar, ttsSpeed) },
                    colors = ButtonDefaults.buttonColors(containerColor = HaneulBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Dengar", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // AREA LATIHAN KETIK SUKU KATA GABUNGAN
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(
                    if (syllableCompleted) SaessakGreen else Color(0xFFE2E8F0)
                )
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Target Suku Kata", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = HaneulContainer,
                            modifier = Modifier.size(64.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(text = formedChar, fontSize = 34.sp, fontWeight = FontWeight.Black, color = HaneulBlue)
                            }
                        }
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Keyboard,
                            contentDescription = "Ketik",
                            tint = if (syllableCompleted) SaessakGreen else HaneulBlue,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = if (syllableCompleted) "Tuntas ✓" else "Ketik Blok 👉",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (syllableCompleted) SaessakGreen else HaneulBlue
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Rangkaian Ketikan", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (syllableCompleted) Color(0xFFD1FAE5) else Color(0xFFF8FAFC),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = androidx.compose.ui.graphics.SolidColor(
                                    if (syllableCompleted) SaessakGreen else Color(0xFFCBD5E1)
                                )
                            ),
                            modifier = Modifier.size(64.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = liveComposedSyllable.ifEmpty { "..." },
                                    fontSize = 34.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (syllableCompleted) Color(0xFF065F46) else InkBlack
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Panduan Urutan Tuts
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Ketik: ", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                    syllableTargetJamos.forEachIndexed { idx, ch ->
                        val isDone = idx < typedSyllableJamos.size
                        val isCurrent = idx == typedSyllableJamos.size
                        val qwertyHint = HangulKeyboardUtil.getQwertyKeyHint(ch)

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = when {
                                isDone -> Color(0xFFD1FAE5)
                                isCurrent -> NunbitYellowContainer
                                else -> Color(0xFFF1F5F9)
                            },
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = androidx.compose.ui.graphics.SolidColor(
                                    when {
                                        isDone -> SaessakGreen
                                        isCurrent -> NunbitYellow
                                        else -> Color(0xFFCBD5E1)
                                    }
                                )
                            ),
                            modifier = Modifier.padding(horizontal = 3.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${idx + 1}. $ch [$qwertyHint]",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = when {
                                        isDone -> Color(0xFF065F46)
                                        isCurrent -> Color(0xFF78350F)
                                        else -> InkBlack
                                    }
                                )
                                if (isDone) {
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Icon(Icons.Default.Check, contentDescription = null, tint = SaessakGreen, modifier = Modifier.size(11.dp))
                                }
                            }
                        }

                        if (idx < syllableTargetJamos.size - 1) {
                            Text(text = "➔", fontSize = 10.sp, color = TextMuted, modifier = Modifier.padding(horizontal = 2.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (wrongSyllableWarning != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFFEE2E2),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFFCA5A5))
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Clear, contentDescription = null, tint = SarangRed, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = wrongSyllableWarning ?: "",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF991B1B),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                AnimatedVisibility(visible = syllableCompleted) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFECFDF5),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFA7F3D0))
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SaessakGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Sempurna! Suku kata '$formedChar' berhasil dirangkai (+15 XP) 🎉",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF065F46)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Virtual Keyboard 2-Beol-Sik
        VirtualKorean2BeolSikKeyboard(
            isShiftActive = isShiftActive,
            highlightChar = if (!syllableCompleted) nextSyllableKey else null,
            onKeyPress = { pressedChar ->
                if (!syllableCompleted && nextSyllableKey != null) {
                    if (pressedChar == nextSyllableKey) {
                        wrongSyllableWarning = null
                        val updated = typedSyllableJamos + pressedChar
                        typedSyllableJamos = updated
                        if (updated.size >= syllableTargetJamos.size) {
                            syllableCompleted = true
                            onSpeak(formedChar, ttsSpeed)
                            onAddXp(15)
                            onRecordProgress("writing", formedChar, "Ketik Suku Kata $formedChar", 100)
                        }
                    } else {
                        val qwerty = HangulKeyboardUtil.getQwertyKeyHint(nextSyllableKey)
                        wrongSyllableWarning = "Salah tombol '$pressedChar'. Tekan tuts '$nextSyllableKey' (tombol QWERTY [$qwerty])"
                    }
                }
            },
            onShiftToggle = { isShiftActive = !isShiftActive },
            onBackspace = {
                wrongSyllableWarning = null
                if (typedSyllableJamos.isNotEmpty()) {
                    typedSyllableJamos = typedSyllableJamos.dropLast(1)
                    syllableCompleted = false
                }
            },
            onReset = {
                wrongSyllableWarning = null
                typedSyllableJamos = emptyList()
                syllableCompleted = false
            },
            onSpeak = { onSpeak(formedChar, ttsSpeed) }
        )

        Spacer(modifier = Modifier.height(28.dp))
    }
}

/**
 * Tab 2: Latihan Menulis Kata Dasar Bergambar
 */
@Composable
fun WordWritingPracticeView(
    ttsSpeed: Float,
    onSpeak: (String, Float) -> Unit,
    onAddXp: (Int) -> Unit,
    onRecordProgress: (moduleId: String, lessonId: String, title: String, score: Int) -> Unit
) {
    val wordList = listOf(
        HangulWordPractice("아이", "a-i", "Anak", "Orang", "👶", listOf("아", "이")),
        HangulWordPractice("우유", "u-yu", "Susu", "Makanan", "🥛", listOf("우", "유")),
        HangulWordPractice("오이", "o-i", "Mentimun", "Makanan", "🥒", listOf("오", "이")),
        HangulWordPractice("나무", "na-mu", "Pohon", "Alam", "🌳", listOf("나", "무")),
        HangulWordPractice("모자", "mo-ja", "Topi", "Benda", "🧢", listOf("모", "자")),
        HangulWordPractice("나비", "na-bi", "Kupu-kupu", "Hewan", "🦋", listOf("나", "비")),
        HangulWordPractice("바나나", "ba-na-na", "Pisang", "Makanan", "🍌", listOf("바", "나", "나")),
        HangulWordPractice("사랑", "sa-rang", "Cinta", "Perasaan", "❤️", listOf("사", "랑")),
        HangulWordPractice("하늘", "ha-neul", "Langit", "Alam", "🌤️", listOf("하", "늘")),
        HangulWordPractice("한국", "han-guk", "Korea", "Tempat", "🇰🇷", listOf("한", "국")),
        HangulWordPractice("친구", "chin-gu", "Teman", "Orang", "🤝", listOf("친", "구")),
        HangulWordPractice("학교", "hak-gyo", "Sekolah", "Tempat", "🏫", listOf("학", "교")),
        HangulWordPractice("사과", "sa-gwa", "Apel", "Makanan", "🍎", listOf("사", "과")),
        HangulWordPractice("물", "mul", "Air", "Minuman", "💧", listOf("물"))
    )

    var selectedWordIndex by remember { mutableIntStateOf(0) }
    val currentWord = wordList.getOrElse(selectedWordIndex) { wordList.first() }
    var selectedSyllableIndex by remember(selectedWordIndex) { mutableIntStateOf(0) }
    val currentSyllable = currentWord.syllables.getOrElse(selectedSyllableIndex) { currentWord.syllables.first() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Pilih Kata Dasar untuk Ditulis:",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = InkBlack,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            itemsIndexed(wordList) { idx, w ->
                val isSel = selectedWordIndex == idx
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSel) HaneulBlue else Color.White,
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(if (isSel) HaneulBlue else Color(0xFFE2E8F0))
                    ),
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            selectedWordIndex = idx
                            selectedSyllableIndex = 0
                            onSpeak(w.word, ttsSpeed)
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = w.iconEmoji, fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = w.word,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSel) Color.White else InkBlack
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Kartu Kata Utama
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(20.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = currentWord.iconEmoji, fontSize = 34.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = currentWord.word, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = InkBlack)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "[${currentWord.romanization}]", fontSize = 14.sp, color = TextSecondary)
                            }
                            Text(text = "Artinya: ${currentWord.meaningId}", fontSize = 13.sp, color = HaneulBlue, fontWeight = FontWeight.Bold)
                        }
                    }

                    Button(
                        onClick = { onSpeak(currentWord.word, ttsSpeed) },
                        colors = ButtonDefaults.buttonColors(containerColor = NunbitYellow),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, tint = InkBlack)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Pemilih Suku Kata / Kata
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Rincian suku kata:", fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                    currentWord.syllables.forEachIndexed { idx, s ->
                        val isCurr = selectedSyllableIndex == idx
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isCurr) HaneulBlue else Color(0xFFF1F5F9),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    selectedSyllableIndex = idx
                                    onSpeak(s, ttsSpeed)
                                }
                        ) {
                            Text(
                                text = "Ke-${idx + 1}: $s",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (isCurr) Color.White else InkBlack,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // State Ketik Kata
        var isShiftWord by remember { mutableStateOf(false) }
        var wrongWordWarning by remember(currentWord.word) { mutableStateOf<String?>(null) }
        var typedWordJamos by remember(currentWord.word) { mutableStateOf(listOf<Char>()) }
        var wordCompleted by remember(currentWord.word) { mutableStateOf(false) }
        val wordTargetJamos = remember(currentWord.word) {
            HangulKeyboardUtil.decompose(currentWord.word)
        }
        val nextWordKey = wordTargetJamos.getOrNull(typedWordJamos.size)
        val liveComposedWord = remember(typedWordJamos) {
            HangulKeyboardUtil.compose(typedWordJamos)
        }

        // AREA LATIHAN PENGETIKAN KATA LENGKAP
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(
                    if (wordCompleted) SaessakGreen else Color(0xFFE2E8F0)
                )
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Target Kata", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = HaneulContainer,
                            modifier = Modifier.size(72.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = currentWord.word,
                                    fontSize = if (currentWord.word.length > 2) 22.sp else 28.sp,
                                    fontWeight = FontWeight.Black,
                                    color = HaneulBlue
                                )
                            }
                        }
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Keyboard,
                            contentDescription = "Ketik",
                            tint = if (wordCompleted) SaessakGreen else HaneulBlue,
                            modifier = Modifier.size(26.dp)
                        )
                        Text(
                            text = if (wordCompleted) "Tuntas ✓" else "Ketik Kata 👉",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (wordCompleted) SaessakGreen else HaneulBlue
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Hasil Ketikan Anda", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (wordCompleted) Color(0xFFD1FAE5) else Color(0xFFF8FAFC),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = androidx.compose.ui.graphics.SolidColor(
                                    if (wordCompleted) SaessakGreen else Color(0xFFCBD5E1)
                                )
                            ),
                            modifier = Modifier.size(72.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = liveComposedWord.ifEmpty { "..." },
                                    fontSize = if (liveComposedWord.length > 2) 22.sp else 28.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (wordCompleted) Color(0xFF065F46) else InkBlack
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Rangkaian Tuts Huruf Kata
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Urutan Tuts: ", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                    wordTargetJamos.forEachIndexed { idx, ch ->
                        val isDone = idx < typedWordJamos.size
                        val isCurrent = idx == typedWordJamos.size
                        val qwertyHint = HangulKeyboardUtil.getQwertyKeyHint(ch)

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = when {
                                isDone -> Color(0xFFD1FAE5)
                                isCurrent -> NunbitYellowContainer
                                else -> Color(0xFFF1F5F9)
                            },
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = androidx.compose.ui.graphics.SolidColor(
                                    when {
                                        isDone -> SaessakGreen
                                        isCurrent -> NunbitYellow
                                        else -> Color(0xFFCBD5E1)
                                    }
                                )
                            ),
                            modifier = Modifier.padding(horizontal = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "$ch[$qwertyHint]",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = when {
                                        isDone -> Color(0xFF065F46)
                                        isCurrent -> Color(0xFF78350F)
                                        else -> InkBlack
                                    }
                                )
                                if (isDone) {
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Icon(Icons.Default.Check, contentDescription = null, tint = SaessakGreen, modifier = Modifier.size(10.dp))
                                }
                            }
                        }

                        if (idx < wordTargetJamos.size - 1) {
                            Text(text = "➔", fontSize = 9.sp, color = TextMuted, modifier = Modifier.padding(horizontal = 1.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (wrongWordWarning != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFFEE2E2),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFFCA5A5))
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Clear, contentDescription = null, tint = SarangRed, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = wrongWordWarning ?: "",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF991B1B),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                AnimatedVisibility(visible = wordCompleted) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFECFDF5),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFA7F3D0))
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SaessakGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Luar Biasa! Kata '${currentWord.word}' (${currentWord.meaningId}) tuntas diketik (+20 XP) 🎉",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF065F46)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Virtual Keyboard 2-Beol-Sik
        VirtualKorean2BeolSikKeyboard(
            isShiftActive = isShiftWord,
            highlightChar = if (!wordCompleted) nextWordKey else null,
            onKeyPress = { pressedChar ->
                if (!wordCompleted && nextWordKey != null) {
                    if (pressedChar == nextWordKey) {
                        wrongWordWarning = null
                        val updated = typedWordJamos + pressedChar
                        typedWordJamos = updated
                        if (updated.size >= wordTargetJamos.size) {
                            wordCompleted = true
                            onSpeak(currentWord.word, ttsSpeed)
                            onAddXp(20)
                            onRecordProgress("writing", currentWord.word, "Ketik Kata ${currentWord.word}", 100)
                        }
                    } else {
                        val qwerty = HangulKeyboardUtil.getQwertyKeyHint(nextWordKey)
                        wrongWordWarning = "Salah tombol '$pressedChar'. Tekan tuts '$nextWordKey' (tombol QWERTY [$qwerty])"
                    }
                }
            },
            onShiftToggle = { isShiftWord = !isShiftWord },
            onBackspace = {
                wrongWordWarning = null
                if (typedWordJamos.isNotEmpty()) {
                    typedWordJamos = typedWordJamos.dropLast(1)
                    wordCompleted = false
                }
            },
            onReset = {
                wrongWordWarning = null
                typedWordJamos = emptyList()
                wordCompleted = false
            },
            onSpeak = { onSpeak(currentWord.word, ttsSpeed) }
        )

        Spacer(modifier = Modifier.height(28.dp))
    }
}

/**
 * Tab 3: Kuis Dikte Huruf Tanpa Panduan (Blind Audio Dictation Test)
 */
@Composable
fun BlindDictationQuizView(
    letters: List<HangulLetter>,
    ttsSpeed: Float,
    onSpeak: (String, Float) -> Unit,
    onAddXp: (Int) -> Unit,
    onRecordProgress: (moduleId: String, lessonId: String, title: String, score: Int) -> Unit
) {
    val basicLetters = remember { letters.filter { it.category == "Vokal Dasar" || it.category == "Konsonan Dasar" } }
    var quizIndex by remember { mutableIntStateOf(0) }
    val currentQuiz = basicLetters.getOrElse(quizIndex) { basicLetters.first() }
    var isRevealed by remember { mutableStateOf(false) }
    var quizScore by remember { mutableStateOf<Int?>(null) }
    var streakCount by remember { mutableIntStateOf(0) }
    var isShiftQuiz by remember { mutableStateOf(false) }
    var typedQuizJamos by remember(currentQuiz.character) { mutableStateOf(listOf<Char>()) }
    val quizTargetJamos = remember(currentQuiz.character) {
        val decomp = HangulKeyboardUtil.decompose(currentQuiz.character)
        if (decomp.isNotEmpty()) decomp else listOf(currentQuiz.character.firstOrNull() ?: ' ')
    }
    val liveQuizComposed = remember(typedQuizJamos) {
        HangulKeyboardUtil.compose(typedQuizJamos)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(20.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Soal ${quizIndex + 1} / ${basicLetters.size}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                    Surface(shape = RoundedCornerShape(6.dp), color = NunbitYellowContainer) {
                        Text(text = "🔥 Streak: $streakCount", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = InkBlack, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Dengarkan audio, lalu ketikkan hurufnya menggunakan Keyboard 2-Beol-Sik!",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = InkBlack,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Tombol Putar Ulang Audio Kuis
                Button(
                    onClick = { onSpeak(currentQuiz.soundSample, ttsSpeed) },
                    colors = ButtonDefaults.buttonColors(containerColor = HaneulBlue),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Putar Bunyi Huruf 🔊", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }

                if (isRevealed) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFEFF6FF),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "Kunci Jawaban Huruf:", fontSize = 11.sp, color = TextSecondary)
                            Text(text = "${currentQuiz.character} (${currentQuiz.name} [${currentQuiz.romanization}])", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = HaneulBlue)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        val isAnswerCorrect = liveQuizComposed == currentQuiz.character

        // AREA INPUT KETIK DIKTE AUDIO
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(
                    if (isAnswerCorrect) SaessakGreen else Color(0xFFE2E8F0)
                )
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Ketikkan huruf yang Anda dengar:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = when {
                        isAnswerCorrect -> Color(0xFFD1FAE5)
                        liveQuizComposed.isNotEmpty() && !isAnswerCorrect && isRevealed -> Color(0xFFFEE2E2)
                        else -> Color(0xFFF8FAFC)
                    },
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(
                            when {
                                isAnswerCorrect -> SaessakGreen
                                liveQuizComposed.isNotEmpty() && !isAnswerCorrect && isRevealed -> SarangRed
                                else -> Color(0xFFCBD5E1)
                            }
                        )
                    ),
                    modifier = Modifier.size(80.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = liveQuizComposed.ifEmpty { "?" },
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Black,
                            color = when {
                                isAnswerCorrect -> Color(0xFF065F46)
                                liveQuizComposed.isNotEmpty() && !isAnswerCorrect && isRevealed -> SarangRed
                                else -> InkBlack
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (isAnswerCorrect) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFECFDF5),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFA7F3D0))
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SaessakGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Tepat sekali! Huruf '${currentQuiz.character}' (${currentQuiz.name}) benar! (+20 XP)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF065F46)
                            )
                        }
                    }
                } else if (!isRevealed) {
                    Text(
                        text = "Tekan tuts pada keyboard di bawah ini sesuai bunyi audio",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Virtual Keyboard 2-Beol-Sik untuk Kuis Dikte
        VirtualKorean2BeolSikKeyboard(
            isShiftActive = isShiftQuiz,
            highlightChar = if (isRevealed) quizTargetJamos.firstOrNull() else null,
            onKeyPress = { pressedChar ->
                if (!isAnswerCorrect) {
                    val updated = typedQuizJamos + pressedChar
                    typedQuizJamos = updated
                    val composed = HangulKeyboardUtil.compose(updated)
                    if (composed == currentQuiz.character) {
                        isRevealed = true
                        quizScore = 100
                        streakCount++
                        onAddXp(20)
                        onRecordProgress("writing", "quiz_${currentQuiz.character}", "Kuis Dikte Ketik ${currentQuiz.character}", 100)
                    }
                }
            },
            onShiftToggle = { isShiftQuiz = !isShiftQuiz },
            onBackspace = {
                if (typedQuizJamos.isNotEmpty()) {
                    typedQuizJamos = typedQuizJamos.dropLast(1)
                }
            },
            onReset = {
                typedQuizJamos = emptyList()
            },
            onSpeak = { onSpeak(currentQuiz.soundSample, ttsSpeed) }
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = { isRevealed = true },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(if (isRevealed) "Kunci Terbuka" else "Buka Kunci")
            }

            Button(
                onClick = {
                    if (basicLetters.isNotEmpty()) {
                        quizIndex = (quizIndex + 1) % basicLetters.size
                        isRevealed = false
                        quizScore = null
                        typedQuizJamos = emptyList()
                        val next = basicLetters.getOrElse(quizIndex) { basicLetters.first() }
                        onSpeak(next.soundSample, ttsSpeed)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = HaneulBlue),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Soal Berikutnya ➡️", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(28.dp))
    }
}

/**
 * Rumus Resmi Unicode Hangul:
 * 0xAC00 + (choseongIndex * 21 * 28) + (jungseongIndex * 28) + jongseongIndex
 */
fun combineHangul(initial: String, medial: String, final: String): String {
    val initialConsonants = listOf("ㄱ", "ㄲ", "ㄴ", "ㄷ", "ㄸ", "ㄹ", "ㅁ", "ㅂ", "ㅃ", "ㅅ", "ㅆ", "ㅇ", "ㅈ", "ㅉ", "ㅊ", "ㅋ", "ㅌ", "ㅍ", "ㅎ")
    val medialVowels = listOf("ㅏ", "ㅐ", "ㅑ", "ㅒ", "ㅓ", "ㅔ", "ㅕ", "ㅖ", "ㅗ", "ㅘ", "ㅙ", "ㅚ", "ㅛ", "ㅜ", "ㅝ", "ㅞ", "ㅟ", "ㅠ", "ㅡ", "ㅢ", "ㅣ")
    val finalConsonants = listOf("", "ㄱ", "ㄲ", "ㄳ", "ㄴ", "ㄵ", "ㄶ", "ㄷ", "ㄹ", "ㄺ", "ㄻ", "ㄼ", "ㄽ", "ㄾ", "ㄿ", "ㅀ", "ㅁ", "ㅂ", "ㅄ", "ㅅ", "ㅆ", "ㅇ", "ㅈ", "ㅊ", "ㅋ", "ㅌ", "ㅍ", "ㅎ")

    val iIndex = initialConsonants.indexOf(initial)
    val mIndex = medialVowels.indexOf(medial)
    val fIndex = if (final.isNotEmpty()) finalConsonants.indexOf(final) else 0

    if (iIndex != -1 && mIndex != -1 && fIndex != -1) {
        val unicode = 0xAC00 + (iIndex * 21 * 28) + (mIndex * 28) + fIndex
        return unicode.toChar().toString()
    }
    return "$initial$medial$final"
}
