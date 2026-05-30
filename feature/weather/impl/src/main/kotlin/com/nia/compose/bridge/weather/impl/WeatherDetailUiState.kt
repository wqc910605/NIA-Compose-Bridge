package com.nia.compose.bridge.weather.impl

import com.nia.compose.bridge.domain.CurrentWeatherDomain
import com.nia.compose.bridge.domain.ForecastDayDomain
import com.nia.compose.bridge.common.UiEffect
import com.nia.compose.bridge.common.UiState

// ═══════════════════════════════════════════════════════════════════════════════
// WeatherDetailUiState —— 详情页宏观状态
// ═══════════════════════════════════════════════════════════════════════════════

/** 天气详情页的全部状态。ViewBinding 和 Compose 共享。 */
sealed class WeatherDetailUiState : UiState {

    /** 加载中。 */
    data object Loading : WeatherDetailUiState()

    /** 加载失败。 */
    data class Error(val message: String) : WeatherDetailUiState()

    /** 加载成功。 */
    data class Success(
        val cityName: String,
        val date: String,
        val currentWeather: CurrentWeatherDomain,
        val forecast: List<ForecastDayDomain>,
    ) : WeatherDetailUiState()
}

// ═══════════════════════════════════════════════════════════════════════════════
// WeatherDetailEffect —— 一次性副作用
// ═══════════════════════════════════════════════════════════════════════════════

/** 天气详情页的一次性副作用。 */
sealed class WeatherDetailEffect : UiEffect {

    /** 弹出 Toast。 */
    data class ShowToast(val message: String) : WeatherDetailEffect()
}
