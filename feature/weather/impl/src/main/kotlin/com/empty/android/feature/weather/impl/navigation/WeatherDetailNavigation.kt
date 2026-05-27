package com.empty.android.feature.weather.impl.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.empty.android.feature.weather.api.WeatherDetailRoute
import com.empty.android.feature.weather.impl.WeatherDetailScreen

/**
 * Weather detail feature 向导航图注册自己的 composable。
 *
 * [onBack] 由 `:app` 传入，实现返回上级页面的导航。
 */
fun NavGraphBuilder.weatherDetailScreen(
    onBack: () -> Unit,
) {
    composable<WeatherDetailRoute> { backStackEntry ->
        val route = backStackEntry.toRoute<WeatherDetailRoute>()
        WeatherDetailScreen(
            cityName = route.cityName,
            date = route.date,
            onBack = onBack,
        )
    }
}
