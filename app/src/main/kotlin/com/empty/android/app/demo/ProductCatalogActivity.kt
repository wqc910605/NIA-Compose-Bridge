package com.empty.android.app.demo

import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.empty.android.app.R
import com.empty.android.app.databinding.ActivityProductCatalogBinding
import com.empty.android.app.demo.adapter.ProductAdapter
import com.empty.android.core.mvi.UiEffect
import com.empty.android.core.mvi.UiState
import com.empty.android.core.viewbinding.BaseActivity
import com.empty.android.core.viewbinding.diffUpdate
import com.empty.android.core.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint

/**
 * 商品目录 Demo Activity。
 *
 * ## 演示清单
 * | 特性 | 对应代码 |
 * |------|----------|
 * | [BaseActivity] 继承 | `class ProductCatalogActivity : BaseActivity()` |
 * | [createBinding] | 返回 `ActivityProductCatalogBinding` |
 * | [viewModel] | Hilt 注入 `ProductCatalogViewModel` |
 * | [initView] | 设置 Adapter + LayoutManager + 事件绑定 |
 * | [render] + `when` 宏观状态 | Loading / Error / Success 三种视图切换 |
 * | [diffUpdate] 局部刷新 | `tvPageTitle.diffUpdate(...)` |
 * | [handleEffect] | 处理 `ShowToast` / `NavigateToDetail` |
 * | 多类型列表 | [ProductAdapter]（4 种 itemViewType） |
 * | Fragment.viewBinding() | 点击商品 → [ProductDetailFragment] 弹详情 |
 *
 * ## 启动方式
 * ```kotlin
 * startActivity(Intent(this, ProductCatalogActivity::class.java))
 * ```
 */
@AndroidEntryPoint
class ProductCatalogActivity : BaseActivity() {

    // ── ViewBinding ──────────────────────────────────────────────────────────

    /** 子类保存 typed binding 引用，可直接访问 `binding.tvPageTitle` 等。 */
    override val binding by viewBinding(ActivityProductCatalogBinding::inflate)

    // ── ViewModel ────────────────────────────────────────────────────────────
    override val viewModel by viewModels<ProductCatalogViewModel>()

    // ── Adapter ──────────────────────────────────────────────────────────────

    private val adapter = ProductAdapter(
        onProductClick = { viewModel.onProductClicked(it) },
        onBannerClick = { viewModel.onBannerClicked(it) },
    )

    // ── Lifecycle ────────────────────────────────────────────────────────────

    /**
     * 一次性 UI 初始化。
     *
     * 在 [BaseActivity.onCreate] 中 [setContentView] 之后、[initObservers] 之前调用。
     */
    override fun initView() {
        // RecyclerView 基础配置
        binding.rvCatalog.adapter = adapter
        binding.rvCatalog.layoutManager = LinearLayoutManager(this)

        // Error 视图的重试按钮
        binding.btnRetry.setOnClickListener { viewModel.loadCatalog() }

        // SwipeRefresh 下拉刷新
        binding.swipeRefresh.setOnRefreshListener { viewModel.loadCatalog() }

        // Fragment 回退栈监听 —— 当详情 Fragment pop 后隐藏容器
        supportFragmentManager.addOnBackStackChangedListener {
            if (supportFragmentManager.backStackEntryCount == 0) {
                binding.fragmentContainer.visibility = View.GONE
            }
        }

        // 启动首次数据加载
        viewModel.loadCatalog()
    }

    // ── 模板方法 ────────────────────────────────────────────────────────────

    /**
     * 状态驱动的 UI 渲染。
     *
     * 每次 [viewModel.uiState] 变化时调用。用 `when` 处理 Sealed Class 宏观互斥状态：
     * - [ProductCatalogUiState.Loading] → 显示 SwipeRefresh loading
     * - [ProductCatalogUiState.Error]   → 显示错误视图 + 错误信息
     * - [ProductCatalogUiState.Success] → 显示列表 + 用 [diffUpdate] 局部刷标题
     *
     * 注意：列表数据通过 [adapter.submitList] 更新，
     * 由 [ProductDiffCallback] 驱动 DiffUtil 计算差异动画。
     */
    override fun render(state: UiState) {
        when (val s = state) {
            is ProductCatalogUiState.Loading -> {
                binding.swipeRefresh.isRefreshing = true
                binding.errorView.visibility = View.GONE
                binding.swipeRefresh.visibility = View.GONE
            }
            is ProductCatalogUiState.Error -> {
                binding.swipeRefresh.isRefreshing = false
                binding.swipeRefresh.visibility = View.GONE
                // diffUpdate：只有错误文案变化时才 setText
                binding.tvError.diffUpdate(s.message) { text = it }
                binding.errorView.visibility = View.VISIBLE
            }
            is ProductCatalogUiState.Success -> {
                binding.swipeRefresh.isRefreshing = false
                binding.errorView.visibility = View.GONE
                binding.swipeRefresh.visibility = View.VISIBLE

                // 标题用 diffUpdate 局部刷新（只在值变化时更新）
                binding.tvPageTitle.diffUpdate(s.data.pageTitle) { text = it }

                // 列表数据通过 submitList → DiffUtil 自动计算差异
                adapter.submitList(s.data.items)
            }
        }
    }

    /**
     * 一次性副作用处理。
     *
     * 每次 [viewModel.emitEffect] 后触发一次，不缓存不重播。
     */
    override fun handleEffect(effect: UiEffect) {
        when (effect) {
            is ProductCatalogEffect.ShowToast -> {
                Toast.makeText(this, effect.message, Toast.LENGTH_SHORT).show()
            }
            is ProductCatalogEffect.NavigateToDetail -> {
                // 加载 ProductDetailFragment 到 fragmentContainer
                // 此处演示 Fragment.viewBinding() 委托的用法
                val fragment = ProductDetailFragment.newInstance(
                    productId = effect.productId,
                    productName = effect.productName,
                    productPrice = "\$99.99", // 模拟价格
                )
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .addToBackStack(null)
                    .commit()
                // 显示 Fragment 容器，覆盖主内容
                binding.fragmentContainer.visibility = View.VISIBLE
            }
        }
    }
}
