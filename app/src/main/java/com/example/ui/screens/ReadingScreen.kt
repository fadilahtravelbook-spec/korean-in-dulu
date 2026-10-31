package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ReadingStory
import com.example.ui.components.AppSubHeader
import com.example.ui.components.AudioSpeakerButton
import com.example.ui.theme.HaneulBlue
import com.example.ui.theme.HaneulContainer
import com.example.ui.theme.InkBlack
import com.example.ui.theme.NunbitYellow
import com.example.ui.theme.SaessakGreen
import com.example.ui.theme.SarangRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary

@Composable
fun ReadingScreen(
    stories: List<ReadingStory>,
    onBack: () -> Unit,
    onSpeak: (String) -> Unit,
    onSaveToVocab: (korean: String, meaning: String) -> Unit,
    onRecordProgress: (moduleId: String, lessonId: String, title: String, score: Int) -> Unit
) {
    val story = stories.firstOrNull() ?: return
    var activeTab by remember { mutableIntStateOf(0) } // 0: Cerita Utama, 1: Tempel Teks Kustom, 2: Rekam Baca Aloud (WPM)

    var activeSentenceIndex by remember { mutableStateOf<Int?>(null) }
    var selectedWordPair by remember { mutableStateOf<Pair<String, String>?>(null) }
    var wordSavedSuccess by remember { mutableStateOf(false) }

    // Custom Paste State
    var customText by remember { mutableStateOf("안녕하세요. 저는 한국 드라마를 좋아합니다. 한국어를 매일 공부합니다.") }

    // Reading Aloud state
    var isReadingAloudRecording by remember { mutableStateOf(false) }
    var readingAloudScore by remember { mutableStateOf<ReadingAloudMetric?>(null) }

    // Quiz state
    var showQuiz by remember { mutableStateOf(false) }
    var quizAnswers by remember { mutableStateOf(mapOf<Int, Int>()) }
    var quizSubmitted by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        AppSubHeader(
            title = "Membaca (읽기 연습)",
            subtitle = "Karaoke highlight reader, kamus kata & tes reading aloud",
            onBack = onBack
        )

        TabRow(
            selectedTabIndex = activeTab,
            containerColor = Color.White,
            contentColor = HaneulBlue
        ) {
            Tab(selected = activeTab == 0, onClick = { activeTab = 0 }, text = { Text("Cerita Seoul", fontWeight = FontWeight.Bold, fontSize = 12.sp) })
            Tab(selected = activeTab == 1, onClick = { activeTab = 1 }, text = { Text("Paste Teks", fontWeight = FontWeight.Bold, fontSize = 12.sp) })
            Tab(selected = activeTab == 2, onClick = { activeTab = 2 }, text = { Text("Reading Aloud", fontWeight = FontWeight.Bold, fontSize = 12.sp) })
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (activeTab) {
                0 -> {
                    // Story Header Card
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
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Surface(shape = RoundedCornerShape(6.dp), color = HaneulContainer) {
                                    Text(
                                        text = "Level: ${story.level}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = HaneulBlue,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = story.titleKo, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = InkBlack)
                                Text(text = story.titleId, fontSize = 13.sp, color = TextSecondary)
                            }

                            AudioSpeakerButton(
                                onClick = {
                                    val allText = story.paragraphs.joinToString(" ") { it.sentenceKo }
                                    onSpeak(allText)
                                },
                                label = "Baca Penuh"
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Paragraphs with Karaoke Highlighting
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(20.dp))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Teks Cerita (Ketuk kalimat untuk mendengar; ketuk kata untuk simpan ke SRS):",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            story.paragraphs.forEachIndexed { idx, p ->
                                val isHighlighted = activeSentenceIndex == idx
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (isHighlighted) HaneulContainer else Color(0xFFF8FAFC),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .clickable {
                                            activeSentenceIndex = idx
                                            onSpeak(p.sentenceKo)
                                        }
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(text = p.sentenceKo, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = if (isHighlighted) HaneulBlue else InkBlack, lineHeight = 24.sp)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(text = p.sentenceRo, fontSize = 12.sp, color = NunbitYellow, fontWeight = FontWeight.SemiBold)
                                        Text(text = p.sentenceId, fontSize = 13.sp, color = TextSecondary)

                                        if (p.keyWords.isNotEmpty()) {
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                p.keyWords.forEach { kw ->
                                                    Surface(
                                                        shape = RoundedCornerShape(8.dp),
                                                        color = Color.White,
                                                        border = CardDefaults.outlinedCardBorder().copy(
                                                            brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFCBD5E1))
                                                        ),
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(8.dp))
                                                            .clickable {
                                                                selectedWordPair = kw
                                                                wordSavedSuccess = false
                                                                onSpeak(kw.first)
                                                            }
                                                    ) {
                                                        Text(
                                                            text = "${kw.first} (${kw.second})",
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = HaneulBlue,
                                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Word Glossary Card with "Simpan ke SRS"
                    selectedWordPair?.let { kw ->
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFFFEF9C3),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = "Kamus Cepat: ${kw.first}", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF854D0E))
                                    Text(text = "Artinya: ${kw.second}", fontSize = 13.sp, color = Color(0xFF713F12))
                                    if (wordSavedSuccess) {
                                        Text(text = "✅ Tersimpan ke kartu SRS!", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF15803D))
                                    }
                                }

                                Button(
                                    onClick = {
                                        onSaveToVocab(kw.first, kw.second)
                                        wordSavedSuccess = true
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = HaneulBlue),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Icon(Icons.Default.BookmarkAdd, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Simpan SRS", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Comprehension Quiz
                    if (!showQuiz) {
                        Button(
                            onClick = { showQuiz = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("open_reading_quiz_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = HaneulBlue),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Default.Quiz, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Uji Pemahaman Bacaan (3 Pertanyaan)", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(18.dp))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(text = "Pertanyaan Pemahaman Cerita:", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = InkBlack)
                                Spacer(modifier = Modifier.height(10.dp))

                                story.questions.forEachIndexed { qIdx, question ->
                                    Text(text = "${qIdx + 1}. ${question.question}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = InkBlack)
                                    Spacer(modifier = Modifier.height(4.dp))

                                    question.options.forEachIndexed { optIdx, opt ->
                                        val isSelected = quizAnswers[qIdx] == optIdx
                                        val isCorrect = optIdx == question.correctIndex
                                        val bg = when {
                                            quizSubmitted && isCorrect -> Color(0xFFD1FAE5)
                                            quizSubmitted && isSelected && !isCorrect -> Color(0xFFFEE2E2)
                                            isSelected -> HaneulContainer
                                            else -> Color(0xFFF8FAFC)
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = bg,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 3.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .clickable(enabled = !quizSubmitted) {
                                                    quizAnswers = quizAnswers + (qIdx to optIdx)
                                                }
                                        ) {
                                            Text(text = opt, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, modifier = Modifier.padding(10.dp))
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                }

                                if (!quizSubmitted) {
                                    Button(
                                        onClick = {
                                            quizSubmitted = true
                                            val correctCount = story.questions.indices.count { idx ->
                                                quizAnswers[idx] == story.questions[idx].correctIndex
                                            }
                                            val score = (correctCount * 100) / story.questions.size
                                            onRecordProgress("reading", story.id, "Membaca: ${story.titleId}", score)
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(46.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = HaneulBlue),
                                        shape = RoundedCornerShape(12.dp),
                                        enabled = quizAnswers.size == story.questions.size
                                    ) {
                                        Text("Kirim Jawaban", color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // Tab 1: Custom Text Paste
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(20.dp))
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(text = "Tempel Teks Korea Sendiri:", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = InkBlack)
                            Text(text = "Tempel artikel berita, lirik lagu, atau bacaan apa saja untuk dibacakan oleh TTS", fontSize = 12.sp, color = TextSecondary)
                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = customText,
                                onValueChange = { customText = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp),
                                shape = RoundedCornerShape(14.dp)
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Button(
                                    onClick = { onSpeak(customText) },
                                    colors = ButtonDefaults.buttonColors(containerColor = HaneulBlue),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.VolumeUp, contentDescription = null, tint = Color.White)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Bacakan dengan TTS", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // Tab 2: Reading Aloud (WPM & Accuracy)
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
                                .padding(18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "Latihan Membaca Keras (Reading Aloud)", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = InkBlack)
                            Text(text = "Sistem akan menghitung kecepatan baca (WPM) dan akurasi pelafalan", fontSize = 12.sp, color = TextSecondary)

                            Spacer(modifier = Modifier.height(14.dp))

                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color(0xFFF8FAFC),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "오늘 저는 한국 서울에 도착했습니다. 날씨가 조금 춥지만 공기가 맑습니다.",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = InkBlack,
                                    modifier = Modifier.padding(14.dp),
                                    lineHeight = 24.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Surface(
                                shape = CircleShape,
                                color = if (isReadingAloudRecording) SarangRed else HaneulBlue,
                                modifier = Modifier
                                    .size(68.dp)
                                    .clip(CircleShape)
                                    .clickable {
                                        if (!isReadingAloudRecording) {
                                            isReadingAloudRecording = true
                                            readingAloudScore = null
                                        } else {
                                            isReadingAloudRecording = false
                                            readingAloudScore = ReadingAloudMetric(
                                                wpm = 86,
                                                accuracy = 94,
                                                pauses = 1,
                                                feedback = "Kecepatan membaca sangat stabil dan lancar!"
                                            )
                                            onRecordProgress("reading", "reading_aloud_1", "Reading Aloud Seoul", 94)
                                        }
                                    }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        if (isReadingAloudRecording) Icons.Default.Stop else Icons.Default.Mic,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(34.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = if (isReadingAloudRecording) "Sedang Merekam... Tekan untuk Analisis" else "Tekan untuk Mulai Membaca",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isReadingAloudRecording) SarangRed else HaneulBlue
                            )

                            readingAloudScore?.let { metric ->
                                Spacer(modifier = Modifier.height(16.dp))
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = Color(0xFFF0FDF4),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(text = "Kecepatan: ${metric.wpm} WPM", fontWeight = FontWeight.Bold, color = Color(0xFF166534), fontSize = 14.sp)
                                            Text(text = "Akurasi: ${metric.accuracy}%", fontWeight = FontWeight.Bold, color = Color(0xFF166534), fontSize = 14.sp)
                                            Text(text = "Jeda: ${metric.pauses}x", fontWeight = FontWeight.Bold, color = Color(0xFF166534), fontSize = 14.sp)
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(text = metric.feedback, fontSize = 12.sp, color = Color(0xFF15803D))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

data class ReadingAloudMetric(
    val wpm: Int,
    val accuracy: Int,
    val pauses: Int,
    val feedback: String
)
