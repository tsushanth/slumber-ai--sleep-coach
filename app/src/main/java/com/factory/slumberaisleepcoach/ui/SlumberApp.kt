package com.factory.slumberaisleepcoach.ui

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalUriHandler
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.factory.slumberaisleepcoach.ui.navigation.NavRoutes
import com.factory.slumberaisleepcoach.ui.screens.HistoryScreen
import com.factory.slumberaisleepcoach.ui.screens.HomeScreen
import com.factory.slumberaisleepcoach.ui.screens.OnboardingScreen
import com.factory.slumberaisleepcoach.ui.screens.PaywallScreen
import com.factory.slumberaisleepcoach.ui.screens.SessionDetailScreen
import com.factory.slumberaisleepcoach.ui.screens.SettingsScreen
import com.factory.slumberaisleepcoach.ui.screens.TrackingScreen
import com.factory.slumberaisleepcoach.viewmodel.SleepViewModel
import com.factory.slumberaisleepcoach.viewmodel.TrackingState

// TODO: replace with the app's real, published Terms of Service / Privacy Policy URLs.
private const val TERMS_URL = "https://www.slumberaisleepcoach.com/terms"
private const val PRIVACY_URL = "https://www.slumberaisleepcoach.com/privacy"

private data class BottomNavItem(val route: String, val label: String, val icon: ImageVector)

private val bottomNavItems = listOf(
    BottomNavItem(NavRoutes.HOME, "Home", Icons.Filled.Bedtime),
    BottomNavItem(NavRoutes.HISTORY, "History", Icons.Filled.History),
    BottomNavItem(NavRoutes.SETTINGS, "Settings", Icons.Filled.Settings)
)

