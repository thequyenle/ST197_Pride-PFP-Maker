package com.pfp.pride.dialog

import android.app.Activity
import android.graphics.drawable.AnimationDrawable
import com.pfp.pride.R
import com.pfp.pride.core.base.BaseDialog
import com.pfp.pride.core.extensions.setBackgroundConnerSmooth
import com.pfp.pride.databinding.DialogLoadingBinding

class WaitingDialog(val context: Activity) :
    BaseDialog<DialogLoadingBinding>(context, maxWidth = true, maxHeight = true) {
    override val layoutId: Int = R.layout.dialog_loading
    override val isCancelOnTouchOutside: Boolean = false
    override val isCancelableByBack: Boolean = false

    override fun initView() {
        // Start loading animation for dot

    }

    override fun initAction() {}

    override fun onDismissListener() {}

}