package com.nia.compose.bridge.feature.settings.impl

import androidx.lifecycle.viewModelScope
import com.nia.compose.bridge.data.repository.UserSettingsRepository
import com.nia.compose.bridge.common.BaseViewModel
import com.nia.compose.bridge.model.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userSettingsRepository: UserSettingsRepository,
) : BaseViewModel<SettingsUiState>(SettingsUiState.Success(SettingsData())) {

    init {
        observeSettings()
    }

    private fun observeSettings() {
        viewModelScope.launch {
            userSettingsRepository.userSettings.collectLatest { settings ->
                setState(SettingsUiState.Success(SettingsData(userSettings = settings)))
            }
        }
    }

    fun setThemeMode(themeMode: ThemeMode) {
        viewModelScope.launch {
            userSettingsRepository.setThemeMode(themeMode)
        }
    }

    fun setUseDynamicColor(enabled: Boolean) {
        viewModelScope.launch {
            userSettingsRepository.setUseDynamicColor(enabled)
        }
    }

    fun navigateBack() {
        emitEffect(SettingsEffect.NavigateBack)
    }
}
