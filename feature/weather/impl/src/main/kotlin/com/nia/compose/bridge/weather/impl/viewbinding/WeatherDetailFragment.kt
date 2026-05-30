package com.nia.compose.bridge.weather.impl.viewbinding

import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.nia.compose.bridge.domain.CurrentWeatherDomain
import com.nia.compose.bridge.domain.ForecastDayDomain
import com.nia.compose.bridge.common.UiEffect
import com.nia.compose.bridge.common.UiState
import com.nia.compose.bridge.core.viewbinding.BaseFragment
import com.nia.compose.bridge.core.viewbinding.diffUpdate
import com.nia.compose.bridge.core.viewbinding.viewBinding
import com.nia.compose.bridge.feature.weather.impl.R
import com.nia.compose.bridge.weather.impl.WeatherDetailEffect
import com.nia.compose.bridge.weather.impl.WeatherDetailUiState
import com.nia.compose.bridge.weather.impl.WeatherDetailViewModel
import com.nia.compose.bridge.weather.impl.adapter.ForecastDetailAdapter
import com.nia.compose.bridge.feature.weather.impl.databinding.ActivityWeatherDetailBinding
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
        binding.bind(state as WeatherDetailUiState) { forecast ->
            adapter.submitList(forecast)
        }
    }

    override fun handleEffect(effect: UiEffect) {
        when (effect) {
            is WeatherDetailEffect.ShowToast ->
                Toast.makeText(requireContext(), effect.message, Toast.LENGTH_SHORT).show()
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// Binding 函数 —— 集中式 View 数据绑定
//
// 所有字段与 View 的映射集中在一处，等同于 XML DataBinding 表达式的能力，
// 但保留 ViewBinding 的编译速度优势。内部使用 diffUpdate 做增量更新。
// ═══════════════════════════════════════════════════════════════════════════════

private fun ActivityWeatherDetailBinding.bind(
    state: WeatherDetailUiState,
    submitForecast: (List<ForecastDayDomain>) -> Unit,
) {
    when (state) {
        is WeatherDetailUiState.Loading -> {
            progressBar.visibility = View.VISIBLE
            errorView.visibility = View.GONE
            contentView.visibility = View.GONE
        }
        is WeatherDetailUiState.Error -> {
            progressBar.visibility = View.GONE
            contentView.visibility = View.GONE
            errorView.visibility = View.VISIBLE
            tvError.diffUpdate(state.message) { text = it }
        }
        is WeatherDetailUiState.Success -> {
            progressBar.visibility = View.GONE
            errorView.visibility = View.GONE
            contentView.visibility = View.VISIBLE
            bindCurrentWeather(state.currentWeather)
            submitForecast(state.forecast)
        }
    }
}

private fun ActivityWeatherDetailBinding.bindCurrentWeather(w: CurrentWeatherDomain) {
    // 天气概要
    tvWeatherIcon.diffUpdate(w.condition.icon) { text = it }
    tvTemperature.diffUpdate(w.temperature) { text = "${it}°" }
    tvCondition.diffUpdate(w.condition.label) { text = it }
    tvHighLow.diffUpdate("↑${w.highTemp}° / ↓${w.lowTemp}°") { text = it }

    // 详细指标
    tvFeelsLike.diffUpdate(w.feelsLike) { text = "${it}°" }
    tvHumidity.diffUpdate(w.humidity) { text = "$it%" }
    tvWind.diffUpdate(w.windSpeed) { text = "${it}km/h ${w.windDirection}" }
    tvVisibility.diffUpdate(w.visibility) { text = "${it}km" }
    tvUvIndex.diffUpdate(w.uvIndex) { text = "$it" }
    tvPressure.diffUpdate(w.pressure) { text = "${it}hPa" }
    tvSunrise.diffUpdate(w.sunrise) { text = "日出 ${it}" }
    tvSunset.diffUpdate(w.sunset) { text = "日落 ${it}" }

    // 空气质量
    tvAirQuality.diffUpdate(w.airQuality) {
        text = "AQI $it · ${w.airQualityLabel}"
    }
}

