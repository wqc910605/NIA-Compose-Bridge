package com.empty.android.core.viewbinding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
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
 * ViewBinding + MVI 架构的 Fragment 基类（无泛型）。
 *
 * 基类全权管控 ViewBinding 生命周期：在 [onCreateView] 中通过 [inflateBinding] 创建，
 * 在 [onDestroyView] 中置空。**子类无需关心内存泄漏问题**。
 *
 * 使用 [collect]（非 collectLatest），确保 Sealed Class 宏观状态的每一次转换
 * 都被 [render] 处理，不漏过 Loading→Error→Success 等过渡。
 *
 * 子类只需：
 * 1. 实现 [inflateBinding]（返回 ViewBinding，子类另存 typed 字段即可）
 * 2. 提供 [viewModel]
 * 3. 实现 [render]（State → UI），推荐用 `when` 处理 Sealed Class
 * 4. 可选覆写 [handleEffect]、[initView]
 *
 * ## 推荐用法：Sealed Class `when` + `diffUpdate` 局部刷新
 * ```kotlin
 * @AndroidEntryPoint
 * class HomeFragment : MviFragment() {
 *
 *     private lateinit var binding: FragmentHomeBinding
 *     override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
 *         FragmentHomeBinding.inflate(inflater, container, false).also { binding = it }
 *
 *     @Inject lateinit var vm: HomeViewModel
 *     override val viewModel: MviViewModel<*> get() = vm
 *
 *     override fun initView() {
 *         binding.recyclerView.adapter = myAdapter
 *     }
 *
 *     override fun render(state: UiState) {
 *         when (val s = state) {
 *             is HomeUiState.Loading -> binding.progressBar.isVisible = true
 *             is HomeUiState.Error   -> showErrorView(s.errorMsg)
 *             is HomeUiState.Success -> {
 *                 binding.progressBar.isVisible = false
 *                 binding.tvTitle.diffUpdate(s.data.title) { text = it }
 *                 binding.rvList.diffUpdate(s.data.items) { submitList(it) }
 *             }
 *         }
 *     }
 *
 *     override fun handleEffect(effect: UiEffect) {
 *         when (effect) {
 *             is HomeEffect.ShowSnackbar -> showSnackbar(effect.msg)
 *             is HomeEffect.Navigate -> findNavController().navigate(effect.dest)
 *         }
 *     }
 * }
 * ```
 */
abstract class BaseFragment(@LayoutRes contentLayoutId: Int) : Fragment(contentLayoutId) {

    // ── Binding（基类管控生命周期）──────────────────────────────────────────

    /**
     * 子类实现: 创建 ViewBinding。
     */
    protected abstract val binding: ViewBinding

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


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.d("[MviFragment] ${this::class.simpleName} onViewCreated")
        initView()
        initObservers()
    }

    // ── 模板方法 ────────────────────────────────────────────────────────────

    /**
     * 一次性 UI 初始化（设置 Adapter、ClickListener 等）。
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
     * 处理一次性副作用（导航、Toast、Snackbar 等）。
     *
     * 默认仅打日志，子类按需覆写。
     */
    protected open fun handleEffect(effect: UiEffect) {
        Timber.d("[MviFragment] unhandled effect: ${effect::class.simpleName}")
    }

    // ── Observer ────────────────────────────────────────────────────────────

    /**
     * 启动 State 和 Effect 的订阅。
     *
     * 以 [viewLifecycleOwner] 为宿主，确保 Fragment 视图销毁时自动取消。
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
                    Timber.v("[MviFragment] render ${state::class.simpleName}")
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
                    Timber.d("[MviFragment] effect ${effect::class.simpleName}")
                    handleEffect(effect)
                }
            }
        }
    }
}
