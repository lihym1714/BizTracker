package com.biztracker.billing

import android.app.Activity
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.StateFlow

@Singleton
class ProStatusRepository @Inject constructor(
    private val billingManager: BillingManager,
) {
    val isPro: StateFlow<Boolean> = billingManager.isPro

    fun launchPurchase(activity: Activity) {
        billingManager.launchPurchase(activity)
    }

    fun refreshPurchases() {
        billingManager.refreshPurchases()
    }
}
