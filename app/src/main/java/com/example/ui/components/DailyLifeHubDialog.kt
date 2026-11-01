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
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Bathtub
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocalConvenienceStore
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.example.ui.theme.NunbitYellowContainer
import com.example.ui.theme.SaessakGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary

data class DailyPhraseItem(
    val korean: String,
    val romanization: String,
    val indonesian: String,
    val situation: String
)

data class DailyLifeCategory(
    val title: String,
    val koreanSubtitle: String,
    val icon: ImageVector,
    val color: Color,
    val containerColor: Color,
    val phrases: List<DailyPhraseItem>,
    val culturalTipTitle: String,
    val culturalTipBody: String
)

val dailyLifeCategories: List<DailyLifeCategory> = listOf(
    // 1. Toilet & Kamar Mandi
    DailyLifeCategory(
        title = "Toilet & Mandi",
        koreanSubtitle = "화장실 & 욕실",
        icon = Icons.Default.Bathtub,
        color = Color(0xFF2563EB),
        containerColor = Color(0xFFEFF6FF),
        culturalTipTitle = "K-Culture: Nomor Sandi Toilet & Pemanas Air (보일러)",
        culturalTipBody = "Di kafe dan resto Korea, pintu toilet sering memakai kunci digital. Nomor sandi 4 digit tercetak di struk bawah belanjaan. Untuk air hangat mandi, pastikan menekan tombol '온수' (Air Hangat) atau '목욕' (Mandi) pada panel remote dinding boiler rumah.",
        phrases = listOf(
            DailyPhraseItem("화장실이 어디예요?", "hwajangsil-i eodiyeyo?", "Di mana toiletnya?", "Menanyakan letak toilet di restoran/stasiun"),
            DailyPhraseItem("화장실 비밀번호가 뭐예요?", "hwajangsil bimilbeonhoga mwoyeyo?", "Berapa nomor sandi pintu toilet?", "Bertanya PIN toilet kafe"),
            DailyPhraseItem("화장실에 휴지가 없어요.", "hwajangsir-e hyujiga eopseoyo.", "Di toilet tidak ada tisu.", "Memberitahu tisu toilet habis"),
            DailyPhraseItem("따뜻한 물이 안 나와요.", "ttatteut-han mur-i an nawayo.", "Air hangatnya tidak keluar.", "Melaporkan masalah shower di guesthouse"),
            DailyPhraseItem("비누와 수건 좀 더 주세요.", "binuwa sugeon jom deo juseyo.", "Tolong beri sabun dan handuk lagi.", "Meminta fasilitas mandi tambahan"),
            DailyPhraseItem("양치하고 세수할게요.", "yangchihago sesuhalgeyo.", "Saya mau sikat gigi dan cuci muka dulu.", "Aktivitas pagi di kamar mandi"),
            DailyPhraseItem("샤워하고 올게요. 15분 걸려요.", "syawohago olgeyo. sibobun geollyeoyo.", "Saya mandi dulu ya. Sekitar 15 menit.", "Memberitahu teman sebelum mandi shower")
        )
    ),

    // 2. Sepeda & Transportasi Harian
    DailyLifeCategory(
        title = "Sepeda & Transportasi",
        koreanSubtitle = "자전거 & 대중교통",
        icon = Icons.AutoMirrored.Filled.DirectionsBike,
        color = Color(0xFF059669),
        containerColor = Color(0xFFF0FDF4),
        culturalTipTitle = "K-Culture: Sepeda Sewa Seoul (따릉이) & Diskon Transit",
        culturalTipBody = "Sepeda publik Ttareungi (따릉이) dapat disewa hanya seharga 1.000 won (~Rp12.000) per jam via scan QR. Wajib dikendarai di jalur khusus sepeda (자전거 도로) dengan helm. Jika pindah dari bus ke subway dalam waktu 30 menit dengan kartu T-Money, Anda mendapat diskon transit gratis!",
        phrases = listOf(
            DailyPhraseItem("따릉이 자전거 어떻게 빌려요?", "ttareung-i jajeongeo eotteoke billyeoyo?", "Bagaimana cara menyewa sepeda Ttareungi?", "Bertanya cara sewa sepeda publik Seoul"),
            DailyPhraseItem("자전거 도로로 안전하게 타세요.", "jajeongeo dororo anjeonhage taseyo.", "Bersepedalah dengan aman di jalur sepeda.", "Panduan mengendarai sepeda"),
            DailyPhraseItem("헬멧을 꼭 착용해야 돼요.", "helmes-eul kkok chak-yonghaeya dwaeyo.", "Harus memakai helm pengaman.", "Aturan keselamatan bersepeda"),
            DailyPhraseItem("교통카드 만 원 충전해 주세요.", "gyotongkadeu man won chungjeonhae juseyo.", "Tolong isi saldo kartu transportasi 10.000 won.", "Top up T-Money di minimarket"),
            DailyPhraseItem("다음 정류장에서 내려요, 잠시만요!", "da-eum jeongnyujang-eseo naeryeoyo, jamsimanyo!", "Saya turun di halte berikutnya, permisi sebentar!", "Saat bus padat dan hendak turun"),
            DailyPhraseItem("지하철로 환승할 수 있나요?", "jihacheol-ro hwanseunghal su innayo?", "Apakah bisa transit ke kereta MRT?", "Menanyakan kelanjutan rute transportasi")
        )
    ),

    // 3. Minimarket & Belanja Keseharian
    DailyLifeCategory(
        title = "Minimarket 24 Jam",
        koreanSubtitle = "편의점 & 마트",
        icon = Icons.Default.LocalConvenienceStore,
        color = Color(0xFFEA580C),
        containerColor = Color(0xFFFFF7ED),
        culturalTipTitle = "K-Culture: Kantong Plastik Berbayar & Promo 1+1",
        culturalTipBody = "Minimarket Korea (CU, GS25, 7-Eleven) mengenakan biaya 100-200 won untuk kantong kresek. Perhatikan label '1+1' (원 플러스 원) atau '2+1' di rak untuk mendapatkan produk gratis ekstra saat membayar di kasir!",
        phrases = listOf(
            DailyPhraseItem("봉투는 필요 없어요, 그냥 들고 갈게요.", "bongtu-neun piryo eopseoyo, geunyang deulgo galgeyo.", "Kantong tidak perlu, saya bawa langsung saja.", "Menolak kantong plastik berbayar"),
            DailyPhraseItem("영수증 버려 주세요.", "yeongsujeung beoryeo juseyo.", "Tolong buang saja struknya.", "Meminta kasir membuang struk kertas"),
            DailyPhraseItem("이거 전자레인지에 데워 먹어요?", "igeo jeonjare-injie dewo meogeoyo?", "Apakah ini dihangatkan di microwave dulu?", "Bertanya cara menyantap makanan siap saji"),
            DailyPhraseItem("1+1 행사 상품 맞나요?", "won peulleoseu won haengsa sangpum mannayo?", "Apakah benar ini promo beli 1 gratis 1?", "Konfirmasi promo di kasir"),
            DailyPhraseItem("카드로 결제할게요.", "kadeuro gyeoljehalgeyo.", "Saya bayar dengan kartu.", "Metode pembayaran kartu")
        )
    ),

    // 4. Rumah & Pemilahan Sampah
    DailyLifeCategory(
        title = "Rumah & Sampah",
        koreanSubtitle = "주거 & 분리수거",
        icon = Icons.Default.Home,
        color = Color(0xFF7C3AED),
        containerColor = Color(0xFFF5F3FF),
        culturalTipTitle = "K-Culture: Aturan Ketat Pemilahan Sampah (분리수거)",
        culturalTipBody = "Korea memiliki salah satu sistem daur ulang terbaik di dunia. Sampah rumah tangga wajib dipisah: 1) Daur ulang (botol plastik dilepas labelnya, kaleng, kardus), 2) Sisa makanan (kantong kuning khusus 음식물), 3) Sampah umum (kantong putih berbayar 종량제). Denda pelanggaran cukup tinggi!",
        phrases = listOf(
            DailyPhraseItem("분리수거는 어디에 해요?", "bunrisugeo-neun eodie haeyo?", "Di mana tempat membuang sampah daur ulang?", "Bertanya lokasi pemilahan sampah"),
            DailyPhraseItem("종량제 봉투는 어디서 사요?", "jongnyangje bongtu-neun eodiseo sayo?", "Di mana membeli kantong sampah resmi?", "Membeli kantong sampah khusus di minimarket"),
            DailyPhraseItem("세탁기 어떻게 켜요?", "setakgi eotteoke kyeoyo?", "Bagaimana cara menyalakan mesin cuci?", "Menanyakan instruksi mesin cuci rumah"),
            DailyPhraseItem("남은 음식은 냉장고에 넣으세요.", "nameun eumsig-eun naengjanggo-e neoeuseyo.", "Masukkan sisa makanan ke dalam kulkas.", "Petunjuk menyimpan makanan"),
            DailyPhraseItem("밤 10시 이후에는 조용히 해 주세요.", "bam yeolsi ihue-neun joyonghi hae juseyo.", "Harap tenang setelah pukul 10 malam.", "Etiket apartemen dan guesthouse")
        )
    ),

    // 5. Emosi & Rasa Kesal (Frustrasi, Mengeluh & Reaksi Spontan)
    DailyLifeCategory(
        title = "Emosi & Kesal",
        koreanSubtitle = "감정 & 불만",
        icon = Icons.Default.LocalFireDepartment,
        color = Color(0xFFDC2626),
        containerColor = Color(0xFFFEF2F2),
        culturalTipTitle = "K-Culture: Mengungkapkan Rasa Kesal (짜증) vs Kesopanan (존댓말)",
        culturalTipBody = "Di antara sahabat sebaya (Banmal / 반말), mengeluh '진짜 짜증나' (kesel banget) atau '열받아' (bikin naik darah) sangat lumrah. Namun terhadap orang lebih tua atau dalam situasi formal, jangan gunakan kata tersebut! Gunakan kata yang lebih santun seperti '조금 곤란하네요' (agak merepotkan/sulit) atau '답답하네요' (terasa menyesakkan). Reaksi spontan seperti '헐...' (astaga/speechless) dan '대박' (gila/hebat) sangat sering dipakai.",
        phrases = listOf(
            DailyPhraseItem("진짜 짜증나 죽겠어.", "jinjja jjajeungna jukgesseo.", "Bener-bener kesel setengah mati.", "Melampiaskan rasa kesal ke teman akrab"),
            DailyPhraseItem("아, 정말 답답하네요.", "a, jeongmal dapdaphaneyo.", "Ah, sungguh bikin frustrasi / nyesek.", "Mengeluh secara sopan saat situasi menemui jalan buntu"),
            DailyPhraseItem("완전 열받아!", "wanjeon yeolbada!", "Bikin naik darah banget!", "Kesal saat barang dirusak atau dibohongi"),
            DailyPhraseItem("어이가 없네...", "eoiga eomne...", "Nggak habis pikir / speechless banget...", "Reaksi saat menghadapi kejadian tak masuk akal"),
            DailyPhraseItem("헐... 진짜? 말도 안 돼!", "heol... jinjja? maldo an dwae!", "Astaga... beneran? Nggak mungkin!", "Reaksi kaget atau syok mendengar kabar mengejutkan"),
            DailyPhraseItem("휴... 다행이다. 큰일 날 뻔했어.", "hyu... dahaeng-ida. keun-il nal ppeonhaesseo.", "Fiuh... untunglah. Nyaris celaka.", "Reaksi lega setelah melewati masa genting"),
            DailyPhraseItem("마음이 너무 속상해요.", "ma-eum-i neomu soksanghaeyo.", "Hati saya sedih / kecewa sekali.", "Mengekspresikan kekecewaan mendalam secara halus"),
            DailyPhraseItem("괜찮아, 그럴 수도 있지.", "gwaenchana, geureol sudo itji.", "Nggak apa-apa, hal itu bisa saja terjadi.", "Menenangkan teman yang sedang panik atau kesal")
        )
    ),

    // 6. Tegur Sapa & Obrolan (Basa-basi, Sapaan & Pamit)
    DailyLifeCategory(
        title = "Tegur Sapa",
        koreanSubtitle = "인사 & 스몰토크",
        icon = Icons.AutoMirrored.Filled.Chat,
        color = Color(0xFFD97706),
        containerColor = Color(0xFFFFFBEB),
        culturalTipTitle = "K-Culture: Makna '밥 먹었어요?' & Budaya Basa-Basi Korea",
        culturalTipBody = "Pertanyaan '식사하셨어요?' atau '밥 먹었어?' (Sudah makan?) adalah cara khas orang Korea menyapa dengan menunjukkan perhatian tulus. Ini berasal dari masa lampau di mana makan kenyang adalah berkat besar. Selain itu, ucapan '수고하셨습니다' (Terima kasih atas kerja keras Anda) adalah salam wajib saat jam kerja usai atau selesai proyek bersama.",
        phrases = listOf(
            DailyPhraseItem("식사하셨어요? / 밥 먹었어?", "siksahasyeosseoyo? / bap meogeosseo?", "Sudah makan belum?", "Tegur sapa paling ramah penanda kepedulian"),
            DailyPhraseItem("좋은 아침이에요! 잘 잤어요?", "joeun achimieyo! jal jasseoyo?", "Selamat pagi! Tidurnya nyenyak?", "Menyapa ceria di pagi hari"),
            DailyPhraseItem("어디 가시는 길이에요?", "eodi gasineun girieyo?", "Mau pergi ke mana?", "Tegur sapa saat berpapasan di jalan atau lobi"),
            DailyPhraseItem("오랜만이에요! 요즘 어떻게 지내세요?", "oraenmanieyo! yojeum eotteoke jinaeseyo?", "Lama tak berjumpa! Kabarnya bagaimana akhir-akhir ini?", "Menyapa teman lama yang baru bertemu lagi"),
            DailyPhraseItem("오늘 날씨가 참 좋네요.", "oneul nalssiga cham jorneyo.", "Cuaca hari ini bagus sekali ya.", "Membuka obrolan santai tentang cuaca"),
            DailyPhraseItem("오늘도 수고 많으셨습니다!", "oneuldo sugo maneusyeosseumnida!", "Terima kasih banyak atas kerja kerasnya hari ini!", "Salam pamit seusai jam kerja atau kuliah"),
            DailyPhraseItem("조심해서 들어가세요, 내일 봬요!", "josimhaeseo deureogaseyo, naeil bwaeyo!", "Hati-hati di jalan pulang, sampai jumpa besok!", "Ucapan perpisahan hangat kepada rekan"),
            DailyPhraseItem("죄송하지만, 말씀 좀 여쭤볼게요.", "joesonghajiman, malsseum jom yeojjwobolgeyo.", "Mohon maaf, bolehkah saya bertanya sebentar?", "Tegur sapa sopan sebelum bertanya arah kepada orang asing")
        )
    )
)

