package com.bengalialphabettracing.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@Composable
fun NavigationButtons(
    onPrevious: () -> Unit,
    onClear: () -> Unit,
    onNext: () -> Unit,
    onWatch: () -> Unit,
    onHint: () -> Unit,
    onScore: () -> Unit,
    isAnimating: Boolean,
    hintCount: Int,
    totalHints: Int,
    hasUserDrawn: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(onClick = onPrevious) { Text(stringResource(R.string.btn_previous)) }
            Button(onClick = onClear)    { Text(stringResource(R.string.btn_clear)) }
            Button(onClick = onNext)     { Text(stringResource(R.string.btn_next)) }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(
                onClick = onWatch,
                enabled = !isAnimating
            ) {
                Text(stringResource(R.string.btn_watch))
            }
            Button(
                onClick = onHint,
                enabled = !isAnimating && hintCount < totalHints
            ) {
                Text(stringResource(R.string.btn_hint, hintCount, totalHints))
            }
        }
        Button(
            onClick = onScore,
            enabled = !isAnimating && hasUserDrawn
        ) {
            Text(stringResource(R.string.btn_check_score))
        }
    }
}
