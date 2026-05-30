package com.nia.compose.bridge.weather.impl.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import com.nia.compose.bridge.domain.ForecastDayDomain
import com.nia.compose.bridge.viewbinding.adapter.BaseAdapter
import com.nia.compose.bridge.viewbinding.adapter.VBViewHolder
import com.nia.compose.bridge.feature.weather.impl.databinding.ItemWeatherDetailDayBinding
import com.nia.compose.bridge.viewbinding.viewBinding

// ═══════════════════════════════════════════════════════════════════════════════
// ForecastDetailAdapter — BaseAdapter + DiffUtil
// ═══════════════════════════════════════════════════════════════════════════════
 class ForecastDetailAdapter : BaseAdapter<ForecastDayDomain, ForecastDetailAdapter.VH>(
    diffCallback = DiffCallback,
) {

    override fun onCreateViewHolderVH(parent: ViewGroup, viewType: Int): VH {
        return VH(parent.viewBinding(ItemWeatherDetailDayBinding::inflate))
    }

    override fun onBindViewHolder(holder: VH, position: Int, item: ForecastDayDomain?) {
        holder.bind(item!!)
    }

    class VH(override val binding: ItemWeatherDetailDayBinding) : VBViewHolder<ItemWeatherDetailDayBinding>(binding) {
        fun bind(day: ForecastDayDomain) {
            binding.tvDate.text = day.date
            binding.tvIcon.text = day.condition.icon
            binding.tvForecastCondition.text = day.condition.label
            binding.tvForecastHigh.text = "${day.highTemp}°"
            binding.tvForecastLow.text = "${day.lowTemp}°"
            binding.tvRainProb.text = "🌧 ${day.rainProbability}%"
        }
    }

    object DiffCallback : DiffUtil.ItemCallback<ForecastDayDomain>() {
        override fun areItemsTheSame(old: ForecastDayDomain, new: ForecastDayDomain) =
            old.date == new.date

        override fun areContentsTheSame(old: ForecastDayDomain, new: ForecastDayDomain) =
            old == new
    }
}
