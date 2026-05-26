package com.empty.android.core.mvi

/**
 * MVI 契约接口标记。
 *
 * ## 推荐的 State 设计模式：Sealed Class + Data Class 组合
 *
 * 用 **Sealed Class** 表达页面的宏观互斥状态（Loading / Error / Success），
 * 用 **Data Class** 把 Success 内部的子模块横向组合，解决多子模块并存问题。
 *
 * ```kotlin
 * // 1. 子模块 Data Class（可并行渲染）
 * data class DetailData(
 *     val headerState: HeaderState = HeaderState(),
 *     val commentState: CommentState = CommentState(),
 *     val recommendState: RecommendState = RecommendState()
 * )
 *
 * // 2. 页面级 Sealed Class（宏观互斥）
 * sealed class DetailUiState : UiState {
 *     data object Loading : DetailUiState()
 *
 *     data class Error(val errorMsg: String) : DetailUiState()
 *
 *     // 关键：Success 包裹子模块 Data Class
 *     data class Success(val data: DetailData) : DetailUiState()
 * }
 * ```
 *
 * ### 在 ViewBinding 和 Compose 中落地
 *
 * **ViewBinding：** `when` 统一处理宏观状态，`diffUpdate` 做局部刷新：
 *
 * ```kotlin
 * override fun render(state: UiState) {
 *     when (val s = state) {
 *         is DetailUiState.Loading -> binding.progressBar.isVisible = true
 *         is DetailUiState.Error   -> showEmptyView(s.errorMsg)
 *         is DetailUiState.Success -> {
 *             binding.progressBar.isVisible = false
 *             binding.tvTitle.diffUpdate(s.data.headerState.title) { text = it }
 *             binding.rvComment.diffUpdate(s.data.commentState.list) { submitList(it) }
 *         }
 *     }
 * }
 * ```
 *
 * **Compose（未来迁移）：** 相同的 State 不做任何修改，只改 UI 层：
 *
 * ```kotlin
 * @Composable
 * fun DetailScreen(viewModel: DetailViewModel) {
 *     val uiState by viewModel.uiState.collectAsStateWithLifecycle()
 *     when (val state = uiState) {
 *         is DetailUiState.Loading -> LoadingScreen()
 *         is DetailUiState.Error   -> ErrorScreen(state.errorMsg)
 *         is DetailUiState.Success -> {
 *             Column {
 *                 HeaderComponent(state = state.data.headerState)
 *                 CommentComponent(state = state.data.commentState)
 *             }
 *         }
 *     }
 * }
 * ```
 *
 * @see UiEffect 一次性副作用
 * @see BaseViewModel
 */
interface UiState

/**
 * 一次性 UI 副作用。
 * ViewModel 通过 [BaseViewModel.emitEffect] 发出，View 订阅后处理一次即丢弃。
 *
 * ```kotlin
 * sealed class DetailUiEffect : UiEffect {
 *     data class ShowToast(val message: String) : DetailUiEffect()
 *     data class NavigateTo(val id: String) : DetailUiEffect()
 * }
 * ```
 */
interface UiEffect
