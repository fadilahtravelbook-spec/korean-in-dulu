package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AppSubHeader
import com.example.ui.components.BatchimGuideView
import com.example.ui.theme.HaneulBlue
import com.example.ui.theme.InkBlack

/**
 * Halaman Khusus Panduan Aturan Batchim (받침 법칙)
 * Membahas 7 Bunyi Pokok Konsonan Akhir, 4 Hukum Fonologi (Peluluhan, Nasalisasi, Aspirasi, Tensifikasi),
 * dan Kuis Kilat Uji Pemahaman.
 */
@Composable
fun BatchimGuideScreen(
    onBack: () -> Unit,
    onSpeak: (String, Float) -> Unit,
    onStopSpeak: () -> Unit = {},
    onRecordProgress: (moduleId: String, lessonId: String, title: String, score: Int) -> Unit
) {
    var ttsSpeed by remember { mutableFloatStateOf(0.9f) }

    DisposableEffect(Unit) {
        onDispose {
            onStopSpeak()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .testTag("batchim_guide_screen")
    ) {
        AppSubHeader(
            title = "Panduan Aturan Batchim (받침 법칙)",
            subtitle = "Kuasai 7 bunyi pokok konsonan akhir & aturan peluluhan bunyi dalam percakapan",
            onBack = onBack
        )

        // Toolbar Kecepatan Audio TTS
        Surface(
            color = Color(0xFFF1F5F9),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Speed,
                        contentDescription = null,
                        tint = HaneulBlue,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Kecepatan Audio:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = InkBlack
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf(
                        0.6f to "0.6x",
                        0.8f to "0.8x",
                        1.0f to "1.0x",
                        1.2f to "1.2x"
                    ).forEach { (spd, label) ->
                        val isSel = ttsSpeed == spd
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSel) HaneulBlue else Color.White,
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = androidx.compose.ui.graphics.SolidColor(
                                    if (isSel) HaneulBlue else Color(0xFFCBD5E1)
                                )
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { ttsSpeed = spd }
                        ) {
                            Text(
                                text = label,
                                fontSize = 10.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSel) Color.White else InkBlack,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(14.dp)
        ) {
            BatchimGuideView(
                onSpeak = { text -> onSpeak(text, ttsSpeed) },
                onRecordProgress = onRecordProgress
            )
        }
    }
}
