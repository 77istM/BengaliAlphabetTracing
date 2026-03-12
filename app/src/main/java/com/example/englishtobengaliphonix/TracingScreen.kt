package com.example.englishtobengaliphonix

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.englishtobengaliphonix.LetterHeader
import com.example.englishtobengaliphonix.NavigationButtons
import com.example.englishtobengaliphonix.TracingCanvas
import com.example.englishtobengaliphonix.scalePath
import com.example.englishtobengaliphonix.TracingViewModel

@Composable
fun TracingScreen(
    viewModel: TracingViewModel = viewModel()
) {
    val userPath by viewModel.userPath.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val currentLetter = viewModel.currentLetter

    // isTracing is purely local touch state — does not need to survive the VM
    var isTracing by remember { mutableStateOf(false) }

    val letterPath = remember(currentLetter) { scalePath(currentLetter.mainPath) }
    val guidePaths = remember(currentLetter) {
        currentLetter.guidePaths.map { scalePath(it) }
    }

    LaunchedEffect(currentLetter) {
        viewModel.playSound()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        LetterHeader(
            letterName = currentLetter.name,
            isPlaying = isPlaying,
            onPlayClick = { viewModel.playSound() }
        )

        TracingCanvas(
            letterPath = letterPath,
            guidePaths = guidePaths,
            userPath = userPath,
            onDragStart = { offset ->
                isTracing = true
                viewModel.updatePath(
                    Path().apply {
                        addPath(userPath)
                        moveTo(offset.x, offset.y)
                    }
                )
            },
            onDrag = { position ->
                if (isTracing) {
                    viewModel.updatePath(
                        Path().apply {
                            addPath(userPath)
                            lineTo(position.x, position.y)
                        }
                    )
                }
            },
            onDragEnd = { isTracing = false }
        )

        Spacer(modifier = Modifier.height(32.dp))

        NavigationButtons(
            onPrevious = { viewModel.previousLetter() },
            onClear = { viewModel.clearPath() },
            onNext = { viewModel.nextLetter() }
        )
    }
}