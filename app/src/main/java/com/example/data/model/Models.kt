package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: String = "default_user",
    val name: String = "Sahabat Korea",
    val email: String = "user@koreanindulu.com",
    val level: Int = 1, // 1: Dasar (입문), 2: Pemula (초급), 3: Menengah (중급)
    val dailyGoalMinutes: Int = 15,
    val streak: Int = 3,
    val xp: Int = 240,
    val lastActiveDate: String = "",
    val lastAccessedLessonId: String = "writing_hangul_vowels",
    val showRomanization: Boolean = true,
    val ttsSpeed: Float = 0.95f,
    val onboarded: Boolean = false
)

@Entity(tableName = "vocab_cards")
data class VocabCard(
    @PrimaryKey val id: String,
    val korean: String,
    val romanization: String,
    val meaning: String,
    val exampleSentence: String,
    val exampleMeaning: String,
    val category: String, // Salam, Makanan, Keluarga, Angka, Kehidupan, Kustom
    val easeFactor: Float = 2.5f,
    val interval: Int = 1,
    val repetitions: Int = 0,
    val nextReview: Long = System.currentTimeMillis(),
    val isBookmarked: Boolean = false
)

@Entity(tableName = "lesson_progress")
data class LessonProgress(
    @PrimaryKey val id: String,
    val moduleId: String, // writing, speaking, listening, vocab, reading, video
    val lessonId: String,
    val title: String,
    val completed: Boolean = false,
    val score: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "activity_logs")
data class ActivityLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val activityType: String, // writing, speaking, listening, vocab, reading, video
    val durationMinutes: Int = 5,
    val xpEarned: Int = 10,
    val timestamp: Long = System.currentTimeMillis(),
    val dateString: String = "" // format YYYY-MM-DD
)

@Entity(tableName = "unlocked_badges")
data class UnlockedBadge(
    @PrimaryKey val badgeId: String,
    val title: String,
    val description: String,
    val iconName: String,
    val unlockedAt: Long = System.currentTimeMillis()
)

data class BadgeItem(
    val id: String,
    val title: String,
    val description: String,
    val iconSymbol: String,
    val requirementDescription: String,
    val isUnlocked: Boolean = false
)

data class DailyChallengeItem(
    val id: String,
    val title: String,
    val description: String,
    val targetCount: Int,
    val currentCount: Int,
    val xpReward: Int,
    val isCompleted: Boolean = false,
    val moduleId: String
)

data class HangulLetter(
    val character: String,
    val name: String,
    val romanization: String,
    val category: String, // Vokal Dasar, Konsonan Dasar, Vokal Ganda, Konsonan Ganda, Batchim
    val description: String,
    val strokeCount: Int,
    val strokeGuide: List<String>,
    val soundSample: String = character
)

data class IntroTemplate(
    val id: String,
    val level: Int,
    val title: String,
    val description: String,
    val templateKo: String,
    val templateId: String,
    val placeholders: List<TemplatePlaceholder>
)

data class TemplatePlaceholder(
    val key: String,
    val label: String,
    val defaultValue: String,
    val options: List<String> = emptyList(),
    val minLength: Int = 2,
    val maxLength: Int = 30
)

data class RolePlayStep(
    val id: Int,
    val hannieDialogueKo: String,
    val hannieDialogueId: String,
    val userOptions: List<RolePlayOption>,
    val hint: String
)

data class RolePlayOption(
    val textKo: String,
    val textId: String,
    val isCorrect: Boolean,
    val feedback: String
)

data class ListeningQuiz(
    val id: String,
    val question: String,
    val spokenKorean: String,
    val romanization: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String
)

data class ReadingStory(
    val id: String,
    val titleKo: String,
    val titleId: String,
    val level: String,
    val paragraphs: List<ReadingParagraph>,
    val questions: List<ReadingQuestion>
)

data class ReadingParagraph(
    val sentenceKo: String,
    val sentenceRo: String,
    val sentenceId: String,
    val keyWords: List<Pair<String, String>> = emptyList() // Korean to Indonesian
)

data class ReadingQuestion(
    val question: String,
    val options: List<String>,
    val correctIndex: Int
)

data class VideoImmersion(
    val id: String,
    val title: String,
    val sourceTitle: String,
    val durationText: String,
    val thumbnailColorHex: Long = 0xFF1E3A8A,
    val lines: List<SubtitleLine>
)

data class SubtitleLine(
    val startMs: Long,
    val endMs: Long,
    val korean: String,
    val romanization: String,
    val indonesian: String
)
