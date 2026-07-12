package com.example.soundplayer.presentation.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.soundplayer.R
import com.example.soundplayer.presentation.components.SoundPlayerPreferenceDivider
import com.example.soundplayer.presentation.components.SoundPlayerPreferenceOption
import com.example.soundplayer.presentation.components.SoundPlayerSingleChoiceDialog
import com.example.soundplayer.presentation.components.SoundPlayerTopBar
import com.example.soundplayer.presentation.viewmodel.PreferencesUiState

private enum class SettingsDialog {
    Theme,
    TextSize,
    Order,
}

@Composable
fun SettingsScreen(
    state: PreferencesUiState,
    onBack: () -> Unit,
    onThemeSelected: (Int) -> Unit,
    onTextSizeSelected: (Float) -> Unit,
    onOrderSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val themeOptions =
        listOf(
            stringResource(id = R.string.theme_light),
            stringResource(id = R.string.theme_dark),
            stringResource(id = R.string.theme_system),
        )
    val textSizeOptions =
        listOf(
            stringResource(id = R.string.pequena),
            stringResource(id = R.string.media),
            stringResource(id = R.string.grande),
        )
    val orderOptions =
        listOf(
            stringResource(id = R.string.order_name),
            stringResource(id = R.string.order_name_desc),
            stringResource(id = R.string.order_inserted_last),
        )

    val textSizeIndex =
        when (state.textSize) {
            15f -> 0
            18f -> 1
            20f -> 2
            else -> 0
        }
    val orderIndex = state.preferences?.orderedSound ?: 0

    var openDialog by remember { mutableStateOf<SettingsDialog?>(null) }

    Surface(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            SoundPlayerTopBar(title = stringResource(id = R.string.tittle_settings), onBack = onBack)
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp, vertical = 32.dp),
            ) {
                SoundPlayerPreferenceOption(
                    title = stringResource(id = R.string.thema_visualizacao),
                    subtitle = themeOptions.getOrElse(state.darkMode) { themeOptions.last() },
                    onClick = { openDialog = SettingsDialog.Theme },
                )
                SoundPlayerPreferenceDivider()
                SoundPlayerPreferenceOption(
                    title = stringResource(id = R.string.tamanho_titulo_musica),
                    subtitle = textSizeOptions[textSizeIndex],
                    onClick = { openDialog = SettingsDialog.TextSize },
                )
                SoundPlayerPreferenceDivider()
                SoundPlayerPreferenceOption(
                    title = stringResource(id = R.string.ordenar_sons_por),
                    subtitle = orderOptions.getOrElse(orderIndex) { orderOptions.first() },
                    onClick = { openDialog = SettingsDialog.Order },
                )
            }
        }
    }

    when (openDialog) {
        SettingsDialog.Theme ->
            SoundPlayerSingleChoiceDialog(
                title = stringResource(id = R.string.settings_choose_theme),
                options = themeOptions,
                selectedIndex = state.darkMode,
                onDismiss = { openDialog = null },
                onSelected = { selected ->
                    openDialog = null
                    onThemeSelected(selected)
                },
            )
        SettingsDialog.TextSize ->
            SoundPlayerSingleChoiceDialog(
                title = stringResource(id = R.string.settings_choose_text_size),
                options = textSizeOptions,
                selectedIndex = textSizeIndex,
                onDismiss = { openDialog = null },
                onSelected = { selected ->
                    openDialog = null
                    val size =
                        when (selected) {
                            0 -> 15f
                            1 -> 18f
                            else -> 20f
                        }
                    onTextSizeSelected(size)
                },
            )
        SettingsDialog.Order ->
            SoundPlayerSingleChoiceDialog(
                title = stringResource(id = R.string.ordenar_sons_por),
                options = orderOptions,
                selectedIndex = orderIndex,
                onDismiss = { openDialog = null },
                onSelected = { selected ->
                    openDialog = null
                    onOrderSelected(selected)
                },
            )
        null -> Unit
    }
}
