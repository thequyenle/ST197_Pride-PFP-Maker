package com.pfp.pride.ui.choose_character

import com.pfp.pride.core.base.BaseAdapter
import com.pfp.pride.core.extensions.gone
import com.pfp.pride.core.extensions.loadImage
import com.pfp.pride.core.extensions.tap
import com.pfp.pride.data.model.custom.CustomizeModel
import com.pfp.pride.databinding.ItemChooseAvatarBinding

class ChooseCharacterAdapter : BaseAdapter<CustomizeModel, ItemChooseAvatarBinding>(ItemChooseAvatarBinding::inflate) {
    var onItemClick: ((position: Int) -> Unit) = {}
    override fun onBind(binding: ItemChooseAvatarBinding, item: CustomizeModel, position: Int) {
        binding.apply {
            loadImage(item.avatar, imvImage, onDismissLoading = {
                sflShimmer.stopShimmer()
                sflShimmer.gone()
            })
            root.tap { onItemClick.invoke(position) }
        }
    }
}