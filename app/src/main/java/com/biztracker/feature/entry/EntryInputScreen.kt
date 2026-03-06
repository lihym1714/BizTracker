package com.biztracker.feature.entry

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.StickyNote2
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.MonetizationOn
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material3.AssistChip
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.biztracker.R
import com.biztracker.domain.Constants
import com.biztracker.domain.DateUtils
import com.biztracker.ui.components.ErrorView
import com.biztracker.ui.components.LoadingView
import com.biztracker.ui.theme.Dimens
import java.util.Locale
import kotlinx.coroutines.flow.collectLatest

@Composable
@OptIn(ExperimentalLayoutApi::class)
fun EntryInputScreen(
    initialType: String,
    onBack: () -> Unit,
    onSaved: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EntryInputViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val error = uiState.error

    LaunchedEffect(initialType) {
        viewModel.setInitialType(initialType)
    }

    LaunchedEffect(viewModel) {
        viewModel.effects.collectLatest { effect ->
            if (effect is EntryInputEffect.Saved) {
                onSaved()
            }
        }
    }

    if (error != null) {
        ErrorView(
            message = error,
            onRetry = viewModel::clearError,
            modifier = modifier,
        )
        return
    }

    if (uiState.isSaving) {
        LoadingView(modifier = modifier)
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Dimens.Space4),
        verticalArrangement = Arrangement.spacedBy(Dimens.Space3),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(Dimens.Space2),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = stringResource(id = R.string.cd_back),
                    )
                }
                Text(
                    text = stringResource(id = R.string.entry_input_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            FilledTonalButton(onClick = viewModel::save) {
                Icon(imageVector = Icons.Rounded.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(Dimens.Space1))
                Text(text = stringResource(id = R.string.action_save))
            }
        }

        SectionTitle(
            icon = Icons.Rounded.Category,
            text = stringResource(id = R.string.entry_input_type_label),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimens.Space2),
        ) {
            FilterChip(
                selected = uiState.type == "INCOME",
                onClick = { viewModel.setType("income") },
                label = { Text(stringResource(id = R.string.label_income)) },
            )
            FilterChip(
                selected = uiState.type == "EXPENSE",
                onClick = { viewModel.setType("expense") },
                label = { Text(stringResource(id = R.string.label_expense)) },
            )
        }

        if (uiState.quickPresets.isNotEmpty()) {
            SectionTitle(
                icon = Icons.Rounded.MonetizationOn,
                text = stringResource(id = R.string.entry_input_quick_presets_title),
            )

            if (!uiState.isPro) {
                Text(
                    text = stringResource(id = R.string.entry_input_quick_presets_free_note),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.Space2),
            ) {
                items(
                    items = uiState.quickPresets,
                    key = { preset -> preset.id },
                ) { preset ->
                    AssistChip(
                        onClick = { viewModel.applyQuickPreset(preset) },
                        label = {
                            Text(
                                text = presetLabel(preset),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        },
                    )
                }
            }
        }

        OutlinedTextField(
            value = uiState.amount,
            onValueChange = viewModel::setAmount,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(id = R.string.entry_input_amount_label)) },
            singleLine = true,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Rounded.MonetizationOn,
                    contentDescription = null,
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        )

        OutlinedTextField(
            value = uiState.occurredDate,
            onValueChange = viewModel::setDate,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(id = R.string.entry_input_date_label)) },
            singleLine = true,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Rounded.CalendarMonth,
                    contentDescription = null,
                )
            },
        )

        SectionTitle(
            icon = Icons.Rounded.Category,
            text = stringResource(id = R.string.label_category),
        )
        if (uiState.categories.isEmpty()) {
            AssistChip(
                onClick = {},
                enabled = false,
                label = {
                    Text(
                        text = stringResource(id = R.string.entry_input_no_categories),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
            )
        } else {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(Dimens.Space2),
                verticalArrangement = Arrangement.spacedBy(Dimens.Space2),
            ) {
                FilterChip(
                    selected = uiState.categoryId == null,
                    onClick = { viewModel.setCategory(null) },
                    label = { Text(stringResource(id = R.string.label_none)) },
                )
                uiState.categories.forEach { category ->
                    FilterChip(
                        selected = uiState.categoryId == category.id,
                        onClick = { viewModel.setCategory(category.id) },
                        label = { Text(category.name) },
                    )
                }
            }
        }

        SectionTitle(
            icon = Icons.Rounded.Payments,
            text = stringResource(id = R.string.label_payment_method),
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(Dimens.Space2),
            verticalArrangement = Arrangement.spacedBy(Dimens.Space2),
        ) {
            PAYMENT_METHODS.forEach { method ->
                FilterChip(
                    selected = uiState.paymentMethod == method,
                    onClick = { viewModel.setPaymentMethod(method) },
                    label = { Text(localizedPaymentMethod(method)) },
                )
            }
        }

        OutlinedTextField(
            value = uiState.memo,
            onValueChange = viewModel::setMemo,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(id = R.string.label_memo)) },
            minLines = 2,
            maxLines = 5,
            leadingIcon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.StickyNote2,
                    contentDescription = null,
                )
            },
        )

        Text(
            text = stringResource(id = R.string.entry_input_today_format, DateUtils.today()),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private val PAYMENT_METHODS = listOf(
    Constants.PAYMENT_CASH,
    Constants.PAYMENT_CARD,
    Constants.PAYMENT_TRANSFER,
)

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
private fun localizedEntryType(rawValue: String): String {
    return when (rawValue.uppercase(Locale.ROOT)) {
        Constants.TYPE_INCOME -> stringResource(id = R.string.label_income)
        Constants.TYPE_EXPENSE -> stringResource(id = R.string.label_expense)
        else -> rawValue
    }
}

@Composable
private fun presetLabel(preset: EntryQuickPreset): String {
    val memo = preset.memo.trim()
    if (memo.isNotEmpty()) {
        return memo
    }

    return stringResource(
        id = R.string.entry_input_quick_preset_format,
        localizedEntryType(preset.type),
        DateUtils.formatCurrency(preset.amount),
        localizedPaymentMethod(preset.paymentMethod),
    )
}

@Composable
private fun SectionTitle(
    icon: ImageVector,
    text: String,
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
            text = text,
            style = MaterialTheme.typography.titleSmall,
        )
    }
}
