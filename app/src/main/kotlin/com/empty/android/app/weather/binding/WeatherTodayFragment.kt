package com.empty.android.app.weather.binding

import android.view.View
import androidx.fragment.app.activityViewModels
import com.empty.android.app.R
import com.empty.android.app.databinding.FragmentWeatherTodayBinding
import com.empty.android.app.weather.CurrentWeather
import com.empty.android.app.weather.WeatherUiState
import com.empty.android.app.weather.WeatherViewModel
import com.empty.android.core.mvi.UiState
import com.empty.android.core.viewbinding.BaseFragment
import com.empty.android.core.viewbinding.diffUpdate
import com.empty.android.core.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint

/**
 * 今日天气 Tab —— ViewBinding 实现。
 *
 * 使用 [BaseFragment] 基类 + `activityViewModels()` 共享 Activity 级别的 ViewModel。
 * 所有 UI 更新通过 [render] 中用 [diffUpdate] 驱动，避免重复渲染。
 *
 * ## 与 Compose 实现对照
 *
 * | ViewBinding | Compose |
 * |-------------|---------|
 * | [BaseFragment.render] + `when(state)` | `when(state)` in Composable body |
 * | `binding.tvTemperature.diffUpdate(temp) { text = "$it°C" }` | `Text("${temp}°C")` —— Compose 自带 diff |
 * | `binding.progressBar.visibility = View.VISIBLE` | `if (state is Loading) CircularProgressIndicator()` |
 */
@AndroidEntryPoint
class WeatherTodayFragment : BaseFragment(R.layout.fragment_weather_today) {

    override val binding by viewBinding(FragmentWeatherTodayBinding::bind)
    override val viewModel by activityViewModels<WeatherViewModel>()

    override fun initView() {
        binding.btnRetry.setOnClickListener {
            viewModel.loadWeather()
        }
    }

    override fun render(state: UiState) {
        binding.bind(state as WeatherUiState)
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// Binding 函数 —— 集中式 View 数据绑定
//
// 所有字段与 View 的映射集中在一处，等同于 XML DataBinding 表达式的能力，
// 但保留 ViewBinding 的编译速度优势。内部使用 diffUpdate 做增量更新，
// 同一值不会重复 setText。
// ═══════════════════════════════════════════════════════════════════════════════

private fun FragmentWeatherTodayBinding.bind(state: WeatherUiState) {
    when (state) {
        is WeatherUiState.Loading -> {
            progressBar.visibility = View.VISIBLE
            errorView.visibility = View.GONE
            contentView.visibility = View.GONE
        }
        is WeatherUiState.Error -> {
            progressBar.visibility = View.GONE
            contentView.visibility = View.GONE
            errorView.visibility = View.VISIBLE
            tvError.diffUpdate(state.message) { text = it }
        }
        is WeatherUiState.Success -> {
            progressBar.visibility = View.GONE
            errorView.visibility = View.GONE
            contentView.visibility = View.VISIBLE
            bindCurrentWeather(state.data.currentWeather)
        }
    }
}

private fun FragmentWeatherTodayBinding.bindCurrentWeather(w: CurrentWeather) {
    // 城市名 + 天气图标
    tvCityName.diffUpdate(w.cityName) { text = it }
    tvWeatherIcon.diffUpdate(w.condition.icon) { text = it }

    // 温度
    tvTemperature.diffUpdate(w.temperature) { text = "${it}°" }
    tvCondition.diffUpdate(w.condition.label) { text = it }
    tvHighLow.diffUpdate("↑${w.highTemp}° / ↓${w.lowTemp}°") { text = it }

    // 详细指标
    tvFeelsLike.diffUpdate(w.feelsLike) { text = "${it}°" }
    tvHumidity.diffUpdate(w.humidity) { text = "$it%" }
    tvVisibility.diffUpdate(w.visibility) { text = "${it}km" }
    tvUvIndex.diffUpdate(w.uvIndex) { text = "$it" }
    tvPressure.diffUpdate(w.pressure) { text = "${it}hPa" }
    tvWind.diffUpdate(w.windSpeed) { text = "${it}km/h ${w.windDirection}" }
    tvSunrise.diffUpdate(w.sunrise) { text = it }
    tvSunset.diffUpdate(w.sunset) { text = it }

    // 空气质量
    tvAirQuality.diffUpdate(w.airQualityLabel) {
        text = "AQI ${w.airQuality} · $it"
    }
}
