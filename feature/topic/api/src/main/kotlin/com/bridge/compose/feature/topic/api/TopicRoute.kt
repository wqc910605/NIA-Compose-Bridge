package com.nia.compose.bridge.feature.topic.api

import android.content.Context
import android.content.Intent

object TopicRoute {
    const val ROUTE = "topic"
    const val EXTRA_ITEM_ID = "itemId"

    fun intent(context: Context, itemId: String): Intent {
        return Intent(context, Class.forName("${context.packageName}.feature.topic.impl.TopicDetailActivity")).apply {
            putExtra(EXTRA_ITEM_ID, itemId)
        }
    }
}
