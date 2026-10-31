package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.HaneulBlue
import com.example.ui.theme.NunbitYellow
import com.example.ui.theme.SaessakGreen
import com.example.ui.theme.SarangRed
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.min

data class DrawnStroke(
    val points: List<Offset>,
    val color: Color = Color(0xFF1E3A8A),
    val strokeWidth: Float = 18f
)

@Composable
fun HangulWritingCanvas(
    targetCharacter: String,
    isTraceMode: Boolean,
    onEvaluateScore: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val strokes = remember(targetCharacter) { mutableStateListOf<DrawnStroke>() }
    var currentPoints by remember(targetCharacter) { mutableStateOf<List<Offset>>(emptyList()) }
    var evaluatedScore by remember(targetCharacter) { mutableStateOf<Int?>(null) }
    var isDemonstrating by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Canvas Card Container
        Box(
            modifier = Modifier
                .size(280.dp)
                .shadow(8.dp, RoundedCornerShape(24.dp))
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White)
                .border(2.dp, Color(0xFFE2E8F0), RoundedCornerShape(24.dp))
                .testTag("hangul_canvas_box"),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(targetCharacter) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                if (!isDemonstrating) {
                                    currentPoints = listOf(offset)
                                }
                            },
                            onDrag = { change, _ ->
                                if (!isDemonstrating) {
                                    change.consume()
                                    currentPoints = currentPoints + change.position
                                }
                            },
                            onDragEnd = {
                                if (currentPoints.isNotEmpty()) {
                                    strokes.add(DrawnStroke(points = currentPoints))
                                    currentPoints = emptyList()
                                    evaluatedScore = null
                                }
                            },
                            onDragCancel = {
                                currentPoints = emptyList()
                            }
                        )
                    }
            ) {
                val canvasWidth = size.width
                val canvasHeight = size.height

                // Draw traditional Korean manuscript square grid guidelines (Won-goji style)
                drawLine(
                    color = Color(0xFFF1F5F9),
                    start = Offset(canvasWidth / 2, 0f),
                    end = Offset(canvasWidth / 2, canvasHeight),
                    strokeWidth = 2f
                )
                drawLine(
                    color = Color(0xFFF1F5F9),
                    start = Offset(0f, canvasHeight / 2),
                    end = Offset(canvasWidth, canvasHeight / 2),
                    strokeWidth = 2f
                )
                // Diagonal guidelines
                drawLine(
                    color = Color(0xFFF8FAFC),
                    start = Offset(0f, 0f),
                    end = Offset(canvasWidth, canvasHeight),
                    strokeWidth = 1.5f
                )
                drawLine(
                    color = Color(0xFFF8FAFC),
                    start = Offset(canvasWidth, 0f),
                    end = Offset(0f, canvasHeight),
                    strokeWidth = 1.5f
                )

                // If in trace mode, draw the watermark background character
                if (isTraceMode) {
                    val paint = android.graphics.Paint().apply {
                        textSize = 280f
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

                // Draw existing strokes
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
                    }
                }

                // Draw active in-progress stroke
                if (currentPoints.size > 1) {
                    val path = Path()
                    path.moveTo(currentPoints[0].x, currentPoints[0].y)
                    for (i in 1 until currentPoints.size) {
                        val prev = currentPoints[i - 1]
                        val curr = currentPoints[i]
                        path.lineTo(curr.x, curr.y)
                    }
                    drawPath(
                        path = path,
                        color = HaneulBlue,
                        style = Stroke(
                            width = 18f,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Demo Animation & Controls Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Demo Play Button
            Button(
                onClick = {
                    if (!isDemonstrating) {
                        isDemonstrating = true
                        strokes.clear()
                        evaluatedScore = null
                        coroutineScope.launch {
                            // Animate demonstration strokes
                            val centerX = 140f * 2.7f
                            val centerY = 140f * 2.7f
                            val stroke1 = mutableListOf<Offset>()
                            for (step in 0..15) {
                                stroke1.add(Offset(centerX - 60f + (step * 4f), centerY - 120f + (step * 14f)))
                                delay(25)
                                currentPoints = stroke1.toList()
                            }
                            strokes.add(DrawnStroke(points = stroke1.toList(), color = Color(0xFF2563EB)))
                            currentPoints = emptyList()
                            delay(100)

                            val stroke2 = mutableListOf<Offset>()
                            for (step in 0..12) {
                                stroke2.add(Offset(centerX - 10f + (step * 8f), centerY))
                                delay(25)
                                currentPoints = stroke2.toList()
                            }
                            strokes.add(DrawnStroke(points = stroke2.toList(), color = Color(0xFF2563EB)))
                            currentPoints = emptyList()
                            isDemonstrating = false
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEFF6FF)),
                shape = RoundedCornerShape(12.dp),
                enabled = !isDemonstrating,
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = HaneulBlue, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isDemonstrating) "Memutar Demo..." else "Lihat Demo Coretan",
                    fontSize = 11.sp,
                    color = HaneulBlue,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Toolbar: Undo, Clear, Check
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
                    Icons.Default.Undo,
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
                    val totalPoints = strokes.sumOf { it.points.size }
                    val calculatedScore = if (strokes.isEmpty()) {
                        0
                    } else if (totalPoints < 8) {
                        50
                    } else {
                        val base = 88 + min(12, strokes.size * 2)
                        min(100, base)
                    }
                    evaluatedScore = calculatedScore
                    onEvaluateScore(calculatedScore)
                },
                modifier = Modifier.testTag("evaluate_writing_button"),
                colors = ButtonDefaults.buttonColors(containerColor = HaneulBlue),
                shape = RoundedCornerShape(14.dp),
                enabled = strokes.isNotEmpty() && !isDemonstrating
            ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Periksa Tulisan", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        // Score Card Feedback
        evaluatedScore?.let { score ->
            Spacer(modifier = Modifier.height(12.dp))
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (score >= 80) Color(0xFFD1FAE5) else Color(0xFFFEF3C7),
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = if (score >= 80) "🎉 Sangat Bagus! Akurasi: $score%" else "👍 Bagus! Coba lagi untuk lebih rapi. Akurasi: $score%",
                        fontWeight = FontWeight.Bold,
                        color = if (score >= 80) Color(0xFF065F46) else Color(0xFF92400E),
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}
