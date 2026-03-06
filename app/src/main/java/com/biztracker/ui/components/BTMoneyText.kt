package com.biztracker.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.biztracker.domain.DateUtils

@Composable
fun BTMoneyText(
    amount: Long,
    modifier: Modifier = Modifier,
    emphasize: Boolean = false,
) {
    Text(
        modifier = modifier,
        text = DateUtils.formatCurrency(amount),
        style = if (emphasize) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge,
        fontFamily = FontFamily.Monospace,
        fontWeight = if (emphasize) FontWeight.SemiBold else FontWeight.Medium,
    )
}
