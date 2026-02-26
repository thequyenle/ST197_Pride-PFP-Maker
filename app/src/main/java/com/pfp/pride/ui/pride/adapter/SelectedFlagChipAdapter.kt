package com.pfp.pride.ui.pride.adapter

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import com.pfp.pride.core.base.BaseAdapter
import com.pfp.pride.core.extensions.tap
import com.pfp.pride.data.model.pride.PrideFlagModel
import com.pfp.pride.databinding.ItemSelectedFlagChipBinding

class SelectedFlagChipAdapter(
    private val onRemoveClick: (PrideFlagModel) -> Unit
) : BaseAdapter<PrideFlagModel, ItemSelectedFlagChipBinding>(
    ItemSelectedFlagChipBinding::inflate
) {
    // Simple color list for the dot per flag
    private val dotColors = listOf(
        "#4CAF50", "#2196F3", "#FF5722", "#9C27B0",
        "#FF9800", "#00BCD4", "#E91E63", "#607D8B"
    )

    override fun onBind(binding: ItemSelectedFlagChipBinding, item: PrideFlagModel, position: Int) {
        binding.apply {
            tvChipName.text = item.name

            // Set dot color
            val colorHex = dotColors.getOrElse(position) { "#888888" }
            val drawable = GradientDrawable()
            drawable.shape = GradientDrawable.OVAL
            drawable.setColor(Color.parseColor(colorHex))
            dotColor.background = drawable

            btnRemoveChip.tap { onRemoveClick(item) }
        }
    }
}
