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
import com.example.ui.components.DailyLifeHubContent

/**
 * Halaman Khusus Panduan Keseharian di Korea (일상생활 가이드)
 * Layar penuh mandiri untuk situasi praktis: kafe, toserba, etiket, toilet, dan sepeda Ttareungi.
 */
@Composable
fun DailyLifeHubScreen(
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
            .testTag("daily_life_hub_screen")
    ) {
        AppSubHeader(
            title = "Panduan Keseharian di Korea (일상생활)",
            subtitle = "Percakapan nyata kafe, toserba 편의점, ekspresi emosi, sepeda 따릉이 & etiket",
            onBack = onBack
        )

        Box(modifier = Modifier.fillMaxSize()) {
            DailyLifeHubContent(onSpeak = onSpeak)
        }
    }
}
