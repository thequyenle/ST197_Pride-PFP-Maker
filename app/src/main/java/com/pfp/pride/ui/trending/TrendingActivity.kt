package com.pfp.pride.ui.trending

import android.animation.ObjectAnimator
import android.app.ActivityOptions
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import androidx.activity.viewModels
import androidx.core.graphics.createBitmap
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.pfp.pride.R
import com.pfp.pride.core.base.BaseActivity
import com.pfp.pride.core.extensions.handleBackLeftToRight
import com.pfp.pride.core.extensions.hideNavigation
import com.pfp.pride.core.extensions.setImageActionBar
import com.pfp.pride.core.extensions.setTextActionBar
import com.pfp.pride.core.extensions.showInterAll
import com.pfp.pride.core.extensions.tap
import com.pfp.pride.core.helper.InternetHelper
import com.pfp.pride.core.helper.MediaHelper
import com.pfp.pride.core.utils.key.IntentKey
import com.pfp.pride.core.utils.key.ValueKey
import com.pfp.pride.core.utils.state.SaveState
import com.pfp.pride.data.model.custom.SuggestionModel
import com.pfp.pride.databinding.ActivityTrendingBinding
import com.pfp.pride.dialog.YesNoDialog
import com.pfp.pride.ui.customize.CustomizeCharacterActivity
import com.pfp.pride.ui.customize.CustomizeCharacterViewModel
import com.pfp.pride.ui.home.DataViewModel
import com.pfp.pride.ui.random_character.RandomCharacterViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

class TrendingActivity : BaseActivity<ActivityTrendingBinding>() {

    private val viewModel: RandomCharacterViewModel by viewModels()
    private val dataViewModel: DataViewModel by viewModels()
    private val customizeCharacterViewModel: CustomizeCharacterViewModel by viewModels()

    private var currentSuggestion: SuggestionModel? = null
    private var isAnimating = false

    override fun setViewBinding(): ActivityTrendingBinding {
        return ActivityTrendingBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
        lifecycleScope.launch { showLoading() }
        dataViewModel.ensureData(this)
    }

    override fun dataObservable() {
        lifecycleScope.launch {
            dataViewModel.allData.collect { data ->
                if (data.isNotEmpty()) {
                    initData()
                }
            }
        }
    }

    override fun viewListener() {
        binding.apply {
            actionBar.btnActionBarLeft.tap { showInterAll { handleBackLeftToRight() } }
            btnGenerate.tap(0) { handleGenerate() }
            btnEdit.tap { handleEdit() }
        }
    }

    override fun initActionBar() {
        binding.actionBar.apply {
            setImageActionBar(btnActionBarLeft, R.drawable.ic_back)
            setTextActionBar(tvCenter, getString(R.string.trending))
            tvCenter.isSelected = true
        }
    }

