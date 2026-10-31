package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.MainViewModel
import com.example.ui.screens.HangulIntroScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ListeningScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.ReadingScreen
import com.example.ui.screens.SpeakingScreen
import com.example.ui.screens.VideoImmersionScreen
import com.example.ui.screens.VocabularyScreen
import com.example.ui.screens.WritingScreen
import com.example.ui.theme.HaneulBlue
import com.example.ui.theme.HaneulContainer
import com.example.ui.theme.InkBlack
import com.example.ui.theme.KoreanInDuluTheme
import com.example.ui.theme.NunbitYellow
import com.example.ui.theme.TextSecondary

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KoreanInDuluTheme {
                val viewModel: MainViewModel = viewModel()
                val profile by viewModel.userProfile.collectAsState()
                val vocabList by viewModel.allVocab.collectAsState()
                val completedCount by viewModel.completedCount.collectAsState()
                val unlockedBadges by viewModel.unlockedBadges.collectAsState()
                val learningMinutes by viewModel.totalLearningMinutes.collectAsState()
                val dailyChallenges by viewModel.dailyChallenges.collectAsState()

                // Current Screen route state
                var currentScreen by remember { mutableStateOf("home") }

                // Check if user has completed onboarding
                val showOnboarding = profile != null && !profile!!.onboarded

                if (showOnboarding) {
                    OnboardingScreen(
                        placementQuestions = viewModel.repository.placementTestQuestions,
                        onComplete = { name, level ->
                            viewModel.completeOnboarding(name, level)
                            currentScreen = "home"
                        }
                    )
                } else {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        contentWindowInsets = WindowInsets.safeDrawing,
                        bottomBar = {
                            val showBottomNav = currentScreen in listOf("home", "writing", "vocab", "profile")
                            if (showBottomNav) {
                                NavigationBar(
                                    containerColor = Color.White,
                                    tonalElevation = 6.dp,
                                    modifier = Modifier
                                        .windowInsetsPadding(WindowInsets.navigationBars)
                                        .testTag("main_bottom_nav_bar")
                                ) {
                                    NavigationBarItem(
                                        selected = currentScreen == "home",
                                        onClick = { currentScreen = "home" },
                                        icon = { Icon(Icons.Default.Home, contentDescription = "Beranda") },
                                        label = { Text("Beranda", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = HaneulBlue,
                                            selectedTextColor = HaneulBlue,
                                            indicatorColor = HaneulContainer,
                                            unselectedIconColor = TextSecondary,
                                            unselectedTextColor = TextSecondary
                                        )
                                    )
                                    NavigationBarItem(
                                        selected = currentScreen == "writing",
                                        onClick = { currentScreen = "writing" },
                                        icon = { Icon(Icons.Default.Edit, contentDescription = "Menulis") },
                                        label = { Text("Menulis", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = HaneulBlue,
                                            selectedTextColor = HaneulBlue,
                                            indicatorColor = HaneulContainer,
                                            unselectedIconColor = TextSecondary,
                                            unselectedTextColor = TextSecondary
                                        )
                                    )
                                    NavigationBarItem(
                                        selected = currentScreen == "vocab",
                                        onClick = { currentScreen = "vocab" },
                                        icon = { Icon(Icons.Default.Book, contentDescription = "Kosakata") },
                                        label = { Text("Kosakata", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = HaneulBlue,
                                            selectedTextColor = HaneulBlue,
                                            indicatorColor = HaneulContainer,
                                            unselectedIconColor = TextSecondary,
                                            unselectedTextColor = TextSecondary
                                        )
                                    )
                                    NavigationBarItem(
                                        selected = currentScreen == "profile",
                                        onClick = { currentScreen = "profile" },
                                        icon = { Icon(Icons.Default.Person, contentDescription = "Profil") },
                                        label = { Text("Profil", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = HaneulBlue,
                                            selectedTextColor = HaneulBlue,
                                            indicatorColor = HaneulContainer,
                                            unselectedIconColor = TextSecondary,
                                            unselectedTextColor = TextSecondary
                                        )
                                    )
                                }
                            }
                        }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            BackHandler(enabled = currentScreen != "home") {
                                currentScreen = "home"
                            }

                            when (currentScreen) {
                                "home" -> {
                                    HomeScreen(
                                        userProfile = profile,
                                        completedLessonsCount = completedCount,
                                        learningMinutes = learningMinutes,
                                        unlockedBadgeCount = unlockedBadges.size,
                                        dailyChallenges = dailyChallenges,
                                        onNavigateToModule = { moduleId ->
                                            currentScreen = moduleId
                                        },
                                        onNavigateToProfile = {
                                            currentScreen = "profile"
                                        },
                                        onSpeak = { text ->
                                            viewModel.speak(text)
                                        }
                                    )
                                }

                                "hangul_intro" -> {
                                    HangulIntroScreen(
                                        onBack = { currentScreen = "home" },
                                        onSpeak = { text -> viewModel.speak(text) },
                                        onNavigateToWriting = { char ->
                                            currentScreen = "writing"
                                        },
                                        onAddXp = { xp ->
                                            viewModel.addXp(xp)
                                        }
                                    )
                                }

                                "writing" -> {
                                    WritingScreen(
                                        letters = viewModel.repository.hangulAlphabet,
                                        onBack = { currentScreen = "home" },
                                        onSpeak = { text -> viewModel.speak(text) },
                                        onRecordProgress = { mod, lesson, title, score ->
                                            viewModel.recordProgress(mod, lesson, title, score)
                                        },
                                        onNavigateToHangulIntro = {
                                            currentScreen = "hangul_intro"
                                        }
                                    )
                                }

                                "speaking" -> {
                                    SpeakingScreen(
                                        templates = viewModel.repository.introTemplates,
                                        rolePlaySteps = viewModel.repository.rolePlaySteps,
                                        onBack = { currentScreen = "home" },
                                        onSpeak = { text -> viewModel.speak(text) },
                                        onRecordProgress = { mod, lesson, title, score ->
                                            viewModel.recordProgress(mod, lesson, title, score)
                                        }
                                    )
                                }

                                "listening" -> {
                                    ListeningScreen(
                                        quizzes = viewModel.repository.listeningQuizzes,
                                        onBack = { currentScreen = "home" },
                                        onSpeak = { text, speed -> viewModel.speak(text, speed) },
                                        onRecordProgress = { mod, lesson, title, score ->
                                            viewModel.recordProgress(mod, lesson, title, score)
                                        }
                                    )
                                }

                                "vocab" -> {
                                    VocabularyScreen(
                                        vocabList = vocabList,
                                        onBack = { currentScreen = "home" },
                                        onSpeak = { text -> viewModel.speak(text) },
                                        onSubmitSrs = { card, rating ->
                                            viewModel.submitSrsReview(card, rating)
                                        },
                                        onToggleBookmark = { cardId ->
                                            viewModel.toggleBookmark(cardId)
                                        }
                                    )
                                }

                                "reading" -> {
                                    ReadingScreen(
                                        stories = viewModel.repository.readingStories,
                                        onBack = { currentScreen = "home" },
                                        onSpeak = { text -> viewModel.speak(text) },
                                        onSaveToVocab = { ko, idMeaning ->
                                            viewModel.addCustomVocabCard(ko, idMeaning)
                                        },
                                        onRecordProgress = { mod, lesson, title, score ->
                                            viewModel.recordProgress(mod, lesson, title, score)
                                        }
                                    )
                                }

                                "video" -> {
                                    VideoImmersionScreen(
                                        videoLessons = viewModel.repository.videoLessons,
                                        onBack = { currentScreen = "home" },
                                        onSpeak = { text, speed -> viewModel.speak(text, speed) },
                                        onSaveToVocab = { ko, idMeaning ->
                                            viewModel.addCustomVocabCard(ko, idMeaning, "Video")
                                        },
                                        onRecordProgress = { mod, lesson, title, score ->
                                            viewModel.recordProgress(mod, lesson, title, score)
                                        }
                                    )
                                }

                                "profile" -> {
                                    ProfileScreen(
                                        userProfile = profile,
                                        completedLessonsCount = completedCount,
                                        learningMinutes = learningMinutes,
                                        unlockedBadges = unlockedBadges,
                                        masterBadges = viewModel.repository.masterBadges,
                                        onBack = { currentScreen = "home" },
                                        onSpeakTest = { text, spd ->
                                            viewModel.speak(text, spd)
                                        },
                                        onToggleRomanization = { show ->
                                            viewModel.toggleRomanization(show)
                                        },
                                        onUpdateTtsSpeed = { spd ->
                                            viewModel.updateTtsSpeed(spd)
                                        },
                                        onResetOnboarding = {
                                            viewModel.resetOnboarding()
                                        }
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
