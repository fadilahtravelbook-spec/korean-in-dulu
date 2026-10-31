package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.repository.PlacementQuestion
import com.example.ui.theme.HaneulBlue
import com.example.ui.theme.HaneulContainer
import com.example.ui.theme.InkBlack
import com.example.ui.theme.NunbitYellow
import com.example.ui.theme.SaessakGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.runtime.mutableStateMapOf
import com.example.ui.theme.SarangRed

@Composable
fun OnboardingScreen(
    placementQuestions: List<PlacementQuestion>,
    onComplete: (name: String, level: Int) -> Unit
) {
    // Stage: 0 = Intro Slides, 1 = Placement Test, 2 = Result & Name Input
    var stage by remember { mutableIntStateOf(0) }
    var currentSlide by remember { mutableIntStateOf(0) }

    // Placement test state
    var currentQuestionIndex by remember { mutableIntStateOf(0) }
    var selectedOptionIndex by remember { mutableStateOf<Int?>(null) }
    var scoreCount by remember { mutableIntStateOf(0) }
    val userAnswers = remember { mutableStateMapOf<Int, Int>() }
    var showDetailedReview by remember { mutableStateOf(false) }

    // Name & Target input state
    var userName by remember { mutableStateOf("Sahabat Korea") }
    var selectedGoalMinutes by remember { mutableIntStateOf(15) }

    val slides = listOf(
        Triple(
            "Belajar Korea dari Nol!",
            "Kuasai abjad Hangul langkah demi langkah dengan panduan stroke interaktif dan animasi.",
            "화이팅! Bersama maskot Hannie, belajar jadi seru dan mudah dipahami."
        ),
        Triple(
            "Latihan Bicara & Dengar",
            "Template perkenalan diri dinamis dan evaluasi pengucapan menyerupai penutur asli Korea.",
            "Tersedia modul speaking, listening quiz, dan rekaman latihan."
        ),
        Triple(
            "Metode SRS Spaced Repetition",
            "Hafalkan ratusan kosakata penting dengan algoritma cerdas yang menjamin ingatan jangka panjang.",
            "Tidak perlu khawatir lupa, aplikasi akan menjadwalkan review tepat waktu!"
        )
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF8FAFC)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            when (stage) {
                0 -> {
                    // Stage 0: Intro Slides
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Spacer(modifier = Modifier.height(20.dp))
                        Image(
                            painter = painterResource(id = R.drawable.ic_logo_korean_in_dulu),
                            contentDescription = "Korean In Dulu",
                            modifier = Modifier
                                .height(68.dp)
                                .fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Image(
                            painter = painterResource(id = R.drawable.ic_mascot_hannie),
                            contentDescription = "Maskot Hannie",
                            modifier = Modifier
                                .size(170.dp)
                                .clip(RoundedCornerShape(24.dp)),
                            contentScale = ContentScale.Fit
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = slides[currentSlide].first,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = HaneulBlue,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = slides[currentSlide].second,
                            fontSize = 14.sp,
                            color = InkBlack,
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = slides[currentSlide].third,
                            fontSize = 12.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Dots indicator
                        Row(horizontalArrangement = Arrangement.Center) {
                            slides.indices.forEach { index ->
                                Box(
                                    modifier = Modifier
                                        .padding(4.dp)
                                        .size(if (index == currentSlide) 12.dp else 8.dp)
                                        .clip(CircleShape)
                                        .background(if (index == currentSlide) HaneulBlue else Color(0xFFCBD5E1))
                                )
                            }
                        }
                    }

                    Column(modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = {
                                if (currentSlide < slides.size - 1) {
                                    currentSlide++
                                } else {
                                    stage = 1 // Start Placement Test
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("onboarding_next_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = HaneulBlue),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = if (currentSlide < slides.size - 1) "Lanjut" else "Mulai Tes Penempatan Level (10 Soal)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color.White
                            )
                        }

                        if (currentSlide == slides.size - 1) {
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedButton(
                                onClick = {
                                    stage = 2 // Skip directly to name setup
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("skip_placement_button"),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text("Saya Pemula Total (Lewati Tes)", color = TextSecondary, fontSize = 13.sp)
                            }
                        }
                    }
                }

                1 -> {
                    // Stage 1: Placement Test (10 Questions)
                    val q = placementQuestions[currentQuestionIndex]
                    val progress = (currentQuestionIndex + 1) / placementQuestions.size.toFloat()

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Tes Penempatan Level",
                                fontWeight = FontWeight.Bold,
                                color = HaneulBlue,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Soal ${currentQuestionIndex + 1}/${placementQuestions.size}",
                                fontWeight = FontWeight.SemiBold,
                                color = TextSecondary,
                                fontSize = 13.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = NunbitYellow,
                            trackColor = Color(0xFFE2E8F0)
                        )

                        Spacer(modifier = Modifier.height(28.dp))

                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(20.dp))
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(
                                    text = q.question,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = InkBlack,
                                    lineHeight = 24.sp
                                )

                                Spacer(modifier = Modifier.height(20.dp))

                                q.options.forEachIndexed { idx, opt ->
                                    val isSelected = selectedOptionIndex == idx
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = if (isSelected) HaneulContainer else Color(0xFFF8FAFC),
                                        border = CardDefaults.outlinedCardBorder().copy(
                                            brush = androidx.compose.ui.graphics.SolidColor(
                                                if (isSelected) HaneulBlue else Color(0xFFE2E8F0)
                                            )
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 5.dp)
                                            .clip(RoundedCornerShape(14.dp))
                                            .clickable { selectedOptionIndex = idx }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(14.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = ('A' + idx).toString(),
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) HaneulBlue else TextSecondary,
                                                fontSize = 14.sp
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Text(
                                                text = opt,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) HaneulBlue else InkBlack,
                                                fontSize = 14.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            val chosen = selectedOptionIndex ?: -1
                            userAnswers[currentQuestionIndex] = chosen
                            if (chosen == q.correctIndex) {
                                scoreCount++
                            }
                            selectedOptionIndex = null
                            if (currentQuestionIndex < placementQuestions.size - 1) {
                                currentQuestionIndex++
                            } else {
                                stage = 2 // Proceed to Name & Setup
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("submit_placement_answer_button"),
                        enabled = selectedOptionIndex != null,
                        colors = ButtonDefaults.buttonColors(containerColor = HaneulBlue),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = if (currentQuestionIndex < placementQuestions.size - 1) "Jawab & Soal Berikutnya" else "Lihat Hasil Level",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                2 -> {
                    // Stage 2: Result & Profile Setup
                    val calculatedLevel = when {
                        scoreCount >= 8 -> 3 // Menengah
                        scoreCount >= 4 -> 2 // Pemula
                        else -> 1 // Dasar
                    }

                    val levelName = when (calculatedLevel) {
                        3 -> "Level Menengah (중급)"
                        2 -> "Level Pemula (초급)"
                        else -> "Level Dasar Hangul (입문)"
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_mascot_hannie),
                            contentDescription = "Maskot Hannie",
                            modifier = Modifier
                                .size(110.dp)
                                .clip(RoundedCornerShape(20.dp))
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Selamat Datang!",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = HaneulBlue
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFFEF9C3)
                        ) {
                            Text(
                                text = "Hasil Tes: $scoreCount/10 Benar → Rekomendasi: $levelName",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF854D0E),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Toggle Button for Question Review
                        OutlinedButton(
                            onClick = { showDetailedReview = !showDetailedReview },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("toggle_review_button")
                        ) {
                            Icon(
                                if (showDetailedReview) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = HaneulBlue
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (showDetailedReview) "Tutup Rincian Jawaban Soal" else "Lihat Kunci & Pembahasan (10 Soal)",
                                color = HaneulBlue,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        AnimatedVisibility(visible = showDetailedReview) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                placementQuestions.forEachIndexed { qIdx, question ->
                                    val userChosen = userAnswers[qIdx]
                                    val isCorrect = userChosen == question.correctIndex
                                    Card(
                                        shape = RoundedCornerShape(14.dp),
                                        colors = CardDefaults.cardColors(containerColor = if (isCorrect) Color(0xFFF0FDF4) else Color(0xFFFEF2F2)),
                                        border = CardDefaults.outlinedCardBorder().copy(
                                            brush = androidx.compose.ui.graphics.SolidColor(
                                                if (isCorrect) Color(0xFF86EFAC) else Color(0xFFFECACA)
                                            )
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "Soal #${qIdx + 1}",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp,
                                                    color = if (isCorrect) Color(0xFF166534) else SarangRed
                                                )
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = if (isCorrect) Color(0xFFDCFCE7) else Color(0xFFFEE2E2)
                                                ) {
                                                    Text(
                                                        text = if (isCorrect) "Benar ✓" else "Salah ✗",
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (isCorrect) Color(0xFF166534) else SarangRed,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = question.question,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = InkBlack
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            val userText = if (userChosen != null && userChosen in question.options.indices) {
                                                question.options[userChosen]
                                            } else {
                                                "Tidak dijawab"
                                            }
                                            Text(
                                                text = "Jawaban Anda: $userText",
                                                fontSize = 12.sp,
                                                color = if (isCorrect) Color(0xFF15803D) else SarangRed,
                                                fontWeight = FontWeight.Medium
                                            )
                                            if (!isCorrect) {
                                                Text(
                                                    text = "Kunci Jawaban: ${question.options[question.correctIndex]}",
                                                    fontSize = 12.sp,
                                                    color = Color(0xFF166534),
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(18.dp))
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Text(
                                    text = "Siapa nama panggilan Anda?",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = InkBlack
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = userName,
                                    onValueChange = {
                                        if (it.length <= 30) userName = it
                                    },
                                    label = { Text("Nama Pengguna (2–30 karakter)") },
                                    singleLine = true,
                                    isError = userName.trim().isNotEmpty() && userName.trim().length < 2,
                                    supportingText = {
                                        if (userName.trim().isNotEmpty() && userName.trim().length < 2) {
                                            Text("Nama minimal 2 karakter", color = SarangRed, fontSize = 11.sp)
                                        } else {
                                            Text("${userName.trim().length}/30 karakter", color = TextSecondary, fontSize = 11.sp)
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("user_name_input"),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                Text(
                                    text = "Target Belajar Harian:",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = InkBlack
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    listOf(10, 15, 25).forEach { min ->
                                        val isSel = selectedGoalMinutes == min
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = if (isSel) HaneulBlue else Color(0xFFF1F5F9),
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(12.dp))
                                                .clickable { selectedGoalMinutes = min }
                                        ) {
                                            Text(
                                                text = "$min Menit",
                                                color = if (isSel) Color.White else InkBlack,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.padding(vertical = 10.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    val isNameValid = userName.trim().length >= 2
                    Button(
                        onClick = {
                            if (isNameValid) {
                                onComplete(userName.trim(), calculatedLevel)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("finish_onboarding_button"),
                        enabled = isNameValid,
                        colors = ButtonDefaults.buttonColors(containerColor = HaneulBlue),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = "Mulai Belajar Sekarang! 🚀",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
