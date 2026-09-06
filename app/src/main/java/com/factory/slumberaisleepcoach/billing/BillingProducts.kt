package com.factory.slumberaisleepcoach.billing

import com.android.billingclient.api.BillingClient.ProductType

/**
 * Fallback prices shown before the Play Store's [com.android.billingclient.api.ProductDetails]
 * have loaded. Once loaded, the real, localized store price always takes precedence.
 */
enum class PremiumProduct(
    val productId: String,
    val type: String,
    val displayName: String,
    val fallbackPrice: String
) {
    WEEKLY(
        productId = "com.factory.slumberaisleepcoach.subscription.weekly",
        type = ProductType.SUBS,
        displayName = "Weekly",
        fallbackPrice = "$3.99"
    ),
    MONTHLY(
        productId = "com.factory.slumberaisleepcoach.subscription.monthly",
        type = ProductType.SUBS,
        displayName = "Monthly",
        fallbackPrice = ""
    ),
    YEARLY(
        productId = "com.factory.slumberaisleepcoach.subscription.yearly",
        type = ProductType.SUBS,
        displayName = "Yearly",
        fallbackPrice = "$33.59"
    ),
    LIFETIME(
        productId = "com.factory.slumberaisleepcoach.subscription.lifetime",
        type = ProductType.INAPP,
        displayName = "Lifetime",
        fallbackPrice = "$67.18"
    ),
    SMALL_IAP(
        productId = "com.factory.slumberaisleepcoach.small_iap",
        type = ProductType.INAPP,
        displayName = "Support the Developer",
        fallbackPrice = "$1.11"
    );

    val isSubscription: Boolean get() = type == ProductType.SUBS

    companion object {
        /** Tiers surfaced as selectable options on the paywall. */
        val PAYWALL_TIERS = listOf(WEEKLY, YEARLY, LIFETIME)

        val ALL_SUBSCRIPTION_IDS: List<String> = entries.filter { it.isSubscription }.map { it.productId }
        val ALL_INAPP_IDS: List<String> = entries.filter { !it.isSubscription }.map { it.productId }

        fun fromProductId(productId: String): PremiumProduct? = entries.find { it.productId == productId }
    }
}
