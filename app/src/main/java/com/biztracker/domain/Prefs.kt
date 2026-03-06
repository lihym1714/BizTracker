package com.biztracker.domain

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Prefs @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    val isProOverride: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[IS_PRO_OVERRIDE_KEY] ?: false
    }

    val languageTag: Flow<String> = dataStore.data.map { preferences ->
        preferences[LANGUAGE_TAG_KEY] ?: DEFAULT_LANGUAGE_TAG
    }

    val profitDropThresholdPercent: Flow<Int> = dataStore.data.map { preferences ->
        preferences[PROFIT_DROP_THRESHOLD_KEY] ?: DEFAULT_PROFIT_DROP_THRESHOLD
    }

    val expenseSpikeThresholdPercent: Flow<Int> = dataStore.data.map { preferences ->
        preferences[EXPENSE_SPIKE_THRESHOLD_KEY] ?: DEFAULT_EXPENSE_SPIKE_THRESHOLD
    }

    suspend fun setProOverride(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[IS_PRO_OVERRIDE_KEY] = enabled
        }
    }

    suspend fun setLanguageTag(languageTag: String) {
        dataStore.edit { preferences ->
            preferences[LANGUAGE_TAG_KEY] = languageTag
        }
    }

    suspend fun setProfitDropThresholdPercent(value: Int) {
        dataStore.edit { preferences ->
            preferences[PROFIT_DROP_THRESHOLD_KEY] = value
        }
    }

    suspend fun setExpenseSpikeThresholdPercent(value: Int) {
        dataStore.edit { preferences ->
            preferences[EXPENSE_SPIKE_THRESHOLD_KEY] = value
        }
    }

    private companion object {
        val IS_PRO_OVERRIDE_KEY = booleanPreferencesKey("is_pro_override")
        val LANGUAGE_TAG_KEY = stringPreferencesKey("language_tag")
        val PROFIT_DROP_THRESHOLD_KEY = intPreferencesKey("profit_drop_threshold_percent")
        val EXPENSE_SPIKE_THRESHOLD_KEY = intPreferencesKey("expense_spike_threshold_percent")
        const val DEFAULT_LANGUAGE_TAG = "ko"
        const val DEFAULT_PROFIT_DROP_THRESHOLD = 15
        const val DEFAULT_EXPENSE_SPIKE_THRESHOLD = 30
    }
}
