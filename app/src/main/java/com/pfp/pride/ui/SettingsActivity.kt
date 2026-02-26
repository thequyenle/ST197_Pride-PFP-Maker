package com.pfp.pride.ui

import android.view.LayoutInflater
import com.pfp.pride.R
import com.pfp.pride.core.base.BaseActivity
import com.pfp.pride.core.extensions.gone
import com.pfp.pride.core.extensions.handleBackLeftToRight
import com.pfp.pride.core.extensions.policy
import com.pfp.pride.core.extensions.select
import com.pfp.pride.core.extensions.setImageActionBar
import com.pfp.pride.core.extensions.setTextActionBar
import com.pfp.pride.core.extensions.shareApp
import com.pfp.pride.core.extensions.startIntentRightToLeft
import com.pfp.pride.core.extensions.visible
import com.pfp.pride.core.utils.key.IntentKey
import com.pfp.pride.core.utils.state.RateState
import com.pfp.pride.databinding.ActivitySettingsBinding
import com.pfp.pride.ui.language.LanguageActivity
import com.pfp.pride.core.extensions.tap
import com.pfp.pride.core.helper.MusicHelper
import com.pfp.pride.core.helper.RateHelper
import kotlin.jvm.java

class SettingsActivity : BaseActivity<ActivitySettingsBinding>() {
    override fun setViewBinding(): ActivitySettingsBinding {
        return ActivitySettingsBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
        binding.tvMusic.select()
        initRate()
        initMusic()
    }

    private fun initMusic() {
        updateMusicUI(sharePreference.isMusicEnabled())
    }

    private fun updateMusicUI(isEnabled: Boolean) {
        binding.btnMusic.setImageResource(
            if (isEnabled) R.drawable.ic_sw_on else R.drawable.ic_sw_off_ms
        )
    }

    private fun toggleMusic() {
        val isEnabled = !sharePreference.isMusicEnabled()
        sharePreference.setMusicEnabled(isEnabled)
        updateMusicUI(isEnabled)
        if (isEnabled) {
            MusicHelper.play()
        } else {
            MusicHelper.pause()
        }
    }

    override fun viewListener() {
        binding.apply {
            actionBar.btnActionBarLeft.tap { handleBackLeftToRight() }
            layoutMusic.tap { toggleMusic() }
            btnLang.tap { startIntentRightToLeft(LanguageActivity::class.java, IntentKey.INTENT_KEY) }
            btnShareApp.tap(1500) { shareApp() }
            btnRate.tap {
                RateHelper.showRateDialog(this@SettingsActivity, sharePreference){ state ->
                    if (state != RateState.CANCEL){
                        btnRate.gone()
                        showToast(R.string.have_rated)
                    }
                }
            }
            btnPolicy.tap(1500) { policy() }
        }
    }

    override fun initText() {
        binding.actionBar.tvCenter.select()
    }

    override fun initActionBar() {
        binding.actionBar.apply {
            setImageActionBar(btnActionBarLeft, R.drawable.ic_back)
            setTextActionBar(tvCenter, getString(R.string.settings))
        }
    }

    private fun initRate() {
        if (sharePreference.getIsRate(this)) {
            binding.btnRate.gone()
        } else {
            binding.btnRate.visible()
        }
    }
}