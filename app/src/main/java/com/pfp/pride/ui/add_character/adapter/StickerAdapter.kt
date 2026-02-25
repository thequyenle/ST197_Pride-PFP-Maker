package com.pfp.pride.ui.add_character.adapter

import com.pfp.pride.core.base.BaseAdapter
import com.pfp.pride.core.extensions.loadImage
import com.pfp.pride.core.extensions.loadImageSticker
import com.pfp.pride.core.extensions.tap
import com.pfp.pride.data.model.SelectedModel
import com.pfp.pride.databinding.ItemStickerBinding

class StickerAdapter : BaseAdapter<SelectedModel, ItemStickerBinding>(ItemStickerBinding::inflate) {
    var onItemClick : ((String) -> Unit) = {}
    override fun onBind(binding: ItemStickerBinding, item: SelectedModel, position: Int) {
        binding.apply {
            loadImageSticker(root, item.path, imvSticker)
            root.tap { onItemClick.invoke(item.path) }
        }
    }
}