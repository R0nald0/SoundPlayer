package com.example.soundplayer.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Black = Color(0x6F000000)
private val BlackToolbar = Color(0xFF400404)
private val White = Color(0xFFFFFFFF)
private val Gray = Color(0xFFE88F8F)
private val Red = Color(0xFFC93636)
private val ColorPrimaryContainer = Color(0xFFE35252)
private val MyPrimary = Color(0xFF242325)
private val NightSurfaceVariant = Color(0xFF49454F)

private val LightColorScheme =
    lightColorScheme(
        primary = BlackToolbar,
        onPrimary = White,
        primaryContainer = ColorPrimaryContainer,
        onPrimaryContainer = White,
        secondary = Gray,
        onSecondary = Black,
        background = White,
        onBackground = Black,
        surface = White,
        onSurface = Black,
        surfaceVariant = Color(0xFFE7E0EC),
        onSurfaceVariant = Black,
        error = Red,
    )

private val DarkColorScheme =
    darkColorScheme(
        primary = NightSurfaceVariant,
        onPrimary = White,
        primaryContainer = Red,
        onPrimaryContainer = White,
        secondary = Gray,
        onSecondary = White,
        background = MyPrimary,
        onBackground = White,
        surface = MyPrimary,
        onSurface = White,
        surfaceVariant = NightSurfaceVariant,
        onSurfaceVariant = White,
        error = Red,
    )

@Composable
fun SoundPlayerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        content = content,
    )
}
