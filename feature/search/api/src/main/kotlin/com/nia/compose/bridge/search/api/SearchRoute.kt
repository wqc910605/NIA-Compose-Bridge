package com.nia.compose.bridge.feature.search.api

import android.content.Context
import android.content.Intent

object SearchRoute {
    const val ROUTE = "search"

    fun intent(context: Context): Intent {
        return Intent(context, Class.forName("${context.packageName}.feature.search.impl.SearchActivity"))
    }
}
