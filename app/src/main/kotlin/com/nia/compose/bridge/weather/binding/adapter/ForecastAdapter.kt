package com.nia.compose.bridge.weather.binding.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import com.nia.compose.bridge.databinding.ItemForecastDayBinding
import com.nia.compose.bridge.weather.ForecastDay
import com.nia.compose.bridge.viewbinding.adapter.BaseAdapter
import com.nia.compose.bridge.viewbinding.adapter.VBViewHolder

/**
 * 天气预报 Adapter。
 *
 * 使用项目 [BaseAdapter] + DiffUtil 模式，替代 AndroidX [ListAdapter]。
 * 与项目现有 `BaseAdapter<T, VH>` 体系一致，支持 DiffUtil 异步差异计算和点击监听。
 *
 * ## 对照
 *
 * | 特性 | 旧版 (ListAdapter) | 新版 (BaseAdapter) |
 * |------|--------------------|---------------------|
 * | 基类 | `ListAdapter<ForecastDay, VH>(DiffCallback)` | `BaseAdapter<ForecastDay, VH>(diffCallback)` |
 * | DiffUtil | 内置 | 通过构造函数传入 |
 * | 点击监听 | 需手动设置 | `setOnItemClickListener {}` 一行 |
 * | 数据提交 | `submitList(list)` | `submitList(list)` 完全兼容 |
 */
class ForecastAdapter(
    private val onItemClick: ((ForecastDay) -> Unit)? = null,
) : BaseAdapter<ForecastDay, ForecastAdapter.VH>(
    diffCallback = DiffCallback,
) {

    init {
        onItemClick?.let { listener ->
            setOnItemClickListener { _, _, position ->
                listener(getItem(position))
            }
        }
    }

    override fun onCreateViewHolderVH(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemForecastDayBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int, item: ForecastDay?) {
        holder.bind(item!!)
    }

    class VH(override val binding: ItemForecastDayBinding) : VBViewHolder<ItemForecastDayBinding>(binding) {
        fun bind(item: ForecastDay) {
            binding.tvDate.text = item.date
            binding.tvForecastCondition.text = item.condition.label
            binding.tvForecastIcon.text = item.condition.icon
            binding.tvForecastHigh.text = "${item.highTemp}°"
            binding.tvForecastLow.text = "${item.lowTemp}°"
            binding.tvRainProb.text = "🌧 ${item.rainProbability}%"
        }
    }

    object DiffCallback : DiffUtil.ItemCallback<ForecastDay>() {
        override fun areItemsTheSame(old: ForecastDay, new: ForecastDay): Boolean =
            old.date == new.date

        override fun areContentsTheSame(old: ForecastDay, new: ForecastDay): Boolean =
            old == new
    }
}
