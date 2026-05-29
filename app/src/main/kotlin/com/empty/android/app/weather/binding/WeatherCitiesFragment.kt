package com.empty.android.app.weather.binding

import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.empty.android.app.R
import com.empty.android.app.databinding.FragmentWeatherCitiesBinding
import com.empty.android.app.weather.CityInfo
import com.empty.android.app.weather.WeatherUiState
import com.empty.android.app.weather.WeatherViewModel
import com.empty.android.app.weather.binding.adapter.CityAdapter
import com.empty.android.core.mvi.UiState
import com.empty.android.core.viewbinding.BaseFragment
import com.empty.android.core.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint

/**
 * 城市管理 Tab —— ViewBinding 实现。
 *
 * 使用独立的 [CityAdapter]（[BaseAdapter] + DiffUtil），替代内联 [ListAdapter]。
 * 城市增删通过 ViewModel 方法驱动，列表刷新由 [render] 中的 `submitList` 自动完成。
 * 点击城市 → 通过 ViewModel 发出 `NavigateToDetail` Effect 跳转到详情页。
 *
 * ## 与 Compose 实现对照
 *
 * | ViewBinding | Compose |
 * |-------------|---------|
 * | EditText + Button + `EditorInfo.IME_ACTION_DONE` | `TextField` + `IconButton` |
 * | RecyclerView + BaseAdapter + DiffUtil | LazyColumn + key() |
 * | 点击城市 → ViewModel.launchNavigateToDetail() | 点击 → navController.navigateToWeatherDetail() |
 */
@AndroidEntryPoint
class WeatherCitiesFragment : BaseFragment(R.layout.fragment_weather_cities) {

    override val binding by viewBinding(FragmentWeatherCitiesBinding::bind)
    override val viewModel by activityViewModels<WeatherViewModel>()

    private val adapter = CityAdapter(
        onSelectCity = { city ->
            (viewModel as WeatherViewModel).launchNavigateToDetail(cityName = city.name)
        },
        onDeleteCity = { city ->
            viewModel.removeCity(city.name)
        },
    )

    override fun initView() {
        binding.rvCities.adapter = adapter
        binding.rvCities.layoutManager = LinearLayoutManager(requireContext())
        binding.btnRetry.setOnClickListener {
            viewModel.loadWeather()
        }

        // 添加城市：键盘 Done 或点击按钮
        binding.etCityName.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                addCity()
                true
            } else false
        }
        binding.btnAddCity.setOnClickListener { addCity() }
    }

    private fun addCity() {
        val name = binding.etCityName.text.toString().trim()
        if (name.isNotEmpty()) {
            (viewModel as WeatherViewModel).addCity(name)
            binding.etCityName.text?.clear()
        }
    }

    override fun render(state: UiState) {
        binding.bind(state as WeatherUiState) { cities ->
            adapter.submitList(cities)
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// Binding 函数 —— 集中式 View 数据绑定
// ═══════════════════════════════════════════════════════════════════════════════

private fun FragmentWeatherCitiesBinding.bind(
    state: WeatherUiState,
    submitCities: (List<CityInfo>) -> Unit,
) {
    when (state) {
        is WeatherUiState.Loading -> {
            progressBar.visibility = View.VISIBLE
            errorView.visibility = View.GONE
            rvCities.visibility = View.GONE
            addCityLayout.visibility = View.GONE
        }
        is WeatherUiState.Error -> {
            progressBar.visibility = View.GONE
            rvCities.visibility = View.GONE
            addCityLayout.visibility = View.GONE
            errorView.visibility = View.VISIBLE
            tvError.text = state.message
        }
        is WeatherUiState.Success -> {
            progressBar.visibility = View.GONE
            errorView.visibility = View.GONE
            rvCities.visibility = View.VISIBLE
            addCityLayout.visibility = View.VISIBLE
            submitCities(state.data.cities)
        }
    }
}
