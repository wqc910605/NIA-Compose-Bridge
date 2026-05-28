package com.empty.android.core.viewbinding

import android.os.Bundle
import android.view.View
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
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
 * ViewBinding DialogFragment 基类。
 *
 * 与 [BaseFragment] 保持一致的 API 设计。
 */
abstract class BaseDialogFragment(@LayoutRes contentLayoutId: Int) : DialogFragment(contentLayoutId) {

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
