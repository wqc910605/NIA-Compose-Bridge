package com.nia.compose.bridge.weather.binding

import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.nia.compose.bridge.R
import com.nia.compose.bridge.databinding.FragmentWeatherForecastBinding
import com.nia.compose.bridge.weather.ForecastDay
import com.nia.compose.bridge.weather.WeatherUiState
import com.nia.compose.bridge.weather.WeatherViewModel
import com.nia.compose.bridge.weather.binding.adapter.ForecastAdapter
import com.nia.compose.bridge.common.UiState
import com.nia.compose.bridge.core.viewbinding.BaseFragment
import com.nia.compose.bridge.core.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint

/**
 * 未来预报 Tab —— ViewBinding 实现。
 *
 * 使用独立的 [ForecastAdapter]（[BaseAdapter] + DiffUtil），替代内联 [ListAdapter]。
 * 点击预报日 → 通过 ViewModel 发出 `NavigateToDetail` Effect，由 Activity 处理跳转。
 *
 * ## 与 Compose 实现对照
 *
 * | ViewBinding | Compose |
 * |-------------|---------|
 * | RecyclerView + BaseAdapter + DiffUtil | LazyColumn + key() |
 * | `adapter.submitList(state.data.forecast)` | LazyColumn 自动比对 key |
 * | 点击 → ViewModel.launchNavigateToDetail() | 点击 → navController.navigateToWeatherDetail() |
 */
@AndroidEntryPoint
class WeatherForecastFragment : BaseFragment(R.layout.fragment_weather_forecast) {

    override val binding by viewBinding(FragmentWeatherForecastBinding::bind)
    override val viewModel by activityViewModels<WeatherViewModel>()

    private val adapter = ForecastAdapter { day ->
        viewModel.launchNavigateToDetail(day.date)
    }

    override fun initView() {
        binding.rvForecast.adapter = adapter
        binding.rvForecast.layoutManager = LinearLayoutManager(requireContext())
        binding.btnRetry.setOnClickListener {
            viewModel.loadWeather()
        }
    }

    override fun render(state: UiState) {
        binding.bind(state as WeatherUiState) { forecast ->
            adapter.submitList(forecast)
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// Binding 函数 —— 集中式 View 数据绑定
// ═══════════════════════════════════════════════════════════════════════════════

private fun FragmentWeatherForecastBinding.bind(
    state: WeatherUiState,
    submitForecast: (List<ForecastDay>) -> Unit,
) {
    when (state) {
        is WeatherUiState.Loading -> {
            progressBar.visibility = View.VISIBLE
            errorView.visibility = View.GONE
            rvForecast.visibility = View.GONE
        }
        is WeatherUiState.Error -> {
            progressBar.visibility = View.GONE
            rvForecast.visibility = View.GONE
            errorView.visibility = View.VISIBLE
            tvError.text = state.message
        }
        is WeatherUiState.Success -> {
            progressBar.visibility = View.GONE
            errorView.visibility = View.GONE
            rvForecast.visibility = View.VISIBLE
            submitForecast(state.data.forecast)
        }
    }
}
