package com.nia.compose.bridge.home.impl

import androidx.lifecycle.viewModelScope
import com.nia.compose.bridge.data.repository.DemoItemRepository
import com.nia.compose.bridge.common.BaseViewModel
import com.nia.compose.bridge.model.DemoItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: DemoItemRepository,
) : BaseViewModel<HomeUiState>(HomeUiState.Loading) {

    init {
        observeItems()
        refresh()
    }

    private fun observeItems() {
        viewModelScope.launch {
            repository.observeItems().collectLatest { items ->
                if (items.isEmpty() && uiState.value is HomeUiState.Success) {
                    setState(HomeUiState.Empty)
                } else if (items.isNotEmpty()) {
                    setState(
                        HomeUiState.Success(
                            HomeData(items = items)
                        )
                    )
                }
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            setState(HomeUiState.Loading)
            try {
                repository.refreshItems()
            } catch (e: Exception) {
                setState(HomeUiState.Error(e.message))
                emitEffect(HomeEffect.ShowToast("刷新失败: ${e.message}"))
            }
        }
    }

    fun addSample() {
        viewModelScope.launch {
            val id = System.currentTimeMillis().toString()
            repository.addItem(
                DemoItem(
                    id = id,
                    title = "Sample #$id",
                    description = "这条数据是在本地新增的示例项",
                ),
            )
            emitEffect(HomeEffect.ShowToast("已添加示例数据"))
        }
    }

    fun remove(id: String) {
        viewModelScope.launch {
            repository.removeItem(id)
            emitEffect(HomeEffect.ShowToast("已删除"))
        }
    }

    fun navigateToSettings() {
        emitEffect(HomeEffect.NavigateToSettings)
    }

    fun navigateToSearch() {
        emitEffect(HomeEffect.NavigateToSearch)
    }

    fun navigateToBookmarks() {
        emitEffect(HomeEffect.NavigateToBookmarks)
    }

    fun navigateToInterests() {
        emitEffect(HomeEffect.NavigateToInterests)
    }
}
