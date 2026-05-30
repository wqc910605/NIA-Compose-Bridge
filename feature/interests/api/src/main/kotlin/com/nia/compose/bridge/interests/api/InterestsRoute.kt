package com.nia.compose.bridge.interests.api

import android.content.Context
import android.content.Intent

object InterestsRoute {
    const val ROUTE = "interests"

    fun intent(context: Context): Intent {
        return Intent(context, Class.forName("${context.packageName}.feature.interests.impl.InterestsActivity"))
    }
}
