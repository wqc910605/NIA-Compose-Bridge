package com.nia.compose.bridge.feature.settings.impl

import com.nia.compose.bridge.common.UiEffect
import com.nia.compose.bridge.common.UiState
import com.nia.compose.bridge.model.UserSettings

data class SettingsData(
    val userSettings: UserSettings = UserSettings(),
)

sealed interface SettingsUiState : UiState {
    data class Success(val data: SettingsData) : SettingsUiState
}

sealed interface SettingsEffect : UiEffect {
    data object NavigateBack : SettingsEffect
}
