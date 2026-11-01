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
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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

fun getQuizBreakdown(quiz: ListeningQuiz): List<Pair<String, String>> {
    return when (quiz.id) {
        "lq1" -> listOf(
            "안녕하세요" to "Halo / Salam sejahtera (sopan santun)",
            "만나서" to "Karena bertemu (kata dasar: 만나다)",
            "반갑습니다" to "Senang / Gembira (bentuk formal sopan)"
        )
        "lq2" -> listOf(
            "지금" to "Sekarang (keterangan waktu)",
            "몇 시" to "Jam berapa (kata tanya)",
            "예요" to "Partikel penutup kalimat sopan (adalah/merupakan)"
        )
        "lq3" -> listOf(
            "불고기" to "Bulgogi (daging sapi bumbu khas Korea)",
            "일인분" to "1 porsi (il-in-bun)",
            "하고" to "Partikel dan / bersama",
            "물 한 병" to "Air 1 botol (satuan botol: 병)",
            "주세요" to "Tolong berikan (permohonan sopan)"
        )
        "lq4" -> listOf(
            "안녕히 계세요" to "Selamat tinggal (diucapkan orang yang pergi ke yang tinggal)",
            "내일" to "Besok (hari esok)",
            "또" to "Lagi / kembali",
            "만나요" to "Ayo bertemu / mari berjumpa"
        )
        "lq5" -> listOf(
            "이번 역" to "Stasiun kali ini",
            "홍대입구" to "Pintu Masuk Universitas Hongik (Hongdae)",
            "내리실 문" to "Pintu untuk turun penumpang",
            "오른쪽" to "Sebelah kanan"
        )
        "lq6" -> listOf(
            "따뜻한" to "Yang bersuhu hangat",
            "카페라떼 한 잔" to "1 cangkir cafe latte (satuan cangkir: 잔)",
            "치즈케이크 하나" to "1 buah kue cheesecake (satuan: 하나)",
            "포장해 주세요" to "Tolong bungkus bawa pulang (takeaway)"
        )
        "lq7" -> listOf(
            "화장실" to "Toilet / Kamar kecil",
            "3번 출구" to "Pintu keluar nomor 3",
            "개찰구 밖" to "Di luar gerbang tiket MRT",
            "오른쪽" to "Sebelah kanan",
            "위치해 있습니다" to "Berlokasi / bertempat"
        )
        "lq8" -> listOf(
            "따릉이" to "Ttareungi (layanan sepeda publik kota Seoul)",
            "대여소" to "Stasiun peminjaman / rental",
            "잠금장치" to "Kunci pengaman / smart lock",
            "해제한 후" to "Setelah melepas / membuka kunci",
            "헬멧" to "Helm pengaman bersepeda",
            "자전거 도로" to "Jalur jalan khusus sepeda"
        )
        else -> listOf(
            quiz.spokenKorean to quiz.explanation
        )
    }
}

