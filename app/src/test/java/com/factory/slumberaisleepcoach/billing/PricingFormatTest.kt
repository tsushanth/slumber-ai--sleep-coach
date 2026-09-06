package com.factory.slumberaisleepcoach.billing

import com.android.billingclient.api.ProductDetails
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Test

class PricingFormatTest {

    @Test
    fun `formattedPrice falls back to the product's fallback price when details are null`() {
        assertEquals(PremiumProduct.WEEKLY.fallbackPrice, formattedPrice(PremiumProduct.WEEKLY, null))
    }

    @Test
    fun `formattedPrice returns the store price for a subscription with an offer`() {
        val pricingPhase = mockk<ProductDetails.PricingPhase>(relaxed = true)
        every { pricingPhase.formattedPrice } returns "$4.99"
        val pricingPhases = mockk<ProductDetails.PricingPhases>(relaxed = true)
        every { pricingPhases.pricingPhaseList } returns listOf(pricingPhase)
        val offer = mockk<ProductDetails.SubscriptionOfferDetails>(relaxed = true)
        every { offer.pricingPhases } returns pricingPhases
        val details = mockk<ProductDetails>(relaxed = true)
        every { details.subscriptionOfferDetails } returns listOf(offer)

        assertEquals("$4.99", formattedPrice(PremiumProduct.MONTHLY, details))
    }

    @Test
    fun `formattedPrice falls back for a subscription with no offer details`() {
        val details = mockk<ProductDetails>(relaxed = true)
        every { details.subscriptionOfferDetails } returns null

        assertEquals(PremiumProduct.MONTHLY.fallbackPrice, formattedPrice(PremiumProduct.MONTHLY, details))
    }

    @Test
    fun `formattedPrice falls back for a subscription with an empty pricing phase list`() {
        val pricingPhases = mockk<ProductDetails.PricingPhases>(relaxed = true)
        every { pricingPhases.pricingPhaseList } returns emptyList()
        val offer = mockk<ProductDetails.SubscriptionOfferDetails>(relaxed = true)
        every { offer.pricingPhases } returns pricingPhases
        val details = mockk<ProductDetails>(relaxed = true)
        every { details.subscriptionOfferDetails } returns listOf(offer)

        assertEquals(PremiumProduct.MONTHLY.fallbackPrice, formattedPrice(PremiumProduct.MONTHLY, details))
    }

    @Test
    fun `formattedPrice returns the store price for a one-time purchase`() {
        val oneTimeDetails = mockk<ProductDetails.OneTimePurchaseOfferDetails>(relaxed = true)
        every { oneTimeDetails.formattedPrice } returns "$67.18"
        val details = mockk<ProductDetails>(relaxed = true)
        every { details.oneTimePurchaseOfferDetails } returns oneTimeDetails

        assertEquals("$67.18", formattedPrice(PremiumProduct.LIFETIME, details))
    }

    @Test
    fun `formattedPrice falls back for a one-time purchase with no offer details`() {
        val details = mockk<ProductDetails>(relaxed = true)
        every { details.oneTimePurchaseOfferDetails } returns null

        assertEquals(PremiumProduct.LIFETIME.fallbackPrice, formattedPrice(PremiumProduct.LIFETIME, details))
    }
}
