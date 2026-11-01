package com.example.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import com.example.data.model.ActivityLog
import com.example.data.model.LessonProgress
import com.example.data.model.UnlockedBadge
import com.example.data.model.UserProfile
import com.example.data.model.VocabCard
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM user_profile WHERE id = 'default_user' LIMIT 1")
    fun getUserProfile(): Flow<UserProfile?>

    @Query("SELECT * FROM user_profile WHERE id = 'default_user' LIMIT 1")
    suspend fun getUserProfileOnce(): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(user: UserProfile)

    @Query("UPDATE user_profile SET xp = xp + :amount WHERE id = 'default_user'")
    suspend fun addXp(amount: Int)

    @Query("UPDATE user_profile SET streak = :newStreak WHERE id = 'default_user'")
    suspend fun updateStreak(newStreak: Int)

    @Query("UPDATE user_profile SET streak = :newStreak, lastActiveDate = :dateStr WHERE id = 'default_user'")
    suspend fun updateStreakAndDate(newStreak: Int, dateStr: String)

    @Query("UPDATE user_profile SET showRomanization = :show WHERE id = 'default_user'")
    suspend fun toggleRomanization(show: Boolean)

    @Query("UPDATE user_profile SET ttsSpeed = :speed WHERE id = 'default_user'")
    suspend fun updateTtsSpeed(speed: Float)

    @Query("UPDATE user_profile SET lastAccessedLessonId = :lessonId WHERE id = 'default_user'")
    suspend fun updateLastAccessedLesson(lessonId: String)

    @Query("UPDATE user_profile SET level = :newLevel WHERE id = 'default_user'")
    suspend fun updateLevel(newLevel: Int)

    @Query("UPDATE user_profile SET onboarded = 1, name = :name, level = :level, dailyGoalMinutes = :goalMinutes WHERE id = 'default_user'")
    suspend fun completeOnboarding(name: String, level: Int, goalMinutes: Int = 15)
}

@Dao
interface VocabDao {
    @Query("SELECT * FROM vocab_cards ORDER BY category, korean ASC")
    fun getAllVocab(): Flow<List<VocabCard>>

    @Query("SELECT * FROM vocab_cards WHERE category = :category")
    fun getVocabByCategory(category: String): Flow<List<VocabCard>>

    @Query("SELECT * FROM vocab_cards WHERE nextReview <= :currentTime ORDER BY nextReview ASC")
    fun getCardsDueForReview(currentTime: Long): Flow<List<VocabCard>>

    @Query("SELECT COUNT(*) FROM vocab_cards WHERE nextReview <= :currentTime")
    fun getCardsDueCount(currentTime: Long): Flow<Int>

    @Query("SELECT * FROM vocab_cards WHERE isBookmarked = 1")
    fun getBookmarkedCards(): Flow<List<VocabCard>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(cards: List<VocabCard>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCard(card: VocabCard)

    @Update
    suspend fun updateCard(card: VocabCard)

    @Query("UPDATE vocab_cards SET isBookmarked = NOT isBookmarked WHERE id = :cardId")
    suspend fun toggleBookmark(cardId: String)
}

@Dao
interface ProgressDao {
    @Query("SELECT * FROM lesson_progress")
    fun getAllProgress(): Flow<List<LessonProgress>>

    @Query("SELECT * FROM lesson_progress WHERE moduleId = :moduleId")
    fun getProgressByModule(moduleId: String): Flow<List<LessonProgress>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun recordProgress(progress: LessonProgress)

    @Query("SELECT COUNT(*) FROM lesson_progress WHERE completed = 1")
    fun getCompletedLessonsCount(): Flow<Int>
}

@Dao
interface ActivityLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: ActivityLog)

    @Query("SELECT * FROM activity_logs ORDER BY timestamp DESC LIMIT 30")
    fun getRecentLogs(): Flow<List<ActivityLog>>

    @Query("SELECT COALESCE(SUM(durationMinutes), 0) FROM activity_logs")
    fun getTotalLearningMinutes(): Flow<Int>
}

@Dao
interface BadgeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBadge(badge: UnlockedBadge)

    @Query("SELECT * FROM unlocked_badges")
    fun getUnlockedBadges(): Flow<List<UnlockedBadge>>

    @Query("SELECT COUNT(*) FROM unlocked_badges WHERE badgeId = :badgeId")
    suspend fun isBadgeUnlocked(badgeId: String): Int
}

@Database(
    entities = [UserProfile::class, VocabCard::class, LessonProgress::class, ActivityLog::class, UnlockedBadge::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun vocabDao(): VocabDao
    abstract fun progressDao(): ProgressDao
    abstract fun activityLogDao(): ActivityLogDao
    abstract fun badgeDao(): BadgeDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "korean_in_dulu.db"
                ).fallbackToDestructiveMigration(true).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
