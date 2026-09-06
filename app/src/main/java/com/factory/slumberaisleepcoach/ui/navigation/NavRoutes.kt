package com.factory.slumberaisleepcoach.ui.navigation

object NavRoutes {
    const val ONBOARDING = "onboarding"
    const val HOME = "home"
    const val TRACKING = "tracking"
    const val HISTORY = "history"
    const val SETTINGS = "settings"
    const val SESSION_DETAIL = "session_detail/{sessionId}"
    const val PAYWALL = "paywall"

    fun sessionDetail(sessionId: Long) = "session_detail/$sessionId"
}
