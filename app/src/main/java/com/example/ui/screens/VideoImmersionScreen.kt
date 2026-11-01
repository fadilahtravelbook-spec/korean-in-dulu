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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SubtitleLine
import com.example.data.model.VideoImmersion
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
fun VideoImmersionScreen(
    videoLessons: List<VideoImmersion>,
    onBack: () -> Unit,
    onSpeak: (String, Float) -> Unit,
    onSaveToVocab: (String, String) -> Unit,
    onRecordProgress: (moduleId: String, lessonId: String, title: String, score: Int) -> Unit
) {
    var selectedVideoIndex by remember { mutableIntStateOf(0) }
    val currentVideo = videoLessons.getOrElse(selectedVideoIndex) { videoLessons.first() }

    var activeLineIndex by remember { mutableIntStateOf(0) }
    var playbackRate by remember { mutableStateOf(0.9f) }
    var isShadowingMode by remember { mutableStateOf(false) }
    var shadowingDone by remember { mutableStateOf(false) }
    var savedLineId by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        AppSubHeader(
            title = "Video Belajar & Subtitle Interaktif",
            subtitle = "Immersion percakapan nyata drama & skenario Korea",
            onBack = onBack
        )

        // Video Scenario Selector Tabs
        ScrollableTabRow(
            selectedTabIndex = selectedVideoIndex,
            containerColor = Color.White,
            contentColor = HaneulBlue,
            edgePadding = 16.dp,
            divider = {}
        ) {
            videoLessons.forEachIndexed { idx, vid ->
                val shortTitle = when (vid.id) {
                    "video_1" -> "GS25 Minimarket"
                    "video_2" -> "Kafe Hongdae"
                    "video_3" -> "Toilet & PIN Kafe"
                    "video_4" -> "Sepeda Ttareungi"
                    "video_5" -> "Subway Hujan & Kesal"
                    "video_6" -> "Tegur Sapa Kantor"
                    else -> vid.title.substringBefore(" (").take(18)
                }
                Tab(
                    selected = selectedVideoIndex == idx,
                    onClick = {
                        selectedVideoIndex = idx
                        activeLineIndex = 0
                        shadowingDone = false
                    },
                    text = {
                        Text(
                            text = shortTitle,
                            fontWeight = if (selectedVideoIndex == idx) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 12.sp
                        )
                    }
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Simulated Visual Player Stage
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color(currentVideo.thumbnailColorHex)),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .testTag("video_player_stage")
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.Black.copy(alpha = 0.4f)
                        ) {
                            Text(
                                text = "🎬 ${currentVideo.sourceTitle}",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = currentVideo.title,
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Active Subtitle Overlay on Video
                        val line = currentVideo.lines[activeLineIndex]
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.Black.copy(alpha = 0.75f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSpeak(line.korean, playbackRate)
                                }
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = line.korean,
                                    color = NunbitYellow,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = line.indonesian,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Player Bar: Prev Line, Play, Next Line, Speed
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        if (activeLineIndex > 0) activeLineIndex--
                    },
                    enabled = activeLineIndex > 0
                ) {
                    Icon(Icons.Default.FastRewind, contentDescription = "Kalimat Sebelumnya", tint = HaneulBlue)
                }

                Button(
                    onClick = {
                        val curLine = currentVideo.lines[activeLineIndex]
                        onSpeak(curLine.korean, playbackRate)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HaneulBlue),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Putar Kalimat (${activeLineIndex + 1}/${currentVideo.lines.size})", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                IconButton(
                    onClick = {
                        if (activeLineIndex < currentVideo.lines.size - 1) activeLineIndex++
                    },
                    enabled = activeLineIndex < currentVideo.lines.size - 1
                ) {
                    Icon(Icons.Default.FastForward, contentDescription = "Kalimat Berikutnya", tint = HaneulBlue)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Shadowing Practice Card
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(18.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Latihan Shadowing (Tiru Langsung)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = InkBlack
                        )
                        Icon(
                            Icons.Default.RecordVoiceOver,
                            contentDescription = null,
                            tint = HaneulBlue
                        )
                    }
                    Text(
                        text = "Dengarkan intonasi penutur lalu ucapkan ulang secara bersamaan",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            isShadowingMode = true
                            shadowingDone = true
                            onRecordProgress("video", currentVideo.id, "Shadowing ${currentVideo.title}", 90)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Mic, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Mulai Ucapkan Bersama Native", color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    if (shadowingDone) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "✅ Hebat! Intonasi shadowing Anda tercatat dengan baik (+20 XP)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0D9488)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Full Dual Subtitle Script List
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(20.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Transkrip Dialog Lengkap (Ketuk untuk Dengarkan / Simpan SRS):",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = InkBlack
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    currentVideo.lines.forEachIndexed { idx, sub ->
                        val isCurr = activeLineIndex == idx
                        val isSaved = savedLineId == "${currentVideo.id}_$idx"
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isCurr) HaneulContainer else Color(0xFFF8FAFC),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    activeLineIndex = idx
                                    onSpeak(sub.korean, playbackRate)
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${idx + 1}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (isCurr) HaneulBlue else TextSecondary,
                                    modifier = Modifier.width(22.dp)
                                )

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = sub.korean,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isCurr) HaneulBlue else InkBlack
                                    )
                                    Text(
                                        text = sub.romanization,
                                        fontSize = 11.sp,
                                        color = NunbitYellow,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = sub.indonesian,
                                        fontSize = 12.sp,
                                        color = TextSecondary
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = {
                                            onSaveToVocab(sub.korean, sub.indonesian)
                                            savedLineId = "${currentVideo.id}_$idx"
                                        }
                                    ) {
                                        Icon(
                                            Icons.Default.BookmarkAdd,
                                            contentDescription = "Simpan ke SRS",
                                            tint = if (isSaved) SaessakGreen else HaneulBlue,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Icon(
                                        Icons.AutoMirrored.Filled.VolumeUp,
                                        contentDescription = null,
                                        tint = if (isCurr) HaneulBlue else TextSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
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
