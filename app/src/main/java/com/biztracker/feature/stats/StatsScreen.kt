package com.biztracker.feature.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.DataUsage
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.PieChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.biztracker.R
import com.biztracker.domain.CategorySum
import com.biztracker.domain.Constants
import com.biztracker.domain.DailySum
import com.biztracker.domain.DateUtils
import com.biztracker.domain.EntryHistory
import com.biztracker.ui.components.EmptyView
import com.biztracker.ui.components.ErrorView
import com.biztracker.ui.components.LoadingView
import com.biztracker.ui.theme.Dimens
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.abs

@Composable
fun StatsScreen(
    modifier: Modifier = Modifier,
    viewModel: StatsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val error = uiState.error

    when {
        uiState.isLoading -> {
            LoadingView(modifier = modifier)
        }

        error != null -> {
            ErrorView(
                message = error,
                onRetry = viewModel::refresh,
                modifier = modifier,
            )
        }

        else -> {
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(Dimens.Space4),
                verticalArrangement = Arrangement.spacedBy(Dimens.Space3),
            ) {
                item {
                    MonthHeader(
                        yearMonth = uiState.yearMonth,
                        onPrev = viewModel::prevMonth,
                        onNext = viewModel::nextMonth,
                    )
                }

                item {
                    SectionHeader(
                        icon = Icons.Rounded.DataUsage,
                        title = stringResource(id = R.string.stats_daily_title),
                    )
                }

                if (uiState.dailySummaries.isEmpty()) {
                    item {
                        EmptyView(
                            title = stringResource(id = R.string.stats_empty_title),
                            message = stringResource(id = R.string.stats_no_daily_data),
                            actionLabel = stringResource(id = R.string.action_retry),
                            onAction = viewModel::refresh,
                        )
                    }
                } else {
                    items(uiState.dailySummaries, key = { item -> item.date }) { daily ->
                        DailySummaryRow(daily)
                    }
                }

                item {
                    SectionHeader(
                        modifier = Modifier.padding(top = Dimens.Space2),
                        icon = Icons.Rounded.PieChart,
                        title = stringResource(id = R.string.stats_category_title),
                    )
                }

                if (uiState.categorySummaries.isEmpty()) {
                    item {
                        Text(
                            text = stringResource(id = R.string.stats_no_category_data),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                } else {
                    items(uiState.categorySummaries, key = { item -> item.categoryId }) { category ->
                        CategorySummaryRow(category)
                    }
                }

                item {
                    SectionHeader(
                        modifier = Modifier.padding(top = Dimens.Space2),
                        icon = Icons.Rounded.CalendarMonth,
                        title = stringResource(id = R.string.stats_calendar_title),
                    )
                }

                item {
                    MonthlyCalendar(
                        yearMonth = uiState.yearMonth,
                        dailySummaries = uiState.dailySummaries,
                        selectedDate = uiState.selectedDate,
                        onDateToggle = viewModel::selectCalendarDate,
                    )
                }

                uiState.selectedDate?.let { selectedDate ->
                    item {
                        SectionHeader(
                            modifier = Modifier.padding(top = Dimens.Space2),
                            icon = Icons.Rounded.Description,
                            title = stringResource(
                                id = R.string.stats_selected_date_history_title,
                                selectedDate,
                            ),
                        )
                    }

                    if (uiState.isHistoryLoading) {
                        item {
                            Text(
                                text = stringResource(id = R.string.loading),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    } else if (uiState.selectedDateEntries.isEmpty()) {
                        item {
                            Text(
                                text = stringResource(id = R.string.stats_selected_date_no_entries),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    } else {
                        items(
                            items = uiState.selectedDateEntries,
                            key = { item -> item.id },
                        ) { history ->
                            SelectedDateHistoryRow(history = history)
                        }
                    }
                }
            }
        }
    }
}

private data class CalendarCell(
    val dayOfMonth: Int,
    val summary: DailySum?,
)

@Composable
private fun MonthHeader(
    yearMonth: String,
    onPrev: () -> Unit,
    onNext: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.Space3),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FilledTonalButton(onClick = onPrev) {
                Icon(imageVector = Icons.Rounded.ArrowBackIosNew, contentDescription = null)
            }
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(Dimens.Space2),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Rounded.CalendarMonth,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = yearMonth,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
            }
            FilledTonalButton(onClick = onNext) {
                Icon(imageVector = Icons.AutoMirrored.Rounded.ArrowForwardIos, contentDescription = null)
            }
        }
    }
}

@Composable
private fun DailySummaryRow(daily: DailySum) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.Space3),
            verticalArrangement = Arrangement.spacedBy(Dimens.Space1),
        ) {
            Text(
                text = daily.date,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            StatLine(
                label = stringResource(id = R.string.label_income),
                value = DateUtils.formatCurrency(daily.income),
            )
            StatLine(
                label = stringResource(id = R.string.label_expense),
                value = DateUtils.formatCurrency(daily.expense),
            )
            StatLine(
                label = stringResource(id = R.string.label_profit),
                value = DateUtils.formatCurrency(daily.profit),
            )
        }
    }
}

