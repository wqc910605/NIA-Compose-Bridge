package com.empty.android.app.demo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.viewbinding.ViewBinding
import com.empty.android.app.R
import com.empty.android.app.databinding.FragmentProductDetailBinding
import com.empty.android.core.mvi.BaseViewModel
import com.empty.android.core.viewbinding.BaseFragment
import com.empty.android.core.viewbinding.diffUpdate
import com.empty.android.core.viewbinding.viewBinding

/**
 * 商品详情 Fragment —— 演示 [Fragment.viewBinding] 扩展委托。
 *
 * ## 对比 BaseFragment
 *
 * | 特性 | BaseFragment | Fragment + viewBinding() |
 * |------|-------------|--------------------------|
 * | 基类约束 | 必须继承 BaseFragment | 任意 Fragment 子类 |
 * | 构造函数 | 不能传 layout（由 inflateBinding 提供） | 可用 `Fragment(R.layout.xxx)` |
 * | Binding 管理 | 基类 set/null | `viewBinding()` 委托自动 null |
 * | MVI 集成 | 内置 state/effect 订阅 | 需自行处理 |
 * | 适用场景 | MVI 页面的 Fragment | 简单 Fragment / DialogFragment / 非 MVI 页面 |
 *
 * ## Fragment.viewBinding() 委托原理
 *
 * ```kotlin
 * private val binding by viewBinding(FragmentProductDetailBinding::bind)
 * ```
 *
 * - 首次访问时从 `this.view` inflate binding（要求 `onCreateView` 已执行）
 * - 注册 `viewLifecycleOwner` 的 `onDestroy` 回调，到时将 binding 置 null
 * - 下次 `onCreateView` 后重新 inflate，新 binding 自动替换旧的
 * - **子类无需覆写 `onDestroyView` 手动置 null**
 *
 * ## 用法场景
 *
 * 当你不需要 MVI 架构、只想安全使用 ViewBinding 的 Fragment 时：
 * - 简单的静态内容页
 * - DialogFragment（配合 `onCreateDialog`）
 * - BottomSheetDialogFragment
 * - 只需要传参 + 展示，不需要 state/effect 订阅
 */
class ProductDetailFragment : BaseFragment(R.layout.fragment_product_detail) {

    // ═══════════════════════════════════════════════════════════════
    // Fragment.viewBinding() 委托 —— 自动管理生命周期
    // ═══════════════════════════════════════════════════════════════
    //
    // 关键点：
    // 1. 构造函数传入了 `R.layout.fragment_product_detail`，
    //    Fragment 会自动 inflate，无需覆写 onCreateView
    // 2. `viewBinding(FragmentProductDetailBinding::bind)` 使用生成类
    //    的 `bind(View)` 静态方法
    // 3. 委托在 `viewLifecycleOwner.onDestroy` 时自动将 binding 置 null
    //
    // 与手动管理的区别：
    // ```kotlin
    // // 手动模式（需要自己置 null）：
    // private var _binding: FragmentProductDetailBinding? = null
    // override fun onCreateView(...): View {
    //     _binding = FragmentProductDetailBinding.inflate(inflater, container, false)
    //     return _binding!!.root
    // }
    // override fun onDestroyView() {
    //     super.onDestroyView()
    //     _binding = null
    // }
    // ```
    //
    // // viewBinding() 委托（一行搞定）：
    // private val binding by viewBinding(FragmentProductDetailBinding::bind)
    // ```
    override val binding by viewBinding(FragmentProductDetailBinding::bind)

    override val viewModel by viewModels<ProductCatalogViewModel>()


    // ── 参数 key ─────────────────────────────────────────────────

    companion object {
        private const val ARG_PRODUCT_NAME = "productName"
        private const val ARG_PRODUCT_PRICE = "productPrice"
        private const val ARG_PRODUCT_ID = "productId"

        fun newInstance(
            productId: String,
            productName: String,
            productPrice: String,
        ): ProductDetailFragment = ProductDetailFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_PRODUCT_ID, productId)
                putString(ARG_PRODUCT_NAME, productName)
                putString(ARG_PRODUCT_PRICE, productPrice)
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // Lifecycle
    // ═══════════════════════════════════════════════════════════════

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 从 arguments 读取参数
        val productName = arguments?.getString(ARG_PRODUCT_NAME) ?: "Unknown"
        val productPrice = arguments?.getString(ARG_PRODUCT_PRICE) ?: "$0.00"

        // diffUpdate：只在值变化时 setText，避免无意义的重绘
        binding.tvProductName.diffUpdate(productName) { text = it }
        binding.tvProductPrice.diffUpdate(productPrice) { text = it }
        binding.tvStockStatus.diffUpdate("In Stock") { text = it }

        // 返回按钮 —— 直接 pop 自己
        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }
}
