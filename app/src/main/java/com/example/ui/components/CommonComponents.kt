package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
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
fun TopStatBar(
    streak: Int,
    xp: Int,
    level: Int,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier,
    onOpenStatsDetail: (() -> Unit)? = null
) {
    var showStreakDialog by remember { mutableStateOf(false) }
    var showXpDialog by remember { mutableStateOf(false) }

    Surface(
        color = Color.White,
        shadowElevation = 2.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // App Mini Branding
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onProfileClick() }
                    .padding(4.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_app_icon),
                    contentDescription = "Logo Korean In Dulu",
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Korean In Dulu",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = HaneulBlue
                    )
                    Text(
                        text = "by Bangdillah210",
                        fontSize = 10.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Gamification Badges (Streak & XP)
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Streak Pill
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFFFFFBEB),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.linearGradient(listOf(NunbitYellow, Color(0xFFF97316)))
                    ),
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable {
                            if (onOpenStatsDetail != null) onOpenStatsDetail() else showStreakDialog = true
                        }
                        .testTag("streak_badge")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.LocalFireDepartment,
                            contentDescription = "Streak Hari",
                            tint = Color(0xFFEA580C),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "$streak",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFFC2410C)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // XP Pill
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = NunbitYellowContainer,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable {
                            if (onOpenStatsDetail != null) onOpenStatsDetail() else showXpDialog = true
                        }
                        .testTag("xp_badge")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = "XP",
                            tint = Color(0xFFCA8A04),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "$xp XP",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFF854D0E)
                        )
                    }
                }
            }
        }
    }

    // Streak Info Modal Dialog
    if (showStreakDialog) {
        AlertDialog(
            onDismissRequest = { showStreakDialog = false },
            confirmButton = {
                Button(
                    onClick = { showStreakDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = HaneulBlue),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Siap, Pertahankan! 🔥")
                }
            },
            icon = {
                Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = Color(0xFFEA580C), modifier = Modifier.size(36.dp))
            },
            title = {
                Text("Streak Belajar $streak Hari!", fontWeight = FontWeight.Bold, fontSize = 17.sp, textAlign = TextAlign.Center)
            },
            text = {
                Text(
                    text = "Api belajarmu sedang berkobar! Selesaikan minimal satu modul pelajaran setiap hari sebelum tengah malam untuk menjaga streak tetap aktif dan meraih bonus XP.",
                    fontSize = 13.sp,
                    color = TextSecondary,
                    lineHeight = 18.sp,
                    textAlign = TextAlign.Center
                )
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White
        )
    }

    // XP Info Modal Dialog
    if (showXpDialog) {
        AlertDialog(
            onDismissRequest = { showXpDialog = false },
            confirmButton = {
                Button(
                    onClick = { showXpDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = HaneulBlue),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Kumpulkan Lebih Banyak ⭐")
                }
            },
            icon = {
                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFCA8A04), modifier = Modifier.size(36.dp))
            },
            title = {
                Text("Total $xp XP Diperoleh", fontWeight = FontWeight.Bold, fontSize = 17.sp, textAlign = TextAlign.Center)
            },
            text = {
                Text(
                    text = "XP (Experience Points) mencerminkan ketekunan belajarmu. Dapatkan XP dari:\n• Menulis Hangul (+10-20 XP)\n• Latihan Speaking & Role-play (+25-35 XP)\n• Listening Quiz (+15 XP)\n• Spaced Repetition SRS (+15 XP)\n\nKumpulkan terus untuk membuka badge kehormatan!",
                    fontSize = 13.sp,
                    color = TextSecondary,
                    lineHeight = 18.sp
                )
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White
        )
    }
}

@Composable
fun HannieMascotCard(
    quote: String,
    title: String = "Pesan Semangat dari Hannie (한이)",
    onSpeak: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(1.5.dp, Color(0xFFE2E8F0), RoundedCornerShape(22.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_mascot_hannie),
                contentDescription = "Maskot Hannie si Harimau Putih",
                modifier = Modifier
                    .size(76.dp)
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = Color(0xFFEEF2FF),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = title,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = HaneulBlue,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    if (onSpeak != null) {
                        IconButton(
                            onClick = onSpeak,
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(HaneulContainer)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Dengarkan pesan Hannie",
                                tint = HaneulBlue,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = quote,
                    fontSize = 13.sp,
                    color = InkBlack,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
fun AppSubHeader(
    title: String,
    subtitle: String? = null,
    onBack: () -> Unit,
    trailingAction: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.testTag("back_button")
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Kembali",
                tint = HaneulBlue
            )
        }

        Spacer(modifier = Modifier.width(4.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = InkBlack
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }

        trailingAction?.invoke()
    }
}

@Composable
fun AudioSpeakerButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = HaneulContainer,
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .testTag("audio_tts_speaker_button")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.AutoMirrored.Filled.VolumeUp,
                contentDescription = "Dengarkan Audio Suara",
                tint = HaneulBlue,
                modifier = Modifier.size(20.dp)
            )
            if (label != null) {
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = label,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = HaneulBlue
                )
            }
        }
    }
}
