package com.biztracker.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.biztracker.BizTrackerNavHost
import com.biztracker.ads.AdsConfig
import com.biztracker.navigateToTopLevel
import com.biztracker.topLevelDestinations
import com.biztracker.ui.components.BottomNavBar
import com.biztracker.ui.theme.Dimens
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import kotlinx.coroutines.delay

@Composable
fun AppScaffold(
    modifier: Modifier = Modifier,
    viewModel: AppScaffoldViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = topLevelDestinations.any { destination -> destination.route == currentRoute }
    val shouldShowAds = showBottomBar && !uiState.isPro
    val context = LocalContext.current

    LaunchedEffect(shouldShowAds, context) {
        if (!shouldShowAds) {
            return@LaunchedEffect
        }

        while (true) {
            delay(AdsConfig.INTERSTITIAL_INTERVAL_MS)
            val activity = context.findActivity() ?: continue
            viewModel.showInterstitialIfEligible(activity)
        }
    }

    Scaffold(
        modifier = modifier,
        containerColor = Color.Transparent,
        bottomBar = {
            if (showBottomBar) {
                Column {
                    if (!uiState.isPro) {
                        FreePlanBannerAd()
                    }
                    BottomNavBar(
                        currentRoute = currentRoute,
                        onNavigate = { destination ->
                            navController.navigateToTopLevel(destination)
                        },
                    )
                }
            }
        },
    ) { innerPadding ->
        val colorScheme = MaterialTheme.colorScheme
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            colorScheme.primaryContainer.copy(alpha = 0.28f),
                            colorScheme.background,
                            colorScheme.background,
                        )
                    )
                )
        ) {
            BizTrackerNavHost(
                navController = navController,
                modifier = Modifier.padding(innerPadding),
            )
        }
    }
}

@Composable
private fun FreePlanBannerAd() {
    val context = LocalContext.current
    val adView = remember {
        AdView(context).apply {
            adUnitId = AdsConfig.BANNER_AD_UNIT_ID
            setAdSize(AdSize.BANNER)
            loadAd(AdRequest.Builder().build())
        }
    }

    DisposableEffect(adView) {
        onDispose {
            adView.destroy()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface),
    ) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = Dimens.Space1),
            contentAlignment = Alignment.Center,
        ) {
            AndroidView(factory = { adView })
        }
    }
}

private fun Context.findActivity(): Activity? {
    var current = this
    while (current is ContextWrapper) {
        if (current is Activity) {
            return current
        }
        current = current.baseContext
    }
    return null
}
