package com.example.englishtobengaliphonix

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp

@Composable
fun LetterHeader(
    letterName: String,
    isPlaying: Boolean,
    onPlayClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val iconScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isPlaying) 1.3f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(400),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Row(
        modifier = modifier.padding(bottom = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Trace: $letterName",
            style = MaterialTheme.typography.headlineLarge
        )
        Spacer(modifier = Modifier.width(16.dp))
        IconButton(onClick = onPlayClick) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Play pronunciation",
                modifier = Modifier.scale(iconScale),
                tint = if (isPlaying) MaterialTheme.colorScheme.primary
                else LocalContentColor.current
            )
        }
    }
}