package com.empty.android.app.weather

import com.empty.android.core.mvi.UiEffect
import com.empty.android.core.mvi.UiState

// ═══════════════════════════════════════════════════════════════════════════════
// 天气数据模型 —— ViewBinding / Compose 共享
// ═══════════════════════════════════════════════════════════════════════════════

/** 天气状况枚举。 */
enum class WeatherCondition(val label: String, val icon: String) {
    SUNNY("晴", "☀️"),
    PARTLY_CLOUDY("多云", "⛅"),
    CLOUDY("阴", "☁️"),
    RAIN("雨", "🌧️"),
    HEAVY_RAIN("大雨", "⛈️"),
    SNOW("雪", "❄️"),
    THUNDER("雷阵雨", "⚡"),
    FOG("雾", "🌫️"),
}

/** 城市信息。 */
data class CityInfo(
    val id: String,
    val name: String,
    val country: String = "中国",
    val isCurrent: Boolean = false,
)

/** 当前天气详细数据。 */
data class CurrentWeather(
    val cityName: String,
    val temperature: Int,               // 摄氏度
    val feelsLike: Int,                 // 体感温度
    val condition: WeatherCondition,
    val humidity: Int,                  // 湿度 %
    val windSpeed: Float,               // 风速 km/h
    val windDirection: String,          // 风向
    val visibility: Float,              // 能见度 km
    val uvIndex: Int,                   // 紫外线指数
    val pressure: Int,                  // 气压 hPa
    val highTemp: Int,                  // 今日最高
    val lowTemp: Int,                   // 今日最低
    val sunrise: String,                // 日出时间 "06:15"
    val sunset: String,                 // 日落时间 "18:45"
    val airQuality: Int,                // AQI
    val airQualityLabel: String,        // "优"
)

/** 单日预报数据。 */
data class ForecastDay(
    val date: String,                   // "5月28日 周四"
    val condition: WeatherCondition,
    val highTemp: Int,
    val lowTemp: Int,
    val humidity: Int,
    val windSpeed: Float,
    val rainProbability: Int,           // 降雨概率 %
    val sunrise: String,
    val sunset: String,
)

// ═══════════════════════════════════════════════════════════════════════════════
// 页面数据容器 —— Data Class 组合模式
// ═══════════════════════════════════════════════════════════════════════════════

/**
 * 天气页面全部数据。
 *
 * 三个 Tab 的子模块横向组合在同一个 Data Class 中，
 * 再由 Sealed Class 的 [WeatherUiState.Success] 分支包裹。
 * ViewBinding 和 Compose 共享同一个数据容器，保证两边渲染的是相同数据。
 */
data class WeatherData(
    val currentWeather: CurrentWeather,
    val forecast: List<ForecastDay> = emptyList(),
    val cities: List<CityInfo> = emptyList(),
    val selectedTab: WeatherTab = WeatherTab.Today,
)

// ═══════════════════════════════════════════════════════════════════════════════
// UiState —— 页面宏观状态（Sealed Class，互斥）
// ═══════════════════════════════════════════════════════════════════════════════

/** 天气页面的全部状态。 */
sealed class WeatherUiState : UiState {

    /** 加载中。 */
    data object Loading : WeatherUiState()

    /** 加载失败。 */
    data class Error(val message: String) : WeatherUiState()

    /** 加载成功。所有 Tab 数据由 [WeatherData] 承载。 */
    data class Success(val data: WeatherData) : WeatherUiState()
}

// ═══════════════════════════════════════════════════════════════════════════════
// UiEffect —— 一次性副作用
// ═══════════════════════════════════════════════════════════════════════════════

/** 天气页面的一次性副作用。 */
sealed class WeatherEffect : UiEffect {

    /** 弹出 Toast。 */
    data class ShowToast(val message: String) : WeatherEffect()

    /** 切换当前城市。 */
    data class SwitchCity(val cityName: String) : WeatherEffect()

    /** 删除城市确认结果。 */
    data class CityRemoved(val cityName: String) : WeatherEffect()

    /**
     * 导航到天气详情页。
     *
     * 由 ViewBinding [WeatherBindingActivity] 处理为 `startActivity`；
     * Compose 侧不走 Effect，直接通过 `NavController.navigateToWeatherDetail()`。
     */
    data class NavigateToDetail(val cityName: String, val date: String = "") : WeatherEffect()
}

// ═══════════════════════════════════════════════════════════════════════════════
// Tab 枚举
// ═══════════════════════════════════════════════════════════════════════════════

/** 底部导航的三个 Tab。 */
//enum class WeatherTab(val label: String, override val ordinal: Int) {
//    TODAY("今日天气", 0),
//    FORECAST("未来预报", 1),
//    CITIES("城市管理", 2),
//}

sealed class WeatherTab(val label: String, val ordinal: Int) {
    object Today: WeatherTab("今日天气", 0)
    object Forecast: WeatherTab("未来预报", 1)
    object Cities: WeatherTab("城市管理", 2)
}
