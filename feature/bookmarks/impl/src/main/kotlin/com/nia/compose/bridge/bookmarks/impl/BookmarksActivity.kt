package com.nia.compose.bridge.bookmarks.impl

import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import com.nia.compose.bridge.viewbinding.BaseActivity
import com.nia.compose.bridge.viewbinding.viewBinding
import com.nia.compose.nia.bookmarks.impl.databinding.ActivityBookmarksBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BookmarksActivity : BaseActivity() {

    override val binding by viewBinding(ActivityBookmarksBinding::inflate)

    override val viewModel by viewModels<BookmarksViewModel>()

    private val adapter = HomeAdapter(
        onItemClick = { item ->
            Toast.makeText(this, "点击: ${item.title}", Toast.LENGTH_SHORT).show()
        },
        onItemLongClick = { item ->
            viewModel.removeBookmark(item.id)
        },
    )

    override fun initView() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }

    override fun render(state: UiState) {
        when (state) {
            is BookmarksUiState.Loading -> {
                binding.progressBar.visibility = View.VISIBLE
                binding.recyclerView.visibility = View.GONE
                binding.emptyView.visibility = View.GONE
            }
            is BookmarksUiState.Empty -> {
                binding.progressBar.visibility = View.GONE
                binding.recyclerView.visibility = View.GONE
                binding.emptyView.visibility = View.VISIBLE
            }
            is BookmarksUiState.Success -> {
                binding.progressBar.visibility = View.GONE
                binding.recyclerView.visibility = View.VISIBLE
                binding.emptyView.visibility = View.GONE
                adapter.submitList(state.data.items)
            }
        }
    }

    override fun handleEffect(effect: UiEffect) {
        when (effect) {
            is BookmarksEffect.ShowToast -> {
                Toast.makeText(this, effect.message, Toast.LENGTH_SHORT).show()
            }
            is BookmarksEffect.ShowUndoSnackbar -> {
                Snackbar.make(binding.root, effect.message, Snackbar.LENGTH_LONG)
                    .setAction("撤销") {
                        viewModel.undoRemove(effect.itemId)
                    }
                    .show()
            }
        }
    }
}
