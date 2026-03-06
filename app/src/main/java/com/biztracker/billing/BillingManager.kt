package com.biztracker.billing

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesResponseListener
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@Singleton
class BillingManager @Inject constructor(
    @ApplicationContext context: Context,
) : PurchasesUpdatedListener {

    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val appContext = context.applicationContext

    private val billingClient: BillingClient = BillingClient.newBuilder(appContext)
        .setListener(this)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .build()
        )
        .build()

    private var proProductDetails: ProductDetails? = null
    private var reconnectAttempted = false

    private val _isPro = MutableStateFlow(false)
    val isPro: StateFlow<Boolean> = _isPro.asStateFlow()

    init {
        refreshPurchases()
    }

    fun launchPurchase(activity: Activity) {
        ensureConnected {
            queryProductDetails {
                val product = proProductDetails
                if (product == null) {
                    Log.w(TAG, "Pro product not available")
                    return@queryProductDetails
                }
                launchBillingFlow(activity, product)
            }
        }
    }

    fun refreshPurchases() {
        ensureConnected {
            billingClient.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder()
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build(),
                PurchasesResponseListener { result, purchases ->
                    if (result.responseCode != BillingClient.BillingResponseCode.OK) {
                        Log.w(TAG, "queryPurchasesAsync failed: ${result.responseCode}")
                        return@PurchasesResponseListener
                    }
                    processPurchases(purchases.orEmpty())
                }
            )
        }
    }

    private fun ensureConnected(onReady: () -> Unit) {
        if (billingClient.isReady) {
            onReady()
            return
        }

        billingClient.startConnection(
            object : BillingClientStateListener {
                override fun onBillingSetupFinished(result: BillingResult) {
                    if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                        reconnectAttempted = false
                        onReady()
                    } else {
                        Log.w(TAG, "Billing setup failed: ${result.responseCode}")
                    }
                }

                override fun onBillingServiceDisconnected() {
                    if (reconnectAttempted) {
                        return
                    }
                    reconnectAttempted = true
                    scope.launch {
                        ensureConnected(onReady)
                    }
                }
            }
        )
    }

    private fun queryProductDetails(onLoaded: () -> Unit) {
        val cached = proProductDetails
        if (cached != null) {
            onLoaded()
            return
        }

        val product = QueryProductDetailsParams.Product.newBuilder()
            .setProductId(PRO_UNLOCK_PRODUCT_ID)
            .setProductType(BillingClient.ProductType.INAPP)
            .build()

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(listOf(product))
            .build()

        billingClient.queryProductDetailsAsync(params) { result, detailsList ->
            if (result.responseCode != BillingClient.BillingResponseCode.OK) {
                Log.w(TAG, "queryProductDetailsAsync failed: ${result.responseCode}")
                return@queryProductDetailsAsync
            }

            proProductDetails = detailsList.firstOrNull()
            onLoaded()
        }
    }

    private fun launchBillingFlow(activity: Activity, details: ProductDetails) {
        val productParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(details)
            .build()

        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productParams))
            .build()

        val result = billingClient.launchBillingFlow(activity, flowParams)
        if (result.responseCode != BillingClient.BillingResponseCode.OK) {
            Log.w(TAG, "launchBillingFlow failed: ${result.responseCode}")
        }
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
        if (result.responseCode == BillingClient.BillingResponseCode.OK) {
            processPurchases(purchases.orEmpty())
            return
        }
        if (result.responseCode != BillingClient.BillingResponseCode.USER_CANCELED) {
            Log.w(TAG, "onPurchasesUpdated failed: ${result.responseCode}")
        }
    }

    private fun processPurchases(purchases: List<Purchase>) {
        var hasPro = false

        purchases.forEach { purchase ->
            val isProPurchase = purchase.products.contains(PRO_UNLOCK_PRODUCT_ID)
            if (!isProPurchase) {
                return@forEach
            }

            if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                hasPro = true
                if (!purchase.isAcknowledged) {
                    acknowledgePurchase(purchase)
                }
            }
        }

        _isPro.value = hasPro
    }

    private fun acknowledgePurchase(purchase: Purchase) {
        val purchaseToken = purchase.purchaseToken
        if (purchaseToken.isBlank()) {
            Log.w(TAG, "Empty purchase token")
            return
        }

        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchaseToken)
            .build()

        billingClient.acknowledgePurchase(params) { result ->
            if (result.responseCode != BillingClient.BillingResponseCode.OK) {
                Log.w(TAG, "acknowledgePurchase failed: ${result.responseCode}")
                return@acknowledgePurchase
            }
            _isPro.value = true
        }
    }

    fun disconnect() {
        if (billingClient.isReady) {
            billingClient.endConnection()
        }
    }

    private companion object {
        const val TAG = "BillingManager"
        const val PRO_UNLOCK_PRODUCT_ID = "pro_unlock"
    }
}
