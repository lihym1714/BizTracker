package com.biztracker

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.biztracker.domain.Prefs
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

@HiltAndroidApp
class BizTrackerApp : Application() {

    override fun onCreate() {
        super.onCreate()

        val prefs = EntryPointAccessors.fromApplication(
            this,
            LocalePrefsEntryPoint::class.java,
        ).prefs()

        val languageTag = runBlocking(Dispatchers.IO) {
            prefs.languageTag.first()
        }.ifBlank { DEFAULT_LANGUAGE_TAG }

        AppCompatDelegate.setApplicationLocales(
            LocaleListCompat.forLanguageTags(languageTag)
        )
    }

    private companion object {
        const val DEFAULT_LANGUAGE_TAG = "ko"
    }
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface LocalePrefsEntryPoint {
    fun prefs(): Prefs
}
