package com.empty.android.app.demo

import androidx.recyclerview.widget.DiffUtil
import com.empty.android.core.mvi.UiEffect
import com.empty.android.core.mvi.UiState

// ═══════════════════════════════════════════════════════════════════════════════
// Display Items —— 列表数据模型（BaseMultiAdapter 的 T）
// ═══════════════════════════════════════════════════════════════════════════════

/** 多类型列表的条目基类。 */
sealed class ProductDisplayItem {

    /** 列表头部（标题 + 副标题）。 */
    data class Header(
        val title: String,
        val subtitle: String,
    ) : ProductDisplayItem()

    /** 促销横幅。 */
    data class Banner(
        val imageUrl: String,
        val title: String,
        val link: String,
    ) : ProductDisplayItem()

    /** 商品卡片。 */
    data class Product(
        val id: String,
        val name: String,
        val price: String,
        val inStock: Boolean,
    ) : ProductDisplayItem()

    /** 列表尾部（商品总数）。 */
    data class Footer(
        val totalCount: Int,
    ) : ProductDisplayItem()
}

// ═══════════════════════════════════════════════════════════════════════════════
// Page Data —— 页面数据容器
// ═══════════════════════════════════════════════════════════════════════════════

/**
 * 页面数据容器。
 *
 * 展示 Sealed Class + Data Class 组合模式：
 * 多个子模块字段（[pageTitle]、[items]）横向组合在同一个 Data Class 中，
 * 再由 Sealed Class 的 `Success` 分支包裹。
 */
data class CatalogData(
    val pageTitle: String = "Product Catalog",
    val items: List<ProductDisplayItem> = emptyList(),
)

// ═══════════════════════════════════════════════════════════════════════════════
// UiState —— 页面宏观状态（Sealed Class，互斥）
// ═══════════════════════════════════════════════════════════════════════════════

/** 商品目录页面的全部状态。 */
sealed class ProductCatalogUiState : UiState {

    /** 加载中。 */
    data object Loading : ProductCatalogUiState()

    /** 加载失败。 */
    data class Error(val message: String) : ProductCatalogUiState()

    /** 加载成功。数据由 [CatalogData] 承载。 */
    data class Success(val data: CatalogData) : ProductCatalogUiState()
}

// ═══════════════════════════════════════════════════════════════════════════════
// UiEffect —— 一次性副作用
// ═══════════════════════════════════════════════════════════════════════════════

/** 商品目录页面的一次性副作用（导航、Toast 等）。 */
sealed class ProductCatalogEffect : UiEffect {

    /** 弹出 Toast。 */
    data class ShowToast(val message: String) : ProductCatalogEffect()

    /** 点击商品，跳转详情页。 */
    data class NavigateToDetail(
        val productId: String,
        val productName: String,
    ) : ProductCatalogEffect()
}

// ═══════════════════════════════════════════════════════════════════════════════
// DiffUtil.ItemCallback —— 用于 BaseMultiAdapter 的 DiffUtil 支持
// ═══════════════════════════════════════════════════════════════════════════════

/**
 * [ProductDisplayItem] 的 DiffUtil 比较器。
 *
 * 列表只有一个 Header / Footer，用硬编码 `true` 判定同一项。
 * Product 用 [id] 判断是否同一项。
 */
object ProductDiffCallback : DiffUtil.ItemCallback<ProductDisplayItem>() {

    override fun areItemsTheSame(
        old: ProductDisplayItem,
        new: ProductDisplayItem,
    ): Boolean = when {
        old is ProductDisplayItem.Header && new is ProductDisplayItem.Header -> true
        old is ProductDisplayItem.Footer && new is ProductDisplayItem.Footer -> true
        old is ProductDisplayItem.Banner && new is ProductDisplayItem.Banner ->
            old.title == new.title
        old is ProductDisplayItem.Product && new is ProductDisplayItem.Product ->
            old.id == new.id
        else -> false
    }

    override fun areContentsTheSame(
        old: ProductDisplayItem,
        new: ProductDisplayItem,
    ): Boolean = old == new
}
