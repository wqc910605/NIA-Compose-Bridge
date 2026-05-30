package com.nia.compose.bridge.home.impl

import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.nia.compose.bridge.common.UiEffect
import com.nia.compose.bridge.common.UiState
import com.nia.compose.bridge.viewbinding.BaseActivity
import com.nia.compose.bridge.core.viewbinding.viewBinding
import com.nia.compose.bridge.feature.bookmarks.api.BookmarksRoute
import com.nia.compose.bridge.feature.home.impl.R
import com.nia.compose.bridge.feature.home.impl.databinding.ActivityHomeBinding
import com.nia.compose.bridge.feature.interests.api.InterestsRoute
import com.nia.compose.bridge.feature.search.api.SearchRoute
import com.nia.compose.bridge.feature.settings.api.SettingsRoute
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeActivity : BaseActivity() {

    override val binding by viewBinding(ActivityHomeBinding::inflate)

    override val viewModel by viewModels<HomeViewModel>()

    private val adapter = HomeAdapter(
        onItemClick = { item ->
            Toast.makeText(this, "点击: ${item.title}", Toast.LENGTH_SHORT).show()
        },
        onItemLongClick = { item ->
            viewModel.remove(item.id)
        },
    )

    override fun initView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_search -> {
                    viewModel.navigateToSearch()
                    true
                }
                R.id.action_refresh -> {
                    viewModel.refresh()
                    true
                }
                R.id.action_bookmarks -> {
                    viewModel.navigateToBookmarks()
                    true
                }
                R.id.action_interests -> {
                    viewModel.navigateToInterests()
                    true
                }
                R.id.action_settings -> {
                    viewModel.navigateToSettings()
                    true
                }
                else -> false
            }
        }

        binding.fabAdd.setOnClickListener {
            viewModel.addSample()
        }
    }

    override fun render(state: UiState) {
        when (state) {
            is HomeUiState.Loading -> {
                binding.progressBar.visibility = View.VISIBLE
                binding.recyclerView.visibility = View.GONE
                binding.emptyView.visibility = View.GONE
                binding.errorView.visibility = View.GONE
            }
            is HomeUiState.Empty -> {
                binding.progressBar.visibility = View.GONE
                binding.recyclerView.visibility = View.GONE
                binding.emptyView.visibility = View.VISIBLE
                binding.errorView.visibility = View.GONE
            }
            is HomeUiState.Error -> {
                binding.progressBar.visibility = View.GONE
                binding.recyclerView.visibility = View.GONE
                binding.emptyView.visibility = View.GONE
                binding.errorView.visibility = View.VISIBLE
                binding.tvError.text = state.message ?: "未知错误"
            }
            is HomeUiState.Success -> {
                binding.progressBar.visibility = View.GONE
                binding.recyclerView.visibility = View.VISIBLE
                binding.emptyView.visibility = View.GONE
                binding.errorView.visibility = View.GONE
                adapter.submitList(state.data.items)
            }
        }
    }

    override fun handleEffect(effect: UiEffect) {
        when (effect) {
            is HomeEffect.ShowToast -> {
                Toast.makeText(this, effect.message, Toast.LENGTH_SHORT).show()
            }
            is HomeEffect.NavigateToSettings -> {
                startActivity(SettingsRoute.intent(this))
            }
            is HomeEffect.NavigateToSearch -> {
                startActivity(SearchRoute.intent(this))
            }
            is HomeEffect.NavigateToBookmarks -> {
                startActivity(BookmarksRoute.intent(this))
            }
            is HomeEffect.NavigateToInterests -> {
                startActivity(InterestsRoute.intent(this))
            }
        }
    }
}
