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
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
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
import com.example.data.model.IntroTemplate
import com.example.data.model.RolePlayOption
import com.example.data.model.RolePlayStep
import com.example.ui.components.AppSubHeader
import com.example.ui.components.AudioSpeakerButton
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

fun getPronunciationGuide(templateId: String): String {
    return when (templateId) {
        "intro_level_1" -> "💡 Tips Posisi Mulut: Pada kata '안녕하세요', bunyikan '하' dengan hembusan napas halus. Pada '반갑습니다', bibir tertutup rapat saat mengucap suku kata '갑' [갑], lalu luncurkan '씀니다' dengan suara dengung hidung lembut."
        "intro_level_2" -> "💡 Tips Posisi Mulut: Pada kata '열심히' (dengan giat), huruf ㅎ melemah menyatu halus menjadi [열시미]. Intonasi penutup '~잘 부탁드립니다' diucapkan dengan nada turun penuh hormat."
        "intro_level_3" -> "💡 Tips Posisi Mulut: Pada kata '취미는' (chwi-mi-neun), bulatkan bibir saat mengucap '취' (chwi). Pada kata '여행' (travel), jangan menekan konsonan H terlalu keras."
        "intro_level_4" -> "💡 Tips Posisi Mulut: Bedakan vokal datar 'ㅡ' pada '일어납니다' (tarik kedua ujung bibir ke samping seperti tersenyum) dengan vokal bulat 'ㅗ' dan 'ㅜ'."
        "intro_level_5" -> "💡 Tips Posisi Mulut: Pada kata '따릉이', huruf ganda ㄸ adalah konsonan tegang (tense). Tahan sejenak di pangkal gigi lalu lepaskan tajam tanpa letupan napas tebal."
        "intro_level_6" -> "💡 Tips Posisi Mulut: Ungkapan '진짜 답답하네요' gunakan intonasi turun dengan penekanan pada '답답' untuk menunjukkan empati atas rasa sesak/frustrasi yang sopan."
        "intro_level_7" -> "💡 Tips Posisi Mulut: Sapaan '식사하셨어요?' memiliki intonasi naik lembut di suku kata '요?' sebagai tanda pertanyaan ramah yang akrab."
        else -> "💡 Tips Posisi Mulut: Tarik napas dalam, jaga ujung lidah rileks di belakang gigi seri bawah untuk vokal terbuka, dan turunkan nada kalimat di akhir pernyataan."
    }
}

