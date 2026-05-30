package com.nia.compose.bridge.home.impl

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nia.compose.bridge.model.DemoItem
import com.nia.compose.bridge.feature.home.impl.databinding.ItemDemoBinding

class HomeAdapter(
    private val onItemClick: (DemoItem) -> Unit,
    private val onItemLongClick: (DemoItem) -> Unit,
) : ListAdapter<DemoItem, HomeAdapter.HomeViewHolder>(DemoItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeViewHolder {
        val binding = ItemDemoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false,
        )
        return HomeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HomeViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class HomeViewHolder(
        private val binding: ItemDemoBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: DemoItem) {
            binding.tvTitle.text = item.title
            binding.tvDescription.text = item.description
            binding.root.setOnClickListener { onItemClick(item) }
            binding.root.setOnLongClickListener {
                onItemLongClick(item)
                true
            }
        }
    }

    private class DemoItemDiffCallback : DiffUtil.ItemCallback<DemoItem>() {
        override fun areItemsTheSame(oldItem: DemoItem, newItem: DemoItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: DemoItem, newItem: DemoItem): Boolean {
            return oldItem == newItem
        }
    }
}
