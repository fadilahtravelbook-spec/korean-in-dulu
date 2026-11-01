package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.ActivityLog
import com.example.data.model.UnlockedBadge
import com.example.data.model.UserProfile
import com.example.data.model.VocabCard
import com.example.data.repository.KoreanRepository
import com.example.util.TtsManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    val repository = KoreanRepository(application)
    val ttsManager = TtsManager(application)

    val userProfile: StateFlow<UserProfile?> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allVocab: StateFlow<List<VocabCard>> = repository.allVocab
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val completedCount: StateFlow<Int> = repository.completedCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val unlockedBadges: StateFlow<List<UnlockedBadge>> = repository.unlockedBadges
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentActivityLogs: StateFlow<List<ActivityLog>> = repository.recentActivityLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalLearningMinutes: StateFlow<Int> = repository.totalLearningMinutes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val dueCardsCount: StateFlow<Int> = repository.getVocabDueCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val dailyChallenges: StateFlow<List<com.example.data.model.DailyChallengeItem>> = repository.dailyChallenges
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun speak(text: String, speed: Float? = null) {
        val spd = speed ?: (userProfile.value?.ttsSpeed ?: 0.95f)
        ttsManager.speak(text, spd)
    }

    fun stopSpeaking() {
        ttsManager.stop()
    }

    fun completeOnboarding(name: String, level: Int, goalMinutes: Int = 15) {
        viewModelScope.launch {
            repository.completeOnboarding(name, level, goalMinutes)
        }
    }

    fun submitSrsReview(card: VocabCard, rating: Int) {
        viewModelScope.launch {
            repository.submitSrsReview(card, rating)
        }
    }

    fun toggleBookmark(cardId: String) {
        viewModelScope.launch {
            repository.toggleBookmark(cardId)
        }
    }

    fun toggleRomanization(show: Boolean) {
        viewModelScope.launch {
            repository.toggleRomanization(show)
        }
    }

    fun updateTtsSpeed(speed: Float) {
        viewModelScope.launch {
            repository.updateTtsSpeed(speed)
        }
    }

    fun addCustomVocabCard(korean: String, meaning: String, category: String = "Kustom") {
        viewModelScope.launch {
            repository.addCustomVocabCard(korean, meaning, category)
        }
    }

    fun addXp(amount: Int) {
        viewModelScope.launch {
            repository.addXp(amount)
        }
    }

    fun recordProgress(moduleId: String, lessonId: String, title: String, score: Int, durationMinutes: Int = 5) {
        viewModelScope.launch {
            repository.recordLessonProgress(moduleId, lessonId, title, score, durationMinutes)
        }
    }

    fun resetOnboarding() {
        viewModelScope.launch {
            val cur = userProfile.value ?: return@launch
            val db = com.example.data.local.AppDatabase.getDatabase(getApplication())
            db.userDao().insertOrUpdate(cur.copy(onboarded = false))
        }
    }

    override fun onCleared() {
        super.onCleared()
        ttsManager.shutdown()
    }
}
