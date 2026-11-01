package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AppSubHeader
import com.example.ui.theme.HaneulBlue
import com.example.ui.theme.HaneulContainer
import com.example.ui.theme.InkBlack
import com.example.ui.theme.NunbitYellow
import com.example.ui.theme.NunbitYellowContainer
import com.example.ui.theme.SaessakGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary

data class ReaderSample(
    val title: String,
    val text: String
)

@Composable
fun CustomTextReaderScreen(
    onBack: () -> Unit,
    onSpeak: (String) -> Unit,
    onSaveToVocab: (korean: String, meaning: String) -> Unit
) {
    val sampleSnippets = remember {
        listOf(
            ReaderSample(
                title = "Perkenalan Diri",
                text = "안녕하세요. 저는 한국 드라마와 음악을 좋아하는 학생입니다. 매일 한국어를 조금씩 공부하고 있어요. 반갑습니다!"
            ),
            ReaderSample(
                title = "Pesan Kafe & Minuman",
                text = "따뜻한 아메리카노 한 잔이랑 딸기 케이크 하나 주세요. 포장해 주실 수 있나요? 영수증은 버려주세요. 감사합니다."
            ),
            ReaderSample(
                title = "Pengalaman Jalan-jalan",
                text = "오늘 친구와 함께 명동에 갔습니다. 맛있는 떡볶이와 따뜻한 어묵을 먹었습니다. 저녁에는 한강 공원에서 자전거를 탔어요."
            ),
            ReaderSample(
                title = "Pekerjaan & Kantor",
                text = "내일 오전 열 시에 팀 회의가 있습니다. 프레젠테이션 자료를 미리 준비해 두었습니다. 오늘 퇴근 후에 검토하겠습니다."
            )
        )
    }

    var customText by remember {
        mutableStateOf("안녕하세요. 저는 한국 드라마를 좋아합니다. 한국어를 매일 공부합니다. 만나서 반갑습니다.")
    }
    var activeSentenceIndex by remember { mutableStateOf<Int?>(null) }
    var selectedWord by remember { mutableStateOf<String?>(null) }
    var wordSavedSuccess by remember { mutableStateOf(false) }

    // Sentences parsed
    val sentences = remember(customText) {
        customText.split(Regex("(?<=[.!?])\\s+"))
            .map { it.trim() }
            .filter { it.isNotBlank() }
    }

    val totalHangulChars = remember(customText) {
        customText.count { it in '\uAC00'..'\uD7A3' || it in '\u3131'..'\u318E' }
    }
    val totalWords = remember(customText) {
        customText.trim().split(Regex("\\s+")).filter { it.isNotBlank() }.size
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        AppSubHeader(
            title = "Pembaca Teks Kustom (텍스트 분석)",
            subtitle = "Tempel teks bahasa Korea apa saja, bedah kalimat, dan dengarkan pelafalannya",
            onBack = onBack
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Preset Sample Snippets
            Column {
                Text(
                    text = "PILIH CONTOH CEPAT:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(sampleSnippets) { snippet ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White,
                            border = CardDefaults.outlinedCardBorder(),
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    customText = snippet.text
                                    activeSentenceIndex = null
                                    selectedWord = null
                                }
                        ) {
                            Text(
                                text = snippet.title,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = HaneulBlue,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            // Input Text Area
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
                        Text(
                            text = "Tempel atau Ketik Teks Korea",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = InkBlack
                        )
                        if (customText.isNotBlank()) {
                            IconButton(
                                onClick = { customText = "" },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.Clear, contentDescription = "Hapus", tint = TextMuted, modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = customText,
                        onValueChange = {
                            customText = it
                            activeSentenceIndex = null
                            selectedWord = null
                        },
                        placeholder = { Text("Contoh: 한국어를 배우는 것은 아주 재미있습니다...", color = TextMuted, fontSize = 13.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .testTag("custom_text_input_field"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFFAFAFA),
                            unfocusedContainerColor = Color(0xFFFAFAFA),
                            focusedBorderColor = HaneulBlue,
                            unfocusedBorderColor = Color(0xFFE2E8F0)
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Text statistics row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "$totalHangulChars Huruf Hangul • $totalWords Kata • ${sentences.size} Kalimat",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )

                        Button(
                            onClick = { onSpeak(customText) },
                            colors = ButtonDefaults.buttonColors(containerColor = HaneulBlue),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Dengarkan Semua", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Word inspect card if selected
            AnimatedVisibility(visible = selectedWord != null) {
                selectedWord?.let { word ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = NunbitYellowContainer),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, NunbitYellow, RoundedCornerShape(14.dp))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = word,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = InkBlack
                                )
                                Text(
                                    text = "Ketuk tombol simpan untuk memasukkan kata ini ke SRS Flashcard Anda.",
                                    fontSize = 11.sp,
                                    color = Color(0xFF92400E)
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { onSpeak(word) },
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(Color.White)
                                        .size(36.dp)
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.VolumeUp,
                                        contentDescription = "Dengarkan",
                                        tint = HaneulBlue,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Button(
                                    onClick = {
                                        onSaveToVocab(word, "Kata dari Pembaca Teks Kustom")
                                        wordSavedSuccess = true
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = HaneulBlue),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        if (wordSavedSuccess) Icons.Default.Check else Icons.Default.BookmarkAdd,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (wordSavedSuccess) "Tersimpan!" else "Simpan Kata",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Parsed Sentences Section
            Text(
                text = "BEDAH KALIMAT & INTERAKSI KATA:",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary
            )

            if (sentences.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier.padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Silakan masukkan teks Korea di atas untuk mulai membedah kalimat.",
                            fontSize = 12.sp,
                            color = TextMuted,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                sentences.forEachIndexed { index, sentence ->
                    val isActive = activeSentenceIndex == index

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isActive) HaneulContainer else Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = if (isActive) 1.5.dp else 1.dp,
                                color = if (isActive) HaneulBlue else Color(0xFFE2E8F0),
                                shape = RoundedCornerShape(16.dp)
                            )
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isActive) HaneulBlue else Color(0xFFF1F5F9)
                                ) {
                                    Text(
                                        text = "Kalimat ${index + 1}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isActive) Color.White else TextSecondary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        activeSentenceIndex = index
                                        onSpeak(sentence)
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.VolumeUp,
                                        contentDescription = "Dengarkan Kalimat",
                                        tint = HaneulBlue,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Interactive Word Chips in this sentence
                            val words = sentence.split(Regex("\\s+")).filter { it.isNotBlank() }
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                words.forEach { w ->
                                    val cleanWord = w.replace(Regex("[.,!?;:'\"()]"), "")
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (selectedWord == cleanWord) NunbitYellowContainer else Color(0xFFF8FAFC),
                                        border = CardDefaults.outlinedCardBorder().copy(
                                            brush = androidx.compose.ui.graphics.SolidColor(
                                                if (selectedWord == cleanWord) NunbitYellow else Color(0xFFE2E8F0)
                                            )
                                        ),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable {
                                                activeSentenceIndex = index
                                                selectedWord = cleanWord
                                                wordSavedSuccess = false
                                                onSpeak(cleanWord)
                                            }
                                    ) {
                                        Text(
                                            text = w,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = InkBlack,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
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
}
