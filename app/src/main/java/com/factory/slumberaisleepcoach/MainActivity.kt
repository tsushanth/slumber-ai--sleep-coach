package com.factory.slumberaisleepcoach

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import com.factory.slumberaisleepcoach.billing.BillingManager
import com.factory.slumberaisleepcoach.billing.PremiumManager
import com.factory.slumberaisleepcoach.data.database.AppDatabase
import com.factory.slumberaisleepcoach.data.repository.SettingsRepository
import com.factory.slumberaisleepcoach.data.repository.SleepRepository
import com.factory.slumberaisleepcoach.ui.SlumberApp
import com.factory.slumberaisleepcoach.ui.theme.SlumberAITheme
import com.factory.slumberaisleepcoach.viewmodel.SleepViewModel
import com.factory.slumberaisleepcoach.viewmodel.SleepViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val database = remember { AppDatabase.getDatabase(applicationContext) }
            val repository = remember { SleepRepository(database.sleepSessionDao(), database.snoringRecordDao()) }
            val settingsRepository = remember { SettingsRepository(applicationContext) }
            val premiumManager = remember { PremiumManager(applicationContext) }
            val billingManager = remember { BillingManager(applicationContext, premiumManager) }
            val factory = remember {
                SleepViewModelFactory(application, repository, settingsRepository, premiumManager, billingManager)
            }
            val viewModel: SleepViewModel = viewModel(factory = factory)

            SlumberAITheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    SlumberApp(viewModel = viewModel)
                }
            }
        }
    }
}
