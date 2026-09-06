package com.factory.slumberaisleepcoach.billing

import com.android.billingclient.api.ProductDetails

/** Localized store price for [product], falling back to the local default if not loaded yet. */
fun formattedPrice(product: PremiumProduct, details: ProductDetails?): String {
    if (details == null) return product.fallbackPrice
    return if (product.isSubscription) {
        details.subscriptionOfferDetails
            ?.firstOrNull()
            ?.pricingPhases
            ?.pricingPhaseList
            ?.firstOrNull()
            ?.formattedPrice
            ?: product.fallbackPrice
    } else {
        details.oneTimePurchaseOfferDetails?.formattedPrice ?: product.fallbackPrice
    }
}
