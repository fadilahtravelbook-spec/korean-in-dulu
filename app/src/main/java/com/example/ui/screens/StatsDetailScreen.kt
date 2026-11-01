package com.example.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
import com.example.ui.theme.SarangRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary

@Composable
fun StatsDetailScreen(
    streak: Int,
    xp: Int,
    level: Int,
    completedLessons: Int,
    onBack: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onUseStreakFreeze: () -> Unit
) {
    var isFreezeEquipped by remember { mutableStateOf(true) }
    val (targetTitle, xpNeeded, currentLevelProgress) = when {
        level <= 1 -> Triple("Menuju Level 2 (초급)", (300 - xp).coerceAtLeast(0), (xp.toFloat() / 300f).coerceIn(0f, 1f))
        level == 2 -> Triple("Menuju Level 3 (중급)", (600 - xp).coerceAtLeast(0), ((xp - 300).toFloat() / 300f).coerceIn(0f, 1f))
        else -> Triple("Level 3: Menengah (중급) - Maksimal", 0, 1f)
    }

    val daysOfWeek = listOf("Sen", "Sel", "Rab", "Kam", "Jum", "Sab", "Min")
    val currentDayIndex = 5 // e.g. Saturday

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        AppSubHeader(
            title = "Statistik & Gamifikasi Belajar",
            subtitle = "Pantau konsistensi belajar, streak harian, dan akumulasi poin pengalaman (XP)",
            onBack = onBack
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Big 4 Metrics Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Streak Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF7ED)),
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, Color(0xFFFED7AA), RoundedCornerShape(16.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFFFEDD5),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = Color(0xFFEA580C), modifier = Modifier.size(24.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "$streak Hari", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFC2410C))
                        Text(text = "Streak Beruntun", fontSize = 11.sp, color = TextSecondary)
                    }
                }

                // XP Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEFCE8)),
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, Color(0xFFFEF08A), RoundedCornerShape(16.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFFEF9C3),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Bolt, contentDescription = null, tint = NunbitYellow, modifier = Modifier.size(24.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "$xp XP", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF854D0E))
                        Text(text = "Total Pengalaman", fontSize = 11.sp, color = TextSecondary)
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Level Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, Color(0xFFBFDBFE), RoundedCornerShape(16.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = HaneulContainer,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = HaneulBlue, modifier = Modifier.size(24.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Level $level", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = HaneulBlue)
                        Text(text = "Pangkat Pembelajar", fontSize = 11.sp, color = TextSecondary)
                    }
                }

                // Lessons Completed Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, Color(0xFFBBF7D0), RoundedCornerShape(16.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFDCFCE7),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, tint = SaessakGreen, modifier = Modifier.size(24.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "$completedLessons Modul", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF166534))
                        Text(text = "Materi Tuntas", fontSize = 11.sp, color = TextSecondary)
                    }
                }
            }

            // Streak Calendar Card
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(18.dp))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Konsistensi Minggu Ini",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = InkBlack
                        )
                        Text(
                            text = "Target: 7/7 Hari",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        daysOfWeek.forEachIndexed { idx, day ->
                            val isCompleted = idx <= currentDayIndex
                            val isToday = idx == currentDayIndex

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Surface(
                                    shape = CircleShape,
                                    color = when {
                                        isToday -> Color(0xFFEA580C)
                                        isCompleted -> Color(0xFFFFEDD5)
                                        else -> Color(0xFFF1F5F9)
                                    },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        if (isCompleted) {
                                            Icon(
                                                Icons.Default.LocalFireDepartment,
                                                contentDescription = null,
                                                tint = if (isToday) Color.White else Color(0xFFEA580C),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        } else {
                                            Text(
                                                text = "•",
                                                color = TextMuted,
                                                fontSize = 16.sp
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = day,
                                    fontSize = 10.sp,
                                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isToday) InkBlack else TextSecondary
                                )
                            }
                        }
                    }
                }
            }

            // Level Progress & Next Milestones
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(18.dp))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = targetTitle,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = InkBlack
                        )
                        Text(
                            text = "${(currentLevelProgress * 100).toInt()}%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = HaneulBlue
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = { currentLevelProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = HaneulBlue,
                        trackColor = Color(0xFFE2E8F0)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (level >= 3) "Selamat! Anda telah mencapai level kemahiran tertinggi saat ini." else "Kumpulkan $xpNeeded XP lagi untuk naik ke level berikutnya dan membuka lencana prestasi baru!",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }

            // Streak Freeze Protection Card
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F9FF)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFBAE6FD), RoundedCornerShape(18.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFE0F2FE),
                        modifier = Modifier.size(46.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.AcUnit, contentDescription = null, tint = HaneulBlue, modifier = Modifier.size(24.dp))
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Perisai Pembeku Streak",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = InkBlack
                        )
                        Text(
                            text = if (isFreezeEquipped) "Aktif: Streak Anda tetap aman jika terlupa belajar 1 hari." else "Nonaktif: Gunakan pembeku saat bepergian atau sibuk.",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            isFreezeEquipped = !isFreezeEquipped
                            onUseStreakFreeze()
                        },
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text(
                            text = if (isFreezeEquipped) "Terpasang" else "Pakai",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = HaneulBlue
                        )
                    }
                }
            }

            // Quick Link to Profile
            Button(
                onClick = onNavigateToProfile,
                colors = ButtonDefaults.buttonColors(containerColor = HaneulBlue),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("stats_to_profile_btn")
            ) {
                Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Buka Profil Lengkap & Sertifikat", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}