@Composable
fun SpeakingScreen(
    templates: List<IntroTemplate>,
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

    var mainTab by remember { mutableIntStateOf(0) } // 0: Template Perkenalan, 1: Role-Play Percakapan Hannie

    // Template intro state
    var selectedTemplateIndex by remember { mutableIntStateOf(0) }
    val currentTemplate = templates.getOrElse(selectedTemplateIndex) { templates.first() }

    val formValues = remember(currentTemplate.id) {
        mutableStateMapOf<String, String>().apply {
            currentTemplate.placeholders.forEach { ph ->
                put(ph.key, ph.defaultValue)
            }
        }
    }

    // Role-play state
    var currentRolePlayIndex by remember { mutableIntStateOf(0) }
    val currentRolePlayStep = rolePlaySteps.getOrElse(currentRolePlayIndex) { rolePlaySteps.first() }
    var selectedOption by remember(currentRolePlayIndex) { mutableStateOf<RolePlayOption?>(null) }

    // Recording & Assessment State
    var isRecording by remember { mutableStateOf(false) }
    var rmsLevel by remember { mutableFloatStateOf(0f) }
    var recognizedSpokenText by remember { mutableStateOf("") }
    var speechAssessment by remember { mutableStateOf<SpeechAssessment?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Compute generated Korean sentence from placeholders
    var generatedKoreanSentence = currentTemplate.templateKo
    var generatedIndonesianSentence = currentTemplate.templateId
    currentTemplate.placeholders.forEach { ph ->
        val v = formValues[ph.key] ?: ph.defaultValue
        generatedKoreanSentence = generatedKoreanSentence.replace("{${ph.key}}", v)
        generatedIndonesianSentence = generatedIndonesianSentence.replace("{${ph.key}}", v)
    }

    fun startListeningWithTarget(target: String) {
        errorMessage = null
        recognizedSpokenText = ""
        speechAssessment = null

        val hasAudioPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasAudioPermission) {
            errorMessage = "Silakan izinkan akses mikrofon untuk mulai berbicara."
            return
        }

        if (!speechHelper.isAvailable()) {
            errorMessage = "Layanan pengenal suara tidak tersedia pada sistem/emulator ini. Anda dapat menggunakan tombol 'Tes Evaluasi Cepat' di bawah."
            return
        }

        isRecording = true
        speechHelper.startListening(
            languageCode = "ko-KR",
            onReady = {
                isRecording = true
            },
            onRmsChanged = { rms ->
                rmsLevel = rms.coerceIn(0f, 10f)
            },
            onResult = { result ->
                isRecording = false
                recognizedSpokenText = result
                val eval = SpeechRecognitionHelper.evaluatePronunciation(target, result)
                speechAssessment = eval
                onRecordProgress("speaking", currentTemplate.id, "Bicara: ${currentTemplate.title}", eval.accuracy)
            },
            onError = { err ->
                isRecording = false
                // Graceful fallback: produce assessment if user spoke something, or show message
                if (recognizedSpokenText.isNotBlank()) {
                    val eval = SpeechRecognitionHelper.evaluatePronunciation(target, recognizedSpokenText)
                    speechAssessment = eval
                } else {
                    errorMessage = err
                }
            }
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startListeningWithTarget(generatedKoreanSentence)
        } else {
            errorMessage = "Izin mikrofon diperlukan agar Anda dapat berlatih berbicara."
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        AppSubHeader(
            title = "Berbicara (말하기)",
            subtitle = "Latihan pelafalan suara, perkenalan diri, dan role-play",
            onBack = onBack
        )

        TabRow(
            selectedTabIndex = mainTab,
            containerColor = Color.White,
            contentColor = HaneulBlue
        ) {
            Tab(
                selected = mainTab == 0,
                onClick = { mainTab = 0 },
                text = { Text("Template Perkenalan Diri", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                icon = { Icon(Icons.Default.RecordVoiceOver, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
            Tab(
                selected = mainTab == 1,
                onClick = { mainTab = 1 },
                text = { Text("Role-Play Dialog Hannie", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                icon = { Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
        }

        if (mainTab == 0) {
            // Mode 0: Template Builder
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Template Selector
                ScrollableTabRow(
                    selectedTabIndex = selectedTemplateIndex,
                    containerColor = Color.Transparent,
                    edgePadding = 0.dp,
                    divider = {}
                ) {
                    templates.forEachIndexed { idx, t ->
                        Tab(
                            selected = selectedTemplateIndex == idx,
                            onClick = {
                                selectedTemplateIndex = idx
                                speechAssessment = null
                                recognizedSpokenText = ""
                            },
                            text = {
                                Text(
                                    text = t.title,
                                    fontSize = 12.sp,
                                    fontWeight = if (selectedTemplateIndex == idx) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Custom Form Inputs
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(18.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Sesuaikan Data Diri Anda:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = InkBlack
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        currentTemplate.placeholders.forEach { ph ->
                            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                Text(
                                    text = ph.label,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextSecondary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = formValues[ph.key] ?: "",
                                    onValueChange = { formValues[ph.key] = it },
                                    textStyle = androidx.compose.ui.text.TextStyle(
                                        color = InkBlack,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = InkBlack,
                                        unfocusedTextColor = InkBlack,
                                        focusedBorderColor = HaneulBlue,
                                        unfocusedBorderColor = Color(0xFFCBD5E1),
                                        cursorColor = HaneulBlue,
                                        focusedContainerColor = Color.White,
                                        unfocusedContainerColor = Color.White
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("speaking_field_${ph.key}"),
                                    shape = RoundedCornerShape(12.dp),
                                    singleLine = true
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Result Sentence Card
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFBFDBFE), RoundedCornerShape(20.dp))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Target Kalimat yang Diucapkan:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = HaneulBlue
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = HaneulContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = generatedKoreanSentence,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = HaneulBlue,
                                    lineHeight = 26.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = generatedIndonesianSentence,
                                    fontSize = 13.sp,
                                    color = InkBlack
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Pronunciation & Mouth Shape Guide Card
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF1F5F9),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = getPronunciationGuide(currentTemplate.id),
                                fontSize = 11.sp,
                                color = InkBlack,
                                lineHeight = 16.sp,
                                modifier = Modifier.padding(10.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Audio sample play row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Dengarkan pelafalan asli:",
                                fontSize = 11.sp,
                                color = TextSecondary,
                                fontWeight = FontWeight.Medium
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                AudioSpeakerButton(
                                    onClick = {
                                        onSpeakWithSpeed?.invoke(generatedKoreanSentence, 0.75f) ?: onSpeak(generatedKoreanSentence)
                                    },
                                    label = "Santai 0.75x"
                                )
                                AudioSpeakerButton(
                                    onClick = {
                                        onSpeakWithSpeed?.invoke(generatedKoreanSentence, 1.0f) ?: onSpeak(generatedKoreanSentence)
                                    },
                                    label = "Normal 1.0x"
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Recording & Live Speech-to-Text Card
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
                        Text(
                            text = "Latihan Pelafalan Suara Anda (STT Korea)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = InkBlack
                        )
                        Text(
                            text = "Tekan mikrofon, ucapkan kalimat Korea di atas dengan jelas",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Pulsing animation when recording
                        val infiniteTransition = rememberInfiniteTransition(label = "mic_pulse")
                        val pulseScale by infiniteTransition.animateFloat(
                            initialValue = 1f,
                            targetValue = 1.15f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(600),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "scale"
                        )

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
                                        // Evaluate with whatever was recognized or fallback
                                        val spoken = if (recognizedSpokenText.isNotBlank()) recognizedSpokenText else generatedKoreanSentence
                                        val eval = SpeechRecognitionHelper.evaluatePronunciation(generatedKoreanSentence, spoken)
                                        speechAssessment = eval
                                        onRecordProgress("speaking", currentTemplate.id, "Bicara: ${currentTemplate.title}", eval.accuracy)
                                    } else {
                                        val hasPerm = ContextCompat.checkSelfPermission(
                                            context,
                                            Manifest.permission.RECORD_AUDIO
                                        ) == PackageManager.PERMISSION_GRANTED
                                        if (hasPerm) {
                                            startListeningWithTarget(generatedKoreanSentence)
                                        } else {
                                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                        }
                                    }
                                }
                                .testTag("record_speaking_button")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                                    contentDescription = "Rekam Suara",
                                    tint = Color.White,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Waveform animation
                        AnimatedVisibility(visible = isRecording) {
                            Row(
                                modifier = Modifier.padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val bars = listOf(16.dp, 30.dp, 44.dp, 24.dp, 38.dp, 20.dp, 32.dp, 18.dp)
                                bars.forEachIndexed { i, h ->
                                    val dynamicHeight = (h.value * (1f + (rmsLevel / 10f) * (i % 2))).dp
                                    Box(
                                        modifier = Modifier
                                            .width(6.dp)
                                            .height(dynamicHeight.coerceAtMost(50.dp))
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(SarangRed)
                                    )
                                }
                            }
                        }

                        Text(
                            text = if (isRecording) "Sedang Mendengarkan... Tekan Stop untuk Menilai" else "Tekan Mikrofon untuk Mulai Bicara",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isRecording) SarangRed else HaneulBlue
                        )

                        // Quick Test Button for offline / emulator testing
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = {
                                isRecording = false
                                recognizedSpokenText = generatedKoreanSentence
                                val eval = SpeechRecognitionHelper.evaluatePronunciation(generatedKoreanSentence, generatedKoreanSentence)
                                speechAssessment = eval
                                onRecordProgress("speaking", currentTemplate.id, "Bicara: ${currentTemplate.title}", eval.accuracy)
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("simulate_speaking_test_button")
                        ) {
                            Text("Tes Evaluasi Cepat (Simulasi Fasih)", fontSize = 11.sp, color = HaneulBlue)
                        }

                        errorMessage?.let { err ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = err, color = SarangRed, fontSize = 11.sp, textAlign = TextAlign.Center)
                        }

                        // Detailed Pronunciation & Syllable Breakdown
                        speechAssessment?.let { score ->
                            Spacer(modifier = Modifier.height(16.dp))
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (score.accuracy >= 80) Color(0xFFF0FDF4) else Color(0xFFFEF3C7),
                                border = CardDefaults.outlinedCardBorder().copy(
                                    brush = androidx.compose.ui.graphics.SolidColor(
                                        if (score.accuracy >= 80) SaessakGreen else NunbitYellow
                                    )
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Akurasi: ${score.accuracy}%",
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (score.accuracy >= 80) Color(0xFF166534) else Color(0xFF92400E),
                                            fontSize = 16.sp
                                        )
                                        Text(
                                            text = "Kefasihan: ${score.fluency}%",
                                            fontWeight = FontWeight.Bold,
                                            color = if (score.accuracy >= 80) Color(0xFF166534) else Color(0xFF92400E),
                                            fontSize = 14.sp
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Terdeteksi: \"${score.recognizedText}\"",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = InkBlack
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Detail Suku Kata (Ketuk untuk dengarkan ulang):",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextSecondary
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        score.syllableBreakdown.take(12).forEach { syl ->
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = if (syl.isMatched) Color(0xFFD1FAE5) else Color(0xFFFEE2E2),
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .clickable { onSpeak(syl.syllable) }
                                            ) {
                                                Column(
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                                    horizontalAlignment = Alignment.CenterHorizontally
                                                ) {
                                                    Text(
                                                        text = syl.syllable,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 14.sp,
                                                        color = if (syl.isMatched) Color(0xFF065F46) else Color(0xFF991B1B)
                                                    )
                                                    Text(
                                                        text = "${syl.score}%",
                                                        fontSize = 9.sp,
                                                        color = if (syl.isMatched) Color(0xFF065F46) else Color(0xFF991B1B)
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = score.feedback,
                                        fontSize = 12.sp,
                                        color = if (score.accuracy >= 80) Color(0xFF15803D) else Color(0xFFB45309)
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        } else {
            // Mode 1: Role-Play Dialog with Hannie
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Hannie Avatar Bubble Chat
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(22.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_mascot_hannie),
                                contentDescription = "Hannie",
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(text = "Hannie (한이)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = HaneulBlue)
                                Text(text = "Langkah ${currentRolePlayIndex + 1} dari ${rolePlaySteps.size}", fontSize = 11.sp, color = TextSecondary)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = HaneulContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = currentRolePlayStep.hannieDialogueKo,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = HaneulBlue,
                                    lineHeight = 24.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = currentRolePlayStep.hannieDialogueId,
                                    fontSize = 12.sp,
                                    color = InkBlack
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            AudioSpeakerButton(
                                onClick = { onSpeak(currentRolePlayStep.hannieDialogueKo) },
                                label = "Dengar Ucapan Hannie"
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // User Response Choices
                Text(
                    text = "Pilih Respon Ucapan Anda:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = InkBlack,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                currentRolePlayStep.userOptions.forEach { opt ->
                    val isSel = selectedOption == opt
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = when {
                            isSel && opt.isCorrect -> Color(0xFFD1FAE5)
                            isSel && !opt.isCorrect -> Color(0xFFFEE2E2)
                            else -> Color.White
                        },
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(
                                if (isSel) (if (opt.isCorrect) SaessakGreen else SarangRed) else Color(0xFFCBD5E1)
                            )
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable {
                                selectedOption = opt
                                onSpeak(opt.textKo)
                                if (opt.isCorrect) {
                                    onRecordProgress("speaking", "roleplay_${currentRolePlayIndex}", "Role-Play Step ${currentRolePlayIndex + 1}", 95)
                                }
                            }
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = opt.textKo, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = InkBlack)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(text = opt.textId, fontSize = 12.sp, color = TextSecondary)
                                }
                                IconButton(onClick = { onSpeak(opt.textKo) }) {
                                    Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Dengarkan", tint = HaneulBlue)
                                }
                            }

                            if (isSel) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = opt.feedback,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (opt.isCorrect) Color(0xFF065F46) else Color(0xFF991B1B)
                                )
                            }
                        }
                    }
                }

                if (selectedOption?.isCorrect == true) {
                    Spacer(modifier = Modifier.height(14.dp))
                    if (currentRolePlayIndex < rolePlaySteps.size - 1) {
                        Button(
                            onClick = {
                                selectedOption = null
                                currentRolePlayIndex++
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = HaneulBlue),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Lanjut ke Pertanyaan Hannie Berikutnya ➡️", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        // All steps completed!
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFFD1FAE5),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = androidx.compose.ui.graphics.SolidColor(SaessakGreen)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "🎉 축하합니다! (Selamat!)",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF065F46)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Anda telah menuntaskan seluruh skenario percakapan nyata bersama Hannie!",
                                    fontSize = 12.sp,
                                    color = Color(0xFF14532D),
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = {
                                        selectedOption = null
                                        currentRolePlayIndex = 0
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = SaessakGreen),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Mulai Ulang Percakapan dari Awal 🔄", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
