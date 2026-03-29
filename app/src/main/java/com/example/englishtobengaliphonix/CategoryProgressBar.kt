package com.example.englishtobengaliphonix

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/**
 * A compact progress bar showing how many letters in the current category
 * the user has already scored at least once, plus a "Clear" button to wipe
 * all stored progress.
 */
@Composable
fun CategoryProgressBar(
    practicedCount: Int,
    totalCount: Int,
    onClearProgress: () -> Unit,
    modifier: Modifier = Modifier
) {
    val fraction = if (totalCount > 0) practicedCount.toFloat() / totalCount else 0f
    val clearDescription = stringResource(R.string.cd_clear_progress)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = stringResource(R.string.progress_label, practicedCount, totalCount),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            LinearProgressIndicator(
                progress = { fraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
            )
        }
        TextButton(
            onClick = onClearProgress,
            modifier = Modifier.semantics { contentDescription = clearDescription },
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = stringResource(R.string.btn_clear_progress),
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}
