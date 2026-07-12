package com.example.soundplayer.presentation.search

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.example.soundplayer.model.PlayList
import com.example.soundplayer.model.SongWithPlayListDomain
import com.example.soundplayer.model.Sound
import com.example.soundplayer.presentation.theme.SoundPlayerTheme
import com.example.soundplayer.presentation.viewmodel.SearchUiState
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test

class SearchScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun searchScreen_updatesQueryAndSelectsPlaylistChip() {
        val playlist = playlist(id = 1, name = "Todas as músicas")
        val result =
            SongWithPlayListDomain(
                sound = sound(id = 20, title = "Yellow (Accoustic)"),
                listOfPlayLists = listOf(playlist),
            )
        var query = ""
        var selectedSoundId: Long? = null
        var selectedPlaylistId: Long? = null

        composeRule.setContent {
            SoundPlayerTheme(darkTheme = false) {
                SearchScreen(
                    query = query,
                    state = SearchUiState(results = listOf(result)),
                    onQueryChange = { query = it },
                    onBack = {},
                    onPlaylistSelected = { soundWithPlaylists, selectedPlaylist ->
                        selectedSoundId = soundWithPlaylists.sound.idSound
                        selectedPlaylistId = selectedPlaylist.idPlayList
                    },
                )
            }
        }

        composeRule.onNodeWithText("Digite o título da música").performTextInput("yellow")
        composeRule.onNodeWithText("Todas as músicas").performClick()

        assertThat(query).isEqualTo("yellow")
        assertThat(selectedSoundId).isEqualTo(20)
        assertThat(selectedPlaylistId).isEqualTo(1)
    }

    private fun playlist(
        id: Long,
        name: String,
    ) = PlayList(
        idPlayList = id,
        name = name,
        currentMusicPosition = 0,
        listSound = mutableSetOf(),
    )

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
