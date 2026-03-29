package com.example.englishtobengaliphonix

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

@Composable
fun NavigationButtons(
    onPrevious: () -> Unit,
    onClear: () -> Unit,
    onNext: () -> Unit,
    onWatch: () -> Unit,
    onHint: () -> Unit,
    isAnimating: Boolean,
    hintCount: Int,
    totalHints: Int
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(onClick = onPrevious) { Text("Previous Letter") }
            Button(onClick = onClear)    { Text("Clear") }
            Button(onClick = onNext)     { Text("Next Letter") }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(
                onClick = onWatch,
                enabled = !isAnimating
            ) {
                Text("▶ Watch")
            }
            Button(
                onClick = onHint,
                enabled = !isAnimating && hintCount < totalHints
            ) {
                Text("Hint ($hintCount/$totalHints)")
            }
        }
    }
}