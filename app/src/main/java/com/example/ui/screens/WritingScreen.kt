package com.example.ui.screens

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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
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
import com.example.data.model.HangulLetter
import com.example.ui.components.AppSubHeader
import com.example.ui.components.AudioSpeakerButton
import com.example.ui.components.BatchimGuideView
import com.example.ui.components.HangulWritingCanvas
import com.example.ui.theme.HaneulBlue
import com.example.ui.theme.HaneulContainer
import com.example.ui.theme.InkBlack
import com.example.ui.theme.NunbitYellow
import com.example.ui.theme.SaessakGreen
import com.example.ui.theme.SarangRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary

@Composable
fun WritingScreen(
    letters: List<HangulLetter>,
    onBack: () -> Unit,
    onSpeak: (String) -> Unit,
    onRecordProgress: (moduleId: String, lessonId: String, title: String, score: Int) -> Unit,
    onNavigateToHangulIntro: (() -> Unit)? = null
) {
    val categories = listOf("Vokal Dasar", "Konsonan Dasar", "Vokal Ganda", "Konsonan Ganda", "Aturan Batchim (받침)")
    var selectedCategoryIndex by remember { mutableIntStateOf(0) }
    val filteredLetters = letters.filter { it.category == categories.getOrElse(selectedCategoryIndex) { "" } }

    var selectedLetterIndex by remember { mutableIntStateOf(0) }
    val currentLetter = filteredLetters.getOrElse(selectedLetterIndex) { filteredLetters.firstOrNull() ?: letters.first() }

    // 0: Trace Mode, 1: Free Writing, 2: Dikte Huruf
    var writingMode by remember { mutableIntStateOf(0) }
    var latestScore by remember { mutableStateOf<Int?>(null) }
    var isDikteRevealed by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        AppSubHeader(
            title = "Menulis Hangul (한글 쓰기)",
            subtitle = "Latihan menulis dengan stroke order, trace mode & kanvas kotak",
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

        // Category Tabs
        ScrollableTabRow(
            selectedTabIndex = selectedCategoryIndex,
            containerColor = Color.White,
            contentColor = HaneulBlue,
            edgePadding = 16.dp,
            divider = {}
        ) {
            categories.forEachIndexed { idx, cat ->
                Tab(
                    selected = selectedCategoryIndex == idx,
                    onClick = {
                        selectedCategoryIndex = idx
                        selectedLetterIndex = 0
                        latestScore = null
                    },
                    text = {
                        Text(
                            text = cat,
                            fontWeight = if (selectedCategoryIndex == idx) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp
                        )
                    }
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            if (selectedCategoryIndex == 4) {
                // Batchim Guide & Phonology Lab
                BatchimGuideView(
                    onSpeak = onSpeak,
                    onRecordProgress = onRecordProgress
                )
            } else {
                // Alphabet Horizontal Selector
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                items(filteredLetters) { letter ->
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
                                selectedLetterIndex = filteredLetters.indexOf(letter)
                                latestScore = null
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
                                fontSize = 18.sp,
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

            // Mode Selector: Trace vs Free Writing vs Dikte
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color.White,
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFE2E8F0))
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val modeLabels = listOf("Bayangan (Trace)", "Kanvas Bebas", "Mode Dikte 🎧")
                    modeLabels.forEachIndexed { idx, label ->
                        val isSel = writingMode == idx
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSel) HaneulBlue else Color.Transparent,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    writingMode = idx
                                    latestScore = null
                                    isDikteRevealed = false
                                }
                                .testTag("writing_mode_$idx")
                        ) {
                            Text(
                                text = label,
                                color = if (isSel) Color.White else InkBlack,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Character Info & Audio Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(20.dp))
            ) {
                if (writingMode == 2 && !isDikteRevealed) {
                    // Dikte Mode: Mystery Character prompt
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Tantangan Dikte Huruf Hangul",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = HaneulBlue
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tekan tombol dengar, lalu tuliskan huruf yang kamu dengar di kanvas kotak di bawah ini!",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { onSpeak(currentLetter.soundSample) },
                            colors = ButtonDefaults.buttonColors(containerColor = NunbitYellow),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("play_dictation_audio_button")
                        ) {
                            Icon(Icons.Default.VolumeUp, contentDescription = null, tint = InkBlack, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Dengarkan Bunyi Huruf 🔊", color = InkBlack, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                } else {
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
                                modifier = Modifier.size(58.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = currentLetter.character,
                                        fontSize = 30.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = HaneulBlue
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = "Huruf ${currentLetter.name} [${currentLetter.romanization}]",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = InkBlack
                                )
                                Text(
                                    text = "${currentLetter.strokeCount} Coretan • ${currentLetter.description}",
                                    fontSize = 11.sp,
                                    color = TextSecondary,
                                    lineHeight = 15.sp
                                )
                            }
                        }

                        AudioSpeakerButton(
                            onClick = { onSpeak(currentLetter.soundSample) },
                            label = "Bunyi"
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Hangul Canvas Drawing Component
            HangulWritingCanvas(
                targetCharacter = currentLetter.character,
                isTraceMode = (writingMode == 0),
                onEvaluateScore = { score ->
                    latestScore = score
                    if (writingMode == 2) {
                        isDikteRevealed = true
                    }
                    onRecordProgress("writing", currentLetter.character, "Tulis ${currentLetter.character}", score)
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Navigation Stepper Bar
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
                            isDikteRevealed = false
                        }
                    },
                    enabled = selectedLetterIndex > 0,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE2E8F0)),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("⬅️ Sebelumnya", color = if (selectedLetterIndex > 0) InkBlack else Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Text(
                    text = "${selectedLetterIndex + 1} / ${filteredLetters.size}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary
                )

                Button(
                    onClick = {
                        if (selectedLetterIndex < filteredLetters.size - 1) {
                            selectedLetterIndex++
                            latestScore = null
                            isDikteRevealed = false
                        }
                    },
                    enabled = selectedLetterIndex < filteredLetters.size - 1,
                    colors = ButtonDefaults.buttonColors(containerColor = HaneulBlue),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("Berikutnya ➡️", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Stroke Guide Steps
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
                            text = "Urutan Goresan Resmi (Stroke Order):",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = InkBlack
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFFEF9C3)
                        ) {
                            Text(
                                text = "${currentLetter.strokeCount} Langkah",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF854D0E),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    currentLetter.strokeGuide.forEach { step ->
                        Text(
                            text = "• $step",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            lineHeight = 18.sp,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
