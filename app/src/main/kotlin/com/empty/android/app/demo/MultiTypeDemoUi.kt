package com.empty.android.app.demo

import com.empty.android.core.mvi.UiEffect
import com.empty.android.core.mvi.UiState
import java.io.Serializable

// ── Item Types ───────────────────────────────────────────────────────────────

const val TYPE_BANNER = 0
const val TYPE_PRODUCT_SMALL = 1
const val TYPE_PRODUCT_LARGE = 2
const val TYPE_LOADING = 3

// ── Data ─────────────────────────────────────────────────────────────────────

sealed interface DemoItem : Serializable {

    data class Banner(
        val title: String,
        val desc: String,
    ) : DemoItem

    data class ProductSmall(
        val name: String,
        val icon: String,
        val price: String,
        val unit: String,
    ) : DemoItem

    data class ProductLarge(
        val name: String,
        val desc: String,
        val price: String,
    ) : DemoItem

    data object Loading : DemoItem
}

// ── State / Effect ───────────────────────────────────────────────────────────

data class DemoUiState(
    val items: List<DemoItem> = emptyList(),
) : UiState

sealed interface DemoEffect : UiEffect {
    data class ShowToast(val message: String) : DemoEffect
}
