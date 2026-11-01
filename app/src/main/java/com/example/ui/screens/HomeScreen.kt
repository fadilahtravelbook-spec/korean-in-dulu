package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import kotlin.math.max
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
    val dailyGoal = userProfile?.dailyGoalMinutes?.takeIf { it > 0 } ?: 15
    val goalProgress = (learningMinutes.toFloat() / dailyGoal.toFloat()).coerceIn(0f, 1f)

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

    // Word of the Day (Rotates or fixed starter)
    val wordOfTheDayKorean = "행복"
    val wordOfTheDayRomanization = "haeng-bok"
    val wordOfTheDayMeaning = "Kebahagiaan / Bahagia"
    val wordOfTheDayExampleKo = "오늘도 행복한 하루 보내세요!"
    val wordOfTheDayExampleId = "Semoga hari ini juga menjadi hari yang membahagiakan!"

    // Menghitung target modul terakhir yang dipelajari
    val lastLessonId = userProfile?.lastAccessedLessonId ?: "writing"
    val (targetModule, lastLessonTitle, targetSubtitle) = when {
        lastLessonId.startsWith("vocab") -> Triple("vocab", "Kosakata SRS & Flashcard", "Spaced Repetition Review")
        lastLessonId.startsWith("speaking") -> Triple("speaking", "Berbicara & Percakapan", "Role-play Percakapan Interaktif")
        lastLessonId.startsWith("listening") -> Triple("listening", "Mendengar & Kuis Audio", "Uji Ketajaman Telinga")
        lastLessonId.startsWith("reading") -> Triple("reading", "Membaca Aloud", "Karaoke Text & Reading Speed")
        lastLessonId.startsWith("video") -> Triple("video", "Video Immersion", "Subtitle Ganda & Shadowing")
        else -> Triple("writing", "Hangul: Menulis Huruf & Suku Kata", "Latihan Coretan Kanvas & Audio")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        // Bar Statistik Atas (Streak, XP, Profil)
        TopStatBar(
            streak = streak,
            xp = xp,
            level = level,
            onProfileClick = onNavigateToProfile
        )

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 640.dp)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 14.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 1. Kartu Sambutan & Sapaan Hangul
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFE2E8F0))
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "안녕하세요, $name! ✨",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = InkBlack
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    IconButton(
                                        onClick = { onSpeak("안녕하세요") },
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(HaneulContainer)
                                    ) {
                                        Icon(
                                            Icons.AutoMirrored.Filled.VolumeUp,
                                            contentDescription = "Bunyikan salam",
                                            tint = HaneulBlue,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "$levelTitle • Belajar Korea dari nol sampai bisa",
                                    fontSize = 12.sp,
                                    color = TextSecondary,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = NunbitYellowContainer,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { onNavigateToProfile() }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.School, contentDescription = null, tint = Color(0xFFCA8A04), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Lv. $level",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 12.sp,
                                        color = Color(0xFF854D0E)
                                    )
                                }
                            }
                        }
                    }
                }

                // 2. Kartu Maskot Hannie (Pesan Semangat Harimau Putih dengan Audio)
                item {
                    HannieMascotCard(
                        quote = "화이팅 (Hwaiting)! Konsisten 10–15 menit setiap hari jauh lebih efektif daripada belajar 3 jam sekaligus dalam seminggu!",
                        title = "Hannie (한이) Sang Maskot Harimau Putih",
                        onSpeak = { onSpeak("화이팅! 오늘도 힘내세요!") }
                    )
                }

                // 3. Kartu Target Belajar Hari Ini & Progress Harian
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
                                    text = if (goalProgress >= 1f) "🎉 Target tercapai hari ini! Luar biasa!" else "Sedikit lagi menuju target harian",
                                    fontSize = 11.sp,
                                    color = if (goalProgress >= 1f) Color(0xFF059669) else TextSecondary,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "$unlockedBadgeCount Badge Diraih 🏅",
                                    fontSize = 11.sp,
                                    color = Color(0xFFCA8A04),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // 4. Quick Resume Card (Lanjut Belajar Terakhir - FIX Navigasi ke targetModule)
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = HaneulBlue),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("quick_resume_card")
                            .clickable { onNavigateToModule(targetModule) }
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
                                    text = "$targetSubtitle • $completedLessonsCount materi tuntas",
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.85f)
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

                // 5. Kartu Kata Hari Ini (오늘의 단어 - Word of the Day)
                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFE2E8F0))
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.WbSunny, contentDescription = null, tint = NunbitYellow, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Kata Hari Ini (오늘의 단어)",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = InkBlack
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFF1F5F9),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { onNavigateToModule("vocab") }
                                ) {
                                    Text(
                                        text = "Lihat Semua 📚",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = HaneulBlue,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = NunbitYellowContainer,
                                        modifier = Modifier.size(48.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(text = "행", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF854D0E))
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(text = wordOfTheDayKorean, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = InkBlack)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(text = "[$wordOfTheDayRomanization]", fontSize = 13.sp, color = TextSecondary)
                                        }
                                        Text(text = wordOfTheDayMeaning, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = HaneulBlue)
                                    }
                                }

                                IconButton(
                                    onClick = { onSpeak("$wordOfTheDayKorean. $wordOfTheDayExampleKo") },
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(HaneulContainer)
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.VolumeUp,
                                        contentDescription = "Bunyikan Kata Hari Ini",
                                        tint = HaneulBlue,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFF8FAFC),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = "“$wordOfTheDayExampleKo”",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = InkBlack
                                    )
                                    Text(
                                        text = wordOfTheDayExampleId,
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }

                // 6. Tantangan Hari Ini (Daily Challenge Quests - dengan safe division & empty fallback)
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

                            if (dailyChallenges.isEmpty()) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFFF8FAFC),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onNavigateToModule("writing") }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.PlayCircle, contentDescription = null, tint = HaneulBlue)
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text("Selesaikan 1 Sesi Latihan Ketik Hangul", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = InkBlack)
                                            Text("Latih pengetikan keyboard 2-Beol-Sik untuk mendapatkan +15 XP", fontSize = 11.sp, color = TextSecondary)
                                        }
                                    }
                                }
                            } else {
                                dailyChallenges.forEach { ch ->
                                    val safeTarget = if (ch.targetCount > 0) ch.targetCount else 1
                                    val progressRatio = (ch.currentCount.toFloat() / safeTarget.toFloat()).coerceIn(0f, 1f)

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
                }

                // 7. Pengenalan Huruf Hangul Flashcard (Featured)
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
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            Color(0xFFEFF6FF),
                                            Color.White
                                        )
                                    )
                                )
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = HaneulContainer,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF93C5FD)),
                                    modifier = Modifier.size(50.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("한", fontSize = 26.sp, fontWeight = FontWeight.Black, color = HaneulBlue)
                                    }
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Pengenalan Huruf Hangul",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = InkBlack,
                                            modifier = Modifier.weight(1f, fill = false)
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
                                                maxLines = 1,
                                                softWrap = false,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = "Flashcard interaktif vokal & konsonan lengkap dengan audio & mnemonic bentuk huruf",
                                        fontSize = 11.sp,
                                        color = Color(0xFF475569),
                                        lineHeight = 15.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "✨ 14 Konsonan Dasar • 10 Vokal Dasar • Diftong",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = HaneulBlue
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Surface(
                                    shape = CircleShape,
                                    color = HaneulContainer,
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.AutoMirrored.Filled.ArrowForward,
                                            contentDescription = "Buka Hangul Flashcard",
                                            tint = HaneulBlue,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 8. Judul Bagian Modul Belajar
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Modul Belajar Praktis",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = InkBlack
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = HaneulContainer
                            ) {
                                Text(
                                    text = "6 Modul",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = HaneulBlue,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Dari Nol Sampai Bisa",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // 9. Grid 6 Modul Utama (2 Kolom)
                item {
                    val modules = listOf(
                        ModuleItem(
                            id = "writing",
                            title = "Ketik & Dokumen",
                            korean = "한글 타자 & 문서 쓰기",
                            desc = "Ketik huruf, suku kata, surat (편지), chat KakaoTalk & dokumen Korea asli",
                            icon = Icons.Default.Keyboard,
                            badgeColor = Color(0xFF2563EB),
                            badgeBg = Color(0xFFDBEAFE),
                            tag = "📜 Surat & Dokumen",
                            gradientStart = Color(0xFFEFF6FF)
                        ),
                        ModuleItem(
                            id = "speaking",
                            title = "Berbicara",
                            korean = "말하기 연습",
                            desc = "Role-play percakapan nyata & evaluasi suara",
                            icon = Icons.Default.Mic,
                            badgeColor = Color(0xFFD97706),
                            badgeBg = Color(0xFFFEF3C7),
                            tag = "🗣️ Interaktif STT",
                            gradientStart = Color(0xFFFFFBEB)
                        ),
                        ModuleItem(
                            id = "listening",
                            title = "Mendengar",
                            korean = "듣기 퀴즈",
                            desc = "Audio kuis, speed control & keypad",
                            icon = Icons.Default.Headphones,
                            badgeColor = Color(0xFF059669),
                            badgeBg = Color(0xFFD1FAE5),
                            tag = "🎧 12 Audio Soal",
                            gradientStart = Color(0xFFECFDF5)
                        ),
                        ModuleItem(
                            id = "vocab",
                            title = "Kosakata & SRS",
                            korean = "단어장 (SRS)",
                            desc = "SuperMemo-2 Spaced Repetition & matching game",
                            icon = Icons.Default.Book,
                            badgeColor = Color(0xFF7C3AED),
                            badgeBg = Color(0xFFEDE9FE),
                            tag = "🧠 Spaced Repetition",
                            gradientStart = Color(0xFFF5F3FF)
                        ),
                        ModuleItem(
                            id = "reading",
                            title = "Membaca Aloud",
                            korean = "읽기 연습",
                            desc = "Karaoke reader teks Korea, custom paste & WPM",
                            icon = Icons.AutoMirrored.Filled.MenuBook,
                            badgeColor = Color(0xFFDC2626),
                            badgeBg = Color(0xFFFEE2E2),
                            tag = "📖 Karaoke & WPM",
                            gradientStart = Color(0xFFFFF1F2)
                        ),
                        ModuleItem(
                            id = "video",
                            title = "Video Belajar",
                            korean = "영상 학습",
                            desc = "Subtitle ganda interaktif & latihan shadowing",
                            icon = Icons.Default.VideoLibrary,
                            badgeColor = Color(0xFF0D9488),
                            badgeBg = Color(0xFFCCFBF1),
                            tag = "🎬 Dual Subtitle",
                            gradientStart = Color(0xFFF0FDFA)
                        )
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        for (i in modules.indices step 2) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
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
                                } else {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }

                // 10. Panduan Tata Bahasa Hub (TOPIK I)
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFFDE68A))
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showGrammarHub = true }
                            .testTag("open_grammar_hub_card")
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            Color(0xFFFFFBEB),
                                            Color.White
                                        )
                                    )
                                )
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = Color(0xFFFEF3C7),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFCD34D)),
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("문법", fontWeight = FontWeight.Black, fontSize = 15.sp, color = Color(0xFF92400E))
                                            Text("Grammar", fontWeight = FontWeight.Bold, fontSize = 8.sp, color = Color(0xFFB45309))
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Panduan Tata Bahasa",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = InkBlack,
                                            modifier = Modifier.weight(1f, fill = false)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color(0xFFFEF3C7)
                                        ) {
                                            Text(
                                                text = "TOPIK I",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color(0xFF92400E),
                                                maxLines = 1,
                                                softWrap = false,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = "Pola SOV, Partikel, Sistem Angka (Sino & Asli), Waktu/Jam & Satuan Hitung",
                                        fontSize = 11.sp,
                                        color = Color(0xFF475569),
                                        lineHeight = 15.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "📚 Tata Bahasa & Sistem Angka Interaktif",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF92400E)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFFFEF3C7),
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.AutoMirrored.Filled.ArrowForward,
                                            contentDescription = "Buka Tata Bahasa",
                                            tint = Color(0xFF92400E),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 11. Panduan Keseharian di Korea (일상생활 Hub)
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFA7F3D0))
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDailyLifeHub = true }
                            .testTag("open_daily_life_hub_card")
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            Color(0xFFECFDF5),
                                            Color.White
                                        )
                                    )
                                )
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = Color(0xFFD1FAE5),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF6EE7B7)),
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("생활", fontWeight = FontWeight.Black, fontSize = 15.sp, color = Color(0xFF065F46))
                                            Text("Life", fontWeight = FontWeight.Bold, fontSize = 8.sp, color = Color(0xFF047857))
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Panduan Keseharian",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = InkBlack,
                                            modifier = Modifier.weight(1f, fill = false)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color(0xFFD1FAE5)
                                        ) {
                                            Text(
                                                text = "6 Situasi",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color(0xFF065F46),
                                                maxLines = 1,
                                                softWrap = false,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = "Ekspresi emosi & kesal, tegur sapa, toilet kafe, sepeda 따릉이 & etiket",
                                        fontSize = 11.sp,
                                        color = Color(0xFF475569),
                                        lineHeight = 15.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "☕ Kafe • 🚲 따릉이 • 🏠 Etiket",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF047857)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFFD1FAE5),
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.AutoMirrored.Filled.ArrowForward,
                                            contentDescription = "Buka Keseharian",
                                            tint = Color(0xFF065F46),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 12. Footer Branding & Author Credit
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
                }
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
    val badgeBg: Color,
    val tag: String,
    val gradientStart: Color
)

@Composable
fun ModuleCard(
    item: ModuleItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .border(
                width = 1.2.dp,
                color = item.badgeColor.copy(alpha = 0.22f),
                shape = RoundedCornerShape(20.dp)
            )
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .testTag("module_card_${item.id}")
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            item.gradientStart,
                            Color.White
                        )
                    )
                )
                .padding(14.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = item.badgeBg,
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                item.icon,
                                contentDescription = item.title,
                                tint = item.badgeColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Surface(
                        shape = CircleShape,
                        color = item.badgeBg,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Buka ${item.title}",
                                tint = item.badgeColor,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = item.badgeColor.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = item.korean,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = item.badgeColor,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = item.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = InkBlack
                )

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = item.desc,
                    fontSize = 11.sp,
                    color = Color(0xFF475569),
                    lineHeight = 15.sp,
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFF1F5F9)
                ) {
                    Text(
                        text = item.tag,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF334155),
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}
