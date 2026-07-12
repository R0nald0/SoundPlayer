package com.example.soundplayer.presentation.components

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.soundplayer.model.Sound
import com.example.soundplayer.presentation.theme.SoundPlayerTheme
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test

class SoundSelectionSheetTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun soundSelectionSheet_togglesSoundAndRunsActionWithSelection() {
        val yellow = sound(id = 20, title = "Yellow (Accoustic)")
        var savedSelection: Set<Sound> = emptySet()

        composeRule.setContent {
            var selectedSounds by remember { mutableStateOf<Set<Sound>>(emptySet()) }
            SoundPlayerTheme(darkTheme = false) {
                SoundSelectionSheet(
                    sounds = listOf(yellow),
                    selectedSounds = selectedSounds,
                    actionText = "Criar playlist",
                    onSoundToggle = { sound ->
                        selectedSounds =
                            if (selectedSounds.contains(sound)) {
                                selectedSounds - sound
                            } else {
                                selectedSounds + sound
                            }
                    },
                    onAction = { savedSelection = selectedSounds },
                    playlistName = "Favoritas",
                    onPlaylistNameChange = {},
                )
            }
        }

        composeRule.onNodeWithText("Yellow (Accoustic)").performClick()
        composeRule.onNodeWithText("Criar playlist").performClick()

        assertThat(savedSelection.map { it.idSound }).containsExactly(20L)
    }

    private fun sound(
        id: Long,
        title: String,
    ) = Sound(
        idSound = id,
        path = "/storage/$id.mp3",
        artistName = "Cold Play",
        albumName = "Acoustic",
        duration = "253000",
        title = title,
        uriMedia = null,
        uriMediaAlbum = null,
        insertedDate = 1,
    )
}
