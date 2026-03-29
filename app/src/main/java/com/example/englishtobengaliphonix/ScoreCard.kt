package com.example.englishtobengaliphonix

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

/**
 * Displays the most-recently computed accuracy score together with the
 * all-time best score for the current letter.  The card is colour-coded
 * based on performance:
 *   ≥ [SCORE_EXCELLENT] → green  "Excellent!"
 *   ≥ [SCORE_GREAT]     → blue   "Great job!"
 *   ≥ [SCORE_GOOD]      → orange "Good effort!"
 *   < [SCORE_GOOD]      → red    "Keep practicing!"
 */
@Composable
fun ScoreCard(
    lastScore: Float,
    bestScore: Float,
    modifier: Modifier = Modifier
) {
    val (accentColor, feedback) = when {
        lastScore >= SCORE_EXCELLENT -> Color(0xFF4CAF50) to "Excellent! \uD83C\uDF1F"
        lastScore >= SCORE_GREAT     -> Color(0xFF2196F3) to "Great job! \uD83D\uDC4D"
        lastScore >= SCORE_GOOD      -> Color(0xFFFF9800) to "Good effort! \uD83D\uDCAA"
        else                         -> Color(0xFFF44336) to "Keep practicing! \u270F\uFE0F"
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        colors = CardDefaults.cardColors(
            containerColor = accentColor.copy(alpha = 0.12f)
        ),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = feedback,
                    style = MaterialTheme.typography.bodyMedium,
                    color = accentColor
                )
                // Show all-time best only when it differs from the current score
                if (bestScore >= 0f && bestScore != lastScore) {
                    Text(
                        text = "Best: ${bestScore.roundToInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = accentColor.copy(alpha = 0.7f)
                    )
                }
            }
            Text(
                text = "${lastScore.roundToInt()}%",
                style = MaterialTheme.typography.headlineMedium
                    .copy(fontWeight = FontWeight.Bold),
                color = accentColor
            )
        }
    }
}

private const val SCORE_EXCELLENT = 90f
private const val SCORE_GREAT     = 75f
private const val SCORE_GOOD      = 50f
