package com.biztracker.feature.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddCircle
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.RemoveCircle
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.biztracker.R
import com.biztracker.ui.components.EmptyView
import com.biztracker.ui.components.ErrorView
import com.biztracker.ui.components.LoadingView
import com.biztracker.ui.components.SummaryCard
import com.biztracker.ui.theme.Dimens

@Composable
fun DashboardScreen(
    onAddIncome: () -> Unit,
    onAddExpense: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = hiltViewModel(),
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

        !uiState.hasAnyEntries -> {
            EmptyView(
                title = stringResource(id = R.string.dashboard_empty_title),
                message = stringResource(id = R.string.dashboard_empty_message),
                actionLabel = stringResource(id = R.string.action_add_expense),
                onAction = onAddExpense,
                modifier = modifier,
            )
        }

        else -> {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(Dimens.Space4),
                verticalArrangement = Arrangement.spacedBy(Dimens.Space3),
            ) {
                Header(isPro = uiState.isPro)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.Space2),
                ) {
                    FilledTonalButton(
                        onClick = onAddIncome,
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(imageVector = Icons.Rounded.AddCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(Dimens.Space1))
                        Text(text = stringResource(id = R.string.action_add_income))
                    }
                    FilledTonalButton(
                        onClick = onAddExpense,
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(imageVector = Icons.Rounded.RemoveCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(Dimens.Space1))
                        Text(text = stringResource(id = R.string.action_add_expense))
                    }
                }

                SummaryCard(
                    title = stringResource(id = R.string.dashboard_today),
                    income = uiState.todayIncome,
                    expense = uiState.todayExpense,
                )

                if (uiState.alerts.isNotEmpty()) {
                    ProfitAlertCard(alerts = uiState.alerts)
                }

                SummaryCard(
                    title = stringResource(id = R.string.dashboard_this_month),
                    income = uiState.monthIncome,
                    expense = uiState.monthExpense,
                )
            }
        }
    }
}

@Composable
private fun ProfitAlertCard(alerts: List<DashboardAlert>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.72f),
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.Space3),
            verticalArrangement = Arrangement.spacedBy(Dimens.Space1),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(Dimens.Space1),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Rounded.WarningAmber,
                    contentDescription = null,
                )
                Text(
                    text = stringResource(id = R.string.dashboard_alerts_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            alerts.forEach { alert ->
                Text(
                    text = when (alert) {
                        is DashboardAlert.ProfitDrop -> stringResource(
                            id = R.string.dashboard_alert_profit_drop,
                            alert.percentage,
                        )

                        is DashboardAlert.ExpenseSpike -> stringResource(
                            id = R.string.dashboard_alert_expense_spike,
                            alert.percentage,
                        )

                        DashboardAlert.ExpenseOverIncome -> stringResource(
                            id = R.string.dashboard_alert_expense_over_income,
                        )
                    },
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun Header(isPro: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.62f),
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.Space4),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimens.Space2),
            ) {
                Icon(
                    imageVector = Icons.Rounded.Dashboard,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = stringResource(id = R.string.tab_dashboard),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
            }

            if (isPro) {
                AssistChip(
                    onClick = {},
                    label = { Text(text = stringResource(id = R.string.pro_badge)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.WorkspacePremium,
                            contentDescription = null,
                        )
                    },
                )
            }
        }
    }
}
