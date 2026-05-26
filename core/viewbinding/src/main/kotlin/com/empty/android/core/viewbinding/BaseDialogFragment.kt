package com.empty.android.core.viewbinding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.fragment.app.DialogFragment
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
 * ViewBinding + MVI 架构的 DialogFragment 基类（无泛型）。
 *
 * 与 [BaseFragment] 保持一致的 API 设计。基类全权管控 ViewBinding 生命周期：
 * 在 [onCreateView] 中通过 [inflateBinding] 创建，在 [onDestroyView] 中置空。
 * **子类无需关心内存泄漏问题**。
 *
 * 使用 [collect]（非 collectLatest），确保 Sealed Class 宏观状态的每一次转换
 * 都被 [render] 处理，不漏过 Loading→Error→Success 等过渡。
 *
 * ## 推荐用法：Sealed Class `when`
 * ```kotlin
 * @AndroidEntryPoint
 * class ConfirmDialog : MviDialogFragment() {
 *
 *     private lateinit var binding: DialogConfirmBinding
 *     override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
 *         DialogConfirmBinding.inflate(inflater, container, false).also { binding = it }
 *
 *     @Inject lateinit var vm: ConfirmViewModel
 *     override val viewModel: MviViewModel<*> get() = vm
 *
 *     override fun onStart() {
 *         super.onStart()
 *         dialog?.window?.setLayout(
 *             (resources.displayMetrics.widthPixels * 0.85).toInt(),
 *             ViewGroup.LayoutParams.WRAP_CONTENT,
 *         )
 *     }
 *
 *     override fun render(state: UiState) {
 *         when (val s = state) {
 *             is ConfirmUiState.Loading -> binding.progressBar.isVisible = true
 *             is ConfirmUiState.Success -> {
 *                 binding.progressBar.isVisible = false
 *                 binding.tvMessage.diffUpdate(s.data.message) { text = it }
 *                 binding.btnConfirm.diffUpdate(s.data.isLoading) { isEnabled = !it }
 *             }
 *         }
 *     }
 *
 *     override fun handleEffect(effect: UiEffect) {
 *         when (effect) {
 *             is ConfirmEffect.Dismiss -> dismiss()
 *         }
 *     }
 * }
 * ```
 */
abstract class BaseDialogFragment : DialogFragment() {

    // ── Binding（基类管控生命周期）──────────────────────────────────────────

    /**
     * 子类实现: 创建 ViewBinding。
     */
    protected abstract val mBinding: ViewBinding

    // ── 子类必须提供 ────────────────────────────────────────────────────────

    /** 关联的 MVI ViewModel。 */
    protected abstract val viewModel: BaseViewModel<*>

    // ── State 追踪 ──────────────────────────────────────────────────────────

    /** 获取当前 UI 状态快照（不订阅，只读）。 */
    @Suppress("UNCHECKED_CAST")
    protected val currentState: UiState
        get() = viewModel.uiState.value as UiState

    /**
     * 上一次 render 收到的状态。
     * 子类需要在跨子模块 diff 时可利用此字段（如 `previousState?.let { ... }`）。
     * 注意：[diffUpdate] 已经在 View 级别做 diff，多数场景不需要额外比较 previousState。
     */
    protected var previousState: UiState? = null
        private set

    // ── Lifecycle ───────────────────────────────────────────────────────────

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.d("[MviDialogFragment] ${this::class.simpleName} onViewCreated")
        initView()
        initObservers()
    }

    // ── 模板方法 ────────────────────────────────────────────────────────────

    /**
     * 一次性 UI 初始化（设置 ClickListener 等）。
     * 在 [onViewCreated] 中 ViewBinding 就绪后调用。
     *
     * 默认空实现，子类按需覆写。
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
     * 处理一次性副作用（关闭弹窗、Toast 等）。
     *
     * 默认仅打日志，子类按需覆写。
     */
    protected open fun handleEffect(effect: UiEffect) {
        Timber.d("[MviDialogFragment] unhandled effect: ${effect::class.simpleName}")
    }

    // ── Observer ────────────────────────────────────────────────────────────

    /**
     * 启动 State 和 Effect 的订阅。
     *
     * 以 [viewLifecycleOwner] 为宿主，确保 DialogFragment 视图销毁时自动取消。
     *
     * State 使用 [collect]（非 collectLatest），保证 Sealed Class 每一次状态
     * 转换（Loading→Error→Success）都被 [render] 处理。
     *
     * 子类若需扩展观察者逻辑，请先调用 `super.initObservers()`。
     */
    @CallSuper
    protected open fun initObservers() {
        val viewLifecycle = viewLifecycleOwner

        // State：collect 不丢中间状态
        viewLifecycle.lifecycleScope.launch {
            viewLifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    Timber.v("[MviDialogFragment] render ${state::class.simpleName}")
                    @Suppress("UNCHECKED_CAST")
                    render(state as UiState)
                    previousState = state as UiState
                }
            }
        }

        // Effect：一次性消费
        viewLifecycle.lifecycleScope.launch {
            viewLifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiEffect.collect { effect ->
                    Timber.d("[MviDialogFragment] effect ${effect::class.simpleName}")
                    handleEffect(effect)
                }
            }
        }
    }
}
