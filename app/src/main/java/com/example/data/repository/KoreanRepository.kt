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

    suspend fun ensureInitialized() {
        seedInitialDataIfNeeded()
    }

    val userProfile: Flow<UserProfile?> = userDao.getUserProfile()
    val allVocab: Flow<List<VocabCard>> = vocabDao.getAllVocab()
    val bookmarkedVocab: Flow<List<VocabCard>> = vocabDao.getBookmarkedCards()
    val completedCount: Flow<Int> = progressDao.getCompletedLessonsCount()
    val unlockedBadges: Flow<List<UnlockedBadge>> = badgeDao.getUnlockedBadges()
    val recentActivityLogs: Flow<List<ActivityLog>> = activityLogDao.getRecentLogs()
    val totalLearningMinutes: Flow<Int> = activityLogDao.getTotalLearningMinutes()

    // Daily challenges for gamification
    private val _dailyChallenges = MutableStateFlow(
        listOf(
            DailyChallengeItem("dc1", "Latihan Menulis 2 Huruf", "Tulis huruf vokal atau konsonan di canvas", 2, 0, 30, false, "writing"),
            DailyChallengeItem("dc2", "Ulas 3 Kartu Kosakata", "Selesaikan sesi review SRS hari ini", 3, 0, 25, false, "vocab"),
            DailyChallengeItem("dc3", "Coba Latihan Speaking", "Buat kalimat perkenalan diri dengan suaramu", 1, 0, 35, false, "speaking")
        )
    )
    val dailyChallenges: Flow<List<DailyChallengeItem>> = _dailyChallenges

    fun getVocabDueCount(): Flow<Int> {
        return vocabDao.getCardsDueCount(System.currentTimeMillis())
    }

    suspend fun resetOnboarding() {
        val cur = userDao.getUserProfileOnce() ?: return
        userDao.insertOrUpdate(cur.copy(onboarded = false))
    }

    suspend fun completeOnboarding(name: String, level: Int, goalMinutes: Int = 15) {
        userDao.completeOnboarding(name, level, goalMinutes)
        // Unlock First Steps badge
        unlockBadge("badge_first_step", "Langkah Pertama", "Menyelesaikan Onboarding & Placement Test", "🎯")
    }

    suspend fun addXp(amount: Int) {
        userDao.addXp(amount)
        val profile = userDao.getUserProfileOnce()
        if (profile != null) {
            val newLevel = when {
                profile.xp >= 600 -> 3
                profile.xp >= 300 -> 2
                else -> 1
            }
            if (newLevel > profile.level) {
                userDao.updateLevel(newLevel)
                if (newLevel >= 2) {
                    unlockBadge("badge_level_up", "Naik Pangkat!", "Mencapai Level $newLevel dalam penguasaan bahasa Korea", "🌟")
                }
            }
        }
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

        // Update daily streak
        checkAndUpdateDailyStreak(dateStr)

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
            "writing", "keyboard_typing" -> unlockBadge("badge_hangul_writer", "Juru Tulis Hangul", "Menulis huruf Hangul dengan akurasi tinggi", "✍️")
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
                else -> maxOf(1, (card.interval * newEaseFactor).roundToInt())
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

    private suspend fun checkAndUpdateDailyStreak(todayDateStr: String) {
        val profile = userDao.getUserProfileOnce() ?: return
        val lastDate = profile.lastActiveDate
        if (lastDate != todayDateStr) {
            if (lastDate.isBlank()) {
                userDao.updateStreakAndDate(1, todayDateStr)
                return
            }
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            try {
                val lastDateTime = dateFormat.parse(lastDate)?.time ?: 0L
                val todayDateTime = dateFormat.parse(todayDateStr)?.time ?: 0L
                // Half-day buffer (12 hours) prevents daylight saving / timezone drift from truncating day difference
                val diffDays = (todayDateTime - lastDateTime + (12 * 60 * 60 * 1000L)) / (24 * 60 * 60 * 1000L)
                val newStreak = when (diffDays) {
                    1L -> profile.streak + 1
                    0L -> profile.streak
                    else -> 1
                }
                userDao.updateStreakAndDate(newStreak, todayDateStr)
                if (newStreak >= 3) {
                    unlockBadge("badge_streak_3", "Semangat Membara", "Mencapai 3 hari streak belajar berturut-turut", "🔥")
                }
            } catch (_: Exception) {
                userDao.updateStreakAndDate(profile.streak, todayDateStr)
            }
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

    // 20 Kosakata Dasar Fondasional (Salam, Kebutuhan Pokok, Angka Dasar)
    val initialCards: List<VocabCard> = listOf(
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

    // Koleksi Kosakata Ekstra: Restoran, Belanja, Kesehatan, Kerja, K-Culture, dan Arah
    val extraVocabCards: List<VocabCard> = listOf(
        // Kuliner & Restoran (음식 & 식당)
        VocabCard("v65", "비빔밥", "bibimbap", "Nasi Campur Sayur & Daging", "전주 비빔밥 한 그릇 주세요.", "Tolong satu mangkuk bibimbap Jeonju.", "Makanan & Minuman"),
        VocabCard("v66", "김치찌개", "kimchijjigae", "Sup Pedas Kimchi & Daging/Tahu", "얼큰한 김치찌개가 먹고 싶어요.", "Saya ingin makan sup kimchi pedas segar.", "Makanan & Minuman"),
        VocabCard("v67", "삼겹살", "samgyeopsal", "Daging Panggang BBQ Korea", "오늘 저녁에 삼겹살 먹으러 가요.", "Nanti malam ayo pergi makan samgyeopsal.", "Makanan & Minuman"),
        VocabCard("v68", "불고기", "bulgogi", "Daging Sapi Marinasi Manis Gurih", "불고기 2인분 주문할게요.", "Saya pesan bulgogi untuk 2 porsi.", "Makanan & Minuman"),
        VocabCard("v69", "떡볶이", "tteokbokki", "Kue Beras Saus Pedas Manis", "포장마차에서 떡볶이를 먹어요.", "Makan tteokbokki di warung tenda pojangmacha.", "Makanan & Minuman"),
        VocabCard("v70", "김밥", "gimbap", "Nasi Gulung Rumput Laut", "소풍 갈 때 김밥을 싸 가요.", "Membawa bekal gimbap saat piknik.", "Makanan & Minuman"),
        VocabCard("v71", "물냉면", "mulnaengmyeon", "Mie Dingin Berkuah Es Segar", "여름에는 시원한 물냉면이 최고예요.", "Saat musim panas, mie dingin kuah es adalah yang terbaik.", "Makanan & Minuman"),
        VocabCard("v72", "메뉴판", "menupan", "Buku / Lembar Menu", "여기 메뉴판 좀 보여주세요.", "Tolong perlihatkan buku menu di sini.", "Makanan & Minuman"),
        VocabCard("v73", "주문하다", "jumunhada", "Memesan Makanan / Minuman", "사장님, 주문할게요!", "Pemilik warung, saya mau pesan!", "Makanan & Minuman"),
        VocabCard("v74", "계산하다", "gyesanhada", "Membayar / Menghitung Tagihan", "따로따로 계산해 주세요.", "Tolong hitung bayar terpisah masing-masing.", "Makanan & Minuman"),
        VocabCard("v75", "숟가락", "sutgarak", "Sendok Makan", "숟가락 하나 더 주세요.", "Tolong minta satu sendok makan lagi.", "Makanan & Minuman"),
        VocabCard("v76", "젓가락", "jeotgarak", "Sumpit Makan", "젓가락을 떨어뜨렸어요.", "Sumpit makan saya terjatuh.", "Makanan & Minuman"),
        VocabCard("v77", "덜 맵게", "deol maepge", "Kurang Pedas / Jangan Terlalu Pedas", "찌개 덜 맵게 해 주세요.", "Tolong buat sup jjigae tidak terlalu pedas.", "Makanan & Minuman"),
        VocabCard("v78", "포장하다", "pojanghada", "Bungkus Bawa Pulang (Takeout)", "남은 음식 포장해 주실 수 있나요?", "Bisakah sisa makanan ini dibungkus bawa pulang?", "Makanan & Minuman"),
        VocabCard("v79", "반찬", "banchan", "Lauk Pauk Pendamping", "반찬 좀 더 리필해 주세요.", "Tolong isi ulang lauk pauk lagi sebentar.", "Makanan & Minuman"),

        // Belanja & Pasar (쇼핑 & 시장)
        VocabCard("v80", "얼마예요?", "eolmayeyo?", "Berapa Harganya?", "이 사과 한 바구니에 얼마예요?", "Satu keranjang apel ini berapa harganya?", "Belanja"),
        VocabCard("v81", "깎아주세요", "kkakkajuseyo", "Tolong Kasih Diskon / Murahan Dikit", "사장님, 조금만 깎아주세요!", "Bos, tolong kasih diskon sedikit dong!", "Belanja"),
        VocabCard("v82", "입어보다", "ibeoboda", "Mencoba Pakaian (Fitting)", "이 바지 한번 입어봐도 돼요?", "Bolehkah saya mencoba celana ini?", "Belanja"),
        VocabCard("v83", "사이즈", "saijeu", "Ukuran Baju / Sepatu", "더 큰 사이즈 있나요?", "Apakah ada ukuran yang lebih besar?", "Belanja"),
        VocabCard("v84", "신용카드", "sinyongkadeu", "Kartu Kredit / Debit", "신용카드로 결제할게요.", "Saya bayar menggunakan kartu kredit.", "Belanja"),
        VocabCard("v85", "현금", "hyeongeum", "Uang Tunai (Cash)", "현금으로 계산하면 할인돼요?", "Apakah ada diskon jika bayar tunai?", "Belanja"),
        VocabCard("v86", "환불하다", "hwanbulhada", "Minta Uang Kembali (Refund)", "영수증이 있으면 환불이 가능해요.", "Bisa refund jika memiliki struk belanja.", "Belanja"),
        VocabCard("v87", "교환하다", "gyohwanhada", "Menukar Barang", "다른 색상으로 교환하고 싶어요.", "Saya ingin menukar dengan warna lain.", "Belanja"),
        VocabCard("v88", "세일", "se-il", "Diskon / Potongan Harga", "전 품목 50% 세일 중이에요.", "Semua barang sedang diskon 50%.", "Belanja"),
        VocabCard("v89", "잘 어울려요", "jal eoullyeoyo", "Sangat Cocok / Pas Dipakai", "이 모자가 정말 잘 어울려요!", "Topi ini sungguh sangat cocok untukmu!", "Belanja"),
        VocabCard("v90", "옷가게", "otgage", "Toko Pakaian", "명동 옷가게에서 쇼핑했어요.", "Berbelanja di toko pakaian Myeongdong.", "Belanja"),
        VocabCard("v91", "신발", "sinbal", "Sepatu", "발이 편한 신발을 찾고 있어요.", "Saya mencari sepatu yang nyaman di kaki.", "Belanja"),

        // Kesehatan & Apotek (병원 & 약국)
        VocabCard("v92", "감기", "gamgi", "Flu / Masuk Angin", "감기에 걸려서 목이 아파요.", "Tenggorokan sakit karena terserang flu.", "Kesehatan & Apotek"),
        VocabCard("v93", "두통", "dutong", "Sakit Kepala / Pusing", "두통이 심해서 약을 먹었어요.", "Minum obat karena sakit kepala berat.", "Kesehatan & Apotek"),
        VocabCard("v94", "열이 나다", "yeori nada", "Demam / Badan Panas", "어젯밤부터 열이 펄펄 나요.", "Badan demam tinggi sejak kemarin malam.", "Kesehatan & Apotek"),
        VocabCard("v95", "약국", "yakguk", "Apotek / Toko Obat", "가장 가까운 약국이 어디예요?", "Di mana apotek yang paling dekat?", "Kesehatan & Apotek"),
        VocabCard("v96", "처방전", "cheobangjeon", "Resep Obat Dokter", "병원에서 처방전을 받았어요.", "Mendapatkan resep obat dari rumah sakit.", "Kesehatan & Apotek"),
        VocabCard("v97", "알약", "alyak", "Obat Tablet / Pil", "이 알약은 하루 세 번 식후에 드세요.", "Minum obat pil ini 3 kali sehari setelah makan.", "Kesehatan & Apotek"),
        VocabCard("v98", "반창고", "banchanggo", "Plester Luka", "손을 베여서 반창고를 붙였어요.", "Menempel plester luka karena jari tergores.", "Kesehatan & Apotek"),
        VocabCard("v99", "소화제", "sohwaje", "Obat Lambung / Pencernaan", "속이 더부룩해서 소화제를 샀어요.", "Membeli obat lambung karena perut kembung.", "Kesehatan & Apotek"),
        VocabCard("v100", "진통제", "jintongje", "Obat Pereda Nyeri", "진통제 먹고 푹 쉬세요.", "Minum obat pereda nyeri lalu istirahatlah.", "Kesehatan & Apotek"),
        VocabCard("v101", "마스크", "maseukeu", "Masker Pelindung", "미세먼지가 심하니 마스크를 쓰세요.", "Pakailah masker karena polusi debu pekat.", "Kesehatan & Apotek"),
        VocabCard("v102", "병원", "byeong-won", "Rumah Sakit / Klinik", "상태가 안 좋으면 병원에 가세요.", "Pergilah ke rumah sakit jika kondisi memburuk.", "Kesehatan & Apotek"),
        VocabCard("v103", "아프다", "apeuda", "Sakit / Nyeri", "배가 너무 아파서 견딜 수 없어요.", "Perut sangat sakit hingga tak tertahankan.", "Kesehatan & Apotek"),

        // Kerja, Kantor & Pabrik (직장 & 일터)
        VocabCard("v104", "출근하다", "chulgeunhada", "Berangkat Kerja", "매일 아침 8시 반에 출근해요.", "Berangkat kerja jam 8.30 setiap pagi.", "Kerja & Pabrik"),
        VocabCard("v105", "퇴근하다", "toegeunhada", "Pulang Kerja", "오늘 정시에 퇴근할 수 있어요.", "Hari ini bisa pulang kerja tepat waktu.", "Kerja & Pabrik"),
        VocabCard("v106", "야근하다", "yageunhada", "Kerja Lembur Malam", "프로젝트 마감이라 야근해요.", "Lembur karena tenggat waktu proyek.", "Kerja & Pabrik"),
        VocabCard("v107", "회의", "hoe-ui", "Rapat Pertemuan", "오후 두 시에 기획 회의가 있어요.", "Ada rapat perencanaan jam dua siang.", "Kerja & Pabrik"),
        VocabCard("v108", "동료", "dongnyo", "Rekan Kerja", "회사 동료들과 점심을 먹어요.", "Makan siang bersama rekan kerja kantor.", "Kerja & Pabrik"),
        VocabCard("v109", "반장님", "banjangnim", "Mandor / Supervisor", "반장님께서 안전 수칙을 알려주셨어요.", "Mandor memberi tahu aturan keselamatan kerja.", "Kerja & Pabrik"),
        VocabCard("v110", "월급", "wolgeup", "Gaji Bulanan", "매달 25일은 신나는 월급날이에요.", "Tanggal 25 setiap bulan adalah hari gajian yang menyenangkan.", "Kerja & Pabrik"),
        VocabCard("v111", "휴가", "hyuga", "Cuti Libur Kerja", "다음 주에 여름 휴가를 떠나요.", "Minggu depan berangkat cuti liburan musim panas.", "Kerja & Pabrik"),
        VocabCard("v112", "안전모", "anjeonmo", "Helm Pengaman Kerja", "공장 현장에서는 안전모를 꼭 쓰세요.", "Selalu kenakan helm pengaman di area pabrik.", "Kerja & Pabrik"),
        VocabCard("v113", "장갑", "janggap", "Sarung Tangan Kerja", "작업용 장갑을 착용하세요.", "Pakailah sarung tangan kerja.", "Kerja & Pabrik"),
        VocabCard("v114", "공장", "gongjang", "Pabrik / Kawasan Industri", "공장 기계를 점검하고 있어요.", "Sedang memeriksa mesin pabrik.", "Kerja & Pabrik"),
        VocabCard("v115", "조심하세요", "josimhaseyo", "Harap Berhati-hati", "바닥이 미끄러우니 조심하세요.", "Hati-hati karena lantainya licin.", "Kerja & Pabrik"),

        // K-Culture, Drama & Bahasa Gaul (K-컬처 & 신조어)
        VocabCard("v116", "심쿵", "simkung", "Bikin Deg-degan / Jantung Berdebar", "주인공 미소에 심쿵했어요!", "Tersenyum melihat senyum manis aktor utama!", "K-Pop & Gaul"),
        VocabCard("v117", "꿀잼", "kkuljaem", "Sangat Seru / Menarik Sekali", "이 한국 드라마 진짜 꿀잼이에요!", "Drama Korea ini sungguh seru banget!", "K-Pop & Gaul"),
        VocabCard("v118", "노잼", "nojaem", "Garing / Nggak Seru", "그 개그는 너무 노잼이었어요.", "Lelucon tadi garing banget.", "K-Pop & Gaul"),
        VocabCard("v119", "멘붕", "menbung", "Mental Rusak / Syok / Bengong", "지갑을 잃어버려서 완전 멘붕이에요.", "Benar-benar syok linglung karena dompet hilang.", "K-Pop & Gaul"),
        VocabCard("v120", "치맥", "chimaek", "Ayam Goreng & Minuman Segar", "불금에는 치맥이 최고예요!", "Di Jumat malam, ayam goreng dan minuman adalah yang terbaik!", "K-Pop & Gaul"),
        VocabCard("v121", "파이팅", "paiting", "Semangat! / Sukses Selalu!", "오늘 시험 잘 봐요, 파이팅!", "Kerjakan ujian dengan baik hari ini, semangat!", "K-Pop & Gaul"),
        VocabCard("v122", "인싸", "inssa", "Orang Gaul / Populer / Supel", "성격이 밝아서 완전 인싸예요.", "Kepribadiannya ceria jadi orangnya sangat supel.", "K-Pop & Gaul"),
        VocabCard("v123", "아싸", "assa", "Penyendiri / Introvert Mandiri", "혼자 책 읽는 아싸 생활이 좋아요.", "Saya suka hidup tenang membaca buku sendiri.", "K-Pop & Gaul"),
        VocabCard("v124", "최애", "choe-ae", "Bias / Idola Favorit Paling Utama", "케이팝 그룹에서 저의 최애는 지민이에요.", "Di grup K-Pop idola utama favorit saya adalah Jimin.", "K-Pop & Gaul"),
        VocabCard("v125", "본방사수", "bonbangsasu", "Nonton Tayangan Perdana Langsung", "오늘 밤 10시 드라마 본방사수할게요!", "Nanti malam jam 10 wajib nonton siaran langsung dramanya!", "K-Pop & Gaul"),
        VocabCard("v126", "극혐", "geukhyeom", "Sangat Tidak Suka / Ilfeel", "새치기하는 사람은 정말 극혐이에요.", "Orang yang menyerobot antrean sungguh bikin ilfeel.", "K-Pop & Gaul"),
        VocabCard("v127", "감동", "gamdong", "Terharu / Tersentuh Hati", "팬들의 편지에 큰 감동을 받았어요.", "Sangat terharu membaca surat-surat dari para penggemar.", "K-Pop & Gaul"),

        // Arah, Lokasi & Bandara (길 찾기 & 공항)
        VocabCard("v128", "직진하다", "jikjinhada", "Jalan Lurus Terus", "사거리에서 앞으로 직진하세요.", "Jalan lurus terus di perempatan jalan.", "Arah & Tempat"),
        VocabCard("v129", "오른쪽", "oreunjjok", "Sebelah Kanan", "약국은 편의점 오른쪽에 있어요.", "Apotek berada di sebelah kanan minimarket.", "Arah & Tempat"),
        VocabCard("v130", "왼쪽", "oenjjok", "Sebelah Kiri", "신호등에서 왼쪽으로 도세요.", "Beloklah ke kiri di lampu lalu lintas.", "Arah & Tempat"),
        VocabCard("v131", "건너편", "geonneopyeon", "Seberang Jalan", "길 건너편에 버스 정류장이 있어요.", "Di seberang jalan ada halte bus.", "Arah & Tempat"),
        VocabCard("v132", "지하철역", "jihacheolyeok", "Stasiun Kereta Bawah Tanah", "가장 가까운 지하철역이 어디예요?", "Di mana stasiun subway paling dekat?", "Arah & Tempat"),
        VocabCard("v133", "출구", "chulgu", "Pintu Keluar", "지하철 3번 출구로 나오시면 돼요.", "Anda cukup keluar dari pintu keluar nomor 3.", "Arah & Tempat"),
        VocabCard("v134", "인천공항", "incheongonghang", "Bandara Internasional Incheon", "공항철도를 타면 인천공항에 가요.", "Naik kereta bandara akan sampai di Bandara Incheon.", "Arah & Tempat"),
        VocabCard("v135", "탑승구", "tapseunggu", "Gerbang Keberangkatan (Gate)", "비행기 탑승구가 몇 번이에요?", "Berapa nomor gerbang keberangkatan pesawat?", "Arah & Tempat"),
        VocabCard("v136", "여권", "yeogwon", "Paspor Perjalanan", "입국 심사할 때 여권을 보여주세요.", "Tunjukkan paspor saat pemeriksaan imigrasi.", "Arah & Tempat"),
        VocabCard("v137", "지도", "jido", "Peta Lokasi", "스마트폰 지도를 보고 길을 찾아요.", "Mencari jalan dengan melihat peta di ponsel pintar.", "Arah & Tempat")
    )

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
            // Kosakata Keseharian: Toilet, Kamar Mandi, Sepeda, dan Transportasi
            val dailyLifeCards = listOf(
                VocabCard("v21", "화장실", "hwajangsil", "Toilet / Kamar Mandi", "화장실이 어디예요?", "Di mana letak toilet?", "Toilet & Mandi"),
                VocabCard("v22", "휴지", "hyuji", "Tisu Toilet", "화장실에 휴지가 없어요.", "Di toilet tidak ada tisu.", "Toilet & Mandi"),
                VocabCard("v23", "비누", "binu", "Sabun Cuci / Mandi", "비누로 손을 씻어요.", "Mencuci tangan dengan sabun.", "Toilet & Mandi"),
                VocabCard("v24", "칫솔", "chitsol", "Sikat Gigi", "칫솔과 치약 주세요.", "Tolong berikan sikat gigi dan pasta gigi.", "Toilet & Mandi"),
                VocabCard("v25", "치약", "chiyak", "Pasta Gigi", "치약이 상쾌해요.", "Pasta giginya menyegarkan.", "Toilet & Mandi"),
                VocabCard("v26", "수건", "sugeon", "Handuk", "깨끗한 수건 한 장 주세요.", "Tolong satu handuk yang bersih.", "Toilet & Mandi"),
                VocabCard("v27", "샴푸", "syampu", "Shampo", "샴푸로 머리를 감아요.", "Mencuci rambut / keramas dengan shampo.", "Toilet & Mandi"),
                VocabCard("v28", "샤워하다", "syawohada", "Mandi (Shower)", "따뜻한 물로 샤워해요.", "Mandi dengan air hangat.", "Toilet & Mandi"),
                VocabCard("v29", "세수하다", "sesuhada", "Cuci Muka", "아침에 일어나서 세수해요.", "Bangun di pagi hari lalu cuci muka.", "Toilet & Mandi"),
                VocabCard("v30", "양치하다", "yangchihada", "Menyikat Gigi", "식사 후 3분 안에 양치해요.", "Menyikat gigi dalam 3 menit setelah makan.", "Toilet & Mandi"),
                VocabCard("v31", "따뜻한 물", "ttatteut-han mul", "Air Hangat", "샤워실에 따뜻한 물이 잘 나와요.", "Air hangat di kamar mandi mengalir lancar.", "Toilet & Mandi"),
                VocabCard("v32", "비밀번호", "bimilbeonho", "Nomor Sandi / PIN", "카페 화장실 비밀번호가 뭐예요?", "Berapa nomor sandi toilet kafe?", "Toilet & Mandi"),
                VocabCard("v33", "자전거", "jajeongeo", "Sepeda", "날씨가 좋아서 자전거를 타요.", "Karena cuaca bagus, saya naik sepeda.", "Sepeda & Transportasi"),
                VocabCard("v34", "따릉이", "ttareung-i", "Sepeda Sewa Publik Seoul", "한강에서 따릉이를 빌렸어요.", "Menyewa sepeda Ttareungi di tepi sungai Han.", "Sepeda & Transportasi"),
                VocabCard("v35", "타다", "tada", "Naik / Mengendarai", "자전거를 타고 공원에 가요.", "Naik sepeda pergi ke taman.", "Sepeda & Transportasi"),
                VocabCard("v36", "내리다", "naerida", "Turun (dari kendaraan)", "다음 버스 정류장에서 내려요.", "Turun di halte bus berikutnya.", "Sepeda & Transportasi"),
                VocabCard("v37", "자전거 도로", "jajeongeo doro", "Jalur Khusus Sepeda", "자전거 도로로 안전하게 달리세요.", "Bersepedalah dengan aman di jalur sepeda.", "Sepeda & Transportasi"),
                VocabCard("v38", "헬멧", "helmet", "Helm Pengaman", "자전거 탈 때는 헬멧을 착용해요.", "Kenakan helm saat mengendarai sepeda.", "Sepeda & Transportasi"),
                VocabCard("v39", "버스 정류장", "beoseu jeongnyujang", "Halte Bus", "버스 정류장에서 5분 기다려요.", "Menunggu 5 menit di halte bus.", "Sepeda & Transportasi"),
                VocabCard("v40", "교통카드", "gyotongkadeu", "Kartu Transportasi (T-Money)", "교통카드를 단말기에 찍으세요.", "Tempelkan kartu transportasi ke mesin kartu.", "Sepeda & Transportasi"),
                VocabCard("v41", "충전하다", "chungjeonhada", "Mengisi Saldo / Top-up", "편의점에서 교통카드 만 원 충전해요.", "Isi saldo kartu transportasi 10.000 won di minimarket.", "Sepeda & Transportasi"),
                VocabCard("v42", "환승", "hwanseung", "Transit / Pindah Kendaraan", "30분 이내에 환승하면 무료예요.", "Transit gratis jika berpindah dalam 30 menit.", "Sepeda & Transportasi"),
                VocabCard("v43", "편의점", "pyeon-uijeom", "Minimarket (24 Jam)", "편의점에서 생수와 삼각김밥을 사요.", "Beli air mineral dan kimbap segitiga di minimarket.", "Keseharian & Rumah"),
                VocabCard("v44", "봉투", "bongtu", "Kantong Belanja", "봉투 필요하세요?", "Apakah Anda butuh kantong plastik?", "Keseharian & Rumah"),
                VocabCard("v45", "영수증", "yeongsujeung", "Struk Pembayaran", "영수증 버려 주세요.", "Tolong buang saja struknya.", "Keseharian & Rumah"),
                VocabCard("v46", "세탁기", "setakgi", "Mesin Cuci", "주말에 세탁기를 돌려요.", "Menjalankan mesin cuci di akhir pekan.", "Keseharian & Rumah"),
                VocabCard("v47", "냉장고", "naengjanggo", "Kulkas", "음식을 냉장고에 보관해요.", "Menyimpan makanan di dalam kulkas.", "Keseharian & Rumah"),
                VocabCard("v48", "분리수거", "bunrisugeo", "Pemilahan Sampah Daur Ulang", "플라스틱과 캔을 분리수거해요.", "Memilah sampah plastik dan kaleng daur ulang.", "Keseharian & Rumah"),
                // Emosi & Kesal
                VocabCard("v49", "짜증나다", "jjajeungnada", "Merasa Kesal / Menyebalkan", "차가 막혀서 진짜 짜증나요.", "Macetnya parah jadi benar-benar bikin kesal.", "Emosi & Kesal"),
                VocabCard("v50", "열받다", "yeolbatda", "Naik Darah / Bikin Emosi", "거짓말을 들어서 완전 열받아요.", "Mendengar kebohongan membuat saya naik darah sekali.", "Emosi & Kesal"),
                VocabCard("v51", "답답하다", "dapdaphada", "Nyesek / Bikin Frustrasi", "일이 잘 안 풀려서 답답해요.", "Urusan tidak lancar jadi bikin frustrasi.", "Emosi & Kesal"),
                VocabCard("v52", "속상하다", "soksanghada", "Sakit Hati / Kecewa", "친구와 다퉈서 마음이 속상해요.", "Bertengkar dengan teman membuat hati sedih.", "Emosi & Kesal"),
                VocabCard("v53", "어이없다", "eoieopda", "Nggak Habis Pikir / Speechless", "그 말을 들으니 정말 어이가 없어요.", "Mendengar hal itu membuat saya speechless.", "Emosi & Kesal"),
                VocabCard("v54", "대박", "daebak", "Gila / Luar Biasa / Hebat", "와, 대박! 시험에 합격했어요!", "Wah hebat! Lulus ujian!", "Emosi & Kesal"),
                VocabCard("v55", "헐", "heol", "Astaga / Kaget", "헐, 스마트폰을 집에 두고 왔어요.", "Astaga, ponsel tertinggal di rumah.", "Emosi & Kesal"),
                VocabCard("v56", "다행이다", "dahaeng-ida", "Untunglah / Syukurlah", "비가 그쳐서 정말 다행이에요.", "Hujan sudah reda jadi sungguh untunglah.", "Emosi & Kesal"),
                // Tegur Sapa
                VocabCard("v57", "식사하다", "siksahada", "Makan (Bentuk Sopan)", "점심 식사 맛있게 하셨어요?", "Apakah sudah makan siang dengan enak?", "Tegur Sapa"),
                VocabCard("v58", "수고하다", "sugohada", "Bekerja Keras / Jerih Payah", "오늘 하루도 수고하셨습니다.", "Terima kasih atas kerja kerasnya hari ini.", "Tegur Sapa"),
                VocabCard("v59", "오랜만", "oraenman", "Lama Tidak Berjumpa", "오랜만이에요! 잘 지냈어요?", "Lama tidak berjumpa! Kabarmu baik?", "Tegur Sapa"),
                VocabCard("v60", "어디 가다", "eodi gada", "Mau Pergi ke Mana", "어디 가시는 길이에요?", "Sedang dalam perjalanan ke mana?", "Tegur Sapa"),
                VocabCard("v61", "조심하다", "josimhada", "Berhati-hati", "눈길 조심해서 들어가세요.", "Hati-hati di jalan bersalju saat pulang.", "Tegur Sapa"),
                VocabCard("v62", "내일 봬요", "naeil bwaeyo", "Sampai Jumpa Besok (Sopan)", "오늘 먼저 퇴근할게요, 내일 봬요!", "Saya pulang kerja duluan ya, sampai jumpa besok!", "Tegur Sapa"),
                VocabCard("v63", "죄송하다", "joesonghada", "Mohon Maaf (Sopan)", "늦어서 정말 죄송합니다.", "Benar-benar mohon maaf karena terlambat.", "Tegur Sapa"),
                VocabCard("v64", "말씀", "malsseum", "Perkataan / Bicara (Halus)", "말씀 좀 여쭤봐도 될까요?", "Bolehkah saya bertanya sesuatu sebentar?", "Tegur Sapa")
            )

            // Koleksi Lengkap Angka Fundamental (Sino-Korea, Asli Korea, dan Satuan Hitung)
            val numberCards = listOf(
                // Angka Sino-Korea (한자어 숫자) - Untuk Uang, Tanggal, Menit, Nomor Telepon, Lantai
                VocabCard("num_s0", "영 / 공", "yeong / gong", "Nol (Sino-Korea)", "전화번호는 공일공...", "Nomor telepon adalah 010...", "Angka"),
                VocabCard("num_s1", "일", "il", "Satu (Sino-Korea)", "일층에 안내 데스크가 있어요.", "Di lantai satu ada meja informasi.", "Angka"),
                VocabCard("num_s2", "이", "i", "Dua (Sino-Korea)", "이층 카페로 올라가요.", "Naik ke kafe lantai dua.", "Angka"),
                VocabCard("num_s3", "삼", "sam", "Tiga (Sino-Korea)", "삼월에 새 학기가 시작해요.", "Di bulan Maret semester baru dimulai.", "Angka"),
                VocabCard("num_s4", "사", "sa", "Empat (Sino-Korea)", "지하철 사 번 출구로 나오세요.", "Keluarlah melalui pintu keluar nomor 4 stasiun subway.", "Angka"),
                VocabCard("num_s5", "오", "o", "Lima (Sino-Korea)", "오 분 뒤에 버스가 도착해요.", "Bus tiba lima menit lagi.", "Angka"),
                VocabCard("num_s6", "육", "yuk", "Enam (Sino-Korea)", "육월에는 날씨가 참 좋아요.", "Di bulan Juni cuacanya sangat bagus.", "Angka"),
                VocabCard("num_s7", "칠", "chil", "Tujuh (Sino-Korea)", "칠 번 버스를 타면 시청에 가요.", "Kalau naik bus nomor 7 akan sampai di balai kota.", "Angka"),
                VocabCard("num_s8", "팔", "pal", "Delapan (Sino-Korea)", "팔월에 부산 바다로 여행 가요.", "Di bulan Agustus pergi liburan ke laut Busan.", "Angka"),
                VocabCard("num_s9", "구", "gu", "Sembilan (Sino-Korea)", "구호선 급행 지하철을 탔어요.", "Naik subway jalur 9 ekspres.", "Angka"),
                VocabCard("num_s10", "십", "sip", "Sepuluh (Sino-Korea)", "십 분만 더 쉬고 공부해요.", "Istirahat sepuluh menit lagi lalu belajar.", "Angka"),
                VocabCard("num_s100", "백", "baek", "Seratus (Sino-Korea)", "백 점 만점을 받았어요!", "Mendapat nilai sempurna seratus!", "Angka"),
                VocabCard("num_s1000", "천", "cheon", "Seribu (Sino-Korea)", "생수 한 병에 천 원이에요.", "Satu botol air mineral harganya seribu won.", "Angka"),
                VocabCard("num_s10000", "만", "man", "Sepuluh Ribu (Sino-Korea)", "교통카드를 만 원 충전했어요.", "Top up kartu transportasi sepuluh ribu won.", "Angka"),
                VocabCard("num_s100000", "십만", "simman", "Seratus Ribu (Sino-Korea)", "신발 가격이 십만 원이에요.", "Harga sepatunya seratus ribu won.", "Angka"),
                VocabCard("num_s1000000", "백만", "baengman", "Satu Juta (Sino-Korea)", "서울 인구가 거의 천만 명이에요.", "Populasi kota Seoul hampir sepuluh juta orang.", "Angka"),

                // Angka Asli Korea (순우리말 숫자) - Untuk Menghitung Benda, Orang, Jam, Umur
                VocabCard("num_n1", "하나 / 한", "hana / han", "Satu (Angka Asli Korea)", "커피 한 잔 주세요.", "Tolong berikan satu cangkir kopi.", "Angka"),
                VocabCard("num_n2", "둘 / 두", "dul / du", "Dua (Angka Asli Korea)", "사과 두 개 샀어요.", "Saya membeli dua buah apel.", "Angka"),
                VocabCard("num_n3", "셋 / 세", "set / se", "Tiga (Angka Asli Korea)", "친구 세 명과 만났어요.", "Bertemu dengan tiga orang teman.", "Angka"),
                VocabCard("num_n4", "넷 / 네", "net / ne", "Empat (Angka Asli Korea)", "네 시에 영화가 시작해요.", "Filmnya mulai jam empat.", "Angka"),
                VocabCard("num_n5", "다섯", "daseot", "Lima (Angka Asli Korea)", "손가락이 다섯 개 있어요.", "Ada lima jari tangan.", "Angka"),
                VocabCard("num_n6", "여섯", "yeoseot", "Enam (Angka Asli Korea)", "오후 여섯 시에 퇴근해요.", "Pulang kerja jam enam sore.", "Angka"),
                VocabCard("num_n7", "일곱", "ilgop", "Tujuh (Angka Asli Korea)", "아침 일곱 시에 일어나요.", "Bangun tidur jam tujuh pagi.", "Angka"),
                VocabCard("num_n8", "여덟", "yeodeol", "Delapan (Angka Asli Korea)", "여덟 명 예약할게요.", "Saya ingin reservasi untuk delapan orang.", "Angka"),
                VocabCard("num_n9", "아홉", "ahop", "Sembilan (Angka Asli Korea)", "아홉 시에 지하철이 와요.", "Subway datang jam sembilan.", "Angka"),
                VocabCard("num_n10", "열", "yeol", "Sepuluh (Angka Asli Korea)", "열 개 포장해 주세요.", "Tolong bungkus sepuluh buah.", "Angka"),
                VocabCard("num_n20", "스물 / 스무", "seumul / seumu", "Dua Puluh (Angka Asli Korea)", "스무 살이 되었어요.", "Sudah berusia dua puluh tahun.", "Angka"),
                VocabCard("num_n30", "서른", "seoreun", "Tiga Puluh (Angka Asli Korea)", "서른 살 청년이에요.", "Pemuda berusia tiga puluh tahun.", "Angka"),

                // Satuan Hitung Fundamental (Classifiers / 단위 명사)
                VocabCard("num_c1", "개", "gae", "Satuan Buah / Benda Umum", "사과 한 개 주세요.", "Tolong satu buah apel.", "Angka"),
                VocabCard("num_c2", "명", "myeong", "Satuan Orang", "식당에 두 명 자리 있나요?", "Apakah ada meja untuk dua orang di restoran?", "Angka"),
                VocabCard("num_c3", "분", "bun", "Satuan Orang / Tamu (Bentuk Hormat)", "손님 세 분 오셨습니다.", "Tiga orang tamu telah tiba.", "Angka"),
                VocabCard("num_c4", "잔", "jan", "Satuan Cangkir / Gelas Minuman", "따뜻한 녹차 한 잔 마셔요.", "Minum secangkir teh hijau hangat.", "Angka"),
                VocabCard("num_c5", "병", "byeong", "Satuan Botol", "콜라 두 병 주세요.", "Tolong dua botol cola.", "Angka"),
                VocabCard("num_c6", "마리", "mari", "Satuan Ekor Hewan", "귀여운 강아지 한 마리가 있어요.", "Ada seekor anak anjing yang lucu.", "Angka"),
                VocabCard("num_c7", "권", "gwon", "Satuan Jilid Buku", "한국어 교재 두 권을 샀어요.", "Membeli dua jilid buku teks bahasa Korea.", "Angka"),
                VocabCard("num_c8", "장", "jang", "Satuan Lembar Kertas / Tiket", "지하철 티켓 한 장 주세요.", "Tolong selembar tiket subway.", "Angka"),
                VocabCard("num_c9", "시", "si", "Satuan Jam (Waktu)", "지금 몇 시예요? 세 시예요.", "Sekarang jam berapa? Jam tiga.", "Angka"),
                VocabCard("num_c10", "분", "bun", "Satuan Menit (Waktu)", "십오 분 후에 만나요.", "Ayo bertemu lima belas menit lagi.", "Angka"),
                VocabCard("num_c11", "원", "won", "Satuan Mata Uang Won Korea", "모두 만 오천 원입니다.", "Semuanya total lima belas ribu won.", "Angka"),
                VocabCard("num_c12", "층", "cheung", "Satuan Lantai Gedung", "우리 사무실은 삼층에 있어요.", "Kantor kami berada di lantai tiga.", "Angka")
            )

            vocabDao.insertAll(initialCards + dailyLifeCards + numberCards + extraVocabCards)
        } else {
            // Ensure daily life, number, and extra vocabulary cards are present in existing user profiles
            val dailyLifeCards = listOf(
                VocabCard("v21", "화장실", "hwajangsil", "Toilet / Kamar Mandi", "화장실이 어디예요?", "Di mana letak toilet?", "Toilet & Mandi"),
                VocabCard("v22", "휴지", "hyuji", "Tisu Toilet", "화장실에 휴지가 없어요.", "Di toilet tidak ada tisu.", "Toilet & Mandi"),
                VocabCard("v23", "비누", "binu", "Sabun Cuci / Mandi", "비누로 손을 씻어요.", "Mencuci tangan dengan sabun.", "Toilet & Mandi"),
                VocabCard("v24", "칫솔", "chitsol", "Sikat Gigi", "칫솔과 치약 주세요.", "Tolong berikan sikat gigi dan pasta gigi.", "Toilet & Mandi"),
                VocabCard("v25", "치약", "chiyak", "Pasta Gigi", "치약이 상쾌해요.", "Pasta giginya menyegarkan.", "Toilet & Mandi"),
                VocabCard("v26", "수건", "sugeon", "Handuk", "깨끗한 수건 한 장 주세요.", "Tolong satu handuk yang bersih.", "Toilet & Mandi"),
                VocabCard("v27", "샴푸", "syampu", "Shampo", "샴푸로 머리를 감아요.", "Mencuci rambut / keramas dengan shampo.", "Toilet & Mandi"),
                VocabCard("v28", "샤워하다", "syawohada", "Mandi (Shower)", "따뜻한 물로 샤워해요.", "Mandi dengan air hangat.", "Toilet & Mandi"),
                VocabCard("v29", "세수하다", "sesuhada", "Cuci Muka", "아침에 일어나서 세수해요.", "Bangun di pagi hari lalu cuci muka.", "Toilet & Mandi"),
                VocabCard("v30", "양치하다", "yangchihada", "Menyikat Gigi", "식사 후 3분 안에 양치해요.", "Menyikat gigi dalam 3 menit setelah makan.", "Toilet & Mandi"),
                VocabCard("v31", "따뜻한 물", "ttatteut-han mul", "Air Hangat", "샤워실에 따뜻한 물이 잘 나와요.", "Air hangat di kamar mandi mengalir lancar.", "Toilet & Mandi"),
                VocabCard("v32", "비밀번호", "bimilbeonho", "Nomor Sandi / PIN", "카페 화장실 비밀번호가 뭐예요?", "Berapa nomor sandi toilet kafe?", "Toilet & Mandi"),
                VocabCard("v33", "자전거", "jajeongeo", "Sepeda", "날씨가 좋아서 자전거를 타요.", "Karena cuaca bagus, saya naik sepeda.", "Sepeda & Transportasi"),
                VocabCard("v34", "따릉이", "ttareung-i", "Sepeda Sewa Publik Seoul", "한강에서 따릉이를 빌렸어요.", "Menyewa sepeda Ttareungi di tepi sungai Han.", "Sepeda & Transportasi"),
                VocabCard("v35", "타다", "tada", "Naik / Mengendarai", "자전거를 타고 공원에 가요.", "Naik sepeda pergi ke taman.", "Sepeda & Transportasi"),
                VocabCard("v36", "내리다", "naerida", "Turun (dari kendaraan)", "다음 버스 정류장에서 내려요.", "Turun di halte bus berikutnya.", "Sepeda & Transportasi"),
                VocabCard("v37", "자전거 도로", "jajeongeo doro", "Jalur Khusus Sepeda", "자전거 도로로 안전하게 달리세요.", "Bersepedalah dengan aman di jalur sepeda.", "Sepeda & Transportasi"),
                VocabCard("v38", "헬멧", "helmet", "Helm Pengaman", "자전거 탈 때는 헬멧을 착용해요.", "Kenakan helm saat mengendarai sepeda.", "Sepeda & Transportasi"),
                VocabCard("v39", "버스 정류장", "beoseu jeongnyujang", "Halte Bus", "버스 정류장에서 5분 기다려요.", "Menunggu 5 menit di halte bus.", "Sepeda & Transportasi"),
                VocabCard("v40", "교통카드", "gyotongkadeu", "Kartu Transportasi (T-Money)", "교통카드를 단말기에 찍으세요.", "Tempelkan kartu transportasi ke mesin kartu.", "Sepeda & Transportasi"),
                VocabCard("v41", "충전하다", "chungjeonhada", "Mengisi Saldo / Top-up", "편의점에서 교통카드 만 원 충전해요.", "Isi saldo kartu transportasi 10.000 won di minimarket.", "Sepeda & Transportasi"),
                VocabCard("v42", "환승", "hwanseung", "Transit / Pindah Kendaraan", "30분 이내에 환승하면 무료예요.", "Transit gratis jika berpindah dalam 30 menit.", "Sepeda & Transportasi"),
                VocabCard("v43", "편의점", "pyeon-uijeom", "Minimarket (24 Jam)", "편의점에서 생수와 삼각김밥을 사요.", "Beli air mineral dan kimbap segitiga di minimarket.", "Keseharian & Rumah"),
                VocabCard("v44", "봉투", "bongtu", "Kantong Belanja", "봉투 필요하세요?", "Apakah Anda butuh kantong plastik?", "Keseharian & Rumah"),
                VocabCard("v45", "영수증", "yeongsujeung", "Struk Pembayaran", "영수증 버려 주세요.", "Tolong buang saja struknya.", "Keseharian & Rumah"),
                VocabCard("v46", "세탁기", "setakgi", "Mesin Cuci", "주말에 세탁기를 돌려요.", "Menjalankan mesin cuci di akhir pekan.", "Keseharian & Rumah"),
                VocabCard("v47", "냉장고", "naengjanggo", "Kulkas", "음식을 냉장고에 보관해요.", "Menyimpan makanan di dalam kulkas.", "Keseharian & Rumah"),
                VocabCard("v48", "분리수거", "bunrisugeo", "Pemilahan Sampah Daur Ulang", "플라스틱과 캔을 분리수거해요.", "Memilah sampah plastik dan kaleng daur ulang.", "Keseharian & Rumah"),
                // Emosi & Kesal
                VocabCard("v49", "짜증나다", "jjajeungnada", "Merasa Kesal / Menyebalkan", "차가 막혀서 진짜 짜증나요.", "Macetnya parah jadi benar-benar bikin kesal.", "Emosi & Kesal"),
                VocabCard("v50", "열받다", "yeolbatda", "Naik Darah / Bikin Emosi", "거짓말을 들어서 완전 열받아요.", "Mendengar kebohongan membuat saya naik darah sekali.", "Emosi & Kesal"),
                VocabCard("v51", "답답하다", "dapdaphada", "Nyesek / Bikin Frustrasi", "일이 잘 안 풀려서 답답해요.", "Urusan tidak lancar jadi bikin frustrasi.", "Emosi & Kesal"),
                VocabCard("v52", "속상하다", "soksanghada", "Sakit Hati / Kecewa", "친구와 다퉈서 마음이 속상해요.", "Bertengkar dengan teman membuat hati sedih.", "Emosi & Kesal"),
                VocabCard("v53", "어이없다", "eoieopda", "Nggak Habis Pikir / Speechless", "그 말을 들으니 정말 어이가 없어요.", "Mendengar hal itu membuat saya speechless.", "Emosi & Kesal"),
                VocabCard("v54", "대박", "daebak", "Gila / Luar Biasa / Hebat", "와, 대박! 시험에 합격했어요!", "Wah hebat! Lulus ujian!", "Emosi & Kesal"),
                VocabCard("v55", "헐", "heol", "Astaga / Kaget", "헐, 스마트폰을 집에 두고 왔어요.", "Astaga, ponsel tertinggal di rumah.", "Emosi & Kesal"),
                VocabCard("v56", "다행이다", "dahaeng-ida", "Untunglah / Syukurlah", "비가 그쳐서 정말 다행이에요.", "Hujan sudah reda jadi sungguh untunglah.", "Emosi & Kesal"),
                // Tegur Sapa
                VocabCard("v57", "식사하다", "siksahada", "Makan (Bentuk Sopan)", "점심 식사 맛있게 하셨어요?", "Apakah sudah makan siang dengan enak?", "Tegur Sapa"),
                VocabCard("v58", "수고하다", "sugohada", "Bekerja Keras / Jerih Payah", "오늘 하루도 수고하셨습니다.", "Terima kasih atas kerja kerasnya hari ini.", "Tegur Sapa"),
                VocabCard("v59", "오랜만", "oraenman", "Lama Tidak Berjumpa", "오랜만이에요! 잘 지냈어요?", "Lama tidak berjumpa! Kabarmu baik?", "Tegur Sapa"),
                VocabCard("v60", "어디 가다", "eodi gada", "Mau Pergi ke Mana", "어디 가시는 길이에요?", "Sedang dalam perjalanan ke mana?", "Tegur Sapa"),
                VocabCard("v61", "조심하다", "josimhada", "Berhati-hati", "눈길 조심해서 들어가세요.", "Hati-hati di jalan bersalju saat pulang.", "Tegur Sapa"),
                VocabCard("v62", "내일 봬요", "naeil bwaeyo", "Sampai Jumpa Besok (Sopan)", "오늘 먼저 퇴근할게요, 내일 봬요!", "Saya pulang kerja duluan ya, sampai jumpa besok!", "Tegur Sapa"),
                VocabCard("v63", "죄송하다", "joesonghada", "Mohon Maaf (Sopan)", "늦어서 정말 죄송합니다.", "Benar-benar mohon maaf karena terlambat.", "Tegur Sapa"),
                VocabCard("v64", "말씀", "malsseum", "Perkataan / Bicara (Halus)", "말씀 좀 여쭤봐도 될까요?", "Bolehkah saya bertanya sesuatu sebentar?", "Tegur Sapa")
            )

            val numberCards = listOf(
                VocabCard("num_s0", "영 / 공", "yeong / gong", "Nol (Sino-Korea)", "전화번호는 공일공...", "Nomor telepon adalah 010...", "Angka"),
                VocabCard("num_s1", "일", "il", "Satu (Sino-Korea)", "일층에 안내 데스크가 있어요.", "Di lantai satu ada meja informasi.", "Angka"),
                VocabCard("num_s2", "이", "i", "Dua (Sino-Korea)", "이층 카페로 올라가요.", "Naik ke kafe lantai dua.", "Angka"),
                VocabCard("num_s3", "삼", "sam", "Tiga (Sino-Korea)", "삼월에 새 학기가 시작해요.", "Di bulan Maret semester baru dimulai.", "Angka"),
                VocabCard("num_s4", "사", "sa", "Empat (Sino-Korea)", "지하철 사 번 출구로 나오세요.", "Keluarlah melalui pintu keluar nomor 4 stasiun subway.", "Angka"),
                VocabCard("num_s5", "오", "o", "Lima (Sino-Korea)", "오 분 뒤에 버스가 도착해요.", "Bus tiba lima menit lagi.", "Angka"),
                VocabCard("num_s6", "육", "yuk", "Enam (Sino-Korea)", "육월에는 날씨가 참 좋아요.", "Di bulan Juni cuacanya sangat bagus.", "Angka"),
                VocabCard("num_s7", "칠", "chil", "Tujuh (Sino-Korea)", "칠 번 버스를 타면 시청에 가요.", "Kalau naik bus nomor 7 akan sampai di balai kota.", "Angka"),
                VocabCard("num_s8", "팔", "pal", "Delapan (Sino-Korea)", "팔월에 부산 바다로 여행 가요.", "Di bulan Agustus pergi liburan ke laut Busan.", "Angka"),
                VocabCard("num_s9", "구", "gu", "Sembilan (Sino-Korea)", "구호선 급행 지하철을 탔어요.", "Naik subway jalur 9 ekspres.", "Angka"),
                VocabCard("num_s10", "십", "sip", "Sepuluh (Sino-Korea)", "십 분만 더 쉬고 공부해요.", "Istirahat sepuluh menit lagi lalu belajar.", "Angka"),
                VocabCard("num_s100", "백", "baek", "Seratus (Sino-Korea)", "백 점 만점을 받았어요!", "Mendapat nilai sempurna seratus!", "Angka"),
                VocabCard("num_s1000", "천", "cheon", "Seribu (Sino-Korea)", "생수 한 병에 천 원이에요.", "Satu botol air mineral harganya seribu won.", "Angka"),
                VocabCard("num_s10000", "만", "man", "Sepuluh Ribu (Sino-Korea)", "교통카드를 만 원 충전했어요.", "Top up kartu transportasi sepuluh ribu won.", "Angka"),
                VocabCard("num_s100000", "십만", "simman", "Seratus Ribu (Sino-Korea)", "신발 가격이 십만 원이에요.", "Harga sepatunya seratus ribu won.", "Angka"),
                VocabCard("num_s1000000", "백만", "baengman", "Satu Juta (Sino-Korea)", "서울 인구가 거의 천만 명이에요.", "Populasi kota Seoul hampir sepuluh juta orang.", "Angka"),

                VocabCard("num_n1", "하나 / 한", "hana / han", "Satu (Angka Asli Korea)", "커피 한 잔 주세요.", "Tolong berikan satu cangkir kopi.", "Angka"),
                VocabCard("num_n2", "둘 / 두", "dul / du", "Dua (Angka Asli Korea)", "사과 두 개 샀어요.", "Saya membeli dua buah apel.", "Angka"),
                VocabCard("num_n3", "셋 / 세", "set / se", "Tiga (Angka Asli Korea)", "친구 세 명과 만났어요.", "Bertemu dengan tiga orang teman.", "Angka"),
                VocabCard("num_n4", "넷 / 네", "net / ne", "Empat (Angka Asli Korea)", "네 시에 영화가 시작해요.", "Filmnya mulai jam empat.", "Angka"),
                VocabCard("num_n5", "다섯", "daseot", "Lima (Angka Asli Korea)", "손가락이 다섯 개 있어요.", "Ada lima jari tangan.", "Angka"),
                VocabCard("num_n6", "여섯", "yeoseot", "Enam (Angka Asli Korea)", "오후 여섯 시에 퇴근해요.", "Pulang kerja jam enam sore.", "Angka"),
                VocabCard("num_n7", "일곱", "ilgop", "Tujuh (Angka Asli Korea)", "아침 일곱 시에 일어나요.", "Bangun tidur jam tujuh pagi.", "Angka"),
                VocabCard("num_n8", "여덟", "yeodeol", "Delapan (Angka Asli Korea)", "여덟 명 예약할게요.", "Saya ingin reservasi untuk delapan orang.", "Angka"),
                VocabCard("num_n9", "아홉", "ahop", "Sembilan (Angka Asli Korea)", "아홉 시에 지하철이 와요.", "Subway datang jam sembilan.", "Angka"),
                VocabCard("num_n10", "열", "yeol", "Sepuluh (Angka Asli Korea)", "열 개 포장해 주세요.", "Tolong bungkus sepuluh buah.", "Angka"),
                VocabCard("num_n20", "스물 / 스무", "seumul / seumu", "Dua Puluh (Angka Asli Korea)", "스무 살이 되었어요.", "Sudah berusia dua puluh tahun.", "Angka"),
                VocabCard("num_n30", "서른", "seoreun", "Tiga Puluh (Angka Asli Korea)", "서른 살 청년이에요.", "Pemuda berusia tiga puluh tahun.", "Angka"),

                VocabCard("num_c1", "개", "gae", "Satuan Buah / Benda Umum", "빵 한 개와 우유 한 병 주세요.", "Tolong satu buah roti dan satu botol susu.", "Angka"),
                VocabCard("num_c2", "명", "myeong", "Satuan Orang", "식당에 두 명 자리 있나요?", "Apakah ada meja untuk dua orang di restoran?", "Angka"),
                VocabCard("num_c3", "분", "bun", "Satuan Orang / Tamu (Bentuk Hormat)", "손님 세 분 오셨습니다.", "Tiga orang tamu telah tiba.", "Angka"),
                VocabCard("num_c4", "잔", "jan", "Satuan Cangkir / Gelas Minuman", "따뜻한 녹차 한 잔 마셔요.", "Minum secangkir teh hijau hangat.", "Angka"),
                VocabCard("num_c5", "병", "byeong", "Satuan Botol", "콜라 두 병 주세요.", "Tolong dua botol cola.", "Angka"),
                VocabCard("num_c6", "마리", "mari", "Satuan Ekor Hewan", "귀여운 강아지 한 마리가 있어요.", "Ada seekor anak anjing yang lucu.", "Angka"),
                VocabCard("num_c7", "권", "gwon", "Satuan Jilid Buku", "한국어 교재 두 권을 샀어요.", "Membeli dua jilid buku teks bahasa Korea.", "Angka"),
                VocabCard("num_c8", "장", "jang", "Satuan Lembar Kertas / Tiket", "지하철 티켓 한 장 주세요.", "Tolong selembar tiket subway.", "Angka"),
                VocabCard("num_c9", "시", "si", "Satuan Jam (Waktu)", "지금 몇 시예요? 세 시예요.", "Sekarang jam berapa? Jam tiga.", "Angka"),
                VocabCard("num_c10", "분", "bun", "Satuan Menit (Waktu)", "십오 분 후에 만나요.", "Ayo bertemu lima belas menit lagi.", "Angka"),
                VocabCard("num_c11", "원", "won", "Satuan Mata Uang Won Korea", "모두 만 오천 원입니다.", "Semuanya total lima belas ribu won.", "Angka"),
                VocabCard("num_c12", "층", "cheung", "Satuan Lantai Gedung", "우리 사무실은 삼층에 있어요.", "Kantor kami berada di lantai tiga.", "Angka")
            )

            vocabDao.insertAll(initialCards + dailyLifeCards + numberCards + extraVocabCards)
        }

        // Guarantee foundational starter cards are present even for existing database instances
        vocabDao.insertAll(initialCards)
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
        ),
        RolePlayStep(
            id = 4,
            hannieDialogueKo = "주문하신 음료 나왔습니다. 영수증 밑에 보시면 와이파이랑 화장실 정보가 있어요.",
            hannieDialogueId = "Minuman yang Anda pesan sudah jadi. Di bagian bawah struk belanja ada info Wi-Fi dan toilet.",
            userOptions = listOf(
                RolePlayOption("감사합니다! 그런데 화장실 비밀번호가 몇 번이에요?", "Terima kasih! Ngomong-ngomong berapa nomor sandi pintu toiletnya?", true, "Tepat sekali! Di kafe Korea kode toilet sering berupa nomor 4 digit."),
                RolePlayOption("물 주세요.", "Tolong beri saya air.", false, "Kurang relevan, Hannie sedang memberikan minuman dan info struk."),
                RolePlayOption("잘 가요.", "Selamat tinggal.", false, "Terlalu informal dan Anda baru saja menerima minuman.")
            ),
            hint = "Tanyakan nomor sandi toilet: 화장실 비밀번호가 몇 번이에요?"
        ),
        RolePlayStep(
            id = 5,
            hannieDialogueKo = "오늘 한강 날씨가 정말 화창해요! 우리 따릉이 자전거 빌려서 여의도 공원 한 바퀴 돌까요?",
            hannieDialogueId = "Hari ini cuaca di Sungai Han sangat cerah! Mau sewa sepeda Ttareungi dan keliling taman Yeouido?",
            userOptions = listOf(
                RolePlayOption("좋아요! 따릉이 앱으로 QR코드 찍고 헬멧 착용할게요.", "Bagus! Saya akan scan QR code dengan aplikasi Ttareungi dan pakai helm.", true, "Luar biasa! Ini kebiasaan bersepeda yang sangat aman dan populer di Seoul."),
                RolePlayOption("자전거 싫어요, 비행기 타요.", "Saya tidak suka sepeda, ayo naik pesawat.", false, "Jawaban kurang cocok untuk suasana santai di Sungai Han."),
                RolePlayOption("지금 몇 시예요?", "Sekarang jam berapa?", false, "Belum menjawab ajakan Hannie untuk bersepeda.")
            ),
            hint = "Setujui ajakan dan sebutkan sewa sepeda Ttareungi: 좋아요! 따릉이 빌려요."
        ),
        RolePlayStep(
            id = 6,
            hannieDialogueKo = "교통카드 잔액이 부족해서 버스를 탈 수 없네요. 어떡하죠?",
            hannieDialogueId = "Saldo kartu transportasi kurang jadi belum bisa naik bus. Bagaimana ya?",
            userOptions = listOf(
                RolePlayOption("저기 앞 편의점에서 교통카드 만 원 충전하고 와요!", "Ayo isi saldo kartu transportasi 10.000 won di minimarket depan sana!", true, "Solusi sempurna! Minimarket Korea melayani isi ulang T-Money kapan saja."),
                RolePlayOption("집에 걸어가세요.", "Silakan jalan kaki pulang ke rumah.", false, "Kurang ramah kepada teman seperjalanan."),
                RolePlayOption("배고파요.", "Saya lapar.", false, "Tidak menjawab kendala kartu transportasi.")
            ),
            hint = "Ajak isi ulang kartu di minimarket: 편의점에서 충전해요."
        ),
        RolePlayStep(
            id = 7,
            hannieDialogueKo = "갑자기 지하철이 고장 나서 30분이나 지연된대요! 밖에는 비까지 쏟아지는데... 진짜 어쩌죠?",
            hannieDialogueId = "Tiba-tiba subway mogok dan katanya tertunda sampai 30 menit! Di luar pun hujan deras... benar-benar gimana dong?",
            userOptions = listOf(
                RolePlayOption("아, 진짜 답답하네요! 그래도 근처 편의점에서 따뜻한 음료 마시며 기다려요.", "Ah, sungguh bikin nyesek/frustrasi! Tapi ayo minum minuman hangat di minimarket terdekat sambil menunggu.", true, "Respon sangat dewasa dan empatik! Mengungkapkan '답답하네요' secara wajar tanpa kehilangan ketenangan."),
                RolePlayOption("운전사한테 가서 소리 질러요!", "Pergi ke masinis lalu berteriak marah!", false, "Jangan agresif! Dalam budaya Korea, bersikap tenang saat kendala publik sangat diutamakan."),
                RolePlayOption("배고프니까 그냥 집에 가요.", "Karena lapar saya pulang saja ke rumah.", false, "Kurang menanggapi situasi subway dan tidak menawarkan jalan keluar bersama teman.")
            ),
            hint = "Ungkapkan rasa frustrasi santun lalu beri solusi: 진짜 답답하네요! 편의점에서 기다려요."
        ),
        RolePlayStep(
            id = 8,
            hannieDialogueKo = "어, 안녕하세요! 오늘 아침 일찍 외출하시네요. 식사는 맛있게 챙겨 드셨어요?",
            hannieDialogueId = "Eh, halo! Pagi-pagi begini sudah bepergian ya. Apakah sudah sarapan dengan enak?",
            userOptions = listOf(
                RolePlayOption("네, 좋은 아침이에요! 든든하게 먹었습니다. 하니 씨도 오늘도 수고하세요!", "Ya, selamat pagi! Saya makan kenyang. Hannie juga semangat dan terima kasih atas kerja kerasnya hari ini!", true, "Tegur sapa yang sempurna! Menjawab pertanyaan '식사하셨어요' dengan sopan dan mendoakan '수고하세요'."),
                RolePlayOption("밥 안 먹었어요. 밥 사주세요.", "Belum makan. Tolong traktir saya makan.", false, "Terlalu blak-blakan untuk tegur sapa sehari-hari dengan kenalan."),
                RolePlayOption("안녕히 주무세요.", "Selamat tidur.", false, "Ini ucapan selamat tidur di malam hari, bukan sapaan pagi.")
            ),
            hint = "Gunakan salam pagi dan jawab sapaan makan: 좋은 아침이에요! 든든하게 먹었어요."
        ),
        RolePlayStep(
            id = 9,
            hannieDialogueKo = "어서 오세요! 몇 분이세요? 편하신 안쪽 자리에 앉으세요.",
            hannieDialogueId = "Selamat datang! Untuk berapa orang? Silakan duduk di meja bagian dalam yang nyaman.",
            userOptions = listOf(
                RolePlayOption("두 명이에요. 여기 김치찌개랑 비빔밥 하나씩 주세요!", "Dua orang. Tolong berikan kimchi jjigae dan bibimbap masing-masing satu porsi di sini!", true, "Tepat sekali! Menyebutkan jumlah orang dan memesan menu dengan sopan."),
                RolePlayOption("저기요, 화장실 비밀번호 뭐예요?", "Permisi, nomor sandi toiletnya berapa?", false, "Belum menjawab jumlah orang dan belum memesan makanan."),
                RolePlayOption("집에 갈게요.", "Saya mau pulang ke rumah.", false, "Kurang tepat, Anda baru saja masuk ke restoran.")
            ),
            hint = "Sebutkan jumlah orang dan pesan makanan: 두 명이에요. 김치찌개 하나 주세요."
        ),
        RolePlayStep(
            id = 10,
            hannieDialogueKo = "식사는 입에 맞으셨어요? 반찬 더 필요하신 것 있으세요?",
            hannieDialogueId = "Apakah makanannya cocok di lidah Anda? Apakah ada lauk pauk (banchan) yang perlu ditambah lagi?",
            userOptions = listOf(
                RolePlayOption("정말 맛있어요! 여기 김치랑 물 좀 더 리필해 주세요.", "Sangat lezat! Tolong isi ulang kimchi dan air putihnya di sini.", true, "Luar biasa! Di restoran Korea, isi ulang banchan (lauk pendamping) dan air putih gratis."),
                RolePlayOption("이거 너무 비싸요, 환불해 주세요.", "Ini terlalu mahal, tolong refund uang saya.", false, "Kurang sopan dan tidak sesuai konteks restoran."),
                RolePlayOption("안녕히 계세요.", "Selamat tinggal.", false, "Anda masih sedang makan di meja.")
            ),
            hint = "Puji rasanya dan minta isi ulang lauk: 정말 맛있어요! 반찬 리필해 주세요."
        ),
        RolePlayStep(
            id = 11,
            hannieDialogueKo = "어디가 불편해서 오셨어요? 증상을 말씀해 주세요.",
            hannieDialogueId = "Bagian mana yang terasa tidak nyaman? Tolong ceritakan gejalanya kepada saya.",
            userOptions = listOf(
                RolePlayOption("어제부터 두통이 심하고 열이 조금 나요. 감기약 좀 주세요.", "Sejak kemarin sakit kepala parah dan agak demam. Tolong berikan obat flu.", true, "Penjelasan gejala yang sangat jelas dan tepat untuk apoteker di apotek Korea."),
                RolePlayOption("여기 따릉이 자전거 대여소예요?", "Apakah ini tempat sewa sepeda Ttareungi?", false, "Salah tempat, ini apotek bukan tempat sewa sepeda."),
                RolePlayOption("비빔밥 한 그릇 주세요.", "Tolong satu mangkuk bibimbap.", false, "Ini toko obat/apotek, bukan restoran.")
            ),
            hint = "Jelaskan rasa sakit dan minta obat flu: 두통이 심해요. 감기약 주세요."
        ),
        RolePlayStep(
            id = 12,
            hannieDialogueKo = "실례합니다. 길을 잃어버렸는데 혹시 홍대입구역 3번 출구가 어디인지 아세요?",
            hannieDialogueId = "Permisi. Saya tersesat, apakah Anda tahu di mana pintu keluar nomor 3 Stasiun Hongdae-ipgu?",
            userOptions = listOf(
                RolePlayOption("저기 신호등에서 왼쪽으로 도시면 바로 3번 출구가 보여요!", "Kalau Anda belok kiri di lampu lalu lintas depan sana, langsung terlihat pintu keluar 3!", true, "Petunjuk arah yang sangat ramah, jelas, dan akurat!"),
                RolePlayOption("저도 몰라요, 저리 가세요.", "Saya juga tidak tahu, pergilah sana.", false, "Kurang ramah kepada orang asing yang meminta tolong."),
                RolePlayOption("영수증 버려 주세요.", "Tolong buang struk belanja.", false, "Tidak relevan dengan pertanyaan arah jalan.")
            ),
            hint = "Berikan petunjuk arah belok kiri: 신호등에서 왼쪽으로 가세요."
        )
    )

    // Datasets
    val hangulAlphabet: List<HangulLetter> = listOf(
        // Vokal Dasar (Menggunakan awalan ㅇ agar pengucapan TTS bulat dan jernih)
        HangulLetter("ㅏ", "A", "a", "Vokal Dasar", "Garis vertikal panjang dengan satu coretan ke kanan.", 2, listOf("1. Tarik garis vertikal dari atas ke bawah", "2. Tarik garis pendek horizontal ke kanan tepat di tengah"), soundSample = "아"),
        HangulLetter("ㅑ", "Ya", "ya", "Vokal Dasar", "Vokal A dengan dua coretan pendek ke kanan.", 3, listOf("1. Tarik garis vertikal dari atas ke bawah", "2. Tarik garis pendek horizontal pertama", "3. Tarik garis pendek horizontal kedua"), soundSample = "야"),
        HangulLetter("ㅓ", "Eo", "eo", "Vokal Dasar", "Garis vertikal dengan satu coretan horizontal ke kiri.", 2, listOf("1. Tarik garis pendek horizontal dari kiri ke kanan", "2. Tarik garis vertikal melintasi ujung kanan"), soundSample = "어"),
        HangulLetter("ㅕ", "Yeo", "yeo", "Vokal Dasar", "Dua garis pendek horizontal ke kiri lalu garis vertikal.", 3, listOf("1. Garis horizontal atas", "2. Garis horizontal bawah", "3. Garis vertikal panjang"), soundSample = "여"),
        HangulLetter("ㅗ", "O", "o", "Vokal Dasar", "Garis horizontal bawah dengan satu coretan pendek ke atas.", 2, listOf("1. Tarik garis pendek vertikal dari atas ke bawah", "2. Tarik garis horizontal panjang dari kiri ke kanan"), soundSample = "오"),
        HangulLetter("ㅛ", "Yo", "yo", "Vokal Dasar", "Dua coretan pendek ke atas di atas garis horizontal.", 3, listOf("1. Garis vertikal pendek kiri", "2. Garis vertikal pendek kanan", "3. Garis horizontal panjang"), soundSample = "요"),
        HangulLetter("ㅜ", "U", "u", "Vokal Dasar", "Garis horizontal dengan satu coretan pendek ke bawah.", 2, listOf("1. Tarik garis horizontal panjang", "2. Tarik garis pendek vertikal tepat di tengah ke bawah"), soundSample = "우"),
        HangulLetter("ㅠ", "Yu", "yu", "Vokal Dasar", "Garis horizontal dengan dua coretan pendek ke bawah.", 3, listOf("1. Garis horizontal panjang", "2. Garis vertikal pendek kiri", "3. Garis vertikal pendek kanan"), soundSample = "유"),
        HangulLetter("ㅡ", "Eu", "eu", "Vokal Dasar", "Garis horizontal datar mewakili tanah datar.", 1, listOf("1. Tarik garis horizontal dari kiri ke kanan secara halus"), soundSample = "으"),
        HangulLetter("ㅣ", "I", "i", "Vokal Dasar", "Garis vertikal lurus mewakili manusia yang berdiri.", 1, listOf("1. Tarik garis vertikal lurus dari atas ke bawah"), soundSample = "이"),

        // Konsonan Dasar
        HangulLetter("ㄱ", "Giyeok", "g/k", "Konsonan Dasar", "Bentuk siku: ke kanan lalu ke bawah.", 1, listOf("1. Dari kiri ke kanan, lalu langsung tekuk ke bawah tanpa putus"), soundSample = "기역"),
        HangulLetter("ㄴ", "Nieun", "n", "Konsonan Dasar", "Bentuk sudut siku bawah: vertikal turun lalu ke kanan.", 1, listOf("1. Dari atas turun lurus ke bawah, lalu tekuk ke kanan"), soundSample = "니은"),
        HangulLetter("ㄷ", "Digeut", "d/t", "Konsonan Dasar", "Bentuk C kotak: garis atas lalu siku bawah.", 2, listOf("1. Garis horizontal atas", "2. Dari awal garis atas, turun lalu tekuk ke kanan"), soundSample = "디귿"),
        HangulLetter("ㄹ", "Rieul", "r/l", "Konsonan Dasar", "Bentuk ular / angka 2 bersudut.", 3, listOf("1. Garis siku (ㄱ)", "2. Garis horizontal tengah", "3. Garis siku bawah (ㄴ)"), soundSample = "리을"),
        HangulLetter("ㅁ", "Mieum", "m", "Konsonan Dasar", "Kotak tertutup mewakili bentuk mulut.", 3, listOf("1. Garis vertikal kiri", "2. Sudut atas ke kanan lalu turun", "3. Garis penutup bawah"), soundSample = "미음"),
        HangulLetter("ㅂ", "Bieup", "b/p", "Konsonan Dasar", "Wadah terbuka atas dengan dua tiang dan jembatan.", 4, listOf("1. Tiang kiri turun", "2. Tiang kanan turun", "3. Jembatan tengah", "4. Dasar penutup bawah"), soundSample = "비읍"),
        HangulLetter("ㅅ", "Siot", "s", "Konsonan Dasar", "Bentuk atap rumah / dua cabang kaki.", 2, listOf("1. Garis miring ke kiri bawah", "2. Garis miring dari tengah cabang ke kanan bawah"), soundSample = "시옷"),
        HangulLetter("ㅇ", "Ieung", "ng / silent", "Konsonan Dasar", "Lingkaran bulat sempurna.", 1, listOf("1. Buat lingkaran berlawanan arah jarum jam mulai dari posisi jam 12"), soundSample = "이응"),
        HangulLetter("ㅈ", "Jieut", "j", "Konsonan Dasar", "Atap datar dengan kaki bercabang dua.", 2, listOf("1. Garis horizontal atas lalu tekuk miring kiri", "2. Garis miring ke kanan dari pertemuan sudut"), soundSample = "지읒"),
        HangulLetter("ㅎ", "Hieut", "h", "Konsonan Dasar", "Topi kecil di atas lingkaran ieung.", 3, listOf("1. Coretan pendek atas", "2. Garis horizontal tengah", "3. Lingkaran di bawah"), soundSample = "히읗"),

        // Vokal Ganda
        HangulLetter("ㅐ", "Ae", "ae", "Vokal Ganda", "Kombinasi vokal ㅏ dan ㅣ.", 3, listOf("1. Tarik garis vertikal kiri", "2. Coretan pendek tengah", "3. Garis vertikal kanan"), soundSample = "애"),
        HangulLetter("ㅔ", "E", "e", "Vokal Ganda", "Kombinasi vokal ㅓ dan ㅣ.", 3, listOf("1. Coretan horizontal kiri", "2. Garis vertikal tengah", "3. Garis vertikal kanan"), soundSample = "에"),
        HangulLetter("ㅘ", "Wa", "wa", "Vokal Ganda", "Kombinasi vokal ㅗ dan ㅏ.", 4, listOf("1. Garis vertikal atas", "2. Garis horizontal bawah", "3. Garis vertikal kanan", "4. Coretan ke kanan"), soundSample = "와"),
        HangulLetter("ㅝ", "Wo", "wo", "Vokal Ganda", "Kombinasi vokal ㅜ dan ㅓ.", 4, listOf("1. Garis horizontal atas", "2. Garis vertikal bawah", "3. Coretan horizontal", "4. Garis vertikal"), soundSample = "워"),
        HangulLetter("ㅢ", "Ui", "ui", "Vokal Ganda", "Kombinasi vokal ㅡ dan ㅣ.", 2, listOf("1. Garis horizontal bawah", "2. Garis vertikal kanan"), soundSample = "의"),

        // Konsonan Ganda & Batchim
        HangulLetter("ㄲ", "Ssang-giyeok", "kk", "Konsonan Ganda", "Dua huruf giyeok berdampingan.", 2, listOf("1. Tulis giyeok pertama (ㄱ)", "2. Tulis giyeok kedua (ㄱ) tepat di sampingnya"), soundSample = "쌍기역"),
        HangulLetter("ㅆ", "Ssang-siot", "ss", "Konsonan Ganda", "Dua huruf siot berdampingan (suara S tegas).", 4, listOf("1. Siot pertama kiri", "2. Siot kedua kanan"), soundSample = "쌍시옷"),
        HangulLetter("ㄸ", "Ssang-digeut", "tt", "Konsonan Ganda", "Dua huruf digeut berdampingan.", 4, listOf("1. Digeut pertama kiri", "2. Digeut kedua kanan"), soundSample = "쌍디귿"),
        HangulLetter("ㅃ", "Ssang-bieup", "pp", "Konsonan Ganda", "Dua huruf bieup berdampingan.", 8, listOf("1. Bieup pertama kiri", "2. Bieup kedua kanan"), soundSample = "쌍비읍"),
        HangulLetter("ㅉ", "Ssang-jieut", "jj", "Konsonan Ganda", "Dua huruf jieut berdampingan.", 4, listOf("1. Jieut pertama kiri", "2. Jieut kedua kanan"), soundSample = "쌍지읒")
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
        ),
        IntroTemplate(
            id = "intro_level_4",
            level = 2,
            title = "Rutinitas Pagi & Kamar Mandi (아침 루틴)",
            description = "Menceritakan kebiasaan pagi di kamar mandi, sarapan, dan persiapan memulai hari.",
            templateKo = "저는 아침 {{jam}}시에 일어납니다. 화장실에서 먼저 {{kebersihan}}을 하고, {{minuman}}을 한 잔 마십니다. 그리고 활기차게 하루를 시작합니다!",
            templateId = "Saya bangun pagi jam {{jam}}. Di kamar mandi pertama-tama saya {{kebersihan}}, lalu minum segelas {{minuman}}. Kemudian memulai hari dengan semangat!",
            placeholders = listOf(
                TemplatePlaceholder("jam", "Jam Bangun", "7", listOf("6", "7", "8")),
                TemplatePlaceholder("kebersihan", "Aktivitas Kamar Mandi", "세수와 양치질 (Cuci Muka & Sikat Gigi)", listOf("세수와 양치질 (Cuci Muka & Sikat Gigi)", "따뜻한 물 샤워 (Mandi Shower Air Hangat)", "머리 감기 (Keramas Shampo)")),
                TemplatePlaceholder("minuman", "Minuman Pagi", "따뜻한 물 (Air Hangat)", listOf("따뜻한 물 (Air Hangat)", "시원한 물 (Air Dingin)", "커피 (Kopi)", "우유 (Susu)"))
            )
        ),
        IntroTemplate(
            id = "intro_level_5",
            level = 2,
            title = "Bersepeda & Transportasi Harian (자전거와 교통)",
            description = "Menjelaskan cara bepergian harian dengan sepeda Ttareungi, bus, atau subway.",
            templateKo = "저는 날씨가 좋은 날에 {{transportasi}}을 즐겨 탑니다. 특히 한강 공원에서 {{sepeda}}을 빌려 타는 것을 좋아합니다. 안전을 위해 항상 {{safety}}을 착용합니다.",
            templateId = "Pada hari yang cerah saya suka mengendarai {{transportasi}}. Terutama saya suka menyewa {{sepeda}} di Taman Sungai Han. Demi keselamatan saya selalu memakai {{safety}}.",
            placeholders = listOf(
                TemplatePlaceholder("transportasi", "Moda Transportasi", "자전거 (Sepeda)", listOf("자전거 (Sepeda)", "지하철 (Subway MRT)", "시내버스 (Bus Kota)")),
                TemplatePlaceholder("sepeda", "Pilihan Sepeda", "따릉이 자전거 (Sepeda Sewa Ttareungi)", listOf("따릉이 자전거 (Sepeda Sewa Ttareungi)", "로드 자전거 (Sepeda Balap)", "전기 자전거 (Sepeda Listrik)")),
                TemplatePlaceholder("safety", "Alat Keselamatan", "안전 헬멧 (Helm Pengaman)", listOf("안전 헬멧 (Helm Pengaman)", "보호대 (Pelindung Lutut)"))
            )
        ),
        IntroTemplate(
            id = "intro_level_6",
            level = 3,
            title = "Ekspresi Emosi & Frustrasi (감정과 리액션)",
            description = "Mengekspresikan rasa kesal, nyesek, atau syok secara luwes dengan penutup yang bijak.",
            templateKo = "오늘 {{kendala}} 때문에 정말 {{emosi}}. 헐, 어이가 없었지만 그래도 {{pelampiasan}} 기분이 한결 풀렸습니다. 다행이에요!",
            templateId = "Hari ini karena {{kendala}} rasanya sangat {{emosi}}. Astaga, sempat speechless tapi setelah {{pelampiasan}} suasana hati membaik. Syukurlah!",
            placeholders = listOf(
                TemplatePlaceholder("kendala", "Penyebab Masalah", "지하철 연착 (Keterlambatan Subway)", listOf("지하철 연착 (Keterlambatan Subway)", "비 오는 날 우산 분실 (Payung Hilang Pas Hujan)", "스마트폰 방전 (Baterai HP Habis)", "심한 교통체증 (Macet Parah)")),
                TemplatePlaceholder("emosi", "Perasaan / Emosi", "답답하고 짜증났습니다 (Frustrasi dan Kesal)", listOf("답답하고 짜증났습니다 (Frustrasi dan Kesal)", "속상하고 당황스러웠습니다 (Sedih dan Panik)", "완전 열받았습니다 (Naik Darah Sekali)")),
                TemplatePlaceholder("pelampiasan", "Cara Menenangkan Diri", "친구와 수다 떨고 (Mengobrol dengan Teman)", listOf("친구와 수다 떨고 (Mengobrol dengan Teman)", "따뜻한 차를 마시고 (Minum Teh Hangat)", "맛있는 떡볶이를 먹고 (Makan Tteokbokki Enak)"))
            )
        ),
        IntroTemplate(
            id = "intro_level_7",
            level = 2,
            title = "Tegur Sapa Sehari-hari (일상 인사와 안부)",
            description = "Berbasa-basi hangat, menanyakan kabar makan, cuaca, dan salam saling mendoakan.",
            templateKo = "안녕하세요! {{sapaan}}? 오늘 날씨가 {{cuaca}}네요. 오늘도 힘내시고 {{doa}}!",
            templateId = "Halo! {{sapaan}}? Cuaca hari ini {{cuaca}} ya. Hari ini tetap semangat dan {{doa}}!",
            placeholders = listOf(
                TemplatePlaceholder("sapaan", "Kalimat Tegur Sapa", "식사하셨어요 (Sudah makan?)", listOf("식사하셨어요 (Sudah makan?)", "좋은 아침이에요 (Selamat pagi)", "오랜만이에요 (Lama tak jumpa)", "어디 가시는 길이에요 (Mau pergi ke mana?)")),
                TemplatePlaceholder("cuaca", "Kondisi Cuaca", "참 화창하고 맑 (sangat cerah dan segar)", listOf("참 화창하고 맑 (sangat cerah dan segar)", "조금 쌀쌀하 (agak dingin)", "바람이 시원하 (anginnya sejuk)")),
                TemplatePlaceholder("doa", "Ucapan Harapan / Pamit", "수고 많으셨습니다 (Terima kasih atas kerja kerasnya)", listOf("수고 많으셨습니다 (Terima kasih atas kerja kerasnya)", "조심해서 들어가세요 (Hati-hati di jalan pulang)", "내일 또 봬요 (Sampai jumpa lagi besok)"))
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
        ),
        ListeningQuiz(
            id = "lq7",
            question = "Dengarkan pengumuman toilet di stasiun MRT Seoul:",
            spokenKorean = "고객 여러분, 화장실은 3번 출구 개찰구 밖 오른쪽에 위치해 있습니다.",
            romanization = "gogaek yeoreobun, hwajangsil-eun sambeon chulgu gaechalgu bakk oreunjjoge wichihae isseumnida.",
            options = listOf(
                "Pintu keluar 3 di luar gerbang sebelah kanan",
                "Di dalam gerbong kereta nomor 3",
                "Lantai 2 dekat eskalator kiri",
                "Di samping loket tiket pintu 1"
            ),
            correctIndex = 0,
            explanation = "화장실 (toilet) + 3번 출구 (pintu keluar 3) + 개찰구 밖 (luar gerbang tiket) + 오른쪽 (sebelah kanan)."
        ),
        ListeningQuiz(
            id = "lq8",
            question = "Dengarkan instruksi keselamatan sewa sepeda Ttareungi:",
            spokenKorean = "따릉이 대여소에서 잠금장치를 해제한 후 헬멧을 착용하고 자전거 도로를 이용하세요.",
            romanization = "ttareung-i daeyeoso-eseo jamgeumjangchireul haejehan hu helmes-eul chak-yonghago jajeongeo dororeul iyonghaseyo.",
            options = listOf(
                "Boleh melewati jalan raya tol cepat",
                "Gunakan helm dan kendarai di jalur khusus sepeda",
                "Dilarang bersepeda setelah pukul 6 sore",
                "Kembalikan sepeda dalam waktu 10 menit"
            ),
            correctIndex = 1,
            explanation = "헬멧을 착용하고 (kenakan helm) + 자전거 도로를 이용하세요 (gunakan jalur khusus sepeda)."
        ),
        ListeningQuiz(
            id = "lq9",
            question = "Dengarkan pertanyaan kasir minimarket kepada pembeli:",
            spokenKorean = "봉투 필요하세요? 백 원 추가됩니다.",
            romanization = "bongtu piryohaseyo? baek won chugadoemnida.",
            options = listOf(
                "Apakah memiliki kartu langganan member?",
                "Apakah memerlukan kantong belanja seharga 100 won?",
                "Berapa nomor telepon untuk poin?",
                "Apakah ingin membayar dengan uang pas?"
            ),
            correctIndex = 1,
            explanation = "봉투 필요하세요? (apakah perlu kantong plastik?) + 백 원 추가됩니다 (tambah biaya 100 won)."
        ),
        ListeningQuiz(
            id = "lq10",
            question = "Dengarkan keluhan tamu guesthouse berikut:",
            spokenKorean = "저기요, 샤워실에 따뜻한 물이 안 나와요. 보일러 좀 확인해 주세요.",
            romanization = "jeogiyo, syawosir-e ttatteut-han mur-i an nawayo. bo-illeo jom hwaginhae juseyo.",
            options = listOf(
                "Kunci pintu kamar mandi tidak bisa dibuka",
                "Air hangat shower tidak keluar, mohon cek mesin boiler pemanas",
                "Lampu kamar mandi berkedip dan mati",
                "Tisu toilet basah terkena air"
            ),
            correctIndex = 1,
            explanation = "샤워실 (kamar mandi shower) + 따뜻한 물이 안 나와요 (air hangat tidak keluar) + 보일러 확인 (cek boiler)."
        ),
        ListeningQuiz(
            id = "lq11",
            question = "Dengarkan audio berikut. Emosi dan situasi apa yang sedang dirasakan pembicara?",
            spokenKorean = "지하철을 바로 눈앞에서 놓쳐서 진짜 짜증나고 답답해 죽겠어!",
            romanization = "jihacheor-eul baro nun-ap-eseo nochyeoseo jinjja jjajeungnago dapdaphae jukgesseo!",
            options = listOf(
                "Sangat kesal dan frustrasi karena ketinggalan subway tepat di depan mata",
                "Sangat senang karena gerbong subway kosong dan sejuk",
                "Bingung mencari nomor pintu keluar stasiun subway",
                "Ingin membeli tiket terusan subway bulanan"
            ),
            correctIndex = 0,
            explanation = "지하철을 놓치다 (ketinggalan kereta) + 진짜 짜증나다 (benar-benar kesal) + 답답해 죽겠어 (sangat frustrasi/nyesek setengah mati)."
        ),
        ListeningQuiz(
            id = "lq12",
            question = "Dengarkan tegur sapa dan salam pamit berikut. Apa maksud utama ucapan tersebut?",
            spokenKorean = "오늘 발표 준비하느라 정말 수고 많으셨습니다. 조심해서 들어가시고 내일 봬요!",
            romanization = "oneul balpyo junbihaneura jeongmal sugo maneusyeosseumnida. josimhaeseo deureogasigo naeil bwaeyo!",
            options = listOf(
                "Meminta rekan kerja membatalkan presentasi besok",
                "Apresiasi kerja keras persiapan presentasi hari ini dan salam hati-hati pulang",
                "Mengajak makan malam bersama di restoran daging",
                "Mengeluhkan proyektor ruang rapat yang rusak"
            ),
            correctIndex = 1,
            explanation = "수고 많으셨습니다 (terima kasih atas jerih payah/kerja keras) + 조심해서 들어가세요 (hati-hati pulang) + 내일 봬요 (sampai jumpa besok)."
        ),
        ListeningQuiz(
            id = "lq13",
            question = "Dengarkan permintaan pelanggan di rumah makan Korea:",
            spokenKorean = "여기 김치찌개 2인분 주시고, 아이가 먹을 거니까 덜 맵게 해 주세요.",
            romanization = "yeogi gimchijjigae i-inbun jusigo, a-iga meogeul geonikka deol maepge hae juseyo.",
            options = listOf(
                "Pesan 2 porsi kimchi jjigae dan minta dibuat tidak terlalu pedas karena untuk anak-anak",
                "Minta tambah sambal cabai ekstra pedas pada sup",
                "Minta bungkus sisa makanan untuk dibawa pulang",
                "Menanyakan menu minuman es teh manis"
            ),
            correctIndex = 0,
            explanation = "김치찌개 2인분 (2 porsi kimchi jjigae) + 덜 맵게 해 주세요 (tolong buat kurang pedas/tidak terlalu pedas)."
        ),
        ListeningQuiz(
            id = "lq14",
            question = "Dengarkan instruksi minum obat dari apoteker berikut:",
            spokenKorean = "이 알약은 하루 세 번 식후 30분에 따뜻한 물과 함께 복용하세요.",
            romanization = "i alyag-eun haru se beon sik-hu samsipbune ttatteut-han mulgwa hamkke bogyonghaseyo.",
            options = listOf(
                "Obat tetes mata sebelum tidur malam",
                "Minum obat pil ini 3 kali sehari 30 menit setelah makan bersama air hangat",
                "Oleskan salep pada kulit yang luka setiap 2 jam",
                "Hanya diminum jika merasa pusing kepala"
            ),
            correctIndex = 1,
            explanation = "알약 (obat pil/tablet) + 하루 세 번 (3 kali sehari) + 식후 30분 (30 menit setelah makan) + 따뜻한 물 (air hangat)."
        ),
        ListeningQuiz(
            id = "lq15",
            question = "Dengarkan petunjuk arah jalan berikut:",
            spokenKorean = "앞으로 100미터 직진하시다가 편의점 골목에서 오른쪽으로 꺾으시면 바로 나와요.",
            romanization = "apeuro baek-miteo jikjinhasi-daga pyeon-uijeom golmog-eseo oreunjjogeuro kkeokkeusimyeon baro nawayo.",
            options = listOf(
                "Naik bus nomor 100 menuju stasiun pusat",
                "Jalan lurus 100 meter lalu belok kanan di gang minimarket, tempatnya langsung terlihat",
                "Putar balik di jembatan penyeberangan orang",
                "Masuk ke dalam stasiun subway di sebelah kiri"
            ),
            correctIndex = 1,
            explanation = "직진하다 (jalan lurus) + 편의점 골목 (gang minimarket) + 오른쪽으로 꺾다 (belok ke kanan)."
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
        ),
        ReadingStory(
            id = "story_3",
            titleKo = "한강 공원에서 따릉이 자전거 타기",
            titleId = "Bersepeda Ttareungi di Taman Sungai Han",
            level = "Pemula 2",
            paragraphs = listOf(
                ReadingParagraph(
                    sentenceKo = "화창한 토요일 오후에 친구와 함께 여의도 한강 공원에 갔습니다.",
                    sentenceRo = "Hwachanghan toyoil ohue chinguwa hamkke yeouido hangang gong-wone gatseumnida.",
                    sentenceId = "Pada Sabtu siang yang cerah, saya pergi ke Taman Sungai Han Yeouido bersama teman.",
                    keyWords = listOf("토요일" to "Hari Sabtu", "한강" to "Sungai Han", "공원" to "Taman")
                ),
                ReadingParagraph(
                    sentenceKo = "우리는 스마트폰 앱으로 따릉이 자전거를 빌리고 헬멧을 착용했습니다.",
                    sentenceRo = "Urineun seumateupon aebeuro ttareung-i jajeongeoreul billigo helmes-eul chak-yonghaetseumnida.",
                    sentenceId = "Kami menyewa sepeda Ttareungi dengan aplikasi smartphone dan mengenakan helm.",
                    keyWords = listOf("따릉이" to "Sepeda publik Seoul", "자전거" to "Sepeda", "헬멧" to "Helm")
                ),
                ReadingParagraph(
                    sentenceKo = "자전거 도로를 따라 시원한 강바람을 맞으며 기분 좋게 달렸습니다.",
                    sentenceRo = "Jajeongeo dororeul ttara siwonhan gangbaram-eul majeumyeo gibun joke dallyeotseumnida.",
                    sentenceId = "Kami bersepeda menyusuri jalur khusus sepeda sambil menikmati hembusan angin sungai yang sejuk.",
                    keyWords = listOf("자전거 도로" to "Jalur sepeda", "강바람" to "Angin sungai", "달리다" to "Berlari/Bersepeda")
                ),
                ReadingParagraph(
                    sentenceKo = "운동 후 편의점에서 끓인 즉석 라면을 먹으니 정말 꿀맛이었습니다.",
                    sentenceRo = "Undong hu pyeon-uijeom-eseo kkeurin jeukseok ramyeon-eul meogeuni jeongmal kkulmasieotseumnida.",
                    sentenceId = "Setelah berolahraga, kami makan ramyun rebusan instan di minimarket yang rasanya sungguh nikmat luar biasa.",
                    keyWords = listOf("편의점" to "Minimarket", "라면" to "Ramyun", "꿀맛" to "Sangat lezat bak madu")
                )
            ),
            questions = listOf(
                ReadingQuestion("Kendaraan apa yang disewa tokoh di Sungai Han?", listOf("Mobil sewa", "Sepeda Ttareungi (따릉이)", "Skuter listrik", "Perahu dayung"), 1),
                ReadingQuestion("Perlengkapan apa yang dipakai demi keselamatan?", listOf("Kacamata hitam", "Helm pengaman (헬멧)", "Sarung tangan", "Jaket tebal"), 1),
                ReadingQuestion("Makanan apa yang dinikmati di minimarket setelah bersepeda?", listOf("Kue beras", "Ramyun instan (라면)", "Pizza", "Roti"), 1)
            )
        ),
        ReadingStory(
            id = "story_4",
            titleKo = "서울 게스트하우스의 아침과 생활 에티켓",
            titleId = "Pagi Hari di Guesthouse Seoul & Etiket Rumah Tangga",
            level = "Menengah 1",
            paragraphs = listOf(
                ReadingParagraph(
                    sentenceKo = "게스트하우스에서는 여러 나라 사람들이 함께 생활합니다.",
                    sentenceRo = "Geseuteuhauseu-eseoneun yeoreo nara saramdeuri hamkke saenghwalhamnida.",
                    sentenceId = "Di guesthouse, orang-orang dari berbagai negara tinggal bersama.",
                    keyWords = listOf("생활하다" to "Tinggal/Berkehidupan", "사람들" to "Orang-orang", "함께" to "Bersama")
                ),
                ReadingParagraph(
                    sentenceKo = "아침에 화장실과 샤워실을 이용할 때는 15분 이내로 배려하여 사용합니다.",
                    sentenceRo = "Achime hwajangsilgwa syawosireul iyonghal ttaeneun sibobun inaero baeryeohayeo sayonghamnida.",
                    sentenceId = "Saat menggunakan toilet dan shower di pagi hari, kita menggunakan dengan tenggang rasa di bawah 15 menit.",
                    keyWords = listOf("화장실" to "Toilet", "샤워실" to "Kamar mandi shower", "배려" to "Tenggang rasa")
                ),
                ReadingParagraph(
                    sentenceKo = "사용 후에는 비누와 샴푸를 제자리에 두고 바닥 물기를 닦습니다.",
                    sentenceRo = "Sayong hueneun binuwa syampureul jejari-e dugo badak mulgireul dakseumnida.",
                    sentenceId = "Setelah dipakai, letakkan sabun dan shampo pada tempatnya serta seka genangan air lantai.",
                    keyWords = listOf("비누" to "Sabun", "샴푸" to "Shampo", "물기" to "Genangan air")
                ),
                ReadingParagraph(
                    sentenceKo = "음식물 쓰레기와 플라스틱은 분리수거통에 바르게 버려야 합니다.",
                    sentenceRo = "Eumsikmul sseuregigwa peullaseutigeun bunrisugeotong-e bareuge beoryeoya hamnida.",
                    sentenceId = "Sampah sisa makanan dan plastik harus dibuang secara tertib ke tempat daur ulang.",
                    keyWords = listOf("음식물 쓰레기" to "Sampah makanan", "분리수거" to "Pilah sampah", "버리다" to "Membuang")
                )
            ),
            questions = listOf(
                ReadingQuestion("Berapa lama batas waktu wajar mandi shower di pagi hari?", listOf("1 jam", "Maksimal 15 menit (15분 이내)", "30 menit", "Bebas kapan saja"), 1),
                ReadingQuestion("Apa yang harus dirapikan setelah memakai kamar mandi?", listOf("Meninggalkan sabun di lantai", "Menaruh sabun/shampo di tempatnya dan menyeka air lantai", "Mematikan lampu saja", "Mengunci pintu"), 1),
                ReadingQuestion("Bagaimana aturan pembuangan sampah di Korea?", listOf("Dibuang langsung ke jalan", "Dipisahkan sesuai jenisnya di tong 분리수거", "Dibakar di halaman", "Dikubur di tanah"), 1)
            )
        ),
        ReadingStory(
            id = "story_5",
            titleKo = "비 오는 월요일의 짜증과 따뜻한 인사",
            titleId = "Kekesalan di Hari Senin yang Hujan & Kehangatan Tegur Sapa",
            level = "Menengah 2",
            paragraphs = listOf(
                ReadingParagraph(
                    sentenceKo = "월요일 아침부터 밖에는 장대비가 쏟아졌고 지하철은 20분이나 연착되었습니다.",
                    sentenceRo = "Woryoil achimbuteo bakk-eneun jangdaebiga ssodajyeotgo jihacheor-eun isipbun-ina yeonchakdoeeotseumnida.",
                    sentenceId = "Sejak Senin pagi di luar hujan lebat mengguyur dan subway tertunda sampai 20 menit.",
                    keyWords = listOf("월요일" to "Hari Senin", "장대비" to "Hujan lebat", "연착" to "Keterlambatan")
                ),
                ReadingParagraph(
                    sentenceKo = "옷도 젖고 지각할 것 같아서 '진짜 짜증나고 답답하다'는 생각이 들었습니다.",
                    sentenceRo = "Otdo jeotgo jigak-hal geot gat-aseo 'jinjja jjajeungnago dapdaphada'neun saenggag-i deureotseumnida.",
                    sentenceId = "Pakaian basah dan saya merasa akan terlambat sehingga terlintas pikiran 'benar-benar kesal dan nyesek'.",
                    keyWords = listOf("젖다" to "Basah", "지각하다" to "Terlambat", "짜증나다" to "Merasa kesal", "답답하다" to "Frustrasi")
                ),
                ReadingParagraph(
                    sentenceKo = "하지만 회사 문을 열자 동료들이 '좋은 아침이에요! 비 많이 맞았죠? 수건 드릴게요'라며 반갑게 맞이해 주었습니다.",
                    sentenceRo = "Hajiman hoesa mun-eul yeolja dongryodeuri 'joeun achimieyo! bi mani majatjyo? sugeon deurilgeyo'ramyeo bangapge majihae jueotseumnida.",
                    sentenceId = "Namun begitu membuka pintu kantor, rekan-rekan menyapa hangat 'Selamat pagi! Kena hujan banyak ya? Saya ambilkan handuk ya'.",
                    keyWords = listOf("동료" to "Rekan kerja", "좋은 아침" to "Selamat pagi", "수건" to "Handuk", "반갑다" to "Senang/Hangat")
                ),
                ReadingParagraph(
                    sentenceKo = "따뜻한 차 한 잔과 함께 나눈 다정한 인사 한마디 덕분에 아침의 짜증이 눈 녹듯 사라졌습니다.",
                    sentenceRo = "Ttatteut-han cha han jan-gwa hamkke nanun dajeonghan insa hanmadi deokbun-e achim-ui jjajeung-i nun nokdeut sarajyeotseumnida.",
                    sentenceId = "Berkat secangkir teh hangat dan sepatah kata tegur sapa yang penuh kasih, rasa kesal di pagi hari lenyap seperti salju mencair.",
                    keyWords = listOf("따뜻한 차" to "Teh hangat", "인사" to "Tegur sapa", "사라지다" to "Menghilang/Lenyap")
                )
            ),
            questions = listOf(
                ReadingQuestion("Mengapa tokoh merasa kesal (짜증나다) di hari Senin pagi?", listOf("Kalah main game", "Hujan lebat dan kereta subway tertunda 20 menit", "Dompetnya hilang", "Makanan paginya gosong"), 1),
                ReadingQuestion("Bagaimana sambutan rekan kerja saat tokoh tiba di kantor?", listOf("Memarahi karena basah", "Menyapa '좋은 아침' dengan ramah dan menawarkan handuk", "Menyuruhnya pulang lagi", "Mengacuhkannya"), 1),
                ReadingQuestion("Pelajaran apa yang dipetik dari cerita?", listOf("Jangan pernah keluar rumah", "Tegur sapa hangat dan perhatian tulus bisa melenyapkan rasa kesal", "Subway Korea selalu mogok", "Minum kopi lebih baik daripada teh"), 1)
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
        ),
        VideoImmersion(
            id = "video_3",
            title = "Mencari Toilet & Kode PIN di Kafe (화장실 비밀번호)",
            sourceTitle = "Daily Cafe Survival Talk",
            durationText = "01:05",
            thumbnailColorHex = 0xFF2563EB,
            lines = listOf(
                SubtitleLine(0, 3000, "저기요, 죄송한데 화장실이 어디에 있어요?", "Jeogiyo, joesonghande hwajangsiri eodie isseoyo?", "Permisi, maaf di manakah letak toilet?"),
                SubtitleLine(3200, 6500, "매장 밖으로 나가셔서 오른쪽 계단 옆에 있습니다.", "Maejang bakkeuro nagasyeoseo oreunjjok gyedan yeope isseumnida.", "Keluar toko ke kanan, ada tepat di samping tangga."),
                SubtitleLine(6700, 9500, "문이 잠겨 있나요? 비밀번호가 있어요?", "Muni jamgyeo innayo? Bimilbeonhoga isseoyo?", "Apakah pintunya terkunci? Apakah ada nomor sandinya?"),
                SubtitleLine(9700, 13500, "네, 영수증 맨 아래쪽에 네 자리 번호 2468입니다.", "Ne, yeongsujeung maen araejjoge ne jari beonho 2468-imnida.", "Ya, di paling bawah struk ada empat digit nomor 2468."),
                SubtitleLine(13800, 16500, "휴지도 화장실 입구에서 챙겨 가세요!", "Hyujido hwajangsil ipgue-seo chaenggyeo gaseyo!", "Ambil juga tisunya di dekat pintu masuk toilet ya!")
            )
        ),
        VideoImmersion(
            id = "video_4",
            title = "Menyewa Sepeda Ttareungi di Seoul (따릉이 자전거)",
            sourceTitle = "Active Han River Scenario",
            durationText = "01:20",
            thumbnailColorHex = 0xFF059669,
            lines = listOf(
                SubtitleLine(0, 3000, "오늘 날씨 정말 맑다! 따릉이 자전거 탈래?", "Oneul nalssi jeongmal makda! Ttareung-i jajeongeo tallae?", "Hari ini cerah sekali! Mau naik sepeda Ttareungi?"),
                SubtitleLine(3200, 6800, "응, 좋아! 따릉이 앱 켜서 안장 아래 QR코드 스캔할게.", "Eung, joa! Ttareung-i aep kyeoseo anjang arae QR kodeu seukaenhalge.", "Ya, ayo! Aku buka aplikasi Ttareungi lalu pindai kode QR di bawah sadel."),
                SubtitleLine(7000, 10000, "잠금장치 풀렸다! 헬멧 꼭 착용하고 타자.", "Jamgeumjangchi pullyeotda! Helmet kkok chak-yonghago taja.", "Kuncinya sudah terbuka! Pastikan pakai helm ya."),
                SubtitleLine(10200, 14000, "자전거 도로 따라서 여의도 한강 공원까지 가자!", "Jajeongeo doro ttara yeouido hangang gong-won-kkaji gaja!", "Ayo ikuti jalur sepeda menuju Taman Sungai Han Yeouido!"),
                SubtitleLine(14200, 17500, "한 시간 타고 근처 대여소에 다시 반납하면 돼.", "Han sigan tago geuncheo daeyeoso-e dasi bannaphamyeon dwae.", "Setelah satu jam, kita kembalikan di stasiun sepeda terdekat.")
            )
        ),
        VideoImmersion(
            id = "video_5",
            title = "Subway Terlambat & Frustrasi Hujan (지하철 연착과 짜증)",
            sourceTitle = "Real Commuter Drama Scenario",
            durationText = "01:10",
            thumbnailColorHex = 0xFFDC2626,
            lines = listOf(
                SubtitleLine(0, 3200, "비도 오는데 지하철까지 20분이나 연착됐어. 진짜 짜증나!", "Bido oneunde jihacheol-kkaji isipbun-ina yeonchakdwaesseo. Jinjja jjajeungna!", "Sudah hujan begini, subway tertunda 20 menit lagi. Benar-benar kesal!"),
                SubtitleLine(3400, 7000, "옷도 젖고 답답해 죽겠네. 팀장님께 먼저 연락드렸어?", "Otdo jeotgo dapdaphae jukgenne. Timjangnimkke meonjeo yeollakdeuryeosseo?", "Pakaian basah kuyup dan rasanya nyesek banget. Sudah kabari manajer tim duluan?"),
                SubtitleLine(7200, 10500, "응, 방금 메시지 남겼는데 '빗길 조심해서 천천히 오세요'라고 하셨어.", "Eung, banggeum mesiji namgyeonneunde 'bitgil josimhaeseo cheoncheonhi oseyo'rago hasyeosseo.", "Ya, barusan aku kirim pesan dan beliau membalas 'Hati-hati di jalan yang licin karena hujan, jalan santai saja ya'."),
                SubtitleLine(10800, 14200, "와, 다행이다! 따뜻한 커피 한 잔 마시며 마음 가라앉히자.", "Wa, dahaeng-ida! Ttatteut-han keopi han jan masimyeo maeum gara-anjhija.", "Wah syukurlah! Ayo minum segelas kopi hangat sambil menenangkan pikiran.")
            )
        ),
        VideoImmersion(
            id = "video_6",
            title = "Tegur Sapa & Semangat Pagi di Kantor (직장 아침 인사와 격려)",
            sourceTitle = "Office Daily Talk Scenario",
            durationText = "01:00",
            thumbnailColorHex = 0xFF7C3AED,
            lines = listOf(
                SubtitleLine(0, 3000, "좋은 아침입니다, 대리님! 주말 잘 보내셨어요?", "Joeun achim-imnida, daerinim! Jumal jal bonaesyeosseoyo?", "Selamat pagi, Asisten Manajer! Apakah akhir pekan Anda menyenangkan?"),
                SubtitleLine(3200, 6500, "네, 푹 쉬었습니다. 밥은 먹고 출근했어요?", "Ne, puk swieotseumnida. Bap-eun meokgo chulgeunhaesseoyo?", "Ya, saya istirahat dengan nyaman. Sudah sarapan sebelum berangkat kerja?"),
                SubtitleLine(6700, 10000, "네, 든든하게 먹었습니다! 오늘 미팅 발표도 화이팅입니다!", "Ne, deundeunhage meogeotseumnida! Oneul miting balpyodo hwaiting-imnida!", "Ya, sudah makan kenyang! Semangat juga untuk presentasi rapat hari ini!"),
                SubtitleLine(10200, 13800, "고마워요! 오늘도 힘내서 같이 수고해 봐요.", "Gomawoyo! Oneuldo himnaeseo gachi sugohae bwayo.", "Terima kasih! Hari ini mari berjuang dan bekerja keras bersama-sama.")
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
