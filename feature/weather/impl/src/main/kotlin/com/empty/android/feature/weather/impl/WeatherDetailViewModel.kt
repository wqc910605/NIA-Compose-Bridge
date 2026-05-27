package com.empty.android.feature.weather.impl

import com.empty.android.core.domain.GetWeatherUseCase
import com.empty.android.core.mvi.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import javax.inject.Inject

/**
 * 天气详情页 ViewModel。
 *
 * ## 设计说明 —— ViewBinding / Compose 共享
 *
 * 同一个 ViewModel 同时驱动 ViewBinding 版和 Compose 版详情页：
 * - 通过构造函数注入 [GetWeatherUseCase] 获取天气数据
 * - ViewBinding 侧通过 [BaseActivity.initObservers] 订阅 [uiState]/[uiEffect]
 * - Compose 侧通过 `collectAsStateWithLifecycle()` + `LaunchedEffect`
 *
 * ## 依赖注入
 * [GetWeatherUseCase] 由 Hilt 自动注入，遵循项目 UseCase 模式。
 */
@HiltViewModel
class WeatherDetailViewModel @Inject constructor(
    private val getWeather: GetWeatherUseCase,
) : BaseViewModel<WeatherDetailUiState>(WeatherDetailUiState.Loading) {

    /**
     * 加载详情页天气数据。
     *
     * @param cityName 城市名
     * @param date 预报日期，为空则显示综合数据
     */
    fun loadDetail(cityName: String, date: String = "") {
        setState(WeatherDetailUiState.Loading)
        launch {
            delay(1_000)
            val domainData = getWeather(
                cityName = cityName,
                cities = emptyList(), // 详情页不关注城市列表
            )
            // 如果指定了 date，过滤当天预报
            val filteredForecast = if (date.isNotBlank()) {
                domainData.forecast.filter { it.date.contains(date.take(3)) }
                    .ifEmpty { domainData.forecast }
            } else {
                domainData.forecast
            }
            setState(
                WeatherDetailUiState.Success(
                    cityName = cityName,
                    date = date,
                    currentWeather = domainData.currentWeather,
                    forecast = filteredForecast,
                )
            )
            if (date.isBlank()) {
                emitEffect(WeatherDetailEffect.ShowToast("已加载 $cityName 天气详情"))
            }
        }
    }
}
