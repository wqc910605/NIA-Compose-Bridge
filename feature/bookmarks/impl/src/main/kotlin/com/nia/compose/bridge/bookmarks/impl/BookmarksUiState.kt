package com.nia.compose.bridge.bookmarks.impl

import com.nia.compose.bridge.common.UiEffect
import com.nia.compose.bridge.common.UiState


data class BookmarksData(
    val items: List<DemoItem> = emptyList(),
)

sealed interface BookmarksUiState : UiState {
    data object Loading : BookmarksUiState
    data object Empty : BookmarksUiState
    data class Success(val data: BookmarksData) : BookmarksUiState
}

sealed interface BookmarksEffect : UiEffect {
    data class ShowToast(val message: String) : BookmarksEffect
    data class ShowUndoSnackbar(val message: String, val itemId: String) : BookmarksEffect
}
