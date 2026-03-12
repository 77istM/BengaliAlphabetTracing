package com.example.englishtobengaliphonix

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

@Composable
fun TracingCanvas(
    letterPath: Path,
    guidePaths: List<Path>,
    userPath: Path,
    onDragStart: (Offset) -> Unit,
    onDrag: (Offset) -> Unit,
    onDragEnd: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(350.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFF5F5F5))
            .pointerInput(letterPath) {
                detectDragGestures(
                    onDragStart = { offset -> onDragStart(offset) },
                    onDragEnd = { onDragEnd() },
                    onDrag = { change, _ -> onDrag(change.position) }
                )
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Guide outline
            drawPath(
                path = letterPath,
                color = Color.LightGray.copy(alpha = 0.5f),
                style = Stroke(width = 60f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
            // Optional sub-guides (arrows, stroke order, etc.)
            guidePaths.forEach { path ->
                drawPath(
                    path = path,
                    color = Color.Red.copy(alpha = 0.3f),
                    style = Stroke(width = 4f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            }
            // User tracing, clipped to letter shape
            clipPath(path = letterPath) {
                drawPath(
                    path = userPath,
                    color = Color(0xFF4CAF50),
                    style = Stroke(width = 80f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            }
        }
    }
}