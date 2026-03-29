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
    isAnimating: Boolean,
    animStrokeIndex: Int,
    animProgress: Float,
    visibleHintCount: Int,
    onDragStart: (Offset) -> Unit,
    onDrag: (Offset) -> Unit,
    onDragEnd: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Pre-compute animated path and tip position before entering the draw scope
    val partialAnimPath: Path?
    val animTipPosition: Offset?
    if (isAnimating && animStrokeIndex < guidePaths.size) {
        val src = guidePaths[animStrokeIndex]
        partialAnimPath = extractPartialPath(src, animProgress)
        animTipPosition = getPathEndPosition(src, animProgress)
    } else {
        partialAnimPath = null
        animTipPosition = null
    }

    Box(
        modifier = modifier
            .size(350.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFF5F5F5))
            .pointerInput(letterPath, isAnimating) {
                if (!isAnimating) {
                    detectDragGestures(
                        onDragStart = { offset -> onDragStart(offset) },
                        onDragEnd = { onDragEnd() },
                        onDrag = { change, _ -> onDrag(change.position) }
                    )
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // 1. Grey guide outline (mainPath)
            drawPath(
                path = letterPath,
                color = Color.LightGray.copy(alpha = 0.5f),
                style = Stroke(width = 60f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            // 2. Static hint strokes revealed so far (blue)
            val hintCount = visibleHintCount.coerceAtMost(guidePaths.size)
            for (i in 0 until hintCount) {
                // Skip the stroke that is currently being animated
                if (isAnimating && i == animStrokeIndex) continue
                drawPath(
                    path = guidePaths[i],
                    color = Color(0xFF1976D2).copy(alpha = 0.6f),
                    style = Stroke(width = 8f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            }

            // 3. Animated partial stroke with moving dot cursor (amber)
            if (partialAnimPath != null) {
                drawPath(
                    path = partialAnimPath,
                    color = Color(0xFFFFC107).copy(alpha = 0.9f),
                    style = Stroke(width = 10f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
                animTipPosition?.let { tip ->
                    drawCircle(
                        color = Color(0xFFFF6F00),
                        radius = 16f,
                        center = tip
                    )
                }
            }

            // 4. User tracing, clipped to letter shape (green)
            clipPath(path = letterPath) {
                drawPath(
                    path = userPath,
                    color = Color(0xFF4CAF50),
                    style = Stroke(width = 100f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            }
        }
    }
}