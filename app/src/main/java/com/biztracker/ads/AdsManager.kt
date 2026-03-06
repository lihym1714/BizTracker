package com.biztracker.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdsManager @Inject constructor(
    @ApplicationContext private val appContext: Context,
) {
    private var isInitialized: Boolean = false
    private var isLoadingInterstitial: Boolean = false
    private var interstitialAd: InterstitialAd? = null
    private var lastInterstitialShownAtMillis: Long = 0L

    fun initialize() {
        if (isInitialized) {
            return
        }
        MobileAds.initialize(appContext)
        isInitialized = true
        loadInterstitial()
    }

    fun loadInterstitial() {
        if (!isInitialized || isLoadingInterstitial || interstitialAd != null) {
            return
        }

        isLoadingInterstitial = true
        InterstitialAd.load(
            appContext,
            AdsConfig.INTERSTITIAL_AD_UNIT_ID,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    isLoadingInterstitial = false
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.w(TAG, "Interstitial ad failed to load: ${error.code}")
                    interstitialAd = null
                    isLoadingInterstitial = false
                }
            },
        )
    }

    fun tryShowInterstitial(activity: Activity): Boolean {
        if (!isInitialized) {
            initialize()
        }

        val now = System.currentTimeMillis()
        if (now - lastInterstitialShownAtMillis < AdsConfig.INTERSTITIAL_INTERVAL_MS) {
            return false
        }

        val ad = interstitialAd ?: run {
            loadInterstitial()
            return false
        }

        interstitialAd = null
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                lastInterstitialShownAtMillis = System.currentTimeMillis()
            }

            override fun onAdDismissedFullScreenContent() {
                loadInterstitial()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                Log.w(TAG, "Interstitial ad failed to show: ${error.code}")
                loadInterstitial()
            }
        }
        ad.show(activity)
        return true
    }

    private companion object {
        const val TAG = "AdsManager"
    }
}
