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
 * MVI 架构 ViewModel 基类。
 *
 * ## 设计原则
 * - **State**：[StateFlow] 保存完整 UI 快照，View 通过 [uiState] 订阅并渲染。
 * - **Effect**：一次性副作用通过 [Channel] 发送，不缓存、不重播，View 通过 [uiEffect] 收集。
 * - **无 Intent 密封类**：ViewModel 直接暴露 public 方法，方法签名本身就是类型安全的合约。
 *
 * ## 推荐的 State 设计：Sealed Class + Data Class 组合
 *
 * 用 Sealed Class 表达页面宏观互斥状态（Loading / Error / Success），
 * 用 Data Class 把 Success 内部的子模块横向组合。
 *
 * ```kotlin
 * // 子模块 —— 并行共存
 * data class DetailData(
 *     val headerState: HeaderState = HeaderState(),
 *     val commentState: CommentState = CommentState(),
 *     val recommendState: RecommendState = RecommendState(),
 * )
 *
 * // 页面宏观状态 —— 互斥
 * sealed class DetailUiState : UiState {
 *     data object Loading : DetailUiState()
 *     data class Error(val errorMsg: String) : DetailUiState()
 *     data class Success(val data: DetailData) : DetailUiState()
 * }
 * ```
 *
 * ## 用法
 * ```kotlin
 * @HiltViewModel
 * class DetailViewModel @Inject constructor(
 *     private val repo: DetailRepository,
 * ) : MviViewModel<DetailUiState>(DetailUiState.Loading) {
 *
 *     fun loadDetail(id: String) {
 *         setState(DetailUiState.Loading)
 *         launch {
 *             repo.getDetail(id)
 *                 .onSuccess { detail ->
 *                     setState(DetailUiState.Success(detail.toData()))
 *                 }
 *                 .onFailure { e ->
 *                     setState(DetailUiState.Error(e.message ?: "Unknown error"))
 *                 }
 *         }
 *     }
 *
 *     fun onCommentClicked(commentId: String) {
 *         emitEffect(DetailUiEffect.NavigateToComment(commentId))
 *     }
 * }
 * ```
 *
 * @param S UI 状态类型，实现 [UiState]
 * @param initialState 初始状态
 */
abstract class BaseViewModel<S : UiState>(
    initialState: S,
) : ViewModel() {

    // ── State ──────────────────────────────────────────────────────────────
    private val _uiState = MutableStateFlow(initialState)

    /** View 订阅此流来驱动 UI 渲染（幂等、可重播）。 */
    val uiState: StateFlow<S> = _uiState.asStateFlow()

    // ── Effect ─────────────────────────────────────────────────────────────
    // BUFFERED channel：发出后只消费一次，不缓存历史。
    private val _uiEffect = Channel<UiEffect>(Channel.BUFFERED)

    /** View 订阅此流来处理一次性副作用（导航、Toast 等）。 */
    val uiEffect: Flow<UiEffect> = _uiEffect.receiveAsFlow()

    // ── Helpers ────────────────────────────────────────────────────────────

    /** 在当前状态基础上进行局部更新（[MutableStateFlow.update] 保证原子性）。 */
    protected fun updateState(reducer: (S) -> S) {
        _uiState.update(reducer)
    }

    /** 直接替换整个 State。 */
    protected fun setState(state: S) {
        _uiState.value = state
    }

    /** 发出一次性副作用。 */
    protected fun emitEffect(effect: UiEffect) {
        Timber.d("[MVI] effect → ${effect::class.simpleName}")
        viewModelScope.launch {
            _uiEffect.send(effect)
        }
    }

    /**
     * 在 [viewModelScope] 中启动协程，并在出错时记录日志。
     * 子类可用此方法替代裸 [viewModelScope.launch] 以获得统一的错误处理。
     */
    protected fun launch(block: suspend () -> Unit) {
        viewModelScope.launch {
            runCatching { block() }
                .onFailure { Timber.e(it, "[MVI] uncaught error in launch") }
        }
    }
}
