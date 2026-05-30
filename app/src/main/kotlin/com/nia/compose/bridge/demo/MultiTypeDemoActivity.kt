package com.nia.compose.bridge.demo

import android.widget.Toast
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nia.compose.bridge.databinding.ActivityMultiTypeDemoBinding
import com.nia.compose.bridge.demo.adapter.MultiTypeDemoAdapter
import com.nia.compose.bridge.common.UiEffect
import com.nia.compose.bridge.common.UiState
import com.nia.compose.bridge.viewbinding.BaseActivity
import com.nia.compose.bridge.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MultiTypeDemoActivity : BaseActivity() {

    override val binding by viewBinding(ActivityMultiTypeDemoBinding::inflate)

    override val viewModel by viewModels<MultiTypeDemoViewModel>()

    private val adapter = MultiTypeDemoAdapter().apply {
        setOnItemClickListener { _, _, position ->
            val item = getItemOrNull(position) ?: return@setOnItemClickListener
            val msg = when (item) {
                is DemoItem.Banner -> item.title
                is DemoItem.ProductSmall -> item.name
                is DemoItem.ProductLarge -> item.name
                is DemoItem.Loading -> "加载中…"
            }
            Toast.makeText(this@MultiTypeDemoActivity, msg, Toast.LENGTH_SHORT).show()
        }
    }

    private val layoutManager = LinearLayoutManager(this)

    override fun initView() {
        binding.rvContent.layoutManager = layoutManager
        binding.rvContent.adapter = adapter
        binding.rvContent.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(
                recyclerView: RecyclerView,
                newState: Int,
            ) {
                if (newState != RecyclerView.SCROLL_STATE_IDLE) return
                val lastVisible = layoutManager.findLastVisibleItemPosition()
                if (lastVisible >= adapter.itemCount - 2) {
                    viewModel.loadMore()
                }
            }
        })

        viewModel.loadData()
    }

    override fun render(state: UiState) {
        if (state !is DemoUiState) return
        adapter.submitList(state.items)
    }

    override fun handleEffect(effect: UiEffect) {
        if (effect !is DemoEffect) return
        when (effect) {
            is DemoEffect.ShowToast ->
                Toast.makeText(this, effect.message, Toast.LENGTH_SHORT).show()
        }
    }
}
