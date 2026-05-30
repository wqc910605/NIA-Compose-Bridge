package com.nia.compose.bridge.common

import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * 生命周期感知的 Flow 收集扩展。
 *
 * 在 [Lifecycle.State.STARTED] 时收集，页面退到后台后自动暂停，回到前台继续。
 */
fun <T> LifecycleOwner.collectFlow(
    flow: Flow<T>,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    collector: suspend (T) -> Unit,
) {
    lifecycleScope.launch {
        repeatOnLifecycle(minActiveState) {
            flow.collect { collector(it) }
        }
    }
}

/**
 * [Fragment] 专用扩展，默认以 [viewLifecycleOwner] 作为生命周期宿主。
 */
fun <T> Fragment.collectFlow(
    flow: Flow<T>,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    collector: suspend (T) -> Unit,
) {
    viewLifecycleOwner.collectFlow(flow, minActiveState, collector)
}
