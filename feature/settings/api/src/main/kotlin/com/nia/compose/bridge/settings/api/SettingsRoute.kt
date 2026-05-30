package com.nia.compose.bridge.feature.settings.api

import android.content.Context
import android.content.Intent

object SettingsRoute {
    const val ROUTE = "settings"

    fun intent(context: Context): Intent {
        return Intent(context, Class.forName("${context.packageName}.feature.settings.impl.SettingsActivity"))
    }
}
