package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AppSubHeader
import com.example.ui.components.HangulKeyboardUtil
import com.example.ui.theme.HaneulBlue
import com.example.ui.theme.HaneulContainer
import com.example.ui.theme.InkBlack
import com.example.ui.theme.NunbitYellow
import com.example.ui.theme.NunbitYellowContainer
import com.example.ui.theme.SaessakGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary

/**
 * Halaman Khusus Detail Karakter Hangul (한글 글자 상세 화면)
 * Menggantikan dialog modal pop-up menjadi halaman utuh yang luas,
 * menjelaskan mnemonik visual, anatomi bentuk, arah coretan, audio TTS,
 * dan tombol langsung untuk latihan ketik serta coretan.
 */
@Composable
fun HangulLetterDetailScreen(
    character: String,
    onBack: () -> Unit,
    onSpeak: (String) -> Unit,
    onNavigateToWriting: (String) -> Unit,
    onNavigateToKeyboard: ((String) -> Unit)? = null
) {
    val item = remember(character) {
        hangulCatalog.find { it.character == character } ?: hangulCatalog.first()
    }

    val qwertyChar = remember(item.character) {
        item.character.firstOrNull()?.let { HangulKeyboardUtil.getQwertyKeyHint(it) } ?: "K"
    }
    val isLeftHand = remember(item.character) {
        item.character.firstOrNull()?.let { HangulKeyboardUtil.isLeftHandConsonant(it) } ?: false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .testTag("hangul_letter_detail_screen")
    ) {
        AppSubHeader(
            title = "Detail Huruf ${item.character} (${item.name})",
            subtitle = "Kategori: ${item.category} • ${item.romanization}",
            onBack = onBack
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Kartu Karakter Raksasa & Audio Player
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.horizontalGradient(listOf(Color(0xFF93C5FD), Color(0xFFBFDBFE)))
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = RoundedCornerShape(22.dp),
                            color = HaneulContainer,
                            modifier = Modifier.size(110.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = item.character,
                                    fontSize = 64.sp,
                                    fontWeight = FontWeight.Black,
                                    color = HaneulBlue
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = item.name,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = InkBlack
                        )

                        Text(
                            text = "Romanisasi: [${item.romanization}]",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = HaneulBlue
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { onSpeak(item.audioQuery) },
                            colors = ButtonDefaults.buttonColors(containerColor = HaneulBlue),
                            shape = RoundedCornerShape(14.dp),
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                            modifier = Modifier.testTag("detail_play_audio_btn")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Dengarkan Bunyi Huruf", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }

            // 2. Tips Pengucapan & Posisi Mulut
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = HaneulBlue, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Panduan Pengucapan", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = InkBlack)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = item.pronunciationTip,
                            fontSize = 13.sp,
                            color = Color(0xFF334155),
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            // 3. Mnemonik Bentuk & Asal Usul
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFFCD34D))
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lightbulb, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Jembatan Keledai (Mnemonik Bentuk)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF92400E))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = item.mnemonicTip,
                            fontSize = 13.sp,
                            color = Color(0xFF78350F),
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            // 4. Posisi Tuts Keyboard 2-Beol-Sik
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
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
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Keyboard, contentDescription = null, tint = HaneulBlue, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Letak di Keyboard 2-Beol-Sik", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = InkBlack)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isLeftHand) "Jari Tangan Kiri (Konsonan)" else "Jari Tangan Kanan (Vokal)",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFF1F5F9),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1)),
                            modifier = Modifier.size(46.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = qwertyChar,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = InkBlack
                                )
                            }
                        }
                    }
                }
            }

            // 5. Urutan Coretan (Stroke Order)
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
                            Text("Urutan Coretan", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = InkBlack)
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = HaneulContainer
                            ) {
                                Text(
                                    text = "${item.strokeCount} Coretan",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = HaneulBlue,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        item.strokeDirections.forEachIndexed { idx, dir ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = HaneulBlue,
                                    modifier = Modifier.size(22.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("${idx + 1}", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(text = dir, fontSize = 13.sp, color = InkBlack)
                            }
                        }
                    }
                }
            }

            // 6. Contoh Kosakata
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
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
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Contoh Kosakata yang Menggunakan Huruf Ini:", fontSize = 11.sp, color = TextSecondary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(item.exampleWordKo, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = InkBlack)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("[${item.exampleWordRo}]", fontSize = 13.sp, color = TextSecondary)
                            }
                            Text(text = "Artinya: ${item.exampleWordMeaning}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = HaneulBlue)
                        }

                        IconButton(
                            onClick = { onSpeak("${item.exampleWordKo}. ${item.exampleWordMeaning}") },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(HaneulContainer)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, tint = HaneulBlue)
                        }
                    }
                }
            }

            // 7. Tombol Aksi Praktik
            item {
                Spacer(modifier = Modifier.height(6.dp))
                Button(
                    onClick = { onNavigateToWriting(item.character) },
                    colors = ButtonDefaults.buttonColors(containerColor = HaneulBlue),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("detail_goto_writing_btn")
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Latihan Menulis Huruf Ini di Kanvas", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                if (onNavigateToKeyboard != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { onNavigateToKeyboard(item.character) },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("detail_goto_keyboard_btn")
                    ) {
                        Icon(Icons.Default.Keyboard, contentDescription = null, tint = HaneulBlue)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Latihan Ketik Huruf Ini di Keyboard 2-Beol-Sik", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = HaneulBlue)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
