package com.empty.android.app.weather.binding

import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.empty.android.app.R
import com.empty.android.app.databinding.FragmentWeatherForecastBinding
import com.empty.android.app.weather.WeatherUiState
import com.empty.android.app.weather.WeatherViewModel
import com.empty.android.app.weather.binding.adapter.ForecastAdapter
import com.empty.android.core.mvi.UiState
import com.empty.android.core.viewbinding.BaseFragment
import com.empty.android.core.viewbinding.viewBinding
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
        when (val s = state) {
            is WeatherUiState.Loading -> {
                binding.progressBar.visibility = View.VISIBLE
                binding.errorView.visibility = View.GONE
                binding.rvForecast.visibility = View.GONE
            }
            is WeatherUiState.Error -> {
                binding.progressBar.visibility = View.GONE
                binding.rvForecast.visibility = View.GONE
                binding.errorView.visibility = View.VISIBLE
                binding.tvError.text = s.message
            }
            is WeatherUiState.Success -> {
                binding.progressBar.visibility = View.GONE
                binding.errorView.visibility = View.GONE
                binding.rvForecast.visibility = View.VISIBLE
                adapter.submitList(s.data.forecast)
            }
        }
    }
}
