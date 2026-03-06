package com.biztracker.feature.entries

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddCircle
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.RemoveCircle
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.biztracker.R
import com.biztracker.domain.Constants
import com.biztracker.domain.DateUtils
import com.biztracker.ui.components.BTCard
import com.biztracker.ui.components.BTEmptyState
import com.biztracker.ui.components.BTErrorState
import com.biztracker.ui.components.BTLoading
import com.biztracker.ui.theme.Dimens
import java.util.Locale

@Composable
fun EntriesScreen(
    onNavigateToEntryInput: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EntriesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val error = uiState.error
    var pendingDeleteId by remember { mutableStateOf<Long?>(null) }

    pendingDeleteId?.let { entryId ->
        AlertDialog(
            onDismissRequest = { pendingDeleteId = null },
            title = { Text(text = stringResource(id = R.string.entries_delete_dialog_title)) },
            text = { Text(text = stringResource(id = R.string.entries_delete_dialog_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteEntry(entryId)
                        pendingDeleteId = null
                    },
                ) {
                    Text(text = stringResource(id = R.string.action_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteId = null }) {
                    Text(text = stringResource(id = R.string.action_cancel))
                }
            },
        )
    }

    when {
        uiState.isLoading -> {
            BTLoading(modifier = modifier)
        }

        error != null -> {
            BTErrorState(
                message = error,
                onRetry = viewModel::refresh,
                modifier = modifier,
            )
        }

        uiState.entries.isEmpty() -> {
            BTEmptyState(
                title = stringResource(id = R.string.entries_empty_title),
                message = stringResource(id = R.string.entries_empty_message),
                action = {
                    Button(onClick = { onNavigateToEntryInput("expense") }) {
                        Text(text = stringResource(id = R.string.action_add_expense))
                    }
                },
                modifier = modifier.padding(Dimens.Space4),
            )
        }

        else -> {
            val groupedEntries = uiState.entries
                .groupBy { item -> item.occurredDate }
                .toSortedMap(compareByDescending { date -> date })

            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(Dimens.Space4),
                verticalArrangement = Arrangement.spacedBy(Dimens.Space3),
            ) {
                item {
                    Header(
                        filter = uiState.filter,
                        onFilterSelected = viewModel::setFilter,
                        onAddIncome = { onNavigateToEntryInput("income") },
                        onAddExpense = { onNavigateToEntryInput("expense") },
                    )
                }

                groupedEntries.forEach { (date, entriesForDate) ->
                    item(key = "date-$date") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.75f))
                                .padding(horizontal = Dimens.Space3, vertical = Dimens.Space2),
                            horizontalArrangement = Arrangement.spacedBy(Dimens.Space2),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CalendarMonth,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp),
                            )
                            Text(
                                text = date,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }

                    items(items = entriesForDate, key = { item -> item.id }) { item ->
                        BTCard(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(Dimens.Space3),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(Dimens.Space2),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Description,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp),
                                        )
                                        Text(
                                            text = item.memo.ifBlank { stringResource(id = R.string.entries_no_memo) },
                                            style = MaterialTheme.typography.bodyLarge,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                    }
                                    Text(
                                        text = listOf(
                                            localizedEntryType(item.type),
                                            item.categoryName,
                                            localizedPaymentMethod(item.paymentMethod),
                                        )
                                            .filter { it.isNotBlank() }
                                            .joinToString(" · "),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                                Column(
                                    horizontalAlignment = Alignment.End,
                                    verticalArrangement = Arrangement.spacedBy(Dimens.Space1),
                                ) {
                                    Text(
                                        text = DateUtils.formatCurrency(item.amount),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                    IconButton(onClick = { pendingDeleteId = item.id }) {
                                        Icon(
                                            imageVector = Icons.Rounded.Delete,
                                            contentDescription = stringResource(id = R.string.action_delete),
                                            tint = MaterialTheme.colorScheme.error,
                                        )
                                    }
                                }
                            }
                        }
                    }
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

@Composable
private fun Header(
    filter: EntryFilter,
    onFilterSelected: (EntryFilter) -> Unit,
    onAddIncome: () -> Unit,
    onAddExpense: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.Space2)) {
        Text(
            text = stringResource(id = R.string.entries_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimens.Space1),
        ) {
            FilledTonalButton(
                onClick = onAddIncome,
                modifier = Modifier.weight(1f),
            ) {
                Icon(imageVector = Icons.Rounded.AddCircle, contentDescription = null)
                Spacer(modifier = Modifier.width(Dimens.Space1))
                Text(text = stringResource(id = R.string.label_income))
            }
            FilledTonalButton(
                onClick = onAddExpense,
                modifier = Modifier.weight(1f),
            ) {
                Icon(imageVector = Icons.Rounded.RemoveCircle, contentDescription = null)
                Spacer(modifier = Modifier.width(Dimens.Space1))
                Text(text = stringResource(id = R.string.label_expense))
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(Dimens.Space1)) {
            EntryFilter.entries.forEach { option ->
                val label = when (option) {
                    EntryFilter.ALL -> stringResource(id = R.string.label_all)
                    EntryFilter.INCOME -> stringResource(id = R.string.label_income)
                    EntryFilter.EXPENSE -> stringResource(id = R.string.label_expense)
                }
                AssistChip(
                    onClick = { onFilterSelected(option) },
                    label = { Text(text = label) },
                    enabled = option != filter,
                )
            }
        }
    }
}
