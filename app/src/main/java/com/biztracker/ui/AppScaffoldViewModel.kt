package com.biztracker.ui

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.biztracker.ads.AdsManager
import com.biztracker.billing.ProStatusRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AppScaffoldUiState(
    val isPro: Boolean = false,
)

@HiltViewModel
class AppScaffoldViewModel @Inject constructor(
    private val proStatusRepository: ProStatusRepository,
    private val adsManager: AdsManager,
) : ViewModel() {

    val uiState: StateFlow<AppScaffoldUiState> = proStatusRepository.isPro
        .map { isPro -> AppScaffoldUiState(isPro = isPro) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
            initialValue = AppScaffoldUiState(),
        )

    init {
        adsManager.initialize()

        viewModelScope.launch {
            proStatusRepository.isPro.collect { isPro ->
                if (!isPro) {
                    adsManager.loadInterstitial()
                }
            }
        }
    }

    fun showInterstitialIfEligible(activity: Activity) {
        if (uiState.value.isPro) {
            return
        }
        adsManager.tryShowInterstitial(activity)
    }
}
