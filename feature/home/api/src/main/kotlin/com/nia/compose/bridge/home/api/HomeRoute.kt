package com.nia.compose.bridge.home.api

import android.content.Context
import android.content.Intent

object HomeRoute {
    const val ROUTE = "home"

    fun intent(context: Context): Intent {
        return Intent(context, Class.forName("${context.packageName}.feature.home.impl.HomeActivity"))
    }
}
