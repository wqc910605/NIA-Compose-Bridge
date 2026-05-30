package com.nia.compose.bridge.feature.interests.impl

import android.widget.Toast
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.nia.compose.bridge.core.mvi.UiEffect
import com.nia.compose.bridge.core.viewbinding.BaseActivity
import com.nia.compose.bridge.core.viewbinding.viewBinding
import com.nia.compose.bridge.feature.interests.api.databinding.ActivityInterestsBinding
import com.google.android.material.chip.Chip
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class InterestsActivity : BaseActivity() {

    override val binding by viewBinding(ActivityInterestsBinding::inflate)

    override val viewModel by viewModels<InterestsViewModel>()

    override fun initView() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    override fun render(state: UiState) {
        when (state) {
            is InterestsUiState.Loading -> {
                binding.progressBar.visibility = android.view.View.VISIBLE
                binding.chipGroup.visibility = android.view.View.GONE
            }
            is InterestsUiState.Empty -> {
                binding.progressBar.visibility = android.view.View.GONE
                binding.chipGroup.visibility = android.view.View.GONE
                binding.tvEmpty.visibility = android.view.View.VISIBLE
            }
            is InterestsUiState.Success -> {
                binding.progressBar.visibility = android.view.View.GONE
                binding.tvEmpty.visibility = android.view.View.GONE
                binding.chipGroup.visibility = android.view.View.VISIBLE

                binding.chipGroup.removeAllViews()
                state.data.items.forEach { item ->
                    val chip = Chip(this).apply {
                        text = item.item.title
                        isCheckable = true
                        isChecked = item.isFollowed
                        setOnCheckedChangeListener { _, isChecked ->
                            viewModel.toggleFollow(item.item.id)
                        }
                    }
                    binding.chipGroup.addView(chip)
                }
            }
        }
    }

    override fun handleEffect(effect: UiEffect) {
        when (effect) {
            is InterestsEffect.ShowToast -> {
                Toast.makeText(this, effect.message, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
