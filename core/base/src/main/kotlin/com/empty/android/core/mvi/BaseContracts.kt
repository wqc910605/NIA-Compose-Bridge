package com.empty.android.core.mvi

/**
 * UI 状态契约接口。
 *
 * 推荐与 [BaseViewModel] 配合使用，采用 Sealed Class + Data Class 组合模式：
 * - Sealed Class 表达页面宏观互斥状态（Loading / Error / Success）
 * - Data Class 承载 Success 内部各子模块数据
 *
 * @see UiEffect 一次性副作用
 * @see BaseViewModel
 */
interface UiState

/**
 * 一次性 UI 副作用标记。
 * ViewModel 通过 [BaseViewModel.emitEffect] 发出，View 订阅后消费一次即丢弃。
 */
interface UiEffect
