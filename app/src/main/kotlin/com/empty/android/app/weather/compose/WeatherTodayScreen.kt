package com.empty.android.app.weather.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.empty.android.app.weather.WeatherUiState

/**
 * 今日天气 Screen —— Compose 实现。
 *
 * 对应 ViewBinding 的 [WeatherTodayFragment]，渲染完全相同的 [WeatherData.currentWeather]。
 *
 * ## 对照表
 *
 * | 元素 | ViewBinding | Compose |
 * |------|-------------|---------|
 * | 天气图标 | `binding.tvWeatherIcon.diffUpdate(icon) { text = it }` | `Text(icon, fontSize = 64.sp)` |
 * | 温度 | `binding.tvTemperature.diffUpdate(temp) { text = "${it}°" }` | `Text("${temp}°", fontSize = 72.sp)` |
 * | 指标 Grid | 4列 LinearLayout | `Row { Column { ... } }` |
 * | 分隔线 | `<View>` | `HorizontalDivider()` |
 * | 状态切换 | `when(state) { ... binding.xxx.visibility = ... }` | `when(state) { Loading -> ... Success -> ... }` |
 */
@Composable
fun WeatherTodayScreen(
    uiState: WeatherUiState,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        when (uiState) {
            is WeatherUiState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            is WeatherUiState.Error -> {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(uiState.message, style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onRetry) {
                        Text("重试")
                    }
                }
            }
            is WeatherUiState.Success -> {
                val w = uiState.data.currentWeather
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    // 城市名
                    Text(w.cityName, fontSize = 24.sp, fontWeight = FontWeight.Bold)

                    // 天气图标
                    Text(w.condition.icon, fontSize = 64.sp)

                    // 温度
                    Text("${w.temperature}°", fontSize = 72.sp, fontWeight = FontWeight.Bold)

                    // 天气状况
                    Text(w.condition.label, fontSize = 18.sp)

                    // 最高/最低
                    Text(
                        "↑${w.highTemp}° / ↓${w.lowTemp}°",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(20.dp))

                    // 详细指标 Grid - Row 1
                    DetailGrid(
                        items = listOf(
                            "体感温度" to "${w.feelsLike}°",
                            "湿度" to "${w.humidity}%",
                            "能见度" to "${w.visibility}km",
                            "紫外线" to "${w.uvIndex}",
                        ),
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 详细指标 Grid - Row 2
                    DetailGrid(
                        items = listOf(
                            "气压" to "${w.pressure}hPa",
                            "风速" to "${w.windSpeed}km/h ${w.windDirection}",
                            "日出" to w.sunrise,
                            "日落" to w.sunset,
                        ),
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(12.dp))

                    // 空气质量
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("空气质量", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            "AQI ${w.airQuality} · ${w.airQualityLabel}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                        )
                    }
                }
            }
        }
    }
}

/** 指标网格：4列均匀分布。 */
@Composable
private fun DetailGrid(items: List<Pair<String, String>>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        items.forEach { (label, value) ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    label,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    value,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}
