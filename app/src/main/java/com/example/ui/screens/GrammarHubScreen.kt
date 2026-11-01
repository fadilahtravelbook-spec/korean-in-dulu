package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import com.example.ui.components.AppSubHeader
import com.example.ui.components.GrammarHubContent

/**
 * Halaman Khusus Panduan Tata Bahasa & Sistem Angka (문법 가이드 - TOPIK I)
 * Layar penuh yang terpisah dan terorganisir rapi tanpa dialog bertumpuk.
 */
@Composable
fun GrammarHubScreen(
    onBack: () -> Unit,
    onSpeak: (String) -> Unit,
    onStopSpeak: () -> Unit = {}
) {
    DisposableEffect(Unit) {
        onDispose {
            onStopSpeak()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .testTag("grammar_hub_screen")
    ) {
        AppSubHeader(
            title = "Panduan Tata Bahasa (문법 가이드)",
            subtitle = "Pola kalimat SOV, partikel topik/subjek/objek, akhiran sopan & sistem angka",
            onBack = onBack
        )

        Box(modifier = Modifier.fillMaxSize()) {
            GrammarHubContent(onSpeak = onSpeak)
        }
    }
}
