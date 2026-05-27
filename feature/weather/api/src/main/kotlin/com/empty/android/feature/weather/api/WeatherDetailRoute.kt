package com.empty.android.feature.weather.api

import androidx.navigation.NavController
import androidx.navigation.NavOptions
import kotlinx.serialization.Serializable

/**
 * 天气详情页的导航 key。
 *
 * 暴露在 `:api` 模块，其他 feature 的 `:impl` 或 `:app` 如果需要跳转到天气详情，
 * 仅依赖 `:feature:weather:api` 即可获取到该 key 以及 [navigateToWeatherDetail]。
 *
 * ## ViewBinding 导航
 * ViewBinding 不使用 NavController，而是通过 Intent 跳转：
 * ```kotlin
 * startActivity(WeatherDetailRoute.intent(this, cityName, date))
 * ```
 *
 * ## Compose 导航
 * ```kotlin
 * navController.navigateToWeatherDetail("深圳", "5月28日")
 * ```
 */
@Serializable
data class WeatherDetailRoute(
    val cityName: String,
    val date: String = "",
)

/**
 * Compose 导航：跳转到天气详情页。
 *
 * @param cityName 城市名
 * @param date 预报日期，留空则显示该城市的综合天气详情
 * @param navOptions 可选导航选项（如 `popUpTo`、`launchSingleTop`）
 */
fun NavController.navigateToWeatherDetail(
    cityName: String,
    date: String = "",
    navOptions: NavOptions? = null,
) = navigate(
    route = WeatherDetailRoute(cityName = cityName, date = date),
    navOptions = navOptions,
)
