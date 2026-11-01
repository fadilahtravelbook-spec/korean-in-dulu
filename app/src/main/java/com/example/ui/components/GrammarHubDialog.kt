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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.HaneulBlue
import com.example.ui.theme.HaneulContainer
import com.example.ui.theme.InkBlack
import com.example.ui.theme.NunbitYellow
import com.example.ui.theme.SaessakGreen
import com.example.ui.theme.TextSecondary

data class GrammarPattern(
    val title: String,
    val category: String,
    val formula: String,
    val explanation: String,
    val examples: List<GrammarExample>
)

data class GrammarExample(
    val sentenceKo: String,
    val sentenceRo: String,
    val sentenceId: String
)

data class NumberItem(
    val arabic: String,
    val sinoKo: String,
    val sinoRo: String,
    val nativeKo: String,
    val nativeRo: String,
    val nativeUnitForm: String = ""
)

data class LargeNumberItem(
    val arabic: String,
    val sinoKo: String,
    val sinoRo: String,
    val meaningId: String
)

data class ClassifierItem(
    val unitKo: String,
    val unitRo: String,
    val category: String,
    val exampleKo: String,
    val exampleRo: String,
    val exampleId: String
)

data class GrammarQuiz(
    val question: String,
    val sentenceWithBlank: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String
)

@Composable
fun GrammarHubDialog(
    onDismiss: () -> Unit,
    onSpeak: (String) -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.94f)
                .clip(RoundedCornerShape(24.dp)),
            color = Color(0xFFF8FAFC)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(HaneulBlue)
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = NunbitYellow,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("문", fontWeight = FontWeight.Black, color = InkBlack, fontSize = 16.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Tata Bahasa & Sistem Angka",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color.White
                            )
                            Text(
                                text = "Sino-Korea, Asli Korea, Partikel & SOV",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_grammar_dialog_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup", tint = Color.White)
                    }
                }

                GrammarHubContent(onSpeak = onSpeak)
            }
        }
    }
}

