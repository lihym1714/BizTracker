package com.biztracker.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.biztracker.ui.theme.BTSpacing

@Composable
fun BTSegmentedToggle(
    leftLabel: String,
    rightLabel: String,
    selected: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(BTSpacing.xs),
    ) {
        FilterChip(
            selected = selected == leftLabel,
            onClick = { onSelect(leftLabel) },
            label = { Text(text = leftLabel) },
        )
        FilterChip(
            selected = selected == rightLabel,
            onClick = { onSelect(rightLabel) },
            label = { Text(text = rightLabel) },
        )
    }
}
