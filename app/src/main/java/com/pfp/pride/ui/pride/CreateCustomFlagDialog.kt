package com.pfp.pride.ui.pride

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import androidx.recyclerview.widget.LinearLayoutManager
import com.pfp.pride.R
import com.pfp.pride.core.base.BaseDialog
import com.pfp.pride.core.extensions.tap
import com.pfp.pride.data.model.pride.CustomFlagModel
import com.pfp.pride.databinding.DialogCreateCustomFlagBinding
import com.pfp.pride.dialog.ChooseColorDialog
import com.pfp.pride.ui.pride.adapter.CustomColorAdapter

class CreateCustomFlagDialog(context: Context) :
    BaseDialog<DialogCreateCustomFlagBinding>(context, maxWidth = true, maxHeight = true) {

    override val layoutId: Int = R.layout.dialog_create_custom_flag
    override val isCancelOnTouchOutside: Boolean = false
    override val isCancelableByBack: Boolean = false

    var onCreateEvent: ((CustomFlagModel) -> Unit) = {}
    var onCloseEvent: (() -> Unit) = {}
    var onDismissEvent: (() -> Unit) = {}

    private val colors = mutableListOf<Int>(Color.BLACK)
    private lateinit var colorAdapter: CustomColorAdapter

    override fun initView() {
        colorAdapter = CustomColorAdapter(
            onColorClick = { position -> openColorPicker(position) },
            onRemoveClick = { position -> removeColor(position) }
        )
        binding.rvColors.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = colorAdapter
        }
        colorAdapter.submitList(colors.toList())
        updatePreview()
        updateColorCount()
    }

    override fun initAction() {
        binding.apply {
            btnClose.tap { onCloseEvent.invoke() }

            btnAddColor.tap {
                if (colors.size < 10) {
                    colors.add(Color.WHITE)
                    colorAdapter.submitList(colors.toList())
                    updatePreview()
                    updateColorCount()
                    openColorPicker(colors.size - 1)
                }
            }

            btnCreate.tap {
                val name = etFlagName.text.toString().trim()
                if (name.isEmpty()) {
                    etFlagName.error = context.getString(R.string.pride_enter_name)
                    return@tap
                }
                onCreateEvent.invoke(CustomFlagModel(name, colors.toMutableList()))
            }
        }
    }

    private fun openColorPicker(position: Int) {
        val dialog = ChooseColorDialog(context)
        dialog.show()
        dialog.onCloseEvent = { dialog.dismiss() }
        dialog.onDoneEvent = { color ->
            dialog.dismiss()
            colors[position] = color
            colorAdapter.submitList(colors.toList())
            updatePreview()
        }
    }

    private fun removeColor(position: Int) {
        if (colors.size > 1) {
            colors.removeAt(position)
            colorAdapter.submitList(colors.toList())
            updatePreview()
            updateColorCount()
        }
    }

    private fun updatePreview() {
        val view = binding.flagPreviewBar
        view.post {
            if (view.width <= 0 || view.height <= 0) return@post
            val bitmap = buildFlagPreview(view.width, view.height)
            view.setBackgroundDrawable(android.graphics.drawable.BitmapDrawable(context.resources, bitmap))
        }
    }

    private fun updateColorCount() {
        binding.tvColorCount.text = "${colors.size} color"
    }

    private fun buildFlagPreview(width: Int, height: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint()
        val partWidth = width.toFloat() / colors.size
        colors.forEachIndexed { index, color ->
            paint.color = color
            canvas.drawRect(
                index * partWidth, 0f,
                (index + 1) * partWidth, height.toFloat(),
                paint
            )
        }
        return bitmap
    }

    override fun onDismissListener() {
        onDismissEvent.invoke()
    }
}
