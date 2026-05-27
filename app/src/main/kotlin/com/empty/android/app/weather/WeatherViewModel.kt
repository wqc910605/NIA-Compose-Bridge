package com.empty.android.app.weather

import com.empty.android.core.domain.CityDomain
import com.empty.android.core.domain.CurrentWeatherDomain
import com.empty.android.core.domain.ForecastDayDomain
import com.empty.android.core.domain.GetWeatherUseCase
import com.empty.android.core.mvi.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import javax.inject.Inject

/**
 * 天气 App ViewModel。
 *
 * ## 设计说明 —— ViewBinding / Compose 共享
 *
 * 同一个 ViewModel 同时驱动 ViewBinding 版和 Compose 版 UI：
 * - ViewBinding 侧通过 [BaseActivity.initObservers] 订阅 [uiState] 和 [uiEffect]，
 *   在 [render]/[handleEffect] 中用 `when` + [diffUpdate] 更新 View。
 * - Compose 侧通过 `collectAsStateWithLifecycle()` 订阅 [uiState]，
 *   用 `LaunchedEffect` 收集 [uiEffect]，在 `when(state)` 中渲染 Composable。
 *
 * 两边不做任何适配——State 和 Effect 对 ViewBinding 和 Compose 是完全透明的。
 *
 * ## API 一览
 * | 方法 | 用途 |
 * |------|------|
 * | [loadWeather] | 首次加载所有天气数据 |
 * | [refreshWeather] | 下拉刷新当前数据 |
 * | [switchCity] | 切换到指定城市 |
 * | [removeCity] | 从城市列表删除指定城市 |
 * | [addCity] | 添加新城市 |
 * | [selectTab] | 底部 Tab 切换 |
 */
