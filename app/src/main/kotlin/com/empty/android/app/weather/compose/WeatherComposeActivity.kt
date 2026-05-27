package com.empty.android.app.weather.compose

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.empty.android.app.weather.WeatherEffect
import com.empty.android.app.weather.WeatherTab
import com.empty.android.app.weather.WeatherUiState
import com.empty.android.app.weather.WeatherViewModel
import com.empty.android.core.designsystem.theme.EmptyAndroidTheme
import com.empty.android.feature.weather.api.navigateToWeatherDetail
import com.empty.android.feature.weather.impl.navigation.weatherDetailScreen
import dagger.hilt.android.AndroidEntryPoint

/**
 * Compose 版天气 App —— 宿主 Activity。
 *
 * ## 架构对照（ViewBinding ↔ Compose）
 *
 * 这是和 [WeatherBindingActivity] **功能完全相同**的 Compose 实现。
 * 两者共享同一套 [WeatherViewModel]、[WeatherUiState]、[WeatherEffect]，只换 UI 层。
 *
 * | 概念 | ViewBinding 版 | Compose 版（本文件） |
 * |------|---------------|---------------------|
 * | 容器 | `BottomNavigationView` + `FragmentContainerView` | `NavigationBar` + `Scaffold` |
 * | Tab 页面 | `BaseFragment` 子类 | `@Composable` 函数 |
 * | Tab 状态 | `supportFragmentManager` 事务 | `rememberSaveable { mutableIntStateOf(0) }` |
 * | State 订阅 | `BaseActivity.initObservers()` → `render()` | `collectAsStateWithLifecycle()` |
 * | Effect 消费 | `handleEffect()` | `LaunchedEffect` |
 * | ViewModel | `viewModels<WeatherViewModel>()` | `hiltViewModel<WeatherViewModel>()` |
 */
@AndroidEntryPoint
class WeatherComposeActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EmptyAndroidTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    WeatherApp()
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// WeatherApp — 顶层 Composable（含 NavHost）
// ═══════════════════════════════════════════════════════════════════════════════

/**
 * WeatherApp 顶层组合。
 *
 * 使用 [NavHost] 管理两个 destinations：
 * - `"weather_main"` → Tab 页（今日 / 预报 / 城市）
 * - [WeatherDetailRoute] → 天气详情页
 *
 * Compose 侧导航：[WeatherEffect.NavigateToDetail] 在 [LaunchedEffect] 中消费，
 * 直接调用 [NavController.navigateToWeatherDetail]。
 */
@Composable
private fun WeatherApp(viewModel: WeatherViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val navController = rememberNavController()

    // 首次加载
    LaunchedEffect(Unit) {
        viewModel.loadWeather()
    }

    // Effect 消费 —— 对应 ViewBinding 的 handleEffect()
    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is WeatherEffect.ShowToast ->
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                is WeatherEffect.SwitchCity -> { /* 由 Tab screen 内部处理 */ }
                is WeatherEffect.CityRemoved -> { /* 列表自动刷新 */ }
                is WeatherEffect.NavigateToDetail ->
                    navController.navigateToWeatherDetail(effect.cityName, effect.date)
            }
        }
    }

    NavHost(navController = navController, startDestination = "weather_main") {
        composable("weather_main") {
            WeatherTabContent(
                uiState = uiState,
                viewModel = viewModel,
                onNavigateToDetail = { city, date ->
                    navController.navigateToWeatherDetail(city, date)
                },
            )
        }
        weatherDetailScreen(onBack = { navController.popBackStack() })
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// WeatherTabContent — Tab 页内容
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
private fun WeatherTabContent(
    uiState: WeatherUiState,
    viewModel: WeatherViewModel,
    onNavigateToDetail: (cityName: String, date: String) -> Unit,
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(WeatherTab.Today.ordinal) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                val tabs = listOf(
                    Triple(WeatherTab.Today, Icons.Filled.WbSunny, WeatherTab.Today.label),
                    Triple(WeatherTab.Forecast, Icons.Filled.DateRange, WeatherTab.Forecast.label),
                    Triple(WeatherTab.Cities, Icons.Filled.LocationOn, WeatherTab.Cities.label),
                )
                tabs.forEach { (tab, icon, label) ->
                    NavigationBarItem(
                        icon = { Icon(icon, contentDescription = label) },
                        label = { Text(label) },
                        selected = selectedTab == tab.ordinal,
                        onClick = {
                            selectedTab = tab.ordinal
                            viewModel.selectTab(tab)
                        },
                    )
                }
            }
        }
    ) { paddingValues ->
        when (selectedTab) {
            WeatherTab.Today.ordinal -> WeatherTodayScreen(
                uiState = uiState,
                onRetry = { viewModel.loadWeather() },
                modifier = Modifier.padding(paddingValues),
            )
            WeatherTab.Forecast.ordinal -> WeatherForecastScreen(
                uiState = uiState,
                onRetry = { viewModel.loadWeather() },
                onNavigateToDetail = { date -> onNavigateToDetail("", date) },
                modifier = Modifier.padding(paddingValues),
            )
            WeatherTab.Cities.ordinal -> WeatherCitiesScreen(
                uiState = uiState,
                onSwitchCity = { viewModel.switchCity(it) },
                onRemoveCity = { viewModel.removeCity(it) },
                onAddCity = { viewModel.addCity(it) },
                onRetry = { viewModel.loadWeather() },
                onNavigateToDetail = { cityName -> onNavigateToDetail(cityName, "") },
                modifier = Modifier.padding(paddingValues),
            )
        }
    }
}
