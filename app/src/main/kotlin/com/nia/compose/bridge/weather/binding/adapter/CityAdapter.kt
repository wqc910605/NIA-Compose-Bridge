package com.nia.compose.bridge.weather.binding.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import com.nia.compose.bridge.databinding.ItemCityBinding
import com.nia.compose.bridge.weather.CityInfo
import com.nia.compose.bridge.viewbinding.adapter.BaseAdapter
import com.nia.compose.bridge.viewbinding.adapter.VBViewHolder

/**
 * 城市列表 Adapter。
 *
 * 使用项目 [BaseAdapter] + DiffUtil 模式，支持 item 点击和子 View 删除按钮点击。
 * 点击机制通过 [BaseAdapter.OnItemClickListener] 和 [BaseAdapter.addOnItemChildClickListener] 实现。
 *
 * ## 对照
 *
 * | 特性 | 旧版 (ListAdapter) | 新版 (BaseAdapter) |
 * |------|--------------------|---------------------|
 * | 基类 | `ListAdapter<CityInfo, VH>(DiffCallback)` | `BaseAdapter<CityInfo, VH>(diffCallback)` |
 * | item 点击 | `binding.root.setOnClickListener {}` | `setOnItemClickListener {}` |
 * | 子 View 点击 | `binding.btnDeleteCity.setOnClickListener {}` | `addOnItemChildClickListener(id, listener)` |
 * | 数据提交 | `submitList(list)` | `submitList(list)` |
 */
class CityAdapter(
    onSelectCity: (CityInfo) -> Unit,
    onDeleteCity: (CityInfo) -> Unit,
) : BaseAdapter<CityInfo, CityAdapter.VH>(
    diffCallback = DiffCallback,
) {

    init {
        // item 点击 → 选择城市
        setOnItemClickListener { _, _, position ->
            onSelectCity(getItem(position))
        }

        // 删除按钮 → 子 View 点击
        addOnItemChildClickListener(com.nia.compose.bridge.R.id.btnDeleteCity) { _, _, position ->
            onDeleteCity(getItem(position))
        }
    }

    override fun onCreateViewHolderVH(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemCityBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int, item: CityInfo?) {
        holder.bind(item!!)
    }

    class VH(override val binding: ItemCityBinding) : VBViewHolder<ItemCityBinding>(binding) {
        fun bind(item: CityInfo) {
            binding.tvCityName.text = item.name
            binding.tvCityCountry.text = item.country
            binding.tvCurrentBadge.visibility = if (item.isCurrent) View.VISIBLE else View.GONE
        }
    }

    object DiffCallback : DiffUtil.ItemCallback<CityInfo>() {
        override fun areItemsTheSame(old: CityInfo, new: CityInfo): Boolean =
            old.id == new.id

        override fun areContentsTheSame(old: CityInfo, new: CityInfo): Boolean =
            old == new
    }
}
