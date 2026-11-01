package com.example.data.model

enum class KoreanDocumentType {
    LETTER,    // Surat (편지)
    CHAT,      // Obrolan / Chatting (대화 / 메신저)
    STORY,     // Cerita / Dongeng / Diary (이야기 / 동화 / 일기)
    REAL_DOC   // Dokumen Kehidupan Sehari-hari (실생활 문서 / 서식)
}

data class DocumentSentence(
    val id: String,
    val korean: String,
    val romanization: String,
    val indonesian: String,
    val speaker: String? = null // Pembicara jika format chat (misal: "하나", "민수", "Aku")
)

data class KoreanDocumentItem(
    val id: String,
    val titleId: String,
    val titleKo: String,
    val type: KoreanDocumentType,
    val categoryBadge: String,
    val iconEmoji: String,
    val description: String,
    val formalityLevel: String, // misal: "존댓말 (Sopan)", "반말 (Santai)", "격식체 (Formal)"
    val recipient: String,     // Penerima / Pihak terkait
    val fullKoreanText: String,
    val fullTranslation: String,
    val sentences: List<DocumentSentence>,
    val vocabNotes: List<Pair<String, String>>,
    val grammarTips: String
)

object KoreanDocumentDataProvider {
    val sampleDocuments: List<KoreanDocumentItem> = listOf(
        // ==================== 1. SURAT (편지) ====================
        KoreanDocumentItem(
            id = "letter_friend",
            titleId = "Surat Hangat untuk Sahabat",
            titleKo = "친구에게 보내는 따뜻한 편지",
            type = KoreanDocumentType.LETTER,
            categoryBadge = "💌 Surat Akrab",
            iconEmoji = "💌",
            description = "Surat persahabatan santai (반말) menyapa teman lama, menceritakan kesibukan, dan mengajak bertemu.",
            formalityLevel = "반말 (Santai / Akrab)",
            recipient = "민우에게 (Kepada Minwoo)",
            fullKoreanText = """
민우에게,

안녕, 민우야! 잘 지내고 있어?
요즘 날씨가 많이 쌀쌀해졌어. 감기 조심하고 있지?
나는 요즘 한국어 공부를 열심히 하고 있어. 한글 타자도 매일 연습하고 있단다.
다음 주말에 시간 있으면 우리 홍대에서 맛있는 밥 먹자!
네 답장 기다릴게. 항상 건강해!

2026년 11월,
너의 오랜 친구 지수가.
            """.trimIndent(),
            fullTranslation = """
Kepada Minwoo,

Halo, Minwoo! Apa kabar?
Akhir-akhir ini cuaca sudah mulai dingin. Kamu jaga kesehatan agar tidak flu, kan?
Aku sekarang lagi rajin belajar bahasa Korea. Setiap hari aku juga latihan mengetik Hangul.
Kalau akhir pekan depan kamu ada waktu, ayo kita makan enak di Hongdae!
Aku tunggu balasanmu ya. Sehat selalu!

November 2026,
Dari sahabat lamamu, Jisu.
            """.trimIndent(),
            sentences = listOf(
                DocumentSentence("s1", "안녕, 민우야! 잘 지내고 있어?", "an-nyeong, min-u-ya! jal ji-nae-go iss-eo?", "Halo, Minwoo! Apa kabar?"),
                DocumentSentence("s2", "요즘 날씨가 많이 쌀쌀해졌어.", "yo-jeum nal-ssi-ga man-i ssal-ssal-hae-jyeoss-eo.", "Akhir-akhir ini cuaca sudah mulai dingin."),
                DocumentSentence("s3", "나는 요즘 한국어 공부를 열심히 하고 있어.", "na-neun yo-jeum han-guk-eo gong-bu-reul yeol-sim-hi ha-go iss-eo.", "Aku sekarang lagi rajin belajar bahasa Korea."),
                DocumentSentence("s4", "한글 타자도 매일 연습하고 있단다.", "han-geul ta-ja-do mae-il yeon-seup-ha-go it-dan-da.", "Aku juga latihan mengetik Hangul setiap hari."),
                DocumentSentence("s5", "다음 주말에 시간 있으면 우리 밥 먹자!", "da-eum ju-mar-e si-gan iss-eu-myeon u-ri bap meok-ja!", "Kalau akhir pekan depan ada waktu, ayo kita makan bareng!"),
                DocumentSentence("s6", "네 답장 기다릴게. 항상 건강해!", "ne dap-jang gi-da-ril-ge. hang-sang geon-gang-hae!", "Aku tunggu balasanmu ya. Sehat selalu!")
            ),
            vocabNotes = listOf(
                "쌀쌀하다" to "Dingin / sejuk menusuk",
                "타자 (打字)" to "Mengetik (typing)",
                "답장" to "Surat balasan",
                "주말" to "Akhir pekan (weekend)",
                "열심히" to "Dengan tekun / rajin"
            ),
            grammarTips = "Khas surat akrab Korea (반말): Menyapa nama dengan imbuhan '-야' (민우야), mengajak dengan akhiran '-자' (먹자 = ayo makan), dan penutup penanda pengirim '-가' (지수가 = dari Jisu)."
        ),

        KoreanDocumentItem(
            id = "letter_fan",
            titleId = "Surat Penggemar K-Pop (Fan Letter)",
            titleKo = "최애 아이돌에게 보내는 팬레터",
            type = KoreanDocumentType.LETTER,
            categoryBadge = "⭐ Fan Letter",
            iconEmoji = "✨",
            description = "Surat apresiasi penggemar kepada idola pujaan dalam bahasa sopan penuh rasa hormat dan kasih sayang.",
            formalityLevel = "존댓말 (Sopan & Hormat)",
            recipient = "사랑하는 오빠에게 (Untuk Oppa tercinta)",
            fullKoreanText = """
사랑하는 정국 오빠에게,

안녕하세요! 저는 인도네시아에서 항상 오빠를 응원하는 팬이에요.
오빠의 노래와 무대는 언제나 저에게 큰 힘과 위로가 되어 줘요.
새 앨범 노래도 매일 듣고 있어요. 목소리가 정말 감미로워요!
월드 투어 일정으로 많이 바쁘겠지만, 밥 잘 챙겨 먹고 아프지 마세요.
언제나 오빠의 행복과 건강을 기도할게요.
항상 곁에서 응원하고 사랑해요! 보라해 💜

인도네시아 팬 아미 올림.
            """.trimIndent(),
            fullTranslation = """
Untuk Jungkook oppa tercinta,

Halo! Saya adalah penggemar dari Indonesia yang selalu mendukung oppa.
Lagu dan penampilan panggung oppa selalu memberikan kekuatan dan penghiburan besar bagi saya.
Lagu di album baru juga saya dengarkan setiap hari. Suaranya sungguh merdu!
Meskipun pasti sangat sibuk dengan jadwal world tour, jangan lupa makan teratur dan jangan sampai sakit ya.
Saya akan selalu berdoa untuk kebahagiaan dan kesehatan oppa.
Saya selalu mendukung dan mencintaimu dari sisi! Borahae (I Purple You) 💜

Hormat saya, ARMY penggemar Indonesia.
            """.trimIndent(),
            sentences = listOf(
                DocumentSentence("sf1", "저는 인도네시아에서 항상 오빠를 응원하는 팬이에요.", "jeo-neun in-do-ne-si-a-e-seo hang-sang op-pa-reul eung-won-ha-neun paen-i-e-yo.", "Saya penggemar dari Indonesia yang selalu mendukung oppa."),
                DocumentSentence("sf2", "오빠의 노래는 저에게 큰 힘과 위로가 돼요.", "op-pa-ui no-rae-neun jeo-e-ge keun him-gwa wi-ro-ga dwae-yo.", "Lagu oppa memberikan kekuatan dan penghiburan besar bagi saya."),
                DocumentSentence("sf3", "새 앨범 노래도 매일 듣고 있어요.", "sae ael-beom no-rae-do mae-il deut-go iss-eo-yo.", "Saya juga mendengarkan lagu di album baru setiap hari."),
                DocumentSentence("sf4", "밥 잘 챙겨 먹고 아프지 마세요.", "bap jal chaeng-gyeo meok-go a-peu-ji ma-se-yo.", "Makanlah dengan teratur dan jangan sampai sakit ya."),
                DocumentSentence("sf5", "언제나 오빠의 행복과 건강을 기도할게요.", "eon-je-na op-pa-ui haeng-bok-gwa geon-gang-eul gi-do-hal-ge-yo.", "Saya akan selalu berdoa untuk kebahagiaan dan kesehatan oppa."),
                DocumentSentence("sf6", "항상 곁에서 응원하고 사랑해요!", "hang-sang gyeot-e-seo eung-won-ha-go sa-rang-hae-yo!", "Saya selalu mendukung dan mencintai oppa dari dekat!")
            ),
            vocabNotes = listOf(
                "응원하다" to "Mendukung / cheering",
                "위로" to "Penghiburan / comfort",
                "챙겨 먹다" to "Menjaga pola makan teratur",
                "보라해" to "Borahae (I purple you / ungkapan cinta K-Pop)",
                "올림" to "Hormat saya (penutup surat sopan)"
            ),
            grammarTips = "Bentuk sopan permohonan santun: '-지 마세요' (아프지 마세요 = jangan sakit), dan janji santun: '-(으)ㄹ게요' (기도할게요 = saya akan berdoa)."
        ),

        // ==================== 2. OBROLAN CHAT / MESSENGER (대화 / 메신저) ====================
        KoreanDocumentItem(
            id = "chat_kakaotalk",
            titleId = "Obrolan Chat Janjian di Kafe Hongdae",
            titleKo = "홍대 카페 약속 카카오톡 대화",
            type = KoreanDocumentType.CHAT,
            categoryBadge = "💬 KakaoTalk Chat",
            iconEmoji = "📱",
            description = "Tangkapan layar obrolan pesan instan khas anak muda Korea dengan bahasa chat asli (ㅋㅋ, ㅎㅎ, ㅇㅇ, ㄱㅅ).",
            formalityLevel = "반말 (Chatting Santai)",
            recipient = "민수 & 하나 단톡방 (Grup Chat Minsu & Hana)",
            fullKoreanText = """
[오후 2:15] 민수: 하나야! 오늘 홍대에서 몇 시에 만날까? ㅎㅎ
[오후 2:16] 하나: 나 3시쯤 도착할 것 같아! 홍대입구역 9번 출구 어때?
[오후 2:17] 민수: 9번 출구 사람 너무 많아 ㅋㅋ 3번 출구 연남동 쪽 스타벅스 앞으로 와!
[오후 2:17] 하나: 아 그래? 알겠어 ㅇㅇ 거기 디저트 맛집 있더라!
[오후 2:18] 민수: 오 진짜? 내가 커피 살 테니까 네가 케이크 사 ㅋㅋㅋ
[오후 2:19] 하나: 콜! 늦지 마~ 이따 봐! 👋
            """.trimIndent(),
            fullTranslation = """
[14:15] Minsu: Hana! Hari ini di Hongdae kita mau ketemu jam berapa? hehe
[14:16] Hana: Kayaknya aku sampai sekitar jam 3! Gimana kalau di Stasiun Hongdae Pintu Keluar 9?
[14:17] Minsu: Pintu 9 ramai banget wkwk. Ke depan Starbucks Pintu 3 arah Yeonnam-dong aja!
[14:17] Hana: Oh gitu? Oke sip. Di sana ada tempat dessert enak lho!
[14:18] Minsu: Wah beneran? Aku yang traktir kopi, kamu yang beli kuenya ya wkwk
[14:19] Hana: Deal (Setuju)! Jangan telat ya~ Sampai ketemu nanti! 👋
            """.trimIndent(),
            sentences = listOf(
                DocumentSentence("c1", "오늘 홍대에서 몇 시에 만날까? ㅎㅎ", "o-neul hong-dae-e-seo myeot si-e man-nal-kka? hh", "Hari ini di Hongdae kita mau ketemu jam berapa? hehe", "민수"),
                DocumentSentence("c2", "나 3시쯤 도착할 것 같아! 9번 출구 어때?", "na se-si-jjeum do-chak-hal geot gat-a! gu-beon chul-gu eo-ttae?", "Aku sampai sekitar jam 3! Gimana kalau Pintu 9?", "하나"),
                DocumentSentence("c3", "9번 출구 사람 너무 많아 ㅋㅋ 3번 출구로 와!", "gu-beon chul-gu sa-ram neo-mu man-a kk se-beon chul-gu-ro wa!", "Pintu 9 ramai banget wkwk. Ke Pintu 3 aja!", "민수"),
                DocumentSentence("c4", "알겠어! 거기 디저트 맛집 있더라.", "al-gess-eo! geo-gi di-jeo-teu mat-jip it-deo-ra.", "Oke sip! Di situ katanya ada toko dessert enak.", "하나"),
                DocumentSentence("c5", "내가 커피 살 테니까 네가 케이크 사!", "nae-ga keo-pi sal te-ni-kka ne-ga ke-i-keu sa!", "Aku yang traktir kopi, kamu yang beli kuenya ya!", "민수"),
                DocumentSentence("c6", "콜! 늦지 마~ 이따 봐!", "kol! neut-ji ma~ it-tta bwa!", "Deal! Jangan telat ya~ Sampai nanti!", "하나")
            ),
            vocabNotes = listOf(
                "출구 (出口)" to "Pintu keluar stasiun (exit)",
                "맛집" to "Tempat makan / resto enak",
                "콜 (Call)" to "Slang Korea: Deal / Setuju!",
                "이따 봐" to "Sampai ketemu nanti (see you later)",
                "ㅋㅋ / ㅎㅎ" to "Tawa chat Korea (wkwk / hehe)"
            ),
            grammarTips = "Khas chat Korea: Singkatan 'ㅇㅇ' (응응 = iya/oke), '콜' (call/deal), dan pola janji traktir '-(으)ㄹ 테니까' (내가 살 테니까 = karena aku yang bayar)."
        ),

        KoreanDocumentItem(
            id = "chat_delivery",
            titleId = "Obrolan Memesan Makanan Delivery (Baemin)",
            titleKo = "배달의민족 음식 주문 대화",
            type = KoreanDocumentType.CHAT,
            categoryBadge = "🍗 Pesan Makanan",
            iconEmoji = "🛵",
            description = "Percakapan pesan antar makanan Korea: konfirmasi menu, alamat pengantaran, dan instruksi tanpa kontak.",
            formalityLevel = "존댓말 (Sopan Pelanggan)",
            recipient = "식당 사장님 (Pemilik Restoran Ayam)",
            fullKoreanText = """
손님: 사장님, 반반치킨 (후라이드 반, 양념 반) 한 마리 주문할게요.
사장님: 네, 손님! 떡볶이 추가하시겠어요? 지금 세트 할인 행사 중입니다.
손님: 네, 치즈 떡볶이 세트로 부탁드려요! 맵기는 보통으로 해 주세요.
사장님: 알겠습니다. 배달 주소가 서울시 마포구 맞으신가요?
손님: 네, 맞아요. 문 앞에 두고 벨 한 번만 눌러 주세요. 결제는 앱으로 완료했습니다!
사장님: 맛있게 조리해서 30분 내로 배달해 드리겠습니다. 감사합니다!
            """.trimIndent(),
            fullTranslation = """
Pelanggan: Pak Bos, saya mau pesan satu porsi ayam setengah-setengah (setengah original, setengah saus yangnyeom).
Penjual: Baik, pelanggan! Mau tambah tteokbokki? Sekarang sedang ada promo diskon paket.
Pelanggan: Ya, tolong buatkan paket tteokbokki keju! Tingkat kepedasannya sedang saja ya.
Penjual: Baik. Apakah alamat pengantaran benar di Mapo-gu, Seoul?
Pelanggan: Ya, benar. Tolong taruh di depan pintu dan tekan bel sekali saja ya. Pembayaran sudah selesai lewat aplikasi!
Penjual: Kami akan masak dengan lezat dan antar dalam 30 menit. Terima kasih!
            """.trimIndent(),
            sentences = listOf(
                DocumentSentence("d1", "반반치킨 한 마리 주문할게요.", "ban-ban-chi-kin han ma-ri ju-mun-hal-ge-yo.", "Saya mau pesan satu porsi ayam setengah-setengah.", "손님"),
                DocumentSentence("d2", "치즈 떡볶이 세트로 부탁드려요.", "chi-jeu tteok-bok-ki se-teu-ro bu-tak-deu-ryeo-yo.", "Tolong buatkan paket tteokbokki keju.", "손님"),
                DocumentSentence("d3", "맵기는 보통으로 해 주세요.", "maep-gi-neun bo-tong-eu-ro hae ju-se-yo.", "Tingkat kepedasannya sedang saja ya.", "손님"),
                DocumentSentence("d4", "문 앞에 두고 벨 한 번만 눌러 주세요.", "mun ap-e du-go bel han beon-man nul-leo ju-se-yo.", "Tolong taruh di depan pintu dan tekan bel sekali saja.", "손님"),
                DocumentSentence("d5", "맛있게 조리해서 배달해 드리겠습니다.", "mas-iss-ge jo-ri-hae-seo bae-dal-hae deu-ri-gess-seum-ni-da.", "Kami akan masak dengan lezat dan antarkan untuk Anda.", "사장님")
            ),
            vocabNotes = listOf(
                "반반치킨" to "Ayam goreng 2 rasa (crispy + bumbu saus manis pedas)",
                "맵기" to "Tingkat kepedasan",
                "배달 (配達)" to "Pengantaran makanan (delivery)",
                "문 앞" to "Depan pintu rumah",
                "보통" to "Sedang / standar (normal)"
            ),
            grammarTips = "Permintaan sopan di restoran/toko: 'Noun + (으)로 부탁드려요' (치즈 떡볶이 세트로 부탁드려요 = tolong buatkan paket tteokbokki keju)."
        ),

        // ==================== 3. CERITA & DIARY (이야기 / 동화 / 일기) ====================
        KoreanDocumentItem(
            id = "story_diary",
            titleId = "Catatan Harian: Musim Gugur di Seoul",
            titleKo = "서울의 맑은 가을 일기",
            type = KoreanDocumentType.STORY,
            categoryBadge = "📖 Catatan Diary",
            iconEmoji = "🍁",
            description = "Format penulisan buku harian (일기) asli Korea yang menceritakan jalan-jalan melihat dedaunan merah di Namsan.",
            formalityLevel = "일기 서술체 (Narasi Harian)",
            recipient = "나의 일기장 (Buku Harian Pribadi)",
            fullKoreanText = """
2026년 11월 1일 일요일 (날씨: 맑고 쾌청함)

오늘은 하늘이 파랗고 날씨가 정말 맑았다.
오랜만에 친구와 함께 남산서울타워에 다녀왔다.
산책로를 따라 걷는데 울긋불긋 단풍나무가 너무나 아름다웠다.
바람이 솔솔 불어서 기분도 상쾌했다.
전망대에 올라가 서울 시내 전체를 내려다보았다. 한강과 높은 빌딩들이 한눈에 보였다.
내려오는 길에 따뜻한 호떡을 사 먹었는데 달콤하고 맛있었다.
오늘 하루는 정말 행복하고 잊을 수 없는 날이다.
내일도 활기차게 시작하자!
            """.trimIndent(),
            fullTranslation = """
Minggu, 1 November 2026 (Cuaca: Cerah dan Sejuk Menyegarkan)

Hari ini langit sangat biru dan cuacanya begitu cerah.
Setelah sekian lama, aku pergi ke Menara N Seoul bersama teman.
Saat berjalan menyusuri jalur setapak, pepohonan maple yang berwarna merah kekuningan sangat indah.
Angin berhembus sepoi-sepoi membuat suasana hati begitu segar.
Kami naik ke dek observasi dan memandang seluruh kota Seoul dari atas. Sungai Han dan gedung-gedung pencakar langit terlihat dalam satu pandangan.
Dalam perjalanan turun, kami membeli dan makan hotteok hangat yang manis dan lezat.
Hari ini benar-benar hari yang membahagiakan dan tak terlupakan.
Ayo mulai hari esok dengan penuh semangat!
            """.trimIndent(),
            sentences = listOf(
                DocumentSentence("di1", "오늘은 하늘이 파랗고 날씨가 정말 맑았다.", "o-neul-eun ha-neul-i pa-rat-go nal-ssi-ga jeong-mal malg-at-da.", "Hari ini langit sangat biru dan cuacanya begitu cerah."),
                DocumentSentence("di2", "친구와 함께 남산서울타워에 다녀왔다.", "chin-gu-wa ham-kke nam-san-seo-ul-ta-wo-e da-nyeo-wat-da.", "Aku pergi ke Menara N Seoul bersama teman."),
                DocumentSentence("di3", "단풍나무가 너무나 아름다웠다.", "dan-pung-na-mu-ga neo-mu-na a-reum-da-wot-da.", "Pepohonan maple merah sangat indah."),
                DocumentSentence("di4", "따뜻한 호떡을 사 먹었는데 달콤했다.", "tta-tteut-han ho-tteok-eul sa meok-eot-neun-de dal-kom-haet-da.", "Aku membeli dan memakan hotteok hangat yang manis."),
                DocumentSentence("di5", "오늘 하루는 정말 행복하고 잊을 수 없는 날이다.", "o-neul ha-ru-neun jeong-mal haeng-bok-ha-go ij-eul su eop-neun nal-i-da.", "Hari ini benar-benar hari membahagiakan dan tak terlupakan.")
            ),
            vocabNotes = listOf(
                "단풍 (丹楓)" to "Dedaunan musim gugur yang memerah/kuning (autumn foliage)",
                "쾌청하다" to "Cerah ceria tanpa awan",
                "산책로" to "Jalur jalan santai / pedestrian trail",
                "호떡" to "Hotteok (pancake manis isi gula kayu manis khas Korea)",
                "한눈에" to "Dalam satu pandangan mata"
            ),
            grammarTips = "Khas tata bahasa penulisan buku harian/artikel (해라체/서술체): Menggunakan akhiran '-다' untuk masa lampau (맑았다, 다녀왔다, 아름다웠다) dan kata sifat/benda (날이다)."
        ),

        KoreanDocumentItem(
            id = "story_folktale",
            titleId = "Dongeng Cerita Rakyat: Kelinci & Kura-kura",
            titleKo = "전래 동화: 토끼와 거북이",
            type = KoreanDocumentType.STORY,
            categoryBadge = "🐢 Dongeng Klasik",
            iconEmoji = "📖",
            description = "Cerita dongeng klasik Korea yang mengajarkan ketekunan dan kerendahan hati.",
            formalityLevel = "동화 구연체 (Cerita Anak-anak)",
            recipient = "어린이와 학습자 (Anak-anak & Pelajar)",
            fullKoreanText = """
옛날 옛적 푸른 숲속에 토끼와 거북이가 살고 있었습니다.
토끼는 발이 아주 빨라서 늘 느린 거북이를 놀렸습니다.
"거북아, 너는 왜 그렇게 느리니? 나와 달리기 시합을 해 볼래?"
거북이는 포기하지 않고 미소를 지으며 승낙했습니다.
경주가 시작되자 토끼는 번개처럼 빠르게 앞서 나갔습니다.
언덕 위에 도착한 토끼는 뒤를 돌아보았습니다. 거북이는 아직 멀리 있었습니다.
"거북이가 오려면 한참 걸리겠군. 나무 그늘에서 잠깐 낮잠을 자야지."
토끼가 깊은 잠에 빠진 사이, 거북이는 쉬지 않고 한 걸음씩 묵묵히 걸어갔습니다.
마침내 거북이가 결승선에 먼저 도착했고, 잠에서 깬 토끼는 깜짝 놀라 후회했습니다.
성실함과 꾸준함이 자만심을 이긴 것입니다.
            """.trimIndent(),
            fullTranslation = """
Pada zaman dahulu kala di hutan hijau hiduplah seekor kelinci dan kura-kura.
Kelinci berlari sangat cepat sehingga selalu mengejek kura-kura yang lambat.
"Kura-kura, mengapa kamu begitu lambat? Mau lomba lari denganku?"
Kura-kura tidak menyerah dan menyetujui sambil tersenyum.
Begitu perlombaan dimulai, kelinci melesat maju secepat kilat.
Setibanya di atas bukit, kelinci menoleh ke belakang. Kura-kura masih jauh sekali di belakang.
"Pasti butuh waktu lama bagi kura-kura untuk sampai sini. Aku tidur siang sebentar saja di bawah bayang-bayang pohon."
Saat kelinci tertidur lelap, kura-kura terus melangkah maju tanpa henti secara tekun.
Akhirnya kura-kura tiba di garis akhir terlebih dahulu, dan kelinci yang baru terbangun terkejut lalu menyesal.
Ketekunan dan keuletan telah mengalahkan kesombongan.
            """.trimIndent(),
            sentences = listOf(
                DocumentSentence("st1", "옛날 옛적 푸른 숲속에 토끼와 거북이가 살았습니다.", "yennal yet-jeok pu-reun sup-sok-e tok-ki-wa geo-buk-i-ga sar-at-seum-ni-da.", "Pada zaman dahulu kala di hutan hijau hiduplah kelinci dan kura-kura."),
                DocumentSentence("st2", "경주가 시작되자 토끼는 빠르게 앞서 나갔습니다.", "gyeong-ju-ga si-jak-doe-ja tok-ki-neun ppa-reu-ge ap-seo na-gat-seum-ni-da.", "Begitu perlombaan dimulai, kelinci melesat maju dengan cepat."),
                DocumentSentence("st3", "토끼는 나무 그늘에서 잠깐 낮잠을 잤습니다.", "tok-ki-neun na-mu geu-neul-e-seo jam-kkan nat-jam-eul jat-seum-ni-da.", "Kelinci tidur siang sebentar di bawah rindang pohon."),
                DocumentSentence("st4", "거북이는 쉬지 않고 한 걸음씩 묵묵히 걸어갔습니다.", "geo-buk-i-neun swi-ji an-ko han geor-eum-ssik mung-muk-hi geor-eo-gat-seum-ni-da.", "Kura-kura tidak beristirahat dan terus melangkah maju."),
                DocumentSentence("st5", "성실함과 꾸준함이 자만심을 이겼습니다.", "seong-sil-ham-gwa kku-jun-ham-i ja-man-sim-eul i-gyeot-seum-ni-da.", "Ketekunan dan keuletan telah mengalahkan kesombongan.")
            ),
            vocabNotes = listOf(
                "옛날 옛적" to "Pada zaman dahulu kala (once upon a time)",
                "시합 / 경주" to "Perlombaan / kompetisi",
                "낮잠" to "Tidur siang (nap)",
                "성실함" to "Ketulusan & ketekunan",
                "꾸준함" to "Konsistensi / keuletan"
            ),
            grammarTips = "Struktur awal dongeng Korea selalu dibuka dengan ungkapan '옛날 옛적에' (Pada zaman dahulu kala), dan menggunakan tata bahasa narasi cerita '-(으)ㅂ니다' yang ritmis."
        ),

        // ==================== 4. DOKUMEN KEHIDUPAN NYATA (실생활 문서 / 서식) ====================
        KoreanDocumentItem(
            id = "doc_ktx_ticket",
            titleId = "Tiket Kereta Cepat KTX & Bukti Perjalanan",
            titleKo = "KTX 승차권 & 영수증",
            type = KoreanDocumentType.REAL_DOC,
            categoryBadge = "🚄 Tiket KTX",
            iconEmoji = "🎫",
            description = "Format resmi tiket kereta cepat Korea (Korail KTX) untuk perjalanan dari Stasiun Seoul ke Busan.",
            formalityLevel = "공식 서식 (Dokumen Resmi)",
            recipient = "승객 (Penumpang Kereta)",
            fullKoreanText = """
[코레일 KTX 영수권 / 승차권]
■ 열차번호: KTX 101호 (경부선)
■ 출발역: 서울역 (08:30 출발)
■ 도착역: 부산역 (11:05 도착)
■ 좌석정보: 5호차 12A석 (창측 / 순방향)
■ 승차인원: 어른 1명 (일반실)
■ 결제금액: 59,800원 (신용카드 일시불)
■ 유의사항: 열차 출발 5분 전까지 승강장에 입장해 주시기 바랍니다.
즐거운 여행 되십시오. 감사합니다.
            """.trimIndent(),
            fullTranslation = """
[Bukti Tiket / Karcis Korail KTX]
■ Nomor Kereta: KTX No. 101 (Jalur Gyeongbu)
■ Stasiun Berangkat: Stasiun Seoul (Berangkat pukul 08:30)
■ Stasiun Tiba: Stasiun Busan (Tiba pukul 11:05)
■ Info Kursi: Gerbong 5 Kursi 12A (Sisi Jendela / Arah Depan)
■ Jumlah Penumpang: 1 Dewasa (Kelas Standar)
■ Total Biaya: 59.800 Won (Kartu Kredit Bayar Penuh)
■ Catatan: Harap masuk ke peron kereta selambat-lambatnya 5 menit sebelum keberangkatan.
Semoga perjalanan Anda menyenangkan. Terima kasih.
            """.trimIndent(),
            sentences = listOf(
                DocumentSentence("k1", "출발역은 서울역이고 도착역은 부산역입니다.", "chul-bal-yeok-eun seo-ul-yeok-i-go do-chak-yeok-eun bu-san-yeok-im-ni-da.", "Stasiun keberangkatan adalah Seoul dan stasiun tujuan adalah Busan."),
                DocumentSentence("k2", "좌석은 5호차 12번 창측 좌석입니다.", "jwa-seok-eun o-ho-cha sib-i-beon chang-cheuk jwa-seok-im-ni-da.", "Kursi berada di Gerbong 5 Nomor 12 sisi jendela."),
                DocumentSentence("k3", "결제 금액은 오만 구천팔백 원입니다.", "gyeol-je geum-aek-eun o-man gu-cheon-pal-baek won-im-ni-da.", "Jumlah pembayaran adalah 59.800 won."),
                DocumentSentence("k4", "열차 출발 5분 전까지 승강장에 입장해 주세요.", "yeol-cha chul-bal o-bun jeon-kka-ji seung-gang-jang-e ip-jang-hae ju-se-yo.", "Harap masuk ke peron 5 menit sebelum keberangkatan."),
                DocumentSentence("k5", "즐거운 기차 여행 되십시오.", "jeul-geo-un gi-cha yeo-haeng doe-sip-si-o.", "Semoga perjalanan kereta Anda menyenangkan.")
            ),
            vocabNotes = listOf(
                "출발역 / 도착역" to "Stasiun keberangkatan / Stasiun kedatangan",
                "창측 / 복도측" to "Sisi jendela (window) / Sisi lorong (aisle)",
                "순방향 / 역방향" to "Menghadap ke depan / Menghadap ke belakang",
                "승강장 (昇降場)" to "Peron stasiun (platform)",
                "일시불" to "Bayar lunas sekaligus (lump-sum)"
            ),
            grammarTips = "Bahasa instruksi formal pada tiket kereta dan pengumuman umum: 'Verb + (으)십시오' (되십시오 = jadilah / semoga) dan 'Verb + (으)시기 바랍니다' (입장해 주시기 바랍니다 = dimohon untuk masuk)."
        ),

        KoreanDocumentItem(
            id = "doc_recipe",
            titleId = "Resep Masakan Asli: Kimchi Jjigae",
            titleKo = "김치찌개 황금 레시피",
            type = KoreanDocumentType.REAL_DOC,
            categoryBadge = "🍲 Resep Masakan",
            iconEmoji = "🍲",
            description = "Resep masakan sup kimchi daging babi khas rumah tangga Korea dengan instruksi langkah demi langkah.",
            formalityLevel = "요리 설명체 (Petunjuk Resep)",
            recipient = "요리 학습자 (Koki & Pelajar)",
            fullKoreanText = """
[맛있는 돼지고기 김치찌개 끓이기]

■ 필수 재료: 잘 익은 묵은지 300g, 돼지고기 목살 200g, 두부 반 모, 대파 반 뿌리.
■ 양념장: 고춧가루 1스푼, 다진 마늘 1스푼, 국간장 1스푼.

■ 조리 순서:
1. 냄비에 돼지고기와 김치를 넣고 참기름 한 방울과 함께 3분간 달달 볶습니다.
2. 고기가 반쯤 익으면 쌀뜨물 또는 물 500ml를 붓고 센 불에서 팔팔 끓입니다.
3. 찌개가 끓어오르면 다진 마늘과 고춧가루를 넣고 중불로 줄여 10분간 푹 끓입니다.
4. 마지막으로 썰어둔 두부와 대파를 얹고 2분간 더 끓여 완성합니다.
따끈한 밥과 함께 맛있게 드세요!
            """.trimIndent(),
            fullTranslation = """
[Cara Memasak Kimchi Jjigae Daging Babi yang Lezat]

■ Bahan Utama: 300g Kimchi matang (mukeunji), 200g daging babi bagian leher, setengah balok tahu, setengah batang daun bawang.
■ Bumbu: 1 sendok makan bubuk cabai (gochugaru), 1 sendok makan bawang putih cincang, 1 sendok makan kecap asin sup.

■ Langkah Memasak:
1. Masukkan daging babi dan kimchi ke dalam panci, tumis bersama setetes minyak wijen selama 3 menit.
2. Saat daging sudah setengah matang, tuang 500ml air cucian beras atau air biasa, lalu rebus dengan api besar hingga mendidih.
3. Saat kuah mendidih, masukkan bawang putih cincang dan bubuk cabai, kecilkan ke api sedang, lalu rebus perlahan selama 10 menit.
4. Terakhir, letakkan tahu dan daun bawang yang sudah diiris di atasnya, rebus lagi selama 2 menit hingga matang sempurna.
Selamat menikmati bersama nasi putih hangat!
            """.trimIndent(),
            sentences = listOf(
                DocumentSentence("r1", "냄비에 돼지고기와 김치를 넣고 볶습니다.", "naem-bi-e dwae-ji-go-gi-wa gim-chi-reul neo-ko bok-seum-ni-da.", "Masukkan daging dan kimchi ke dalam panci lalu tumis."),
                DocumentSentence("r2", "쌀뜨물을 붓고 센 불에서 끓입니다.", "ssal-tteu-mur-eul bu-ko sen bur-e-seo kkeul-im-ni-da.", "Tuang air cucian beras lalu rebus dengan api besar."),
                DocumentSentence("r3", "다진 마늘과 고춧가루를 넣습니다.", "da-jin ma-neul-gwa go-chut-ga-ru-reul neok-seum-ni-da.", "Masukkan bawang putih cincang dan bubuk cabai."),
                DocumentSentence("r4", "두부와 대파를 얹고 더 끓입니다.", "du-bu-wa dae-pa-reul eon-ko deo kkeul-im-ni-da.", "Letakkan tahu dan daun bawang lalu rebus lagi sebentar."),
                DocumentSentence("r5", "따뜻한 밥과 함께 맛있게 드세요!", "tta-tteut-han bap-gwa ham-kke mas-iss-ge deu-se-yo!", "Selamat menikmati bersama nasi hangat!")
            ),
            vocabNotes = listOf(
                "묵은지" to "Kimchi yang sudah terfermentasi lama (asam dan cocok untuk sup)",
                "쌀뜨물" to "Air cucian beras (bikin kuah gurih kental khas Korea)",
                "다진 마늘" to "Bawang putih cincang",
                "참기름" to "Minyak wijen",
                "맛있게 드세요" to "Selamat makan / selamat menikmati"
            ),
            grammarTips = "Kosa kata verba memasak Korea: 볶다 (menumis), 끓이다 (merebus), 붓다 (menuang), 썰다 (mengiris), 얹다 (meletakkan di atas)."
        )
    )
}
