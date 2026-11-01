package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.focusable
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.key.utf16CodePoint
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DocumentSentence
import com.example.data.model.KoreanDocumentDataProvider
import com.example.data.model.KoreanDocumentItem
import com.example.data.model.KoreanDocumentType
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
fun KoreanDocumentPracticeView(
    ttsSpeed: Float = 0.9f,
    onSpeak: (String, Float) -> Unit,
    onAddXp: (Int) -> Unit,
    onRecordProgress: (moduleId: String, lessonId: String, title: String, score: Int) -> Unit
) {
    val documents = KoreanDocumentDataProvider.sampleDocuments
    var selectedCategoryFilter by remember { mutableStateOf<KoreanDocumentType?>(null) }

    val filteredDocuments = remember(selectedCategoryFilter) {
        if (selectedCategoryFilter == null) documents
        else documents.filter { it.type == selectedCategoryFilter }
    }

    var selectedDocId by remember { mutableStateOf(documents.first().id) }
    val currentDoc = documents.find { it.id == selectedDocId } ?: documents.first()

    // Sub-tab: 0: 📖 Baca & Bedah Dokumen Asli, 1: ⌨️ Latihan Ketik Dokumen, 2: ✍️ Tulis Sendiri
    var currentSubTab by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        // Filter Kategori Dokumen
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            item {
                FilterChip(
                    selected = selectedCategoryFilter == null,
                    onClick = { selectedCategoryFilter = null },
                    label = { Text("🌟 Semua Dokumen (${documents.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = HaneulBlue,
                        selectedLabelColor = Color.White
                    )
                )
            }
            item {
                FilterChip(
                    selected = selectedCategoryFilter == KoreanDocumentType.LETTER,
                    onClick = { selectedCategoryFilter = KoreanDocumentType.LETTER },
                    label = { Text("💌 Surat (편지)", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFE11D48),
                        selectedLabelColor = Color.White
                    )
                )
            }
            item {
                FilterChip(
                    selected = selectedCategoryFilter == KoreanDocumentType.CHAT,
                    onClick = { selectedCategoryFilter = KoreanDocumentType.CHAT },
                    label = { Text("💬 Chat (카카오톡)", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFD97706),
                        selectedLabelColor = Color.White
                    )
                )
            }
            item {
                FilterChip(
                    selected = selectedCategoryFilter == KoreanDocumentType.STORY,
                    onClick = { selectedCategoryFilter = KoreanDocumentType.STORY },
                    label = { Text("📖 Cerita & Diary", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF16A34A),
                        selectedLabelColor = Color.White
                    )
                )
            }
            item {
                FilterChip(
                    selected = selectedCategoryFilter == KoreanDocumentType.REAL_DOC,
                    onClick = { selectedCategoryFilter = KoreanDocumentType.REAL_DOC },
                    label = { Text("📑 Dokumen Nyata", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF7C3AED),
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        // Horizontal Carousel Pemilihan Dokumen
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(end = 12.dp)
        ) {
            items(filteredDocuments) { doc ->
                val isSelected = doc.id == currentDoc.id
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) Color.White else Color(0xFFF1F5F9),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.horizontalGradient(
                            if (isSelected) listOf(HaneulBlue, Color(0xFF2563EB))
                            else listOf(Color(0xFFE2E8F0), Color(0xFFE2E8F0))
                        )
                    ),
                    shadowElevation = if (isSelected) 3.dp else 0.dp,
                    modifier = Modifier
                        .width(220.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { selectedDocId = doc.id }
                        .testTag("doc_card_${doc.id}")
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(doc.iconEmoji, fontSize = 20.sp)
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSelected) HaneulContainer else Color(0xFFE2E8F0)
                            ) {
                                Text(
                                    text = doc.categoryBadge,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) HaneulBlue else TextSecondary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = doc.titleKo,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = InkBlack,
                            maxLines = 1
                        )
                        Text(
                            text = doc.titleId,
                            fontSize = 11.sp,
                            color = TextSecondary,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Level: ${doc.formalityLevel}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = HaneulBlue
                        )
                    }
                }
            }
        }

        // Sub-Tab Switcher
        ScrollableTabRow(
            selectedTabIndex = currentSubTab,
            containerColor = Color.White,
            contentColor = HaneulBlue,
            edgePadding = 12.dp,
            divider = {}
        ) {
            val subTabs = listOf(
                "📖 Dokumen Asli & Audio",
                "⌨️ Latihan Ketik Dokumen",
                "✍️ Tulis Sendiri & Salin"
            )
            subTabs.forEachIndexed { index, title ->
                Tab(
                    selected = currentSubTab == index,
                    onClick = { currentSubTab = index },
                    text = {
                        Text(
                            text = title,
                            fontSize = 12.sp,
                            fontWeight = if (currentSubTab == index) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                )
            }
        }

        // Konten Berdasarkan Sub-Tab
        when (currentSubTab) {
            0 -> DocumentReaderAndStudyView(
                doc = currentDoc,
                ttsSpeed = ttsSpeed,
                onSpeak = onSpeak,
                onGoToTyping = { currentSubTab = 1 }
            )
            1 -> DocumentTypingPracticeView(
                doc = currentDoc,
                ttsSpeed = ttsSpeed,
                onSpeak = onSpeak,
                onAddXp = onAddXp,
                onRecordProgress = onRecordProgress
            )
            2 -> DocumentSelfDraftingView(
                ttsSpeed = ttsSpeed,
                onSpeak = onSpeak,
                onAddXp = onAddXp
            )
        }
    }
}

