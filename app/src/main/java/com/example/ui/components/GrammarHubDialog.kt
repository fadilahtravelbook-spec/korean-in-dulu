package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.HaneulBlue
import com.example.ui.theme.HaneulContainer
import com.example.ui.theme.InkBlack
import com.example.ui.theme.NunbitYellow
import com.example.ui.theme.SaessakGreen
import com.example.ui.theme.SarangRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary

data class GrammarPattern(
    val title: String,
    val category: String,
    val formula: String,
    val explanation: String,
    val examples: List<GrammarExample>
)

data class GrammarExample(
    val sentenceKo: String,
    val sentenceRo: String,
    val sentenceId: String
)

data class GrammarQuiz(
    val question: String,
    val sentenceWithBlank: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String
)

@Composable
fun GrammarHubDialog(
    onDismiss: () -> Unit,
    onSpeak: (String) -> Unit
) {
    var selectedSection by remember { mutableIntStateOf(0) } // 0: Struktur SOV & Partikel, 1: Akhiran Sopan, 2: Kuis Latihan

    val patterns = remember {
        listOf(
            GrammarPattern(
                title = "Struktur Kalimat: S - O - P (SOV)",
                category = "Pondasi Dasar",
                formula = "[Subjek] + [Objek] + [Predikat/Kata Kerja]",
                explanation = "Berbeda dengan bahasa Indonesia (S-P-O), kata kerja dalam bahasa Korea SELALU berada di akhir kalimat.",
                examples = listOf(
                    GrammarExample("저는 사과를 먹어요.", "Jeoneun sagwareul meogeoyo.", "Saya makan apel. (Saya + Apel + Makan)"),
                    GrammarExample("친구가 한국어를 공부해요.", "Chinguga hangugeoreul gongbuhaeyo.", "Teman belajar bahasa Korea. (Teman + Bhs Korea + Belajar)")
                )
            ),
            GrammarPattern(
                title = "Partikel Topik: 은 / 는 (eun / neun)",
                category = "Partikel (조사)",
                formula = "Konsonan + 은 | Vokal + 는",
                explanation = "Menandai topik pembicaraan utama atau memberikan nuansa kontras/penekanan.",
                examples = listOf(
                    GrammarExample("저는 인도네시아 사람입니다.", "Jeoneun indonesia saram-imnida.", "Saya adalah orang Indonesia. (저 berakhiran vokal ➔ 는)"),
                    GrammarExample("이 책은 재미있어요.", "I chaegeun jaemi-isseoyo.", "Buku ini menarik. (책 berakhiran konsonan ➔ 은)")
                )
            ),
            GrammarPattern(
                title = "Partikel Subjek: 이 / 가 (i / ga)",
                category = "Partikel (조사)",
                formula = "Konsonan + 이 | Vokal + 가",
                explanation = "Menandai pelaku yang melakukan tindakan spesifik atau menjawab pertanyaan 'Siapa / Apa yang...'.",
                examples = listOf(
                    GrammarExample("날씨가 좋아요.", "Nalssiga joayo.", "Cuacanya bagus. (날씨 berakhiran vokal ➔ 가)"),
                    GrammarExample("비가 와요.", "Biga wayo.", "Hujan turun."),
                    GrammarExample("선생님이 오셨어요.", "Seonsaengnimi osyeosseoyo.", "Guru telah datang. (선생님 berakhiran konsonan ➔ 이)")
                )
            ),
            GrammarPattern(
                title = "Partikel Objek: 을 / 를 (eul / reul)",
                category = "Partikel (조사)",
                formula = "Konsonan + 을 | Vokal + 를",
                explanation = "Menempel pada kata benda yang menjadi sasaran langsung tindakan kata kerja.",
                examples = listOf(
                    GrammarExample("밥을 먹어요.", "Babeul meogeoyo.", "Makan nasi. (밥 berakhiran konsonan ➔ 을)"),
                    GrammarExample("커피를 마셔요.", "Keopireul masyeoyo.", "Minum kopi. (커피 berakhiran vokal ➔ 를)")
                )
            ),
            GrammarPattern(
                title = "Partikel Tempat: 에 vs 에서 (e vs eseo)",
                category = "Partikel (조사)",
                formula = "Tujuan/Waktu: [Tempat] + 에 | Lokasi Aktivitas: [Tempat] + 에서",
                explanation = "'에' dipakai bersama kata kerja arah/keberadaan (가다, 오다, 있다). '에서' dipakai saat melakukan kegiatan aktif di tempat tersebut.",
                examples = listOf(
                    GrammarExample("학교에 가요.", "Hakgyoe gayo.", "Pergi ke sekolah. (Tujuan pergerakan)"),
                    GrammarExample("카페에서 친구를 만나요.", "Kape-eseo chingureul mannayo.", "Bertemu teman di cafe. (Lokasi aktivitas)")
                )
            ),
            GrammarPattern(
                title = "Akhiran Sopan: -아요 / -어요 / -해요",
                category = "Akhiran Predikat",
                formula = "Vokal ㅏ/ㅗ ➔ -아요 | Vokal lain ➔ -어요 | 하다 ➔ -해요",
                explanation = "Bentuk sopan santai (Haeyo-che) yang paling umum digunakan dalam kehidupan sehari-hari.",
                examples = listOf(
                    GrammarExample("가다 ➔ 가요", "Gada -> Gayo", "Pergi (Vokal ㅏ ➔ -아요)"),
                    GrammarExample("먹다 ➔ 먹어요", "Meokda -> Meogeoyo", "Makan (Vokal ㅓ ➔ -어요)"),
                    GrammarExample("공부하다 ➔ 공부해요", "Gongbuhada -> Gongbuhaeyo", "Belajar (하다 ➔ 해요)")
                )
            ),
            GrammarPattern(
                title = "Akhiran Formal: -ㅂ니다 / -습니다",
                category = "Akhiran Predikat",
                formula = "Vokal + ㅂ니다 | Konsonan + 습니다",
                explanation = "Bentuk sangat formal dan hormat (Hasipsio-che) digunakan di berita, pidato, militer, atau saat bertemu pertama kali.",
                examples = listOf(
                    GrammarExample("감사합니다.", "Gamsahamnida.", "Terima kasih."),
                    GrammarExample("만나서 반갑습니다.", "Mannaseo bangapseumnida.", "Senang bertemu dengan Anda.")
                )
            )
        )
    }

    val grammarQuizzes = remember {
        listOf(
            GrammarQuiz(
                question = "Pilihlah partikel subjek yang tepat untuk melengkapi kalimat:",
                sentenceWithBlank = "민우___ 노래를 잘해요. (Minwoo bernyanyi dengan merdu)",
                options = listOf("는", "가", "를", "에서"),
                correctIndex = 1,
                explanation = "'민우' berakhiran huruf vokal '우', sehingga memakai partikel subjek '가' (민우가)."
            ),
            GrammarQuiz(
                question = "Lengkapilah dengan partikel tempat aktivitas yang tepat:",
                sentenceWithBlank = "도서관___ 책을 읽어요. (Saya membaca buku di perpustakaan)",
                options = listOf("에", "에서", "을", "과"),
                correctIndex = 1,
                explanation = "Membaca buku adalah aktivitas tindakan aktif, sehingga partikel lokasi yang benar adalah '에서' (도서관에서)."
            ),
            GrammarQuiz(
                question = "Bagaimanakah perubahan bentuk kata kerja '보다' (Melihat/Menonton) ke bentuk sopan -아요/어요?",
                sentenceWithBlank = "영화를 _____? (Menonton film)",
                options = listOf("보어요", "봐요", "보해요", "봅니다요"),
                correctIndex = 1,
                explanation = "'보다' memiliki vokal 'ㅗ', digabung dengan '-아요' berkontraksi menjadi '봐요' (bwayo)."
            )
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.92f)
                .clip(RoundedCornerShape(24.dp)),
            color = Color(0xFFF8FAFC)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(HaneulBlue)
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = NunbitYellow,
                            modifier = Modifier.size(34.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("문", fontWeight = FontWeight.Bold, color = InkBlack, fontSize = 16.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Panduan Tata Bahasa (문법 가이드)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color.White
                            )
                            Text(
                                text = "Pola SOV, Partikel & Akhiran Sopan",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_grammar_dialog_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup", tint = Color.White)
                    }
                }

                // Tab Selector
                TabRow(
                    selectedTabIndex = selectedSection,
                    containerColor = Color.White,
                    contentColor = HaneulBlue
                ) {
                    Tab(
                        selected = selectedSection == 0,
                        onClick = { selectedSection = 0 },
                        text = { Text("Pola & Partikel", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedSection == 1,
                        onClick = { selectedSection = 1 },
                        text = { Text("Akhiran Sopan", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedSection == 2,
                        onClick = { selectedSection = 2 },
                        text = { Text("Kuis Cepat ⚡", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    )
                }

                when (selectedSection) {
                    0, 1 -> {
                        val filterCat = if (selectedSection == 0) listOf("Pondasi Dasar", "Partikel (조사)") else listOf("Akhiran Predikat")
                        val filteredPatterns = patterns.filter { it.category in filterCat }

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 14.dp),
                            contentPadding = PaddingValues(vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(filteredPatterns) { pat ->
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    border = CardDefaults.outlinedCardBorder().copy(
                                        brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFE2E8F0))
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
                                                text = pat.title,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = InkBlack
                                            )
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = HaneulContainer
                                            ) {
                                                Text(
                                                    text = pat.category,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = HaneulBlue,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color(0xFFF1F5F9),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = "Rumus: ${pat.formula}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = HaneulBlue,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = pat.explanation,
                                            fontSize = 12.sp,
                                            color = TextSecondary,
                                            lineHeight = 17.sp
                                        )

                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text(
                                            text = "Contoh Kalimat (Tekan untuk Dengar):",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = InkBlack
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))

                                        pat.examples.forEach { eg ->
                                            Surface(
                                                shape = RoundedCornerShape(10.dp),
                                                color = Color(0xFFF8FAFC),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 3.dp)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .clickable { onSpeak(eg.sentenceKo) }
                                                    .padding(8.dp)
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        Icons.AutoMirrored.Filled.VolumeUp,
                                                        contentDescription = "Dengar",
                                                        tint = HaneulBlue,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Column {
                                                        Text(
                                                            text = eg.sentenceKo,
                                                            fontSize = 13.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = InkBlack
                                                        )
                                                        Text(
                                                            text = eg.sentenceId,
                                                            fontSize = 11.sp,
                                                            color = TextSecondary
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

                    2 -> {
                        // Interactive Grammar Quiz Section
                        var quizIndex by remember { mutableIntStateOf(0) }
                        var selectedOption by remember { mutableStateOf<Int?>(null) }
                        var correctCount by remember { mutableIntStateOf(0) }
                        var isFinished by remember { mutableStateOf(false) }

                        val currentQ = grammarQuizzes.getOrElse(quizIndex) { grammarQuizzes.first() }

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        ) {
                            if (!isFinished) {
                                Text(
                                    text = "Pertanyaan ${quizIndex + 1} dari ${grammarQuizzes.size}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = HaneulBlue
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = currentQ.question,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = InkBlack
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = Color(0xFFEFF6FF),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = currentQ.sentenceWithBlank,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = HaneulBlue,
                                        modifier = Modifier.padding(16.dp),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                currentQ.options.forEachIndexed { optIdx, optText ->
                                    val isSelected = selectedOption == optIdx
                                    val showResult = selectedOption != null
                                    val isCorrect = optIdx == currentQ.correctIndex

                                    val bg = when {
                                        !showResult -> Color.White
                                        isCorrect -> Color(0xFFD1FAE5)
                                        isSelected -> Color(0xFFFEE2E2)
                                        else -> Color.White
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = bg,
                                        border = CardDefaults.outlinedCardBorder().copy(
                                            brush = androidx.compose.ui.graphics.SolidColor(
                                                if (showResult && isCorrect) SaessakGreen else Color(0xFFE2E8F0)
                                            )
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable(enabled = selectedOption == null) {
                                                selectedOption = optIdx
                                                if (optIdx == currentQ.correctIndex) {
                                                    correctCount += 1
                                                }
                                            }
                                            .testTag("grammar_opt_$optIdx")
                                    ) {
                                        Text(
                                            text = optText,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = InkBlack,
                                            modifier = Modifier.padding(14.dp)
                                        )
                                    }
                                }

                                AnimatedVisibility(visible = selectedOption != null) {
                                    Column(modifier = Modifier.padding(top = 10.dp)) {
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = Color(0xFFFFFBEB),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = "💡 ${currentQ.explanation}",
                                                fontSize = 12.sp,
                                                color = InkBlack,
                                                modifier = Modifier.padding(10.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(12.dp))

                                        Button(
                                            onClick = {
                                                if (quizIndex < grammarQuizzes.size - 1) {
                                                    quizIndex += 1
                                                    selectedOption = null
                                                } else {
                                                    isFinished = true
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = HaneulBlue),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = if (quizIndex < grammarQuizzes.size - 1) "Soal Selanjutnya" else "Selesai",
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            } else {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text("🎉", fontSize = 48.sp)
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "Kuis Tata Bahasa Selesai!",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = InkBlack
                                    )
                                    Text(
                                        text = "Kamu menjawab benar $correctCount dari ${grammarQuizzes.size} soal",
                                        fontSize = 13.sp,
                                        color = TextSecondary
                                    )
                                    Spacer(modifier = Modifier.height(18.dp))
                                    Button(
                                        onClick = {
                                            quizIndex = 0
                                            selectedOption = null
                                            correctCount = 0
                                            isFinished = false
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = HaneulBlue),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("Coba Lagi", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
