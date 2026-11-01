package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Keyboard
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
import com.example.ui.theme.SaessakGreen
import com.example.ui.theme.TextSecondary

/**
 * Halaman Khusus Tambah Kosakata Kustom (단어 추가 화면)
 * Menggantikan dialog alert kecil dengan halaman formulir penuh yang elegan,
 * lengkap dengan dekomposisi huruf Hangul, uji audio sebelum simpan, dan kategori.
 */
@Composable
fun AddCustomVocabScreen(
    onBack: () -> Unit,
    onSpeak: (String) -> Unit,
    onAddCustomVocab: (korean: String, meaning: String, category: String) -> Unit,
    onAddXp: ((Int) -> Unit)? = null
) {
    val context = LocalContext.current
    var inputKorean by remember { mutableStateOf("") }
    var inputMeaning by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Kustom") }

    val categories = listOf("Kustom", "Kuliner", "Drama & Lagu", "Travel", "Percakapan", "Benda", "Pekerjaan")

    val decomposedJamos = remember(inputKorean) {
        if (inputKorean.isNotBlank()) HangulKeyboardUtil.decompose(inputKorean)
        else emptyList()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .testTag("add_custom_vocab_screen")
    ) {
        AppSubHeader(
            title = "Tambah Kosakata Baru (단어 추가)",
            subtitle = "Simpan kata atau kalimat baru ke sistem flashcard Spaced Repetition (SRS)",
            onBack = onBack
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Kartu Penjelasan
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFE2E8F0))
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = NunbitYellow, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Kosakata yang Anda simpan akan diulang otomatis menggunakan algoritma SuperMemo-2 SRS untuk daya ingat jangka panjang.",
                        fontSize = 12.sp,
                        color = Color(0xFF334155),
                        lineHeight = 16.sp
                    )
                }
            }

            // Input Kata Hangul
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Kata / Kalimat Korea (Hangul) *",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = InkBlack
                )
                OutlinedTextField(
                    value = inputKorean,
                    onValueChange = { inputKorean = it },
                    placeholder = { Text("Contoh: 봄, 바다, 맛있어요") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = HaneulBlue,
                        unfocusedBorderColor = Color(0xFFE2E8F0)
                    ),
                    trailingIcon = {
                        if (inputKorean.isNotBlank()) {
                            IconButton(onClick = { onSpeak(inputKorean) }) {
                                Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Dengarkan", tint = HaneulBlue)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_korean_text_field")
                )
            }

            // Live Preview Dekomposisi Huruf
            if (decomposedJamos.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFEFF6FF),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFDBFE)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Keyboard, contentDescription = null, tint = HaneulBlue, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Struktur Huruf Jamo (Keyboard 2-Beol-Sik):",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = HaneulBlue
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            decomposedJamos.forEach { jamo ->
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color.White,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF93C5FD))
                                ) {
                                    Text(
                                        text = "$jamo",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = InkBlack,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Input Arti Bahasa Indonesia
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Arti Bahasa Indonesia *",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = InkBlack
                )
                OutlinedTextField(
                    value = inputMeaning,
                    onValueChange = { inputMeaning = it },
                    placeholder = { Text("Contoh: Musim semi, Laut, Enak") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = HaneulBlue,
                        unfocusedBorderColor = Color(0xFFE2E8F0)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_meaning_text_field")
                )
            }

            // Pilih Kategori
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Pilih Kategori:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = InkBlack
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(categories) { cat ->
                        val isSelected = selectedCategory == cat
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) HaneulBlue else Color.White,
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = androidx.compose.ui.graphics.SolidColor(
                                    if (isSelected) HaneulBlue else Color(0xFFE2E8F0)
                                )
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { selectedCategory = cat }
                        ) {
                            Text(
                                text = cat,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else TextSecondary,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Tombol Simpan
            Button(
                onClick = {
                    if (inputKorean.isNotBlank() && inputMeaning.isNotBlank()) {
                        onAddCustomVocab(inputKorean.trim(), inputMeaning.trim(), selectedCategory)
                        onAddXp?.invoke(10)
                        Toast.makeText(context, "Berhasil menambahkan \"${inputKorean.trim()}\" (+10 XP)", Toast.LENGTH_SHORT).show()
                        onBack()
                    } else {
                        Toast.makeText(context, "Harap isi kata Korea dan artinya", Toast.LENGTH_SHORT).show()
                    }
                },
                enabled = inputKorean.isNotBlank() && inputMeaning.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = HaneulBlue),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_custom_vocab_btn")
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Simpan ke Bank Kosakata (+10 XP)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}
