package com.nia.compose.bridge.viewbinding.adapter

import android.content.Context
import android.util.SparseArray
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.annotation.IdRes
import androidx.annotation.IntRange
import androidx.recyclerview.widget.AdapterListUpdateCallback
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import java.util.Collections

/**
 * RecyclerView Adapter 基类，支持 DiffUtil、CRUD 操作、点击监听。
 *
 * ## 与 BRVAH 的关系
 * 从 [BaseRecyclerViewAdapterHelper](https://github.com/CymChad/BaseRecyclerViewAdapterHelper)
 * 的 `BaseQuickAdapter` 提取核心能力，移除动画、空视图、拖拽等功能。
 *
 * ## 核心特性
 * - **DiffUtil 可选**：传入 [DiffUtil.ItemCallback] 自动启用异步 Diff，否则退化为普通列表
 * - **CRUD 操作**：add / remove / set / swap / move / submitList
 * - **点击监听**：item 点击、长按；子 View 点击、长按
 * - **recyclerView 上下文**：通过 [recyclerView] / [context] 便捷获取
 *
 * ## 用法
 * ```kotlin
 * class TaskAdapter : BaseAdapter<TaskItem, TaskAdapter.VH>(TaskDiffCallback) {
 *
 *     override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
 *         val binding = ItemTaskBinding.inflate(
 *             LayoutInflater.from(parent.context), parent, false
 *         )
 *         return VH(binding)
 *     }
 *
 *     override fun onBindViewHolder(holder: VH, position: Int, item: TaskItem?) {
 *         holder.bind(item!!)
 *     }
 *
 *     class VH(private val binding: ItemTaskBinding) : RecyclerView.ViewHolder(binding.root) {
 *         fun bind(task: TaskItem) {
 *             binding.tvTitle.text = task.title
 *         }
 *     }
 *
 *     companion object DiffCallback : DiffUtil.ItemCallback<TaskItem>() {
 *         override fun areItemsTheSame(old: TaskItem, new: TaskItem) = old.id == new.id
 *         override fun areContentsTheSame(old: TaskItem, new: TaskItem) = old == new
 *     }
 * }
 * ```
 *
 * @param T 数据类型
 * @param VH ViewHolder 类型
 */
