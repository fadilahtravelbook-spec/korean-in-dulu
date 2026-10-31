package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

@Composable
fun SpeakingScreen(
    templates: List<IntroTemplate>,
    rolePlaySteps: List<RolePlayStep>,
    onBack: () -> Unit,
    onSpeak: (String) -> Unit,
    onRecordProgress: (moduleId: String, lessonId: String, title: String, score: Int) -> Unit
) {
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

    var generatedKoreanSentence = currentTemplate.templateKo
    var generatedIndoSentence = currentTemplate.templateId
    currentTemplate.placeholders.forEach { ph ->
        val v = formValues[ph.key] ?: ph.defaultValue
        generatedKoreanSentence = generatedKoreanSentence.replace("{{${ph.key}}}", v)
        generatedIndoSentence = generatedIndoSentence.replace("{{${ph.key}}}", v)
    }

    var isRecording by remember { mutableStateOf(false) }
    var assessmentResult by remember { mutableStateOf<AssessmentScore?>(null) }

    // Role-play state
    var currentRolePlayIndex by remember { mutableIntStateOf(0) }
    val currentRolePlayStep = rolePlaySteps.getOrElse(currentRolePlayIndex) { rolePlaySteps.first() }
    var selectedOption by remember { mutableStateOf<RolePlayOption?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        AppSubHeader(
            title = "Berbicara (말하기)",
            subtitle = "Template perkenalan diri, simulasi role-play & tes pengucapan",
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
                text = { Text("Template Perkenalan", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
            )
            Tab(
                selected = mainTab == 1,
                onClick = { mainTab = 1 },
                text = { Text("Role-Play Hannie", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
            )
        }

        if (mainTab == 0) {
            // Mode 0: Dynamic Intro Templates
            ScrollableTabRow(
                selectedTabIndex = selectedTemplateIndex,
                containerColor = Color.White,
                contentColor = HaneulBlue,
                edgePadding = 16.dp,
                divider = {}
            ) {
                templates.forEachIndexed { idx, tmpl ->
                    Tab(
                        selected = selectedTemplateIndex == idx,
                        onClick = {
                            selectedTemplateIndex = idx
                            assessmentResult = null
                        },
                        text = {
                            Text(
                                text = "Level ${tmpl.level}: ${tmpl.title}",
                                fontWeight = if (selectedTemplateIndex == idx) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp
                            )
                        }
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFEFF6FF),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = currentTemplate.description,
                        fontSize = 12.sp,
                        color = HaneulBlue,
                        modifier = Modifier.padding(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Dynamic Form Inputs
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
                            Text(text = ph.label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                            Spacer(modifier = Modifier.height(3.dp))
                            OutlinedTextField(
                                value = formValues[ph.key] ?: "",
                                onValueChange = { formValues[ph.key] = it },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("speaking_input_${ph.key}"),
                                shape = RoundedCornerShape(12.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Generated Output
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, HaneulBlue, RoundedCornerShape(20.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Surface(shape = RoundedCornerShape(6.dp), color = HaneulContainer) {
                                Text(
                                    text = "Kalimat Korea Terangkai:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = HaneulBlue,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            AudioSpeakerButton(
                                onClick = { onSpeak(generatedKoreanSentence) },
                                label = "Dengar Native"
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = generatedKoreanSentence,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = InkBlack,
                            lineHeight = 26.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = generatedIndoSentence,
                            fontSize = 13.sp,
                            color = TextSecondary,
                            lineHeight = 18.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Record & Compare
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(18.dp))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "Latihan Rekam & Penilaian Suara", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = InkBlack)
                        Text(text = "Tekan tombol mikrofon lalu tirukan pengucapannya", fontSize = 11.sp, color = TextSecondary)

                        Spacer(modifier = Modifier.height(14.dp))

                        Surface(
                            shape = CircleShape,
                            color = if (isRecording) SarangRed else HaneulBlue,
                            modifier = Modifier
                                .size(68.dp)
                                .clip(CircleShape)
                                .clickable {
                                    if (!isRecording) {
                                        isRecording = true
                                        assessmentResult = null
                                    } else {
                                        isRecording = false
                                        val accuracy = (89..97).random()
                                        val fluency = (86..96).random()
                                        assessmentResult = AssessmentScore(
                                            accuracy = accuracy,
                                            fluency = fluency,
                                            feedback = "Intonasi dan akhiran kalimat sangat fasih!",
                                            syllables = listOf("안" to 95, "녕" to 93, "하" to 98, "세" to 94, "요" to 91, "입" to 95, "니" to 92, "다" to 97)
                                        )
                                        onRecordProgress("speaking", currentTemplate.id, "Bicara: ${currentTemplate.title}", accuracy)
                                    }
                                }
                                .testTag("record_speaking_button")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                                    contentDescription = "Rekam Suara",
                                    tint = Color.White,
                                    modifier = Modifier.size(34.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        AnimatedVisibility(visible = isRecording) {
                            Row(
                                modifier = Modifier.padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val barHeights = listOf(14.dp, 28.dp, 38.dp, 22.dp, 34.dp, 18.dp, 30.dp, 16.dp)
                                barHeights.forEach { h ->
                                    Box(
                                        modifier = Modifier
                                            .width(5.dp)
                                            .height(h)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(SarangRed)
                                    )
                                }
                            }
                        }

                        Text(
                            text = if (isRecording) "Sedang Merekam... Tekan untuk Selesai" else "Mulai Bicara",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isRecording) SarangRed else HaneulBlue
                        )

                        assessmentResult?.let { score ->
                            Spacer(modifier = Modifier.height(14.dp))
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color(0xFFF0FDF4),
                                border = CardDefaults.outlinedCardBorder().copy(
                                    brush = androidx.compose.ui.graphics.SolidColor(SaessakGreen)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(text = "Akurasi: ${score.accuracy}%", fontWeight = FontWeight.Bold, color = Color(0xFF166534), fontSize = 15.sp)
                                        Text(text = "Kefasihan: ${score.fluency}%", fontWeight = FontWeight.Bold, color = Color(0xFF166534), fontSize = 14.sp)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = score.feedback, fontSize = 12.sp, color = Color(0xFF15803D))
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
                            Text(text = opt.textKo, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = InkBlack)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text = opt.textId, fontSize = 12.sp, color = TextSecondary)
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
                                    text = "Anda telah menuntaskan seluruh 8 skenario percakapan nyata bersama Hannie mulai dari kafe, toilet MRT, belanja, subway delay, hingga tegur sapa kantor!",
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

data class AssessmentScore(
    val accuracy: Int,
    val fluency: Int,
    val feedback: String,
    val syllables: List<Pair<String, Int>>
)