@Composable
fun GrammarHubContent(
    onSpeak: (String) -> Unit
) {
    // 0: Pola & Partikel, 1: Akhiran Sopan, 2: Sistem Angka (숫자), 3: Kuis Cepat ⚡
    var selectedSection by remember { mutableIntStateOf(0) }

    val patterns = remember {
        listOf(
            GrammarPattern(
                title = "Struktur Kalimat: S - O - P (SOV)",
                category = "Pondasi Dasar",
                formula = "[Subjek] + [Objek] + [Predikat/Kata Kerja]",
                explanation = "Berbeda dengan bahasa Indonesia (S-P-O), kata kerja dalam bahasa Korea SELALU berada di akhir kalimat.",
                examples = listOf(
                    GrammarExample("저는 사과를 먹어요.", "Jeoneun sagwareul meogeoyo.", "Saya makan apel. (Saya + Apel + Makan)"),
                    GrammarExample("친구가 한국어를 공부해요.", "Chinguga hangugeoreul gongbuhaeyo.", "Teman belajar bahasa Korea. (Teman + Bhs Korea + Belajar)")
                )
            ),
            GrammarPattern(
                title = "Partikel Topik: 은 / 는 (eun / neun)",
                category = "Partikel (조사)",
                formula = "Konsonan + 은 | Vokal + 는",
                explanation = "Menandai topik pembicaraan utama atau memberikan nuansa kontras/penekanan.",
                examples = listOf(
                    GrammarExample("저는 인도네시아 사람입니다.", "Jeoneun indonesia saram-imnida.", "Saya adalah orang Indonesia. (저 berakhiran vokal ➔ 는)"),
                    GrammarExample("이 책은 재미있어요.", "I chaegeun jaemi-isseoyo.", "Buku ini menarik. (책 berakhiran konsonan ➔ 은)")
                )
            ),
            GrammarPattern(
                title = "Partikel Subjek: 이 / 가 (i / ga)",
                category = "Partikel (조사)",
                formula = "Konsonan + 이 | Vokal + 가",
                explanation = "Menandai pelaku yang melakukan tindakan spesifik atau menjawab pertanyaan 'Siapa / Apa yang...'.",
                examples = listOf(
                    GrammarExample("날씨가 좋아요.", "Nalssiga joayo.", "Cuacanya bagus. (날씨 berakhiran vokal ➔ 가)"),
                    GrammarExample("비가 와요.", "Biga wayo.", "Hujan turun."),
                    GrammarExample("선생님이 오셨어요.", "Seonsaengnimi osyeosseoyo.", "Guru telah datang. (선생님 berakhiran konsonan ➔ 이)")
                )
            ),
            GrammarPattern(
                title = "Partikel Objek: 을 / 를 (eul / reul)",
                category = "Partikel (조사)",
                formula = "Konsonan + 을 | Vokal + 를",
                explanation = "Menempel pada kata benda yang menjadi sasaran langsung tindakan kata kerja.",
                examples = listOf(
                    GrammarExample("밥을 먹어요.", "Babeul meogeoyo.", "Makan nasi. (밥 berakhiran konsonan ➔ 을)"),
                    GrammarExample("커피를 마셔요.", "Keopireul masyeoyo.", "Minum kopi. (커피 berakhiran vokal ➔ 를)")
                )
            ),
            GrammarPattern(
                title = "Partikel Tempat: 에 vs 에서 (e vs eseo)",
                category = "Partikel (조사)",
                formula = "Tujuan/Waktu: [Tempat] + 에 | Lokasi Aktivitas: [Tempat] + 에서",
                explanation = "'에' dipakai bersama kata kerja arah/keberadaan (가다, 오다, 있다). '에서' dipakai saat melakukan kegiatan aktif di tempat tersebut.",
                examples = listOf(
                    GrammarExample("학교에 가요.", "Hakgyoe gayo.", "Pergi ke sekolah. (Tujuan pergerakan)"),
                    GrammarExample("카페에서 친구를 만나요.", "Kape-eseo chingureul mannayo.", "Bertemu teman di cafe. (Lokasi aktivitas)")
                )
            ),
            GrammarPattern(
                title = "Akhiran Sopan: -아요 / -어요 / -해요",
                category = "Akhiran Predikat",
                formula = "Vokal ㅏ/ㅗ ➔ -아요 | Vokal lain ➔ -어요 | 하다 ➔ -해요",
                explanation = "Bentuk sopan santai (Haeyo-che) yang paling umum digunakan dalam kehidupan sehari-hari.",
                examples = listOf(
                    GrammarExample("가다 ➔ 가요", "Gada -> Gayo", "Pergi (Vokal ㅏ ➔ -아요)"),
                    GrammarExample("먹다 ➔ 먹어요", "Meokda -> Meogeoyo", "Makan (Vokal ㅓ ➔ -어요)"),
                    GrammarExample("공부하다 ➔ 공부해요", "Gongbuhada -> Gongbuhaeyo", "Belajar (하다 ➔ 해요)")
                )
            ),
            GrammarPattern(
                title = "Akhiran Formal: -ㅂ니다 / -습니다",
                category = "Akhiran Predikat",
                formula = "Vokal + ㅂ니다 | Konsonan + 습니다",
                explanation = "Bentuk sangat formal dan hormat (Hasipsio-che) digunakan di berita, pidato, militer, atau saat bertemu pertama kali.",
                examples = listOf(
                    GrammarExample("감사합니다.", "Gamsahamnida.", "Terima kasih."),
                    GrammarExample("만나서 반갑습니다.", "Mannaseo bangapseumnida.", "Senang bertemu dengan Anda.")
                )
            )
        )
    }

    val numberTable = remember {
        listOf(
            NumberItem("1", "일", "il", "하나", "hana", "한 (sebelum satuan)"),
            NumberItem("2", "이", "i", "둘", "dul", "두 (sebelum satuan)"),
            NumberItem("3", "삼", "sam", "셋", "set", "세 (sebelum satuan)"),
            NumberItem("4", "사", "sa", "넷", "net", "네 (sebelum satuan)"),
            NumberItem("5", "오", "o", "다섯", "daseot"),
            NumberItem("6", "육", "yuk", "여섯", "yeoseot"),
            NumberItem("7", "칠", "chil", "일곱", "ilgop"),
            NumberItem("8", "팔", "pal", "여덟", "yeodeol"),
            NumberItem("9", "구", "gu", "아홉", "ahop"),
            NumberItem("10", "십", "sip", "열", "yeol")
        )
    }

    val largeNumbers = remember {
        listOf(
            LargeNumberItem("100", "백", "baek", "Seratus"),
            LargeNumberItem("1.000", "천", "cheon", "Seribu"),
            LargeNumberItem("10.000", "만", "man", "Sepuluh ribu (basis 4 digit)"),
            LargeNumberItem("100.000", "십만", "simman", "Seratus ribu"),
            LargeNumberItem("1.000.000", "백만", "baengman", "Satu juta"),
            LargeNumberItem("10.000.000", "천만", "cheonman", "Sepuluh juta"),
            LargeNumberItem("100.000.000", "억", "eok", "Seratus juta")
        )
    }

    val nativeTens = remember {
        listOf(
            NumberItem("20", "이십 (Sino)", "isip", "스물", "seumul", "스무 (sebelum satuan)"),
            NumberItem("30", "삼십 (Sino)", "samsip", "서른", "seoreun"),
            NumberItem("40", "사십 (Sino)", "sasip", "마흔", "maheun"),
            NumberItem("50", "오십 (Sino)", "osip", "쉰", "swin")
        )
    }

    val classifiers = remember {
        listOf(
            ClassifierItem("개", "gae", "Benda Umum & Buah", "사과 두 개", "sagwa du gae", "2 buah apel"),
            ClassifierItem("명", "myeong", "Orang (Biasa)", "학생 세 명", "haksaeng se myeong", "3 orang siswa"),
            ClassifierItem("분", "bun", "Orang (Hormat)", "손님 두 분", "sonnim du bun", "2 orang tamu yang terhormat"),
            ClassifierItem("잔", "jan", "Cangkir / Gelas", "커피 한 잔", "keopi han jan", "1 cangkir kopi"),
            ClassifierItem("병", "byeong", "Botol", "물 네 병", "mul ne byeong", "4 botol air"),
            ClassifierItem("마리", "mari", "Hewan", "고양이 한 마리", "goyangi han mari", "1 ekor kucing"),
            ClassifierItem("권", "gwon", "Buku / Jilid", "책 다섯 권", "chaek daseot gwon", "5 buku"),
            ClassifierItem("장", "jang", "Lembar / Tiket", "표 두 장", "pyo du jang", "2 lembar tiket"),
            ClassifierItem("대", "dae", "Kendaraan & Elektronik", "차 한 대", "cha han dae", "1 unit mobil"),
            ClassifierItem("살", "sal", "Umur (Tahun)", "스무 살", "seumu sal", "Berusia 20 tahun"),
            ClassifierItem("원", "won", "Mata Uang Won (Sino)", "만 원", "man won", "10.000 Won"),
            ClassifierItem("층", "cheung", "Lantai Gedung (Sino)", "삼 층", "sam cheung", "Lantai 3")
        )
    }

    val grammarQuizzes = remember {
        listOf(
            GrammarQuiz(
                question = "Pilihlah partikel subjek yang tepat untuk melengkapi kalimat:",
                sentenceWithBlank = "민우___ 노래를 잘해요. (Minwoo bernyanyi dengan merdu)",
                options = listOf("는", "가", "를", "에서"),
                correctIndex = 1,
                explanation = "'민우' berakhiran huruf vokal '우', sehingga memakai partikel subjek '가' (민우가)."
            ),
            GrammarQuiz(
                question = "Bagaimana membaca pukul 03:15 (Jam 3 lewat 15 menit) dalam bahasa Korea?",
                sentenceWithBlank = "03:15 ➔ ________?",
                options = listOf("삼 시 십오 분", "세 시 십오 분", "셋 시 십오 분", "세 시 열다섯 분"),
                correctIndex = 1,
                explanation = "Jam (시) wajib memakai Angka Asli (셋 ➔ 세 시), sedangkan Menit (분) wajib memakai Angka Sino (십오 분). Hasilnya: '세 시 십오 분'."
            ),
            GrammarQuiz(
                question = "Bagaimana cara memesan '2 cangkir kopi' dengan satuan hitung yang benar?",
                sentenceWithBlank = "커피 ________ 주세요. (Tolong berikan 2 cangkir kopi)",
                options = listOf("두 개", "이 잔", "두 잔", "둘 잔"),
                correctIndex = 2,
                explanation = "Cangkir memakai satuan '잔', dan angka asli 2 (둘) berkontraksi menjadi '두' sebelum satuan hitung. Jadi: '두 잔'."
            ),
            GrammarQuiz(
                question = "Lengkapilah dengan partikel tempat aktivitas yang tepat:",
                sentenceWithBlank = "도서관___ 책을 읽어요. (Saya membaca buku di perpustakaan)",
                options = listOf("에", "에서", "을", "과"),
                correctIndex = 1,
                explanation = "Membaca buku adalah aktivitas tindakan aktif, sehingga partikel lokasi yang benar adalah '에서' (도서관에서)."
            ),
            GrammarQuiz(
                question = "Angka Sino-Korea untuk uang '10.000 Won' adalah:",
                sentenceWithBlank = "10.000 Won ➔ ________?",
                options = listOf("열 원", "일만 원 / 만 원", "천 원", "십천 원"),
                correctIndex = 1,
                explanation = "Uang menggunakan sistem angka Sino-Korea dengan basis 4 digit (10.000 = 만). Jadi 10.000 Won disebut '만 원' (man won)."
            ),
            GrammarQuiz(
                question = "Bagaimanakah perubahan bentuk kata kerja '보다' (Melihat/Menonton) ke bentuk sopan -아요/어요?",
                sentenceWithBlank = "영화를 _____? (Menonton film)",
                options = listOf("보어요", "봐요", "보해요", "봅니다요"),
                correctIndex = 1,
                explanation = "'보다' memiliki vokal 'ㅗ', digabung dengan '-아요' berkontraksi menjadi '봐요' (bwayo)."
            )
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {

                // Tab Selector
                TabRow(
                    selectedTabIndex = selectedSection,
                    containerColor = Color.White,
                    contentColor = HaneulBlue
                ) {
                    Tab(
                        selected = selectedSection == 0,
                        onClick = { selectedSection = 0 },
                        text = { Text("Pola & Partikel", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedSection == 1,
                        onClick = { selectedSection = 1 },
                        text = { Text("Akhiran Sopan", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedSection == 2,
                        onClick = { selectedSection = 2 },
                        text = { Text("Angka (숫자)", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedSection == 3,
                        onClick = { selectedSection = 3 },
                        text = { Text("Kuis ⚡", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    )
                }

                when (selectedSection) {
                    0, 1 -> {
                        val filterCat = if (selectedSection == 0) listOf("Pondasi Dasar", "Partikel (조사)") else listOf("Akhiran Predikat")
                        val filteredPatterns = patterns.filter { it.category in filterCat }

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 14.dp),
                            contentPadding = PaddingValues(vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(filteredPatterns) { pat ->
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    border = CardDefaults.outlinedCardBorder().copy(
                                        brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFE2E8F0))
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = pat.title,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = InkBlack
                                            )
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = HaneulContainer
                                            ) {
                                                Text(
                                                    text = pat.category,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = HaneulBlue,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color(0xFFF1F5F9),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = "Rumus: ${pat.formula}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = HaneulBlue,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = pat.explanation,
                                            fontSize = 12.sp,
                                            color = TextSecondary,
                                            lineHeight = 17.sp
                                        )

                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text(
                                            text = "Contoh Kalimat (Tekan untuk Dengar):",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = InkBlack
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))

                                        pat.examples.forEach { eg ->
                                            Surface(
                                                shape = RoundedCornerShape(10.dp),
                                                color = Color(0xFFF8FAFC),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 3.dp)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .clickable { onSpeak(eg.sentenceKo) }
                                                    .padding(8.dp)
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        Icons.AutoMirrored.Filled.VolumeUp,
                                                        contentDescription = "Dengar",
                                                        tint = HaneulBlue,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Column {
                                                        Text(
                                                            text = eg.sentenceKo,
                                                            fontSize = 13.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = InkBlack
                                                        )
                                                        Text(
                                                            text = eg.sentenceId,
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
                        }
                    }

                    2 -> {
                        // Section 2: Sistem Angka Korea Lengkap (Sino vs Asli + Waktu + Satuan Hitung)
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 14.dp),
                            contentPadding = PaddingValues(vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // 1. Pengantar Dua Sistem Angka
                            item {
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                                    border = CardDefaults.outlinedCardBorder().copy(
                                        brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFBFDBFE))
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Tag, contentDescription = null, tint = HaneulBlue, modifier = Modifier.size(20.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Dua Sistem Angka Bahasa Korea",
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 14.sp,
                                                color = HaneulBlue
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "Bahasa Korea memiliki dua sistem angka paralel yang penggunaannya tidak boleh tertukar:",
                                            fontSize = 12.sp,
                                            color = InkBlack,
                                            lineHeight = 16.sp
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "1. Angka Sino-Korea (한자어): Dari karakter Tiongkok (일, 이, 삼, 사...). Digunakan untuk uang (원), menit & detik (분/초), tanggal/tahun, nomor telepon, dan lantai gedung (층).",
                                            fontSize = 11.sp,
                                            color = TextSecondary,
                                            lineHeight = 15.sp
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "2. Angka Asli Korea (순우리말): Dari bahasa asli Korea (하나, 둘, 셋, 넷...). Digunakan untuk menghitung kuantitas benda/orang bersama satuan hitung (개, 명, 잔...), jam waktu (시), dan umur (살).",
                                            fontSize = 11.sp,
                                            color = TextSecondary,
                                            lineHeight = 15.sp
                                        )
                                    }
                                }
                            }

                            // 2. Catatan Khusus Perubahan Bentuk Satuan Hitung
                            item {
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = Color(0xFFFEF3C7),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFCD34D)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = "⚠️ Perubahan Bentuk Sebelum Satuan Hitung:",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFF92400E)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Angka Asli 1, 2, 3, 4, dan 20 berubah bentuk jika bertemu satuan hitung:\n" +
                                                    "• 하나 ➔ 한 (한 개 = 1 buah)\n" +
                                                    "• 둘 ➔ 두 (두 명 = 2 orang)\n" +
                                                    "• 셋 ➔ 세 (세 잔 = 3 cangkir)\n" +
                                                    "• 넷 ➔ 네 (네 마리 = 4 ekor)\n" +
                                                    "• 스물 ➔ 스무 (스무 살 = 20 tahun)",
                                            fontSize = 11.sp,
                                            color = Color(0xFF78350F),
                                            lineHeight = 16.sp
                                        )
                                    }
                                }
                            }

                            // 3. Tabel Perbandingan Angka 1 - 10
                            item {
                                Text(
                                    text = "Perbandingan Angka 1 – 10 (Tekan untuk Dengar)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = InkBlack
                                )
                            }

                            items(numberTable) { num ->
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    border = CardDefaults.outlinedCardBorder().copy(
                                        brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFE2E8F0))
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        // Lingkaran Angka Arab
                                        Surface(
                                            shape = CircleShape,
                                            color = HaneulContainer,
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(num.arabic, fontWeight = FontWeight.Bold, color = HaneulBlue, fontSize = 14.sp)
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(10.dp))

                                        // Kolom Sino-Korea
                                        Column(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable { onSpeak(num.sinoKo) }
                                                .padding(4.dp)
                                        ) {
                                            Text("Sino-Korea", fontSize = 9.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(num.sinoKo, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = InkBlack)
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("(${num.sinoRo})", fontSize = 11.sp, color = HaneulBlue)
                                            }
                                        }

                                        // Kolom Asli Korea
                                        Column(
                                            modifier = Modifier
                                                .weight(1.3f)
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable { onSpeak(num.nativeKo) }
                                                .padding(4.dp)
                                        ) {
                                            Text("Asli Korea", fontSize = 9.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(num.nativeKo, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFD97706))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("(${num.nativeRo})", fontSize = 11.sp, color = TextSecondary)
                                            }
                                            if (num.nativeUnitForm.isNotEmpty()) {
                                                Text(num.nativeUnitForm, fontSize = 9.sp, color = SaessakGreen, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }

                            // 4. Aturan Waktu & Jam (시간)
                            item {
                                Spacer(modifier = Modifier.height(4.dp))
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    border = CardDefaults.outlinedCardBorder().copy(
                                        brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFE2E8F0))
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Schedule, contentDescription = null, tint = HaneulBlue, modifier = Modifier.size(20.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Aturan Membaca Jam & Waktu (시간)",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = InkBlack
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color(0xFFF1F5F9),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = "Rumus Emas: [Angka Asli] 시 (Jam) + [Angka Sino] 분 (Menit)",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = HaneulBlue,
                                                modifier = Modifier.padding(8.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        val timeExamples = listOf(
                                            Triple("03:15", "세 시 십오 분", "se si sibo bun (Jam 3 lewat 15 menit)"),
                                            Triple("07:30", "일곱 시 삼십 분", "ilgop si samsip bun (atau 일곱 시 반 = jam 7 setengah)"),
                                            Triple("10:45", "열 시 사십오 분", "yeol si sasibo bun (Jam 10 lewat 45 menit)")
                                        )

                                        timeExamples.forEach { (digital, ko, id) ->
                                            Surface(
                                                shape = RoundedCornerShape(10.dp),
                                                color = Color(0xFFF8FAFC),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 3.dp)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .clickable { onSpeak(ko) }
                                                    .padding(8.dp)
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, tint = HaneulBlue, modifier = Modifier.size(16.dp))
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Column {
                                                        Text("$digital ➔ $ko", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = InkBlack)
                                                        Text(id, fontSize = 11.sp, color = TextSecondary)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // 5. Satuan Hitung Populer (단위 명사)
                            item {
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.ShoppingBag, contentDescription = null, tint = HaneulBlue, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Satuan Hitung Populer (단위 명사)",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = InkBlack
                                    )
                                }
                            }

                            items(classifiers) { cl ->
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    border = CardDefaults.outlinedCardBorder().copy(
                                        brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFE2E8F0))
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { onSpeak("${cl.exampleKo}. ${cl.unitKo}") }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = HaneulContainer
                                                ) {
                                                    Text(
                                                        text = "${cl.unitKo} (${cl.unitRo})",
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = HaneulBlue,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(cl.category, fontSize = 11.sp, color = TextSecondary)
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Contoh: ${cl.exampleKo} (${cl.exampleRo})",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = InkBlack
                                            )
                                            Text(cl.exampleId, fontSize = 11.sp, color = TextSecondary)
                                        }

                                        Icon(
                                            Icons.AutoMirrored.Filled.VolumeUp,
                                            contentDescription = "Dengar",
                                            tint = HaneulBlue,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }

                            // 6. Angka Besar Sino (Uang & Tanggal)
                            item {
                                Spacer(modifier = Modifier.height(4.dp))
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    border = CardDefaults.outlinedCardBorder().copy(
                                        brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFE2E8F0))
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text(
                                            text = "Angka Besar Sino-Korea (Uang Won & Tanggal)",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = InkBlack
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "Korea menghitung angka besar per 4 digit (만 = 10.000). Contoh: 50.000 Won = 오만 원 (o-man won).",
                                            fontSize = 11.sp,
                                            color = TextSecondary
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))

                                        largeNumbers.forEach { lnum ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 3.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .clickable { onSpeak(lnum.sinoKo) }
                                                    .padding(horizontal = 6.dp, vertical = 4.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(lnum.arabic, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = InkBlack)
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(lnum.sinoKo, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = HaneulBlue)
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("(${lnum.sinoRo})", fontSize = 11.sp, color = TextSecondary)
                                                }
                                                Text(lnum.meaningId, fontSize = 11.sp, color = TextSecondary)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    3 -> {
                        // Interactive Grammar & Number Quiz Section
                        var quizIndex by remember { mutableIntStateOf(0) }
                        var selectedOption by remember { mutableStateOf<Int?>(null) }
                        var correctCount by remember { mutableIntStateOf(0) }
                        var isFinished by remember { mutableStateOf(false) }

                        val currentQ = grammarQuizzes.getOrElse(quizIndex) { grammarQuizzes.first() }

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        ) {
                            if (!isFinished) {
                                Text(
                                    text = "Pertanyaan ${quizIndex + 1} dari ${grammarQuizzes.size}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = HaneulBlue
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = currentQ.question,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = InkBlack
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = Color(0xFFEFF6FF),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = currentQ.sentenceWithBlank,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = HaneulBlue,
                                        modifier = Modifier.padding(14.dp),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                currentQ.options.forEachIndexed { optIdx, optText ->
                                    val isSelected = selectedOption == optIdx
                                    val showResult = selectedOption != null
                                    val isCorrect = optIdx == currentQ.correctIndex

                                    val bg = when {
                                        !showResult -> Color.White
                                        isCorrect -> Color(0xFFD1FAE5)
                                        isSelected -> Color(0xFFFEE2E2)
                                        else -> Color.White
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = bg,
                                        border = CardDefaults.outlinedCardBorder().copy(
                                            brush = androidx.compose.ui.graphics.SolidColor(
                                                if (showResult && isCorrect) SaessakGreen else Color(0xFFE2E8F0)
                                            )
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable(enabled = selectedOption == null) {
                                                selectedOption = optIdx
                                                if (optIdx == currentQ.correctIndex) {
                                                    correctCount += 1
                                                }
                                            }
                                            .testTag("grammar_opt_$optIdx")
                                    ) {
                                        Text(
                                            text = optText,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = InkBlack,
                                            modifier = Modifier.padding(14.dp)
                                        )
                                    }
                                }

                                AnimatedVisibility(visible = selectedOption != null) {
                                    Column(modifier = Modifier.padding(top = 10.dp)) {
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = Color(0xFFFFFBEB),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = "💡 ${currentQ.explanation}",
                                                fontSize = 12.sp,
                                                color = InkBlack,
                                                modifier = Modifier.padding(10.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(12.dp))

                                        Button(
                                            onClick = {
                                                if (quizIndex < grammarQuizzes.size - 1) {
                                                    quizIndex += 1
                                                    selectedOption = null
                                                } else {
                                                    isFinished = true
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = HaneulBlue),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = if (quizIndex < grammarQuizzes.size - 1) "Soal Selanjutnya" else "Selesai",
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            } else {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text("🎉", fontSize = 48.sp)
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "Kuis Tata Bahasa & Angka Selesai!",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = InkBlack
                                    )
                                    Text(
                                        text = "Kamu menjawab benar $correctCount dari ${grammarQuizzes.size} soal",
                                        fontSize = 13.sp,
                                        color = TextSecondary
                                    )
                                    Spacer(modifier = Modifier.height(18.dp))
                                    Button(
                                        onClick = {
                                            quizIndex = 0
                                            selectedOption = null
                                            correctCount = 0
                                            isFinished = false
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = HaneulBlue),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("Coba Lagi", fontWeight = FontWeight.Bold)
                                    }
                        }
                    }
                }
            }
        }
    }
}
