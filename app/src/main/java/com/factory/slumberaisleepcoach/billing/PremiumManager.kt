package com.factory.slumberaisleepcoach.billing

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.premiumDataStore by preferencesDataStore(name = "slumber_premium")

/**
 * Tracks and persists the user's premium entitlement. This is a client-side cache of
 * entitlement derived from Play Billing purchase state; [BillingManager] is the source of
 * truth on each app start / resume and reconciles this store (e.g. clearing it when a
 * subscription is no longer found among the user's active purchases).
 */
class PremiumManager(private val context: Context) {

    private object Keys {
        val IS_PREMIUM = booleanPreferencesKey("is_premium")
        val PRODUCT_ID = stringPreferencesKey("premium_product_id")
    }

    val isPremiumFlow: Flow<Boolean> = context.premiumDataStore.data
        .map { prefs -> prefs[Keys.IS_PREMIUM] ?: false }
        .distinctUntilChanged()

    suspend fun isCurrentlyPremium(): Boolean = isPremiumFlow.first()

    suspend fun grantPremium(productId: String) {
        context.premiumDataStore.edit { prefs ->
            prefs[Keys.IS_PREMIUM] = true
            prefs[Keys.PRODUCT_ID] = productId
        }
    }

    suspend fun revokePremium() {
        context.premiumDataStore.edit { prefs ->
            prefs[Keys.IS_PREMIUM] = false
            prefs.remove(Keys.PRODUCT_ID)
        }
    }
}
