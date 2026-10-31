package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import com.example.data.model.BadgeItem
import com.example.data.model.UnlockedBadge
import com.example.data.model.UserProfile
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
import kotlin.math.roundToInt

@Composable
fun ProfileScreen(
    userProfile: UserProfile?,
    completedLessonsCount: Int,
    learningMinutes: Int,
    unlockedBadges: List<UnlockedBadge>,
    masterBadges: List<BadgeItem>,
    onBack: () -> Unit,
    onSpeakTest: (String, Float) -> Unit,
    onToggleRomanization: (Boolean) -> Unit,
    onUpdateTtsSpeed: (Float) -> Unit,
    onResetOnboarding: () -> Unit
) {
    val profile = userProfile ?: UserProfile()
    var currentSpeed by remember(profile.ttsSpeed) { mutableFloatStateOf(profile.ttsSpeed) }
    var showRom by remember(profile.showRomanization) { mutableStateOf(profile.showRomanization) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        AppSubHeader(
            title = "Profil & Pengaturan",
            subtitle = "Kelola akun, preferensi belajar, badge & info pengembang",
            onBack = onBack
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Card Header
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
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
                    Image(
                        painter = painterResource(id = R.drawable.ic_mascot_hannie),
                        contentDescription = "Avatar Maskot Hannie",
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .border(3.dp, NunbitYellow, CircleShape),
                        contentScale = ContentScale.Fit
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(text = profile.name, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = InkBlack)
                    Text(text = profile.email, fontSize = 12.sp, color = TextSecondary)

                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(shape = RoundedCornerShape(20.dp), color = HaneulContainer) {
                        Text(
                            text = when (profile.level) {
                                3 -> "Level 3: Menengah (중급)"
                                2 -> "Level 2: Pemula (초급)"
                                else -> "Level 1: Dasar Hangul (입문)"
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = HaneulBlue,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Gamification 3 Stats Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFE2E8F0))
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = Color(0xFFEA580C), modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "${profile.streak} Hari", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = InkBlack)
                        Text(text = "Streak", fontSize = 11.sp, color = TextSecondary)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFE2E8F0))
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFCA8A04), modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "${profile.xp}", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = InkBlack)
                        Text(text = "Total XP", fontSize = 11.sp, color = TextSecondary)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFE2E8F0))
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Timer, contentDescription = null, tint = HaneulBlue, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "$learningMinutes m", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = InkBlack)
                        Text(text = "Waktu Belajar", fontSize = 11.sp, color = TextSecondary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Badges & Achievements Section
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(20.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Pencapaian & Badge (${unlockedBadges.size}/${masterBadges.size})", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = InkBlack)
                        Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Color(0xFFCA8A04), modifier = Modifier.size(20.dp))
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(horizontal = 2.dp)
                    ) {
                        items(masterBadges) { badge ->
                            val isUnlocked = unlockedBadges.any { it.badgeId == badge.id }
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (isUnlocked) NunbitYellowContainer else Color(0xFFF1F5F9),
                                border = CardDefaults.outlinedCardBorder().copy(
                                    brush = androidx.compose.ui.graphics.SolidColor(if (isUnlocked) NunbitYellow else Color(0xFFCBD5E1))
                                ),
                                modifier = Modifier.width(130.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(text = badge.iconSymbol, fontSize = 28.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = badge.title,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isUnlocked) Color(0xFF854D0E) else TextSecondary,
                                        textAlign = TextAlign.Center,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = if (isUnlocked) "Terbuka ✨" else "Terkunci",
                                        fontSize = 9.sp,
                                        color = if (isUnlocked) Color(0xFF059669) else TextMuted
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Learning Preferences Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(20.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Preferensi Belajar & Audio:", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = InkBlack)

                    Spacer(modifier = Modifier.height(12.dp))

                    // Romanization Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Tampilkan Romanisasi Latin", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = InkBlack)
                            Text(text = "Matikan untuk melatih membaca langsung huruf Hangul", fontSize = 11.sp, color = TextSecondary)
                        }
                        Switch(
                            checked = showRom,
                            onCheckedChange = {
                                showRom = it
                                onToggleRomanization(it)
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = HaneulBlue),
                            modifier = Modifier.testTag("toggle_romanization_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // TTS Speed Slider with "Tes Suara" Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Kecepatan Suara TTS: ${(currentSpeed * 100).roundToInt()}%",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = InkBlack
                        )

                        Button(
                            onClick = {
                                onSpeakTest("안녕하세요! 코리안인들루에서 한국어를 배워요.", currentSpeed)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = HaneulContainer),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Icon(Icons.Default.VolumeUp, contentDescription = null, tint = HaneulBlue, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Tes Suara", fontSize = 11.sp, color = HaneulBlue, fontWeight = FontWeight.Bold)
                        }
                    }

                    Slider(
                        value = currentSpeed,
                        onValueChange = {
                            currentSpeed = it
                            onUpdateTtsSpeed(it)
                        },
                        valueRange = 0.6f..1.4f,
                        steps = 7,
                        colors = SliderDefaults.colors(thumbColor = HaneulBlue, activeTrackColor = HaneulBlue),
                        modifier = Modifier.testTag("tts_speed_slider")
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Developer & App Identity Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(20.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_logo_korean_in_dulu),
                            contentDescription = "Korean In Dulu",
                            modifier = Modifier.height(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(text = "Pengembang: Achmat Fadilah (Bangdillah210)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = InkBlack)
                    Text(text = "Tagline: \"Belajar Korea, dari nol sampai bisa.\"", fontSize = 12.sp, color = HaneulBlue, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                    Text(text = "Versi Aplikasi: 1.0.0 (Production Release)", fontSize = 11.sp, color = TextSecondary)
                    Text(text = "Arsitektur: Modular MVVM + Clean Architecture + Room Offline-First", fontSize = 11.sp, color = TextMuted)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Reset Onboarding Option
            Button(
                onClick = onResetOnboarding,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEE2E2)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reset_onboarding_button")
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, tint = SarangRed)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Ulangi Tes Penempatan & Onboarding", color = SarangRed, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
