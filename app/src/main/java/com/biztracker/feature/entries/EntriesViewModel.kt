package com.biztracker.feature.entries

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.biztracker.R
import com.biztracker.data.local.Entry
import com.biztracker.data.local.EntryType
import com.biztracker.data.repository.BusinessRepository
import com.biztracker.data.repository.CategoryRepository
import com.biztracker.data.repository.EntryRepository
import com.biztracker.domain.Constants
import com.biztracker.domain.EntryNoteCodec
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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

enum class EntryFilter {
    ALL,
    INCOME,
    EXPENSE,
}

data class EntryListItem(
    val id: Long,
    val amount: Long,
    val type: String,
    val categoryId: Long?,
    val categoryName: String,
    val memo: String,
    val paymentMethod: String,
    val occurredDate: String,
)

data class EntriesUiState(
    val entries: List<EntryListItem> = emptyList(),
    val filter: EntryFilter = EntryFilter.ALL,
    val isLoading: Boolean = true,
    val error: String? = null,
)

@HiltViewModel
class EntriesViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val businessRepository: BusinessRepository,
    private val entryRepository: EntryRepository,
    private val categoryRepository: CategoryRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(EntriesUiState())
    val uiState: StateFlow<EntriesUiState> = _uiState.asStateFlow()

    private var observeJob: Job? = null
    private var businessId: Long? = null
    private var allItems: List<EntryListItem> = emptyList()
    private var entriesById: Map<Long, Entry> = emptyMap()

    init {
        refresh()
    }

    fun setFilter(filter: EntryFilter) {
        _uiState.update { current ->
            current.copy(
                filter = filter,
                entries = filterItems(allItems, filter),
            )
        }
    }

    fun deleteEntry(id: Long) {
        viewModelScope.launch {
            val target = entriesById[id] ?: return@launch
            try {
                entryRepository.deleteEntry(target)
            } catch (throwable: Throwable) {
                _uiState.update { current ->
                    current.copy(error = throwable.message ?: appContext.getString(R.string.entries_error_delete_failed))
                }
            }
        }
    }

    fun refresh() {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            _uiState.update { current ->
                current.copy(isLoading = true, error = null)
            }

            try {
                val resolvedBusinessId = businessId ?: withContext(Dispatchers.IO) {
                    businessRepository
                        .getOrCreateDefaultBusiness()
                        .id
                        .also { id -> businessId = id }
                }

                combine(
                    entryRepository.entries(resolvedBusinessId),
                    categoryRepository.categories(resolvedBusinessId, EntryType.INCOME),
                    categoryRepository.categories(resolvedBusinessId, EntryType.EXPENSE),
                ) { entries, incomeCategories, expenseCategories ->
                    val categoriesById = (incomeCategories + expenseCategories)
                        .associateBy { category -> category.id }

                    entriesById = entries.associateBy { entry -> entry.id }
                    allItems = entries.map { entry ->
                        val categoryName = entry.categoryId
                            ?.let { categoryId -> categoriesById[categoryId]?.name }
                            .orEmpty()
                        val (paymentMethod, memo) = EntryNoteCodec.split(
                            note = entry.note,
                            defaultPaymentMethod = Constants.PAYMENT_CASH,
                        )
                        EntryListItem(
                            id = entry.id,
                            amount = entry.amount,
                            type = entry.type,
                            categoryId = entry.categoryId,
                            categoryName = categoryName,
                            memo = memo,
                            paymentMethod = paymentMethod,
                            occurredDate = entry.occurredDate,
                        )
                    }
                    filterItems(allItems, _uiState.value.filter)
                }.catch { throwable ->
                    _uiState.update { current ->
                        current.copy(
                            isLoading = false,
                            error = throwable.message ?: appContext.getString(R.string.entries_error_load_failed),
                        )
                    }
                }.collect { filteredItems ->
                    _uiState.update { current ->
                        current.copy(
                            entries = filteredItems,
                            isLoading = false,
                            error = null,
                        )
                    }
                }
            } catch (throwable: Throwable) {
                _uiState.update { current ->
                    current.copy(
                        isLoading = false,
                        error = throwable.message ?: appContext.getString(R.string.entries_error_load_failed),
                    )
                }
            }
        }
    }

    private fun filterItems(
        source: List<EntryListItem>,
        filter: EntryFilter,
    ): List<EntryListItem> {
        return when (filter) {
            EntryFilter.ALL -> source
            EntryFilter.INCOME -> source.filter { item -> item.type == Constants.TYPE_INCOME }
            EntryFilter.EXPENSE -> source.filter { item -> item.type == Constants.TYPE_EXPENSE }
        }
    }
}
