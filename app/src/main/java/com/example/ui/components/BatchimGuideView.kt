package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.HaneulBlue
import com.example.ui.theme.HaneulContainer
import com.example.ui.theme.InkBlack
import com.example.ui.theme.NunbitYellow
import com.example.ui.theme.SaessakGreen
import com.example.ui.theme.SarangRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary

data class RepresentativeBatchim(
    val soundSymbol: String,
    val members: String,
    val description: String,
    val examples: List<BatchimWordExample>
)

data class BatchimWordExample(
    val writtenKo: String,
    val pronouncedKo: String,
    val romanization: String,
    val meaningId: String
)

data class PhonologyRule(
    val name: String,
    val koreanName: String,
    val description: String,
    val formula: String,
    val examples: List<BatchimWordExample>
)

data class BatchimQuizItem(
    val word: String,
    val meaning: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String
)

@Composable
fun BatchimGuideView(
    onSpeak: (String) -> Unit,
    onRecordProgress: (moduleId: String, lessonId: String, title: String, score: Int) -> Unit
) {
    var subTab by remember { mutableIntStateOf(0) } // 0: 7 Bunyi Pokok, 1: 4 Aturan Perubahan Bunyi, 2: Kuis Kilat

    val repBatchims = remember {
        listOf(
            RepresentativeBatchim(
                soundSymbol = "[ㄱ] (K/G)",
                members = "ㄱ, ㅋ, ㄲ, ㄳ, ㄺ",
                description = "Konsonan di kerongkongan tertutup tanpa pelepasan udara keras.",
                examples = listOf(
                    BatchimWordExample("책", "책 [chek]", "chek", "Buku"),
                    BatchimWordExample("부엌", "부억 [bueok]", "bu-eok", "Dapur"),
                    BatchimWordExample("밖", "박 [bak]", "bak", "Luar"),
                    BatchimWordExample("닭", "닥 [dak]", "dak", "Ayam")
                )
            ),
            RepresentativeBatchim(
                soundSymbol = "[ㄴ] (N)",
                members = "ㄴ, ㄵ, ㄶ",
                description = "Ujung lidah menempel pada pangkal gigi atas.",
                examples = listOf(
                    BatchimWordExample("눈", "눈 [nun]", "nun", "Mata / Salju"),
                    BatchimWordExample("산", "산 [san]", "san", "Gunung"),
                    BatchimWordExample("앉다", "안따 [an-tta]", "an-tta", "Duduk")
                )
            ),
            RepresentativeBatchim(
                soundSymbol = "[ㄷ] (T/D)",
                members = "ㄷ, ㅌ, ㅅ, ㅆ, ㅈ, ㅊ, ㅎ",
                description = "Kategori terbesar! Semua huruf desis dan letup ini dibaca 'T' saat di akhir suku kata.",
                examples = listOf(
                    BatchimWordExample("옷", "옫 [ot]", "ot", "Pakaian / Baju"),
                    BatchimWordExample("꽃", "꼳 [kkot]", "kkot", "Bunga"),
                    BatchimWordExample("끝", "끋 [kkeut]", "kkeut", "Akhir / Selesai"),
                    BatchimWordExample("낮", "낟 [nat]", "nat", "Siang hari"),
                    BatchimWordExample("있다", "읻따 [it-tta]", "it-tta", "Ada / Memiliki")
                )
            ),
            RepresentativeBatchim(
                soundSymbol = "[ㄹ] (L)",
                members = "ㄹ, ㄼ, ㄽ, ㄾ, ㅀ",
                description = "Ujung lidah melengkung menyentuh langit-langit mulut (bunyi L tebal).",
                examples = listOf(
                    BatchimWordExample("물", "물 [mul]", "mul", "Air"),
                    BatchimWordExample("달", "달 [dal]", "dal", "Bulan"),
                    BatchimWordExample("길", "길 [gil]", "gil", "Jalan / Rute"),
                    BatchimWordExample("여덟", "여덜 [yeodeol]", "yeo-deol", "Delapan (8)")
                )
            ),
            RepresentativeBatchim(
                soundSymbol = "[ㅁ] (M)",
                members = "ㅁ, ㄻ",
                description = "Bibir tertutup rapat menyalurkan suara lewat rongga hidung.",
                examples = listOf(
                    BatchimWordExample("밤", "밤 [bam]", "bam", "Malam / Buah berangan"),
                    BatchimWordExample("마음", "마음 [maeum]", "ma-eum", "Hati / Pikiran"),
                    BatchimWordExample("삶", "삼 [sam]", "sam", "Kehidupan")
                )
            ),
            RepresentativeBatchim(
                soundSymbol = "[ㅂ] (P/B)",
                members = "ㅂ, ㅍ, ㅄ, ㄿ",
                description = "Kedua bibir terkatup rapat menghentikan aliran udara.",
                examples = listOf(
                    BatchimWordExample("집", "집 [jip]", "jip", "Rumah"),
                    BatchimWordExample("앞", "압 [ap]", "ap", "Depan"),
                    BatchimWordExample("값", "갑 [gap]", "gap", "Harga")
                )
            ),
            RepresentativeBatchim(
                soundSymbol = "[ㅇ] (NG)",
                members = "ㅇ",
                description = "Hanya berbunyi 'NG' saat berada di bawah (batchim). Di awal suku kata tidak bersuara.",
                examples = listOf(
                    BatchimWordExample("방", "방 [bang]", "bang", "Kamar / Ruangan"),
                    BatchimWordExample("강", "강 [gang]", "gang", "Sungai"),
                    BatchimWordExample("사랑", "사랑 [sarang]", "sa-rang", "Cinta / Kasih sayang")
                )
            )
        )
    }

    val phonologyRules = remember {
        listOf(
            PhonologyRule(
                name = "1. Hukum Penghubung (Liaison)",
                koreanName = "연음 법칙 (Yeon-eum)",
                description = "Jika suku kata berakhiran konsonan (batchim) bertemu vokal (huruf ㅇ), konsonan tersebut langsung pindah menempati vokal tersebut.",
                formula = "Batchim + ㅇ (Vokal) ➔ Bunyi konsonan pindah ke atas",
                examples = listOf(
                    BatchimWordExample("한국어", "한구거 [hangugeo]", "hangugeo", "Bahasa Korea"),
                    BatchimWordExample("음악", "으막 [eumak]", "eu-mak", "Musik"),
                    BatchimWordExample("밥을", "바블 [babeul]", "ba-beul", "Nasi (objek)"),
                    BatchimWordExample("할아버지", "하라버지 [harabeoji]", "ha-ra-beo-ji", "Kakek")
                )
            ),
            PhonologyRule(
                name = "2. Hukum Nasalisasi (Pelebutan Sengau)",
                koreanName = "비음화 (Bi-eum-hwa)",
                description = "Ketika batchim letup (ㄱ, ㄷ, ㅂ) bertemu bunyi sengau (ㄴ, ㅁ), bunyinya melunak menjadi sengau (ㅇ, ㄴ, ㅁ) agar mudah diucapkan.",
                formula = "ㄱ/ㄷ/ㅂ + ㄴ/ㅁ ➔ ㅇ/ㄴ/ㅁ",
                examples = listOf(
                    BatchimWordExample("감사합니다", "감사함니다 [gamsahamnida]", "gam-sa-ham-ni-da", "Terima kasih"),
                    BatchimWordExample("한국말", "한궁말 [hangungmal]", "han-gung-mal", "Bahasa Korea (lisan)"),
                    BatchimWordExample("국물", "궁물 [gungmul]", "gung-mul", "Kuah sup"),
                    BatchimWordExample("십만", "심만 [simman]", "sim-man", "100.000 (Seratus ribu)")
                )
            ),
            PhonologyRule(
                name = "3. Hukum Aspirasi (Letupan Nafas)",
                koreanName = "격음화 (Gyeok-eum-hwa)",
                description = "Ketika konsonan biasa (ㄱ, ㄷ, ㅂ, ㅈ) bertemu atau didahului ㅎ, keduanya bergabung menjadi letupan nafas keras (ㅋ, ㅌ, ㅍ, ㅊ).",
                formula = "ㄱ/ㄷ/ㅂ/ㅈ + ㅎ ➔ ㅋ/ㅌ/ㅍ/ㅊ",
                examples = listOf(
                    BatchimWordExample("축하", "추카 [chuka]", "chu-ka", "Selamat / Perayaan"),
                    BatchimWordExample("좋다", "조타 [jota]", "jo-ta", "Bagus / Baik"),
                    BatchimWordExample("입학", "이팍 [ipak]", "i-pak", "Masuk sekolah/kuliah"),
                    BatchimWordExample("맞히다", "마치다 [machida]", "ma-chi-da", "Menebak dengan benar")
                )
            ),
            PhonologyRule(
                name = "4. Hukum Penegangan (Tensification)",
                koreanName = "경음화 (Gyeong-eum-hwa)",
                description = "Konsonan lembut (ㄱ, ㄷ, ㅂ, ㅅ, ㅈ) berubah menjadi konsonan ganda tegang (ㄲ, ㄸ, ㅃ, ㅆ, ㅉ) saat berada setelah batchim letup.",
                formula = "ㄱ/ㄷ/ㅂ + ㄱ/ㄷ/ㅂ/ㅅ/ㅈ ➔ ㄲ/ㄸ/ㅃ/ㅆ/ㅉ",
                examples = listOf(
                    BatchimWordExample("학교", "학꾜 [hak-kkyo]", "hak-kkyo", "Sekolah"),
                    BatchimWordExample("식당", "식땅 [sik-ttang]", "sik-ttang", "Restoran"),
                    BatchimWordExample("문법", "문뻡 [mun-ppeop]", "mun-ppeop", "Tata bahasa")
                )
            )
        )
    }

    val quizItems = remember {
        listOf(
            BatchimQuizItem(
                word = "한국어",
                meaning = "Bahasa Korea",
                options = listOf("[한구거] (Hangugeo)", "[한국어] (Hanguk-eo)", "[한곡어] (Hangogeo)", "[한구어] (Hangu-eo)"),
                correctIndex = 0,
                explanation = "Hukum Yeon-eum (Liaison): batchim 'ㄱ' pindah ke vokal '어' menjadi '거' sehingga dibaca [한구거]."
            ),
            BatchimQuizItem(
                word = "감사합니다",
                meaning = "Terima kasih",
                options = listOf("[감사합니당]", "[감사함니다] (Gamsahamnida)", "[감사밥니다]", "[감사합디다]"),
                correctIndex = 1,
                explanation = "Hukum Nasalisasi: batchim 'ㅂ' bertemu 'ㄴ' berubah bunyi menjadi 'ㅁ' sehingga diucapkan [감사함니다]."
            ),
            BatchimQuizItem(
                word = "축하해요",
                meaning = "Selamat (ucapan selamat)",
                options = listOf("[축하해요]", "[추카해요] (Chukahaeyo)", "[추하해요]", "[축카해요]"),
                correctIndex = 1,
                explanation = "Hukum Aspirasi: 'ㄱ' bertemu 'ㅎ' menghasilkan letupan 'ㅋ' sehingga berbunyi [추카해요]."
            ),
            BatchimQuizItem(
                word = "옷",
                meaning = "Pakaian / Baju",
                options = listOf("[오스]", "[옥]", "[옫] (Ot)", "[옹]"),
                correctIndex = 2,
                explanation = "Huruf 'ㅅ' sebagai batchim tunggal termasuk dalam 7 Bunyi Pokok [ㄷ] sehingga dibaca [옫] / [ot]."
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        // Sub-tabs for Batchim Section
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val tabs = listOf("7 Bunyi Pokok", "4 Aturan Perubahan", "Kuis Kilat Batchim ⚡")
            tabs.forEachIndexed { idx, title ->
                val isSel = subTab == idx
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSel) HaneulBlue else Color.White,
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(if (isSel) HaneulBlue else Color(0xFFE2E8F0))
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { subTab = idx }
                        .testTag("batchim_subtab_$idx")
                ) {
                    Text(
                        text = title,
                        fontSize = 11.sp,
                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSel) Color.White else InkBlack,
                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        when (subTab) {
            0 -> {
                // 7 Bunyi Pokok View
                var selectedRepIndex by remember { mutableIntStateOf(0) }
                val currentRep = repBatchims[selectedRepIndex]

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFE2E8F0))
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = HaneulContainer,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("받", fontWeight = FontWeight.Bold, color = HaneulBlue, fontSize = 16.sp)
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "7 Bunyi Pokok Batchim (7 대표받침)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = InkBlack
                                )
                                Text(
                                    text = "Meskipun ada 27 variasi batchim, semuanya disederhanakan ke 7 bunyi pokok!",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Horizontal list of 7 sounds
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(repBatchims.indices.toList()) { idx ->
                                val rep = repBatchims[idx]
                                val isSel = selectedRepIndex == idx
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSel) HaneulBlue else Color(0xFFF1F5F9),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { selectedRepIndex = idx }
                                        .testTag("rep_batchim_tab_$idx")
                                ) {
                                    Text(
                                        text = rep.soundSymbol,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSel) Color.White else InkBlack,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Detail of selected sound
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFEFF6FF),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "Huruf yang masuk kelompok ini: ${currentRep.members}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = HaneulBlue
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = currentRep.description,
                                    fontSize = 12.sp,
                                    color = InkBlack
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Contoh Kata & Pelafalan (Tekan untuk Dengar):",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = InkBlack
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        currentRep.examples.forEach { eg ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFF8FAFC),
                                border = CardDefaults.outlinedCardBorder().copy(
                                    brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFE2E8F0))
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable { onSpeak(eg.writtenKo) }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        AudioSpeakerButton(onClick = { onSpeak(eg.writtenKo) })
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = eg.writtenKo,
                                                    fontSize = 17.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = InkBlack
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "dibaca ${eg.pronouncedKo}",
                                                    fontSize = 12.sp,
                                                    color = HaneulBlue,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                            Text(
                                                text = "${eg.meaningId} (${eg.romanization})",
                                                fontSize = 12.sp,
                                                color = TextSecondary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            1 -> {
                // 4 Aturan Perubahan Bunyi
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    phonologyRules.forEach { rule ->
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFE2E8F0))
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = rule.name,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = InkBlack
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = NunbitYellow.copy(alpha = 0.25f)
                                    ) {
                                        Text(
                                            text = rule.koreanName,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF854D0E),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = rule.description,
                                    fontSize = 12.sp,
                                    color = TextSecondary,
                                    lineHeight = 17.sp
                                )

                                Spacer(modifier = Modifier.height(10.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFF1F5F9),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "⚡ Rumus: ${rule.formula}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = HaneulBlue,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Contoh Perubahan Suara:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = InkBlack
                                )
                                Spacer(modifier = Modifier.height(4.dp))

                                rule.examples.forEach { ex ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 3.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFFF8FAFC))
                                            .clickable { onSpeak(ex.writtenKo) }
                                            .padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                Icons.Default.VolumeUp,
                                                contentDescription = "Dengar",
                                                tint = HaneulBlue,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "${ex.writtenKo} ➔ ${ex.pronouncedKo}",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = InkBlack
                                            )
                                        }
                                        Text(
                                            text = ex.meaningId,
                                            fontSize = 11.sp,
                                            color = TextSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            2 -> {
                // Kuis Kilat Batchim
                var currentQuizIndex by remember { mutableIntStateOf(0) }
                var selectedAnswer by remember { mutableStateOf<Int?>(null) }
                var quizFinished by remember { mutableStateOf(false) }
                var scoreCount by remember { mutableIntStateOf(0) }

                val currentQuiz = quizItems[currentQuizIndex]

                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFE2E8F0))
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        if (!quizFinished) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Pertanyaan ${currentQuizIndex + 1} dari ${quizItems.size}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = HaneulBlue
                                )
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = NunbitYellow.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "+15 XP per jawaban benar",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF854D0E),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Large Word Card
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color(0xFFEFF6FF),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(18.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = currentQuiz.word,
                                        fontSize = 32.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = InkBlack
                                    )
                                    Text(
                                        text = "Arti: ${currentQuiz.meaning}",
                                        fontSize = 13.sp,
                                        color = TextSecondary
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = { onSpeak(currentQuiz.word) },
                                        colors = ButtonDefaults.buttonColors(containerColor = HaneulBlue),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                    ) {
                                        Icon(Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Dengarkan Suara", fontSize = 11.sp)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "Bagaimanakah pelafalan yang tepat dari kata di atas?",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = InkBlack
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            // Options
                            currentQuiz.options.forEachIndexed { optIdx, optText ->
                                val isSelected = selectedAnswer == optIdx
                                val isCorrect = optIdx == currentQuiz.correctIndex
                                val showResult = selectedAnswer != null

                                val btnColor = when {
                                    !showResult -> Color.White
                                    isCorrect -> Color(0xFFD1FAE5)
                                    isSelected -> Color(0xFFFEE2E2)
                                    else -> Color.White
                                }

                                val borderColor = when {
                                    !showResult && isSelected -> HaneulBlue
                                    showResult && isCorrect -> SaessakGreen
                                    showResult && isSelected -> SarangRed
                                    else -> Color(0xFFE2E8F0)
                                }

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = btnColor,
                                    border = CardDefaults.outlinedCardBorder().copy(
                                        brush = androidx.compose.ui.graphics.SolidColor(borderColor)
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable(enabled = selectedAnswer == null) {
                                            selectedAnswer = optIdx
                                            if (optIdx == currentQuiz.correctIndex) {
                                                scoreCount += 1
                                            }
                                        }
                                        .testTag("batchim_quiz_opt_$optIdx")
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = if (isSelected || (showResult && isCorrect)) HaneulBlue else Color(0xFFF1F5F9),
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = ('A' + optIdx).toString(),
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isSelected || (showResult && isCorrect)) Color.White else InkBlack
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = optText,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = InkBlack
                                        )
                                    }
                                }
                            }

                            // Explanation Card
                            AnimatedVisibility(visible = selectedAnswer != null) {
                                Column(modifier = Modifier.padding(top = 10.dp)) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (selectedAnswer == currentQuiz.correctIndex) Color(0xFFECFDF5) else Color(0xFFFFFBEB),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "💡 Penjelasan: ${currentQuiz.explanation}",
                                            fontSize = 12.sp,
                                            color = InkBlack,
                                            modifier = Modifier.padding(10.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Button(
                                        onClick = {
                                            if (currentQuizIndex < quizItems.size - 1) {
                                                currentQuizIndex += 1
                                                selectedAnswer = null
                                            } else {
                                                quizFinished = true
                                                val finalScore = (scoreCount * 100) / quizItems.size
                                                onRecordProgress("writing", "batchim_quiz", "Kuis Pemahaman Batchim", finalScore)
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = HaneulBlue),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("batchim_quiz_next_button")
                                    ) {
                                        Text(
                                            text = if (currentQuizIndex < quizItems.size - 1) "Lanjut ke Soal Berikutnya" else "Lihat Hasil Kuis",
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        } else {
                            // Quiz Completed Screen
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = SaessakGreen,
                                    modifier = Modifier.size(54.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Luar Biasa! Kuis Batchim Tuntas! 🎉",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = InkBlack
                                )
                                Text(
                                    text = "Benar $scoreCount dari ${quizItems.size} soal (${(scoreCount * 100) / quizItems.size}%)",
                                    fontSize = 13.sp,
                                    color = TextSecondary
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Button(
                                    onClick = {
                                        currentQuizIndex = 0
                                        selectedAnswer = null
                                        quizFinished = false
                                        scoreCount = 0
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = HaneulBlue),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Ulangi Kuis Batchim", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
