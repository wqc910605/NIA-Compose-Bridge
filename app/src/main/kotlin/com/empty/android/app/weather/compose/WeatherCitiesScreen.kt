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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.empty.android.app.weather.CityInfo
import com.empty.android.app.weather.WeatherUiState

/**
 * 城市管理 Screen —— Compose 实现。
 *
 * 对应 ViewBinding 的 [WeatherCitiesFragment]，城市列表 CRUD + 切换当前城市。
 *
 * ## 对照表
 *
 * | ViewBinding | Compose |
 * |-------------|---------|
 * | EditText + Button + `EditorInfo.IME_ACTION_DONE` | `OutlinedTextField` + `IconButton(Icons.Add)` |
 * | RecyclerView + ListAdapter + DiffUtil | LazyColumn + items(key=id) |
 * | `binding.tvCurrentBadge.visibility` | `if (isCurrent) SuggestionChip("当前")` |
 */
@Composable
fun WeatherCitiesScreen(
    uiState: WeatherUiState,
    onSwitchCity: (String) -> Unit,
    onRemoveCity: (String) -> Unit,
    onAddCity: (String) -> Unit,
    onRetry: () -> Unit,
    onNavigateToDetail: (cityName: String) -> Unit = {},
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
                val cities = uiState.data.cities
                Column(modifier = Modifier.fillMaxSize()) {
                    // 城市列表
                    LazyColumn(
                        modifier = Modifier.weight(1f).padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(
                            items = cities,
                            key = { it.id },
                        ) { city ->
                            CityCard(
                                city = city,
                                onClick = { onNavigateToDetail(city.name) },
                                onDelete = { onRemoveCity(city.name) },
                            )
                        }
                    }

                    // 添加城市输入区
                    var newCity by remember { mutableStateOf("") }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        OutlinedTextField(
                            value = newCity,
                            onValueChange = { newCity = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("输入城市名称") },
                            singleLine = true,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                if (newCity.isNotBlank()) {
                                    onAddCity(newCity.trim())
                                    newCity = ""
                                }
                            },
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = "添加城市")
                        }
                    }
                }
            }
        }
    }
}

/** 单个城市卡片（点击跳转详情页）。 */
@Composable
private fun CityCard(
    city: CityInfo,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
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
            Column(modifier = Modifier.weight(1f)) {
                Text(city.name, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(
                    city.country,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // 当前城市标记
            if (city.isCurrent) {
                SuggestionChip(
                    onClick = {},
                    label = { Text("当前") },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                    ),
                )
                Spacer(modifier = Modifier.width(8.dp))
            }

            // 删除按钮
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "删除",
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}
