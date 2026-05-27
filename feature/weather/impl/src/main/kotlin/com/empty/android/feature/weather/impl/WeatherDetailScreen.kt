package com.empty.android.feature.weather.impl

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.empty.android.core.domain.ForecastDayDomain

/**
 * 天气详情页 —— Compose 实现。
 *
 * ## 架构对照（ViewBinding ↔ Compose）
 *
 * | 维度 | ViewBinding | Compose |
 * |------|-------------|---------|
 * | 容器 | [BaseActivity] | [ComponentActivity] + [Scaffold] |
 * | 状态订阅 | [BaseActivity.initObservers] → [render] | [collectAsStateWithLifecycle] |
 * | 返回导航 | `finish()` | `onBack()` callback |
 * | 列表渲染 | RecyclerView + Adapter | [LazyColumn] + [items] |
 * | ViewModel | [viewModels] | [hiltViewModel] |
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherDetailScreen(
    cityName: String,
    date: String,
    onBack: () -> Unit,
    viewModel: WeatherDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(cityName, date) {
        viewModel.loadDetail(cityName, date)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (date.isNotBlank()) "$cityName · $date" else "$cityName 天气详情"
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when (val state = uiState) {
                is WeatherDetailUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is WeatherDetailUiState.Error -> {
                    Text(
                        state.message,
                        modifier = Modifier.align(Alignment.Center),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
                is WeatherDetailUiState.Success -> {
                    DetailContent(state)
                }
            }
        }
    }
}

@Composable
private fun DetailContent(state: WeatherDetailUiState.Success) {
    val w = state.currentWeather

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // ── 当前天气摘要 ────────────────────────────────────────────
        item(key = "summary") {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(w.condition.icon, fontSize = 56.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("${w.temperature}°", fontSize = 56.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(w.condition.label, fontSize = 20.sp)
                    Text(
                        "↑${w.highTemp}° / ↓${w.lowTemp}°",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        // ── 详细指标 ────────────────────────────────────────────────
        item(key = "details") {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("详细指标", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))

                    DetailRow("体感温度", "${w.feelsLike}°")
                    HorizontalDivider()
                    DetailRow("湿度", "${w.humidity}%")
                    HorizontalDivider()
                    DetailRow("风速", "${w.windSpeed}km/h ${w.windDirection}")
                    HorizontalDivider()
                    DetailRow("能见度", "${w.visibility}km")
                    HorizontalDivider()
                    DetailRow("紫外线指数", "${w.uvIndex}")
                    HorizontalDivider()
                    DetailRow("气压", "${w.pressure}hPa")
                    HorizontalDivider()
                    DetailRow("日出/日落", "${w.sunrise} / ${w.sunset}")
                    HorizontalDivider()
                    DetailRow("空气质量", "AQI ${w.airQuality} · ${w.airQualityLabel}")
                }
            }
        }

        // ── 预报列表 ────────────────────────────────────────────────
        item(key = "forecast_header") {
            Text(
                "未来预报",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp),
            )
        }

        items(
            items = state.forecast,
            key = { it.date },
        ) { day ->
            ForecastDayCard(day)
        }
    }
}

@Composable
private fun ForecastDayCard(day: ForecastDayDomain) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(day.date, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(
                    day.condition.label,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(day.condition.icon, fontSize = 28.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text("${day.highTemp}°", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("${day.lowTemp}°", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("🌧 ${day.rainProbability}%", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
    ) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.weight(1f))
        Text(value, fontWeight = FontWeight.Medium)
    }
}
