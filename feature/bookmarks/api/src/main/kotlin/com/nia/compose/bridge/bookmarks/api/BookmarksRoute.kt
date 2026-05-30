package com.nia.compose.bridge.bookmarks.api

import android.content.Context
import android.content.Intent

object BookmarksRoute {
    const val ROUTE = "bookmarks"

    fun intent(context: Context): Intent {
        return Intent(context, Class.forName("${context.packageName}.feature.bookmarks.impl.BookmarksActivity"))
    }
}
