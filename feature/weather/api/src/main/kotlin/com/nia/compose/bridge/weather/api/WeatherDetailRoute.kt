package com.nia.compose.bridge.weather.api

import android.content.Context
import android.content.Intent
import kotlinx.serialization.Serializable

@Serializable
data class WeatherDetailRoute(
    val cityName: String,
    val date: String = "",
)

fun WeatherDetailRoute.intent(context: Context): Intent {
    return Intent(context, Class.forName("${context.packageName}.feature.weather.impl.WeatherDetailActivity")).apply {
        putExtra("cityName", cityName)
        putExtra("date", date)
    }
}