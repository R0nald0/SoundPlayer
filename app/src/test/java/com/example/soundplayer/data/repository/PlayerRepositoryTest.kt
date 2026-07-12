package com.example.soundplayer.data.repository

import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.exoplayer.ExoPlayer
import com.example.soundplayer.model.PlayList
import com.example.soundplayer.model.Sound
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
@DisplayName("PlayerRepository")
class PlayerRepositoryTest {
    private val exoPlayer: ExoPlayer = mockk(relaxed = true)
    private val repository = PlayerRepository(exoPlayer)

    @Test
    fun `dado playlist valida, quando playPlaylist, entao prepara player e retorna playlist`() =
        runTest {
            every { exoPlayer.isPlaying } returns false
            every { exoPlayer.mediaItemCount } returns 0
            every { exoPlayer.currentMediaItem } returns mediaItem()
            every { exoPlayer.duration } returns 125_000L
            val playlist =
                PlayList(
                    idPlayList = 1L,
                    name = "Playlist",
                    currentMusicPosition = 0,
                    listSound = mutableSetOf(sound()),
                )

            val result = repository.playPlaylist(playlist)

            assertThat(result).isEqualTo(playlist)
            assertThat(repository.playbackState.value.currentPlayList).isEqualTo(playlist)
            verify(exactly = 1) { exoPlayer.seekTo(0, 0L) }
            verify(exactly = 1) { exoPlayer.addMediaItems(any()) }
            verify(exactly = 1) { exoPlayer.prepare() }
        }

    @Test
    fun `dado playlist vazia, quando playPlaylist, entao retorna null e emite erro`() =
        runTest {
            val events = collectPlaybackErrors()
            val playlist =
                PlayList(
                    idPlayList = 1L,
                    name = "Playlist vazia",
                    currentMusicPosition = 0,
                    listSound = mutableSetOf(),
                )

            val result = repository.playPlaylist(playlist)

            assertThat(result).isNull()
            assertThat(events).hasSize(1)
            assertThat(events.first().message).isEqualTo("Erro ao inicializar a playlist")
        }

    @Test
    fun `dado posicao invalida, quando playPlaylist, entao retorna null e emite erro`() =
        runTest {
            val events = collectPlaybackErrors()
            val playlist =
                PlayList(
                    idPlayList = 1L,
                    name = "Playlist",
                    currentMusicPosition = 3,
                    listSound = mutableSetOf(sound()),
                )

            val result = repository.playPlaylist(playlist)

            assertThat(result).isNull()
            assertThat(events).hasSize(1)
            assertThat(events.first().message).isEqualTo("Erro ao inicializar a playlist")
        }

    @Test
    fun `dado falha do player, quando playPlaylist, entao retorna null e emite erro`() =
        runTest {
            val events = collectPlaybackErrors()
            every { exoPlayer.isPlaying } returns false
            every { exoPlayer.seekTo(any<Int>(), any<Long>()) } throws IllegalStateException("player indisponivel")
            val playlist =
                PlayList(
                    idPlayList = 1L,
                    name = "Playlist",
                    currentMusicPosition = 0,
                    listSound = mutableSetOf(sound()),
                )

            val result = repository.playPlaylist(playlist)

            assertThat(result).isNull()
            assertThat(events).hasSize(1)
            assertThat(events.first().message).isEqualTo("Erro ao inicializar a playlist")
        }

    private fun TestScope.collectPlaybackErrors(): MutableList<Throwable> {
        val events = mutableListOf<Throwable>()
        launch(UnconfinedTestDispatcher(testScheduler)) {
            repository.getPlaybackError().take(1).toList(events)
        }
        return events
    }

    private fun sound() =
        Sound(
            idSound = 1L,
            artistName = "Artist",
            albumName = "Album",
            title = "Song",
            uriMedia = "file:///music.mp3",
            uriMediaAlbum = null,
            insertedDate = 1L,
        )

    private fun mediaItem() =
        MediaItem
            .Builder()
            .setMediaId("1")
            .setMediaMetadata(
                MediaMetadata
                    .Builder()
                    .setTitle("Song")
                    .setDisplayTitle("Song")
                    .setArtist("Artist")
                    .setAlbumTitle("Album")
                    .build(),
            ).build()
}
