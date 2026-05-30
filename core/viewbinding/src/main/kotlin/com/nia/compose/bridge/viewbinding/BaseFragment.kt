package com.nia.compose.bridge.viewbinding

import android.os.Bundle
import android.view.View
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewbinding.ViewBinding
import com.nia.compose.bridge.common.BaseViewModel
import com.nia.compose.bridge.common.UiEffect
import com.nia.compose.bridge.common.UiState
import kotlinx.coroutines.launch

/**
 * ViewBinding Fragment 基类。
 *
 * 基类管控 ViewBinding 生命周期：在 [onViewCreated] 时就绪，[onDestroyView] 时自动置空。
 * 子类只需提供 [binding]、[viewModel]，实现 [render] 即可。
 */
abstract class BaseFragment(@LayoutRes contentLayoutId: Int) : Fragment(contentLayoutId) {

    // ── Binding（基类管控生命周期）──────────────────────────────────────────

    protected abstract val binding: ViewBinding

    protected abstract val viewModel: BaseViewModel<*>

    @Suppress("UNCHECKED_CAST")
    protected val currentState: UiState
        get() = viewModel.uiState.value as UiState

    protected var previousState: UiState? = null
        private set

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initObservers()
    }

    protected open fun initView() {}

    protected open fun render(state: UiState) {}

    protected open fun handleEffect(effect: UiEffect) {}

    @CallSuper
    protected open fun initObservers() {
        val viewLifecycle = viewLifecycleOwner
        viewLifecycle.lifecycleScope.launch {
            viewLifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    @Suppress("UNCHECKED_CAST")
                    render(state as UiState)
                    previousState = state as UiState
                }
            }
        }
        viewLifecycle.lifecycleScope.launch {
            viewLifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiEffect.collect { effect ->
                    handleEffect(effect)
                }
            }
        }
    }
}
