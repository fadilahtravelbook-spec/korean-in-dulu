package com.example.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.DailyChallengeItem
import com.example.data.model.UserProfile
import com.example.ui.components.DailyLifeHubDialog
import com.example.ui.components.GrammarHubDialog
import com.example.ui.components.HannieMascotCard
import com.example.ui.components.TopStatBar
import com.example.ui.theme.HaneulBlue
import com.example.ui.theme.HaneulContainer
import com.example.ui.theme.InkBlack
import com.example.ui.theme.NunbitYellow
import com.example.ui.theme.NunbitYellowContainer
import com.example.ui.theme.SaessakGreen
import com.example.ui.theme.SarangRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import kotlin.math.min

@Composable
fun HomeScreen(
    userProfile: UserProfile?,
    completedLessonsCount: Int,
    learningMinutes: Int,
    unlockedBadgeCount: Int,
    dailyChallenges: List<DailyChallengeItem>,
    onNavigateToModule: (String) -> Unit,
    onNavigateToProfile: () -> Unit,
    onSpeak: (String) -> Unit = {}
) {
    val streak = userProfile?.streak ?: 1
    val xp = userProfile?.xp ?: 100
    val level = userProfile?.level ?: 1
    val name = userProfile?.name ?: "Sahabat Korea"
    val dailyGoal = userProfile?.dailyGoalMinutes ?: 15
    val goalProgress = min(1f, learningMinutes.toFloat() / dailyGoal.toFloat())

    var showGrammarHub by remember { mutableStateOf(false) }
    var showDailyLifeHub by remember { mutableStateOf(false) }

    if (showGrammarHub) {
        GrammarHubDialog(
            onDismiss = { showGrammarHub = false },
            onSpeak = onSpeak
        )
    }

    if (showDailyLifeHub) {
        DailyLifeHubDialog(
            onDismiss = { showDailyLifeHub = false },
            onSpeak = onSpeak
        )
    }

    val levelTitle = when (level) {
        3 -> "Level 3: Menengah (중급)"
        2 -> "Level 2: Pemula (초급)"
        else -> "Level 1: Dasar Hangul (입문)"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        // Top App Bar with Streak & XP
        TopStatBar(
            streak = streak,
            xp = xp,
            level = level,
            onProfileClick = onNavigateToProfile
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Welcome Greeting
            item {
                Column {
                    Text(
                        text = "안녕하세요, $name! ✨",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = InkBlack
                    )
                    Text(
                        text = "$levelTitle • Belajar Korea dari nol sampai bisa",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Hannie Mascot Motivation Card
            item {
                HannieMascotCard(
                    quote = "화이팅 (Hwaiting)! Konsisten 10–15 menit setiap hari jauh lebih efektif daripada belajar 3 jam sekaligus dalam seminggu!",
                    title = "Hannie (한이) Sang Maskot Harimau Putih"
                )
            }

            // Daily Target & Progress Card
            item {
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Timer, contentDescription = null, tint = HaneulBlue, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Target Belajar Hari Ini",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = InkBlack
                                )
                            }
                            Text(
                                text = "$learningMinutes / $dailyGoal Menit",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp,
                                color = HaneulBlue
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        LinearProgressIndicator(
                            progress = { goalProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = if (goalProgress >= 1f) SaessakGreen else HaneulBlue,
                            trackColor = Color(0xFFE2E8F0)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (goalProgress >= 1f) "🎉 Target tercapai hari ini!" else "Sedikit lagi menuju target harian",
                                fontSize = 11.sp,
                                color = if (goalProgress >= 1f) Color(0xFF059669) else TextSecondary,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "$unlockedBadgeCount Badge Diraih",
                                fontSize = 11.sp,
                                color = Color(0xFFCA8A04),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Quick Resume Card (Lanjut Belajar)
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = HaneulBlue),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("quick_resume_card")
                        .clickable { onNavigateToModule("writing") }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.2f),
                            modifier = Modifier.size(46.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.PlayCircle,
                                    contentDescription = null,
                                    tint = NunbitYellow,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        val lastLessonId = userProfile?.lastAccessedLessonId ?: "writing"
                        val (targetModule, lastLessonTitle) = when {
                            lastLessonId.startsWith("vocab") -> "vocab" to "Kosakata SRS & Flashcard"
                            lastLessonId.startsWith("speaking") -> "speaking" to "Berbicara: Percakapan & Role-Play"
                            lastLessonId.startsWith("listening") -> "listening" to "Mendengar: Kuis Pemahaman Audio"
                            lastLessonId.startsWith("reading") -> "reading" to "Membaca: Cerita & Reading Aloud"
                            lastLessonId.startsWith("video") -> "video" to "Video Immersion & Subtitle"
                            else -> "writing" to "Hangul: Menulis Huruf Vokal & Konsonan"
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Lanjut Belajar Terakhir",
                                fontSize = 11.sp,
                                color = NunbitYellow,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = lastLessonTitle,
                                fontSize = 15.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "$completedLessonsCount pelajaran telah tuntas diselesaikan",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }

                        Button(
                            onClick = { onNavigateToModule(targetModule) },
                            colors = ButtonDefaults.buttonColors(containerColor = NunbitYellow),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("quick_resume_button")
                        ) {
                            Text("Buka", color = InkBlack, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }

            // Daily Challenge Quests
            item {
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = NunbitYellow, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Tantangan Hari Ini (Daily Challenge)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = InkBlack
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFFEF3C7)
                            ) {
                                Text(
                                    text = "+Bonus XP",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF92400E),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        dailyChallenges.forEach { ch ->
                            val progressRatio = min(1f, ch.currentCount.toFloat() / ch.targetCount.toFloat())
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (ch.isCompleted) Color(0xFFF0FDF4) else Color(0xFFF8FAFC),
                                border = CardDefaults.outlinedCardBorder().copy(
                                    brush = androidx.compose.ui.graphics.SolidColor(
                                        if (ch.isCompleted) Color(0xFF86EFAC) else Color(0xFFE2E8F0)
                                    )
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { onNavigateToModule(ch.moduleId) }
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                            Icon(
                                                if (ch.isCompleted) Icons.Default.CheckCircle else Icons.Default.PlayCircle,
                                                contentDescription = null,
                                                tint = if (ch.isCompleted) SaessakGreen else HaneulBlue,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = ch.title,
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = InkBlack
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = "(${ch.currentCount}/${ch.targetCount})",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = if (ch.isCompleted) SaessakGreen else HaneulBlue
                                                    )
                                                }
                                                Text(text = ch.description, fontSize = 11.sp, color = TextSecondary)
                                            }
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (ch.isCompleted) Color(0xFFD1FAE5) else Color(0xFFEFF6FF)
                                        ) {
                                            Text(
                                                text = if (ch.isCompleted) "Selesai ✓" else "+${ch.xpReward} XP",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (ch.isCompleted) Color(0xFF065F46) else HaneulBlue,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }

                                    if (!ch.isCompleted) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        LinearProgressIndicator(
                                            progress = { progressRatio },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(4.dp)
                                                .clip(RoundedCornerShape(2.dp)),
                                            color = NunbitYellow,
                                            trackColor = Color(0xFFE2E8F0)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Featured Card: Pengenalan Huruf Hangul Flashcard
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFBFDBFE))
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToModule("hangul_intro") }
                        .testTag("open_hangul_intro_card")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = HaneulContainer,
                            modifier = Modifier.size(52.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("한", fontSize = 26.sp, fontWeight = FontWeight.Black, color = HaneulBlue)
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Pengenalan Huruf Hangul",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = InkBlack
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = NunbitYellowContainer
                                ) {
                                    Text(
                                        text = "40 Huruf",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFFCA8A04),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "Tombol flashcard interaktif untuk setiap vokal & konsonan lengkap dengan audio & tips",
                                fontSize = 11.sp,
                                color = TextSecondary,
                                lineHeight = 15.sp
                            )
                        }

                        Icon(
                            Icons.Default.PlayCircle,
                            contentDescription = "Buka Hangul Flashcard",
                            tint = HaneulBlue,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }

            // Section Header: Modul Belajar
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Modul Belajar Lengkap",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = InkBlack
                    )
                    Text(
                        text = "6 Modul Utama",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // 6 Primary Modules Grid Cards (2 columns layout)
            item {
                val modules = listOf(
                    ModuleItem("writing", "Menulis Hangul", "한글 쓰기", "Trace mode, stroke guide, & dikte", Icons.Default.Edit, Color(0xFF2563EB), Color(0xFFDBEAFE)),
                    ModuleItem("speaking", "Berbicara", "말하기 연습", "Template perkenalan & role-play dialog", Icons.Default.Mic, Color(0xFFD97706), Color(0xFFFEF3C7)),
                    ModuleItem("listening", "Mendengar", "듣기 퀴즈", "Listening quiz, speed control, & dikte", Icons.Default.Headphones, Color(0xFF059669), Color(0xFFD1FAE5)),
                    ModuleItem("vocab", "Kosakata & SRS", "단어장 (SRS)", "SuperMemo-2 Spaced Repetition & matching", Icons.Default.Book, Color(0xFF7C3AED), Color(0xFFEDE9FE)),
                    ModuleItem("reading", "Membaca Aloud", "읽기 연습", "Karaoke reader, custom text, & WPM", Icons.AutoMirrored.Filled.MenuBook, Color(0xFFDC2626), Color(0xFFFEE2E2)),
                    ModuleItem("video", "Video Belajar", "영상 학습", "Subtitle ganda interaktif & shadowing", Icons.Default.VideoLibrary, Color(0xFF0D9488), Color(0xFFCCFBF1))
                )

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    for (i in modules.indices step 2) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            ModuleCard(
                                item = modules[i],
                                onClick = { onNavigateToModule(modules[i].id) },
                                modifier = Modifier.weight(1f)
                            )
                            if (i + 1 < modules.size) {
                                ModuleCard(
                                    item = modules[i + 1],
                                    onClick = { onNavigateToModule(modules[i + 1].id) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            // Grammar Guide Hub Card
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFE2E8F0))
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showGrammarHub = true }
                        .testTag("open_grammar_hub_card")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFFFEF3C7),
                            modifier = Modifier.size(46.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("문", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF92400E))
                            }
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Panduan Tata Bahasa (문법)",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = InkBlack
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = HaneulContainer
                                ) {
                                    Text(
                                        text = "TOPIK I",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = HaneulBlue,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Pola SOV, partikel 은/는, 이/가, 을/를 & akhiran sopan -아요/어요",
                                fontSize = 11.sp,
                                color = TextSecondary,
                                lineHeight = 15.sp
                            )
                        }
                        Icon(
                            Icons.Default.PlayCircle,
                            contentDescription = null,
                            tint = HaneulBlue,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // Daily Life Guide Hub Card (Toilet, Bike, Convenience Store, Recycling)
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFE2E8F0))
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDailyLifeHub = true }
                        .testTag("open_daily_life_hub_card")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFFD1FAE5),
                            modifier = Modifier.size(46.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("생", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF065F46))
                            }
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Panduan Keseharian (일상생활)",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = InkBlack
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFFE0F2FE)
                                ) {
                                    Text(
                                        text = "6 Topik Nyata",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = HaneulBlue,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Rasa kesal & emosi, tegur sapa, toilet kafe, sepeda 따릉이 & etiket rumah",
                                fontSize = 11.sp,
                                color = TextSecondary,
                                lineHeight = 15.sp
                            )
                        }
                        Icon(
                            Icons.Default.PlayCircle,
                            contentDescription = null,
                            tint = Color(0xFF059669),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // Developer Footer Note
            item {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFE2E8F0))
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_logo_korean_in_dulu),
                            contentDescription = "Korean In Dulu",
                            modifier = Modifier.height(34.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Dikembangkan oleh Bangdillah210 (Achmat Fadilah)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextSecondary
                        )
                        Text(
                            text = "Tagline: \"Belajar Korea, dari nol sampai bisa.\"",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

data class ModuleItem(
    val id: String,
    val title: String,
    val korean: String,
    val desc: String,
    val icon: ImageVector,
    val badgeColor: Color,
    val badgeBg: Color
)

@Composable
fun ModuleCard(
    item: ModuleItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .testTag("module_card_${item.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = item.badgeBg,
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            item.icon,
                            contentDescription = item.title,
                            tint = item.badgeColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFF1F5F9)
                ) {
                    Text(
                        text = "Buka",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = HaneulBlue,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = item.korean,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = item.badgeColor
            )

            Text(
                text = item.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                color = InkBlack
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = item.desc,
                fontSize = 11.sp,
                color = TextSecondary,
                lineHeight = 14.sp,
                maxLines = 2
            )
        }
    }
}
