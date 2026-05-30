package com.nia.compose.bridge.data.repository

import com.nia.compose.bridge.model.DemoItem
import kotlinx.coroutines.flow.Flow

interface DemoItemRepository {
    fun observeItems(): Flow<List<DemoItem>>
    suspend fun refreshItems()
    suspend fun addItem(item: DemoItem)
    suspend fun removeItem(id: String)
}
