package com.nia.compose.bridge.viewbinding.adapter

import android.util.SparseArray
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

/**
 * 多条目 Adapter —— 一个条目类型对应一个 ViewBinding 布局。
 *
 * ## 设计理念
 * 每个 itemViewType 通过 [ItemViewBinder] 绑定到特定的 [ViewBinding] 布局，
 * 由 [VBViewHolder] 持有该绑定对象。数据绑定逻辑封装在 [ItemViewBinder.onBind] 中。
 * ## 用法
 *
 * ### 方式一：ItemViewBinder（推荐，复杂逻辑）
 * ```kotlin
 * class ChatAdapter : BaseMultiAdapter<ChatMessage>(diffCallback = ChatMessage.Diff) {
 *
 *     init {
 *         addItemType(TYPE_TEXT, TextBinder())
 *         addItemType(TYPE_IMAGE, ImageBinder())
 *     }
 *
 *     override fun onItemViewType(position: Int, list: List<ChatMessage>): Int {
 *         return when (list[position]) {
 *             is ChatMessage.Text  -> TYPE_TEXT
 *             is ChatMessage.Image -> TYPE_IMAGE
 *         }
 *     }
 * }
 *
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
 * ### 方式二：Lambda（简单场景）
 * ```kotlin
 * adapter.addItemTypeExt(TYPE_TEXT, ItemTextBinding::inflate) { binding, _, item, _ ->
 *     binding.tvContent.text = (item as? ChatMessage.Text)?.content
 * }
 *
 * // 或更简：
 * adapter.addItemTypeExt(TYPE_HEADER, ItemHeaderBinding::inflate) { msg ->
 *     tvTitle.text = (msg as? HeaderMsg)?.title
 * }
 * ```
 *
 * @param T 数据类型
 */
abstract class BaseMultiAdapter<T : Any> @JvmOverloads constructor(
    items: List<T> = emptyList(),
    diffCallback: DiffUtil.ItemCallback<T>? = null,
) : BaseAdapter<T, RecyclerView.ViewHolder>(items, diffCallback) {

    private val typeBinders = SparseArray<ItemViewBinder<T, *>>(4)
    private var onItemViewTypeListener: OnItemViewTypeListener<T>? = null

    companion object {
        private val TAG_BINDER = 100
    }

    // ── 注册条目类型 ──────────────────────────────────────────────────────

    /**
     * 注册一种条目类型。
     *
     * @param itemViewType 由 [onItemViewType] 解析的 viewType 值
     * @param binder [ItemViewBinder]，定义该类型的 ViewBinding 创建与数据绑定逻辑
     */
    fun <VB : ViewBinding> addItemType(
        itemViewType: Int,
        binder: ItemViewBinder<T, VB>,
    ) = apply {
        typeBinders.put(itemViewType, binder)
    }

    /**
     * 设置 ViewType 解析器。根据 position 和 list 返回对应的 viewType。
     */
    fun onItemViewType(listener: OnItemViewTypeListener<T>?) = apply {
        onItemViewTypeListener = listener
    }

    // ── Adapter 回调 ──────────────────────────────────────────────────────

    final override fun onCreateViewHolderVH(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binder = requireBinder(viewType)
        val binding = binder.onCreateBinding(LayoutInflater.from(parent.context), parent)
        return VBViewHolder(binding).also { vh ->
            vh.itemView.setTag(TAG_BINDER, binder)
        }
    }

    @Suppress("UNCHECKED_CAST")
    final override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        item: T?,
    ) {
        val binder = holder.itemView.getTag(TAG_BINDER) as ItemViewBinder<T, ViewBinding>
        binder.onBind(holder as VBViewHolder<ViewBinding>, position, item)
    }

    @Suppress("UNCHECKED_CAST")
    final override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        item: T?,
        payloads: List<Any>,
    ) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position, item)
            return
        }
        val binder = holder.itemView.getTag(TAG_BINDER) as ItemViewBinder<T, ViewBinding>
        binder.onBind(holder as VBViewHolder<ViewBinding>, position, item, payloads)
    }

    override fun getItemViewType(position: Int, list: List<T>): Int {
        return onItemViewTypeListener?.onItemViewType(position, list)
            ?: super.getItemViewType(position, list)
    }

    // ── 生命周期转发 ──────────────────────────────────────────────────────

    @Suppress("UNCHECKED_CAST")
    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
        super.onViewAttachedToWindow(holder)
        (holder.itemView.getTag(TAG_BINDER) as? ItemViewBinder<T, ViewBinding>)
            ?.onViewAttachedToWindow(holder as VBViewHolder<ViewBinding>)
    }

    @Suppress("UNCHECKED_CAST")
    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
        super.onViewDetachedFromWindow(holder)
        (holder.itemView.getTag(TAG_BINDER) as? ItemViewBinder<T, ViewBinding>)
            ?.onViewDetachedFromWindow(holder as VBViewHolder<ViewBinding>)
    }

    @Suppress("UNCHECKED_CAST")
    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)
        (holder.itemView.getTag(TAG_BINDER) as? ItemViewBinder<T, ViewBinding>)
            ?.onViewRecycled(holder as VBViewHolder<ViewBinding>)
    }

    @Suppress("UNCHECKED_CAST")
    override fun onFailedToRecycleView(holder: RecyclerView.ViewHolder): Boolean {
        return (holder.itemView.getTag(TAG_BINDER) as? ItemViewBinder<T, ViewBinding>)
            ?.onFailedToRecycleView(holder as VBViewHolder<ViewBinding>) ?: false
    }

    // ── 内部 ──────────────────────────────────────────────────────────────

    private fun requireBinder(viewType: Int): ItemViewBinder<T, *> {
        return typeBinders.get(viewType)
            ?: throw IllegalArgumentException(
                "ViewType $viewType not registered. Call addItemType() in init {} first."
            )
    }

    // ── 内部接口 ──────────────────────────────────────────────────────────

    /** ViewType 解析器。根据 position 和 list 决定 itemViewType。 */
    fun interface OnItemViewTypeListener<T> {
        fun onItemViewType(position: Int, list: List<T>): Int
    }
}

