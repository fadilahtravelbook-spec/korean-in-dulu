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
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.MainViewModel
import com.example.ui.screens.AddCustomVocabScreen
import com.example.ui.screens.BatchimGuideScreen
import com.example.ui.screens.CustomTextReaderScreen
import com.example.ui.screens.DailyLifeHubScreen
import com.example.ui.screens.GrammarHubScreen
import com.example.ui.screens.HangulIntroScreen
import com.example.ui.screens.HangulLetterDetailScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.KeyboardChallengeScreen
import com.example.ui.screens.KoreanDocumentScreen
import com.example.ui.screens.ListeningScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.ReadingScreen
import com.example.ui.screens.RolePlayScreen
import com.example.ui.screens.SpeakingScreen
import com.example.ui.screens.StatsDetailScreen
import com.example.ui.screens.SyllableCombineScreen
import com.example.ui.screens.VideoImmersionScreen
import com.example.ui.screens.VocabMatchingGameScreen
import com.example.ui.screens.VocabularyScreen
import com.example.ui.screens.WordPracticeScreen
import com.example.ui.screens.WpmSpeedReadingScreen
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
                var initialWritingLetter by remember { mutableStateOf<String?>(null) }
                var selectedDetailCharacter by remember { mutableStateOf("ㅏ") }

                // Check if user profile is ready or onboarding is needed
                val currentProfile = profile
                if (currentProfile == null) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = HaneulBlue)
                    }
                } else if (!currentProfile.onboarded) {
                    OnboardingScreen(
                        placementQuestions = viewModel.repository.placementTestQuestions,
                        onComplete = { name, level, goalMinutes ->
                            viewModel.completeOnboarding(name, level, goalMinutes)
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
                                    modifier = Modifier.testTag("main_bottom_nav_bar")
                                ) {
                                    NavigationBarItem(
                                        selected = currentScreen == "home",
                                        onClick = {
                                            viewModel.stopSpeaking()
                                            currentScreen = "home"
                                        },
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
                                        onClick = {
                                            viewModel.stopSpeaking()
                                            currentScreen = "writing"
                                        },
                                        icon = { Icon(Icons.Default.Keyboard, contentDescription = "Ketik") },
                                        label = { Text("Ketik", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
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
                                        onClick = {
                                            viewModel.stopSpeaking()
                                            currentScreen = "vocab"
                                        },
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
                                        onClick = {
                                            viewModel.stopSpeaking()
                                            currentScreen = "profile"
                                        },
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
                            val navigateBack: () -> Unit = {
                                viewModel.stopSpeaking()
                                currentScreen = when (currentScreen) {
                                    "hangul_letter_detail" -> "hangul_intro"
                                    "role_play" -> "speaking"
                                    "add_custom_vocab", "vocab_matching_game" -> "vocab"
                                    "custom_reader", "wpm_reading" -> "reading"
                                    "korean_documents", "word_practice", "keyboard_challenge", "batchim_guide", "syllable_combine" -> "writing"
                                    else -> "home"
                                }
                            }

                            BackHandler(enabled = currentScreen != "home") {
                                navigateBack()
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
                                        onBack = navigateBack,
                                        onSpeak = { text -> viewModel.speak(text) },
                                        onNavigateToWriting = { char ->
                                            initialWritingLetter = char
                                            currentScreen = "writing"
                                        },
                                        onAddXp = { xp ->
                                            viewModel.addXp(xp)
                                        },
                                        onNavigateToDetail = { char ->
                                            selectedDetailCharacter = char
                                            currentScreen = "hangul_letter_detail"
                                        }
                                    )
                                }

                                "writing" -> {
                                    WritingScreen(
                                        letters = viewModel.repository.hangulAlphabet,
                                        initialLetter = initialWritingLetter,
                                        onBack = {
                                            initialWritingLetter = null
                                            navigateBack()
                                        },
                                        onSpeak = { text, speed -> viewModel.speak(text, speed) },
                                        onStopSpeak = { viewModel.stopSpeaking() },
                                        onAddXp = { xp -> viewModel.addXp(xp) },
                                        onRecordProgress = { mod, lesson, title, score ->
                                            viewModel.recordProgress(mod, lesson, title, score)
                                        },
                                        onNavigateToHangulIntro = {
                                            currentScreen = "hangul_intro"
                                        },
                                        onNavigateToDocuments = {
                                            currentScreen = "korean_documents"
                                        },
                                        onNavigateToWordPractice = {
                                            currentScreen = "word_practice"
                                        },
                                        onNavigateToKeyboardChallenge = {
                                            currentScreen = "keyboard_challenge"
                                        },
                                        onNavigateToSyllableCombine = {
                                            currentScreen = "syllable_combine"
                                        },
                                        onNavigateToBatchim = {
                                            currentScreen = "batchim_guide"
                                        }
                                    )
                                }

                                "speaking" -> {
                                    SpeakingScreen(
                                        templates = viewModel.repository.introTemplates,
                                        rolePlaySteps = viewModel.repository.rolePlaySteps,
                                        onBack = { currentScreen = "home" },
                                        onSpeak = { text -> viewModel.speak(text) },
                                        onSpeakWithSpeed = { text, speed -> viewModel.speak(text, speed) },
                                        onRecordProgress = { mod, lesson, title, score ->
                                            viewModel.recordProgress(mod, lesson, title, score)
                                        },
                                        onNavigateToRolePlay = {
                                            currentScreen = "role_play"
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
                                        },
                                        onAddCustomVocab = { ko, meaning, category ->
                                            viewModel.addCustomVocabCard(ko, meaning, category)
                                        },
                                        onAddXp = { xp ->
                                            viewModel.addXp(xp)
                                        },
                                        onNavigateToAddVocab = {
                                            currentScreen = "add_custom_vocab"
                                        },
                                        onNavigateToGame = {
                                            currentScreen = "vocab_matching_game"
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
                                        },
                                        onNavigateToCustomReader = {
                                            currentScreen = "custom_reader"
                                        },
                                        onNavigateToWpmReading = {
                                            currentScreen = "wpm_reading"
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

                                "korean_documents" -> {
                                    KoreanDocumentScreen(
                                        onBack = navigateBack,
                                        onSpeak = { text, speed -> viewModel.speak(text, speed) },
                                        onStopSpeak = { viewModel.stopSpeaking() },
                                        onAddXp = { xp -> viewModel.addXp(xp) },
                                        onRecordProgress = { mod, lesson, title, score ->
                                            viewModel.recordProgress(mod, lesson, title, score)
                                        }
                                    )
                                }

                                "word_practice" -> {
                                    WordPracticeScreen(
                                        onBack = navigateBack,
                                        onSpeak = { text, speed -> viewModel.speak(text, speed) },
                                        onStopSpeak = { viewModel.stopSpeaking() },
                                        onAddXp = { xp -> viewModel.addXp(xp) },
                                        onRecordProgress = { mod, lesson, title, score ->
                                            viewModel.recordProgress(mod, lesson, title, score)
                                        }
                                    )
                                }

                                "keyboard_challenge" -> {
                                    KeyboardChallengeScreen(
                                        onBack = navigateBack,
                                        onSpeak = { text, speed -> viewModel.speak(text, speed) },
                                        onStopSpeak = { viewModel.stopSpeaking() },
                                        onAddXp = { xp -> viewModel.addXp(xp) },
                                        onRecordProgress = { mod, lesson, title, score ->
                                            viewModel.recordProgress(mod, lesson, title, score)
                                        }
                                    )
                                }

                                "batchim_guide" -> {
                                    BatchimGuideScreen(
                                        onBack = navigateBack,
                                        onSpeak = { text, speed -> viewModel.speak(text, speed) },
                                        onStopSpeak = { viewModel.stopSpeaking() },
                                        onRecordProgress = { mod, lesson, title, score ->
                                            viewModel.recordProgress(mod, lesson, title, score)
                                        }
                                    )
                                }

                                "syllable_combine" -> {
                                    SyllableCombineScreen(
                                        onBack = navigateBack,
                                        onSpeak = { text, speed -> viewModel.speak(text, speed) },
                                        onStopSpeak = { viewModel.stopSpeaking() },
                                        onAddXp = { xp -> viewModel.addXp(xp) },
                                        onRecordProgress = { mod, lesson, title, score ->
                                            viewModel.recordProgress(mod, lesson, title, score)
                                        }
                                    )
                                }

                                "grammar_hub" -> {
                                    GrammarHubScreen(
                                        onBack = navigateBack,
                                        onSpeak = { text -> viewModel.speak(text) },
                                        onStopSpeak = { viewModel.stopSpeaking() }
                                    )
                                }

                                "daily_life_hub" -> {
                                    DailyLifeHubScreen(
                                        onBack = navigateBack,
                                        onSpeak = { text -> viewModel.speak(text) },
                                        onStopSpeak = { viewModel.stopSpeaking() }
                                    )
                                }

                                "hangul_letter_detail" -> {
                                    HangulLetterDetailScreen(
                                        character = selectedDetailCharacter,
                                        onBack = navigateBack,
                                        onSpeak = { text -> viewModel.speak(text) },
                                        onNavigateToWriting = { char ->
                                            initialWritingLetter = char
                                            currentScreen = "writing"
                                        }
                                    )
                                }

                                "role_play" -> {
                                    RolePlayScreen(
                                        rolePlaySteps = viewModel.repository.rolePlaySteps,
                                        onBack = navigateBack,
                                        onSpeak = { text -> viewModel.speak(text) },
                                        onSpeakWithSpeed = { text, speed -> viewModel.speak(text, speed) },
                                        onRecordProgress = { mod, lesson, title, score ->
                                            viewModel.recordProgress(mod, lesson, title, score)
                                        }
                                    )
                                }

                                "add_custom_vocab" -> {
                                    AddCustomVocabScreen(
                                        onBack = navigateBack,
                                        onSpeak = { text -> viewModel.speak(text) },
                                        onAddCustomVocab = { ko, meaning, category ->
                                            viewModel.addCustomVocabCard(ko, meaning, category)
                                            currentScreen = "vocab"
                                        },
                                        onAddXp = { xp -> viewModel.addXp(xp) }
                                    )
                                }

                                "vocab_matching_game" -> {
                                    VocabMatchingGameScreen(
                                        cards = vocabList,
                                        onBack = navigateBack,
                                        onSpeak = { text -> viewModel.speak(text) },
                                        onRewardXp = { xp -> viewModel.addXp(xp) }
                                    )
                                }

                                "custom_reader" -> {
                                    CustomTextReaderScreen(
                                        onBack = navigateBack,
                                        onSpeak = { text -> viewModel.speak(text) },
                                        onSaveToVocab = { ko, idMeaning ->
                                            viewModel.addCustomVocabCard(ko, idMeaning, "Kustom")
                                        }
                                    )
                                }

                                "wpm_reading" -> {
                                    WpmSpeedReadingScreen(
                                        stories = viewModel.repository.readingStories,
                                        onBack = navigateBack,
                                        onSpeak = { text -> viewModel.speak(text) },
                                        onRecordProgress = { mod, lesson, title, score ->
                                            viewModel.recordProgress(mod, lesson, title, score)
                                        }
                                    )
                                }

                                "stats_detail" -> {
                                    StatsDetailScreen(
                                        streak = profile?.streak ?: 1,
                                        xp = profile?.xp ?: 100,
                                        level = profile?.level ?: 1,
                                        completedLessons = completedCount,
                                        onBack = navigateBack,
                                        onNavigateToProfile = { currentScreen = "profile" },
                                        onUseStreakFreeze = { /* freeze logic */ }
                                    )
                                }

                                else -> {
                                    currentScreen = "home"
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