abstract class BaseAdapter<T : Any, VH : RecyclerView.ViewHolder>
@JvmOverloads constructor(
    items: List<T> = emptyList(),
    diffCallback: DiffUtil.ItemCallback<T>? = null,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // ── DiffUtil ────────────────────────────────────────────────────────────

    private val mDiffer: AsyncListDiffer<T>? =
        diffCallback?.let { cb ->
            AsyncListDiffer(AdapterListUpdateCallback(this), AsyncDifferConfig.Builder(cb).build())
                .also { it.submitList(items) }
        }

    // ── Items ───────────────────────────────────────────────────────────────

    private var _items: List<T> = if (mDiffer == null) items else emptyList()

    /** 当前数据列表 */
    var items: List<T>
        get() = mDiffer?.currentList ?: _items
        set(value) {
            if (mDiffer != null) {
                mDiffer.submitList(value, null)
            } else {
                val old = _items
                _items = value
                onItemsChanged(old, _items)
            }
        }

    /** 内部可变列表（无 DiffUtil 时使用） */
    private val mutableItems: MutableList<T>
        get() = (_items as? ArrayList<T>) ?: ArrayList(_items).also { _items = it }

    // ── RecyclerView / Context ──────────────────────────────────────────────

    private var _recyclerView: RecyclerView? = null

    /** 绑定的 RecyclerView（在 [onAttachedToRecyclerView] 后可用）。 */
    val recyclerView: RecyclerView
        get() = checkNotNull(_recyclerView) {
            "Please get it after onAttachedToRecyclerView()"
        }

    /** 上下文快捷方式 */
    val context: Context
        get() = recyclerView.context

    // ── 点击监听 ────────────────────────────────────────────────────────────

    private var mOnItemClickListener: OnItemClickListener<T>? = null
    private var mOnItemLongClickListener: OnItemLongClickListener<T>? = null
    private var mOnItemChildClickArray: SparseArray<OnItemChildClickListener<T>>? = null
    private var mOnItemChildLongClickArray: SparseArray<OnItemChildLongClickListener<T>>? = null

    fun setOnItemClickListener(listener: OnItemClickListener<T>?) = apply {
        mOnItemClickListener = listener
    }

    fun setOnItemLongClickListener(listener: OnItemLongClickListener<T>?) = apply {
        mOnItemLongClickListener = listener
    }

    fun addOnItemChildClickListener(@IdRes id: Int, listener: OnItemChildClickListener<T>) = apply {
        (mOnItemChildClickArray ?: SparseArray<OnItemChildClickListener<T>>(2).also {
            mOnItemChildClickArray = it
        }).put(id, listener)
    }

    fun addOnItemChildLongClickListener(@IdRes id: Int, listener: OnItemChildLongClickListener<T>) = apply {
        (mOnItemChildLongClickArray ?: SparseArray<OnItemChildLongClickListener<T>>(2).also {
            mOnItemChildLongClickArray = it
        }).put(id, listener)
    }

    // ── 子类必须实现 ────────────────────────────────────────────────────────

    protected abstract fun onCreateViewHolderVH(parent: ViewGroup, viewType: Int): VH

    protected abstract fun onBindViewHolder(holder: VH, position: Int, item: T?)

    final override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return onCreateViewHolderVH(parent, viewType).apply { bindViewClickListener(this) }
    }

    /** 局部刷新（可通过 payload 优化）。默认委托给 [onBindViewHolder]。 */
    protected open fun onBindViewHolder(holder: VH, position: Int, item: T?, payloads: List<Any>) {
        onBindViewHolder(holder, position, item)
    }

    // ── 可选覆写 ────────────────────────────────────────────────────────────

    /** 返回数据条数。默认 [List.size]，单条目适配器会覆写为 1。 */
    protected open fun getItemCount(items: List<T>): Int = items.size

    /** 返回 ViewType。默认 0，多条目适配器会覆写。 */
    protected open fun getItemViewType(@Suppress("UNUSED_PARAMETER") position: Int, list: List<T>): Int = 0

    // ── RecyclerView.Adapter final methods ──────────────────────────────────

    final override fun getItemCount(): Int = getItemCount(items)

    final override fun getItemViewType(position: Int): Int = getItemViewType(position, items)

    final override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        @Suppress("UNCHECKED_CAST")
        onBindViewHolder(holder as VH, position, getItemOrNull(position))
    }

    final override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position)
            return
        }
        @Suppress("UNCHECKED_CAST")
        onBindViewHolder(holder as VH, position, getItemOrNull(position), payloads)
    }

    // ── Lifecycle ───────────────────────────────────────────────────────────

    @CallSuper
    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        _recyclerView = recyclerView
    }

    @CallSuper
    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        _recyclerView = null
    }

    // ── 数据操作 ────────────────────────────────────────────────────────────

    fun getItem(@IntRange(from = 0) position: Int): T = items[position]

    fun getItemOrNull(@IntRange(from = 0) position: Int): T? = items.getOrNull(position)

    open fun submitList(list: List<T>?, commitCallback: Runnable? = null) {
        if (mDiffer != null) {
            mDiffer.submitList(list, commitCallback)
        } else {
            val newList = list ?: emptyList()
            val old = _items
            _items = newList
            notifyDataSetChanged()
            onItemsChanged(old, newList)
            commitCallback?.run()
        }
    }

    open operator fun set(@IntRange(from = 0) position: Int, data: T) {
        require(position in items.indices) { "position: $position. size: ${items.size}" }
        if (mDiffer != null) {
            mDiffer.currentList.toMutableList().also {
                it[position] = data
                mDiffer.submitList(it)
            }
        } else {
            mutableItems[position] = data
            notifyItemChanged(position)
        }
    }

    open fun add(data: T) {
        if (mDiffer != null) {
            mDiffer.currentList.toMutableList().also {
                it.add(data)
                mDiffer.submitList(it)
            }
        } else {
            mutableItems.add(data)
            notifyItemInserted(_items.size - 1)
        }
    }

    open fun add(position: Int, data: T) {
        require(position in 0..items.size) { "position: $position. size: ${items.size}" }
        if (mDiffer != null) {
            mDiffer.currentList.toMutableList().also {
                it.add(position, data)
                mDiffer.submitList(it)
            }
        } else {
            mutableItems.add(position, data)
            notifyItemInserted(position)
        }
    }

    open fun addAll(collection: Collection<T>) {
        if (collection.isEmpty()) return
        if (mDiffer != null) {
            mDiffer.currentList.toMutableList().also {
                it.addAll(collection)
                mDiffer.submitList(it)
            }
        } else {
            val oldSize = _items.size
            if (mutableItems.addAll(collection)) {
                notifyItemRangeInserted(oldSize, collection.size)
            }
        }
    }

    open fun remove(data: T) {
        if (mDiffer != null) {
            mDiffer.currentList.toMutableList().also {
                it.remove(data)
                mDiffer.submitList(it)
            }
        } else {
            val index = _items.indexOf(data)
            if (index == -1) return
            removeAt(index)
        }
    }

    open fun removeAt(@IntRange(from = 0) position: Int) {
        require(position in items.indices) { "position: $position. size: ${items.size}" }
        if (mDiffer != null) {
            mDiffer.currentList.toMutableList().also {
                it.removeAt(position)
                mDiffer.submitList(it)
            }
        } else {
            mutableItems.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    open fun swap(fromPosition: Int, toPosition: Int) {
        if (mDiffer != null) {
            val list = mDiffer.currentList
            if (fromPosition in list.indices && toPosition in list.indices) {
                list.toMutableList().also {
                    Collections.swap(it, fromPosition, toPosition)
                    mDiffer.submitList(it)
                }
            }
        } else {
            if (fromPosition in _items.indices && toPosition in _items.indices) {
                Collections.swap(mutableItems, fromPosition, toPosition)
                notifyItemChanged(fromPosition)
                notifyItemChanged(toPosition)
            }
        }
    }

    open fun move(fromPosition: Int, toPosition: Int) {
        if (mDiffer != null) {
            val list = mDiffer.currentList
            if (fromPosition in list.indices && toPosition in list.indices) {
                list.toMutableList().also {
                    val e = it.removeAt(fromPosition)
                    it.add(toPosition, e)
                    mDiffer.submitList(it)
                }
            }
        } else {
            if (fromPosition in _items.indices && toPosition in _items.indices) {
                val e = mutableItems.removeAt(fromPosition)
                mutableItems.add(toPosition, e)
                notifyItemMoved(fromPosition, toPosition)
            }
        }
    }

    /** 数据变化回调。默认空实现。 */
    open fun onItemsChanged(previous: List<T>, current: List<T>) {}

    // ── 内部：点击事件绑定 ──────────────────────────────────────────────────

    protected open fun bindViewClickListener(viewHolder: VH) {
        mOnItemClickListener?.let {
            viewHolder.itemView.setOnClickListener { v ->
                val pos = viewHolder.bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) onItemClick(v, pos)
            }
        }
        mOnItemLongClickListener?.let {
            viewHolder.itemView.setOnLongClickListener { v ->
                val pos = viewHolder.bindingAdapterPosition
                if (pos == RecyclerView.NO_POSITION) false
                else onItemLongClick(v, pos)
            }
        }
        mOnItemChildClickArray?.let { arr ->
            for (i in 0 until arr.size()) {
                val id = arr.keyAt(i)
                viewHolder.itemView.findViewById<View>(id)?.setOnClickListener { v ->
                    val pos = viewHolder.bindingAdapterPosition
                    if (pos != RecyclerView.NO_POSITION) onItemChildClick(v, pos)
                }
            }
        }
        mOnItemChildLongClickArray?.let { arr ->
            for (i in 0 until arr.size()) {
                val id = arr.keyAt(i)
                viewHolder.itemView.findViewById<View>(id)?.setOnLongClickListener { v ->
                    val pos = viewHolder.bindingAdapterPosition
                    if (pos == RecyclerView.NO_POSITION) false
                    else onItemChildLongClick(v, pos)
                }
            }
        }
    }

    protected open fun onItemClick(v: View, position: Int) {
        mOnItemClickListener?.onClick(this, v, position)
    }

    protected open fun onItemLongClick(v: View, position: Int): Boolean {
        return mOnItemLongClickListener?.onLongClick(this, v, position) ?: false
    }

    protected open fun onItemChildClick(v: View, position: Int) {
        mOnItemChildClickArray?.get(v.id)?.onItemClick(this, v, position)
    }

    protected open fun onItemChildLongClick(v: View, position: Int): Boolean {
        return mOnItemChildLongClickArray?.get(v.id)?.onItemLongClick(this, v, position) ?: false
    }

    // ── Listener 接口 ───────────────────────────────────────────────────────

    fun interface OnItemClickListener<T : Any> {
        fun onClick(adapter: BaseAdapter<T, *>, view: View, position: Int)
    }

    fun interface OnItemLongClickListener<T : Any> {
        fun onLongClick(adapter: BaseAdapter<T, *>, view: View, position: Int): Boolean
    }

    fun interface OnItemChildClickListener<T : Any> {
        fun onItemClick(adapter: BaseAdapter<T, *>, view: View, position: Int)
    }

    fun interface OnItemChildLongClickListener<T : Any> {
        fun onItemLongClick(adapter: BaseAdapter<T, *>, view: View, position: Int): Boolean
    }
}