// ── Lambda 扩展：addItemType 便捷 API ──────────────────────────────────────

/**
 * Lambda 方式注册条目类型（完整版）。
 *
 * 直接传入 [ItemViewBinder.onBind] 签名的 lambda，无需创建 ItemViewBinder 子类。
 *
 * ```kotlin
 * adapter.addItemTypeExt(TYPE_TEXT, ItemTextBinding::inflate) { holder, pos, item, payloads ->
 *     holder.binding.tvContent.text = (item as? ChatMessage.Text)?.content
 * }
 * ```
 *
 * @param itemViewType viewType 值
 * @param factory ViewBinding 创建工厂，通常传入 `BindingClass::inflate`
 * @param onBind 绑定回调，签名与 [ItemViewBinder.onBind] 一致
 */
fun <T: Any, VB : ViewBinding> BaseMultiAdapter<T>.addItemTypeExt(
    itemViewType: Int,
    factory: (LayoutInflater, ViewGroup?, Boolean) -> VB,
    onBind: (VBViewHolder<VB>, Int, T?, List<Any>) -> Unit = { _, _, _, _ -> },
): BaseMultiAdapter<T> = addItemType(itemViewType, object : ItemViewBinder<T, VB>() {
    override fun onCreateBinding(inflater: LayoutInflater, parent: ViewGroup) =
        factory(inflater, parent, false)

    override fun onBind(
        holder: VBViewHolder<VB>,
        position: Int,
        item: T?,
        payloads: List<Any>,
    ) {
        onBind(holder, position, item, payloads)
    }
})

/**
 * Lambda 方式注册条目类型（简化版）。
 *
 * 以 binding 为接收者，适合只需绑定数据、不关心 position/payloads 的场景。
 *
 * ```kotlin
 * adapter.addItemTypeExt(TYPE_HEADER, ItemHeaderBinding::inflate) { msg ->
 *     tvTitle.text = (msg as? HeaderMsg)?.title
 * }
 * ```
 *
 * @param itemViewType viewType 值
 * @param factory ViewBinding 创建工厂
 * @param bind 绑定逻辑，binding 作为接收者，参数为数据项
 */
fun <T: Any, VB : ViewBinding> BaseMultiAdapter<T>.addItemTypeExt(
    itemViewType: Int,
    factory: (LayoutInflater, ViewGroup, Boolean) -> VB,
    bind: VB.(T?) -> Unit,
): BaseMultiAdapter<T> = addItemType(itemViewType, object : ItemViewBinder<T, VB>() {
    override fun onCreateBinding(inflater: LayoutInflater, parent: ViewGroup) =
        factory(inflater, parent, false)

    override fun onBind(
        holder: VBViewHolder<VB>,
        position: Int,
        item: T?,
        payloads: List<Any>,
    ) {
        holder.binding.bind(item)
    }
})
