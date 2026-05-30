package com.nia.compose.bridge.feature.interests.impl

import androidx.lifecycle.viewModelScope
import com.nia.compose.bridge.core.data.repository.DemoItemRepository
import com.nia.compose.bridge.core.mvi.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InterestsViewModel @Inject constructor(
    private val repository: DemoItemRepository,
) : BaseViewModel<InterestsUiState>(InterestsUiState.Loading) {

    private val followedIds = mutableSetOf<String>()

    init {
        observeItems()
    }

    private fun observeItems() {
        viewModelScope.launch {
            repository.observeItems().collectLatest { items ->
                val interestItems = items.map { item ->
                    InterestItem(item = item, isFollowed = followedIds.contains(item.id))
                }
                if (interestItems.isEmpty()) {
                    setState(InterestsUiState.Empty)
                } else {
                    setState(InterestsUiState.Success(InterestsData(items = interestItems)))
                }
            }
        }
    }

    fun toggleFollow(id: String) {
        if (followedIds.contains(id)) {
            followedIds.remove(id)
            emitEffect(InterestsEffect.ShowToast("已取消关注"))
        } else {
            followedIds.add(id)
            emitEffect(InterestsEffect.ShowToast("已关注"))
        }
        refreshState()
    }

    fun selectItem(id: String) {
        setState(
            (uiState.value as? InterestsUiState.Success)?.copy(
                data = (uiState.value as InterestsUiState.Success).data.copy(selectedId = id)
            ) ?: uiState.value
        )
    }

    private fun refreshState() {
        viewModelScope.launch {
            repository.observeItems().collectLatest { items ->
                val interestItems = items.map { item ->
                    InterestItem(item = item, isFollowed = followedIds.contains(item.id))
                }
                setState(InterestsUiState.Success(InterestsData(items = interestItems)))
            }
        }
    }
}
