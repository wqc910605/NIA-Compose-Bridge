package com.empty.android.feature.weather.impl.viewbinding

import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.empty.android.core.mvi.UiEffect
import com.empty.android.core.mvi.UiState
import com.empty.android.core.viewbinding.BaseFragment
import com.empty.android.core.viewbinding.diffUpdate
import com.empty.android.core.viewbinding.viewBinding
import com.empty.android.feature.weather.impl.R
import com.empty.android.feature.weather.impl.WeatherDetailEffect
import com.empty.android.feature.weather.impl.WeatherDetailUiState
import com.empty.android.feature.weather.impl.WeatherDetailViewModel
import com.empty.android.feature.weather.impl.adapter.ForecastDetailAdapter
import com.empty.android.feature.weather.impl.databinding.ActivityWeatherDetailBinding
import dagger.hilt.android.AndroidEntryPoint

/**
 * ViewBinding 版天气详情页 —— Fragment destination。
 *
 * 由 Navigation Component 管理生命周期，通过 [arguments] Bundle 接收参数。
 *
 * ## Activity → Fragment 改造要点
 *
 * | 维度 | WeatherDetailBindingActivity (旧) | WeatherDetailFragment (新) |
 * |------|-----------------------------------|----------------------------|
 * | 基类 | [BaseActivity] | [BaseFragment] |
 * | ViewModel 作用域 | `viewModels()` → Activity 级 | `viewModels()` → Fragment 级 |
 * | 参数来源 | `intent.getStringExtra()` | `arguments?.getString()` |
 * | 返回导航 | `finish()` | `findNavController().popBackStack()` |
 * | 布局绑定 | `ActivityWeatherDetailBinding::inflate` | `ActivityWeatherDetailBinding::bind` |
 *
 * ## 导航方式对照（ViewBinding ↔ Compose）
 *
 * | | ViewBinding (Navigation Component) | Compose |
 * |--|-------------------------------------|---------|
 * | 路由定义 | `weather_nav_graph.xml` 中 `<fragment>` + `<argument>` | `@Serializable WeatherDetailRoute` data class |
 * | 参数获取 | `arguments?.getString("cityName")` | `navBackStackEntry.toRoute<WeatherDetailRoute>()` |
 * | 返回 | `findNavController().popBackStack()` | `onBack()` 回调 → `navController.popBackStack()` |
 * | 类型安全 | 无（字符串 key，运行时取值） | 有（编译期 `@Serializable`） |
 */
@AndroidEntryPoint
class WeatherDetailFragment : BaseFragment(R.layout.activity_weather_detail) {

    // ── ViewBinding ──────────────────────────────────────────────────────────

    override val binding by viewBinding(ActivityWeatherDetailBinding::bind)

    // ── ViewModel (Fragment-scoped) ──────────────────────────────────────────

    override val viewModel by viewModels<WeatherDetailViewModel>()

    // ── Adapter ──────────────────────────────────────────────────────────────

    private val adapter = ForecastDetailAdapter()

    // ── Lifecycle ────────────────────────────────────────────────────────────

    override fun initView() {
        // 返回按钮 → Navigation Component 出栈
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        // RecyclerView
        binding.rvForecast.adapter = adapter
        binding.rvForecast.layoutManager = LinearLayoutManager(requireContext())

        // 重试按钮
        binding.btnRetry.setOnClickListener { loadData() }

        loadData()
    }

    private fun loadData() {
        val cityName = arguments?.getString("cityName") ?: "深圳"
        val date = arguments?.getString("date") ?: ""
        viewModel.loadDetail(cityName, date)
    }

    // ── Render / Effect ─────────────────────────────────────────────────────

    override fun render(state: UiState) {
        when (val s = state) {
            is WeatherDetailUiState.Loading -> {
                binding.progressBar.visibility = View.VISIBLE
                binding.errorView.visibility = View.GONE
                binding.contentView.visibility = View.GONE
            }
            is WeatherDetailUiState.Error -> {
                binding.progressBar.visibility = View.GONE
                binding.contentView.visibility = View.GONE
                binding.errorView.visibility = View.VISIBLE
                binding.tvError.diffUpdate(s.message) { text = it }
            }
            is WeatherDetailUiState.Success -> {
                binding.progressBar.visibility = View.GONE
                binding.errorView.visibility = View.GONE
                binding.contentView.visibility = View.VISIBLE

                val w = s.currentWeather
                // 天气概要
                binding.tvWeatherIcon.diffUpdate(w.condition.icon) { text = it }
                binding.tvTemperature.diffUpdate(w.temperature) { text = "${it}°" }
                binding.tvCondition.diffUpdate(w.condition.label) { text = it }
                binding.tvHighLow.diffUpdate("↑${w.highTemp}° / ↓${w.lowTemp}°") { text = it }

                // 详细指标
                binding.tvFeelsLike.diffUpdate(w.feelsLike) { text = "${it}°" }
                binding.tvHumidity.diffUpdate(w.humidity) { text = "$it%" }
                binding.tvWind.diffUpdate(w.windSpeed) { text = "${it}km/h ${w.windDirection}" }
                binding.tvVisibility.diffUpdate(w.visibility) { text = "${it}km" }
                binding.tvUvIndex.diffUpdate(w.uvIndex) { text = "$it" }
                binding.tvPressure.diffUpdate(w.pressure) { text = "${it}hPa" }
                binding.tvSunrise.diffUpdate(w.sunrise) { text = "日出 ${it}" }
                binding.tvSunset.diffUpdate(w.sunset) { text = "日落 ${it}" }
                binding.tvAirQuality.diffUpdate(w.airQuality) {
                    text = "AQI $it · ${w.airQualityLabel}"
                }

                // 预报列表
                adapter.submitList(s.forecast)
            }
        }
    }

    override fun handleEffect(effect: UiEffect) {
        when (effect) {
            is WeatherDetailEffect.ShowToast ->
                Toast.makeText(requireContext(), effect.message, Toast.LENGTH_SHORT).show()
        }
    }
}

