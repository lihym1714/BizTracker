package com.biztracker.feature.dashboard

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.biztracker.R
import com.biztracker.data.local.Entry
import com.biztracker.billing.ProStatusRepository
import com.biztracker.data.repository.BusinessRepository
import com.biztracker.data.repository.EntryRepository
import com.biztracker.domain.DateUtils
import com.biztracker.domain.Prefs
import com.biztracker.domain.Summary
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface DashboardAlert {
    data class ProfitDrop(
        val percentage: Int,
    ) : DashboardAlert

    data class ExpenseSpike(
        val percentage: Int,
    ) : DashboardAlert

    data object ExpenseOverIncome : DashboardAlert
}

data class DashboardUiState(
    val todayIncome: Long = 0L,
    val todayExpense: Long = 0L,
    val monthIncome: Long = 0L,
    val monthExpense: Long = 0L,
    val alerts: List<DashboardAlert> = emptyList(),
    val isPro: Boolean = false,
    val isLoading: Boolean = true,
    val error: String? = null,
    val hasAnyEntries: Boolean = false,
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val businessRepository: BusinessRepository,
    private val entryRepository: EntryRepository,
    private val proStatusRepository: ProStatusRepository,
    private val prefs: Prefs,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private var observeJob: Job? = null
    private var profitDropThresholdPercent: Int = DEFAULT_PROFIT_DROP_THRESHOLD_PERCENT
    private var expenseSpikeThresholdPercent: Int = DEFAULT_EXPENSE_SPIKE_THRESHOLD_PERCENT

    init {
        observeProStatus()
        observeAlertThresholds()
        refresh()
    }

    fun refresh() {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            _uiState.update { current ->
                current.copy(isLoading = true, error = null)
            }

            try {
                val businessId = withContext(Dispatchers.IO) {
                    businessRepository.getOrCreateDefaultBusiness().id
                }
                val today = DateUtils.today()
                val todayDate = LocalDate.parse(today)
                val yearMonth = DateUtils.yearMonth(today)
                val previousYearMonth = todayDate
                    .minusMonths(1)
                    .toString()
                    .substring(0, 7)

                combine(
                    entryRepository.todaySummary(businessId = businessId, date = today),
                    entryRepository.monthSummary(businessId = businessId, yearMonth = yearMonth),
                    entryRepository.monthSummary(businessId = businessId, yearMonth = previousYearMonth),
                    entryRepository.entries(businessId = businessId),
                ) { todaySummary, monthSummary, previousMonthSummary, entries ->
                    DashboardSnapshot(
                        todaySummary = todaySummary,
                        monthSummary = monthSummary,
                        previousMonthSummary = previousMonthSummary,
                        entries = entries,
                    )
                }.catch { throwable ->
                    _uiState.update { current ->
                        current.copy(
                            isLoading = false,
                            error = throwable.message ?: appContext.getString(R.string.dashboard_error_load_failed),
                        )
                    }
                }.collect { snapshot ->
                    val isProEnabled = _uiState.value.isPro
                    val alerts = buildAlerts(
                        monthSummary = snapshot.monthSummary,
                        previousMonthSummary = snapshot.previousMonthSummary,
                        entries = snapshot.entries,
                        today = todayDate,
                        isProEnabled = isProEnabled,
                    )
                    _uiState.update { current ->
                        current.copy(
                            todayIncome = snapshot.todaySummary.income,
                            todayExpense = snapshot.todaySummary.expense,
                            monthIncome = snapshot.monthSummary.income,
                            monthExpense = snapshot.monthSummary.expense,
                            hasAnyEntries = snapshot.entries.isNotEmpty(),
                            alerts = alerts,
                            isLoading = false,
                            error = null,
                        )
                    }
                }
            } catch (throwable: Throwable) {
                _uiState.update { current ->
                    current.copy(
                        isLoading = false,
                        error = throwable.message ?: appContext.getString(R.string.dashboard_error_load_failed),
                    )
                }
            }
        }
    }

    private fun buildAlerts(
        monthSummary: Summary,
        previousMonthSummary: Summary,
        entries: List<Entry>,
        today: LocalDate,
        isProEnabled: Boolean,
    ): List<DashboardAlert> {
        val result = mutableListOf<DashboardAlert>()

        val previousProfit = previousMonthSummary.profit
        val currentProfit = monthSummary.profit
        if (isProEnabled && previousProfit > 0L && currentProfit < previousProfit) {
            val delta = previousProfit - currentProfit
            val percentage = ((delta * 100L) / previousProfit).toInt()
            if (percentage >= profitDropThresholdPercent) {
                result += DashboardAlert.ProfitDrop(percentage = percentage)
            }
        }

        if (monthSummary.expense > monthSummary.income) {
            result += DashboardAlert.ExpenseOverIncome
        }

        val recentExpense = expenseInRange(
            entries = entries,
            start = today.minusDays(6),
            end = today,
        )
        val previousExpense = expenseInRange(
            entries = entries,
            start = today.minusDays(13),
            end = today.minusDays(7),
        )
        if (previousExpense > 0L && recentExpense > previousExpense) {
            val spike = recentExpense - previousExpense
            val percentage = ((spike * 100L) / previousExpense).toInt()
            if (isProEnabled && percentage >= expenseSpikeThresholdPercent) {
                result += DashboardAlert.ExpenseSpike(percentage = percentage)
            }
        }

        return result
    }

    private fun expenseInRange(
        entries: List<Entry>,
        start: LocalDate,
        end: LocalDate,
    ): Long {
        var sum = 0L
        entries.forEach { entry ->
            if (entry.type != EXPENSE_TYPE) {
                return@forEach
            }

            val date = runCatching { LocalDate.parse(entry.occurredDate) }.getOrNull() ?: return@forEach
            if ((date.isEqual(start) || date.isAfter(start)) && (date.isEqual(end) || date.isBefore(end))) {
                sum += entry.amount
            }
        }
        return sum
    }

    private fun observeProStatus() {
        viewModelScope.launch {
            proStatusRepository.isPro.collect { isPro ->
                _uiState.update { current ->
                    current.copy(isPro = isPro)
                }
            }
        }
    }

    private fun observeAlertThresholds() {
        viewModelScope.launch {
            prefs.profitDropThresholdPercent.collect { value ->
                profitDropThresholdPercent = value.coerceIn(5, 90)
            }
        }

        viewModelScope.launch {
            prefs.expenseSpikeThresholdPercent.collect { value ->
                expenseSpikeThresholdPercent = value.coerceIn(10, 200)
            }
        }
    }

    private data class DashboardSnapshot(
        val todaySummary: Summary,
        val monthSummary: Summary,
        val previousMonthSummary: Summary,
        val entries: List<Entry>,
    )

    private companion object {
        const val EXPENSE_TYPE = "EXPENSE"
        const val DEFAULT_PROFIT_DROP_THRESHOLD_PERCENT = 15
        const val DEFAULT_EXPENSE_SPIKE_THRESHOLD_PERCENT = 30
    }
}
