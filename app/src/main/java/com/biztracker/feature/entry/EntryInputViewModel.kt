package com.biztracker.feature.entry

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.biztracker.R
import com.biztracker.billing.ProStatusRepository
import com.biztracker.data.local.Category
import com.biztracker.data.local.EntryType
import com.biztracker.data.repository.BusinessRepository
import com.biztracker.data.repository.CategoryRepository
import com.biztracker.data.repository.EntryRepository
import com.biztracker.domain.Constants
import com.biztracker.domain.DateUtils
import com.biztracker.domain.EntryNoteCodec
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EntryInputUiState(
    val amount: String = "",
    val type: String = EntryType.EXPENSE,
    val categoryId: Long? = null,
    val memo: String = "",
    val paymentMethod: String = Constants.PAYMENT_CASH,
    val occurredDate: String = DateUtils.today(),
    val quickPresets: List<EntryQuickPreset> = emptyList(),
    val isPro: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val categories: List<Category> = emptyList(),
)

data class EntryQuickPreset(
    val id: Long,
    val type: String,
    val amount: Long,
    val categoryId: Long?,
    val paymentMethod: String,
    val memo: String,
)

sealed interface EntryInputEffect {
    data object Saved : EntryInputEffect
}

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class EntryInputViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val proStatusRepository: ProStatusRepository,
    private val businessRepository: BusinessRepository,
    private val categoryRepository: CategoryRepository,
    private val entryRepository: EntryRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(EntryInputUiState())
    val uiState: StateFlow<EntryInputUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<EntryInputEffect>()
    val effects: SharedFlow<EntryInputEffect> = _effects.asSharedFlow()

    private val businessId = MutableStateFlow<Long?>(null)

    init {
        viewModelScope.launch {
            val defaultBusinessId = withContext(Dispatchers.IO) {
                businessRepository.getOrCreateDefaultBusiness().id
            }
            businessId.value = defaultBusinessId
        }

        viewModelScope.launch {
            combine(
                businessId.filterNotNull(),
                _uiState.map { state -> state.type }.distinctUntilChanged(),
            ) { id, type ->
                id to type
            }.flatMapLatest { (id, type) ->
                categoryRepository.categories(id, type)
            }.collect { categories ->
                _uiState.update { current ->
                    val selectedCategoryId = current.categoryId
                    val isValidSelection = selectedCategoryId != null && categories.any { category ->
                        category.id == selectedCategoryId
                    }
                    current.copy(
                        categories = categories,
                        categoryId = if (isValidSelection) selectedCategoryId else null,
                    )
                }
            }
        }

        viewModelScope.launch {
            combine(
                proStatusRepository.isPro,
                businessId
                    .filterNotNull()
                    .flatMapLatest { id ->
                        entryRepository.entries(id)
                    },
            ) { isProEnabled, entries ->
                isProEnabled to entries
            }.collect { (isProEnabled, entries) ->
                val presets = entries
                    .asSequence()
                    .take(if (isProEnabled) PRESET_SOURCE_LIMIT_PRO else PRESET_SOURCE_LIMIT_FREE)
                    .map { entry ->
                        val (paymentMethod, memo) = EntryNoteCodec.split(
                            note = entry.note,
                            defaultPaymentMethod = Constants.PAYMENT_CASH,
                        )
                        EntryQuickPreset(
                            id = entry.id,
                            type = entry.type,
                            amount = entry.amount,
                            categoryId = entry.categoryId,
                            paymentMethod = paymentMethod,
                            memo = memo,
                        )
                    }
                    .toList()
                _uiState.update { current ->
                    current.copy(
                        isPro = isProEnabled,
                        quickPresets = presets,
                    )
                }
            }
        }
    }

    fun setInitialType(type: String) {
        setType(type)
    }

    fun setAmount(raw: String) {
        val digitsOnly = raw.filter { char -> char.isDigit() }
        _uiState.update { current ->
            current.copy(
                amount = digitsOnly,
                error = null,
            )
        }
    }

    fun setType(type: String) {
        val mappedType = when (type.lowercase()) {
            "income", EntryType.INCOME.lowercase() -> EntryType.INCOME
            "expense", EntryType.EXPENSE.lowercase() -> EntryType.EXPENSE
            else -> return
        }

        _uiState.update { current ->
            current.copy(
                type = mappedType,
                categoryId = null,
                error = null,
            )
        }
    }

    fun setCategory(categoryId: Long?) {
        _uiState.update { current ->
            current.copy(categoryId = categoryId)
        }
    }

    fun setMemo(memo: String) {
        _uiState.update { current ->
            current.copy(memo = memo)
        }
    }

    fun setPaymentMethod(paymentMethod: String) {
        _uiState.update { current ->
            current.copy(paymentMethod = paymentMethod)
        }
    }

    fun setDate(date: String) {
        _uiState.update { current ->
            current.copy(occurredDate = date)
        }
    }

    fun applyQuickPreset(preset: EntryQuickPreset) {
        _uiState.update { current ->
            current.copy(
                amount = preset.amount.toString(),
                type = preset.type,
                categoryId = preset.categoryId,
                paymentMethod = preset.paymentMethod,
                memo = preset.memo,
                error = null,
            )
        }
    }

    fun save() {
        val current = _uiState.value
        val parsedAmount = current.amount.toLongOrNull()
        if (parsedAmount == null || parsedAmount <= 0L) {
            _uiState.update { state ->
                state.copy(error = appContext.getString(R.string.entry_input_error_amount_minimum))
            }
            return
        }

        if (current.occurredDate.isBlank()) {
            _uiState.update { state ->
                state.copy(error = appContext.getString(R.string.entry_input_error_date_required))
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(isSaving = true, error = null)
            }

            try {
                val resolvedBusinessId = businessId.value
                    ?: withContext(Dispatchers.IO) {
                        businessRepository.getOrCreateDefaultBusiness().id.also { id ->
                            businessId.value = id
                        }
                    }

                entryRepository.addEntry(
                    businessId = resolvedBusinessId,
                    categoryId = current.categoryId,
                    type = current.type,
                    amount = parsedAmount,
                    occurredDate = current.occurredDate,
                    note = EntryNoteCodec.merge(current.paymentMethod, current.memo),
                )

                _uiState.update { state ->
                    state.copy(isSaving = false)
                }
                _effects.emit(EntryInputEffect.Saved)
            } catch (throwable: Throwable) {
                Log.w(TAG, "Failed to save entry", throwable)
                _uiState.update { state ->
                    state.copy(
                        isSaving = false,
                        error = throwable.message ?: appContext.getString(R.string.entry_input_error_save_failed),
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { current ->
            current.copy(error = null)
        }
    }

    private companion object {
        const val TAG = "EntryInputViewModel"
        const val PRESET_SOURCE_LIMIT_FREE = 2
        const val PRESET_SOURCE_LIMIT_PRO = 6
    }
}
