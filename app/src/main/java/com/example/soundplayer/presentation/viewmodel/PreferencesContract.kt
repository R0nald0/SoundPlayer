package com.example.soundplayer.presentation.viewmodel

import com.example.soundplayer.data.entities.UserDataPreference

sealed interface PreferencesIntent {
    data object ReadAllPreferences : PreferencesIntent

    data object ReadDarkMode : PreferencesIntent

    data object ReadTextSize : PreferencesIntent

    data class SaveDarkMode(
        val value: Int,
    ) : PreferencesIntent

    data class SaveTextSize(
        val size: Float,
    ) : PreferencesIntent

    data class SaveOrderedSound(
        val value: Int,
    ) : PreferencesIntent

    data class SavePlaylistId(
        val id: Long,
    ) : PreferencesIntent

    data class SaveCurrentSoundPosition(
        val position: Int,
    ) : PreferencesIntent
}

data class PreferencesUiState(
    val preferences: UserDataPreference? = null,
    val darkMode: Int = 2,
    val textSize: Float = 16f,
    val error: String? = null,
)

sealed interface PreferencesUiEvent {
    data class ShowError(
        val message: String,
    ) : PreferencesUiEvent
}
