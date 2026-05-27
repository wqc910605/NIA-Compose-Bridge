package com.empty.android.core.viewbinding.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

/**
 * ViewBinding 感知的 ViewHolder。
 *
 * 直接持有 [ViewBinding] 实例，通过 [binding] 访问绑定对象。
 * 是 [BaseMultiAdapter] 多条目体系的标准 ViewHolder。
 *
 * @param VB ViewBinding 类型
 * @param binding 布局绑定实例
 */
open class VBViewHolder<VB : ViewBinding>(
    open val binding: VB,
) : RecyclerView.ViewHolder(binding.root) {

    /** 便捷访问 root view。 */
    val root: View get() = binding.root
}