fun getPhoneticsTip(quiz: ListeningQuiz): String {
    return when (quiz.id) {
        "lq1" -> "Aturan Nasalisasi (비음화): Huruf ㅂ pada '반갑습니다' dibaca sebagai [ㅁ] menjadi [반갑씀니다] karena bertemu konsonan ㄴ."
        "lq2" -> "Aturan Sambung Bunyi (연음): Huruf ㅊ pada '몇' menyambung ke suku kata berikutnya '시' menghasilkan lafal halus alami [멷씨예요]."
        "lq3" -> "Satuan Hitung Korea: Makanan menggunakan satuan '인분' (porsi), sedangkan minuman memakai '병' (botol) atau '잔' (cangkir/gelas)."
        "lq4" -> "Perbedaan Kritis Salam: '안녕히 계세요' (Anda yang pergi pamit) vs '안녕히 가세요' (Anda tuan rumah melepas tamu yang pergi)."
        "lq5" -> "Liaison MRT: '문은' dibaca bersambung [무는] karena konsonan akhir ㄴ berpindah ke lingkaran ㅇ vokal di depannya."
        "lq6" -> "Intonasi Pemesanan: Suku kata '주세요' diucapkan dengan nada turun santun melengkung di akhir kalimat."
        "lq7" -> "Hukum Bunyi: '개찰구 밖' memiliki batchim ㄲ yang berbunyi K tegas di akhir kata [박]."
        "lq8" -> "Kosa Kata Transportasi Publik: '따릉이' (Ttareungi) dinamai dari tiruan bunyi bel sepeda 'klining-klining' dalam bahasa Korea."
        else -> "Dengarkan perpindahan konsonan akhir (Batchim) ke huruf vokal berikutnya saat penutur asli Korea berbicara santai."
    }
}

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
    var dictationInput by remember(currentQuizIndex) { mutableStateOf("") }
    var dictationChecked by remember(currentQuizIndex) { mutableStateOf(false) }
    var showDictationHint by remember(currentQuizIndex) { mutableStateOf(false) }

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
                                Icons.AutoMirrored.Filled.VolumeUp,
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
                            val isCorrect = selectedOptionIndex == currentQuiz.correctIndex
                            // Comprehensive Educational Feedback Card
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isCorrect) Color(0xFFF0FDF4) else Color(0xFFFFFBEB)
                                ),
                                border = CardDefaults.outlinedCardBorder().copy(
                                    brush = androidx.compose.ui.graphics.SolidColor(
                                        if (isCorrect) Color(0xFF86EFAC) else Color(0xFFFDE68A)
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
                                        Text(
                                            text = if (isCorrect) "🎉 Jawaban Benar! (+100 XP)" else "🧐 Jawaban Belum Tepat",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = if (isCorrect) Color(0xFF166534) else Color(0xFF92400E)
                                        )
                                        IconButton(
                                            onClick = { onSpeak(currentQuiz.spokenKorean, speechRate) },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(
                                                Icons.AutoMirrored.Filled.VolumeUp,
                                                contentDescription = "Putar Ulang",
                                                tint = HaneulBlue
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = currentQuiz.spokenKorean,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = InkBlack
                                    )
                                    Text(
                                        text = "[${currentQuiz.romanization}]",
                                        fontSize = 12.sp,
                                        color = TextSecondary
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "💡 ${currentQuiz.explanation}",
                                        fontSize = 12.sp,
                                        color = InkBlack,
                                        lineHeight = 18.sp
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Bedah Kosakata & Partikel
                                    Text(
                                        text = "📚 Bedah Kosakata & Struktur Kalimat:",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = HaneulBlue
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(4.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color.White, RoundedCornerShape(10.dp))
                                            .padding(10.dp)
                                    ) {
                                        getQuizBreakdown(currentQuiz).forEach { (word, meaning) ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = word,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = InkBlack
                                                )
                                                Text(
                                                    text = meaning,
                                                    fontSize = 11.sp,
                                                    color = TextSecondary,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Kaidah Fonetik Korea
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color(0xFFEFF6FF),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Icon(
                                                Icons.Default.Lightbulb,
                                                contentDescription = null,
                                                tint = HaneulBlue,
                                                modifier = Modifier.size(16.dp).padding(top = 2.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = getPhoneticsTip(currentQuiz),
                                                fontSize = 11.sp,
                                                color = HaneulBlue,
                                                lineHeight = 16.sp
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

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
                            placeholder = { Text("Ketik dalam huruf Hangul di sini...", color = TextSecondary) },
                            singleLine = true,
                            textStyle = androidx.compose.ui.text.TextStyle(color = InkBlack, fontSize = 16.sp, fontWeight = FontWeight.Bold),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = InkBlack,
                                unfocusedTextColor = InkBlack,
                                focusedBorderColor = HaneulBlue,
                                unfocusedBorderColor = Color(0xFFCBD5E1),
                                cursorColor = HaneulBlue,
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("dictation_input_field"),
                            shape = RoundedCornerShape(14.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Hint & Assistance Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (showDictationHint) Color(0xFFFEF3C7) else Color(0xFFEFF6FF),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { showDictationHint = !showDictationHint }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Lightbulb,
                                        contentDescription = null,
                                        tint = if (showDictationHint) Color(0xFFB45309) else HaneulBlue,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (showDictationHint) "Tutup Petunjuk" else "💡 Petunjuk Bunyi",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (showDictationHint) Color(0xFFB45309) else HaneulBlue
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFF1F5F9),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        val full = currentQuiz.spokenKorean
                                        if (dictationInput.length < full.length) {
                                            val nextChar = full[dictationInput.length]
                                            dictationInput += nextChar
                                            dictationChecked = false
                                        }
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = HaneulBlue,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Bantu 1 Huruf ✨",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = HaneulBlue
                                    )
                                }
                            }
                        }

                        AnimatedVisibility(visible = showDictationHint) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFFFFBEB),
                                border = CardDefaults.outlinedCardBorder().copy(
                                    brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFFDE68A))
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = "Petunjuk Bunyi: [${currentQuiz.romanization}]",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF92400E)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = getPhoneticsTip(currentQuiz),
                                        fontSize = 11.sp,
                                        color = Color(0xFF78350F),
                                        lineHeight = 15.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // On-screen Hangul Assistant Pad
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF1F5F9), RoundedCornerShape(12.dp))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = "Bantuan Papan Ketik Hangul (한글 키패드):",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = HaneulBlue
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            // Extract syllables from current quiz target for quick typing
                            val targetChars = currentQuiz.spokenKorean.replace(" ", "").toList().distinct().take(12)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                targetChars.forEach { c ->
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color.White,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .clickable {
                                                dictationInput += c
                                                dictationChecked = false
                                            }
                                    ) {
                                        Text(
                                            text = c.toString(),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = InkBlack,
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 5.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Actions: Space & Backspace
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color.White,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .clickable {
                                            dictationInput += " "
                                            dictationChecked = false
                                        }
                                ) {
                                    Text(
                                        text = "Spasi (␣)",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = HaneulBlue,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                        modifier = Modifier.padding(vertical = 6.dp)
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFFFEE2E2),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .clickable {
                                            if (dictationInput.isNotEmpty()) {
                                                dictationInput = dictationInput.dropLast(1)
                                                dictationChecked = false
                                            }
                                        }
                                ) {
                                    Text(
                                        text = "Hapus (⌫)",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = Color(0xFF991B1B),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                        modifier = Modifier.padding(vertical = 6.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

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
