package com.factory.slumberaisleepcoach.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.factory.slumberaisleepcoach.billing.BillingManager
import com.factory.slumberaisleepcoach.billing.PremiumManager
import com.factory.slumberaisleepcoach.data.repository.SettingsRepository
import com.factory.slumberaisleepcoach.data.repository.SleepRepository

class SleepViewModelFactory(
    private val application: Application,
    private val repository: SleepRepository,
    private val settingsRepository: SettingsRepository,
    private val premiumManager: PremiumManager,
    private val billingManager: BillingManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SleepViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SleepViewModel(application, repository, settingsRepository, premiumManager, billingManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