    private fun initData() {
        val handleExceptionCoroutine = CoroutineExceptionHandler { _, throwable ->
            CoroutineScope(Dispatchers.Main).launch {
                val dialogExit = YesNoDialog(
                    this@TrendingActivity,
                    R.string.error,
                    R.string.an_error_occurred
                )
                dialogExit.show()
                dialogExit.onNoClick = {
                    dialogExit.dismiss()
                    finish()
                }
                dialogExit.onYesClick = {
                    dialogExit.dismiss()
                    hideNavigation()
                    finish()
                }
            }
        }

        CoroutineScope(SupervisorJob() + Dispatchers.Main + handleExceptionCoroutine).launch {
            val hasInternet = withContext(Dispatchers.IO) {
                InternetHelper.isInternetAvailable(this@TrendingActivity)
            }
            val filteredData = if (hasInternet) {
                dataViewModel.allData.value
            } else {
                dataViewModel.allData.value.filter { !it.isFromAPI }
            }
            if (filteredData.isEmpty()) return@launch

            suspend fun processCharacter(data: com.pfp.pride.data.model.custom.CustomizeModel) {
                customizeCharacterViewModel.positionSelected =
                    dataViewModel.allData.value.indexOf(data)
                customizeCharacterViewModel.setDataCustomize(data)
                customizeCharacterViewModel.updateAvatarPath(data.avatar)
                customizeCharacterViewModel.resetDataList()
                customizeCharacterViewModel.addValueToItemNavList()
                customizeCharacterViewModel.setItemColorDefault()
                customizeCharacterViewModel.setBottomNavigationListDefault()
                for (j in 0 until ValueKey.RANDOM_QUANTITY) {
                    customizeCharacterViewModel.setClickRandomFullLayer()
                    val suggestion = customizeCharacterViewModel.getSuggestionList()
                    viewModel.updateRandomList(suggestion)
                }
            }

            // Xử lý character đầu tiên → show ngay
            withContext(Dispatchers.IO) {
                try { processCharacter(filteredData[0]) } catch (e: Exception) { e.printStackTrace() }
                viewModel.upsideDownList()
            }
            showRandomSuggestion { lifecycleScope.launch { dismissLoading() } }

            // Xử lý phần còn lại ở background
            if (filteredData.size > 1) {
                withContext(Dispatchers.IO) {
                    for (i in 1 until filteredData.size) {
                        try { processCharacter(filteredData[i]) } catch (e: Exception) { e.printStackTrace() }
                    }
                    viewModel.upsideDownList()
                }
            }
        }
    }

    private fun showRandomSuggestion(onComplete: (() -> Unit)? = null) {
        if (viewModel.randomList.isEmpty()) {
            onComplete?.invoke()
            return
        }
        val model = viewModel.randomList.random()
        currentSuggestion = model
        renderSuggestion(model, onComplete)
    }

    private fun handleGenerate() {
        if (viewModel.randomList.isEmpty()) return
        if (isAnimating) return
        isAnimating = true
        binding.btnGenerate.visibility = View.INVISIBLE
        binding.btnEdit.visibility = View.INVISIBLE

        val totalDuration = 800L

        // Show GIF while generating
        Glide.with(this).asGif().load(R.drawable.gif).into(binding.imvImage)

        // Dice: spin 3 full rounds, decelerating like a real dice roll
        val diceAnim = ObjectAnimator.ofFloat(binding.dices, "rotation", 0f, 1080f).apply {
            duration = totalDuration
            interpolator = DecelerateInterpolator(2f)
            start()
        }

        lifecycleScope.launch {
            delay(totalDuration)

            diceAnim.cancel()
            binding.dices.rotation = 0f

            // Check internet sau khi delay xong, timeout 3s để tránh hang khi mất mạng
            val hasInternet = withContext(Dispatchers.IO) {
                try {
                    withTimeout(3000) { InternetHelper.isInternetAvailable(this@TrendingActivity) }
                } catch (e: TimeoutCancellationException) {
                    false
                }
            }
            val availableList = if (hasInternet) {
                viewModel.randomList
            } else {
                viewModel.randomList.filter { model ->
                    val character = dataViewModel.allData.value.firstOrNull { it.avatar == model.avatarPath }
                    character?.isFromAPI != true
                }
            }
            val finalModel = availableList.randomOrNull() ?: run {
                isAnimating = false
                binding.btnGenerate.visibility = View.VISIBLE
                binding.btnEdit.visibility = View.VISIBLE
                return@launch
            }

            currentSuggestion = finalModel
            renderSuggestion(finalModel) {
                isAnimating = false
                binding.btnGenerate.visibility = View.VISIBLE
                binding.btnEdit.visibility = View.VISIBLE
            }
        }
    }

