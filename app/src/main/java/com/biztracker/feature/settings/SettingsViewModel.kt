package com.biztracker.feature.settings

import android.app.Activity
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.biztracker.R
import com.biztracker.billing.ProStatusRepository
import com.biztracker.data.local.Category
import com.biztracker.data.local.EntryType
import com.biztracker.data.repository.BusinessRepository
import com.biztracker.data.repository.CategoryRepository
import com.biztracker.data.repository.EntryRepository
import com.biztracker.di.IoDispatcher
import com.biztracker.domain.DateUtils
import com.biztracker.domain.EntryNoteCodec
import com.biztracker.domain.Prefs
import com.biztracker.export.CsvEntryRow
import com.biztracker.export.CsvExporter
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.lang.ref.WeakReference
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val EXPORT_FORMAT_STANDARD = "standard"
private const val EXPORT_FORMAT_SETTLEMENT = "settlement"

sealed interface ExportState {
    data object Idle : ExportState

    data object Exporting : ExportState

    data class Success(
        val fileName: String,
        val csvContent: String,
    ) : ExportState

    data class Error(
        val message: String,
    ) : ExportState
}

data class SettingsUiState(
    val isPro: Boolean = false,
    val languageTag: String = "ko",
    val categories: List<Category> = emptyList(),
    val profitDropThresholdPercent: Int = 15,
    val expenseSpikeThresholdPercent: Int = 30,
    val exportFormat: String = EXPORT_FORMAT_STANDARD,
    val exportState: ExportState = ExportState.Idle,
    val error: String? = null,
    val isLoading: Boolean = true,
    val selectedType: String = EntryType.EXPENSE,
    val yearMonth: String = DateUtils.yearMonth(DateUtils.today()),
    val includeUtf8Bom: Boolean = true,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val businessRepository: BusinessRepository,
    private val categoryRepository: CategoryRepository,
    private val entryRepository: EntryRepository,
    private val csvExporter: CsvExporter,
    private val proStatusRepository: ProStatusRepository,
    private val prefs: Prefs,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private var businessId: Long? = null
    private var purchaseActivityRef: WeakReference<Activity>? = null

    init {
        viewModelScope.launch {
            proStatusRepository.isPro.collect { isPro ->
                _uiState.update { current ->
                    current.copy(
                        isPro = isPro,
                        exportFormat = if (!isPro && current.exportFormat == EXPORT_FORMAT_SETTLEMENT) {
                            EXPORT_FORMAT_STANDARD
                        } else {
                            current.exportFormat
                        },
                    )
                }
            }
        }

        viewModelScope.launch {
            prefs.languageTag.collect { languageTag ->
                _uiState.update { current ->
                    current.copy(languageTag = languageTag)
                }
            }
        }

        viewModelScope.launch {
            prefs.profitDropThresholdPercent.collect { value ->
                _uiState.update { current ->
                    current.copy(profitDropThresholdPercent = value.coerceIn(5, 90))
                }
            }
        }

        viewModelScope.launch {
            prefs.expenseSpikeThresholdPercent.collect { value ->
                _uiState.update { current ->
                    current.copy(expenseSpikeThresholdPercent = value.coerceIn(10, 200))
                }
            }
        }

        refresh()
    }

    fun setLanguage(languageTag: String) {
        if (languageTag !in SUPPORTED_LANGUAGE_TAGS) {
            return
        }

        viewModelScope.launch {
            try {
                withContext(ioDispatcher) {
                    prefs.setLanguageTag(languageTag)
                }
                AppCompatDelegate.setApplicationLocales(
                    LocaleListCompat.forLanguageTags(languageTag)
                )
            } catch (throwable: Throwable) {
                _uiState.update { current ->
                    current.copy(
                        error = throwable.message
                            ?: appContext.getString(R.string.settings_error_change_language_failed)
                    )
                }
            }
        }
    }

    fun refresh() {
        viewModelScope.launch(ioDispatcher) {
            _uiState.update { current ->
                current.copy(isLoading = true, error = null)
            }

            try {
                val resolvedBusinessId = ensureBusinessId()
                val income = categoryRepository.categories(
                    businessId = resolvedBusinessId,
                    type = EntryType.INCOME,
                ).first()
                val expense = categoryRepository.categories(
                    businessId = resolvedBusinessId,
                    type = EntryType.EXPENSE,
                ).first()

                _uiState.update { current ->
                    current.copy(
                        categories = (income + expense).sortedWith(
                            compareBy<Category> { category -> category.type }
                                .thenBy { category -> category.name.lowercase() }
                        ),
                        isLoading = false,
                        error = null,
                    )
                }
            } catch (throwable: Throwable) {
                _uiState.update { current ->
                    current.copy(
                        isLoading = false,
                        error = throwable.message ?: appContext.getString(R.string.settings_error_load_failed),
                    )
                }
            }
        }
    }

    fun attachPurchaseActivity(activity: Activity?) {
        purchaseActivityRef = if (activity == null) {
            null
        } else {
            WeakReference(activity)
        }
    }

    fun setCategoryType(type: String) {
        if (type != EntryType.INCOME && type != EntryType.EXPENSE) {
            return
        }
        _uiState.update { current ->
            current.copy(selectedType = type)
        }
    }

    fun previousMonth() {
        val previous = java.time.YearMonth.parse(_uiState.value.yearMonth).minusMonths(1).toString()
        _uiState.update { current ->
            current.copy(yearMonth = previous)
        }
    }

    fun nextMonth() {
        val next = java.time.YearMonth.parse(_uiState.value.yearMonth).plusMonths(1).toString()
        _uiState.update { current ->
            current.copy(yearMonth = next)
        }
    }

    fun setIncludeUtf8Bom(enabled: Boolean) {
        _uiState.update { current ->
            current.copy(includeUtf8Bom = enabled)
        }
    }

    fun setProfitDropThresholdPercent(value: Int) {
        viewModelScope.launch(ioDispatcher) {
            prefs.setProfitDropThresholdPercent(value.coerceIn(5, 90))
        }
    }

    fun setExpenseSpikeThresholdPercent(value: Int) {
        viewModelScope.launch(ioDispatcher) {
            prefs.setExpenseSpikeThresholdPercent(value.coerceIn(10, 200))
        }
    }

    fun setExportFormat(format: String) {
        if (format != EXPORT_FORMAT_STANDARD && format != EXPORT_FORMAT_SETTLEMENT) {
            return
        }

        if (format == EXPORT_FORMAT_SETTLEMENT && !_uiState.value.isPro) {
            _uiState.update { current ->
                current.copy(
                    error = appContext.getString(R.string.settings_error_settlement_export_pro_required)
                )
            }
            return
        }

        _uiState.update { current ->
            current.copy(exportFormat = format)
        }
    }

    fun addCategory(name: String) {
        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) {
            _uiState.update { current ->
                current.copy(
                    error = appContext.getString(R.string.settings_error_category_name_required)
                )
            }
            return
        }

        viewModelScope.launch(ioDispatcher) {
            try {
                val state = _uiState.value
                val resolvedBusinessId = ensureBusinessId()
                val canAdd = categoryRepository.canAddCustomCategory(
                    businessId = resolvedBusinessId,
                    type = state.selectedType,
                    isPro = state.isPro,
                )

                if (!canAdd) {
                    _uiState.update { current ->
                        current.copy(
                            error = appContext.getString(R.string.settings_error_category_limit_free)
                        )
                    }
                    return@launch
                }

                categoryRepository.addCategory(
                    businessId = resolvedBusinessId,
                    name = trimmedName,
                    type = state.selectedType,
                    isCustom = true,
                )
                refresh()
            } catch (throwable: Throwable) {
                _uiState.update { current ->
                    current.copy(
                        error = throwable.message
                            ?: appContext.getString(R.string.settings_error_category_add_failed)
                    )
                }
            }
        }
    }

    fun updateCategory(
        category: Category,
        name: String,
    ) {
        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) {
            _uiState.update { current ->
                current.copy(
                    error = appContext.getString(R.string.settings_error_category_name_required)
                )
            }
            return
        }

        viewModelScope.launch(ioDispatcher) {
            try {
                categoryRepository.updateCategory(category.copy(name = trimmedName))
                refresh()
            } catch (throwable: Throwable) {
                _uiState.update { current ->
                    current.copy(
                        error = throwable.message
                            ?: appContext.getString(R.string.settings_error_category_update_failed)
                    )
                }
            }
        }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch(ioDispatcher) {
            try {
                categoryRepository.deleteCategory(category)
                refresh()
            } catch (throwable: Throwable) {
                _uiState.update { current ->
                    current.copy(
                        error = throwable.message
                            ?: appContext.getString(R.string.settings_error_category_delete_failed)
                    )
                }
            }
        }
    }

    fun exportCsv() {
        viewModelScope.launch(ioDispatcher) {
            _uiState.update { current ->
                current.copy(exportState = ExportState.Exporting, error = null)
            }

            try {
                val state = _uiState.value
                val resolvedBusinessId = ensureBusinessId()
                val categoriesById = state.categories.associateBy { category -> category.id }
                if (state.exportFormat == EXPORT_FORMAT_SETTLEMENT && !state.isPro) {
                    val message = appContext.getString(
                        R.string.settings_error_settlement_export_pro_required
                    )
                    _uiState.update { current ->
                        current.copy(
                            exportState = ExportState.Error(message),
                            error = message,
                        )
                    }
                    return@launch
                }

                val rows = entryRepository.entries(resolvedBusinessId)
                    .first()
                    .asSequence()
                    .filter { entry -> entry.occurredDate.startsWith(state.yearMonth) }
                    .sortedWith(compareBy({ entry -> entry.occurredDate }, { entry -> entry.id }))
                    .map { entry ->
                        val (paymentMethod, memo) = EntryNoteCodec.split(
                            note = entry.note,
                            defaultPaymentMethod = "",
                        )
                        CsvEntryRow(
                            date = entry.occurredDate,
                            type = entry.type,
                            amount = entry.amount,
                            categoryName = entry.categoryId
                                ?.let { categoryId -> categoriesById[categoryId]?.name }
                                .orEmpty(),
                            paymentMethod = paymentMethod,
                            memo = memo,
                        )
                    }
                    .toList()

                val isSettlement = state.exportFormat == EXPORT_FORMAT_SETTLEMENT
                val csv = if (isSettlement) {
                    csvExporter.exportSettlementEntries(
                        rows = rows,
                        yearMonth = state.yearMonth,
                        includeUtf8Bom = state.includeUtf8Bom,
                    )
                } else {
                    csvExporter.exportEntries(
                        rows = rows,
                        includeUtf8Bom = state.includeUtf8Bom,
                    )
                }
                val fileName = if (isSettlement) {
                    "settlement_${state.yearMonth}.csv"
                } else {
                    "entries_${state.yearMonth}.csv"
                }

                _uiState.update { current ->
                    current.copy(
                        exportState = ExportState.Success(
                            fileName = fileName,
                            csvContent = csv,
                        )
                    )
                }
            } catch (throwable: Throwable) {
                val message = throwable.message
                    ?: appContext.getString(R.string.settings_error_csv_export_failed)
                _uiState.update { current ->
                    current.copy(
                        exportState = ExportState.Error(
                            message = message,
                        ),
                        error = message,
                    )
                }
            }
        }
    }

    fun clearExportState() {
        _uiState.update { current ->
            current.copy(exportState = ExportState.Idle)
        }
    }

    fun purchasePro() {
        val activity = purchaseActivityRef?.get()
        if (activity == null) {
            _uiState.update { current ->
                current.copy(
                    error = appContext.getString(R.string.settings_error_purchase_unavailable)
                )
            }
            return
        }
        proStatusRepository.launchPurchase(activity)
    }

    fun restorePurchase() {
        proStatusRepository.refreshPurchases()
    }

    fun clearError() {
        _uiState.update { current ->
            current.copy(error = null)
        }
    }

    private suspend fun ensureBusinessId(): Long {
        val existing = businessId
        if (existing != null) {
            return existing
        }
        val resolved = businessRepository.getOrCreateDefaultBusiness().id
        businessId = resolved
        return resolved
    }

    private companion object {
        val SUPPORTED_LANGUAGE_TAGS = setOf("ko", "en")
    }
}
