package com.biztracker.feature.stats

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.biztracker.R
import com.biztracker.data.repository.BusinessRepository
import com.biztracker.data.repository.EntryRepository
import com.biztracker.domain.CategorySum
import com.biztracker.domain.DailySum
import com.biztracker.domain.DateUtils
import com.biztracker.domain.EntryHistory
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.YearMonth

data class StatsUiState(
    val yearMonth: String = DateUtils.yearMonth(DateUtils.today()),
    val dailySummaries: List<DailySum> = emptyList(),
    val categorySummaries: List<CategorySum> = emptyList(),
    val selectedDate: String? = null,
    val selectedDateEntries: List<EntryHistory> = emptyList(),
    val isHistoryLoading: Boolean = false,
    val isLoading: Boolean = true,
    val error: String? = null,
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val businessRepository: BusinessRepository,
    private val entryRepository: EntryRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

    private var businessId: Long? = null
    private var loadJob: Job? = null
    private var detailJob: Job? = null

    init {
        refresh()
    }

    fun prevMonth() {
        shiftMonth(-1)
    }

    fun nextMonth() {
        shiftMonth(1)
    }

    fun refresh() {
        loadStats(_uiState.value.yearMonth)
    }

    private fun shiftMonth(offset: Long) {
        val current = YearMonth.parse(_uiState.value.yearMonth)
        val next = current.plusMonths(offset).toString()
        _uiState.update { state ->
            state.copy(
                yearMonth = next,
                selectedDate = null,
                selectedDateEntries = emptyList(),
                isHistoryLoading = false,
            )
        }
        detailJob?.cancel()
        loadStats(next)
    }

    fun selectCalendarDate(date: String?) {
        detailJob?.cancel()

        if (date == null) {
            _uiState.update { state ->
                state.copy(
                    selectedDate = null,
                    selectedDateEntries = emptyList(),
                    isHistoryLoading = false,
                )
            }
            return
        }

        _uiState.update { state ->
            state.copy(
                selectedDate = date,
                selectedDateEntries = emptyList(),
                isHistoryLoading = true,
            )
        }

        detailJob = viewModelScope.launch {
            try {
                val resolvedBusinessId = resolveBusinessId()
                val entries = entryRepository.entryHistoryForDate(resolvedBusinessId, date)
                _uiState.update { state ->
                    if (state.selectedDate != date) {
                        state
                    } else {
                        state.copy(
                            selectedDateEntries = entries,
                            isHistoryLoading = false,
                        )
                    }
                }
            } catch (throwable: Throwable) {
                _uiState.update { state ->
                    state.copy(
                        selectedDateEntries = emptyList(),
                        isHistoryLoading = false,
                        error = throwable.message
                            ?: appContext.getString(R.string.stats_error_load_selected_date_entries_failed),
                    )
                }
            }
        }
    }

    private fun loadStats(yearMonth: String) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.update { state ->
                state.copy(isLoading = true, error = null)
            }

            try {
                val resolvedBusinessId = resolveBusinessId()

                val dailySums = entryRepository.dailySumsForMonth(resolvedBusinessId, yearMonth)
                val categorySums = entryRepository.expenseSumsByCategoryForMonth(
                    resolvedBusinessId,
                    yearMonth,
                )

                val selectedDate = _uiState.value.selectedDate
                val selectedDateEntries = if (selectedDate != null && selectedDate.startsWith(yearMonth)) {
                    entryRepository.entryHistoryForDate(resolvedBusinessId, selectedDate)
                } else {
                    emptyList()
                }

                _uiState.update { state ->
                    state.copy(
                        dailySummaries = dailySums,
                        categorySummaries = categorySums,
                        selectedDate = state.selectedDate?.takeIf { it.startsWith(yearMonth) },
                        selectedDateEntries = selectedDateEntries,
                        isHistoryLoading = false,
                        isLoading = false,
                        error = null,
                    )
                }
            } catch (throwable: Throwable) {
                _uiState.update { state ->
                    state.copy(
                        dailySummaries = emptyList(),
                        categorySummaries = emptyList(),
                        selectedDate = null,
                        selectedDateEntries = emptyList(),
                        isHistoryLoading = false,
                        isLoading = false,
                        error = throwable.message ?: appContext.getString(R.string.stats_error_load_failed),
                    )
                }
            }
        }
    }

    private suspend fun resolveBusinessId(): Long {
        return businessId ?: withContext(Dispatchers.IO) {
            businessRepository
                .getOrCreateDefaultBusiness()
                .id
                .also { id -> businessId = id }
        }
    }
}
