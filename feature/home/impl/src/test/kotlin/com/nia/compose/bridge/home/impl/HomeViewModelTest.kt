package com.nia.compose.bridge.feature.home.impl

import com.nia.compose.bridge.core.testing.MainDispatcherRule
import com.nia.compose.bridge.core.testing.repository.FakeDemoItemRepository
import com.nia.compose.bridge.home.impl.HomeUiState
import com.nia.compose.bridge.home.impl.HomeViewModel
import com.nia.compose.bridge.model.DemoItem
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var fakeRepository: FakeDemoItemRepository
    private lateinit var viewModel: HomeViewModel

    @Before
    fun setup() {
        fakeRepository = FakeDemoItemRepository()
        viewModel = HomeViewModel(repository = fakeRepository)
    }

    @Test
    fun stateIsInitiallyLoading() = runTest {
        assertTrue(viewModel.uiState.value is HomeUiState.Loading)
    }

    @Test
    fun stateIsEmptyWhenNoItems() = runTest {
        fakeRepository.setItems(emptyList())
        viewModel.refresh()
        val state = viewModel.uiState.first { it !is HomeUiState.Loading }
        assertTrue(state is HomeUiState.Empty)
    }

    @Test
    fun stateIsSuccessWhenItemsExist() = runTest {
        val testItems = listOf(
            DemoItem("1", "Title 1", "Desc 1"),
            DemoItem("2", "Title 2", "Desc 2"),
        )
        fakeRepository.setItems(testItems)
        viewModel.refresh()
        val state = viewModel.uiState.first { it !is HomeUiState.Loading }
        assertTrue(state is HomeUiState.Success)
        assertEquals(2, (state as HomeUiState.Success).data.items.size)
    }

    @Test
    fun addItemIncreasesItemCount() = runTest {
        viewModel.addSample()
        val state = viewModel.uiState.first { it is HomeUiState.Success }
        assertEquals(1, (state as HomeUiState.Success).data.items.size)
    }

    @Test
    fun removeItemDecreasesItemCount() = runTest {
        val testItem = DemoItem("1", "Title", "Desc")
        fakeRepository.setItems(listOf(testItem))
        viewModel.remove("1")
        val state = viewModel.uiState.first { it !is HomeUiState.Loading }
        assertTrue(state is HomeUiState.Empty)
    }
}
