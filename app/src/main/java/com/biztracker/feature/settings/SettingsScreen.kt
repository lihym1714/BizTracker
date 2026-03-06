package com.biztracker.feature.settings

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.FileUpload
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.LocalOffer
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Stars
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.biztracker.R
import com.biztracker.data.local.Category
import com.biztracker.data.local.EntryType
import com.biztracker.ui.components.CategoryRow
import com.biztracker.ui.components.EmptyView
import com.biztracker.ui.components.ErrorView
import com.biztracker.ui.components.LoadingView
import com.biztracker.ui.theme.Dimens

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val error = uiState.error
    val context = LocalContext.current

    var showAddDialog by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<Category?>(null) }
    var deletingCategory by remember { mutableStateOf<Category?>(null) }

    LaunchedEffect(context) {
        viewModel.attachPurchaseActivity(context.findActivity())
    }

    LaunchedEffect(uiState.exportState) {
        val exportState = uiState.exportState
        if (exportState is ExportState.Success) {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_SUBJECT, exportState.fileName)
                putExtra(Intent.EXTRA_TEXT, exportState.csvContent)
            }
            context.startActivity(
                Intent.createChooser(
                    shareIntent,
                    context.getString(R.string.settings_export_share_chooser),
                )
            )
            viewModel.clearExportState()
        }
    }

    when {
        uiState.isLoading -> {
            LoadingView(modifier = modifier)
            return
        }

        error != null -> {
            ErrorView(
                message = error,
                onRetry = {
                    viewModel.clearError()
                    viewModel.refresh()
                },
                modifier = modifier,
            )
            return
        }
    }

    if (showAddDialog) {
        CategoryEditDialog(
            title = stringResource(id = R.string.settings_category_add),
            initialName = "",
            onDismiss = { showAddDialog = false },
            onConfirm = { name ->
                viewModel.addCategory(name)
                showAddDialog = false
            },
        )
    }

    editingCategory?.let { category ->
        CategoryEditDialog(
            title = stringResource(id = R.string.settings_category_edit),
            initialName = category.name,
            onDismiss = { editingCategory = null },
            onConfirm = { name ->
                viewModel.updateCategory(category, name)
                editingCategory = null
            },
        )
    }

    deletingCategory?.let { category ->
        AlertDialog(
            onDismissRequest = { deletingCategory = null },
            title = { Text(text = stringResource(id = R.string.settings_category_delete_title)) },
            text = {
                Text(
                    text = stringResource(
                        id = R.string.settings_category_delete_message,
                        category.name,
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteCategory(category)
                        deletingCategory = null
                    },
                ) {
                    Text(text = stringResource(id = R.string.action_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingCategory = null }) {
                    Text(text = stringResource(id = R.string.action_cancel))
                }
            },
        )
    }

    val selectedCategories = uiState.categories.filter { category ->
        category.type == uiState.selectedType
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(Dimens.Space4),
        verticalArrangement = Arrangement.spacedBy(Dimens.Space3),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.Space2),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Settings,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = stringResource(id = R.string.settings_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        item {
            SectionTitle(
                icon = Icons.Rounded.Language,
                title = stringResource(id = R.string.settings_language_title),
            )
        }

        item {
            TabRow(selectedTabIndex = if (uiState.languageTag == LANGUAGE_TAG_ENGLISH) 1 else 0) {
                Tab(
                    selected = uiState.languageTag == LANGUAGE_TAG_KOREAN,
                    onClick = { viewModel.setLanguage(LANGUAGE_TAG_KOREAN) },
                    text = { Text(text = stringResource(id = R.string.settings_language_korean)) },
                )
                Tab(
                    selected = uiState.languageTag == LANGUAGE_TAG_ENGLISH,
                    onClick = { viewModel.setLanguage(LANGUAGE_TAG_ENGLISH) },
                    text = { Text(text = stringResource(id = R.string.settings_language_english)) },
                )
            }
        }

        item {
            SectionTitle(
                icon = Icons.Rounded.LocalOffer,
                title = stringResource(id = R.string.settings_categories_title),
            )
        }

        item {
            TabRow(selectedTabIndex = if (uiState.selectedType == EntryType.INCOME) 0 else 1) {
                Tab(
                    selected = uiState.selectedType == EntryType.INCOME,
                    onClick = { viewModel.setCategoryType(EntryType.INCOME) },
                    text = { Text(text = stringResource(id = R.string.label_income)) },
                )
                Tab(
                    selected = uiState.selectedType == EntryType.EXPENSE,
                    onClick = { viewModel.setCategoryType(EntryType.EXPENSE) },
                    text = { Text(text = stringResource(id = R.string.label_expense)) },
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                FilledTonalButton(onClick = { showAddDialog = true }) {
                    Icon(imageVector = Icons.Rounded.LocalOffer, contentDescription = null)
                    Spacer(modifier = Modifier.width(Dimens.Space1))
                    Text(text = stringResource(id = R.string.settings_category_add))
                }
            }
        }

        if (selectedCategories.isEmpty()) {
            item {
                EmptyView(
                    title = stringResource(id = R.string.settings_categories_empty_title),
                    message = stringResource(id = R.string.settings_categories_empty_message),
                    actionLabel = stringResource(id = R.string.settings_categories_empty_action),
                    onAction = { showAddDialog = true },
                )
            }
        } else {
            items(selectedCategories, key = { category -> category.id }) { category ->
                CategoryRow(
                    category = category,
                    onEdit = { editingCategory = category },
                    onDelete = { deletingCategory = category },
                )
                HorizontalDivider()
            }
        }

        item {
            SectionTitle(
                icon = Icons.Rounded.FileUpload,
                title = stringResource(id = R.string.settings_export_title),
            )
        }

        item {
            TabRow(selectedTabIndex = if (uiState.exportFormat == EXPORT_FORMAT_SETTLEMENT) 1 else 0) {
                Tab(
                    selected = uiState.exportFormat == EXPORT_FORMAT_STANDARD,
                    onClick = { viewModel.setExportFormat(EXPORT_FORMAT_STANDARD) },
                    text = { Text(text = stringResource(id = R.string.settings_export_format_standard)) },
                )
                Tab(
                    enabled = uiState.isPro,
                    selected = uiState.exportFormat == EXPORT_FORMAT_SETTLEMENT,
                    onClick = { viewModel.setExportFormat(EXPORT_FORMAT_SETTLEMENT) },
                    text = { Text(text = stringResource(id = R.string.settings_export_format_settlement)) },
                )
            }
        }

        item {
            SectionTitle(
                icon = Icons.Rounded.Settings,
                title = stringResource(id = R.string.settings_alert_thresholds_title),
            )
        }

        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(Dimens.Space2),
            ) {
                Text(
                    text = stringResource(
                        id = R.string.settings_alert_profit_drop_threshold,
                        uiState.profitDropThresholdPercent,
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Slider(
                    value = uiState.profitDropThresholdPercent.toFloat(),
                    onValueChange = { value ->
                        viewModel.setProfitDropThresholdPercent(value.toInt())
                    },
                    valueRange = 5f..90f,
                )

                Text(
                    text = stringResource(
                        id = R.string.settings_alert_expense_spike_threshold,
                        uiState.expenseSpikeThresholdPercent,
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Slider(
                    value = uiState.expenseSpikeThresholdPercent.toFloat(),
                    onValueChange = { value ->
                        viewModel.setExpenseSpikeThresholdPercent(value.toInt())
                    },
                    valueRange = 10f..200f,
                )
            }
        }

        item {
            MonthSelector(
                yearMonth = uiState.yearMonth,
                onPrevious = viewModel::previousMonth,
                onNext = viewModel::nextMonth,
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = stringResource(id = R.string.settings_export_utf8_bom))
                Switch(
                    checked = uiState.includeUtf8Bom,
                    onCheckedChange = viewModel::setIncludeUtf8Bom,
                )
            }
        }

        item {
            val exporting = uiState.exportState is ExportState.Exporting
            Button(
                onClick = viewModel::exportCsv,
                enabled = !exporting,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = if (exporting) {
                        stringResource(id = R.string.settings_exporting)
                    } else if (uiState.exportFormat == EXPORT_FORMAT_SETTLEMENT) {
                        stringResource(id = R.string.settings_export_button_settlement)
                    } else {
                        stringResource(id = R.string.settings_export_button)
                    }
                )
            }
        }

        item {
            SectionTitle(
                icon = Icons.Rounded.Stars,
                title = stringResource(id = R.string.settings_pro_title),
            )
        }

        if (!uiState.isPro) {
            item {
                Button(
                    onClick = viewModel::purchasePro,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(imageVector = Icons.Rounded.Stars, contentDescription = null)
                    Spacer(modifier = Modifier.width(Dimens.Space1))
                    Text(text = stringResource(id = R.string.settings_buy_pro))
                }
            }

            item {
                Text(
                    text = stringResource(id = R.string.settings_pro_promo_profit_tracker),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        item {
            TextButton(
                onClick = viewModel::restorePurchase,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(text = stringResource(id = R.string.settings_restore_purchase))
            }
        }
    }
}

@Composable
private fun MonthSelector(
    yearMonth: String,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimens.Space1),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.Start,
        ) {
            TextButton(onClick = onPrevious) {
                Icon(imageVector = Icons.Rounded.ArrowBackIosNew, contentDescription = null)
                Spacer(modifier = Modifier.width(Dimens.Space1))
                Text(text = stringResource(id = R.string.action_previous))
            }
        }
        Text(
            text = yearMonth,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
        )
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.End,
        ) {
            TextButton(onClick = onNext) {
                Text(text = stringResource(id = R.string.action_next))
                Spacer(modifier = Modifier.width(Dimens.Space1))
                Icon(imageVector = Icons.AutoMirrored.Rounded.ArrowForwardIos, contentDescription = null)
            }
        }
    }
}

@Composable
private fun SectionTitle(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimens.Space2),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun CategoryEditDialog(
    title: String,
    initialName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var text by remember(initialName) { mutableStateOf(initialName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { value -> text = value },
                singleLine = true,
                label = { Text(text = stringResource(id = R.string.label_name)) },
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(text) }) {
                Text(text = stringResource(id = R.string.action_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(id = R.string.action_cancel))
            }
        },
    )
}

private fun Context.findActivity(): Activity? {
    var current = this
    while (current is ContextWrapper) {
        if (current is Activity) {
            return current
        }
        current = current.baseContext
    }
    return null
}

private const val LANGUAGE_TAG_KOREAN = "ko"
private const val LANGUAGE_TAG_ENGLISH = "en"
private const val EXPORT_FORMAT_STANDARD = "standard"
private const val EXPORT_FORMAT_SETTLEMENT = "settlement"