/**
 * Interactive Dialog Modal for exploring practical daily life situations in Korea.
 */
@Composable
fun DailyLifeHubDialog(
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
                .fillMaxHeight(0.92f)
                .clip(RoundedCornerShape(26.dp)),
            color = Color.White
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
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Panduan Keseharian Korea",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = NunbitYellow
                            ) {
                                Text(
                                    text = "일상생활",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = InkBlack,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Toilet, sepeda, belanja, dan rutinitas harian dengan audio",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color.White.copy(alpha = 0.2f), CircleShape)
                            .testTag("close_daily_hub_btn")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }

                DailyLifeHubContent(onSpeak = onSpeak)
            }
        }
    }
}

@Composable
fun DailyLifeHubContent(
    onSpeak: (String) -> Unit
) {
    var selectedCategoryIndex by remember { mutableIntStateOf(0) }
    val currentCategory = dailyLifeCategories.getOrElse(selectedCategoryIndex) { dailyLifeCategories.first() }

    Column(modifier = Modifier.fillMaxSize()) {
        // Category Tabs
        ScrollableTabRow(
                    selectedTabIndex = selectedCategoryIndex,
                    containerColor = Color(0xFFF8FAFC),
                    contentColor = HaneulBlue,
                    edgePadding = 12.dp,
                    divider = {}
                ) {
                    dailyLifeCategories.forEachIndexed { index, cat ->
                        val isSelected = selectedCategoryIndex == index
                        Tab(
                            selected = isSelected,
                            onClick = { selectedCategoryIndex = index },
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                ) {
                                    Icon(
                                        cat.icon,
                                        contentDescription = null,
                                        tint = if (isSelected) cat.color else TextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = cat.title,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) cat.color else TextSecondary
                                    )
                                }
                            }
                        )
                    }
                }

                // Content List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(top = 14.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Cultural Tip Card
                    item {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = currentCategory.containerColor,
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = androidx.compose.ui.graphics.SolidColor(currentCategory.color.copy(alpha = 0.3f))
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Lightbulb, contentDescription = null, tint = currentCategory.color, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = currentCategory.culturalTipTitle,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = currentCategory.color
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = currentCategory.culturalTipBody,
                                    fontSize = 11.sp,
                                    color = InkBlack,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }

                    // Section Title
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Ungkapan Nyata & Audio Pelafalan (${currentCategory.phrases.size})",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = InkBlack
                            )
                            Text(
                                text = currentCategory.koreanSubtitle,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = currentCategory.color
                            )
                        }
                    }

                    // Phrases List
                    items(currentCategory.phrases) { phrase ->
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFE2E8F0))
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFFF1F5F9)
                                    ) {
                                        Text(
                                            text = phrase.situation,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = TextSecondary,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = phrase.korean,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = InkBlack
                                    )

                                    Text(
                                        text = "[${phrase.romanization}]",
                                        fontSize = 11.sp,
                                        color = currentCategory.color,
                                        fontWeight = FontWeight.Medium
                                    )

                                    Spacer(modifier = Modifier.height(2.dp))

                                    Text(
                                        text = phrase.indonesian,
                                        fontSize = 12.sp,
                                        color = TextSecondary
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Surface(
                                    shape = CircleShape,
                                    color = currentCategory.containerColor,
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .clickable { onSpeak(phrase.korean) }
                                        .testTag("phrase_audio_btn_${phrase.korean.take(4)}")
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.AutoMirrored.Filled.VolumeUp,
                                            contentDescription = "Dengarkan",
                                            tint = currentCategory.color,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
    }
}
