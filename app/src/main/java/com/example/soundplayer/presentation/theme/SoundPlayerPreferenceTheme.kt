package com.example.soundplayer.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable

@Composable
fun SoundPlayerPreferenceTheme(
    darkMode: Int,
    content: @Composable () -> Unit,
) {
    val darkTheme =
        when (darkMode) {
            0 -> false
            1 -> true
            else -> isSystemInDarkTheme()
        }

    SoundPlayerTheme(
        darkTheme = darkTheme,
        content = content,
    )
}
