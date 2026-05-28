package com.empty.android.app.demo

import com.empty.android.core.mvi.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import javax.inject.Inject

@HiltViewModel
class MultiTypeDemoViewModel @Inject constructor() :
    BaseViewModel<DemoUiState>(DemoUiState()) {

    fun loadData() {
        launch {
            setState(DemoUiState(items = mockItems()))
        }
    }

    fun loadMore() {
        val current = uiState.value.items
        if (current.lastOrNull() is DemoItem.Loading) return
        setState(DemoUiState(items = current + DemoItem.Loading))
        launch {
            delay(1200)
            val more = mockProducts().map {
                if (it is DemoItem.ProductLarge) {
                    it.copy(price = "¥${(30..99).random()}.00")
                } else it
            }
            val updated = current + more
            setState(DemoUiState(items = updated))
        }
    }

    private fun mockItems(): List<DemoItem> = listOf(
        DemoItem.Banner(title = "🔥 限时特惠", desc = "全场满 199 减 30，点击查看"),
        DemoItem.ProductSmall(name = "有机苹果", icon = "🍎", price = "¥12.80", unit = "/500g"),
        DemoItem.ProductSmall(name = "新鲜牛奶", icon = "🥛", price = "¥9.90", unit = "/250ml"),
        DemoItem.ProductLarge(
            name = "进口牛排套装",
            desc = "澳洲谷饲安格斯西冷牛排 200g×4片，肉质鲜嫩多汁",
            price = "¥168.00",
        ),
        DemoItem.ProductSmall(name = "手工吐司", icon = "🍞", price = "¥16.50", unit = "/袋"),
        DemoItem.ProductSmall(name = "有机鸡蛋", icon = "🥚", price = "¥22.80", unit = "/盒"),
        DemoItem.ProductLarge(
            name = "熟冻大虾",
            desc = "厄瓜多尔白虾 1.8kg 装，去虾线，鲜美 Q 弹",
            price = "¥89.00",
        ),
        DemoItem.ProductSmall(name = "澳洲燕麦", icon = "🥣", price = "¥35.00", unit = "/袋"),
        DemoItem.ProductLarge(
            name = "精品红酒礼盒",
            desc = "智利原装进口赤霞珠干红 750ml×2瓶，附赠礼袋",
            price = "¥298.00",
        ),
    )

    private fun mockProducts() = listOf(
        DemoItem.ProductLarge(
            name = "日式抹茶粉",
            desc = "宇治抹茶 100g 装，茶道级品质",
            price = "¥58.00",
        ),
        DemoItem.ProductSmall(name = "蜂蜜柚子茶", icon = "🍯", price = "¥28.00", unit = "/瓶"),
    )
}
