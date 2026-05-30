package com.nia.compose.bridge.demo.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import com.nia.compose.bridge.demo.DemoItem
import com.nia.compose.bridge.demo.TYPE_BANNER
import com.nia.compose.bridge.demo.TYPE_LOADING
import com.nia.compose.bridge.demo.TYPE_PRODUCT_LARGE
import com.nia.compose.bridge.demo.TYPE_PRODUCT_SMALL
import com.nia.compose.bridge.databinding.ItemMtBannerBinding
import com.nia.compose.bridge.databinding.ItemMtLoadingBinding
import com.nia.compose.bridge.databinding.ItemMtProductLargeBinding
import com.nia.compose.bridge.databinding.ItemMtProductSmallBinding
import com.nia.compose.bridge.viewbinding.adapter.BaseMultiAdapter
import com.nia.compose.bridge.viewbinding.adapter.ItemViewBinder
import com.nia.compose.bridge.viewbinding.adapter.VBViewHolder

class MultiTypeDemoAdapter : BaseMultiAdapter<DemoItem>(diffCallback = DiffCallback) {

    init {
        addItemType(TYPE_BANNER, object : ItemViewBinder<DemoItem, ItemMtBannerBinding>() {
            override fun onCreateBinding(inflater: LayoutInflater, parent: ViewGroup) =
                ItemMtBannerBinding.inflate(inflater, parent, false)

            override fun onBind(
                holder: VBViewHolder<ItemMtBannerBinding>,
                position: Int,
                item: DemoItem?,
                payloads: List<Any>,
            ) {
                val banner = item as? DemoItem.Banner ?: return
                holder.binding.tvTitle.text = banner.title
                holder.binding.tvDesc.text = banner.desc
            }
        })

        addItemType(TYPE_PRODUCT_SMALL, object : ItemViewBinder<DemoItem, ItemMtProductSmallBinding>() {
            override fun onCreateBinding(inflater: LayoutInflater, parent: ViewGroup) =
                ItemMtProductSmallBinding.inflate(inflater, parent, false)

            override fun onBind(
                holder: VBViewHolder<ItemMtProductSmallBinding>,
                position: Int,
                item: DemoItem?,
                payloads: List<Any>,
            ) {
                val p = item as? DemoItem.ProductSmall ?: return
                holder.binding.tvIcon.text = p.icon
                holder.binding.tvName.text = p.name
                holder.binding.tvPrice.text = p.price
                holder.binding.tvUnit.text = p.unit
            }
        })

        addItemType(TYPE_PRODUCT_LARGE, object : ItemViewBinder<DemoItem, ItemMtProductLargeBinding>() {
            override fun onCreateBinding(inflater: LayoutInflater, parent: ViewGroup) =
                ItemMtProductLargeBinding.inflate(inflater, parent, false)

            override fun onBind(
                holder: VBViewHolder<ItemMtProductLargeBinding>,
                position: Int,
                item: DemoItem?,
                payloads: List<Any>,
            ) {
                val p = item as? DemoItem.ProductLarge ?: return
                holder.binding.tvImagePlaceholder.text = p.name.take(1)
                holder.binding.tvName.text = p.name
                holder.binding.tvDesc.text = p.desc
                holder.binding.tvPrice.text = p.price
            }
        })

        addItemType(TYPE_LOADING, object : ItemViewBinder<DemoItem, ItemMtLoadingBinding>() {
            override fun onCreateBinding(inflater: LayoutInflater, parent: ViewGroup) =
                ItemMtLoadingBinding.inflate(inflater, parent, false)

            override fun onBind(
                holder: VBViewHolder<ItemMtLoadingBinding>,
                position: Int,
                item: DemoItem?,
                payloads: List<Any>,
            ) = Unit
        })

        onItemViewType { position, list ->
            when (list[position]) {
                is DemoItem.Banner -> TYPE_BANNER
                is DemoItem.ProductSmall -> TYPE_PRODUCT_SMALL
                is DemoItem.ProductLarge -> TYPE_PRODUCT_LARGE
                is DemoItem.Loading -> TYPE_LOADING
            }
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<DemoItem>() {
        override fun areItemsTheSame(old: DemoItem, new: DemoItem): Boolean {
            return when {
                old is DemoItem.Banner && new is DemoItem.Banner ->
                    old.title == new.title
                old is DemoItem.ProductSmall && new is DemoItem.ProductSmall ->
                    old.name == new.name
                old is DemoItem.ProductLarge && new is DemoItem.ProductLarge ->
                    old.name == new.name
                else -> old.javaClass == new.javaClass
            }
        }

        override fun areContentsTheSame(old: DemoItem, new: DemoItem) = old == new
    }
}
