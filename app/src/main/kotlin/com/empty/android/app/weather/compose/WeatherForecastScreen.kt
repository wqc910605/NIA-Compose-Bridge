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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.empty.android.app.weather.ForecastDay
import com.empty.android.app.weather.WeatherUiState

/**
 * 未来预报 Screen —— Compose 实现。
 *
 * 对应 ViewBinding 的 [WeatherForecastFragment]，用 [LazyColumn] + key() 替代 RecyclerView + DiffUtil。
 *
 * ## 对照表
 *
 * | ViewBinding | Compose |
 * |-------------|---------|
 * | RecyclerView + ListAdapter + DiffUtil | LazyColumn + items(key=date) |
 * | item_forecast_day.xml 布局 | ForecastRow @Composable |
 * | adapter.submitList(list) | items(list, key = { it.date }) |
 */
@Composable
fun WeatherForecastScreen(
    uiState: WeatherUiState,
    onRetry: () -> Unit,
    onNavigateToDetail: (date: String) -> Unit = {},
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
                    Button(onClick = onRetry) { Text("重试") }
                }
            }
            is WeatherUiState.Success -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(
                        items = uiState.data.forecast,
                        key = { it.date },
                    ) { day ->
                        ForecastCard(day, onClick = { onNavigateToDetail(day.date) })
                    }
                }
            }
        }
    }
}

/** 单日预报卡片（点击跳转详情页）。 */
@Composable
private fun ForecastCard(day: ForecastDay, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // 日期 + 天气状况
            Column(modifier = Modifier.weight(1f)) {
                Text(day.date, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(
                    day.condition.label,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // 天气图标
            Text(day.condition.icon, fontSize = 32.sp)

            Spacer(modifier = Modifier.width(16.dp))

            // 温度 + 降雨概率
            Column(horizontalAlignment = Alignment.End) {
                Text("${day.highTemp}°", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(
                    "${day.lowTemp}°",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    "🌧 ${day.rainProbability}%",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}
