package com.empty.android.app.weather.binding

import android.view.View
import androidx.fragment.app.activityViewModels
import com.empty.android.app.R
import com.empty.android.app.databinding.FragmentWeatherTodayBinding
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
        when (val s = state) {
            is WeatherUiState.Loading -> {
                binding.progressBar.visibility = View.VISIBLE
                binding.errorView.visibility = View.GONE
                binding.contentView.visibility = View.GONE
            }
            is WeatherUiState.Error -> {
                binding.progressBar.visibility = View.GONE
                binding.contentView.visibility = View.GONE
                binding.errorView.visibility = View.VISIBLE
                binding.tvError.diffUpdate(s.message) { text = it }
            }
            is WeatherUiState.Success -> {
                binding.progressBar.visibility = View.GONE
                binding.errorView.visibility = View.GONE
                binding.contentView.visibility = View.VISIBLE

                val w = s.data.currentWeather

                // 城市名 + 天气图标
                binding.tvCityName.diffUpdate(w.cityName) { text = it }
                binding.tvWeatherIcon.diffUpdate(w.condition.icon) { text = it }

                // 温度
                binding.tvTemperature.diffUpdate(w.temperature) { text = "${it}°" }
                binding.tvCondition.diffUpdate(w.condition.label) { text = it }
                binding.tvHighLow.diffUpdate("↑${w.highTemp}° / ↓${w.lowTemp}°") { text = it }

                // 详细指标
                binding.tvFeelsLike.diffUpdate(w.feelsLike) { text = "${it}°" }
                binding.tvHumidity.diffUpdate(w.humidity) { text = "$it%" }
                binding.tvVisibility.diffUpdate(w.visibility) { text = "${it}km" }
                binding.tvUvIndex.diffUpdate(w.uvIndex) { text = "$it" }
                binding.tvPressure.diffUpdate(w.pressure) { text = "${it}hPa" }
                binding.tvWind.diffUpdate(w.windSpeed) { text = "${it}km/h ${w.windDirection}" }
                binding.tvSunrise.diffUpdate(w.sunrise) { text = it }
                binding.tvSunset.diffUpdate(w.sunset) { text = it }

                // 空气质量
                binding.tvAirQuality.diffUpdate(w.airQualityLabel) {
                    text = "AQI ${w.airQuality} · $it"
                }
            }
        }
    }
}
