package com.pfp.pride.ui.intro

import android.content.Context
import com.pfp.pride.core.base.BaseAdapter
import com.pfp.pride.core.extensions.loadImage
import com.pfp.pride.core.extensions.select
import com.pfp.pride.core.extensions.strings
import com.pfp.pride.data.model.IntroModel
import com.pfp.pride.databinding.ItemIntroBinding

class IntroAdapter(val context: Context) : BaseAdapter<IntroModel, ItemIntroBinding>(
    ItemIntroBinding::inflate
) {
    override fun onBind(binding: ItemIntroBinding, item: IntroModel, position: Int) {
        binding.apply {
            loadImage(root, item.image, imvImage, false)
            tvContent.text = context.strings(item.content)
            tvContent.select()
        }
    }
}