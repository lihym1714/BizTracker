package com.biztracker.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.biztracker.R
import com.biztracker.domain.Constants
import com.biztracker.domain.DateUtils
import com.biztracker.feature.entries.EntryListItem
import java.util.Locale

@Composable
fun EntryRow(
    item: EntryListItem,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = item.memo.ifBlank { stringResource(id = R.string.entries_no_memo) },
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                )
                Text(
                    text = stringResource(
                        id = R.string.entries_meta_format,
                        item.categoryName.ifBlank { stringResource(id = R.string.entries_uncategorized) },
                        localizedPaymentMethod(item.paymentMethod),
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = DateUtils.formatCurrency(item.amount),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = localizedEntryType(item.type),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                TextButton(onClick = onDelete) {
                    Text(text = stringResource(id = R.string.action_delete))
                }
            }
        }
    }
}

@Composable
private fun localizedEntryType(rawValue: String): String {
    return when (rawValue.uppercase(Locale.ROOT)) {
        Constants.TYPE_INCOME -> stringResource(id = R.string.label_income)
        Constants.TYPE_EXPENSE -> stringResource(id = R.string.label_expense)
        else -> rawValue
    }
}

@Composable
private fun localizedPaymentMethod(rawValue: String): String {
    return when (rawValue.uppercase(Locale.ROOT)) {
        Constants.PAYMENT_CASH -> stringResource(id = R.string.payment_method_cash)
        Constants.PAYMENT_CARD -> stringResource(id = R.string.payment_method_card)
        Constants.PAYMENT_TRANSFER -> stringResource(id = R.string.payment_method_transfer)
        else -> rawValue
    }
}