    private fun renderSuggestion(model: SuggestionModel, onComplete: (() -> Unit)? = null) {
        if (model.pathInternalRandom.isNotEmpty()) {
            Glide.with(this)
                .load(model.pathInternalRandom)
                .listener(glideListener(onComplete))
                .into(binding.imvImage)
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val paths = model.pathSelectedList.filter { it.isNotEmpty() }
                if (paths.isEmpty()) {
                    withContext(Dispatchers.Main) { onComplete?.invoke() }
                    return@launch
                }

                val bitmapDefault = Glide.with(this@TrendingActivity)
                    .asBitmap().load(paths.first()).submit().get()
                val width = bitmapDefault.width / 2
                val height = bitmapDefault.height / 2

                val listBitmap = ArrayList<Bitmap>()
                paths.forEach { path ->
                    listBitmap.add(
                        Glide.with(this@TrendingActivity)
                            .asBitmap().load(path).submit(width, height).get()
                    )
                }

                val combinedBitmap = createBitmap(width, height)
                val canvas = Canvas(combinedBitmap)
                for (bitmap in listBitmap) {
                    val left = (width - bitmap.width) / 2f
                    val top = (height - bitmap.height) / 2f
                    canvas.drawBitmap(bitmap, left, top, null)
                }

                MediaHelper.saveBitmapToInternalStorage(
                    this@TrendingActivity,
                    ValueKey.RANDOM_TEMP_ALBUM,
                    combinedBitmap
                ).collect { state ->
                    if (state is SaveState.Success) {
                        model.pathInternalRandom = state.path
                    }
                }

                withContext(Dispatchers.Main) {
                    Glide.with(this@TrendingActivity)
                        .load(model.pathInternalRandom)
                        .listener(glideListener(onComplete))
                        .into(binding.imvImage)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) { onComplete?.invoke() }
            }
        }
    }

    private fun glideListener(onComplete: (() -> Unit)?): RequestListener<android.graphics.drawable.Drawable> {
        return object : RequestListener<android.graphics.drawable.Drawable> {
            override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<android.graphics.drawable.Drawable>, isFirstResource: Boolean): Boolean {
                onComplete?.invoke()
                return false
            }
            override fun onResourceReady(resource: android.graphics.drawable.Drawable, model: Any, target: Target<android.graphics.drawable.Drawable>?, dataSource: DataSource, isFirstResource: Boolean): Boolean {
                onComplete?.invoke()
                return false
            }
        }
    }

    private fun handleEdit() {
        val suggestion = currentSuggestion ?: return
        customizeCharacterViewModel.positionSelected =
            dataViewModel.allData.value.indexOfFirst { it.avatar == suggestion.avatarPath }
        val selectedCharacter =
            dataViewModel.allData.value.getOrNull(customizeCharacterViewModel.positionSelected)
        viewModel.setIsDataAPI(selectedCharacter?.isFromAPI ?: false)
        viewModel.checkDataInternet(this) {
            lifecycleScope.launch {
                showLoading()
                withContext(Dispatchers.IO) {
                    MediaHelper.writeModelToFile(
                        this@TrendingActivity,
                        ValueKey.SUGGESTION_FILE_INTERNAL,
                        suggestion
                    )
                }
                val intent = Intent(this@TrendingActivity, CustomizeCharacterActivity::class.java)
                intent.putExtra(IntentKey.INTENT_KEY, customizeCharacterViewModel.positionSelected)
                intent.putExtra(IntentKey.STATUS_FROM_KEY, ValueKey.SUGGESTION)
                val option = ActivityOptions.makeCustomAnimation(
                    this@TrendingActivity,
                    R.anim.slide_out_left,
                    R.anim.slide_in_right
                )
                dismissLoading()
                showInterAll { startActivity(intent, option.toBundle()) }
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            applyUiCustomize()
            hideNavigation(true)
            window.decorView.removeCallbacks(reHideRunnable)
            window.decorView.postDelayed(reHideRunnable, 2000)
        } else {
            window.decorView.removeCallbacks(reHideRunnable)
        }
    }

    private val reHideRunnable = Runnable {
        applyUiCustomize()
        hideNavigation(true)
    }

    @Suppress("DEPRECATION")
    private fun applyUiCustomize() {
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
    }
}
