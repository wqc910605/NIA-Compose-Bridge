package com.empty.android.app.demo

import com.empty.android.core.mvi.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import javax.inject.Inject

/**
 * 商品目录 ViewModel。
 *
 * 演示 [BaseViewModel] 核心 API：
 * - [setState]  全量替换 State（Loading → Error / Success）
 * - [updateState]  局部更新 State（如更新某个字段）
 * - [emitEffect]  发出一次性副作用（Toast、导航）
 * - [launch]  安全的协程启动
 */
@HiltViewModel
class ProductCatalogViewModel @Inject constructor() :
    BaseViewModel<ProductCatalogUiState>(ProductCatalogUiState.Loading) {

    // ── 公开方法（替代 Intent 密封类）──────────────────────────────────────

    /**
     * 加载商品目录。
     *
     * 演示完整的 Loading → Success / Error 状态流转。
     */
    fun loadCatalog() {
        setState(ProductCatalogUiState.Loading)

        launch {
            // 模拟网络延迟
            delay(1_800)

            // 模拟：随机 20% 概率加载失败
            if (System.currentTimeMillis() % 5 == 0L) {
                setState(
                    ProductCatalogUiState.Error("Network error: unable to load catalog")
                )
                return@launch
            }

            val items = buildCatalogItems()
            setState(
                ProductCatalogUiState.Success(
                    CatalogData(
                        pageTitle = "Summer Sale 2026",
                        items = items,
                    )
                )
            )
        }
    }

    /** 商品点击 → 发出导航副作用。 */
    fun onProductClicked(product: ProductDisplayItem.Product) {
        emitEffect(
            ProductCatalogEffect.NavigateToDetail(
                productId = product.id,
                productName = product.name,
            )
        )
    }

    /** Banner 点击 → 发出 Toast 副作用。 */
    fun onBannerClicked(banner: ProductDisplayItem.Banner) {
        emitEffect(
            ProductCatalogEffect.ShowToast("Banner clicked: ${banner.title}")
        )
    }

    /** 模拟刷新：用 [updateState] 局部更新标题，不重新加载数据。 */
    fun refreshTitle() {
        @Suppress("UNCHECKED_CAST")
        val current = uiState.value as? ProductCatalogUiState.Success ?: return
        updateState {
            @Suppress("UNCHECKED_CAST")
            (it as ProductCatalogUiState.Success).copy(
                data = current.data.copy(
                    pageTitle = "Updated at ${System.currentTimeMillis() % 100_000}"
                )
            )
        }
    }

    // ── 模拟数据 ────────────────────────────────────────────────────────────

    private fun buildCatalogItems(): List<ProductDisplayItem> = listOf(
        ProductDisplayItem.Header(
            title = "Hot Products",
            subtitle = "Summer sale — up to 50% off!",
        ),
        ProductDisplayItem.Banner(
            imageUrl = "https://example.com/sale.jpg",
            title = "SUMMER SALE",
            link = "https://example.com/sale",
        ),
        ProductDisplayItem.Product(
            id = "1",
            name = "Laptop Pro 16\"",
            price = "$1,299",
            inStock = true,
        ),
        ProductDisplayItem.Product(
            id = "2",
            name = "Wireless Headphones",
            price = "$249",
            inStock = true,
        ),
        ProductDisplayItem.Product(
            id = "3",
            name = "Smart Watch Ultra",
            price = "$799",
            inStock = false,
        ),
        ProductDisplayItem.Product(
            id = "4",
            name = "Tablet Air",
            price = "$599",
            inStock = true,
        ),
        ProductDisplayItem.Product(
            id = "5",
            name = "Mechanical Keyboard",
            price = "$149",
            inStock = true,
        ),
        ProductDisplayItem.Footer(
            totalCount = 5,
        ),
    )
}
