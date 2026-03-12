package com.example.englishtobengaliphonix

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

@Composable
fun NavigationButtons(
    onPrevious: () -> Unit,
    onClear: () -> Unit,
    onNext: () -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Button(onClick = onPrevious) { Text("Previous Letter") }
        Button(onClick = onClear) { Text("Clear") }
        Button(onClick = onNext) { Text("Next Letter") }
    }
}