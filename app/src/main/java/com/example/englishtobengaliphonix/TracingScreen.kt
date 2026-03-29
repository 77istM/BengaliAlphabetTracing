package com.example.englishtobengaliphonix

import androidx.compose.foundation.layout.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

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

    val letterPath = remember(currentLetter) { scalePath(currentLetter.mainPath) }
    val guidePaths = remember(currentLetter) {
        viewModel.effectiveGuidePaths.map { scalePath(it) }
    }

    LaunchedEffect(currentLetter) {
        viewModel.playSound()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ── Category selector bar ────────────────────────────────────────────
        CategorySelector(
            selectedCategory = selectedCategory,
            onCategorySelected = { viewModel.selectCategory(it) }
        )

        // ── Category progress bar ────────────────────────────────────────────
        CategoryProgressBar(
            practicedCount = categoryProgress.first,
            totalCount     = categoryProgress.second,
            onClearProgress = { viewModel.clearAllProgress() }
        )

        HorizontalDivider(
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant
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
                letterName = currentLetter.name,
                isPlaying = isPlaying,
                onPlayClick = { viewModel.playSound() }
            )

            TracingCanvas(
                letterPath       = letterPath,
                guidePaths       = guidePaths,
                userPath         = userPath,
                isAnimating      = isAnimating,
                animStrokeIndex  = animStrokeIndex,
                animProgress     = animProgress,
                visibleHintCount = visibleHintCount,
                onDragStart      = { offset -> viewModel.startPath(offset) },
                onDrag           = { position -> viewModel.dragTo(position) },
                onDragEnd        = { }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ── Score card (shown after "Check Score" is tapped) ─────────────
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