@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val getWeather: GetWeatherUseCase,
) : BaseViewModel<WeatherUiState>(WeatherUiState.Loading) {

    // ── 内部状态 ────────────────────────────────────────────────────────────

    private var currentCities: List<CityInfo> = mockCities.toList()

    // ── 公开方法 ────────────────────────────────────────────────────────────

    /** 加载所有天气数据（模拟网络请求）。 */
    fun loadWeather() {
        setState(WeatherUiState.Loading)
        launch {
            delay(1_200)
            val data = buildWeatherData()
            setState(WeatherUiState.Success(data))
        }
    }

    /** 下拉刷新（加载 + 短暂 loading 动画）。 */
    fun refreshWeather() {
        setState(WeatherUiState.Loading)
        launch {
            delay(800)
            val data = buildWeatherData()
            setState(WeatherUiState.Success(data))
            emitEffect(WeatherEffect.ShowToast("刷新成功"))
        }
    }

    /** 切换当前城市。 */
    fun switchCity(cityName: String) {
        currentCities = currentCities.map {
            it.copy(isCurrent = it.name == cityName)
        }
        refreshWeather()
        emitEffect(WeatherEffect.SwitchCity(cityName))
    }

    /** 从城市列表删除城市。 */
    fun removeCity(cityName: String) {
        currentCities = currentCities.filter { it.name != cityName }
        // 如果删的是当前城市，切到第一个
        val newCurrent = currentCities.firstOrNull { it.isCurrent }
            ?: currentCities.firstOrNull()?.copy(isCurrent = true)
        if (newCurrent != null) {
            currentCities = currentCities.map {
                if (it.id == newCurrent.id) newCurrent else it.copy(isCurrent = false)
            }
        }
        updateWeatherData()
        emitEffect(WeatherEffect.CityRemoved(cityName))
        emitEffect(WeatherEffect.ShowToast("已删除 $cityName"))
    }

    /** 添加新城市。 */
    fun addCity(cityName: String) {
        val alreadyExists = currentCities.any { it.name == cityName }
        if (alreadyExists) {
            emitEffect(WeatherEffect.ShowToast("$cityName 已在列表中"))
            return
        }
        val newCity = CityInfo(
            id = "city_${System.currentTimeMillis()}",
            name = cityName,
            isCurrent = false,
        )
        currentCities = currentCities + newCity
        updateWeatherData()
        emitEffect(WeatherEffect.ShowToast("已添加 $cityName"))
    }

    /** 底部 Tab 切换。 */
    fun selectTab(tab: WeatherTab) {
        @Suppress("UNCHECKED_CAST")
        (uiState.value as? WeatherUiState.Success)?.let { current ->
            setState(
                WeatherUiState.Success(
                    current.data.copy(selectedTab = tab)
                )
            )
        }
    }

    /**
     * 发出导航到天气详情页的 Effect。
     *
     * ViewBinding 端由 [WeatherBindingActivity.handleEffect] 处理为
     * [NavController.navigate] 跳转到 `nav_weather_detail` destination；
     * Compose 端在 [WeatherComposeActivity] 中直接调用 `navController.navigateToWeatherDetail()`。
     */
    fun launchNavigateToDetail(cityName: String, date: String = "") {
        emitEffect(WeatherEffect.NavigateToDetail(cityName, date))
    }

    // ── 内部方法 ────────────────────────────────────────────────────────────

    /** 不触发 Loading，直接在已加载的 WeatherData 中更新城市列表。 */
    private fun updateWeatherData() {
        @Suppress("UNCHECKED_CAST")
        (uiState.value as? WeatherUiState.Success)?.let { current ->
            val currentCity = currentCities.firstOrNull { it.isCurrent }
                ?: currentCities.firstOrNull()
            val cityName = currentCity?.name ?: "深圳"
            val domainData = getWeather(cityName, currentCities.map { it.toDomain() })
            setState(
                WeatherUiState.Success(
                    current.data.copy(
                        cities = currentCities,
                        currentWeather = domainData.currentWeather.toApp(),
                    )
                )
            )
        }
    }

    private fun buildWeatherData(): WeatherData {
        val currentCity = currentCities.firstOrNull { it.isCurrent }
            ?: currentCities.firstOrNull()
        val cityName = currentCity?.name ?: "深圳"
        val domainData = getWeather(cityName, currentCities.map { it.toDomain() })
        return WeatherData(
            currentWeather = domainData.currentWeather.toApp(),
            forecast = domainData.forecast.map { it.toApp() },
            cities = currentCities,
            selectedTab = WeatherTab.Today,
        )
    }

    // ── Domain ↔ App 映射 ───────────────────────────────────────────────────

    private fun CityInfo.toDomain() = CityDomain(id, name, country, isCurrent)

    private fun CurrentWeatherDomain.toApp() = CurrentWeather(
        cityName = cityName,
        temperature = temperature,
        feelsLike = feelsLike,
        condition = WeatherCondition.valueOf(condition.name),
        humidity = humidity,
        windSpeed = windSpeed,
        windDirection = windDirection,
        visibility = visibility,
        uvIndex = uvIndex,
        pressure = pressure,
        highTemp = highTemp,
        lowTemp = lowTemp,
        sunrise = sunrise,
        sunset = sunset,
        airQuality = airQuality,
        airQualityLabel = airQualityLabel,
    )

    private fun ForecastDayDomain.toApp() = ForecastDay(
        date = date,
        condition = WeatherCondition.valueOf(condition.name),
        highTemp = highTemp,
        lowTemp = lowTemp,
        humidity = humidity,
        windSpeed = windSpeed,
        rainProbability = rainProbability,
        sunrise = sunrise,
        sunset = sunset,
    )

    companion object {
        private val mockCities = listOf(
            CityInfo(id = "city_sz", name = "深圳", isCurrent = true),
            CityInfo(id = "city_bj", name = "北京"),
            CityInfo(id = "city_sh", name = "上海"),
            CityInfo(id = "city_gz", name = "广州"),
            CityInfo(id = "city_cd", name = "成都"),
            CityInfo(id = "city_hz", name = "杭州"),
        )
    }
}
