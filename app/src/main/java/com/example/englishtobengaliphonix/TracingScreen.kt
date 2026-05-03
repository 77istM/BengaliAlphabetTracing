package com.bengalialphabettracing.app

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

/** Minimum total horizontal drag distance (dp) to trigger letter navigation via swipe. */
private const val SWIPE_THRESHOLD_DP = 80f

@Composable
fun TracingScreen(
    viewModel: TracingViewModel = viewModel()
) {
    val userPath         by viewModel.userPath.collectAsState()
    val isPlaying        by viewModel.isPlaying.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val isAnimating      by viewModel.isAnimating.collectAsState()
    val animStrokeIndex  by viewModel.animStrokeIndex.collectAsState()
    val animProgress     by viewModel.animProgress.collectAsState()
    val visibleHintCount by viewModel.visibleHintCount.collectAsState()
    val lastScore        by viewModel.lastScore.collectAsState()
    val hasUserDrawn     by viewModel.hasUserDrawn.collectAsState()
    val categoryProgress by viewModel.categoryProgress.collectAsState()
    val currentBestScore by viewModel.currentBestScore.collectAsState()
    val currentLetter    = viewModel.currentLetter

    val density = LocalDensity.current
    val swipeThresholdPx = with(density) { SWIPE_THRESHOLD_DP.dp.toPx() }

    LaunchedEffect(currentLetter) {
        viewModel.playSound()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            // Horizontal swipe on areas outside the canvas navigates between letters.
            // The canvas consumes drag events itself, so only non-canvas areas trigger this.
            .pointerInput(isAnimating) {
                if (!isAnimating) {
                    var totalDrag = 0f
                    detectHorizontalDragGestures(
                        onDragStart  = { totalDrag = 0f },
                        onDragEnd    = {
                            if (totalDrag < -swipeThresholdPx) viewModel.nextLetter()
                            else if (totalDrag > swipeThresholdPx) viewModel.previousLetter()
                        },
                        onHorizontalDrag = { _, dragAmount -> totalDrag += dragAmount }
                    )
                }
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ── Category selector bar ────────────────────────────────────────────
        CategorySelector(
            selectedCategory   = selectedCategory,
            onCategorySelected = { viewModel.selectCategory(it) }
        )

        // ── Category progress bar ────────────────────────────────────────────
        CategoryProgressBar(
            practicedCount  = categoryProgress.first,
            totalCount      = categoryProgress.second,
            onClearProgress = { viewModel.clearAllProgress() }
        )

        HorizontalDivider(
            thickness = 1.dp,
            color     = MaterialTheme.colorScheme.outlineVariant
        )

        // ── Main tracing content ─────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            LetterHeader(
                letterName  = currentLetter.name,
                romanized   = currentLetter.romanized,
                isPlaying   = isPlaying,
                onPlayClick = { viewModel.playSound() }
            )

            // Responsive canvas: fills available width, square, capped at 500 dp.
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                val canvasSize   = minOf(maxWidth, 500.dp)
                val canvasSizePx = with(density) { canvasSize.toPx() }

                // Report pixel size to ViewModel for accurate path scaling/scoring.
                LaunchedEffect(canvasSizePx) {
                    viewModel.setCanvasSize(canvasSizePx)
                }

                val letterPath = remember(currentLetter, canvasSizePx) {
                    scalePath(currentLetter.mainPath, canvasSizePx)
                }
                val guidePaths = remember(currentLetter, canvasSizePx) {
                    viewModel.effectiveGuidePaths.map { scalePath(it, canvasSizePx) }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    TracingCanvas(
                        letterPath       = letterPath,
                        guidePaths       = guidePaths,
                        userPath         = userPath,
                        isAnimating      = isAnimating,
                        animStrokeIndex  = animStrokeIndex,
                        animProgress     = animProgress,
                        visibleHintCount = visibleHintCount,
                        canvasSize       = canvasSize,
                        onDragStart      = { offset -> viewModel.startPath(offset) },
                        onDrag           = { position -> viewModel.dragTo(position) },
                        // Auto-score on finger lift
                        onDragEnd        = { viewModel.scoreUserPath() }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // ── Score card ───────────────────────────────────────────
                    if (lastScore != null) {
                        ScoreCard(
                            lastScore = lastScore!!,
                            bestScore = currentBestScore
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    NavigationButtons(
                        onPrevious   = { viewModel.previousLetter() },
                        onClear      = { viewModel.clearPath() },
                        onNext       = { viewModel.nextLetter() },
                        onWatch      = { viewModel.startAnimation() },
                        onHint       = { viewModel.showNextHint() },
                        onScore      = { viewModel.scoreUserPath() },
                        isAnimating  = isAnimating,
                        hintCount    = visibleHintCount.coerceAtMost(guidePaths.size),
                        totalHints   = guidePaths.size,
                        hasUserDrawn = hasUserDrawn
                    )
                }
            }
        }
    }
}
