package com.pfp.pride.ui.pride.adapter

import android.content.Context
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.ViewGroup
import com.pfp.pride.core.base.BaseAdapter
import com.pfp.pride.core.extensions.gone
import com.pfp.pride.core.extensions.visible
import com.pfp.pride.data.model.pride.PrideFlagModel
import com.pfp.pride.databinding.ItemPrideFlagBinding

class PrideFlagAdapter(
    private val context: Context,
    private val onFlagClick: (PrideFlagModel) -> Unit
) : BaseAdapter<PrideFlagModel, ItemPrideFlagBinding>(
    ItemPrideFlagBinding::inflate
) {

    private var maxReached = false

    fun setMaxReached(reached: Boolean) {
        maxReached = reached
        notifyDataSetChanged()
    }

    override fun onBind(binding: ItemPrideFlagBinding, item: PrideFlagModel, position: Int) {
        binding.apply {
            tvFlagName.text = item.name

            // Load flag image from assets
            try {
                val inputStream = context.assets.open(item.assetPath)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                imgFlag.setImageBitmap(bitmap)
                inputStream.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // Checkbox state
            imgCheckbox.setImageResource(
                if (item.isSelected) com.pfp.pride.R.drawable.ic_selected
                else com.pfp.pride.R.drawable.ic_not_select
            )

            // Card background
            cardFlag.setCardBackgroundColor(
                if (item.isSelected)
                    android.graphics.Color.parseColor("#F3EEFF")
                else
                    android.graphics.Color.WHITE
            )

            // Dim overlay when max reached and not selected
            if (maxReached && !item.isSelected) {
                dimOverlay.visible()
            } else {
                dimOverlay.gone()
            }

            root.setOnClickListener {
                if (!maxReached || item.isSelected) {
                    onFlagClick(item)
                }
            }
        }
    }
}
