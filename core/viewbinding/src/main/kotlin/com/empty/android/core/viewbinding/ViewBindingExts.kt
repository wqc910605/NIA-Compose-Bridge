package com.empty.android.core.viewbinding

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.viewbinding.ViewBinding
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

fun <VB : ViewBinding> AppCompatActivity.viewBinding(
    inflate: (LayoutInflater) -> VB,
): ReadOnlyProperty<AppCompatActivity, VB> = ActivityViewBindingDelegate(inflate)

private class ActivityViewBindingDelegate<VB : ViewBinding>(
    private val bind: (LayoutInflater) -> VB,
) : ReadOnlyProperty<AppCompatActivity, VB> {

    private var _binding: VB? = null

    override fun getValue(thisRef: AppCompatActivity, property: KProperty<*>): VB {
        _binding?.let { return it }

        return bind(thisRef.layoutInflater).also { binding ->
            _binding = binding
            // 在 viewLifecycleOwner DESTROYED 时清空引用，防止内存泄漏
            thisRef.lifecycle.addObserver(object : DefaultLifecycleObserver {
                override fun onDestroy(owner: LifecycleOwner) {
                    _binding = null
                }
            })
        }
    }
}

/**
 * Fragment ViewBinding 属性委托。
 *
 * 解决 Fragment 视图生命周期短于 Fragment 对象生命周期导致的内存泄漏问题：
 * 在 [Fragment.onDestroyView] 时自动将绑定引用清空。
 *
 * ## 用法
 * ```kotlin
 * class HomeFragment : Fragment(R.layout.fragment_home) {
 *
 *     // 无需手动 inflate，直接用 viewBinding 委托
 *     private val binding by viewBinding(FragmentHomeBinding::bind)
 *
 *     override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
 *         super.onViewCreated(view, savedInstanceState)
 *         binding.tvTitle.diffUpdate(viewModel.title) { text = it }
 *     }
 * }
 * ```
 *
 * @param bind 通常传入生成的 `XxxBinding::bind` 方法引用
 */
fun <VB : ViewBinding> Fragment.viewBinding(
    bind: (View) -> VB,
): ReadOnlyProperty<Fragment, VB> = FragmentViewBindingDelegate(bind)

private class FragmentViewBindingDelegate<VB : ViewBinding>(
    private val bind: (View) -> VB,
) : ReadOnlyProperty<Fragment, VB> {

    private var _binding: VB? = null

    override fun getValue(thisRef: Fragment, property: KProperty<*>): VB {
        _binding?.let { return it }

        val view = checkNotNull(thisRef.view) {
            "Cannot access ViewBinding before onCreateView() or after onDestroyView()."
        }
        return bind(view).also { binding ->
            _binding = binding
            // 在 viewLifecycleOwner DESTROYED 时清空引用，防止内存泄漏
            thisRef.viewLifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
                override fun onDestroy(owner: LifecycleOwner) {
                    _binding = null
                }
            })
        }
    }
}

/**
 * 在 [ViewGroup] 中 inflate ViewBinding 的便捷扩展。
 *
 * ```kotlin
 * // Adapter.onCreateViewHolder
 * val binding = parent.inflate(ItemDemoBinding::inflate)
 * ```
 */
fun <VB : ViewBinding> ViewGroup.viewBinding(
    inflater: (LayoutInflater, ViewGroup, Boolean) -> VB,
    attachToParent: Boolean = false,
): VB = inflater(LayoutInflater.from(context), this, attachToParent)
