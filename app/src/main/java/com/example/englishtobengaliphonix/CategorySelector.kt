package com.example.englishtobengaliphonix

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class LetterCategory(val label: String) {
    ALL("All"),
    VOWELS("Vowels/স্বরবর্ণ"),
    CONSONANTS("Consonants/ব্যঞ্জনবর্ণ"),
    NUMBERS("Numbers/সংখ্যা")
}

@Composable
fun CategorySelector(
    selectedCategory: LetterCategory,
    onCategorySelected: (LetterCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    // Stable alternative to FlowRow using Column and Rows
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val categories = LetterCategory.entries
        
        // Row 1: All and Vowels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CategoryChip(
                label = categories[0].label,
                isSelected = categories[0] == selectedCategory,
                onClick = { onCategorySelected(categories[0]) },
                modifier = Modifier.weight(1f)
            )
            CategoryChip(
                label = categories[1].label,
                isSelected = categories[1] == selectedCategory,
                onClick = { onCategorySelected(categories[1]) },
                modifier = Modifier.weight(1f)
            )
        }
        
        // Row 2: Consonants and Numbers
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CategoryChip(
                label = categories[2].label,
                isSelected = categories[2] == selectedCategory,
                onClick = { onCategorySelected(categories[2]) },
                modifier = Modifier.weight(1f)
            )
            CategoryChip(
                label = categories[3].label,
                isSelected = categories[3] == selectedCategory,
                onClick = { onCategorySelected(categories[3]) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun CategoryChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected)
            MaterialTheme.colorScheme.primary
        else
            MaterialTheme.colorScheme.surfaceVariant,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "chipBg"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isSelected)
            MaterialTheme.colorScheme.onPrimary
        else
            MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "chipContent"
    )

    val shadowElevation = if (isSelected) 4.dp else 0.dp

    Box(
        modifier = modifier
            .shadow(elevation = shadowElevation, shape = RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = Color.White.copy(alpha = 0.3f))
            ) { onClick() }
            .padding(horizontal = 8.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            // Replace / with newline to show Bengali text below English
            text = label.replace("/", "\n"),
            color = contentColor,
            fontSize = 12.sp,
            lineHeight = 14.sp,
            textAlign = TextAlign.Center,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            maxLines = 2
        )
    }
}
