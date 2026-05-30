package com.nia.compose.bridge.domain

import javax.inject.Inject

// ═══════════════════════════════════════════════════════════════════════════════
// Domain 层天气数据模型
// ═══════════════════════════════════════════════════════════════════════════════

/** 天气状况枚举（Domain 层）。 */
enum class WeatherConditionDomain(val label: String, val icon: String) {
    SUNNY("晴", "☀️"),
    PARTLY_CLOUDY("多云", "⛅"),
    CLOUDY("阴", "☁️"),
    RAIN("雨", "🌧️"),
    HEAVY_RAIN("大雨", "⛈️"),
    SNOW("雪", "❄️"),
    THUNDER("雷阵雨", "⚡"),
    FOG("雾", "🌫️"),
}

data class CityDomain(
    val id: String,
    val name: String,
    val country: String = "中国",
    val isCurrent: Boolean = false,
)

data class CurrentWeatherDomain(
    val cityName: String,
    val temperature: Int,
    val feelsLike: Int,
    val condition: WeatherConditionDomain,
    val humidity: Int,
    val windSpeed: Float,
    val windDirection: String,
    val visibility: Float,
    val uvIndex: Int,
    val pressure: Int,
    val highTemp: Int,
    val lowTemp: Int,
    val sunrise: String,
    val sunset: String,
    val airQuality: Int,
    val airQualityLabel: String,
)

data class ForecastDayDomain(
    val date: String,
    val condition: WeatherConditionDomain,
    val highTemp: Int,
    val lowTemp: Int,
    val humidity: Int,
    val windSpeed: Float,
    val rainProbability: Int,
    val sunrise: String,
    val sunset: String,
)

data class WeatherDataDomain(
    val currentWeather: CurrentWeatherDomain,
    val forecast: List<ForecastDayDomain>,
    val cities: List<CityDomain>,
)

// ═══════════════════════════════════════════════════════════════════════════════
// GetWeatherUseCase
// ═══════════════════════════════════════════════════════════════════════════════

/**
 * 天气数据 UseCase。
 *
 * 封装天气数据的生成逻辑，供不同 UI 层（ViewBinding / Compose）共享。
 * 实际业务中这里会注入 Repository，此处使用 mock 数据演示。
 *
 * ## 用法
 * ```kotlin
 * class WeatherViewModel @Inject constructor(
 *     private val getWeather: GetWeatherUseCase,
 * ) : BaseViewModel<...>(...) {
 *     fun loadWeather() {
 *         val data = getWeather("深圳", cities)
 *         setState(...)
 *     }
 * }
 * ```
 */
class GetWeatherUseCase @Inject constructor() {

    /**
     * 获取指定城市的天气数据。
     *
     * @param cityName 当前城市名
     * @param cities 城市列表
     * @return 包含当前天气、预报、城市列表的 [WeatherDataDomain]
     */
    operator fun invoke(cityName: String, cities: List<CityDomain>): WeatherDataDomain = WeatherDataDomain(
        currentWeather = mockCurrentWeather(cityName),
        forecast = mockForecast(cityName),
        cities = cities,
    )

    // ── 模拟数据生成 ──────────────────────────────────────────────────────────

    private fun mockCurrentWeather(cityName: String) = CurrentWeatherDomain(
        cityName = cityName,
        temperature = 28,
        feelsLike = 30,
        condition = WeatherConditionDomain.PARTLY_CLOUDY,
        humidity = 65,
        windSpeed = 12.5f,
        windDirection = "东南风",
        visibility = 10.0f,
        uvIndex = 6,
        pressure = 1013,
        highTemp = 32,
        lowTemp = 24,
        sunrise = "05:42",
        sunset = "19:08",
        airQuality = 42,
        airQualityLabel = "优",
    )

    private fun mockForecast(cityName: String): List<ForecastDayDomain> {
        val dates = listOf(
            "5月28日 周三", "5月29日 周四", "5月30日 周五",
            "5月31日 周六", "6月1日 周日", "6月2日 周一", "6月3日 周二",
        )
        val conditions = listOf(
            WeatherConditionDomain.SUNNY,
            WeatherConditionDomain.PARTLY_CLOUDY,
            WeatherConditionDomain.RAIN,
            WeatherConditionDomain.THUNDER,
            WeatherConditionDomain.CLOUDY,
            WeatherConditionDomain.SUNNY,
            WeatherConditionDomain.PARTLY_CLOUDY,
        )
        return dates.zip(conditions).mapIndexed { i, (date, cond) ->
            ForecastDayDomain(
                date = date,
                condition = cond,
                highTemp = 30 + i,
                lowTemp = 22 + i,
                humidity = 60 + i * 3,
                windSpeed = 8.0f + i * 2,
                rainProbability = when (cond) {
                    WeatherConditionDomain.RAIN -> 80
                    WeatherConditionDomain.THUNDER -> 95
                    WeatherConditionDomain.CLOUDY -> 30
                    else -> 5 + i * 5
                },
                sunrise = "05:42",
                sunset = "19:08",
            )
        }
    }
}