@Composable
private fun CategorySummaryRow(category: CategorySum) {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.Space3),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(Dimens.Space2),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Analytics,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text(text = category.categoryName)
            }
            Text(
                text = DateUtils.formatCurrency(category.sum),
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.End,
            )
        }
    }
}

@Composable
private fun SelectedDateHistoryRow(history: EntryHistory) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.Space3),
            verticalArrangement = Arrangement.spacedBy(Dimens.Space1),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = localizedEntryType(history.type),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = DateUtils.formatCurrency(history.amount),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            Text(
                text = listOf(
                    history.categoryName.ifBlank { stringResource(id = R.string.entries_uncategorized) },
                    localizedPaymentMethod(history.paymentMethod),
                ).joinToString(" · "),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (history.memo.isNotBlank()) {
                Text(
                    text = history.memo,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun MonthlyCalendar(
    yearMonth: String,
    dailySummaries: List<DailySum>,
    selectedDate: String?,
    onDateToggle: (String?) -> Unit,
) {
    val locale = Locale.getDefault()
    val dayLabels = remember(locale) {
        listOf(
            DayOfWeek.SUNDAY,
            DayOfWeek.MONDAY,
            DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY,
            DayOfWeek.FRIDAY,
            DayOfWeek.SATURDAY,
        ).map { dayOfWeek ->
            dayOfWeek.getDisplayName(TextStyle.NARROW, locale)
        }
    }
    val calendarRows = remember(yearMonth, dailySummaries) {
        buildCalendarRows(yearMonth = yearMonth, dailySummaries = dailySummaries)
    }
    val selectedDay = remember(yearMonth, selectedDate) {
        if (selectedDate != null && selectedDate.startsWith(yearMonth)) {
            runCatching { LocalDate.parse(selectedDate).dayOfMonth }.getOrNull()
        } else {
            null
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimens.Space2),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimens.Space1),
        ) {
            dayLabels.forEach { dayLabel ->
                Text(
                    text = dayLabel,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(1.dp),
        ) {
            calendarRows.forEach { week ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(1.dp),
                ) {
                    week.forEach { cell ->
                        CalendarDayCell(
                            cell = cell,
                            isSelected = cell?.dayOfMonth == selectedDay,
                            onClick = { dayOfMonth ->
                                val nextDate = if (selectedDay == dayOfMonth) {
                                    null
                                } else {
                                    "$yearMonth-${dayOfMonth.toString().padStart(2, '0')}"
                                }
                                onDateToggle(nextDate)
                            },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarDayCell(
    cell: CalendarCell?,
    isSelected: Boolean,
    onClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (cell == null) {
        Column(modifier = modifier.aspectRatio(1f)) {}
        return
    }

    val summary = cell.summary
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.34f)
    } else {
        Color.Transparent
    }

    Column(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick(cell.dayOfMonth) }
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(8.dp),
            )
            .padding(horizontal = 2.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = cell?.dayOfMonth?.toString().orEmpty(),
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.End,
        )

        Text(
            text = summary?.let { value -> formatCompactSigned(value.profit) }.orEmpty(),
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = when {
                summary == null -> Color.Transparent
                summary.profit > 0L -> MaterialTheme.colorScheme.primary
                summary.profit < 0L -> MaterialTheme.colorScheme.error
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            },
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
    }
}

private fun buildCalendarRows(
    yearMonth: String,
    dailySummaries: List<DailySum>,
): List<List<CalendarCell?>> {
    val month = YearMonth.parse(yearMonth)
    val summaryByDay = dailySummaries.mapNotNull { summary ->
        runCatching {
            LocalDate.parse(summary.date).dayOfMonth to summary
        }.getOrNull()
    }.toMap()

    val firstOffset = month.atDay(1).dayOfWeek.value % 7
    val lengthOfMonth = month.lengthOfMonth()
    val totalCells = ((firstOffset + lengthOfMonth + 6) / 7) * 7

    val cells = List(totalCells) { index ->
        val dayOfMonth = index - firstOffset + 1
        if (dayOfMonth in 1..lengthOfMonth) {
            CalendarCell(
                dayOfMonth = dayOfMonth,
                summary = summaryByDay[dayOfMonth],
            )
        } else {
            null
        }
    }

    return cells.chunked(7)
}

private fun formatCompactSigned(value: Long): String {
    val absolute = abs(value)
    val formatted = when {
        absolute >= 1_000_000L -> {
            val scaled = absolute / 100_000L
            if (scaled % 10L == 0L) {
                "${scaled / 10}M"
            } else {
                "${scaled / 10}.${scaled % 10}M"
            }
        }

        absolute >= 1_000L -> {
            val scaled = absolute / 100L
            if (scaled % 10L == 0L) {
                "${scaled / 10}K"
            } else {
                "${scaled / 10}.${scaled % 10}K"
            }
        }

        else -> absolute.toString()
    }

    return when {
        value > 0L -> "+$formatted"
        value < 0L -> "-$formatted"
        else -> "0"
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
private fun SectionHeader(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
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
private fun StatLine(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
        )
    }
}
