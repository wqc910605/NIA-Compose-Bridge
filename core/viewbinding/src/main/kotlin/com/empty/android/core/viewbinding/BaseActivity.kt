package com.empty.android.core.viewbinding

import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewbinding.ViewBinding
import com.empty.android.core.mvi.BaseViewModel
import com.empty.android.core.mvi.UiEffect
import com.empty.android.core.mvi.UiState
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * ViewBinding + MVI 架构的 Activity 基类（无泛型）。
 *
 * 封装了 ViewBinding 设置、State/Effect 订阅等通用模板。使用 [collect]（非 collectLatest）
 * 确保 Sealed Class 宏观状态的每一次转换都会被 [render] 处理，不漏过 Loading→Error→Success 等过渡。
 *
 * 子类只需：
 * 1. 实现 [createBinding] 返回自己的 ViewBinding（子类另存为 typed 字段即可直接访问具体控件）
 * 2. 提供 [viewModel]
 * 3. 实现 [render]（State → UI），推荐用 `when` 处理 Sealed Class
 * 4. 可选覆写 [handleEffect]、[initView]
 *
 * ## 推荐用法：Sealed Class `when` + `diffUpdate` 局部刷新
 *
 * ```kotlin
 * sealed class DetailUiState : UiState {
 *     data object Loading : DetailUiState()
 *     data class Error(val errorMsg: String) : DetailUiState()
 *     data class Success(val data: DetailData) : DetailUiState()
 * }
 *
 * @AndroidEntryPoint
 * class DetailActivity : MviActivity() {
 *
 *     private val binding = ActivityDetailBinding.inflate(layoutInflater)
 *     override fun createBinding() = binding
 *
 *     @Inject lateinit var vm: DetailViewModel
 *     override val viewModel: MviViewModel<*> get() = vm
 *
 *     override fun render(state: UiState) {
 *         when (val s = state) {
 *             is DetailUiState.Loading -> binding.progressBar.isVisible = true
 *             is DetailUiState.Error   -> showErrorView(s.errorMsg)
 *             is DetailUiState.Success -> {
 *                 binding.progressBar.isVisible = false
 *                 // diffUpdate 比 previousState 粒度更细：每个 View 独立 diff
 *                 binding.tvTitle.diffUpdate(s.data.headerState.title) { text = it }
 *                 binding.ivAvatar.diffUpdate(s.data.headerState.avatarUrl) { load(it) }
 *                 binding.rvComment.diffUpdate(s.data.commentState.list) { submitList(it) }
 *             }
 *         }
 *     }
 *
 *     override fun handleEffect(effect: UiEffect) {
 *         when (effect) {
 *             is DetailUiEffect.ShowToast -> showToast(effect.message)
 *             is DetailUiEffect.NavigateTo -> startActivity(...)
 *         }
 *     }
 * }
 * ```
 */
abstract class BaseActivity : AppCompatActivity() {

    // ── 子类必须提供 ────────────────────────────────────────────────────────

    /**
     * 子类实现: 创建 ViewBinding。
     */
    protected abstract val binding: ViewBinding

    /** 关联的 MVI ViewModel。 */
    protected abstract val viewModel: BaseViewModel<*>

    // ── State 追踪 ──────────────────────────────────────────────────────────

    /** 当前 UI 状态快照（不订阅，只读）。 */
    @Suppress("UNCHECKED_CAST")
    val currentState: UiState
        get() = viewModel.uiState.value as UiState

    /**
     * 上一次 render 收到的状态。
     * 子类需要在跨子模块 diff 时可利用此字段（如 `previousState?.let { ... }`）。
     * 注意：[diffUpdate] 已经在 View 级别做 diff，多数场景不需要额外比较 previousState。
     */
    protected var previousState: UiState? = null
        private set

    // ── Lifecycle ───────────────────────────────────────────────────────────

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        Timber.d("[MviActivity] ${this::class.simpleName} onCreate")
        initView()
        initObservers()
    }

    // ── 模板方法 ────────────────────────────────────────────────────────────

    /**
     * 一次性 UI 初始化（设置 Adapter、Toolbar、ClickListener 等）。
     * 在 [onCreate] 中 [setContentView] 之后调用。
     */
    protected open fun initView() {}

    /**
     * 根据 [state] 驱动 UI 渲染。
     * 每次 [viewModel.uiState] 变化时在 [Lifecycle.State.STARTED] 状态下调用。
     *
     * 推荐用 `when` 处理 Sealed Class 宏观状态，再用 [diffUpdate] 做 View 级局部刷新。
     */
    protected open fun render(state: UiState) {}

    /**
     * 处理一次性副作用（导航、Toast、Snackbar 等）。
     */
    protected open fun handleEffect(effect: UiEffect) {
        Timber.d("[MviActivity] unhandled effect: ${effect::class.simpleName}")
    }

    // ── Observer ────────────────────────────────────────────────────────────

    /**
     * 启动 State 和 Effect 的订阅。
     *
     * State 使用 [collect]（非 collectLatest），保证 Sealed Class 每一次状态
     * 转换（Loading→Error→Success）都被 [render] 处理。
     */
    @CallSuper
    protected open fun initObservers() {
        // State：collect 不丢中间状态
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    Timber.v("[MviActivity] render ${state::class.simpleName}")
                    @Suppress("UNCHECKED_CAST")
                    render(state as UiState)
                    previousState = state as UiState
                }
            }
        }

        // Effect：一次性消费
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiEffect.collect { effect ->
                    Timber.d("[MviActivity] effect ${effect::class.simpleName}")
                    handleEffect(effect)
                }
            }
        }
    }
}
