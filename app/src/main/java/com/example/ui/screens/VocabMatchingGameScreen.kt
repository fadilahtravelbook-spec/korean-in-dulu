package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun VocabMatchingGameScreen(
    cards: List<VocabCard>,
    onBack: () -> Unit,
    onSpeak: (String) -> Unit,
    onRewardXp: ((Int) -> Unit)? = null
) {
    var roundSeed by remember { mutableIntStateOf(0) }
    var selectedCategory by remember { mutableStateOf("Semua") }

    val categories = remember(cards) {
        listOf("Semua") + cards.map { it.category }.distinct()
    }

    val poolCards = remember(cards, selectedCategory) {
        if (selectedCategory == "Semua") cards
        else cards.filter { it.category == selectedCategory }
    }

    val activeRoundCards = remember(poolCards, roundSeed) {
        val available = if (poolCards.isNotEmpty()) poolCards else cards
        if (available.size > 5) available.shuffled().take(5) else available
    }

    var selectedKoreanId by remember(roundSeed, selectedCategory) { mutableStateOf<String?>(null) }
    var selectedMeaningId by remember(roundSeed, selectedCategory) { mutableStateOf<String?>(null) }
    val matchedIds = remember(roundSeed, selectedCategory) { mutableStateListOf<String>() }
    val shuffledMeanings = remember(activeRoundCards, roundSeed) { activeRoundCards.shuffled() }

    // Gamification & timer stats
    var comboStreak by remember(roundSeed, selectedCategory) { mutableIntStateOf(0) }
    var maxCombo by remember(roundSeed, selectedCategory) { mutableIntStateOf(0) }
    var elapsedSeconds by remember(roundSeed, selectedCategory) { mutableIntStateOf(0) }
    var hasCompletedRound by remember(roundSeed, selectedCategory) { mutableStateOf(false) }
    var wrongPair by remember(roundSeed, selectedCategory) { mutableStateOf<Pair<String, String>?>(null) }
    val coroutineScope = rememberCoroutineScope()

    // Timer loop while game is active
    LaunchedEffect(roundSeed, selectedCategory, hasCompletedRound) {
        if (!hasCompletedRound && activeRoundCards.isNotEmpty()) {
            while (true) {
                delay(1000L)
                elapsedSeconds++
            }
        }
    }

    // Check game completion
    LaunchedEffect(matchedIds.size) {
        if (matchedIds.size == activeRoundCards.size && activeRoundCards.isNotEmpty()) {
            hasCompletedRound = true
            val xpEarned = 25 + (maxCombo * 5)
            onRewardXp?.invoke(xpEarned)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        AppSubHeader(
            title = "Game Cocokkan Kata (매칭 게임)",
            subtitle = "Pasangkan Hangul dan artinya untuk melatih daya ingat cepat",
            onBack = onBack
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Category selector filter
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories) { cat ->
                    val isSelected = cat == selectedCategory
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) HaneulBlue else Color.White,
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.horizontalGradient(
                                if (isSelected) listOf(HaneulBlue, HaneulBlue)
                                else listOf(Color(0xFFE2E8F0), Color(0xFFE2E8F0))
                            )
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable {
                                selectedCategory = cat
                                roundSeed++
                            }
                    ) {
                        Text(
                            text = cat,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else TextSecondary,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                        )
                    }
                }
            }

            // Status Scoreboard Bar
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
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Progress & Counter
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Extension, contentDescription = null, tint = HaneulBlue, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Progres: ${matchedIds.size}/${activeRoundCards.size}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = InkBlack
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = {
                                if (activeRoundCards.isEmpty()) 0f
                                else matchedIds.size.toFloat() / activeRoundCards.size.toFloat()
                            },
                            modifier = Modifier
                                .width(120.dp)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = SaessakGreen,
                            trackColor = Color(0xFFE2E8F0)
                        )
                    }

                    // Combo Counter
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(
                                if (comboStreak >= 2) NunbitYellowContainer else Color(0xFFF1F5F9),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            Icons.Default.LocalFireDepartment,
                            contentDescription = null,
                            tint = if (comboStreak >= 2) Color(0xFFF97316) else TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Combo ${comboStreak}x",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (comboStreak >= 2) Color(0xFFC2410C) else TextSecondary
                        )
                    }

                    // Timer Counter
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Timer, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        val minutes = elapsedSeconds / 60
                        val secs = elapsedSeconds % 60
                        Text(
                            text = "%02d:%02d".format(minutes, secs),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = InkBlack
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Completion Banner Card
            if (hasCompletedRound) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, SaessakGreen, RoundedCornerShape(20.dp))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFDCFCE7),
                            modifier = Modifier.size(64.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = SaessakGreen, modifier = Modifier.size(36.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "축하합니다! Sempurna!",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = InkBlack
                        )
                        Text(
                            text = "Semua pasangan kata berhasil dicocokkan dalam waktu ${elapsedSeconds} detik dengan Max Combo ${maxCombo}x!",
                            fontSize = 13.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = NunbitYellowContainer,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Bolt, contentDescription = null, tint = NunbitYellow, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "+${25 + (maxCombo * 5)} XP Berhasil Didapatkan!",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color(0xFFB45309)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                roundSeed++
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = HaneulBlue),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("matching_game_play_again_btn")
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Mainkan Ronde Baru", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            } else {
                // Game Board: 2 Columns (Hangul on left, Meaning on right)
                Text(
                    text = "Ketuk satu kata Korea, lalu ketuk arti Indonesianya yang tepat:",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Left Column: Hangul
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "KOREA (한글)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = HaneulBlue,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        activeRoundCards.forEach { card ->
                            val isMatched = matchedIds.contains(card.id)
                            val isSelected = selectedKoreanId == card.id
                            val isWrong = wrongPair?.first == card.id

                            val bgColor by animateColorAsState(
                                targetValue = when {
                                    isMatched -> Color(0xFFDCFCE7)
                                    isWrong -> Color(0xFFFEE2E2)
                                    isSelected -> HaneulContainer
                                    else -> Color.White
                                },
                                animationSpec = tween(200)
                            )

                            val borderColor = when {
                                isMatched -> SaessakGreen
                                isWrong -> SarangRed
                                isSelected -> HaneulBlue
                                else -> Color(0xFFCBD5E1)
                            }

                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = bgColor),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(if (isSelected || isMatched || isWrong) 2.dp else 1.dp, borderColor, RoundedCornerShape(14.dp))
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable(enabled = !isMatched && wrongPair == null) {
                                        onSpeak(card.korean)
                                        if (selectedMeaningId != null) {
                                            // Check match
                                            if (selectedMeaningId == card.id) {
                                                matchedIds.add(card.id)
                                                comboStreak++
                                                if (comboStreak > maxCombo) maxCombo = comboStreak
                                                selectedKoreanId = null
                                                selectedMeaningId = null
                                            } else {
                                                val wrongMeaning = selectedMeaningId ?: ""
                                                comboStreak = 0
                                                wrongPair = Pair(card.id, wrongMeaning)
                                                selectedKoreanId = null
                                                selectedMeaningId = null
                                                coroutineScope.launch {
                                                    delay(450L)
                                                    wrongPair = null
                                                }
                                            }
                                        } else {
                                            selectedKoreanId = card.id
                                        }
                                    }
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 14.dp, horizontal = 10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = card.korean,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isMatched) SaessakGreen else InkBlack,
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        text = "[${card.romanization}]",
                                        fontSize = 10.sp,
                                        color = TextSecondary,
                                        textAlign = TextAlign.Center
                                    )
                                    if (isMatched) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            contentDescription = "Cocok",
                                            tint = SaessakGreen,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Right Column: Indonesian Meaning
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "INDONESIA (ARTI)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SarangRed,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        shuffledMeanings.forEach { card ->
                            val isMatched = matchedIds.contains(card.id)
                            val isSelected = selectedMeaningId == card.id
                            val isWrong = wrongPair?.second == card.id

                            val bgColor by animateColorAsState(
                                targetValue = when {
                                    isMatched -> Color(0xFFDCFCE7)
                                    isWrong -> Color(0xFFFEE2E2)
                                    isSelected -> Color(0xFFFFEDD5)
                                    else -> Color.White
                                },
                                animationSpec = tween(200)
                            )

                            val borderColor = when {
                                isMatched -> SaessakGreen
                                isWrong -> SarangRed
                                isSelected -> Color(0xFFF97316)
                                else -> Color(0xFFCBD5E1)
                            }

                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = bgColor),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(if (isSelected || isMatched || isWrong) 2.dp else 1.dp, borderColor, RoundedCornerShape(14.dp))
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable(enabled = !isMatched && wrongPair == null) {
                                        if (selectedKoreanId != null) {
                                            // Check match
                                            if (selectedKoreanId == card.id) {
                                                matchedIds.add(card.id)
                                                comboStreak++
                                                if (comboStreak > maxCombo) maxCombo = comboStreak
                                                selectedKoreanId = null
                                                selectedMeaningId = null
                                            } else {
                                                val wrongKo = selectedKoreanId ?: ""
                                                comboStreak = 0
                                                wrongPair = Pair(wrongKo, card.id)
                                                selectedKoreanId = null
                                                selectedMeaningId = null
                                                coroutineScope.launch {
                                                    delay(450L)
                                                    wrongPair = null
                                                }
                                            }
                                        } else {
                                            selectedMeaningId = card.id
                                        }
                                    }
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 14.dp, horizontal = 10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = card.meaning,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isMatched) SaessakGreen else InkBlack,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = card.category,
                                        fontSize = 10.sp,
                                        color = TextMuted,
                                        textAlign = TextAlign.Center
                                    )
                                    if (isMatched) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            contentDescription = "Cocok",
                                            tint = SaessakGreen,
                                            modifier = Modifier.size(14.dp)
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
}
