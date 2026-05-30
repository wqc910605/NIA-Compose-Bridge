package com.nia.compose.bridge.feature.search.impl

import androidx.lifecycle.viewModelScope
import com.nia.compose.bridge.core.data.repository.DemoItemRepository
import com.nia.compose.bridge.core.mvi.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: DemoItemRepository,
) : BaseViewModel<SearchUiState>(SearchUiState.Idle) {

    private var searchJob: Job? = null

    fun onSearchQueryChanged(query: String) {
        searchJob?.cancel()
        if (query.isBlank()) {
            setState(SearchUiState.Idle)
            return
        }
        searchJob = viewModelScope.launch {
            setState(SearchUiState.Searching)
            delay(300)
            repository.observeItems().collectLatest { items ->
                val filtered = items.filter {
                    it.title.contains(query, ignoreCase = true) ||
                    it.description.contains(query, ignoreCase = true)
                }
                if (filtered.isEmpty()) {
                    setState(SearchUiState.Empty(query))
                } else {
                    setState(
                        SearchUiState.Success(
                            SearchData(query = query, results = filtered)
                        )
                    )
                }
            }
        }
    }

    fun clearSearch() {
        searchJob?.cancel()
        setState(SearchUiState.Idle)
    }
}