/**
 * Sub-Tab 1: 📖 Baca & Bedah Dokumen Asli Korea (Surat, Chat, Cerita, Dokumen Nyata)
 */
@Composable
fun DocumentReaderAndStudyView(
    doc: KoreanDocumentItem,
    ttsSpeed: Float,
    onSpeak: (String, Float) -> Unit,
    onGoToTyping: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var showTranslation by remember { mutableStateOf(true) }
    var showGrammarNotes by remember { mutableStateOf(true) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Toolbar Aksi Dokumen
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${doc.iconEmoji} ${doc.titleKo}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = InkBlack
                        )
                        Text(
                            text = "${doc.titleId} • ${doc.recipient}",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        // Putar Audio Lengkap
                        IconButton(
                            onClick = {
                                val fullText = doc.sentences.joinToString(" ") { it.korean }
                                onSpeak(fullText, ttsSpeed)
                            },
                            modifier = Modifier
                                .background(HaneulContainer, CircleShape)
                                .size(36.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Putar Audio", tint = HaneulBlue, modifier = Modifier.size(18.dp))
                        }

                        // Salin Seluruh Teks Korea
                        IconButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(doc.fullKoreanText))
                                Toast.makeText(context, "Teks Korea disalin ke clipboard!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .background(NunbitYellowContainer, CircleShape)
                                .size(36.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Salin Teks", tint = Color(0xFF92400E), modifier = Modifier.size(18.dp))
                        }

                        // Toggle Terjemahan
                        IconButton(
                            onClick = { showTranslation = !showTranslation },
                            modifier = Modifier
                                .background(Color(0xFFE2E8F0), CircleShape)
                                .size(36.dp)
                        ) {
                            Icon(
                                if (showTranslation) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle Terjemahan",
                                tint = InkBlack,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        // Tampilan Dokumen Sesuai Format (Surat, Chat, Cerita, Dokumen Nyata)
        item {
            when (doc.type) {
                KoreanDocumentType.LETTER -> LetterPaperLayout(doc, showTranslation, ttsSpeed, onSpeak)
                KoreanDocumentType.CHAT -> KakaoTalkChatLayout(doc, showTranslation, ttsSpeed, onSpeak)
                KoreanDocumentType.STORY -> StorybookLayout(doc, showTranslation, ttsSpeed, onSpeak)
                KoreanDocumentType.REAL_DOC -> RealDocumentLayout(doc, showTranslation, ttsSpeed, onSpeak)
            }
        }

        // Tombol Ajakan Latihan Ketik Dokumen
        item {
            Button(
                onClick = onGoToTyping,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("btn_practice_type_doc"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = HaneulBlue)
            ) {
                Icon(Icons.Default.Keyboard, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Latih Ketik Kalimat Dokumen Ini ⌨️", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }

        // Catatan Kosakata & Tata Bahasa Dokumen
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(14.dp),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showGrammarNotes = !showGrammarNotes },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lightbulb, contentDescription = null, tint = NunbitYellow, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Kosakata & Tata Bahasa Kunci", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = InkBlack)
                        }
                        Text(
                            text = if (showGrammarNotes) "Tutup ▲" else "Buka ▼",
                            fontSize = 11.sp,
                            color = HaneulBlue,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (showGrammarNotes) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "📌 Kosakata Baru:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = HaneulBlue
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        doc.vocabNotes.forEach { (word, meaning) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                                    .clickable { onSpeak(word, ttsSpeed) },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("• ", color = HaneulBlue, fontWeight = FontWeight.Bold)
                                Text(word, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = InkBlack)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("($meaning)", fontSize = 12.sp, color = TextSecondary)
                                Spacer(modifier = Modifier.weight(1f))
                                Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, tint = HaneulBlue, modifier = Modifier.size(14.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "💡 Panduan Tata Bahasa & Budaya:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD97706)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = NunbitYellowContainer.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = doc.grammarTips,
                                fontSize = 12.sp,
                                color = Color(0xFF78350F),
                                modifier = Modifier.padding(10.dp),
                                lineHeight = 17.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 1. Tampilan Bergaya Kertas Surat Korea (Letter Paper)
 */
@Composable
fun LetterPaperLayout(
    doc: KoreanDocumentItem,
    showTranslation: Boolean,
    ttsSpeed: Float,
    onSpeak: (String, Float) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFDF5)),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(listOf(Color(0xFFFDE68A), Color(0xFFFCD34D)))
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Surat
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFEE2E2)
                ) {
                    Text(
                        text = doc.recipient,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFB91C1C),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = NunbitYellowContainer
                ) {
                    Text(
                        text = doc.formalityLevel,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF92400E),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Kalimat-kalimat isi surat berbaris rapi dengan audio per baris
            doc.sentences.forEachIndexed { index, sentence ->
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { onSpeak(sentence.korean, ttsSpeed) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = sentence.korean,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = InkBlack,
                                lineHeight = 20.sp
                            )
                            if (showTranslation) {
                                Text(
                                    text = sentence.indonesian,
                                    fontSize = 11.sp,
                                    color = TextSecondary,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Dengar",
                            tint = HaneulBlue,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * 2. Tampilan Obrolan Chat KakaoTalk (Messenger)
 */
@Composable
fun KakaoTalkChatLayout(
    doc: KoreanDocumentItem,
    showTranslation: Boolean,
    ttsSpeed: Float,
    onSpeak: (String, Float) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFBACEE0)), // Warna khas background KakaoTalk
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header Chat Room
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.9f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("💬", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(doc.titleKo, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = InkBlack)
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFFEE500) // Kakao yellow
                ) {
                    Text("카카오톡 KakaoTalk", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color(0xFF3C1E1E), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Bubble Chat Percakapan
            doc.sentences.forEach { sentence ->
                val isMe = sentence.speaker == "Aku" || sentence.speaker == "나" || sentence.speaker == "저"

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                ) {
                    if (!isMe) {
                        // Avatar lawan bicara
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = sentence.speaker?.take(1) ?: "친구",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = HaneulBlue
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    Column(
                        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start,
                        modifier = Modifier.widthIn(max = 280.dp)
                    ) {
                        if (!isMe && sentence.speaker != null) {
                            Text(
                                text = sentence.speaker,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF334155),
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(
                                topStart = 12.dp,
                                topEnd = 12.dp,
                                bottomStart = if (isMe) 12.dp else 2.dp,
                                bottomEnd = if (isMe) 2.dp else 12.dp
                            ),
                            color = if (isMe) Color(0xFFFEE500) else Color.White,
                            shadowElevation = 1.dp,
                            modifier = Modifier.clickable { onSpeak(sentence.korean, ttsSpeed) }
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = sentence.korean,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isMe) Color(0xFF3C1E1E) else InkBlack
                                    )
                                    Icon(
                                        Icons.AutoMirrored.Filled.VolumeUp,
                                        contentDescription = null,
                                        tint = if (isMe) Color(0xFF78350F) else HaneulBlue,
                                        modifier = Modifier.size(13.dp)
                                    )
                                }

                                if (showTranslation) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = sentence.indonesian,
                                        fontSize = 10.sp,
                                        color = if (isMe) Color(0xFF573E3E) else TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 3. Tampilan Buku Cerita / Diary Korea (Storybook Layout)
 */
@Composable
fun StorybookLayout(
    doc: KoreanDocumentItem,
    showTranslation: Boolean,
    ttsSpeed: Float,
    onSpeak: (String, Float) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)), // Hijau lembut buku cerita
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(listOf(Color(0xFF86EFAC), Color(0xFF4ADE80)))
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Judul Cerita
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(doc.iconEmoji, fontSize = 24.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(doc.titleKo, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF14532D))
                    Text(doc.titleId, fontSize = 12.sp, color = Color(0xFF166534))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Paragraf Narasi
            doc.sentences.forEachIndexed { idx, sentence ->
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { onSpeak(sentence.korean, ttsSpeed) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFDCFCE7),
                            modifier = Modifier.size(24.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("${idx + 1}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF15803D))
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(sentence.korean, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = InkBlack)
                            if (showTranslation) {
                                Text(sentence.indonesian, fontSize = 11.sp, color = TextSecondary)
                            }
                        }
                        Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, tint = Color(0xFF15803D), modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

/**
 * 4. Tampilan Dokumen Kehidupan Nyata Korea (KTX Ticket / Resep Masakan)
 */
@Composable
fun RealDocumentLayout(
    doc: KoreanDocumentItem,
    showTranslation: Boolean,
    ttsSpeed: Float,
    onSpeak: (String, Float) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAF5FF)), // Ungu elegan
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(listOf(Color(0xFFD8B4FE), Color(0xFFC084FC)))
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Dokumen
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(doc.iconEmoji, fontSize = 22.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(doc.titleKo, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF581C87))
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFEDE9FE)
                ) {
                    Text("실제 한국 서식", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6B21A8), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Baris data dokumen
            doc.sentences.forEach { sentence ->
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                        .clickable { onSpeak(sentence.korean, ttsSpeed) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(sentence.korean, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = InkBlack)
                            if (showTranslation) {
                                Text(sentence.indonesian, fontSize = 11.sp, color = TextSecondary)
                            }
                        }
                        Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, tint = Color(0xFF7C3AED), modifier = Modifier.size(15.dp))
                    }
                }
            }
        }
    }
}

/**
 * Sub-Tab 2: ⌨️ Latihan Mengetik Kalimat Dokumen dengan Keyboard 2-Beol-Sik
 */
@Composable
fun DocumentTypingPracticeView(
    doc: KoreanDocumentItem,
    ttsSpeed: Float,
    onSpeak: (String, Float) -> Unit,
    onAddXp: (Int) -> Unit,
    onRecordProgress: (moduleId: String, lessonId: String, title: String, score: Int) -> Unit
) {
    var selectedSentenceIndex by remember(doc.id) { mutableIntStateOf(0) }
    val currentSentence = doc.sentences.getOrNull(selectedSentenceIndex) ?: doc.sentences.first()

    // Dekomposisi kalimat target ke urutan tuts Hangul 2-Beol-Sik
    val targetJamos = remember(doc.id, selectedSentenceIndex) {
        HangulKeyboardUtil.decompose(currentSentence.korean)
    }

    val typedJamos = remember(doc.id, selectedSentenceIndex) { mutableStateListOf<Char>() }
    var isShiftActive by remember(doc.id, selectedSentenceIndex) { mutableStateOf(false) }
    var wrongKeyWarning by remember(doc.id, selectedSentenceIndex) { mutableStateOf<String?>(null) }
    var isSentenceFinished by remember(doc.id, selectedSentenceIndex) { mutableStateOf(false) }

    // Indeks tuts berikutnya yang harus ditekan
    val nextJamoIndex = typedJamos.size
    val nextExpectedJamo: Char? = if (nextJamoIndex < targetJamos.size) targetJamos[nextJamoIndex] else null

    // Compose hasil ketikan saat ini
    val composedText = remember(typedJamos.toList()) {
        HangulKeyboardUtil.compose(typedJamos)
    }

    // Cek kelulusan kalimat
    LaunchedEffect(typedJamos.size, targetJamos.size) {
        if (targetJamos.isNotEmpty() && typedJamos.size >= targetJamos.size && !isSentenceFinished) {
            isSentenceFinished = true
            onAddXp(25)
            onSpeak("잘했어요!", ttsSpeed)
            onRecordProgress("doc_typing", "${doc.id}_$selectedSentenceIndex", "${doc.titleKo} #${selectedSentenceIndex + 1}", 100)
        }
    }

    // Handler penekanan tuts keyboard
    fun handleKeyPress(pressedChar: Char) {
        if (!isSentenceFinished) {
            if (nextExpectedJamo != null && pressedChar == nextExpectedJamo) {
                typedJamos.add(pressedChar)
                wrongKeyWarning = null
                if (isShiftActive) isShiftActive = false
            } else if (nextExpectedJamo != null) {
                val expectedQwerty = HangulKeyboardUtil.getQwertyKeyHint(nextExpectedJamo)
                wrongKeyWarning = "⚠️ Tuts ditekan: '$pressedChar' | Seharusnya: '$nextExpectedJamo' (QWERTY: $expectedQwerty)"
            }
        }
    }

    // Bantuan otomatis 1 tuts jika pengguna kesulitan
    fun autoAssistOneJamo() {
        if (!isSentenceFinished && nextExpectedJamo != null) {
            typedJamos.add(nextExpectedJamo)
            wrongKeyWarning = null
            if (isShiftActive) isShiftActive = false
        }
    }

    // Dukungan input keyboard fisik / hardware
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(doc.id, selectedSentenceIndex) {
        try {
            focusRequester.requestFocus()
        } catch (_: Exception) {}
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .focusRequester(focusRequester)
            .focusable()
            .onKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown) {
                    when (keyEvent.key) {
                        Key.Backspace -> {
                            if (typedJamos.isNotEmpty()) {
                                typedJamos.removeAt(typedJamos.lastIndex)
                                wrongKeyWarning = null
                                isSentenceFinished = false
                            }
                            true
                        }
                        Key.Spacebar -> {
                            handleKeyPress(' ')
                            true
                        }
                        else -> {
                            val codePoint = keyEvent.utf16CodePoint
                            if (codePoint > 0) {
                                val ch = codePoint.toChar()
                                val mapped = HangulKeyboardUtil.mapQwertyToHangul(ch, keyEvent.isShiftPressed)
                                if (mapped != null) {
                                    handleKeyPress(mapped)
                                    true
                                } else false
                            } else false
                        }
                    }
                } else false
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Pemilih Kalimat Dokumen (Sentence Selector)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Kalimat ${selectedSentenceIndex + 1} dari ${doc.sentences.size}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = HaneulBlue
            )

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                OutlinedButton(
                    onClick = {
                        if (selectedSentenceIndex > 0) {
                            selectedSentenceIndex--
                        }
                    },
                    enabled = selectedSentenceIndex > 0,
                    modifier = Modifier.height(32.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Text("◀ Sebelumnya", fontSize = 10.sp)
                }

                Button(
                    onClick = {
                        if (selectedSentenceIndex < doc.sentences.size - 1) {
                            selectedSentenceIndex++
                        }
                    },
                    enabled = selectedSentenceIndex < doc.sentences.size - 1,
                    colors = ButtonDefaults.buttonColors(containerColor = HaneulBlue),
                    modifier = Modifier.height(32.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Text("Berikutnya ▶", fontSize = 10.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Target Kalimat yang harus diketik
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = CardDefaults.outlinedCardBorder(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "🎯 Target Kalimat",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = HaneulBlue
                        )
                        if (currentSentence.speaker != null) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = HaneulContainer
                            ) {
                                Text(
                                    text = currentSentence.speaker,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = HaneulBlue,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                    IconButton(
                        onClick = { onSpeak(currentSentence.korean, ttsSpeed) },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Lafal", tint = HaneulBlue, modifier = Modifier.size(16.dp))
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = currentSentence.korean,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = InkBlack,
                    lineHeight = 22.sp
                )
                Text(
                    text = currentSentence.indonesian,
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Roadmap Tuts Jamo yang perlu ditekan
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF1F5F9), RoundedCornerShape(8.dp))
                .padding(horizontal = 6.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            itemsIndexed(targetJamos) { idx, jamo ->
                val isTyped = idx < typedJamos.size
                val isCurrent = idx == typedJamos.size
                val qwertyKey = HangulKeyboardUtil.getQwertyKeyHint(jamo)

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = when {
                        isTyped -> SaessakGreen
                        isCurrent -> NunbitYellow
                        else -> Color.White
                    },
                    modifier = Modifier.border(
                        1.dp,
                        if (isCurrent) Color(0xFFD97706) else Color(0xFFCBD5E1),
                        RoundedCornerShape(6.dp)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (jamo == ' ') "␣" else jamo.toString(),
                            fontSize = 12.sp,
                            fontWeight = if (isCurrent) FontWeight.Black else FontWeight.Bold,
                            color = when {
                                isTyped -> Color.White
                                isCurrent -> Color(0xFF78350F)
                                else -> InkBlack
                            }
                        )
                        Text(
                            text = qwertyKey,
                            fontSize = 8.sp,
                            color = when {
                                isTyped -> Color.White.copy(alpha = 0.8f)
                                isCurrent -> Color(0xFF92400E)
                                else -> TextMuted
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Hasil Ketikan Pengguna
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color.White,
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.horizontalGradient(
                    if (isSentenceFinished) listOf(SaessakGreen, SaessakGreen)
                    else listOf(Color(0xFFCBD5E1), Color(0xFFCBD5E1))
                )
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Hasil Ketikan Anda:", fontSize = 10.sp, color = TextMuted)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${typedJamos.size}/${targetJamos.size} tuts",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSentenceFinished) SaessakGreen else HaneulBlue
                        )
                    }
                    Text(
                        text = if (composedText.isEmpty()) "Mulai tekan tuts keyboard di bawah..." else "$composedText ✍️",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (composedText.isEmpty()) TextMuted else InkBlack
                    )
                }

                if (isSentenceFinished) {
                    Column(horizontalAlignment = Alignment.End) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFDCFCE7)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SaessakGreen, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("Sempurna! +25 XP", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF15803D))
                            }
                        }

                        Spacer(modifier = Modifier.height(3.dp))

                        if (selectedSentenceIndex < doc.sentences.size - 1) {
                            Button(
                                onClick = {
                                    selectedSentenceIndex++
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SaessakGreen),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.height(28.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                Text("Lanjut Berikutnya ▶", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Button(
                                onClick = {
                                    selectedSentenceIndex = 0
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = HaneulBlue),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.height(28.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                Text("🎉 Selesai Dokumen! 🔄", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Peringatan jika menekan tuts yang keliru
        if (wrongKeyWarning != null) {
            Spacer(modifier = Modifier.height(3.dp))
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFFEE2E2),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = wrongKeyWarning ?: "",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFB91C1C)
                    )
                }
            }
        }

        // Toolbar Bantuan Cepat (Bantu 1 Tuts 💡 & Kontrol)
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                OutlinedButton(
                    onClick = { autoAssistOneJamo() },
                    enabled = !isSentenceFinished && nextExpectedJamo != null,
                    modifier = Modifier.height(30.dp),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Icon(Icons.Default.Lightbulb, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("Bantu 1 Tuts", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF92400E))
                }

                OutlinedButton(
                    onClick = {
                        if (typedJamos.isNotEmpty()) {
                            typedJamos.removeAt(typedJamos.lastIndex)
                            wrongKeyWarning = null
                            isSentenceFinished = false
                        }
                    },
                    enabled = typedJamos.isNotEmpty(),
                    modifier = Modifier.height(30.dp),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Backspace, contentDescription = null, tint = SarangRed, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("Hapus", fontSize = 10.sp, color = InkBlack)
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                OutlinedButton(
                    onClick = {
                        typedJamos.clear()
                        wrongKeyWarning = null
                        isSentenceFinished = false
                    },
                    modifier = Modifier.height(30.dp),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("Reset", fontSize = 10.sp, color = InkBlack)
                }

                IconButton(
                    onClick = {
                        if (composedText.isNotEmpty()) onSpeak(composedText, ttsSpeed)
                        else onSpeak(currentSentence.korean, ttsSpeed)
                    },
                    modifier = Modifier.size(30.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Lafal", tint = HaneulBlue, modifier = Modifier.size(18.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Virtual 2-Beol-Sik Keyboard
        VirtualKorean2BeolSikKeyboard(
            isShiftActive = isShiftActive,
            highlightChar = nextExpectedJamo,
            onKeyPress = { pressedChar -> handleKeyPress(pressedChar) },
            onShiftToggle = { isShiftActive = !isShiftActive },
            onBackspace = {
                if (typedJamos.isNotEmpty()) {
                    typedJamos.removeAt(typedJamos.lastIndex)
                    wrongKeyWarning = null
                    isSentenceFinished = false
                }
            },
            onReset = {
                typedJamos.clear()
                wrongKeyWarning = null
                isSentenceFinished = false
            },
            onSpeak = {
                if (composedText.isNotEmpty()) {
                    onSpeak(composedText, ttsSpeed)
                } else {
                    onSpeak(currentSentence.korean, ttsSpeed)
                }
            }
        )
    }
}

/**
 * Sub-Tab 3: ✍️ Tulis Sendiri Dokumen Korea (Drafting, Template & Salin Teks)
 */
@Composable
fun DocumentSelfDraftingView(
    ttsSpeed: Float,
    onSpeak: (String, Float) -> Unit,
    onAddXp: (Int) -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    // Pilihan Template Siap Pakai
    val templates = listOf(
        Pair("💌 Surat Sahabat (반말)", """
            민우에게,
            
            안녕! 잘 지내?
            요즘 한국어 공부가 너무 재미있어.
            우리 주말에 만나서 맛있는 거 먹자!
            
            너의 친구가.
        """.trimIndent()),
        Pair("💌 Fan Letter Idola (존댓말)", """
            사랑하는 오빠에게,
            
            안녕하세요! 항상 멀리서 응원하고 있어요.
            오빠의 노래를 들으면 언제나 큰 힘이 돼요.
            항상 아프지 말고 건강하세요! 사랑해요!
            
            인도네시아 팬이.
        """.trimIndent()),
        Pair("💬 Chat Janjian (KakaoTalk)", """
            오늘 수업 끝나고 뭐 해?
            홍대 카페 갈래?
            ㅇㅇ 좋아! 3시에 9번 출구에서 봐!
            콜! 이따 봐~ ㅋㅋ
        """.trimIndent()),
        Pair("📖 Diary Hari Ini (일기)", """
            2026년 11월 1일 (맑음)
            
            오늘은 하루 종일 한글 타자 연습을 했다.
            처음에는 어려웠지만 점점 타자가 빨라져서 뿌듯하다.
            내일도 열심히 공부해야지!
        """.trimIndent())
    )

    var draftTitle by remember { mutableStateOf("Surat untuk Teman") }
    var draftContent by remember { mutableStateOf(templates[0].second) }
    var isVirtualKeyboardOpen by remember { mutableStateOf(false) }
    var isShiftActive by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Info Banner
        item {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = HaneulContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, tint = HaneulBlue, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Tulis Dokumen / Surat Korea Anda Sendiri", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = InkBlack)
                        Text("Pilih template awal, edit kata-katanya, dengarkan lafalnya, lalu salin teks untuk dikirim ke KakaoTalk / Media Sosial!", fontSize = 11.sp, color = TextSecondary)
                    }
                }
            }
        }

        // Pilihan Template Awal
        item {
            Text("Pilih Template Cepat:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = InkBlack)
            Spacer(modifier = Modifier.height(4.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(templates) { (title, templateText) ->
                    OutlinedButton(
                        onClick = {
                            draftTitle = title
                            draftContent = templateText
                        },
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Area Pengeditan Teks
        item {
            OutlinedTextField(
                value = draftContent,
                onValueChange = { draftContent = it },
                label = { Text("Isi Dokumen / Surat Bahasa Korea") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .testTag("doc_draft_content_input"),
                shape = RoundedCornerShape(12.dp)
            )
        }

        // Toolbar Tombol Aksi
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Tombol Putar Lafal Dokumen Sendiri
                Button(
                    onClick = {
                        if (draftContent.isNotBlank()) {
                            onSpeak(draftContent, ttsSpeed)
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = HaneulBlue)
                ) {
                    Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Dengar Suara", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                // Tombol Salin ke Clipboard
                Button(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(draftContent))
                        Toast.makeText(context, "Dokumen berhasil disalin ke clipboard! Siap dipaste ke KakaoTalk/IG.", Toast.LENGTH_LONG).show()
                        onAddXp(15)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SaessakGreen)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Salin Teks", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                // Tombol Toggle Keyboard Virtual
                OutlinedButton(
                    onClick = { isVirtualKeyboardOpen = !isVirtualKeyboardOpen },
                    modifier = Modifier.height(44.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Keyboard, contentDescription = null, tint = HaneulBlue, modifier = Modifier.size(18.dp))
                }
            }
        }

        // Keyboard Virtual jika dibuka
        if (isVirtualKeyboardOpen) {
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    border = CardDefaults.outlinedCardBorder(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("Keyboard 2-Beol-Sik Tambahan:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = HaneulBlue)
                        Spacer(modifier = Modifier.height(4.dp))
                        VirtualKorean2BeolSikKeyboard(
                            isShiftActive = isShiftActive,
                            highlightChar = null,
                            onKeyPress = { ch ->
                                val decomposed = HangulKeyboardUtil.decompose(draftContent).toMutableList()
                                decomposed.add(ch)
                                draftContent = HangulKeyboardUtil.compose(decomposed)
                            },
                            onShiftToggle = { isShiftActive = !isShiftActive },
                            onBackspace = {
                                val decomposed = HangulKeyboardUtil.decompose(draftContent).toMutableList()
                                if (decomposed.isNotEmpty()) {
                                    decomposed.removeAt(decomposed.lastIndex)
                                    draftContent = HangulKeyboardUtil.compose(decomposed)
                                }
                            },
                            onReset = { draftContent = "" },
                            onSpeak = {
                                if (draftContent.isNotEmpty()) onSpeak(draftContent, ttsSpeed)
                            }
                        )
                    }
                }
            }
        }

        // Pratinjau Dokumen Format Bersih (Preview Card)
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFDF5)),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.horizontalGradient(listOf(Color(0xFFFDE68A), Color(0xFFFCD34D)))
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("📄 Pratinjau Dokumen Jadi", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF92400E))
                        Text("Siap Kirim ✅", fontSize = 11.sp, color = SaessakGreen, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (draftContent.isBlank()) "(Dokumen masih kosong...)" else draftContent,
                        fontSize = 14.sp,
                        color = InkBlack,
                        lineHeight = 22.sp
                    )
                }
            }
        }
    }
}
