package com.nia.compose.bridge.feature.topic.impl

import androidx.lifecycle.viewModelScope
import com.nia.compose.bridge.core.data.repository.DemoItemRepository
import com.nia.compose.bridge.core.mvi.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TopicViewModel @Inject constructor(
    private val repository: DemoItemRepository,
) : BaseViewModel<TopicUiState>(TopicUiState.Loading) {

    fun loadTopic(itemId: String) {
        viewModelScope.launch {
            setState(TopicUiState.Loading)
            val item = runCatching { repository.observeItems().collect { items ->
                val found = items.find { it.id == itemId }
                if (found != null) {
                    setState(TopicUiState.Success(TopicData(item = found)))
                } else {
                    setState(TopicUiState.NotFound)
                }
            } }.onFailure {
                setState(TopicUiState.NotFound)
            }
        }
    }

    fun navigateBack() {
        emitEffect(TopicEffect.NavigateBack)
    }
}
