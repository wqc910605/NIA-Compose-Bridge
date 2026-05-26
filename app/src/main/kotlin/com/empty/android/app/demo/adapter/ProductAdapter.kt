package com.empty.android.app.demo.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import com.empty.android.app.databinding.ItemProductBannerBinding
import com.empty.android.app.databinding.ItemProductCardBinding
import com.empty.android.app.databinding.ItemProductFooterBinding
import com.empty.android.app.databinding.ItemProductHeaderBinding
import com.empty.android.app.demo.ProductDisplayItem
import com.empty.android.core.viewbinding.adapter.BaseMultiAdapter
import com.empty.android.core.viewbinding.adapter.ItemViewBinder
import com.empty.android.core.viewbinding.adapter.VBViewHolder
import com.empty.android.core.viewbinding.adapter.addItemTypeExt
import com.empty.android.core.viewbinding.diffUpdate
import com.empty.android.core.viewbinding.diffUpdateNullable

/**
 * 商品目录多类型 Adapter。
 *
 * ## 设计要点
 *
 * **方式一：[ItemViewBinder] 子类** —— Header、Banner：
 * - 适合需要生命周期回调（如 `onViewRecycled` 清理资源）或逻辑较复杂时。
 * - `ItemViewBinder<T, VB>` 在类型层面建立"数据 → ViewBinding → ViewHolder"关系。
 *
 * **方式二：Lambda `addItemType`** —— Product、Footer：
 * - 适合只需绑定数据、无需生命周期回调的简单场景。
 * - Product 用完整版 lambda（接收 holder, position, item, payloads），
 *   在内部使用 [diffUpdate] 做局部刷新。
 * - Footer 用简化版 lambda（binding 为 receiver），仅需 `tvSummary.text = ...`。
 *
 * ## DiffUtil
 * 通过 [ProductDiffCallback] 启用异步 Diff，列表更新时自动计算差异动画。
 */
class ProductAdapter(
    private val onProductClick: (ProductDisplayItem.Product) -> Unit,
    private val onBannerClick: (ProductDisplayItem.Banner) -> Unit,
) : BaseMultiAdapter<ProductDisplayItem>(
    diffCallback = com.empty.android.app.demo.ProductDiffCallback,
) {

    companion object {
        const val TYPE_HEADER = 0
        const val TYPE_BANNER = 1
        const val TYPE_PRODUCT = 2
        const val TYPE_FOOTER = 3
    }

    init {
        // ── 方式一：ItemViewBinder 子类 ─────────────────────────────────

        // HeaderBinder：独立类，封装创建+绑定+生命周期
        addItemType(TYPE_HEADER, HeaderBinder())

        // BannerBinder：独立类，需要外部回调（点击事件）
        addItemType(TYPE_BANNER, BannerBinder(onBannerClick))

        // ── 方式二：Lambda addItemType ──────────────────────────────────

        // 完整版 lambda：访问 holder、position、item、payloads 全部参数
        // 在 lambda 内用 diffUpdate 做 View 级局部刷新
        addItemTypeExt(
            itemViewType = TYPE_PRODUCT,
            factory = ItemProductCardBinding::inflate,
        ) { holder, _, item, _ ->
            val product = item as? ProductDisplayItem.Product ?: return@addItemTypeExt
            with(holder.binding) {
                tvName.diffUpdate(product.name) { text = it }
                tvPrice.diffUpdate(product.price) { text = it }
                tvStock.diffUpdate(product.inStock) {
                    text = if (it) "In Stock" else "Out of Stock"
                    setTextColor(
                        if (it) Color.parseColor("#4CAF50") else Color.parseColor("#F44336")
                    )
                }
                root.setOnClickListener { onProductClick(product) }
            }
        }

        // 简化版 lambda：binding 作为 receiver，不需 position/payloads
        // 适合"拿到数据 → 更新控件"的简单绑定
        addItemTypeExt(
            itemViewType = TYPE_FOOTER,
            factory = ItemProductFooterBinding::inflate,
        ) { item ->
            val footer = item as? ProductDisplayItem.Footer ?: return@addItemTypeExt
            tvSummary.text = "${footer.totalCount} products in catalog"
        }
    }

    // ── ViewType 解析 ────────────────────────────────────────────────────
    override fun getItemViewType(position: Int, list: List<ProductDisplayItem>): Int {
        return when (list[position]) {
            is ProductDisplayItem.Header -> TYPE_HEADER
            is ProductDisplayItem.Banner -> TYPE_BANNER
            is ProductDisplayItem.Product -> TYPE_PRODUCT
            is ProductDisplayItem.Footer -> TYPE_FOOTER
        }
    }

}

// ═══════════════════════════════════════════════════════════════════════════════
// ItemViewBinder 子类示例
// ═══════════════════════════════════════════════════════════════════════════════

/**
 * Header 绑定器。
 *
 * 演示：
 * - [ItemViewBinder.onCreateBinding] 创建 ViewBinding
 * - [ItemViewBinder.onBind] 中用 [diffUpdate] / [diffUpdateNullable] 做局部刷新
 * - [ItemViewBinder.onViewRecycled] 生命周期回调
 */
class HeaderBinder : ItemViewBinder<ProductDisplayItem, ItemProductHeaderBinding>() {

    override fun onCreateBinding(inflater: LayoutInflater, parent: ViewGroup) =
        ItemProductHeaderBinding.inflate(inflater, parent, false)

    override fun onBind(
        holder: VBViewHolder<ItemProductHeaderBinding>,
        position: Int,
        item: ProductDisplayItem?,
        payloads: List<Any>,
    ) {
        val header = item as? ProductDisplayItem.Header ?: return
        with(holder.binding) {
            // diffUpdate：新值与旧值相同时跳过 setText
            tvTitle.diffUpdate(header.title) { text = it }

            // diffUpdateNullable：值为 null 时自动 GONE，非空时 VISIBLE 并更新
            // 这里 subtitle 可能为空字符串，演示 diffUpdateNullable 的可见性控制
            tvSubtitle.diffUpdateNullable(
                value = header.subtitle.takeIf { it.isNotBlank() },
                hiddenVisibility = android.view.View.GONE,
            ) { text = it }
        }
    }

    override fun onViewRecycled(holder: VBViewHolder<ItemProductHeaderBinding>) {
        // 清理图片加载等资源（示例）
    }
}

/**
 * Banner 绑定器。
 *
 * 演示：
 * - 通过构造参数接收外部回调
 * - 在 [onBind] 中设置点击监听
 */
class BannerBinder(
    private val onBannerClick: (ProductDisplayItem.Banner) -> Unit,
) : ItemViewBinder<ProductDisplayItem, ItemProductBannerBinding>() {

    override fun onCreateBinding(inflater: LayoutInflater, parent: ViewGroup) =
        ItemProductBannerBinding.inflate(inflater, parent, false)

    override fun onBind(
        holder: VBViewHolder<ItemProductBannerBinding>,
        position: Int,
        item: ProductDisplayItem?,
        payloads: List<Any>,
    ) {
        val banner = item as? ProductDisplayItem.Banner ?: return
        holder.binding.tvBannerTitle.diffUpdate(banner.title) { text = it }
        holder.binding.root.setOnClickListener { onBannerClick(banner) }
    }
}
