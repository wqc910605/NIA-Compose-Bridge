package com.nia.compose.bridge.feature.topic.impl

import android.os.Bundle
import androidx.activity.viewModels
import com.nia.compose.bridge.core.mvi.UiEffect
import com.nia.compose.bridge.core.viewbinding.BaseActivity
import com.nia.compose.bridge.core.viewbinding.viewBinding
import com.nia.compose.bridge.feature.topic.api.TopicRoute
import com.nia.compose.bridge.feature.topic.api.databinding.ActivityTopicDetailBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TopicDetailActivity : BaseActivity() {

    override val binding by viewBinding(ActivityTopicDetailBinding::inflate)

    override val viewModel by viewModels<TopicViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val itemId = intent.getStringExtra(TopicRoute.EXTRA_ITEM_ID)
        itemId?.let { viewModel.loadTopic(it) }
    }

    override fun initView() {
        binding.toolbar.setNavigationOnClickListener {
            viewModel.navigateBack()
        }
    }

    override fun render(state: UiState) {
        when (state) {
            is TopicUiState.Loading -> {
                binding.progressBar.visibility = android.view.View.VISIBLE
                binding.contentLayout.visibility = android.view.View.GONE
                binding.tvNotFound.visibility = android.view.View.GONE
            }
            is TopicUiState.NotFound -> {
                binding.progressBar.visibility = android.view.View.GONE
                binding.contentLayout.visibility = android.view.View.GONE
                binding.tvNotFound.visibility = android.view.View.VISIBLE
            }
            is TopicUiState.Success -> {
                binding.progressBar.visibility = android.view.View.GONE
                binding.tvNotFound.visibility = android.view.View.GONE
                binding.contentLayout.visibility = android.view.View.VISIBLE
                binding.toolbar.title = state.data.item.title
                binding.tvTitle.text = state.data.item.title
                binding.tvDescription.text = state.data.item.description
            }
        }
    }

    override fun handleEffect(effect: UiEffect) {
        when (effect) {
            is TopicEffect.NavigateBack -> {
                finish()
            }
        }
    }
}
