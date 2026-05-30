package com.nia.compose.bridge.viewbinding.adapter

import androidx.annotation.IntRange
import androidx.recyclerview.widget.RecyclerView

/**
 * 单条目 Adapter —— 用于只展示一个 item 的场景（如详情页头部、设置卡片）。
 *
 * 从 BRVAH `BaseSingleItemAdapter` 提取，适配 [BaseAdapter] 体系。
 *
 * ## 用法
 * ```kotlin
 * class ProfileAdapter : BaseSingleAdapter<Profile, ProfileAdapter.VH>() {
 *
 *     override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
 *         val binding = ItemProfileBinding.inflate(
 *             LayoutInflater.from(parent.context), parent, false
 *         )
 *         return VH(binding)
 *     }
 *
 *     override fun onBindViewHolder(holder: VH, item: Profile?) {
 *         holder.bind(item)
 *     }
 *
 *     // 调用：
 *     adapter.item = profile      // 全量刷新
 *     adapter.setItem(profile, payload)  // 局部刷新
 * }
 * ```
 *
 * @param T 数据类型
 * @param VH ViewHolder 类型
 */
abstract class BaseSingleAdapter<T : Any, VH : RecyclerView.ViewHolder>(
    private var mItem: T? = null,
) : BaseAdapter<Any, VH>() {

    /** 子类实现：绑定 item 到 ViewHolder。 */
    protected abstract fun onBindViewHolder(holder: VH, item: T?)

    /** 局部刷新（payload）。默认委托给 [onBindViewHolder]。 */
    protected open fun onBindViewHolder(holder: VH, item: T?, payloads: List<Any>) {
        onBindViewHolder(holder, item)
    }

    final override fun onBindViewHolder(holder: VH, position: Int, item: Any?) {
        onBindViewHolder(holder, mItem)
    }

    final override fun onBindViewHolder(holder: VH, position: Int, item: Any?, payloads: List<Any>) {
        onBindViewHolder(holder, mItem, payloads)
    }

    final override fun getItemCount(items: List<Any>): Int = 1

    /** 获取/设置 item 数据，设置时触发全量刷新。 */
    var item: T?
        get() = mItem
        set(value) {
            mItem = value
            notifyItemChanged(0)
        }

    /** 设置 item 并携带 payload 做局部刷新。 */
    fun setItem(t: T?, payload: Any?) {
        mItem = t
        notifyItemChanged(0, payload)
    }

    // ── 禁用多条目操作方法 ──────────────────────────────────────────────────

    override fun submitList(list: List<Any>?, commitCallback: Runnable?) {
        throw UnsupportedOperationException("Please use setItem()")
    }

    override fun add(data: Any) {
        throw UnsupportedOperationException("Please use setItem()")
    }

    override fun add(@IntRange(from = 0) position: Int, data: Any) {
        throw UnsupportedOperationException("Please use setItem()")
    }

    override fun addAll(collection: Collection<Any>) {
        throw UnsupportedOperationException("Please use setItem()")
    }

    override fun remove(data: Any) {
        throw UnsupportedOperationException("Please use setItem()")
    }

    override fun removeAt(position: Int) {
        throw UnsupportedOperationException("Please use setItem()")
    }

    override fun set(position: Int, data: Any) {
        throw UnsupportedOperationException("Please use setItem()")
    }
}
