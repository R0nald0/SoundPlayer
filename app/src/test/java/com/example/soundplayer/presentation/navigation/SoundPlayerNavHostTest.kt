package com.example.soundplayer.presentation.navigation

import com.example.soundplayer.model.PlayList
import com.example.soundplayer.model.Sound
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class SoundPlayerNavHostTest {
    @Test
    fun findSoundPositionInPlaylist_matchesByStableSoundId() {
        val playlist =
            PlayList(
                idPlayList = 1,
                name = "Todas as músicas",
                currentMusicPosition = 0,
                listSound =
                    mutableSetOf(
                        sound(id = 10, title = "Intro"),
                        sound(id = 20, title = "Yellow (Accoustic)"),
                        sound(id = 30, title = "Outro"),
                    ),
            )
        val searchResultSound = sound(id = 20, title = "Yellow")

        val position = findSoundPositionInPlaylist(playlist, searchResultSound)

        assertThat(position).isEqualTo(1)
    }

    @Test
    fun findSoundPositionInPlaylist_defaultsToFirstItemWhenSoundIsMissing() {
        val playlist =
            PlayList(
                idPlayList = 1,
                name = "Todas as músicas",
                currentMusicPosition = 0,
                listSound = mutableSetOf(sound(id = 10, title = "Intro")),
            )

        val position = findSoundPositionInPlaylist(playlist, sound(id = 99, title = "Missing"))

        assertThat(position).isEqualTo(0)
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
