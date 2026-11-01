package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.VocabCard
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

@Composable
fun VocabularyScreen(
    vocabList: List<VocabCard>,
    onBack: () -> Unit,
    onSpeak: (String) -> Unit,
    onSubmitSrs: (card: VocabCard, rating: Int) -> Unit,
    onToggleBookmark: (cardId: String) -> Unit,
    onAddCustomVocab: ((korean: String, meaning: String, category: String) -> Unit)? = null
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Flashcard SRS, 1: Daftar Kosakata, 2: Game Cocokkan Kata
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Semua") }
    var showOnlyBookmarked by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }

    // Dialog Input State
    var inputKorean by remember { mutableStateOf("") }
    var inputMeaning by remember { mutableStateOf("") }
    var inputCategory by remember { mutableStateOf("Kustom") }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = {
                Text("Tambah Kosakata Kustom", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = HaneulBlue)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Simpan kata baru ke koleksi SRS Spaced Repetition Anda.", fontSize = 12.sp, color = TextSecondary)
                    OutlinedTextField(
                        value = inputKorean,
                        onValueChange = { inputKorean = it },
                        label = { Text("Bahasa Korea (Hangul)") },
                        placeholder = { Text("Contoh: 사과") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("add_vocab_korean_input")
                    )
                    OutlinedTextField(
                        value = inputMeaning,
                        onValueChange = { inputMeaning = it },
                        label = { Text("Arti Bahasa Indonesia") },
                        placeholder = { Text("Contoh: Apel") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("add_vocab_meaning_input")
                    )
                    OutlinedTextField(
                        value = inputCategory,
                        onValueChange = { inputCategory = it },
                        label = { Text("Kategori") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("add_vocab_category_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (inputKorean.isNotBlank() && inputMeaning.isNotBlank()) {
                            onAddCustomVocab?.invoke(inputKorean.trim(), inputMeaning.trim(), inputCategory.trim().ifBlank { "Kustom" })
                            inputKorean = ""
                            inputMeaning = ""
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HaneulBlue),
                    shape = RoundedCornerShape(10.dp),
                    enabled = inputKorean.isNotBlank() && inputMeaning.isNotBlank()
                ) {
                    Text("Simpan ke Kamus", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                Button(
                    onClick = { showAddDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F5F9))
                ) {
                    Text("Batal", color = InkBlack)
                }
            }
        )
    }

    Scaffold(
        floatingActionButton = {
            if (selectedTab == 1 && onAddCustomVocab != null) {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = HaneulBlue,
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier.testTag("add_vocab_fab")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Tambah Kata")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Tambah Kata", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF8FAFC))
        ) {
            AppSubHeader(
                title = "Kosakata (단어장)",
                subtitle = "Spaced Repetition (SRS SM-2) & Kamus Pintar",
                onBack = onBack
            )

            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = HaneulBlue
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("SRS Flashcards", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Daftar Kata (${vocabList.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Game Pasangan", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                )
            }

            when (selectedTab) {
                0 -> {
                    SrsFlashcardReviewView(
                        cards = vocabList,
                        onSpeak = onSpeak,
                        onSubmitSrs = onSubmitSrs
                    )
                }
                1 -> {
                    VocabListView(
                        vocabList = vocabList,
                        searchQuery = searchQuery,
                        onSearchChange = { searchQuery = it },
                        selectedCategory = selectedCategory,
                        onCategoryChange = { selectedCategory = it },
                        showOnlyBookmarked = showOnlyBookmarked,
                        onToggleOnlyBookmarked = { showOnlyBookmarked = !showOnlyBookmarked },
                        onSpeak = onSpeak,
                        onToggleBookmark = onToggleBookmark
                    )
                }
                2 -> {
                    VocabMatchingGameView(
                        cards = vocabList,
                        onSpeak = onSpeak
                    )
                }
            }
        }
    }
}

@Composable
fun SrsFlashcardReviewView(
    cards: List<VocabCard>,
    onSpeak: (String) -> Unit,
    onSubmitSrs: (card: VocabCard, rating: Int) -> Unit
) {
    var currentIndex by remember { mutableIntStateOf(0) }
    var isFlipped by remember { mutableStateOf(false) }

    if (cards.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Belum ada kosakata untuk di-review.", color = TextSecondary)
        }
        return
    }

    val currentCard = cards[currentIndex % cards.size]

    // 3D Card Flip Animation
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 350),
        label = "flashcard_flip_animation"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Progress header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFEF3C7)
                ) {
                    Text(
                        text = "SM-2 Interval: ${currentCard.interval} hari",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = Color(0xFF92400E),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
            Text(
                text = "Kartu ${(currentIndex % cards.size) + 1} dari ${cards.size}",
                fontSize = 12.sp,
                color = TextSecondary,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Big 3D Animated Flashcard
        Card(
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .graphicsLayer {
                    rotationY = rotation
                    cameraDistance = 12f * density
                }
                .border(1.5.dp, Color(0xFFE2E8F0), RoundedCornerShape(26.dp))
                .clickable {
                    isFlipped = !isFlipped
                }
                .testTag("srs_flashcard_card")
        ) {
            if (rotation <= 90f) {
                // FRONT OF CARD
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFEFF6FF)
                    ) {
                        Text(
                            text = "Kategori: ${currentCard.category}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = HaneulBlue,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    Text(
                        text = currentCard.korean,
                        fontSize = 42.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = InkBlack,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "[${currentCard.romanization}]",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    IconButton(
                        onClick = { onSpeak(currentCard.korean) },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(HaneulContainer)
                            .size(54.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Dengarkan Audio",
                            tint = HaneulBlue,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.TouchApp, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Ketuk kartu untuk melihat arti & contoh", fontSize = 11.sp, color = TextMuted)
                    }
                }
            } else {
                // BACK OF CARD (Flipped)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { rotationY = 180f }
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = currentCard.korean,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = HaneulBlue
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = currentCard.meaning,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = InkBlack,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFF8FAFC),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Contoh Kalimat:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = currentCard.exampleSentence,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = InkBlack
                            )
                            Text(
                                text = currentCard.exampleMeaning,
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    IconButton(
                        onClick = { onSpeak(currentCard.exampleSentence) },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(HaneulContainer)
                            .size(46.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Dengarkan Contoh",
                            tint = HaneulBlue,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // SRS Rating Buttons (SM-2 Algorithm grading)
        if (isFlipped) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Berapa mudah Anda mengingat kata ini?",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            onSubmitSrs(currentCard, 1)
                            isFlipped = false
                            currentIndex++
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SarangRed),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Lupa", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("Ulangi", fontSize = 9.sp)
                        }
                    }

                    Button(
                        onClick = {
                            onSubmitSrs(currentCard, 3)
                            isFlipped = false
                            currentIndex++
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NunbitYellow),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Ragu", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = InkBlack)
                            Text("+3 Hari", fontSize = 9.sp, color = InkBlack)
                        }
                    }

                    Button(
                        onClick = {
                            onSubmitSrs(currentCard, 5)
                            isFlipped = false
                            currentIndex++
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SaessakGreen),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Mudah", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("+7 Hari", fontSize = 9.sp)
                        }
                    }
                }
            }
        } else {
            Button(
                onClick = { isFlipped = true },
                colors = ButtonDefaults.buttonColors(containerColor = HaneulBlue),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("show_answer_button")
            ) {
                Text("Buka Jawaban Kartu (Balik)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun VocabListView(
    vocabList: List<VocabCard>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedCategory: String,
    onCategoryChange: (String) -> Unit,
    showOnlyBookmarked: Boolean,
    onToggleOnlyBookmarked: () -> Unit,
    onSpeak: (String) -> Unit,
    onToggleBookmark: (String) -> Unit
) {
    val categories = listOf("Semua", "Salam", "Makanan", "Keluarga", "Angka", "Kehidupan", "Emosi & Kesal", "Tegur Sapa", "Toilet & Mandi", "Sepeda & Transportasi", "Kustom")

    val filteredList = vocabList.filter { card ->
        (!showOnlyBookmarked || card.isBookmarked) &&
        (selectedCategory == "Semua" || card.category.contains(selectedCategory, ignoreCase = true)) &&
        (searchQuery.isBlank() || card.korean.contains(searchQuery) || card.meaning.contains(searchQuery, ignoreCase = true) || card.romanization.contains(searchQuery, ignoreCase = true))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            placeholder = { Text("Cari kata dalam Hangul, Latin, atau Indonesia...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchChange("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Hapus pencarian", tint = TextSecondary)
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("vocab_search_field")
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (showOnlyBookmarked) Color(0xFFFEF3C7) else Color(0xFFF1F5F9),
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onToggleOnlyBookmarked() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        if (showOnlyBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = null,
                        tint = if (showOnlyBookmarked) NunbitYellow else TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (showOnlyBookmarked) "Hanya Bookmark ⭐" else "Filter Bookmark",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (showOnlyBookmarked) Color(0xFF92400E) else TextSecondary
                    )
                }
            }

            Text(
                text = "${filteredList.size} kosakata ditemukan",
                fontSize = 11.sp,
                color = TextSecondary,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(categories) { cat ->
                val isSel = cat == selectedCategory
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSel) HaneulBlue else Color.White,
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(if (isSel) HaneulBlue else Color(0xFFE2E8F0))
                    ),
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onCategoryChange(cat) }
                ) {
                    Text(
                        text = cat,
                        color = if (isSel) Color.White else InkBlack,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFE2E8F0))
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "🔍 Tidak ada kosakata ditemukan",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = InkBlack
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tidak ada kata yang cocok dengan filter atau kata kunci \"$searchQuery\".",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = {
                                onSearchChange("")
                                onCategoryChange("Semua")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = HaneulBlue),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Reset Pencarian & Kategori", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredList) { card ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = card.korean, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = InkBlack)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = "[${card.romanization}]", fontSize = 12.sp, color = TextSecondary)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = card.meaning, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = HaneulBlue)
                                if (card.exampleSentence.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = "Contoh: ${card.exampleSentence}", fontSize = 11.sp, color = TextMuted)
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { onSpeak(card.korean) }) {
                                    Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Dengarkan", tint = HaneulBlue)
                                }
                                IconButton(onClick = { onToggleBookmark(card.id) }) {
                                    Icon(
                                        if (card.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                        contentDescription = "Simpan Bookmark",
                                        tint = if (card.isBookmarked) NunbitYellow else TextMuted
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

@Composable
fun VocabMatchingGameView(
    cards: List<VocabCard>,
    onSpeak: (String) -> Unit
) {
    var roundSeed by remember { mutableIntStateOf(0) }
    val samplePairs = remember(cards, roundSeed) {
        if (cards.size > 5) cards.shuffled().take(5) else cards
    }
    var selectedKorean by remember(roundSeed) { mutableStateOf<String?>(null) }
    var selectedMeaning by remember(roundSeed) { mutableStateOf<String?>(null) }
    val matchedKeys = remember(roundSeed) { mutableStateListOf<String>() }
    val shuffledMeanings = remember(samplePairs, roundSeed) { samplePairs.map { it.meaning }.shuffled() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "Game Pasangan Kata (Matching Pairs)", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = HaneulBlue)
                Text(text = "Cocokkan Hangul dengan artinya (${matchedKeys.size}/${samplePairs.size})", fontSize = 12.sp, color = TextSecondary)
            }
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = NunbitYellowContainer
            ) {
                Text(
                    text = "Babak ${roundSeed + 1}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = InkBlack,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                samplePairs.forEach { card ->
                    val isMatched = matchedKeys.contains(card.korean)
                    val isSelected = selectedKorean == card.korean

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = when {
                            isMatched -> Color(0xFFD1FAE5)
                            isSelected -> HaneulContainer
                            else -> Color.White
                        },
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(if (isSelected) HaneulBlue else Color(0xFFE2E8F0))
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(enabled = !isMatched) {
                                selectedKorean = card.korean
                                onSpeak(card.korean)
                                if (selectedMeaning != null) {
                                    val match = samplePairs.find { it.korean == card.korean && it.meaning == selectedMeaning }
                                    if (match != null) {
                                        matchedKeys.add(card.korean)
                                    }
                                    selectedKorean = null
                                    selectedMeaning = null
                                }
                            }
                    ) {
                        Text(
                            text = card.korean,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isMatched) Color(0xFF065F46) else InkBlack,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 14.dp)
                        )
                    }
                }
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                shuffledMeanings.forEach { meaning ->
                    val matchedCard = samplePairs.find { it.meaning == meaning }
                    val isMatched = matchedCard != null && matchedKeys.contains(matchedCard.korean)
                    val isSelected = selectedMeaning == meaning

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = when {
                            isMatched -> Color(0xFFD1FAE5)
                            isSelected -> HaneulContainer
                            else -> Color.White
                        },
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(if (isSelected) HaneulBlue else Color(0xFFE2E8F0))
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(enabled = !isMatched) {
                                selectedMeaning = meaning
                                if (selectedKorean != null) {
                                    val match = samplePairs.find { it.korean == selectedKorean && it.meaning == meaning }
                                    if (match != null) {
                                        matchedKeys.add(match.korean)
                                    }
                                    selectedKorean = null
                                    selectedMeaning = null
                                }
                            }
                    ) {
                        Text(
                            text = meaning,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isMatched) Color(0xFF065F46) else InkBlack,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 14.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (matchedKeys.size == samplePairs.size && samplePairs.isNotEmpty()) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFD1FAE5),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🎉 Luar Biasa! Semua 5 pasangan kata berhasil dicocokkan!",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF065F46),
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            roundSeed++
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SaessakGreen),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Babak Baru (Acak 5 Kata Lain) 🔄", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
