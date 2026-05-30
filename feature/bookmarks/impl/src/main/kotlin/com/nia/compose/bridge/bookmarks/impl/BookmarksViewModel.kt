package com.nia.compose.bridge.bookmarks.impl

import androidx.lifecycle.viewModelScope
import com.nia.compose.bridge.common.BaseViewModel
import com.nia.compose.bridge.data.repository.DemoItemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookmarksViewModel @Inject constructor(
    private val repository: DemoItemRepository,
) : BaseViewModel<BookmarksUiState>(BookmarksUiState.Loading) {

    init {
        observeBookmarks()
    }

    private fun observeBookmarks() {
        viewModelScope.launch {
            repository.observeItems().collectLatest { items ->
                if (items.isEmpty()) {
                    setState(BookmarksUiState.Empty)
                } else {
                    setState(BookmarksUiState.Success(BookmarksData(items = items)))
                }
            }
        }
    }

    fun removeBookmark(id: String) {
        viewModelScope.launch {
            repository.removeItem(id)
            emitEffect(BookmarksEffect.ShowUndoSnackbar("已移除收藏", id))
        }
    }

    fun undoRemove(id: String) {
        viewModelScope.launch {
            val id = System.currentTimeMillis().toString()
            repository.addItem(
                com.nia.compose.bridge.core.model.DemoItem(
                    id = id,
                    title = "Sample #$id",
                    description = "这条数据是在本地新增的示例项",
                ),
            )
            emitEffect(BookmarksEffect.ShowToast("已恢复收藏"))
        }
    }
}
