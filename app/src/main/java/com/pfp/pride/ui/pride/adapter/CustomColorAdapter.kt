package com.pfp.pride.ui.pride.adapter

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.pfp.pride.core.base.BaseAdapter
import com.pfp.pride.core.extensions.tap
import com.pfp.pride.databinding.ItemCustomColorBinding

class CustomColorAdapter(
    private val onColorClick: (Int) -> Unit,
    private val onRemoveClick: (Int) -> Unit
) : BaseAdapter<Int, ItemCustomColorBinding>(
    ItemCustomColorBinding::inflate
) {
    override fun submitList(list: List<Int>) {
        val oldSize = items.size
        super.submitList(list)
        if ((oldSize == 1) != (items.size == 1)) notifyItemChanged(0)
    }

    override fun onBind(binding: ItemCustomColorBinding, item: Int, position: Int) {
        binding.apply {
            tvColorLabel.text = "Color ${position + 1}"
            tvColorHex.text = String.format("#%06X", 0xFFFFFF and item)

            // Set swatch color
            val drawable = GradientDrawable()
            drawable.shape = GradientDrawable.RECTANGLE
            drawable.cornerRadius = 12f
            drawable.setColor(item)
            colorSwatch.background = drawable

            btnRemoveColor.visibility = if (itemCount == 1) View.INVISIBLE else View.VISIBLE
            colorSwatch.tap { onColorClick(position) }
            btnRemoveColor.tap { onRemoveClick(position) }
        }
    }
}
