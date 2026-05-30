package com.nia.compose.bridge.feature.search.impl

import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.nia.compose.bridge.core.mvi.UiEffect
import com.nia.compose.bridge.core.viewbinding.BaseActivity
import com.nia.compose.bridge.core.viewbinding.viewBinding
import com.nia.compose.bridge.feature.home.impl.HomeAdapter
import com.nia.compose.bridge.feature.search.api.databinding.ActivitySearchBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SearchActivity : BaseActivity() {

    override val binding by viewBinding(ActivitySearchBinding::inflate)

    override val viewModel by viewModels<SearchViewModel>()

    private val adapter = HomeAdapter(
        onItemClick = { item ->
            Toast.makeText(this, "点击: ${item.title}", Toast.LENGTH_SHORT).show()
        },
        onItemLongClick = { },
    )

    override fun initView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        binding.etSearch.addTextChangedListener { editable ->
            viewModel.onSearchQueryChanged(editable?.toString() ?: "")
        }

        binding.btnClear.setOnClickListener {
            binding.etSearch.text?.clear()
            viewModel.clearSearch()
        }
    }

    override fun render(state: UiState) {
        when (state) {
            is SearchUiState.Idle -> {
                binding.progressBar.visibility = android.view.View.GONE
                binding.recyclerView.visibility = android.view.View.GONE
                binding.emptyView.visibility = android.view.View.GONE
            }
            is SearchUiState.Searching -> {
                binding.progressBar.visibility = android.view.View.VISIBLE
                binding.recyclerView.visibility = android.view.View.GONE
                binding.emptyView.visibility = android.view.View.GONE
            }
            is SearchUiState.Empty -> {
                binding.progressBar.visibility = android.view.View.GONE
                binding.recyclerView.visibility = android.view.View.GONE
                binding.emptyView.visibility = android.view.View.VISIBLE
                binding.tvEmpty.text = "未找到与 \"${state.query}\" 相关的结果"
            }
            is SearchUiState.Success -> {
                binding.progressBar.visibility = android.view.View.GONE
                binding.recyclerView.visibility = android.view.View.VISIBLE
                binding.emptyView.visibility = android.view.View.GONE
                adapter.submitList(state.data.results)
            }
            is SearchUiState.Error -> {
                binding.progressBar.visibility = android.view.View.GONE
                binding.recyclerView.visibility = android.view.View.GONE
                binding.emptyView.visibility = android.view.View.VISIBLE
                binding.tvEmpty.text = state.message ?: "搜索失败"
            }
        }
    }

    override fun handleEffect(effect: UiEffect) {
        when (effect) {
            is SearchEffect.ShowToast -> {
                Toast.makeText(this, effect.message, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
