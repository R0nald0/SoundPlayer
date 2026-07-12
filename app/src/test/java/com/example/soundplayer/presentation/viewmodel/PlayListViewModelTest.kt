package com.example.soundplayer.presentation.viewmodel

import com.example.soundplayer.HelperDataTest
import com.example.soundplayer.commons.constants.Constants
import com.example.soundplayer.data.source.MediaStoreSoundDataSource
import com.example.soundplayer.model.DataSoundPlayListToUpdate
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
@DisplayName("PlayListViewModel")
class PlayListViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    private val soundDomainService = mockk<com.example.soundplayer.service.SoundDomainService>()
    private val servicePlayer = mockk<com.example.soundplayer.service.ServicePlayer>()
    private val mediaStoreSoundDataSource = mockk<MediaStoreSoundDataSource>()

    private lateinit var viewModel: PlayListViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = PlayListViewModel(soundDomainService, servicePlayer, mediaStoreSoundDataSource)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Nested
    @DisplayName("LoadPlaylists")
    inner class LoadPlaylistsTest {
        @Test
        fun `quando LoadPlaylists, entao uiState playlists e atualizado`() =
            runTest {
                val expected = HelperDataTest.playlistWithSoundDomainList()
                coEvery { servicePlayer.findAllPlayList() } returns expected

                viewModel.onIntent(PlayListIntent.LoadPlaylists)
                advanceUntilIdle()

                assertThat(viewModel.uiState.value.playlists).isEqualTo(expected)
                assertThat(viewModel.uiState.value.isLoading).isFalse()
            }

        @Test
        fun `quando LoadPlaylists falha, entao isLoading e falso e evento de erro e emitido`() =
            runTest {
                coEvery { servicePlayer.findAllPlayList() } throws RuntimeException("Falha")

                viewModel.onIntent(PlayListIntent.LoadPlaylists)
                advanceUntilIdle()

                assertThat(viewModel.uiState.value.isLoading).isFalse()
            }
    }

    @Nested
    @DisplayName("CountSounds")
    inner class CountSoundsTest {
        @Test
        fun `quando CountSounds, entao soundListSize e atualizado`() =
            runTest {
                coEvery { soundDomainService.findAllSound() } returns HelperDataTest.listSound().toList()

                viewModel.onIntent(PlayListIntent.CountSounds)
                advanceUntilIdle()

                assertThat(viewModel.uiState.value.soundListSize).isEqualTo(2)
            }
    }

    @Nested
    @DisplayName("FindPlayListById")
    inner class FindPlayListByIdTest {
        @Test
        fun `dado id valido, quando FindPlayListById, entao selectedPlayList e atualizado`() =
            runTest {
                val expected =
                    HelperDataTest
                        .playlistWithSoundDomainList()
                        .first()
                        .playList
                        .copy(idPlayList = 2L)
                coEvery { servicePlayer.findPlayListById(2L) } returns expected

                viewModel.onIntent(PlayListIntent.FindPlayListById(2L))
                advanceUntilIdle()

                assertThat(viewModel.uiState.value.selectedPlayList).isEqualTo(expected)
            }
    }

    @Nested
    @DisplayName("ClearSelectedPlayList")
    inner class ClearSelectedPlayListTest {
        @Test
        fun `quando ClearSelectedPlayList, entao selectedPlayList e nulo`() =
            runTest {
                val playlist = HelperDataTest.playlistWithSoundDomainList().first().playList
                coEvery { servicePlayer.findPlayListById(any()) } returns playlist

                viewModel.onIntent(PlayListIntent.FindPlayListById(1L))
                advanceUntilIdle()
                assertThat(viewModel.uiState.value.selectedPlayList).isNotNull()

                viewModel.onIntent(PlayListIntent.ClearSelectedPlayList)
                advanceUntilIdle()

                assertThat(viewModel.uiState.value.selectedPlayList).isNull()
            }
    }

    @Nested
    @DisplayName("SaveAllSoundsByContentProvider")
    inner class SaveAllSoundsByContentProviderTest {
        @Test
        fun `dado sons do sistema, quando salvar todos, entao cria playlist principal no viewmodel`() =
            runTest {
                val sounds = HelperDataTest.listSound()

                coEvery { servicePlayer.saveSoundProvideFromDb(sounds) } returns listOf(1L)
                coEvery { soundDomainService.findAllSound() } returns sounds.toList()
                coEvery { servicePlayer.createPlayList(any()) } returns listOf(1L)
                coEvery { servicePlayer.findAllPlayList() } returns emptyList()

                viewModel.onIntent(PlayListIntent.SaveAllSoundsByContentProvider(sounds))
                advanceUntilIdle()

                coVerify(exactly = 1) {
                    servicePlayer.createPlayList(
                        match { playlist ->
                            playlist.name == Constants.ALL_MUSIC_NAME &&
                                playlist.currentMusicPosition == 0 &&
                                playlist.listSound == sounds.toMutableSet()
                        },
                    )
                }
                assertThat(viewModel.uiState.value.soundListSize).isEqualTo(sounds.size)
            }

        @Test
        fun `dado intent LoadSoundsFromDevice, quando media store retorna sons, entao salva sons do dispositivo`() =
            runTest {
                val sounds = HelperDataTest.listSound()

                every { mediaStoreSoundDataSource.findDeviceSounds() } returns sounds
                coEvery { servicePlayer.saveSoundProvideFromDb(sounds) } returns listOf(1L)
                coEvery { soundDomainService.findAllSound() } returns sounds.toList()
                coEvery { servicePlayer.createPlayList(any()) } returns listOf(1L)
                coEvery { servicePlayer.findAllPlayList() } returns emptyList()

                viewModel.onIntent(PlayListIntent.LoadSoundsFromDevice)
                advanceUntilIdle()

                verify(exactly = 1) { mediaStoreSoundDataSource.findDeviceSounds() }
                assertThat(viewModel.uiState.value.soundListSize).isEqualTo(sounds.size)
            }
    }

    @Nested
    @DisplayName("UpdateSoundAtPlaylist — BUG-04 race condition")
    inner class UpdateSoundAtPlaylistTest {
        @Test
        fun `dado chamadas concorrentes, quando UpdateSoundAtPlaylist, entao estado final e consistente`() =
            runTest {
                val sound = HelperDataTest.listSound().first()
                val playlist =
                    HelperDataTest
                        .playlistWithSoundDomainList()
                        .first()
                        .playList
                        .copy(idPlayList = 2L)
                val data =
                    DataSoundPlayListToUpdate(
                        idPlayList = 2L,
                        positionSound = listOf(0),
                        sounds = setOf(sound),
                    )
                val expectedPlaylists = HelperDataTest.playlistWithSoundDomainList()

                coEvery { servicePlayer.addItemFromListMusic(any(), any()) } returns listOf(1L)
                coEvery { servicePlayer.findPlayListById(2L) } returns playlist
                coEvery { servicePlayer.findAllPlayList() } returns expectedPlaylists

                viewModel.onIntent(PlayListIntent.UpdateSoundAtPlaylist(data))
                viewModel.onIntent(PlayListIntent.UpdateSoundAtPlaylist(data))
                viewModel.onIntent(PlayListIntent.UpdateSoundAtPlaylist(data))
                advanceUntilIdle()

                assertThat(viewModel.uiState.value.selectedPlayList).isEqualTo(playlist)
                assertThat(viewModel.uiState.value.playlists).isEqualTo(expectedPlaylists)
            }

        @Test
        fun `dado playlist id 1, quando UpdateSoundAtPlaylist, entao soundListSize e atualizado`() =
            runTest {
                val sound = HelperDataTest.listSound().first()
                val playlist =
                    HelperDataTest
                        .playlistWithSoundDomainList()
                        .first()
                        .playList
                        .copy(idPlayList = 1L)
                val data =
                    DataSoundPlayListToUpdate(
                        idPlayList = 1L,
                        positionSound = listOf(0),
                        sounds = setOf(sound),
                    )

                coEvery { servicePlayer.addItemFromListMusic(any(), any()) } returns listOf(1L)
                coEvery { servicePlayer.findPlayListById(1L) } returns playlist
                coEvery { servicePlayer.findAllPlayList() } returns emptyList()
                coEvery { soundDomainService.findAllSound() } returns HelperDataTest.listSound().toList()

                viewModel.onIntent(PlayListIntent.UpdateSoundAtPlaylist(data))
                advanceUntilIdle()

                assertThat(viewModel.uiState.value.soundListSize).isEqualTo(2)
            }
    }

    @Nested
    @DisplayName("RemoveSoundFromPlayList")
    inner class RemoveSoundFromPlayListTest {
        @Test
        fun `dado som valido, quando RemoveSoundFromPlayList, entao estado e atualizado apos remocao`() =
            runTest {
                val sound = HelperDataTest.listSound().first()
                val playlist =
                    HelperDataTest
                        .playlistWithSoundDomainList()
                        .first()
                        .playList
                        .copy(idPlayList = 2L)
                val data =
                    DataSoundPlayListToUpdate(
                        idPlayList = 2L,
                        positionSound = listOf(0),
                        sounds = setOf(sound),
                    )

                coEvery { servicePlayer.removeItemFromListMusic(any(), any(), any()) } returns 1
                coEvery { servicePlayer.findPlayListById(2L) } returns playlist
                coEvery { servicePlayer.findAllPlayList() } returns emptyList()

                viewModel.onIntent(PlayListIntent.RemoveSoundFromPlayList(data))
                advanceUntilIdle()

                coVerify(exactly = 1) { servicePlayer.removeItemFromListMusic(2L, sound.idSound, 0) }
                assertThat(viewModel.uiState.value.selectedPlayList).isEqualTo(playlist)
            }

        @Test
        fun `dado remocao sem linhas afetadas, entao selectedPlayList permanece nulo`() =
            runTest {
                val sound = HelperDataTest.listSound().first()
                val data =
                    DataSoundPlayListToUpdate(
                        idPlayList = 2L,
                        positionSound = listOf(0),
                        sounds = setOf(sound),
                    )

                coEvery { servicePlayer.removeItemFromListMusic(any(), any(), any()) } returns 0

                viewModel.onIntent(PlayListIntent.RemoveSoundFromPlayList(data))
                advanceUntilIdle()

                assertThat(viewModel.uiState.value.selectedPlayList).isNull()
            }
    }
}
