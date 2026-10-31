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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import com.example.data.model.ListeningQuiz
import com.example.ui.components.AppSubHeader
import com.example.ui.theme.HaneulBlue
import com.example.ui.theme.HaneulContainer
import com.example.ui.theme.InkBlack
import com.example.ui.theme.NunbitYellow
import com.example.ui.theme.SaessakGreen
import com.example.ui.theme.SarangRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary

@Composable
fun ListeningScreen(
    quizzes: List<ListeningQuiz>,
    onBack: () -> Unit,
    onSpeak: (String, Float) -> Unit,
    onRecordProgress: (moduleId: String, lessonId: String, title: String, score: Int) -> Unit
) {
    var modeTab by remember { mutableIntStateOf(0) } // 0: Quiz Pilihan Ganda, 1: Dikte Tulisan
    var currentQuizIndex by remember { mutableIntStateOf(0) }
    val currentQuiz = quizzes.getOrElse(currentQuizIndex) { quizzes.first() }

    var speechRate by remember { mutableFloatStateOf(0.9f) }
    var showTranscript by remember { mutableStateOf(false) }
    var selectedOptionIndex by remember { mutableStateOf<Int?>(null) }
    var hasAnswered by remember { mutableStateOf(false) }

    // Dictation state
    var dictationInput by remember { mutableStateOf("") }
    var dictationChecked by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        AppSubHeader(
            title = "Mendengar (듣기)",
            subtitle = "Latihan pemahaman audio, kontrol kecepatan, dan dikte",
            onBack = onBack
        )

        TabRow(
            selectedTabIndex = modeTab,
            containerColor = Color.White,
            contentColor = HaneulBlue
        ) {
            Tab(
                selected = modeTab == 0,
                onClick = { modeTab = 0 },
                text = { Text("Quiz Pemahaman", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
            )
            Tab(
                selected = modeTab == 1,
                onClick = { modeTab = 1 },
                text = { Text("Dikte Tulisan", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Audio Player Control Card
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(22.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Audio Korea Asli",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = HaneulBlue
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Big Play Button
                    Surface(
                        shape = CircleShape,
                        color = HaneulBlue,
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .clickable {
                                onSpeak(currentQuiz.spokenKorean, speechRate)
                            }
                            .testTag("play_audio_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.VolumeUp,
                                contentDescription = "Putar Audio",
                                tint = Color.White,
                                modifier = Modifier.size(38.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Speed controls & Repeat & Transcript toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Speed pills: 0.5x, 0.75x, 1.0x, 1.25x
                        listOf(0.5f, 0.75f, 1.0f, 1.25f).forEach { spd ->
                            val isSel = speechRate == spd
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSel) HaneulBlue else Color(0xFFF1F5F9),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { speechRate = spd }
                            ) {
                                Text(
                                    text = "${spd}x",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) Color.White else InkBlack,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                )
                            }
                        }

                        // Transcript Toggle
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (showTranscript) HaneulContainer else Color(0xFFF1F5F9),
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { showTranscript = !showTranscript }
                                .testTag("toggle_transcript_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    if (showTranscript) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null,
                                    tint = HaneulBlue,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (showTranscript) "Tutup" else "Transkrip",
                                    fontSize = 11.sp,
                                    color = HaneulBlue,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Revealed Transcript
                    AnimatedVisibility(visible = showTranscript) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF8FAFC))
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = currentQuiz.spokenKorean,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = InkBlack
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = currentQuiz.romanization,
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            if (modeTab == 0) {
                // Mode 0: Quiz Pilihan Ganda
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(20.dp))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = currentQuiz.question,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = InkBlack
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        currentQuiz.options.forEachIndexed { idx, opt ->
                            val isSelected = selectedOptionIndex == idx
                            val isCorrect = idx == currentQuiz.correctIndex

                            val bgColor = when {
                                hasAnswered && isCorrect -> Color(0xFFD1FAE5)
                                hasAnswered && isSelected && !isCorrect -> Color(0xFFFEE2E2)
                                isSelected -> HaneulContainer
                                else -> Color(0xFFF8FAFC)
                            }

                            val textColor = when {
                                hasAnswered && isCorrect -> Color(0xFF065F46)
                                hasAnswered && isSelected && !isCorrect -> Color(0xFF991B1B)
                                isSelected -> HaneulBlue
                                else -> InkBlack
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = bgColor,
                                border = CardDefaults.outlinedCardBorder().copy(
                                    brush = androidx.compose.ui.graphics.SolidColor(
                                        if (isSelected) HaneulBlue else Color(0xFFE2E8F0)
                                    )
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 5.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable(enabled = !hasAnswered) {
                                        selectedOptionIndex = idx
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = ('A' + idx).toString(),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = textColor
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = opt,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = textColor
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        if (!hasAnswered) {
                            Button(
                                onClick = {
                                    hasAnswered = true
                                    val isCorrect = selectedOptionIndex == currentQuiz.correctIndex
                                    onRecordProgress(
                                        "listening",
                                        currentQuiz.id,
                                        "Mendengar Quiz ${currentQuizIndex + 1}",
                                        if (isCorrect) 100 else 40
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("submit_listening_answer_button"),
                                enabled = selectedOptionIndex != null,
                                colors = ButtonDefaults.buttonColors(containerColor = HaneulBlue),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Periksa Jawaban", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            // Explanation
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFEFF6FF),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "💡 ${currentQuiz.explanation}",
                                    fontSize = 12.sp,
                                    color = HaneulBlue,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Button(
                                onClick = {
                                    hasAnswered = false
                                    selectedOptionIndex = null
                                    showTranscript = false
                                    currentQuizIndex = (currentQuizIndex + 1) % quizzes.size
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("next_listening_quiz_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = HaneulBlue),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Lanjut ke Soal Berikutnya (${currentQuizIndex + 1}/${quizzes.size}) ➡️", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                // Mode 1: Dikte Tulisan (Dictation)
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(20.dp))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Dikte Tulisan (Dengar lalu ketik):",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = InkBlack
                        )
                        Text(
                            text = "Dengarkan audio di atas lalu ketik kalimat dalam huruf Hangul",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = dictationInput,
                            onValueChange = {
                                dictationInput = it
                                dictationChecked = false
                            },
                            placeholder = { Text("Ketik dalam huruf Hangul di sini...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("dictation_input_field"),
                            shape = RoundedCornerShape(14.dp)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                dictationChecked = true
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("check_dictation_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = HaneulBlue),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Cocokkan dengan Kunci Dikte", color = Color.White, fontWeight = FontWeight.Bold)
                        }

                        if (dictationChecked) {
                            Spacer(modifier = Modifier.height(14.dp))
                            val isMatch = dictationInput.trim().replace(" ", "") == currentQuiz.spokenKorean.trim().replace(" ", "")
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isMatch) Color(0xFFD1FAE5) else Color(0xFFFEF3C7),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = if (isMatch) "🎉 Hebat! Tulisan Anda 100% Cocok!" else "✍️ Perhatikan perbedaannya:",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = if (isMatch) Color(0xFF065F46) else Color(0xFF92400E)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = "Kunci: ${currentQuiz.spokenKorean}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = InkBlack)
                                    Text(text = "Tulisan Anda: ${dictationInput.ifBlank { "(kosong)" }}", fontSize = 13.sp, color = TextSecondary)

                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(text = "Kesesuaian Karakter Hangul:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(modifier = Modifier.fillMaxWidth()) {
                                        val cleanKey = currentQuiz.spokenKorean.replace(" ", "")
                                        val cleanInput = dictationInput.replace(" ", "")
                                        cleanKey.forEachIndexed { idx, char ->
                                            val userChar = cleanInput.getOrNull(idx)
                                            val isCorrect = userChar == char
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = if (isCorrect) Color(0xFFD1FAE5) else Color(0xFFFEE2E2),
                                                modifier = Modifier.padding(horizontal = 2.dp)
                                            ) {
                                                Text(
                                                    text = char.toString(),
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    color = if (isCorrect) Color(0xFF065F46) else Color(0xFF991B1B),
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = {
                                    if (currentQuizIndex > 0) {
                                        currentQuizIndex--
                                        dictationInput = ""
                                        dictationChecked = false
                                    }
                                },
                                enabled = currentQuizIndex > 0,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE2E8F0)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("⬅️ Soal Sebelumnya", color = if (currentQuizIndex > 0) InkBlack else Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            Text(
                                text = "Soal ${currentQuizIndex + 1} / ${quizzes.size}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary
                            )

                            Button(
                                onClick = {
                                    currentQuizIndex = (currentQuizIndex + 1) % quizzes.size
                                    dictationInput = ""
                                    dictationChecked = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = HaneulBlue),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Soal Berikutnya ➡️", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
