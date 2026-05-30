package com.nia.compose.bridge.domain

import com.nia.compose.bridge.data.repository.DemoItemRepository
import com.nia.compose.bridge.model.DemoItem
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * UseCase 示例：将仓库暴露给上层，方便在多个 feature 间复用。
 *
 * 实际业务可以在此加入排序、过滤、组合其它仓库等逻辑。
 */
class GetDemoItemsUseCase @Inject constructor(
    private val repository: DemoItemRepository,
) {
    operator fun invoke(): Flow<List<DemoItem>> = repository.observeItems()
}
