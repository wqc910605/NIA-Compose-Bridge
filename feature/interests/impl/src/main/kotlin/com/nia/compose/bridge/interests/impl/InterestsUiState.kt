package com.nia.compose.bridge.feature.interests.impl

import com.nia.compose.bridge.core.mvi.UiEffect
import com.nia.compose.bridge.core.mvi.UiState
import com.nia.compose.bridge.core.model.DemoItem

data class InterestItem(
    val item: DemoItem,
    val isFollowed: Boolean,
)

data class InterestsData(
    val items: List<InterestItem> = emptyList(),
    val selectedId: String? = null,
)

sealed interface InterestsUiState : UiState {
    data object Loading : InterestsUiState
    data object Empty : InterestsUiState
    data class Success(val data: InterestsData) : InterestsUiState
}

sealed interface InterestsEffect : UiEffect {
    data class ShowToast(val message: String) : InterestsEffect
}
