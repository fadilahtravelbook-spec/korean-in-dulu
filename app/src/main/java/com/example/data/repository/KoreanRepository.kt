package com.example.data.repository

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.model.ActivityLog
import com.example.data.model.BadgeItem
import com.example.data.model.DailyChallengeItem
import com.example.data.model.HangulLetter
import com.example.data.model.IntroTemplate
import com.example.data.model.LessonProgress
import com.example.data.model.ListeningQuiz
import com.example.data.model.ReadingParagraph
import com.example.data.model.ReadingQuestion
import com.example.data.model.ReadingStory
import com.example.data.model.RolePlayOption
import com.example.data.model.RolePlayStep
import com.example.data.model.SubtitleLine
import com.example.data.model.TemplatePlaceholder
import com.example.data.model.UnlockedBadge
import com.example.data.model.UserProfile
import com.example.data.model.VideoImmersion
import com.example.data.model.VocabCard
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

class KoreanRepository(context: Context) {
    private val db = AppDatabase.getDatabase(context)
    private val userDao = db.userDao()
    private val vocabDao = db.vocabDao()
    private val progressDao = db.progressDao()
    private val activityLogDao = db.activityLogDao()
    private val badgeDao = db.badgeDao()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            seedInitialDataIfNeeded()
        }
    }

    val userProfile: Flow<UserProfile?> = userDao.getUserProfile()
    val allVocab: Flow<List<VocabCard>> = vocabDao.getAllVocab()
    val bookmarkedVocab: Flow<List<VocabCard>> = vocabDao.getBookmarkedCards()
    val completedCount: Flow<Int> = progressDao.getCompletedLessonsCount()
    val unlockedBadges: Flow<List<UnlockedBadge>> = badgeDao.getUnlockedBadges()
    val recentActivityLogs: Flow<List<ActivityLog>> = activityLogDao.getRecentLogs()
    val totalLearningMinutes: Flow<Int> = activityLogDao.getTotalLearningMinutes()

    fun getVocabDueCount(): Flow<Int> {
        return vocabDao.getCardsDueCount(System.currentTimeMillis())
    }

    suspend fun completeOnboarding(name: String, level: Int) {
        userDao.completeOnboarding(name, level)
        // Unlock First Steps badge
        unlockBadge("badge_first_step", "Langkah Pertama", "Menyelesaikan Onboarding & Placement Test", "🎯")
    }

    suspend fun addXp(amount: Int) {
        userDao.addXp(amount)
        checkBadges()
    }

    suspend fun updateStreak(streak: Int) {
        userDao.updateStreak(streak)
        if (streak >= 3) {
            unlockBadge("badge_streak_3", "Semangat Membara", "Mencapai 3 hari streak belajar berturut-turut", "🔥")
        }
    }

    suspend fun toggleRomanization(show: Boolean) {
        userDao.toggleRomanization(show)
    }

    suspend fun updateTtsSpeed(speed: Float) {
        userDao.updateTtsSpeed(speed)
    }

    suspend fun updateLastAccessedLesson(lessonId: String) {
        userDao.updateLastAccessedLesson(lessonId)
    }

    suspend fun toggleBookmark(cardId: String) {
        vocabDao.toggleBookmark(cardId)
    }

    suspend fun addCustomVocabCard(korean: String, meaning: String, category: String = "Kustom") {
        val card = VocabCard(
            id = "custom_${System.currentTimeMillis()}",
            korean = korean,
            romanization = "",
            meaning = meaning,
            exampleSentence = "$korean 공부해요.",
            exampleMeaning = "Belajar $meaning.",
            category = category,
            nextReview = System.currentTimeMillis()
        )
        vocabDao.insertCard(card)
        addXp(15)
        unlockBadge("badge_vocab_saver", "Kolektor Kata", "Menyimpan kata baru ke daftar SRS", "📖")
    }

    suspend fun recordLessonProgress(moduleId: String, lessonId: String, title: String, score: Int, durationMinutes: Int = 5) {
        progressDao.recordProgress(
            LessonProgress(
                id = "${moduleId}_$lessonId",
                moduleId = moduleId,
                lessonId = lessonId,
                title = title,
                completed = true,
                score = score
            )
        )
        updateLastAccessedLesson("${moduleId}_$lessonId")

        // Record activity log
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dateStr = dateFormat.format(Date())
        activityLogDao.insertLog(
            ActivityLog(
                activityType = moduleId,
                durationMinutes = durationMinutes,
                xpEarned = score * 2,
                dateString = dateStr
            )
        )

        // Award XP
        addXp(score * 2)

        // Increment matching daily challenges
        val updatedChallenges = _dailyChallenges.value.map { ch ->
            if (ch.moduleId == moduleId && !ch.isCompleted) {
                val newCount = ch.currentCount + 1
                val completed = newCount >= ch.targetCount
                if (completed) {
                    addXp(ch.xpReward)
                }
                ch.copy(currentCount = newCount, isCompleted = completed)
            } else {
                ch
            }
        }
        _dailyChallenges.value = updatedChallenges

        // Check module specific badges
        when (moduleId) {
            "writing" -> unlockBadge("badge_hangul_writer", "Juru Tulis Hangul", "Menulis huruf Hangul dengan akurasi tinggi", "✍️")
            "speaking" -> unlockBadge("badge_native_speaker", "Lidah Seoul", "Menyelesaikan latihan rekaman speaking perkenalan", "🗣️")
            "listening" -> unlockBadge("badge_sharp_ear", "Telinga Tajam", "Berhasil menjawab quiz pemahaman listening", "🎧")
            "reading" -> unlockBadge("badge_bookworm", "Pembaca Handal", "Membaca teks cerita Korea dan menjawab quiz", "📚")
            "video" -> unlockBadge("badge_kdrama_fan", "K-Immersion", "Menyelesaikan skenario percakapan video interaktif", "🎬")
        }
    }

    // SM-2 Spaced Repetition Algorithm Implementation
    suspend fun submitSrsReview(card: VocabCard, rating: Int) {
        val q = rating + 2 // map 0..3 to 2..5 quality
        var newInterval: Int
        var newReps = card.repetitions
        var newEaseFactor = card.easeFactor + (0.1f - (5 - q) * (0.08f + (5 - q) * 0.02f))
        if (newEaseFactor < 1.3f) newEaseFactor = 1.3f

        if (q < 3) {
            newReps = 0
            newInterval = 1
        } else {
            newReps += 1
            newInterval = when (newReps) {
                1 -> 1
                2 -> 6
                else -> (card.interval * newEaseFactor).roundToInt()
            }
        }

        val oneDayMs = 24L * 60 * 60 * 1000
        val nextReview = System.currentTimeMillis() + (newInterval * oneDayMs)

        val updated = card.copy(
            easeFactor = newEaseFactor,
            interval = newInterval,
            repetitions = newReps,
            nextReview = nextReview
        )
        vocabDao.updateCard(updated)
        addXp(15)

        // Increment vocab daily challenge
        val updatedChallenges = _dailyChallenges.value.map { ch ->
            if (ch.moduleId == "vocab" && !ch.isCompleted) {
                val newCount = ch.currentCount + 1
                val completed = newCount >= ch.targetCount
                if (completed) {
                    addXp(ch.xpReward)
                }
                ch.copy(currentCount = newCount, isCompleted = completed)
            } else {
                ch
            }
        }
        _dailyChallenges.value = updatedChallenges

        // Check SRS master badge
        if (newReps >= 3) {
            unlockBadge("badge_srs_master", "Master Memori SM-2", "Menguasai kosakata hingga interval panjang", "🧠")
        }
    }

    private suspend fun checkBadges() {
        val profile = userDao.getUserProfileOnce() ?: return
        if (profile.xp >= 300) {
            unlockBadge("badge_xp_300", "Bintang Emas 300 XP", "Mengumpulkan lebih dari 300 XP belajar", "⭐")
        }
        if (profile.xp >= 600) {
            unlockBadge("badge_xp_600", "Juara 600 XP", "Mengumpulkan lebih dari 600 XP belajar", "🏆")
        }
    }

    private suspend fun unlockBadge(badgeId: String, title: String, description: String, icon: String) {
        val exists = badgeDao.isBadgeUnlocked(badgeId)
        if (exists == 0) {
            badgeDao.insertBadge(
                UnlockedBadge(
                    badgeId = badgeId,
                    title = title,
                    description = description,
                    iconName = icon
                )
            )
        }
    }

    private suspend fun seedInitialDataIfNeeded() {
        val currentProfile = userDao.getUserProfileOnce()
        if (currentProfile == null) {
            userDao.insertOrUpdate(
                UserProfile(
                    id = "default_user",
                    name = "Sahabat Korea",
                    email = "sahabat@koreanindulu.com",
                    level = 1,
                    dailyGoalMinutes = 15,
                    streak = 3,
                    xp = 280,
                    lastActiveDate = "2026-10-31",
                    showRomanization = true,
                    ttsSpeed = 0.95f,
                    onboarded = false
                )
            )

            // Seed initial badge
            unlockBadge("badge_welcome", "Selamat Datang", "Memulai perjalanan belajar di Korean In Dulu", "🐯")

            // Seed initial activity logs for chart
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val todayStr = dateFormat.format(Date())
            activityLogDao.insertLog(ActivityLog(activityType = "writing", durationMinutes = 10, xpEarned = 40, dateString = todayStr))
            activityLogDao.insertLog(ActivityLog(activityType = "vocab", durationMinutes = 5, xpEarned = 30, dateString = todayStr))

            // Seed rich vocabulary cards
            val initialCards = listOf(
                VocabCard("v1", "안녕하세요", "annyeonghaseyo", "Halo / Selamat pagi/siang/malam", "안녕하세요! 만나서 반갑습니다.", "Halo! Senang bertemu dengan Anda.", "Salam"),
                VocabCard("v2", "감사합니다", "gamsahamnida", "Terima kasih (formal)", "도와주셔서 감사합니다.", "Terima kasih sudah membantu.", "Salam"),
                VocabCard("v3", "죄송합니다", "joesonghamnida", "Maaf / Mohon maaf (formal)", "늦어서 죄송합니다.", "Mohon maaf saya terlambat.", "Salam"),
                VocabCard("v4", "물", "mul", "Air", "물 좀 주세요.", "Tolong beri saya air.", "Makanan & Minuman"),
                VocabCard("v5", "밥", "bap", "Nasi / Makanan", "밥 먹었어요?", "Sudah makan nasi?", "Makanan & Minuman"),
                VocabCard("v6", "맛있어요", "mas-isseoyo", "Enak / Lezat", "이 김치찌개는 정말 맛있어요!", "Kimchi jjigae ini sangat lezat!", "Makanan & Minuman"),
                VocabCard("v7", "선생님", "seonsaengnim", "Guru / Pengajar", "선생님, 질문이 있습니다.", "Guru, saya punya pertanyaan.", "Kehidupan"),
                VocabCard("v8", "친구", "chingu", "Teman / Sahabat", "우리는 좋은 친구입니다.", "Kita adalah teman baik.", "Kehidupan"),
                VocabCard("v9", "한국어", "hangugeo", "Bahasa Korea", "저는 한국어를 열심히 공부해요.", "Saya belajar bahasa Korea dengan giat.", "Kehidupan"),
                VocabCard("v10", "사랑해요", "saranghaeyo", "Saya mencintaimu / menyayangimu", "엄마, 아빠 사랑해요.", "Ibu, ayah, aku mencintai kalian.", "Salam"),
                VocabCard("v11", "하나, 둘, 셋", "hana, dul, set", "Satu, dua, tiga (Angka Asli Korea)", "하나, 둘, 셋, 김치!", "Satu, dua, tiga, kimchi!", "Angka"),
                VocabCard("v12", "일, 이, 삼", "il, i, sam", "Satu, dua, tiga (Angka Sino-Korea)", "일, 이, 삼, 사, 오!", "Satu, dua, tiga, empat, lima!", "Angka"),
                VocabCard("v13", "얼마예요?", "eolmayeyo?", "Berapa harganya?", "이거 얼마예요?", "Ini berapa harganya?", "Belanja"),
                VocabCard("v14", "저기요", "jeogiyo", "Permisi (memanggil pelayan)", "저기요, 주문할게요.", "Permisi, saya mau pesan.", "Kehidupan"),
                VocabCard("v15", "어디예요?", "eodiyeyo?", "Di mana letaknya?", "화장실이 어디예요?", "Toilet di mana letaknya?", "Arah & Tempat"),
                VocabCard("v16", "지하철", "jihacheol", "Kereta bawah tanah / MRT", "지하철을 타고 가요.", "Pergi naik kereta bawah tanah.", "Arah & Tempat"),
                VocabCard("v17", "괜찮아요", "gwaenchanayo", "Tidak apa-apa / Baik-baik saja", "괜찮아요, 걱정하지 마세요.", "Tidak apa-apa, jangan khawatir.", "Salam"),
                VocabCard("v18", "커피", "keopi", "Kopi", "따뜻한 커피 한 잔 주세요.", "Tolong beri saya secangkir kopi hangat.", "Makanan & Minuman"),
                VocabCard("v19", "지금", "jigeum", "Sekarang / Saat ini", "지금 몇 시예요?", "Sekarang jam berapa?", "Waktu"),
                VocabCard("v20", "오늘", "oneul", "Hari ini", "오늘 날씨가 정말 좋아요.", "Hari ini cuacanya sangat bagus.", "Waktu")
            )
            vocabDao.insertAll(initialCards)
        } else {
            val existing = vocabDao.getAllVocab().firstOrNull() ?: emptyList()
            if (existing.size < 18) {
                val moreCards = listOf(
                    VocabCard("v12", "일, 이, 삼", "il, i, sam", "Satu, dua, tiga (Angka Sino-Korea)", "일, 이, 삼, 사, 오!", "Satu, dua, tiga, empat, lima!", "Angka"),
                    VocabCard("v14", "저기요", "jeogiyo", "Permisi (memanggil pelayan)", "저기요, 주문할게요.", "Permisi, saya mau pesan.", "Kehidupan"),
                    VocabCard("v15", "어디예요?", "eodiyeyo?", "Di mana letaknya?", "화장실이 어디예요?", "Toilet di mana letaknya?", "Arah & Tempat"),
                    VocabCard("v16", "지하철", "jihacheol", "Kereta bawah tanah / MRT", "지하철을 타고 가요.", "Pergi naik kereta bawah tanah.", "Arah & Tempat"),
                    VocabCard("v17", "괜찮아요", "gwaenchanayo", "Tidak apa-apa / Baik-baik saja", "괜찮아요, 걱정하지 마세요.", "Tidak apa-apa, jangan khawatir.", "Salam"),
                    VocabCard("v18", "커피", "keopi", "Kopi", "따뜻한 커피 한 잔 주세요.", "Tolong beri saya secangkir kopi hangat.", "Makanan & Minuman"),
                    VocabCard("v19", "지금", "jigeum", "Sekarang / Saat ini", "지금 몇 시예요?", "Sekarang jam berapa?", "Waktu"),
                    VocabCard("v20", "오늘", "oneul", "Hari ini", "오늘 날씨가 정말 좋아요.", "Hari ini cuacanya sangat bagus.", "Waktu")
                )
                vocabDao.insertAll(moreCards)
            }
        }
    }

    // Master list of all defineable badges
    val masterBadges: List<BadgeItem> = listOf(
        BadgeItem("badge_welcome", "Selamat Datang", "Memulai langkah pertama di Korean In Dulu", "🐯", "Buka aplikasi pertama kali"),
        BadgeItem("badge_first_step", "Langkah Pertama", "Menyelesaikan Onboarding & Placement Test", "🎯", "Selesaikan Onboarding"),
        BadgeItem("badge_streak_3", "Semangat Membara", "Mencapai 3 hari streak belajar berturut-turut", "🔥", "Streak 3 hari"),
        BadgeItem("badge_hangul_writer", "Juru Tulis Hangul", "Menulis huruf Hangul dengan skor >80%", "✍️", "Selesaikan latihan menulis"),
        BadgeItem("badge_native_speaker", "Lidah Seoul", "Menyelesaikan rekaman speaking perkenalan", "🗣️", "Latihan modul speaking"),
        BadgeItem("badge_sharp_ear", "Telinga Tajam", "Berhasil menjawab quiz pemahaman listening", "🎧", "Selesaikan listening quiz"),
        BadgeItem("badge_srs_master", "Master Memori SM-2", "Menguasai kosakata hingga interval panjang", "🧠", "Ulangi flashcard 3 kali"),
        BadgeItem("badge_vocab_saver", "Kolektor Kata", "Menyimpan kata baru dari bacaan atau video", "📖", "Simpan kata ke SRS"),
        BadgeItem("badge_xp_300", "Bintang Emas 300 XP", "Mengumpulkan lebih dari 300 XP belajar", "⭐", "Kumpulkan 300 XP"),
        BadgeItem("badge_bookworm", "Pembaca Handal", "Membaca teks cerita Korea dan menjawab quiz", "📚", "Selesaikan bacaan"),
        BadgeItem("badge_kdrama_fan", "K-Immersion", "Menyelesaikan skenario percakapan video interaktif", "🎬", "Latihan video immersion")
    )

    // Daily challenges for gamification
    private val _dailyChallenges = MutableStateFlow(
        listOf(
            DailyChallengeItem("dc1", "Latihan Menulis 2 Huruf", "Tulis huruf vokal atau konsonan di canvas", 2, 0, 30, false, "writing"),
            DailyChallengeItem("dc2", "Ulas 3 Kartu Kosakata", "Selesaikan sesi review SRS hari ini", 3, 0, 25, false, "vocab"),
            DailyChallengeItem("dc3", "Coba Latihan Speaking", "Buat kalimat perkenalan diri dengan suaramu", 1, 0, 35, false, "speaking")
        )
    )
    val dailyChallenges: Flow<List<DailyChallengeItem>> = _dailyChallenges

    // Role-play conversation steps with Hannie
    val rolePlaySteps: List<RolePlayStep> = listOf(
        RolePlayStep(
            id = 1,
            hannieDialogueKo = "안녕하세요! 처음 뵙겠습니다. 이름이 뭐예요?",
            hannieDialogueId = "Halo! Senang bertemu pertama kali. Siapa nama Anda?",
            userOptions = listOf(
                RolePlayOption("안녕하세요! 제 이름은 파딜라입니다.", "Halo! Nama saya adalah Fadilah.", true, "Jawaban sangat sopan dan tepat!"),
                RolePlayOption("물 주세요.", "Tolong beri saya air.", false, "Kurang tepat, Hannie sedang menanyakan nama Anda."),
                RolePlayOption("안녕, 잘 가!", "Hai, selamat tinggal!", false, "Terlalu informal dan merupakan salam perpisahan.")
            ),
            hint = "Gunakan kalimat: 안녕하세요! 제 이름은... (Halo! Nama saya...)"
        ),
        RolePlayStep(
            id = 2,
            hannieDialogueKo = "어느 나라에서 오셨어요?",
            hannieDialogueId = "Dari negara mana Anda berasal?",
            userOptions = listOf(
                RolePlayOption("저는 인도네시아에서 왔습니다.", "Saya datang dari Indonesia.", true, "Tepat sekali! Menjelaskan asal negara dengan sopan."),
                RolePlayOption("이거 얼마예요?", "Berapa harganya?", false, "Ini kalimat bertanya harga di toko."),
                RolePlayOption("감사합니다.", "Terima kasih.", false, "Bukan jawaban tentang asal negara.")
            ),
            hint = "Gunakan kalimat: 저는 인도네시아에서 왔습니다."
        ),
        RolePlayStep(
            id = 3,
            hannieDialogueKo = "한국어 공부는 재미있어요?",
            hannieDialogueId = "Apakah belajar bahasa Korea menyenangkan?",
            userOptions = listOf(
                RolePlayOption("네, 정말 재미있고 흥미로워요!", "Ya, sangat menyenangkan dan menarik!", true, "Luar biasa! Semangat belajarmu terpancar."),
                RolePlayOption("아니요, 배고파요.", "Tidak, saya lapar.", false, "Hannie menanyakan keseruan belajar bahasa Korea."),
                RolePlayOption("죄송합니다.", "Mohon maaf.", false, "Jawaban kurang relevan dengan pertanyaan.")
            ),
            hint = "Katakan '네' (Ya) atau '재미있어요' (Menyenangkan)."
        )
    )

    // Datasets
    val hangulAlphabet: List<HangulLetter> = listOf(
        // Vokal Dasar
        HangulLetter("ㅏ", "A", "a", "Vokal Dasar", "Garis vertikal panjang dengan satu coretan ke kanan.", 2, listOf("1. Tarik garis vertikal dari atas ke bawah", "2. Tarik garis pendek horizontal ke kanan tepat di tengah")),
        HangulLetter("ㅑ", "Ya", "ya", "Vokal Dasar", "Vokal A dengan dua coretan pendek ke kanan.", 3, listOf("1. Tarik garis vertikal dari atas ke bawah", "2. Tarik garis pendek horizontal pertama", "3. Tarik garis pendek horizontal kedua")),
        HangulLetter("ㅓ", "Eo", "eo", "Vokal Dasar", "Garis vertikal dengan satu coretan horizontal ke kiri.", 2, listOf("1. Tarik garis pendek horizontal dari kiri ke kanan", "2. Tarik garis vertikal melintasi ujung kanan")),
        HangulLetter("ㅕ", "Yeo", "yeo", "Vokal Dasar", "Dua garis pendek horizontal ke kiri lalu garis vertikal.", 3, listOf("1. Garis horizontal atas", "2. Garis horizontal bawah", "3. Garis vertikal panjang")),
        HangulLetter("ㅗ", "O", "o", "Vokal Dasar", "Garis horizontal bawah dengan satu coretan pendek ke atas.", 2, listOf("1. Tarik garis pendek vertikal dari atas ke bawah", "2. Tarik garis horizontal panjang dari kiri ke kanan")),
        HangulLetter("ㅛ", "Yo", "yo", "Vokal Dasar", "Dua coretan pendek ke atas di atas garis horizontal.", 3, listOf("1. Garis vertikal pendek kiri", "2. Garis vertikal pendek kanan", "3. Garis horizontal panjang")),
        HangulLetter("ㅜ", "U", "u", "Vokal Dasar", "Garis horizontal dengan satu coretan pendek ke bawah.", 2, listOf("1. Tarik garis horizontal panjang", "2. Tarik garis pendek vertikal tepat di tengah ke bawah")),
        HangulLetter("ㅠ", "Yu", "yu", "Vokal Dasar", "Garis horizontal dengan dua coretan pendek ke bawah.", 3, listOf("1. Garis horizontal panjang", "2. Garis vertikal pendek kiri", "3. Garis vertikal pendek kanan")),
        HangulLetter("ㅡ", "Eu", "eu", "Vokal Dasar", "Garis horizontal datar mewakili tanah datar.", 1, listOf("1. Tarik garis horizontal dari kiri ke kanan secara halus")),
        HangulLetter("ㅣ", "I", "i", "Vokal Dasar", "Garis vertikal lurus mewakili manusia yang berdiri.", 1, listOf("1. Tarik garis vertikal lurus dari atas ke bawah")),

        // Konsonan Dasar
        HangulLetter("ㄱ", "Giyeok", "g/k", "Konsonan Dasar", "Bentuk siku: ke kanan lalu ke bawah.", 1, listOf("1. Dari kiri ke kanan, lalu langsung tekuk ke bawah tanpa putus")),
        HangulLetter("ㄴ", "Nieun", "n", "Konsonan Dasar", "Bentuk sudut siku bawah: vertikal turun lalu ke kanan.", 1, listOf("1. Dari atas turun lurus ke bawah, lalu tekuk ke kanan")),
        HangulLetter("ㄷ", "Digeut", "d/t", "Konsonan Dasar", "Bentuk C kotak: garis atas lalu siku bawah.", 2, listOf("1. Garis horizontal atas", "2. Dari awal garis atas, turun lalu tekuk ke kanan")),
        HangulLetter("ㄹ", "Rieul", "r/l", "Konsonan Dasar", "Bentuk ular / angka 2 bersudut.", 3, listOf("1. Garis siku (ㄱ)", "2. Garis horizontal tengah", "3. Garis siku bawah (ㄴ)")),
        HangulLetter("ㅁ", "Mieum", "m", "Konsonan Dasar", "Kotak tertutup mewakili bentuk mulut.", 3, listOf("1. Garis vertikal kiri", "2. Sudut atas ke kanan lalu turun", "3. Garis penutup bawah")),
        HangulLetter("ㅂ", "Bieup", "b/p", "Konsonan Dasar", "Wadah terbuka atas dengan dua tiang dan jembatan.", 4, listOf("1. Tiang kiri turun", "2. Tiang kanan turun", "3. Jembatan tengah", "4. Dasar penutup bawah")),
        HangulLetter("ㅅ", "Siot", "s", "Konsonan Dasar", "Bentuk atap rumah / dua cabang kaki.", 2, listOf("1. Garis miring ke kiri bawah", "2. Garis miring dari tengah cabang ke kanan bawah")),
        HangulLetter("ㅇ", "Ieung", "ng / silent", "Konsonan Dasar", "Lingkaran bulat sempurna.", 1, listOf("1. Buat lingkaran berlawanan arah jarum jam mulai dari posisi jam 12")),
        HangulLetter("ㅈ", "Jieut", "j", "Konsonan Dasar", "Atap datar dengan kaki bercabang dua.", 2, listOf("1. Garis horizontal atas lalu tekuk miring kiri", "2. Garis miring ke kanan dari pertemuan sudut")),
        HangulLetter("ㅎ", "Hieut", "h", "Konsonan Dasar", "Topi kecil di atas lingkaran ieung.", 3, listOf("1. Coretan pendek atas", "2. Garis horizontal tengah", "3. Lingkaran di bawah")),

        // Vokal Ganda
        HangulLetter("ㅐ", "Ae", "ae", "Vokal Ganda", "Kombinasi vokal ㅏ dan ㅣ.", 3, listOf("1. Tarik garis vertikal kiri", "2. Coretan pendek tengah", "3. Garis vertikal kanan")),
        HangulLetter("ㅔ", "E", "e", "Vokal Ganda", "Kombinasi vokal ㅓ dan ㅣ.", 3, listOf("1. Coretan horizontal kiri", "2. Garis vertikal tengah", "3. Garis vertikal kanan")),
        HangulLetter("ㅘ", "Wa", "wa", "Vokal Ganda", "Kombinasi vokal ㅗ dan ㅏ.", 4, listOf("1. Garis vertikal atas", "2. Garis horizontal bawah", "3. Garis vertikal kanan", "4. Coretan ke kanan")),
        HangulLetter("ㅝ", "Wo", "wo", "Vokal Ganda", "Kombinasi vokal ㅜ dan ㅓ.", 4, listOf("1. Garis horizontal atas", "2. Garis vertikal bawah", "3. Coretan horizontal", "4. Garis vertikal")),
        HangulLetter("ㅢ", "Ui", "ui", "Vokal Ganda", "Kombinasi vokal ㅡ dan ㅣ.", 2, listOf("1. Garis horizontal bawah", "2. Garis vertikal kanan")),

        // Konsonan Ganda & Batchim
        HangulLetter("ㄲ", "Ssang-giyeok", "kk", "Konsonan Ganda", "Dua huruf giyeok berdampingan.", 2, listOf("1. Tulis giyeok pertama (ㄱ)", "2. Tulis giyeok kedua (ㄱ) tepat di sampingnya")),
        HangulLetter("ㅆ", "Ssang-siot", "ss", "Konsonan Ganda", "Dua huruf siot berdampingan (suara S tegas).", 4, listOf("1. Siot pertama kiri", "2. Siot kedua kanan")),
        HangulLetter("ㄸ", "Ssang-digeut", "tt", "Konsonan Ganda", "Dua huruf digeut berdampingan.", 4, listOf("1. Digeut pertama kiri", "2. Digeut kedua kanan")),
        HangulLetter("ㅃ", "Ssang-bieup", "pp", "Konsonan Ganda", "Dua huruf bieup berdampingan.", 8, listOf("1. Bieup pertama kiri", "2. Bieup kedua kanan")),
        HangulLetter("ㅉ", "Ssang-jieut", "jj", "Konsonan Ganda", "Dua huruf jieut berdampingan.", 4, listOf("1. Jieut pertama kiri", "2. Jieut kedua kanan"))
    )

    val introTemplates: List<IntroTemplate> = listOf(
        IntroTemplate(
            id = "intro_level_1",
            level = 1,
            title = "Perkenalan Diri Dasar",
            description = "Cocok untuk pemula yang baru pertama kali menyapa dalam bahasa Korea.",
            templateKo = "안녕하세요! 제 이름은 {{nama}}입니다. 저는 {{asal}} 사람입니다. 만나서 반갑습니다!",
            templateId = "Halo! Nama saya adalah {{nama}}. Saya orang {{asal}}. Senang bertemu dengan Anda!",
            placeholders = listOf(
                TemplatePlaceholder("nama", "Nama Anda", "Fadilah"),
                TemplatePlaceholder("asal", "Negara / Kota Asal", "인도네시아 (Indonesia)", listOf("인도네시아 (Indonesia)", "자카르타 (Jakarta)", "반둥 (Bandung)", "수라바야 (Surabaya)"))
            )
        ),
        IntroTemplate(
            id = "intro_level_2",
            level = 2,
            title = "Perkenalan Profesi & Pekerjaan",
            description = "Menjelaskan identitas, pekerjaan, dan motivasi belajar bahasa Korea.",
            templateKo = "안녕하세요! 저는 {{nama}}라고 합니다. 제 직업은 {{pekerjaan}}입니다. 요즘 한국어를 열심히 공부하고 있습니다. 잘 부탁드립니다!",
            templateId = "Halo! Saya dipanggil {{nama}}. Pekerjaan saya adalah {{pekerjaan}}. Akhir-akhir ini saya belajar bahasa Korea dengan giat. Mohon bimbingannya!",
            placeholders = listOf(
                TemplatePlaceholder("nama", "Nama Anda", "Fadilah"),
                TemplatePlaceholder("pekerjaan", "Profesi", "학생 (Pelajar)", listOf("학생 (Pelajar)", "개발자 (Developer)", "회사원 (Karyawan)", "선생님 (Guru)"))
            )
        ),
        IntroTemplate(
            id = "intro_level_3",
            level = 3,
            title = "Perkenalan Lengkap & Hobi",
            description = "Format lengkap yang sering digunakan pada sesi wawancara atau pertukaran budaya.",
            templateKo = "반갑습니다. 저는 {{asal}}에서 온 {{nama}}입니다. 제 취미는 {{hobi}}입니다. 앞으로 한국에 가서 여행을 하고 싶습니다. 잘 부탁드립니다!",
            templateId = "Senang bertemu. Saya {{nama}} yang datang dari {{asal}}. Hobi saya adalah {{hobi}}. Kedepannya saya ingin pergi jalan-jalan ke Korea. Mohon kerja samanya!",
            placeholders = listOf(
                TemplatePlaceholder("nama", "Nama Anda", "Achmat"),
                TemplatePlaceholder("asal", "Asal", "인도네시아", listOf("인도네시아", "자카르타")),
                TemplatePlaceholder("hobi", "Hobi", "음악 듣기 (Mendengarkan Musik)", listOf("음악 듣기 (Mendengarkan Musik)", "영화 보기 (Nonton Film)", "요리하기 (Memasak)", "코딩하기 (Coding)"))
            )
        )
    )

    val listeningQuizzes: List<ListeningQuiz> = listOf(
        ListeningQuiz(
            id = "lq1",
            question = "Dengarkan audio berikut. Apa arti ungkapan tersebut?",
            spokenKorean = "안녕하세요? 만나서 반갑습니다.",
            romanization = "annyeonghaseyo? mannaseo bangapseumnida.",
            options = listOf(
                "Halo? Senang bertemu dengan Anda.",
                "Selamat jalan, sampai jumpa lagi.",
                "Berapa harga makanan ini?",
                "Tolong berikan saya secangkir air."
            ),
            correctIndex = 0,
            explanation = "안녕하세요 berarti 'Halo' dan 만나서 반갑습니다 berarti 'Senang bertemu denganmu'."
        ),
        ListeningQuiz(
            id = "lq2",
            question = "Dengarkan kalimat berikut. Pilihlah kata yang tepat diucapkan:",
            spokenKorean = "지금 몇 시예요?",
            romanization = "jigeum myeot siyeyo?",
            options = listOf(
                "Di mana letak stasiun?",
                "Sekarang jam berapa?",
                "Siapa nama Anda?",
                "Apa kabar hari ini?"
            ),
            correctIndex = 1,
            explanation = "지금 (sekarang) + 몇 시 (jam berapa) + 예요 (akhiran sopan)."
        ),
        ListeningQuiz(
            id = "lq3",
            question = "Dengarkan ucapan pemesanan di restoran berikut:",
            spokenKorean = "불고기 일인분하고 물 한 병 주세요.",
            romanization = "bulgogi il-inbun-hago mul han byeong juseyo.",
            options = listOf(
                "Tolong bon pembayarannya.",
                "Tolong satu porsi bulgogi dan sebotol air.",
                "Dua mangkok mie dingin dan jus.",
                "Apakah restoran ini buka sekarang?"
            ),
            correctIndex = 1,
            explanation = "불고기 일인분 (1 porsi bulgogi) + 하고 (dan) + 물 한 병 (sebotol air) + 주세요 (tolong beri)."
        ),
        ListeningQuiz(
            id = "lq4",
            question = "Dengarkan ungkapan perpisahan santun:",
            spokenKorean = "안녕히 계세요. 내일 또 만나요!",
            romanization = "annyeonghi gyeseyo. naeil tto mannayo!",
            options = listOf(
                "Selamat tinggal (kepada yang tetap tinggal). Besok jumpa lagi!",
                "Selamat jalan di perjalanan.",
                "Terima kasih atas jamuan makannya.",
                "Jangan lupa membawa payung."
            ),
            correctIndex = 0,
            explanation = "안녕히 계세요 diucapkan oleh orang yang pergi kepada tuan rumah/orang yang tetap di tempat."
        ),
        ListeningQuiz(
            id = "lq5",
            question = "Dengarkan pengumuman stasiun subway Seoul berikut:",
            spokenKorean = "이번 역은 홍대입구, 홍대입구역입니다. 내리실 문은 오른쪽입니다.",
            romanization = "ibeon yeogeun hongdae-ipgu, hongdae-ipguyeok-imnida. naerisil muneun oreunjjok-imnida.",
            options = listOf(
                "Stasiun ini Hongdae-ipgu, pintu keluar sebelah kanan.",
                "Kereta ini tidak berhenti di stasiun berikutnya.",
                "Harap berhati-hati saat melintasi rel.",
                "Pintu keluar stasiun ada di sebelah kiri."
            ),
            correctIndex = 0,
            explanation = "이번 역 (stasiun ini) + 홍대입구 (Hongdae) + 내리실 문 (pintu keluar) + 오른쪽 (sebelah kanan)."
        ),
        ListeningQuiz(
            id = "lq6",
            question = "Dengarkan pesanan di kafe kopi berikut:",
            spokenKorean = "따뜻한 카페라떼 한 잔과 치즈케이크 하나 포장해 주세요.",
            romanization = "ttatteut-han kape-ratte han jan-gwa chijeuke-ikeu hana pojanghae juseyo.",
            options = listOf(
                "Tolong satu latte dingin dan roti tawar.",
                "Tolong bungkus satu cafe latte hangat dan satu cheesecake.",
                "Apakah ada menu teh lemon dan es krim?",
                "Tolong berikan sendok dan sedotan."
            ),
            correctIndex = 1,
            explanation = "따뜻한 (hangat) + 카페라떼 한 잔 (1 gelas latte) + 치즈케이크 하나 (1 cheesecake) + 포장해 주세요 (tolong bungkus/takeaway)."
        )
    )

    val readingStories: List<ReadingStory> = listOf(
        ReadingStory(
            id = "story_1",
            titleKo = "서울에서의 첫날",
            titleId = "Hari Pertama di Seoul",
            level = "Pemula 1",
            paragraphs = listOf(
                ReadingParagraph(
                    sentenceKo = "오늘 저는 한국 서울에 도착했습니다.",
                    sentenceRo = "Oneul jeoneun hanguk seour-e dochakhaetseumnida.",
                    sentenceId = "Hari ini saya telah tiba di Seoul, Korea.",
                    keyWords = listOf("오늘" to "Hari ini", "서울" to "Seoul", "도착했습니다" to "Tiba/Sampai")
                ),
                ReadingParagraph(
                    sentenceKo = "날씨가 조금 춥지만 공기가 맑습니다.",
                    sentenceRo = "Nalssiga jogeum chupjiman gonggiga malkseumnida.",
                    sentenceId = "Cuacanya agak dingin tetapi udaranya segar/bersih.",
                    keyWords = listOf("날씨" to "Cuaca", "춥다" to "Dingin", "공기" to "Udara")
                ),
                ReadingParagraph(
                    sentenceKo = "식당에 가서 따뜻한 김치찌개를 먹었습니다.",
                    sentenceRo = "Sikdang-e gaseo ttatteut-han gimchijjigaereul meogeotseumnida.",
                    sentenceId = "Saya pergi ke restoran dan menyantap kimchi jjigae yang hangat.",
                    keyWords = listOf("식당" to "Restoran", "따뜻한" to "Hangat", "먹었습니다" to "Telah makan")
                ),
                ReadingParagraph(
                    sentenceKo = "한국 사람들은 매우 친절했습니다.",
                    sentenceRo = "Hanguk saramdeur-eun maeu chinjeol-haetseumnida.",
                    sentenceId = "Orang-orang Korea sangatlah ramah.",
                    keyWords = listOf("사람들" to "Orang-orang", "매우" to "Sangat", "친절하다" to "Ramah")
                )
            ),
            questions = listOf(
                ReadingQuestion("Di mana tokoh cerita tiba hari ini?", listOf("Tokyo", "Seoul", "Busan", "Jakarta"), 1),
                ReadingQuestion("Makanan apa yang dimakan oleh tokoh?", listOf("Bibimbap", "Tteokbokki", "Kimchi Jjigae", "Bulgogi"), 2),
                ReadingQuestion("Bagaimana sifat orang-orang Korea menurut cerita?", listOf("Galak", "Sangat Ramah (친절)", "Cuek", "Sibuk"), 1)
            )
        ),
        ReadingStory(
            id = "story_2",
            titleKo = "명동에서 즐거운 쇼핑",
            titleId = "Berbelanja Menyenangkan di Myeongdong",
            level = "Pemula 2",
            paragraphs = listOf(
                ReadingParagraph(
                    sentenceKo = "주말에 친구와 함께 명동에 갔습니다.",
                    sentenceRo = "Jumare chinguwa hamkke myeongdong-e gatseumnida.",
                    sentenceId = "Pada akhir pekan saya pergi ke Myeongdong bersama teman.",
                    keyWords = listOf("주말" to "Akhir pekan", "친구" to "Teman", "함께" to "Bersama")
                ),
                ReadingParagraph(
                    sentenceKo = "명동 거리에는 예쁜 옷과 맛있는 길거리 음식이 많았습니다.",
                    sentenceRo = "Myeongdong georie-neun yeppeun ot-gwa masinneun gilgeori eumsigi manhatseumnida.",
                    sentenceId = "Di jalanan Myeongdong terdapat banyak baju bagus dan jajanan pinggir jalan yang lezat.",
                    keyWords = listOf("옷" to "Baju/Pakaian", "길거리 음식" to "Jajanan jalanan", "많다" to "Banyak")
                ),
                ReadingParagraph(
                    sentenceKo = "우리는 따뜻한 떡볶이와 어묵을 사서 먹었습니다.",
                    sentenceRo = "Urineun ttatteut-han tteokbokkiwa eomugeul saseo meogeotseumnida.",
                    sentenceId = "Kami membeli dan menyantap tteokbokki hangat serta eomuk (fishcake).",
                    keyWords = listOf("떡볶이" to "Tteokbokki", "어묵" to "Fishcake", "사다" to "Membeli")
                ),
                ReadingParagraph(
                    sentenceKo = "정말 행복하고 기억에 남는 하루였습니다.",
                    sentenceRo = "Jeongmal haengbokhago gieoge namneun haruyeotseumnida.",
                    sentenceId = "Sungguh hari yang sangat membahagiakan dan berkesan.",
                    keyWords = listOf("행복하다" to "Bahagia", "하루" to "Satu hari", "기억" to "Kenangan")
                )
            ),
            questions = listOf(
                ReadingQuestion("Dengan siapa tokoh pergi ke Myeongdong?", listOf("Guru", "Teman (친구)", "Orang tua", "Sendirian"), 1),
                ReadingQuestion("Jajanan apa yang dibeli dan dimakan bersama?", listOf("Kimchi dan Ramyun", "Tteokbokki dan Eomuk (어묵)", "Kimbap dan Kopi", "Es Krim"), 1),
                ReadingQuestion("Bagaimana perasaan tokoh setelah jalan-jalan?", listOf("Bosan", "Sangat bahagia (행복하다)", "Lelah", "Sedih"), 1)
            )
        )
    )

    val videoLessons: List<VideoImmersion> = listOf(
        VideoImmersion(
            id = "video_1",
            title = "Percakapan Belanja di Convenience Store (편의점)",
            sourceTitle = "Drama Clip / Real Talk Scenario",
            durationText = "01:15",
            thumbnailColorHex = 0xFF1E3A8A,
            lines = listOf(
                SubtitleLine(0, 3000, "어서 오세요! GS25입니다.", "Eoseo oseyo! GS25-imnida.", "Selamat datang! Selamat datang di GS25."),
                SubtitleLine(3200, 6500, "안녕하세요. 삼각김밥 어디에 있어요?", "Annyeonghaseyo. Samgakgimbap eodie isseoyo?", "Halo. Di mana kimbap segitiga berada?"),
                SubtitleLine(6700, 10000, "신선식품 코너 안쪽에 있습니다.", "Sinseonsikpum koneo anjjog-e isseumnida.", "Ada di sebelah dalam konter makanan segar."),
                SubtitleLine(10200, 14000, "감사합니다! 봉투도 한 장 주세요.", "Gamsahamnida! Bongtudo han jang juseyo.", "Terima kasih! Tolong beri saya satu kantong plastik juga."),
                SubtitleLine(14200, 18000, "봉투 백 원 추가됩니다. 카드 결제 도와드릴게요.", "Bongtu baek won chugadoemnida. Kadeu gyeolje dowadeurilgeyo.", "Kantong tambah 100 won. Saya bantu pembayaran kartunya.")
            )
        ),
        VideoImmersion(
            id = "video_2",
            title = "Memesan Kopi di Cafe Hongdae (카페 주문)",
            sourceTitle = "K-Cafe Daily Scenario",
            durationText = "00:55",
            thumbnailColorHex = 0xFF0D9488,
            lines = listOf(
                SubtitleLine(0, 3000, "주문 도와드리겠습니다!", "Jumun dowadeurigesseumnida!", "Bisa saya bantu pesanannya!"),
                SubtitleLine(3200, 6500, "아이스 아메리카노 한 잔 부탁드려요.", "A-i-seu americano han jan butakdeuryeoyo.", "Tolong satu es americano ya."),
                SubtitleLine(6700, 9500, "드시고 가시나요, 아니면 포장인가요?", "Deusigo gasinayo, animyeon pojang-ingayo?", "Minum di sini atau dibungkus take-away?"),
                SubtitleLine(9700, 13000, "테이크아웃으로 해 주세요.", "Te-i-keu-a-us-euro hae juseyo.", "Tolong dibuat untuk take-away saja.")
            )
        )
    )

    val placementTestQuestions = listOf(
        PlacementQuestion(1, "Huruf Hangul 'ㅏ' dibaca sebagai suara apa?", listOf("U", "A", "O", "I"), 1),
        PlacementQuestion(2, "Manakah huruf konsonan Hangul yang menghasilkan bunyi 'M' (kotak tertutup)?", listOf("ㅁ", "ㅂ", "ㄱ", "ㄴ"), 0),
        PlacementQuestion(3, "Bagaimana penulisan suku kata 'Han' (seperti dalam Hangul)?", listOf("하", "한", "항", "합"), 1),
        PlacementQuestion(4, "Apa arti sapaan yang paling umum '안녕하세요' (annyeonghaseyo)?", listOf("Selamat makan", "Terima kasih banyak", "Halo / Selamat pagi/siang/malam", "Sampai jumpa besok"), 2),
        PlacementQuestion(5, "Manakah kata yang berarti 'Air' dalam bahasa Korea?", listOf("불 (Bul)", "물 (Mul)", "밥 (Bap)", "술 (Sul)"), 1),
        PlacementQuestion(6, "Lengkapilah kalimat perkenalan: '저는 학생_____' (Saya adalah seorang siswa).", listOf("있습니다", "아닙니다", "입니다", "합니다"), 2),
        PlacementQuestion(7, "Dalam sistem tata bahasa Korea, susunan kalimat yang benar adalah:", listOf("Subjek - Objek - Predikat (S-O-P)", "Subjek - Predikat - Objek (S-P-O)", "Predikat - Subjek - Objek (P-S-O)", "Objek - Predikat - Subjek (O-P-S)"), 0),
        PlacementQuestion(8, "Huruf 'ㅇ' jika berada di akhir suku kata (Batchim) dibaca sebagai:", listOf("H", "N", "NG", "M"), 2),
        PlacementQuestion(9, "Apa arti dari kalimat tanya '이거 얼마예요?' (Igeo eolmayeyo?)?", listOf("Ini di mana?", "Ini punya siapa?", "Ini apa?", "Ini berapa harganya?"), 3),
        PlacementQuestion(10, "Manakah ungkapan yang diucapkan untuk 'Terima kasih' secara sopan?", listOf("감사합니다", "죄송합니다", "실례합니다", "괜찮아요"), 0)
    )
}

data class PlacementQuestion(
    val id: Int,
    val question: String,
    val options: List<String>,
    val correctIndex: Int
)
