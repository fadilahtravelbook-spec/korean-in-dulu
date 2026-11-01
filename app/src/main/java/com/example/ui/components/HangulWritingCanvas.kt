package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.HaneulBlue
import com.example.ui.theme.NunbitYellow
import com.example.ui.theme.SaessakGreen
import com.example.ui.theme.SarangRed
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

data class DrawnStroke(
    val points: List<Offset>,
    val color: Color = Color(0xFF1E3A8A),
    val strokeWidth: Float = 16f
)

@Composable
fun HangulWritingCanvas(
    targetCharacter: String,
    isTraceMode: Boolean,
    expectedStrokeCount: Int = 2,
    onEvaluateScore: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val strokes = remember(targetCharacter) { mutableStateListOf<DrawnStroke>() }
    var currentPoints by remember(targetCharacter) { mutableStateOf<List<Offset>>(emptyList()) }
    var evaluatedScore by remember(targetCharacter) { mutableStateOf<Int?>(null) }
    var scoreFeedback by remember(targetCharacter) { mutableStateOf("") }
    var isDemonstrating by remember { mutableStateOf(false) }

    // Brush Options: Width & Color
    var selectedBrushWidth by remember { mutableFloatStateOf(16f) }
    var selectedInkColor by remember { mutableStateOf(Color(0xFF0F172A)) }

    // Grid mode: 0 = Full Won-goji (Diagonal + Quad), 1 = Cross (Salib), 2 = Polos (Kosong)
    var gridStyle by remember { mutableIntStateOf(0) }

    var canvasPixelSize by remember { mutableStateOf(IntSize.Zero) }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .widthIn(max = 420.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Toolbar Atas Kanvas: Warna Tinta, Ukuran Kuas, Mode Grid
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
                .background(Color(0xFFF1F5F9), RoundedCornerShape(14.dp))
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Pilihan Warna Tinta
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf(
                    Color(0xFF0F172A) to "Hitam",
                    Color(0xFF2563EB) to "Biru",
                    Color(0xFFDC2626) to "Merah"
                ).forEach { (col, _) ->
                    val isSel = selectedInkColor == col
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(col)
                            .border(
                                width = if (isSel) 2.5.dp else 1.dp,
                                color = if (isSel) NunbitYellow else Color.White,
                                shape = CircleShape
                            )
                            .clickable { selectedInkColor = col }
                    )
                }
            }

            // Pilihan Ukuran Kuas
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf(
                    10f to "Halus",
                    16f to "Sedang",
                    24f to "Tebal"
                ).forEach { (w, label) ->
                    val isSel = selectedBrushWidth == w
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSel) HaneulBlue else Color.Transparent,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { selectedBrushWidth = w }
                    ) {
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSel) Color.White else Color(0xFF475569),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            // Pilihan Garis Kisi Kotak
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color.White,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable {
                        gridStyle = (gridStyle + 1) % 3
                    }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.GridOn,
                        contentDescription = null,
                        tint = HaneulBlue,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = when (gridStyle) {
                            0 -> "Grid 4x"
                            1 -> "Salib"
                            else -> "Polos"
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = HaneulBlue
                    )
                }
            }
        }

        // Kanvas Utama (Card Kotak Rasio 1:1)
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .aspectRatio(1f)
                .shadow(10.dp, RoundedCornerShape(24.dp), spotColor = Color(0x331E3A8A))
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White)
                .border(2.dp, Color(0xFFCBD5E1), RoundedCornerShape(24.dp))
                .onSizeChanged { canvasPixelSize = it }
                .testTag("hangul_canvas_box"),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(targetCharacter) {
                        // Menggunakan awaitEachGesture dan consume gesture agar scroll induk tidak mengganggu
                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            if (!isDemonstrating) {
                                down.consume()
                                currentPoints = listOf(down.position)
                                do {
                                    val event = awaitPointerEvent()
                                    event.changes.forEach { change ->
                                        if (change.pressed) {
                                            change.consume()
                                            currentPoints = currentPoints + change.position
                                        }
                                    }
                                } while (event.changes.any { it.pressed })

                                if (currentPoints.isNotEmpty()) {
                                    strokes.add(
                                        DrawnStroke(
                                            points = currentPoints,
                                            color = selectedInkColor,
                                            strokeWidth = selectedBrushWidth
                                        )
                                    )
                                    currentPoints = emptyList()
                                    evaluatedScore = null
                                }
                            }
                        }
                    }
            ) {
                val canvasWidth = size.width
                val canvasHeight = size.height

                // 1. Gambar Garis Kisi Kotak Won-goji (Buku Kotak Korea)
                if (gridStyle == 0 || gridStyle == 1) {
                    // Garis sumbu tengah horizontal & vertikal
                    drawLine(
                        color = Color(0xFFE2E8F0),
                        start = Offset(canvasWidth / 2f, 0f),
                        end = Offset(canvasWidth / 2f, canvasHeight),
                        strokeWidth = 2.5f
                    )
                    drawLine(
                        color = Color(0xFFE2E8F0),
                        start = Offset(0f, canvasHeight / 2f),
                        end = Offset(canvasWidth, canvasHeight / 2f),
                        strokeWidth = 2.5f
                    )
                }

                if (gridStyle == 0) {
                    // Garis diagonal silang kuadran halus
                    drawLine(
                        color = Color(0xFFF1F5F9),
                        start = Offset(0f, 0f),
                        end = Offset(canvasWidth, canvasHeight),
                        strokeWidth = 1.5f
                    )
                    drawLine(
                        color = Color(0xFFF1F5F9),
                        start = Offset(canvasWidth, 0f),
                        end = Offset(0f, canvasHeight),
                        strokeWidth = 1.5f
                    )
                    // Garis batas margin dalam
                    val margin = canvasWidth * 0.1f
                    drawRect(
                        color = Color(0xFFF8FAFC),
                        topLeft = Offset(margin, margin),
                        size = androidx.compose.ui.geometry.Size(canvasWidth - (margin * 2), canvasHeight - (margin * 2)),
                        style = Stroke(width = 1.5f)
                    )
                }

                // 2. Gambar Karakter Bayangan (Watermark Panduan Trace)
                if (isTraceMode) {
                    val textSizePx = when {
                        targetCharacter.length <= 1 -> canvasWidth * 0.72f
                        targetCharacter.length == 2 -> canvasWidth * 0.42f
                        else -> canvasWidth * 0.32f
                    }
                    val paint = android.graphics.Paint().apply {
                        textSize = textSizePx
                        color = android.graphics.Color.parseColor("#E0E7FF")
                        textAlign = android.graphics.Paint.Align.CENTER
                        isAntiAlias = true
                        typeface = android.graphics.Typeface.DEFAULT_BOLD
                    }
                    val yPos = (canvasHeight / 2f) - ((paint.descent() + paint.ascent()) / 2f)
                    drawContext.canvas.nativeCanvas.drawText(
                        targetCharacter,
                        canvasWidth / 2f,
                        yPos,
                        paint
                    )
                }

                // 3. Gambar Goresan yang Sudah Selesai
                for (stroke in strokes) {
                    if (stroke.points.size > 1) {
                        val path = Path()
                        path.moveTo(stroke.points[0].x, stroke.points[0].y)
                        for (i in 1 until stroke.points.size) {
                            val prev = stroke.points[i - 1]
                            val curr = stroke.points[i]
                            val midX = (prev.x + curr.x) / 2f
                            val midY = (prev.y + curr.y) / 2f
                            path.quadraticTo(prev.x, prev.y, midX, midY)
                        }
                        path.lineTo(stroke.points.last().x, stroke.points.last().y)
                        drawPath(
                            path = path,
                            color = stroke.color,
                            style = Stroke(
                                width = stroke.strokeWidth,
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )
                    } else if (stroke.points.isNotEmpty()) {
                        // Titik tunggal (dot)
                        drawCircle(
                            color = stroke.color,
                            radius = stroke.strokeWidth / 2f,
                            center = stroke.points[0]
                        )
                    }
                }

                // 4. Gambar Goresan Aktif yang Sedang Digambar
                if (currentPoints.size > 1) {
                    val path = Path()
                    path.moveTo(currentPoints[0].x, currentPoints[0].y)
                    for (i in 1 until currentPoints.size) {
                        val prev = currentPoints[i - 1]
                        val curr = currentPoints[i]
                        val midX = (prev.x + curr.x) / 2f
                        val midY = (prev.y + curr.y) / 2f
                        path.quadraticTo(prev.x, prev.y, midX, midY)
                    }
                    drawPath(
                        path = path,
                        color = selectedInkColor,
                        style = Stroke(
                            width = selectedBrushWidth,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Tombol Demo Animasi Stroke Order
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = {
                    if (!isDemonstrating) {
                        isDemonstrating = true
                        strokes.clear()
                        evaluatedScore = null
                        coroutineScope.launch {
                            val w = if (canvasPixelSize.width > 0) canvasPixelSize.width.toFloat() else 750f
                            val h = if (canvasPixelSize.height > 0) canvasPixelSize.height.toFloat() else 750f
                            val midX = w / 2f
                            val midY = h / 2f

                            fun animLine(fromX: Float, fromY: Float, toX: Float, toY: Float, steps: Int = 14) {
                                val list = mutableListOf<Offset>()
                                for (s in 0..steps) {
                                    val t = s.toFloat() / steps
                                    list.add(Offset(fromX + ((toX - fromX) * t), fromY + ((toY - fromY) * t)))
                                    currentPoints = list.toList()
                                }
                                strokes.add(DrawnStroke(points = list.toList(), color = Color(0xFF2563EB), strokeWidth = 18f))
                                currentPoints = emptyList()
                            }

                            when (targetCharacter) {
                                "ㄱ" -> {
                                    val s = mutableListOf<Offset>()
                                    for (step in 0..12) {
                                        s.add(Offset(midX - (w * 0.22f) + (step * (w * 0.44f / 12)), midY - (h * 0.22f)))
                                        delay(20)
                                        currentPoints = s.toList()
                                    }
                                    for (step in 0..12) {
                                        s.add(Offset(midX + (w * 0.22f), midY - (h * 0.22f) + (step * (h * 0.44f / 12))))
                                        delay(20)
                                        currentPoints = s.toList()
                                    }
                                    strokes.add(DrawnStroke(points = s.toList(), color = Color(0xFF2563EB), strokeWidth = 18f))
                                    currentPoints = emptyList()
                                }
                                "ㄴ" -> {
                                    val s = mutableListOf<Offset>()
                                    for (step in 0..12) {
                                        s.add(Offset(midX - (w * 0.22f), midY - (h * 0.22f) + (step * (h * 0.44f / 12))))
                                        delay(20)
                                        currentPoints = s.toList()
                                    }
                                    for (step in 0..12) {
                                        s.add(Offset(midX - (w * 0.22f) + (step * (w * 0.44f / 12)), midY + (h * 0.22f)))
                                        delay(20)
                                        currentPoints = s.toList()
                                    }
                                    strokes.add(DrawnStroke(points = s.toList(), color = Color(0xFF2563EB), strokeWidth = 18f))
                                    currentPoints = emptyList()
                                }
                                "ㄷ" -> {
                                    animLine(midX - (w * 0.22f), midY - (h * 0.22f), midX + (w * 0.22f), midY - (h * 0.22f))
                                    delay(120)
                                    val s = mutableListOf<Offset>()
                                    for (step in 0..10) {
                                        s.add(Offset(midX - (w * 0.22f), midY - (h * 0.22f) + (step * (h * 0.44f / 10))))
                                        delay(20)
                                        currentPoints = s.toList()
                                    }
                                    for (step in 0..10) {
                                        s.add(Offset(midX - (w * 0.22f) + (step * (w * 0.44f / 10)), midY + (h * 0.22f)))
                                        delay(20)
                                        currentPoints = s.toList()
                                    }
                                    strokes.add(DrawnStroke(points = s.toList(), color = Color(0xFF2563EB), strokeWidth = 18f))
                                    currentPoints = emptyList()
                                }
                                "ㄹ" -> {
                                    // 1. ㄱ atas
                                    animLine(midX - (w * 0.2f), midY - (h * 0.22f), midX + (w * 0.2f), midY - (h * 0.22f))
                                    animLine(midX + (w * 0.2f), midY - (h * 0.22f), midX + (w * 0.2f), midY)
                                    delay(100)
                                    // 2. Horizontal tengah
                                    animLine(midX - (w * 0.2f), midY, midX + (w * 0.2f), midY)
                                    delay(100)
                                    // 3. ㄴ bawah
                                    animLine(midX - (w * 0.2f), midY, midX - (w * 0.2f), midY + (h * 0.22f))
                                    animLine(midX - (w * 0.2f), midY + (h * 0.22f), midX + (w * 0.2f), midY + (h * 0.22f))
                                }
                                "ㅁ" -> {
                                    animLine(midX - (w * 0.2f), midY - (h * 0.2f), midX - (w * 0.2f), midY + (h * 0.2f))
                                    delay(100)
                                    animLine(midX - (w * 0.2f), midY - (h * 0.2f), midX + (w * 0.2f), midY - (h * 0.2f))
                                    animLine(midX + (w * 0.2f), midY - (h * 0.2f), midX + (w * 0.2f), midY + (h * 0.2f))
                                    delay(100)
                                    animLine(midX - (w * 0.2f), midY + (h * 0.2f), midX + (w * 0.2f), midY + (h * 0.2f))
                                }
                                "ㅇ" -> {
                                    val s = mutableListOf<Offset>()
                                    val radius = w * 0.24f
                                    for (angle in 270 downTo -90 step 15) {
                                        val rad = Math.toRadians(angle.toDouble())
                                        s.add(Offset(midX + (radius * cos(rad)).toFloat(), midY + (radius * sin(rad)).toFloat()))
                                        delay(20)
                                        currentPoints = s.toList()
                                    }
                                    strokes.add(DrawnStroke(points = s.toList(), color = Color(0xFF2563EB), strokeWidth = 18f))
                                    currentPoints = emptyList()
                                }
                                "ㅏ" -> {
                                    // 1. Vertikal panjang
                                    animLine(midX - (w * 0.08f), midY - (h * 0.28f), midX - (w * 0.08f), midY + (h * 0.28f), 16)
                                    delay(120)
                                    // 2. Horizontal pendek ke kanan
                                    animLine(midX - (w * 0.08f), midY, midX + (w * 0.22f), midY, 10)
                                }
                                "ㅑ" -> {
                                    animLine(midX - (w * 0.08f), midY - (h * 0.28f), midX - (w * 0.08f), midY + (h * 0.28f), 16)
                                    delay(100)
                                    animLine(midX - (w * 0.08f), midY - (h * 0.09f), midX + (w * 0.22f), midY - (h * 0.09f), 10)
                                    delay(100)
                                    animLine(midX - (w * 0.08f), midY + (h * 0.09f), midX + (w * 0.22f), midY + (h * 0.09f), 10)
                                }
                                "ㅓ" -> {
                                    animLine(midX - (w * 0.22f), midY, midX + (w * 0.08f), midY, 10)
                                    delay(120)
                                    animLine(midX + (w * 0.08f), midY - (h * 0.28f), midX + (w * 0.08f), midY + (h * 0.28f), 16)
                                }
                                "ㅕ" -> {
                                    animLine(midX - (w * 0.22f), midY - (h * 0.09f), midX + (w * 0.08f), midY - (h * 0.09f), 10)
                                    delay(100)
                                    animLine(midX - (w * 0.22f), midY + (h * 0.09f), midX + (w * 0.08f), midY + (h * 0.09f), 10)
                                    delay(100)
                                    animLine(midX + (w * 0.08f), midY - (h * 0.28f), midX + (w * 0.08f), midY + (h * 0.28f), 16)
                                }
                                "ㅗ" -> {
                                    animLine(midX, midY - (h * 0.20f), midX, midY, 10)
                                    delay(120)
                                    animLine(midX - (w * 0.28f), midY, midX + (w * 0.28f), midY, 16)
                                }
                                "ㅛ" -> {
                                    animLine(midX - (w * 0.11f), midY - (h * 0.20f), midX - (w * 0.11f), midY, 10)
                                    delay(100)
                                    animLine(midX + (w * 0.11f), midY - (h * 0.20f), midX + (w * 0.11f), midY, 10)
                                    delay(100)
                                    animLine(midX - (w * 0.28f), midY, midX + (w * 0.28f), midY, 16)
                                }
                                "ㅜ" -> {
                                    animLine(midX - (w * 0.28f), midY, midX + (w * 0.28f), midY, 16)
                                    delay(120)
                                    animLine(midX, midY, midX, midY + (h * 0.20f), 10)
                                }
                                "ㅠ" -> {
                                    animLine(midX - (w * 0.28f), midY, midX + (w * 0.28f), midY, 16)
                                    delay(100)
                                    animLine(midX - (w * 0.11f), midY, midX - (w * 0.11f), midY + (h * 0.20f), 10)
                                    delay(100)
                                    animLine(midX + (w * 0.11f), midY, midX + (w * 0.11f), midY + (h * 0.20f), 10)
                                }
                                "ㅡ" -> {
                                    animLine(midX - (w * 0.28f), midY, midX + (w * 0.28f), midY, 16)
                                }
                                "ㅣ" -> {
                                    animLine(midX, midY - (h * 0.28f), midX, midY + (h * 0.28f), 16)
                                }
                                else -> {
                                    // Fallback stroke demo
                                    animLine(midX - (w * 0.15f), midY - (h * 0.2f), midX + (w * 0.15f), midY - (h * 0.2f), 12)
                                    delay(100)
                                    animLine(midX, midY - (h * 0.2f), midX, midY + (h * 0.2f), 14)
                                }
                            }
                            isDemonstrating = false
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEFF6FF)),
                shape = RoundedCornerShape(12.dp),
                enabled = !isDemonstrating,
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 7.dp)
            ) {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = HaneulBlue,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isDemonstrating) "Memutar Demo Coretan..." else "Lihat Demo Urutan Coretan 🎬",
                    fontSize = 12.sp,
                    color = HaneulBlue,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Toolbar Bawah: Urungkan, Bersihkan, dan Tombol Periksa Jawaban
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    if (strokes.isNotEmpty()) {
                        strokes.removeAt(strokes.lastIndex)
                        evaluatedScore = null
                    }
                },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color(0xFFF1F5F9))
                    .testTag("undo_stroke_button"),
                enabled = strokes.isNotEmpty() && !isDemonstrating
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Undo,
                    contentDescription = "Urungkan coretan",
                    tint = if (strokes.isNotEmpty()) HaneulBlue else Color.Gray
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            IconButton(
                onClick = {
                    strokes.clear()
                    currentPoints = emptyList()
                    evaluatedScore = null
                    scoreFeedback = ""
                },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color(0xFFF1F5F9))
                    .testTag("clear_canvas_button"),
                enabled = strokes.isNotEmpty() && !isDemonstrating
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Hapus semua coretan",
                    tint = if (strokes.isNotEmpty()) SarangRed else Color.Gray
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Button(
                onClick = {
                    val allPts = strokes.flatMap { it.points }
                    if (allPts.isEmpty()) {
                        evaluatedScore = 0
                        scoreFeedback = "Kanvas masih kosong. Tuliskan huruf di dalam kotak lalu periksa kembali!"
                        onEvaluateScore(0)
                        return@Button
                    }

                    // Smart Handwriting Evaluation
                    val minX = allPts.minOf { it.x }
                    val maxX = allPts.maxOf { it.x }
                    val minY = allPts.minOf { it.y }
                    val maxY = allPts.maxOf { it.y }

                    val drawnWidth = maxX - minX
                    val drawnHeight = maxY - minY
                    val cWidth = if (canvasPixelSize.width > 0) canvasPixelSize.width.toFloat() else 750f
                    val cHeight = if (canvasPixelSize.height > 0) canvasPixelSize.height.toFloat() else 750f

                    // Checks:
                    val spanScore = when {
                        drawnWidth > (cWidth * 0.15f) && drawnHeight > (cHeight * 0.15f) -> 40
                        drawnWidth > (cWidth * 0.08f) || drawnHeight > (cHeight * 0.08f) -> 25
                        else -> 10
                    }

                    val countDiff = abs(strokes.size - expectedStrokeCount)
                    val strokeScore = when (countDiff) {
                        0 -> 40
                        1 -> 32
                        2 -> 20
                        else -> 10
                    }

                    val densityScore = if (allPts.size >= 12) 20 else 10
                    val finalScore = min(100, spanScore + strokeScore + densityScore)

                    evaluatedScore = finalScore
                    scoreFeedback = when {
                        finalScore >= 85 -> "🎉 Sempurna! Goresan sangat rapi dan jumlah langkah tepat ($finalScore/100)."
                        finalScore >= 70 -> "👍 Sangat Bagus! Huruf terbaca jelas. Tingkatkan kerapian agar makin mantap ($finalScore/100)."
                        else -> "✍️ Cukup Baik! Ikuti garis panduan bayangan untuk bentuk yang lebih proporsional ($finalScore/100)."
                    }
                    onEvaluateScore(finalScore)
                },
                modifier = Modifier.testTag("evaluate_writing_button"),
                colors = ButtonDefaults.buttonColors(containerColor = HaneulBlue),
                shape = RoundedCornerShape(14.dp),
                enabled = strokes.isNotEmpty() && !isDemonstrating
            ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Periksa Tulisan ✨", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        // Kartu Evaluasi & Bintang Feedback
        evaluatedScore?.let { score ->
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (score >= 80) Color(0xFFECFDF5) else Color(0xFFFFFBEB)
                ),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(
                        if (score >= 80) SaessakGreen else NunbitYellow
                    )
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val stars = when {
                            score >= 85 -> 3
                            score >= 65 -> 2
                            else -> 1
                        }
                        repeat(3) { idx ->
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                tint = if (idx < stars) NunbitYellow else Color(0xFFD1D5DB),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = scoreFeedback,
                        fontWeight = FontWeight.Bold,
                        color = if (score >= 80) Color(0xFF065F46) else Color(0xFF92400E),
                        fontSize = 13.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }
}
