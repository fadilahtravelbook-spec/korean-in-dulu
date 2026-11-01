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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.R
import com.example.data.model.RolePlayOption
import com.example.data.model.RolePlayStep
import com.example.ui.components.AppSubHeader
import com.example.ui.theme.HaneulBlue
import com.example.ui.theme.HaneulContainer
import com.example.ui.theme.InkBlack
import com.example.ui.theme.NunbitYellow
import com.example.ui.theme.SaessakGreen
import com.example.ui.theme.SarangRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.util.SpeechAssessment
import com.example.util.SpeechRecognitionHelper

/**
 * Halaman Khusus Role-Play Percakapan Interaktif dengan Hannie (롤플레잉 대화)
 * Pengalaman simulasi dialog dua arah dengan pengenalan suara (STT),
 * opsi respons kontekstual, feedback penilaian pelafalan, dan reward XP.
 */
@Composable
fun RolePlayScreen(
    rolePlaySteps: List<RolePlayStep>,
    onBack: () -> Unit,
    onSpeak: (String) -> Unit,
    onSpeakWithSpeed: ((String, Float) -> Unit)? = null,
    onRecordProgress: (moduleId: String, lessonId: String, title: String, score: Int) -> Unit
) {
    val context = LocalContext.current
    val speechHelper = remember { SpeechRecognitionHelper(context) }

    DisposableEffect(Unit) {
        onDispose {
            speechHelper.stopListening()
        }
    }

    if (rolePlaySteps.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8FAFC))
        ) {
            AppSubHeader(
                title = "Role-Play Percakapan (대화 연습)",
                subtitle = "Percakapan interaktif dua arah bersama maskot Hannie",
                onBack = onBack
            )
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Skenario percakapan sedang disiapkan.", color = TextSecondary)
            }
        }
        return
    }

    var currentRolePlayIndex by remember { mutableIntStateOf(0) }
    val currentRolePlayStep = rolePlaySteps.getOrElse(currentRolePlayIndex) { rolePlaySteps.first() }
    var selectedOption by remember(currentRolePlayIndex) { mutableStateOf<RolePlayOption?>(null) }

    var isListening by remember { mutableStateOf(false) }
    var recognizedText by remember { mutableStateOf("") }
    var assessmentResult by remember { mutableStateOf<SpeechAssessment?>(null) }
    var hasMicPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasMicPermission = isGranted
        if (isGranted) {
            isListening = true
            speechHelper.startListening(
                onResult = { text ->
                    recognizedText = text
                    isListening = false
                    selectedOption?.let { opt ->
                        assessmentResult = SpeechRecognitionHelper.evaluatePronunciation(opt.textKo, text)
                    }
                },
                onError = {
                    isListening = false
                }
            )
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .testTag("role_play_screen")
    ) {
        AppSubHeader(
            title = "Role-Play Percakapan (대화)",
            subtitle = "Skenario interaktif nyata: Kafe, Sepeda 따릉이 & Sapaan Sehari-hari",
            onBack = onBack
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Spacer(modifier = Modifier.height(2.dp))

            // Progress Bar Langkah Percakapan
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Langkah ${currentRolePlayIndex + 1} dari ${rolePlaySteps.size}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = HaneulBlue
                )
                Text(
                    text = "${((currentRolePlayIndex + 1).toFloat() / rolePlaySteps.size * 100).toInt()}% Selesai",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary
                )
            }

            LinearProgressIndicator(
                progress = { (currentRolePlayIndex + 1).toFloat() / rolePlaySteps.size },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = HaneulBlue,
                trackColor = Color(0xFFE2E8F0)
            )

            // Kartu Dialog Hannie
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
                    verticalAlignment = Alignment.Top
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_mascot_hannie),
                        contentDescription = "Hannie",
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(HaneulContainer)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Hannie (한이)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = HaneulBlue
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = currentRolePlayStep.hannieDialogueKo,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = InkBlack,
                            lineHeight = 22.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = currentRolePlayStep.hannieDialogueId,
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }

                    IconButton(
                        onClick = { onSpeak(currentRolePlayStep.hannieDialogueKo) },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(HaneulContainer)
                            .testTag("roleplay_speak_hannie_btn")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Dengarkan",
                            tint = HaneulBlue,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Text(
                text = "Pilih Respons Percakapan Anda:",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = InkBlack
            )

            // Opsi Respons Pengguna
            currentRolePlayStep.userOptions.forEach { opt ->
                val isSelected = selectedOption?.textKo == opt.textKo
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) HaneulContainer else Color.White
                    ),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(
                            if (isSelected) HaneulBlue else Color(0xFFE2E8F0)
                        )
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable {
                            selectedOption = opt
                            assessmentResult = null
                            recognizedText = ""
                            onSpeak(opt.textKo)
                            onRecordProgress(
                                "speaking",
                                "roleplay_${currentRolePlayIndex}",
                                "Role-Play Step ${currentRolePlayIndex + 1}",
                                95
                            )
                        }
                        .testTag("roleplay_option_${opt.textKo.take(4)}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = opt.textKo,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) HaneulBlue else InkBlack
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = opt.textId,
                                fontSize = 12.sp,
                                color = Color(0xFF475569)
                            )
                        }

                        if (isSelected) {
                            Surface(
                                shape = CircleShape,
                                color = HaneulBlue,
                                modifier = Modifier.size(26.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Bagian Latihan Pengucapan Suara (STT) Jika Opsi Telah Dipilih
            selectedOption?.let { opt ->
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFE2E8F0))
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Uji Pengucapan Suara Anda (STT):",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = InkBlack
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Surface(
                            shape = CircleShape,
                            color = if (isListening) SarangRed else HaneulBlue,
                            modifier = Modifier
                                .size(60.dp)
                                .scale(if (isListening) pulseScale else 1f)
                                .clip(CircleShape)
                                .clickable {
                                    if (isListening) {
                                        speechHelper.stopListening()
                                        isListening = false
                                    } else {
                                        if (hasMicPermission) {
                                            isListening = true
                                            speechHelper.startListening(
                                                onResult = { text ->
                                                    recognizedText = text
                                                    isListening = false
                                                    val eval = SpeechRecognitionHelper.evaluatePronunciation(opt.textKo, text)
                                                    assessmentResult = eval
                                                    onRecordProgress("speaking", "roleplay_${currentRolePlayIndex}", "Role-Play Step ${currentRolePlayIndex + 1}", eval.accuracy)
                                                },
                                                onError = { isListening = false }
                                            )
                                        } else {
                                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                        }
                                    }
                                }
                                .testTag("roleplay_mic_button")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    if (isListening) Icons.Default.Stop else Icons.Default.Mic,
                                    contentDescription = "Rekam",
                                    tint = Color.White,
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isListening) "Sedang mendengarkan... Ucapkan sekarang!" else "Sentuh mikrofon lalu ucapkan kalimat di atas",
                            fontSize = 11.sp,
                            color = if (isListening) SarangRed else TextSecondary,
                            fontWeight = if (isListening) FontWeight.Bold else FontWeight.Normal
                        )

                        val isSpeechAvailable = remember { speechHelper.isAvailable() }
                        if (!isSpeechAvailable) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Layanan pengenalan suara bawaan sistem tidak tersedia.",
                                fontSize = 10.sp,
                                color = SarangRed
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = {
                                isListening = false
                                recognizedText = opt.textKo
                                val eval = SpeechRecognitionHelper.evaluatePronunciation(opt.textKo, opt.textKo)
                                assessmentResult = eval
                                onRecordProgress("speaking", "roleplay_${currentRolePlayIndex}", "Role-Play Step ${currentRolePlayIndex + 1}", eval.accuracy)
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("simulate_roleplay_speech_btn")
                        ) {
                            Text("Simulasi Ucap Fasih (Evaluasi Cepat)", fontSize = 11.sp, color = HaneulBlue)
                        }

                        if (recognizedText.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFF1F5F9),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Terdeteksi: \"$recognizedText\"",
                                    fontSize = 12.sp,
                                    color = InkBlack,
                                    modifier = Modifier.padding(10.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        assessmentResult?.let { eval ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (eval.accuracy >= 70) Color(0xFFF0FDF4) else Color(0xFFFFFBEB),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (eval.accuracy >= 70) SaessakGreen else NunbitYellow
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "Skor Akurasi: ${eval.accuracy}%",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 14.sp,
                                        color = if (eval.accuracy >= 70) Color(0xFF15803D) else Color(0xFFB45309)
                                    )
                                    Text(
                                        text = eval.feedback,
                                        fontSize = 11.sp,
                                        color = Color(0xFF475569)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Tombol Navigasi Langkah Berikutnya
            if (currentRolePlayIndex < rolePlaySteps.size - 1) {
                Button(
                    onClick = {
                        currentRolePlayIndex++
                        selectedOption = null
                        assessmentResult = null
                        recognizedText = ""
                    },
                    enabled = selectedOption != null,
                    colors = ButtonDefaults.buttonColors(containerColor = HaneulBlue),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("roleplay_next_step_btn")
                ) {
                    Text("Lanjut ke Dialog Berikutnya →", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFF0FDF4),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF86EFAC))
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🎉 Skenario Percakapan Selesai!", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Color(0xFF15803D))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Luar biasa! Kamu telah menuntaskan seluruh alur percakapan role-play bersama Hannie.", fontSize = 12.sp, color = TextSecondary, textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                currentRolePlayIndex = 0
                                selectedOption = null
                                assessmentResult = null
                                recognizedText = ""
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SaessakGreen),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Ulangi Skenario Percakapan", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
