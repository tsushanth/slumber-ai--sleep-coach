package com.factory.slumberaisleepcoach.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore(name = "slumber_settings")

class SettingsRepository(private val context: Context) {

    private val sensitivityKey = floatPreferencesKey("snoring_sensitivity")
    private val onboardingCompleteKey = booleanPreferencesKey("onboarding_complete")

    val sensitivityFlow: Flow<Float> = context.settingsDataStore.data.map { prefs ->
        prefs[sensitivityKey] ?: 3500f
    }

    val hasCompletedOnboardingFlow: Flow<Boolean> = context.settingsDataStore.data.map { prefs ->
        prefs[onboardingCompleteKey] ?: false
    }

    suspend fun setSensitivity(value: Float) {
        context.settingsDataStore.edit { prefs ->
            prefs[sensitivityKey] = value
        }
    }

    suspend fun setOnboardingComplete() {
        context.settingsDataStore.edit { prefs ->
            prefs[onboardingCompleteKey] = true
        }
    }
}
