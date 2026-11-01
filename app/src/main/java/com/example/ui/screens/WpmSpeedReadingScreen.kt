package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.ReadingStory
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
import com.example.util.SpeechRecognitionHelper
import kotlin.math.max

data class SpeedReadingResult(
    val wpm: Int,
    val accuracy: Int,
    val elapsedSeconds: Int,
    val feedback: String,
    val recognizedText: String
)

@Composable
fun WpmSpeedReadingScreen(
    stories: List<ReadingStory>,
    onBack: () -> Unit,
    onSpeak: (String) -> Unit,
    onRecordProgress: (moduleId: String, lessonId: String, title: String, score: Int) -> Unit
) {
    val context = LocalContext.current
    val speechHelper = remember { SpeechRecognitionHelper(context) }

    DisposableEffect(Unit) {
        onDispose {
            speechHelper.stopListening()
        }
    }

    var selectedStoryIndex by remember { mutableIntStateOf(0) }
    val currentStory = stories.getOrElse(selectedStoryIndex) { stories.firstOrNull() }
    val storyTitle = currentStory?.let { "${it.titleKo} (${it.titleId})" } ?: ""
    val storyKorean = currentStory?.paragraphs?.joinToString(" ") { it.sentenceKo } ?: ""
    val storyRomanization = currentStory?.paragraphs?.joinToString(" ") { it.sentenceRo } ?: ""
    val storyTranslation = currentStory?.paragraphs?.joinToString(" ") { it.sentenceId } ?: ""

    var isRecording by remember { mutableStateOf(false) }
    var startTimeMs by remember { mutableLongStateOf(0L) }
    var recognizedResultText by remember { mutableStateOf("") }
    var resultMetric by remember { mutableStateOf<SpeedReadingResult?>(null) }
    var showRomanization by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted && currentStory != null) {
            isRecording = true
            startTimeMs = System.currentTimeMillis()
            speechHelper.startListening(
                onResult = { text ->
                    recognizedResultText = text
                    val durationSec = max(1, ((System.currentTimeMillis() - startTimeMs) / 1000).toInt())
                    val expectedKorean = storyKorean
                    val wordsCount = expectedKorean.split(Regex("\\s+")).filter { it.isNotBlank() }.size
                    val calculatedWpm = ((wordsCount.toFloat() / durationSec) * 60).toInt().coerceIn(30, 220)

                    // Word matching accuracy
                    val cleanExpected = expectedKorean.replace(Regex("[^가-힣]"), "")
                    val cleanSpoken = text.replace(Regex("[^가-힣]"), "")
                    val matchScore = if (cleanExpected.isEmpty()) 80 else {
                        val matched = cleanSpoken.count { cleanExpected.contains(it) }
                        ((matched.toFloat() / cleanExpected.length) * 100).toInt().coerceIn(40, 100)
                    }

                    val feedback = when {
                        calculatedWpm >= 110 && matchScore >= 80 -> "Luar biasa! Pelafalan lancar dan kecepatan membaca mendekati penutur asli Korea."
                        calculatedWpm >= 70 && matchScore >= 65 -> "Bagus sekali! Ritme membaca stabil. Tingkatkan pelafalan vokal dan konsonan akhir (batchim)."
                        else -> "Usaha yang bagus! Coba dengarkan audio referensi sekali lagi dan ucapkan dengan tenang tanpa terburu-buru."
                    }

                    resultMetric = SpeedReadingResult(
                        wpm = calculatedWpm,
                        accuracy = matchScore,
                        elapsedSeconds = durationSec,
                        feedback = feedback,
                        recognizedText = text
                    )
                    isRecording = false
                    onRecordProgress("reading", currentStory.id, "WPM Reading: $storyTitle", matchScore)
                },
                onError = {
                    isRecording = false
                }
            )
        }
    }

    val pulseTransition = rememberInfiniteTransition(label = "mic_pulse")
    val pulseScale by pulseTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        AppSubHeader(
            title = "Tes Kecepatan Membaca (WPM)",
            subtitle = "Latihan membaca nyaring untuk mengukur kelancaran dan akurasi pelafalan",
            onBack = onBack
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Story Selector Chips
            Text(
                text = "PILIH BACAAN:",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                itemsIndexed(stories) { index, storyItem ->
                    val isSelected = index == selectedStoryIndex
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) HaneulBlue else Color.White,
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(
                                if (isSelected) HaneulBlue else Color(0xFFE2E8F0)
                            )
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .clickable {
                                selectedStoryIndex = index
                                resultMetric = null
                                recognizedResultText = ""
                            }
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
                            Text(
                                text = "${storyItem.titleKo} (${storyItem.titleId})",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else InkBlack
                            )
                            Text(
                                text = storyItem.level,
                                fontSize = 10.sp,
                                color = if (isSelected) Color(0xFFE0F2FE) else TextMuted
                            )
                        }
                    }
                }
            }

            if (currentStory != null) {
                // Passage Card
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
                                text = storyTitle,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = InkBlack
                            )

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (showRomanization) HaneulContainer else Color(0xFFF1F5F9),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { showRomanization = !showRomanization }
                                ) {
                                    Text(
                                        text = if (showRomanization) "Sembunyikan Latin" else "Tampilkan Latin",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (showRomanization) HaneulBlue else TextSecondary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(6.dp))

                                IconButton(
                                    onClick = { onSpeak(storyKorean) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.VolumeUp,
                                        contentDescription = "Dengarkan Referensi",
                                        tint = HaneulBlue,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Korean Text
                        Text(
                            text = storyKorean,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = InkBlack,
                            lineHeight = 28.sp
                        )

                        if (showRomanization && storyRomanization.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = storyRomanization,
                                fontSize = 13.sp,
                                color = TextSecondary,
                                lineHeight = 20.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = storyTranslation,
                            fontSize = 12.sp,
                            color = TextMuted,
                            lineHeight = 18.sp
                        )
                    }
                }

                // Recording Action Card
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isRecording) Color(0xFFFEF2F2) else Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            if (isRecording) SarangRed else Color(0xFFE2E8F0),
                            RoundedCornerShape(18.dp)
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (isRecording) "Merekam suara Anda secara langsung..." else "Siap untuk membaca?",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isRecording) SarangRed else InkBlack
                        )
                        Text(
                            text = if (isRecording) "Bacalah teks Korea di atas dengan suara lantang dan jelas. Tekan tombol stop setelah selesai."
                            else "Tekan tombol mikrofon di bawah dan bacalah teks Korea di atas secara nyaring.",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                        )

                        // Big Mic / Stop Button
                        Surface(
                            shape = CircleShape,
                            color = if (isRecording) SarangRed else HaneulBlue,
                            modifier = Modifier
                                .size(72.dp)
                                .scale(if (isRecording) pulseScale else 1f)
                                .clip(CircleShape)
                                .clickable {
                                    if (isRecording) {
                                        speechHelper.stopListening()
                                        isRecording = false
                                    } else {
                                        val hasPermission = ContextCompat.checkSelfPermission(
                                            context,
                                            Manifest.permission.RECORD_AUDIO
                                        ) == PackageManager.PERMISSION_GRANTED
                                        if (hasPermission) {
                                            isRecording = true
                                            startTimeMs = System.currentTimeMillis()
                                            speechHelper.startListening(
                                                onResult = { text ->
                                                    recognizedResultText = text
                                                    val durationSec = max(1, ((System.currentTimeMillis() - startTimeMs) / 1000).toInt())
                                                    val wordsCount = storyKorean.split(Regex("\\s+")).filter { it.isNotBlank() }.size
                                                    val calculatedWpm = ((wordsCount.toFloat() / durationSec) * 60).toInt().coerceIn(30, 220)
                                                    val cleanExpected = storyKorean.replace(Regex("[^가-힣]"), "")
                                                    val cleanSpoken = text.replace(Regex("[^가-힣]"), "")
                                                    val matchScore = if (cleanExpected.isEmpty()) 80 else {
                                                        val matched = cleanSpoken.count { cleanExpected.contains(it) }
                                                        ((matched.toFloat() / cleanExpected.length) * 100).toInt().coerceIn(40, 100)
                                                    }
                                                    resultMetric = SpeedReadingResult(
                                                        wpm = calculatedWpm,
                                                        accuracy = matchScore,
                                                        elapsedSeconds = durationSec,
                                                        feedback = "Bagus! Anda membaca ${wordsCount} kata dalam $durationSec detik.",
                                                        recognizedText = text
                                                    )
                                                    isRecording = false
                                                    onRecordProgress("reading", currentStory.id, "WPM: $storyTitle", matchScore)
                                                },
                                                onError = { isRecording = false }
                                            )
                                        } else {
                                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                        }
                                    }
                                }
                                .testTag("wpm_record_mic_btn")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                                    contentDescription = if (isRecording) "Berhenti" else "Mulai Rekam",
                                    tint = Color.White,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = if (isRecording) "Ketuk untuk Selesai & Hitung WPM" else "Ketuk untuk Mulai Rekam",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isRecording) SarangRed else HaneulBlue
                        )

                        val isSpeechAvailable = remember { speechHelper.isAvailable() }
                        if (!isSpeechAvailable) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Layanan pengenalan suara tidak aktif pada perangkat ini.",
                                fontSize = 10.sp,
                                color = SarangRed
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = {
                                currentStory?.let { story ->
                                    isRecording = false
                                    val simulatedSec = 12
                                    val wordsCount = storyKorean.split(Regex("\\s+")).filter { it.isNotBlank() }.size
                                    val calculatedWpm = ((wordsCount.toFloat() / simulatedSec) * 60).toInt().coerceIn(40, 160)
                                    val matchScore = 95
                                    resultMetric = SpeedReadingResult(
                                        wpm = calculatedWpm,
                                        accuracy = matchScore,
                                        elapsedSeconds = simulatedSec,
                                        feedback = "Luar biasa! Kecepatan membaca $calculatedWpm WPM dengan pelafalan 95% akurat.",
                                        recognizedText = storyKorean
                                    )
                                    onRecordProgress("reading", story.id, "WPM: $storyTitle", matchScore)
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("simulate_wpm_reading_btn")
                        ) {
                            Text("Simulasi Baca Cepat (Hitung WPM)", fontSize = 11.sp, color = HaneulBlue)
                        }
                    }
                }

                // WPM Result Card
                resultMetric?.let { metric ->
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.5.dp, SaessakGreen, RoundedCornerShape(18.dp))
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Speed, contentDescription = null, tint = SaessakGreen, modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Hasil Analisis Kelancaran",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF166534)
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = NunbitYellowContainer
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Bolt, contentDescription = null, tint = NunbitYellow, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text("+35 XP", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF92400E))
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color.White,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(10.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(text = "${metric.wpm}", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = HaneulBlue)
                                        Text(text = "WPM (Kata/Menit)", fontSize = 10.sp, color = TextSecondary)
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color.White,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(10.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(text = "${metric.accuracy}%", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = SaessakGreen)
                                        Text(text = "Akurasi Kata", fontSize = 10.sp, color = TextSecondary)
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color.White,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(10.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(text = "${metric.elapsedSeconds}s", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = InkBlack)
                                        Text(text = "Durasi Baca", fontSize = 10.sp, color = TextSecondary)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = metric.feedback,
                                fontSize = 12.sp,
                                color = Color(0xFF15803D),
                                fontWeight = FontWeight.Medium
                            )

                            if (metric.recognizedText.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color.White,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(text = "Ucapan Terdeteksi:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                                        Text(text = metric.recognizedText, fontSize = 13.sp, color = InkBlack)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
