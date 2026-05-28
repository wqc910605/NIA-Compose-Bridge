package com.empty.android.core.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * ViewModel 基类，封装 State + Effect 模式。
 *
 * - **State**：[StateFlow] 保存完整 UI 快照，View 通过 [uiState] 订阅并渲染。
 * - **Effect**：一次性副作用通过 [Channel] 发送，不缓存、不重播。
 * - ViewModel 直接暴露 public 方法作为类型安全的合约。
 *
 * @param S UI 状态类型，实现 [UiState]
 * @param initialState 初始状态
 */
abstract class BaseViewModel<S : UiState>(
    initialState: S,
) : ViewModel() {

    // ── State ──────────────────────────────────────────────────────────────
    private val _uiState = MutableStateFlow(initialState)

    val uiState: StateFlow<S> = _uiState.asStateFlow()

    // ── Effect ─────────────────────────────────────────────────────────────
    private val _uiEffect = Channel<UiEffect>(Channel.BUFFERED)

    val uiEffect: Flow<UiEffect> = _uiEffect.receiveAsFlow()

    // ── Helpers ────────────────────────────────────────────────────────────

    protected fun updateState(reducer: (S) -> S) {
        _uiState.update(reducer)
    }

    protected fun setState(state: S) {
        _uiState.value = state
    }

    protected fun emitEffect(effect: UiEffect) {
        viewModelScope.launch {
            _uiEffect.send(effect)
        }
    }

    protected fun launch(block: suspend () -> Unit) {
        viewModelScope.launch {
            runCatching { block() }
                .onFailure { Timber.e(it, "uncaught error in BaseViewModel.launch") }
        }
    }
}