@Composable
fun SlumberApp(viewModel: SleepViewModel) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val haptics = LocalHapticFeedback.current

    val onboardingFlag by viewModel.hasCompletedOnboarding.collectAsStateWithLifecycle()
    val hasCompletedOnboarding = onboardingFlag

    // DataStore hasn't reported the onboarding flag yet; avoid picking a start destination.
    if (hasCompletedOnboarding == null) {
        Box(modifier = Modifier.fillMaxSize())
        return
    }

    val trackingState by viewModel.trackingState.collectAsStateWithLifecycle()
    val elapsedMs by viewModel.elapsedMs.collectAsStateWithLifecycle()
    val currentStage by viewModel.currentStage.collectAsStateWithLifecycle()
    val stageHistory by viewModel.stageHistory.collectAsStateWithLifecycle()
    val amplitudeHistory by viewModel.amplitudeHistory.collectAsStateWithLifecycle()
    val snoringDetected by viewModel.snoringDetected.collectAsStateWithLifecycle()
    val snoringEventsCount by viewModel.snoringEventsCount.collectAsStateWithLifecycle()
    val recentSessions by viewModel.recentSessions.collectAsStateWithLifecycle(initialValue = emptyList())
    val allSessions by viewModel.allSessions.collectAsStateWithLifecycle(initialValue = emptyList())
    val averageSleepDuration by viewModel.averageSleepDuration.collectAsStateWithLifecycle(initialValue = null)
    val averageQuality by viewModel.averageQuality.collectAsStateWithLifecycle(initialValue = null)
    val totalSessionCount by viewModel.totalSessionCount.collectAsStateWithLifecycle(initialValue = 0)
    val totalSnoringEvents by viewModel.totalSnoringEvents.collectAsStateWithLifecycle(initialValue = null)
    val sensitivity by viewModel.snoringSensitivity.collectAsStateWithLifecycle()
    val selectedSession by viewModel.selectedSession.collectAsStateWithLifecycle()
    val isPremium by viewModel.isPremium.collectAsStateWithLifecycle()
    val productDetails by viewModel.productDetails.collectAsStateWithLifecycle()
    val billingConnectionState by viewModel.billingConnectionState.collectAsStateWithLifecycle()
    val purchaseMessage by viewModel.purchaseMessage.collectAsStateWithLifecycle()
    val trackingError by viewModel.trackingError.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        if (results.values.all { it }) {
            viewModel.startTracking()
            navController.navigate(NavRoutes.TRACKING)
        } else {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            viewModel.onTrackingPermissionDenied()
        }
    }

    LaunchedEffect(trackingError) {
        trackingError?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.dismissTrackingError()
        }
    }

    LaunchedEffect(trackingState) {
        if (trackingState == TrackingState.IDLE && navController.currentDestination?.route == NavRoutes.TRACKING) {
            navController.popBackStack()
        }
    }

    fun openPaywall() {
        navController.navigate(NavRoutes.PAYWALL)
    }

    fun closePaywall() {
        if (!navController.popBackStack()) {
            navController.navigate(NavRoutes.HOME) {
                popUpTo(NavRoutes.PAYWALL) { inclusive = true }
            }
        }
    }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = currentRoute != null && bottomNavItems.any { it.route == currentRoute }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(snackbarData = data)
            }
        },
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            selected = currentRoute == item.route,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(imageVector = item.icon, contentDescription = null) },
                            label = { Text(item.label) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = if (hasCompletedOnboarding) NavRoutes.HOME else NavRoutes.ONBOARDING,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(NavRoutes.ONBOARDING) {
                OnboardingScreen(
                    onGetStarted = {
                        viewModel.completeOnboarding()
                        navController.navigate(NavRoutes.PAYWALL) {
                            popUpTo(NavRoutes.ONBOARDING) { inclusive = true }
                        }
                    }
                )
            }

            composable(NavRoutes.HOME) {
                HomeScreen(
                    recentSessions = recentSessions,
                    averageSleepDuration = averageSleepDuration,
                    averageQuality = averageQuality,
                    totalSnoringEvents = totalSnoringEvents,
                    totalSessionCount = totalSessionCount,
                    isPremium = isPremium,
                    onStartTracking = {
                        val permissions = mutableListOf(android.Manifest.permission.RECORD_AUDIO)
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                            permissions.add(android.Manifest.permission.POST_NOTIFICATIONS)
                        }
                        permissionLauncher.launch(permissions.toTypedArray())
                    },
                    onSeeAllHistory = {
                        navController.navigate(NavRoutes.HISTORY) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onSessionClick = { id ->
                        navController.navigate(NavRoutes.sessionDetail(id))
                    },
                    onUpgradeClick = { openPaywall() }
                )
            }

            composable(NavRoutes.TRACKING) {
                TrackingScreen(
                    elapsedMs = elapsedMs,
                    currentStage = currentStage,
                    stageHistory = stageHistory,
                    amplitudeHistory = amplitudeHistory,
                    snoringDetected = snoringDetected,
                    snoringEventsCount = snoringEventsCount,
                    isPremium = isPremium,
                    onStopTracking = { viewModel.stopTracking() },
                    onUpgradeClick = { openPaywall() }
                )
            }

            composable(NavRoutes.HISTORY) {
                HistoryScreen(
                    sessions = allSessions,
                    isPremium = isPremium,
                    onSessionClick = { id -> navController.navigate(NavRoutes.sessionDetail(id)) },
                    onUpgradeClick = { openPaywall() }
                )
            }

            composable(NavRoutes.SETTINGS) {
                SettingsScreen(
                    sensitivity = sensitivity,
                    isPremium = isPremium,
                    onSensitivityChange = { viewModel.setSensitivity(it) },
                    onUpgradeClick = { openPaywall() }
                )
            }

            composable(NavRoutes.SESSION_DETAIL) { backStack ->
                val sessionId = backStack.arguments?.getString("sessionId")?.toLongOrNull() ?: 0L
                val snoringRecords by remember(sessionId) { viewModel.repository.getSnoringRecords(sessionId) }
                    .collectAsStateWithLifecycle(initialValue = emptyList())

                LaunchedEffect(sessionId) {
                    viewModel.loadSession(sessionId)
                }

                SessionDetailScreen(
                    session = selectedSession,
                    snoringRecords = snoringRecords,
                    isPremium = isPremium,
                    onBack = {
                        viewModel.clearSelectedSession()
                        navController.popBackStack()
                    },
                    onUpgradeClick = { openPaywall() }
                )
            }

            composable(NavRoutes.PAYWALL) {
                LaunchedEffect(isPremium) {
                    if (isPremium) closePaywall()
                }

                PaywallScreen(
                    productDetails = productDetails,
                    connectionState = billingConnectionState,
                    purchaseMessage = purchaseMessage,
                    onDismissMessage = { viewModel.dismissPurchaseMessage() },
                    onSelectTier = { product ->
                        (context as? Activity)?.let { activity -> viewModel.purchase(activity, product) }
                    },
                    onRestorePurchases = { viewModel.restorePurchases() },
                    onClose = { closePaywall() },
                    onOpenTerms = { uriHandler.openUri(TERMS_URL) },
                    onOpenPrivacy = { uriHandler.openUri(PRIVACY_URL) }
                )
            }
        }
    }
}
