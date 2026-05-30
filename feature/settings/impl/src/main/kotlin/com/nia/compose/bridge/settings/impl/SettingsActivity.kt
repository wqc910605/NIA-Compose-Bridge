package com.nia.compose.bridge.feature.settings.impl

import com.nia.compose.bridge.common.UiEffect
import com.nia.compose.bridge.viewbinding.BaseActivity
import com.nia.compose.bridge.core.viewbinding.viewBinding
import com.nia.compose.bridge.feature.settings.api.databinding.ActivitySettingsBinding
import com.nia.compose.bridge.model.ThemeMode
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsActivity : BaseActivity() {

    override val binding by viewBinding(ActivitySettingsBinding::inflate)

    override val viewModel by viewModels<SettingsViewModel>()

    override fun initView() {
        binding.toolbar.setNavigationOnClickListener {
            viewModel.navigateBack()
        }

        binding.switchDynamicColor.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setUseDynamicColor(isChecked)
        }

        binding.radioGroupTheme.setOnCheckedChangeListener { _, checkedId ->
            val themeMode = when (checkedId) {
                com.nia.compose.bridge.feature.settings.impl.R.id.radioLight -> ThemeMode.LIGHT
                com.nia.compose.bridge.feature.settings.impl.R.id.radioDark -> ThemeMode.DARK
                else -> ThemeMode.SYSTEM
            }
            viewModel.setThemeMode(themeMode)
        }
    }

    override fun render(state: UiState) {
        if (state !is SettingsUiState.Success) return
        val settings = state.data.userSettings

        binding.switchDynamicColor.isChecked = settings.useDynamicColor

        val radioId = when (settings.themeMode) {
            ThemeMode.LIGHT -> com.nia.compose.bridge.feature.settings.impl.R.id.radioLight
            ThemeMode.DARK -> com.nia.compose.bridge.feature.settings.impl.R.id.radioDark
            ThemeMode.SYSTEM -> com.nia.compose.bridge.feature.settings.impl.R.id.radioSystem
        }
        binding.radioGroupTheme.check(radioId)
    }

    override fun handleEffect(effect: UiEffect) {
        when (effect) {
            is SettingsEffect.NavigateBack -> {
                finish()
            }
        }
    }
}
