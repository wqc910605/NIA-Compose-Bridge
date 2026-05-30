package com.nia.compose.bridge.weather.binding

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.nia.compose.bridge.R
import com.nia.compose.bridge.databinding.ActivityWeatherBinding
import com.nia.compose.bridge.weather.WeatherEffect
import com.nia.compose.bridge.weather.WeatherTab
import com.nia.compose.bridge.weather.WeatherViewModel
import com.nia.compose.bridge.common.UiEffect
import com.nia.compose.bridge.viewbinding.BaseActivity
import com.nia.compose.bridge.core.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint

/**
 * ViewBinding 版天气 App —— Navigation Component 宿主 Activity。
 *
 * ## 导航方式对照（ViewBinding ↔ Compose）
 *
 * | 维度 | ViewBinding (Navigation Component) | Compose |
 * |------|-------------------------------------|---------|
 * | 容器 | FragmentContainerView + NavHostFragment | NavHost composable |
 * | 导航图 | `res/navigation/weather_nav_graph.xml` 声明式 XML | `NavGraphBuilder` 声明式 DSL |
 * | Tab 切换 | `NavigationUI.setupWithNavController()` 自动联动 | `NavigationBar` + `navController.navigate()` |
 * | 详情跳转 | `navController.navigate(R.id.nav_weather_detail, args)` | `navController.navigateToWeatherDetail()` |
 * | 参数传递 | Bundle arg (String key) | @Serializable data class (type-safe) |
 * | 返回导航 | `navController.popBackStack()` 自动处理 | `navController.popBackStack()` |
 * | 底部栏联动 | menu item id ≡ destination id 自动匹配 | `selectedTab` state 手动管理 |
 *
 * ## 迁移结论
 * - ViewBinding → Compose 导航迁移本质是：XML nav graph → Kotlin DSL NavGraphBuilder
 * - Tab 切换 / 参数传递 / 返回栈逻辑完全对等，只是表达形式不同
 * - Compose 额外优势：类型安全参数（`@Serializable`）、编译期校验
 * - 迁移成本：集中在 `NavHost`/`NavGraph` 层的重写，Fragment/Composable 内部逻辑不变
 */
@AndroidEntryPoint
class WeatherBindingActivity : BaseActivity() {

    // ── ViewBinding ──────────────────────────────────────────────────────────

    override val binding by viewBinding(ActivityWeatherBinding::inflate)

    // ── ViewModel ────────────────────────────────────────────────────────────

    override val viewModel by viewModels<WeatherViewModel>()

    // ── Navigation Component ─────────────────────────────────────────────────

    private val navController by lazy {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navHostFragment.navController
    }

    // ── Lifecycle ────────────────────────────────────────────────────────────

    override fun initView() {
        // BottomNavigationView ←→ NavController 自动联动
        // menu item id (nav_today/nav_forecast/nav_cities) ≡ destination id
        binding.bottomNav.setupWithNavController(navController)

        // 监听 destination 变化，同步 ViewModel 的 selectedTab
        navController.addOnDestinationChangedListener { _, destination, _ ->
            val tab = when (destination.id) {
                R.id.nav_today -> WeatherTab.Today
                R.id.nav_forecast -> WeatherTab.Forecast
                R.id.nav_cities -> WeatherTab.Cities
                else -> return@addOnDestinationChangedListener
            }
            viewModel.selectTab(tab)
        }

        viewModel.loadWeather()
    }

    // ── Effect 处理 ──────────────────────────────────────────────────────────

    override fun handleEffect(effect: UiEffect) {
        when (effect) {
            is WeatherEffect.ShowToast -> {
                Toast.makeText(this, effect.message, Toast.LENGTH_SHORT).show()
            }
            is WeatherEffect.SwitchCity -> {
                // 切换城市后 Tab Fragment 通过 render() 自动刷新
            }
            is WeatherEffect.CityRemoved -> {
                // citiesFragment 会通过 render() 自动刷新
            }
            is WeatherEffect.NavigateToDetail -> {
                val args = Bundle().apply {
                    putString("cityName", effect.cityName)
                    putString("date", effect.date)
                }
                navController.navigate(R.id.nav_weather_detail, args)
            }
        }
    }
}
