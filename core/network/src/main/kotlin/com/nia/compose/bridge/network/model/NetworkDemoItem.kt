package com.nia.compose.bridge.network.model

import com.nia.compose.bridge.model.DemoItem
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NetworkDemoItem(
    @SerialName("id") val id: String,
    @SerialName("title") val title: String,
    @SerialName("description") val description: String = "",
    @SerialName("iconUrl") val iconUrl: String? = null,
)

fun NetworkDemoItem.asExternalModel(): DemoItem = DemoItem(
    id = id,
    title = title,
    description = description,
    iconUrl = iconUrl,
)
