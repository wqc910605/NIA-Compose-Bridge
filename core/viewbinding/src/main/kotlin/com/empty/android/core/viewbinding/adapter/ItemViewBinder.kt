package com.empty.android.core.viewbinding.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding

/**
 * 条目类型与 ViewBinding 的绑定器。
 *
 * 定义了一个条目类型（itemViewType）如何：
 * 1. 创建对应的 [ViewBinding] 布局 —— [onCreateBinding]
 * 2. 将数据绑定到布局 —— [onBind]
 * 3. 响应生命周期回调
 *
 * ## 设计意图
 * 替代旧版 [OnMultiItemAdapterListener]，让"一个条目类型 → 一个 ViewBinding → 一个 ViewHolder"
 * 的关系显式化。ItemViewBinder 即这个三元关系的载体。
 *
 * ## 用法
 * ```kotlin
 * class TextBinder : ItemViewBinder<ChatMessage, ItemTextBinding>() {
 *     override fun onCreateBinding(inflater: LayoutInflater, parent: ViewGroup) =
 *         ItemTextBinding.inflate(inflater, parent, false)
 *
 *     override fun onBind(holder: VBViewHolder<ItemTextBinding>, position: Int,
 *                         item: ChatMessage?, payloads: List<Any>) {
 *         holder.binding.tvContent.text = (item as? ChatMessage.Text)?.content
 *     }
 * }
 * ```
 *
 * @param T 数据模型类型（与 Adapter 的 T 一致）
 * @param VB ViewBinding 类型，对应此条目类型的布局
 */
abstract class ItemViewBinder<T, VB : ViewBinding> {

    /**
     * 创建 ViewBinding 实例。
     *
     * 典型实现：
     * ```kotlin
     * override fun onCreateBinding(inflater: LayoutInflater, parent: ViewGroup) =
     *     ItemTextBinding.inflate(inflater, parent, false)
     * ```
     */
    abstract fun onCreateBinding(inflater: LayoutInflater, parent: ViewGroup): VB

    /**
     * 将数据绑定到 ViewHolder。
     *
     * @param holder ViewBinding ViewHolder，通过 `holder.binding` 访问布局
     * @param position 列表位置
     * @param item 数据项，可能为 null
     * @param payloads DiffUtil payloads，空列表表示全量刷新
     */
    open fun onBind(
        holder: VBViewHolder<VB>,
        position: Int,
        item: T?,
        payloads: List<Any> = emptyList(),
    ) {
    }

    // ── 生命周期回调 ──────────────────────────────────────────────────────

    /** ViewHolder 附着到窗口时回调。 */
    open fun onViewAttachedToWindow(holder: VBViewHolder<VB>) {}

    /** ViewHolder 从窗口分离时回调。 */
    open fun onViewDetachedFromWindow(holder: VBViewHolder<VB>) {}

    /** ViewHolder 被回收时回调。 */
    open fun onViewRecycled(holder: VBViewHolder<VB>) {}

    /** ViewHolder 回收失败时回调。返回 true 表示已处理。 */
    open fun onFailedToRecycleView(holder: VBViewHolder<VB>): Boolean = false
}
