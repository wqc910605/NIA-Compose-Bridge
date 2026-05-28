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
 * ViewBinding Activity 基类。
 *
 * 封装 ViewBinding 设置、State/Effect 订阅等通用逻辑。
 * 子类只需提供 [binding]、[viewModel]，实现 [render] 即可。
 */
abstract class BaseActivity : AppCompatActivity() {

    // ── 子类必须提供 ────────────────────────────────────────────────────────

    protected abstract val binding: ViewBinding

    protected abstract val viewModel: BaseViewModel<*>

    // ── State 追踪 ──────────────────────────────────────────────────────────

    @Suppress("UNCHECKED_CAST")
    val currentState: UiState
        get() = viewModel.uiState.value as UiState

    protected var previousState: UiState? = null
        private set

    // ── Lifecycle ───────────────────────────────────────────────────────────

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initView()
        initObservers()
    }

    // ── 模板方法 ────────────────────────────────────────────────────────────

    protected open fun initView() {}

    protected open fun render(state: UiState) {}

    protected open fun handleEffect(effect: UiEffect) {}

    // ── Observer ────────────────────────────────────────────────────────────

    @CallSuper
    protected open fun initObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    @Suppress("UNCHECKED_CAST")
                    render(state as UiState)
                    previousState = state as UiState
                }
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiEffect.collect { effect ->
                    handleEffect(effect)
                }
            }
        }
    }
}
