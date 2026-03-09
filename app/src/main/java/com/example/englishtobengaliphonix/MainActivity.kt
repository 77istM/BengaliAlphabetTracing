package com.example.englishtobengaliphonix

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.core.graphics.PathParser
import kotlin.math.min

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TracingScreen()
                }
            }
        }
    }
}

@Composable
fun TracingScreen() {
    val letters = AlphabetPaths.letters
    var currentLetterIndex by remember { mutableIntStateOf(0) }
    val currentLetter = letters[currentLetterIndex]

    var userPath by remember { mutableStateOf(Path()) }
    var isTracing by remember { mutableStateOf(false) }

    // Helper to scale paths
    fun scalePath(svgData: String): Path {
        val rawPath = PathParser.createPathFromPathData(svgData)
        val bounds = android.graphics.RectF()
        rawPath.computeBounds(bounds, true)

        val targetSize = 800f
        val scale = min(targetSize / (if (bounds.width() == 0f) 1f else bounds.width()),
            targetSize / (if (bounds.height() == 0f) 1f else bounds.height()))

        val matrix = android.graphics.Matrix()
        matrix.postTranslate(-bounds.left, -bounds.top)
        matrix.postScale(scale, scale)
        matrix.postTranslate(100f, 100f)

        rawPath.transform(matrix)
        return rawPath.asComposePath()
    }

    // Prepare paths
    val letterPath = remember(currentLetter) { scalePath(currentLetter.mainPath) }
    val guidePaths = remember(currentLetter) {
        currentLetter.guidePaths.map { scalePath(it) }
    }

    Column(
        modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Trace: ${currentLetter.name}",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Box(
            modifier = Modifier
                .size(350.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFF5F5F5))
                .pointerInput(currentLetter) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            isTracing = true
                            val newPath = Path().apply {
                                addPath(userPath)
                                moveTo(offset.x, offset.y)
                            }
                            userPath = newPath
                        },
                        onDragEnd = { isTracing = false },
                        onDrag = { change, _ ->
                            if (isTracing) {
                                val newPath = Path().apply {
                                    addPath(userPath)
                                    lineTo(change.position.x, change.position.y)
                                }
                                userPath = newPath
                            }
                        }
                    )
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // 1. Draw the main letter guide (Background)
                // Note: If letterPath is an outline, Fill is often better than Stroke here.
                drawPath(
                    path = letterPath,
                    color = Color.LightGray.copy(alpha = 0.5f),
                    style = Stroke(width = 60f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )

                // 2. Draw additional guide lines
                guidePaths.forEach { path ->
                    drawPath(
                        path = path,
                        color = Color.Red.copy(alpha = 0.3f),
                        style = Stroke(width = 4f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                }

                // 3. Draw user tracing, clipped strictly to the letter path
                clipPath(path = letterPath) {
                    drawPath(
                        path = userPath,
                        color = Color(0xFF4CAF50),
                        // Increased stroke width slightly to ensure it fills the clipped area well
                        style = Stroke(width = 80f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(onClick = {
                currentLetterIndex = (currentLetterIndex - 1 + letters.size) % letters.size
                userPath = Path()
            }) {
                Text("Previous Letter")
            }

            Button(onClick = { userPath = Path() }) {
                Text("Clear")
            }

            Button(onClick = {
                currentLetterIndex = (currentLetterIndex + 1) % letters.size
                userPath = Path()
            }) {
                Text("Next Letter")
            }
        }
    }
}