package com.nia.compose.bridge.feature.topic.impl

import com.nia.compose.bridge.core.mvi.UiEffect
import com.nia.compose.bridge.core.mvi.UiState
import com.nia.compose.bridge.core.model.DemoItem

data class TopicData(
    val item: DemoItem,
)

sealed interface TopicUiState : UiState {
    data object Loading : TopicUiState
    data object NotFound : TopicUiState
    data class Success(val data: TopicData) : TopicUiState
}

sealed interface TopicEffect : UiEffect {
    data object NavigateBack : TopicEffect
}
