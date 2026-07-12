package com.example.soundplayer.service

import com.example.soundplayer.HelperDataTest
import com.example.soundplayer.commons.constants.Constants
import com.example.soundplayer.data.repository.DataStorePreferenceRepository
import com.example.soundplayer.data.repository.SoundPlayListRepository
import com.example.soundplayer.data.repository.SoundRepository
import com.example.soundplayer.model.PlayList
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class DeviceSoundSyncServiceTest {
    @Mock
    lateinit var soundRepository: SoundRepository

    @Mock
    lateinit var soundPlayListRepository: SoundPlayListRepository

    @Mock
    lateinit var dataStorePreferenceRepository: DataStorePreferenceRepository

    private lateinit var service: DeviceSoundSyncService

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        service =
            DeviceSoundSyncService(
                soundRepository = soundRepository,
                soundPlayListRepository = soundPlayListRepository,
                dataStorePreferenceRepository = dataStorePreferenceRepository,
                playlistOrderingService = PlaylistOrderingService(),
            )
    }

    @Test
    fun `Given empty database, When saving device sounds, Then persist all sounds`() =
        runTest {
            val sounds = HelperDataTest.listSound()
            Mockito.`when`(soundRepository.findAllSound()).thenReturn(emptyList())
            Mockito.`when`(soundRepository.saveSound(sounds.first())).thenReturn(1L)
            Mockito.`when`(soundRepository.saveSound(sounds.last())).thenReturn(2L)

            val result = service.saveDeviceSoundsIfDatabaseIsEmpty(sounds)

            assertThat(result).containsExactly(1L, 2L).inOrder()
        }

    @Test
    fun `Given database already has songs, When saving device sounds, Then skip insert`() =
        runTest {
            val sounds = HelperDataTest.listSound()
            Mockito.`when`(soundRepository.findAllSound()).thenReturn(sounds.toList())

            val result = service.saveDeviceSoundsIfDatabaseIsEmpty(sounds)

            assertThat(result).isNull()
        }

    @Test
    fun `Given main playlist has removed system song, When syncing, Then delete missing sound`() =
        runTest {
            val sounds = HelperDataTest.listSound().toList()
            val mainPlaylist =
                PlayList(
                    idPlayList = 1L,
                    listSound = sounds.toMutableSet(),
                    name = "Todas as musicas",
                    currentMusicPosition = 0,
                )
            Mockito
                .`when`(
                    dataStorePreferenceRepository.readUserPreference(Constants.ID_ORDERED_SOUNDS_PREFERENCE),
                ).thenReturn(flowOf(0))
            Mockito.`when`(soundPlayListRepository.findPlayListById(1L)).thenReturn(mainPlaylist)
            Mockito.`when`(soundRepository.delete(sounds.last())).thenReturn(1)

            val result = service.removeMissingSoundsFromMainPlaylist(setOf(sounds.first()))

            assertThat(result).containsExactly(sounds.last())
            Mockito.verify(soundRepository).delete(sounds.last())
        }

    @Test
    fun `Given playlist does not contain every system song, When comparing, Then return missing sounds`() =
        runTest {
            val sounds = HelperDataTest.listSound().toList()
            val playlist =
                PlayList(
                    idPlayList = 2L,
                    listSound = mutableSetOf(sounds.first()),
                    name = "Playlist",
                    currentMusicPosition = 0,
                )
            Mockito
                .`when`(
                    dataStorePreferenceRepository.readUserPreference(Constants.ID_ORDERED_SOUNDS_PREFERENCE),
                ).thenReturn(flowOf(0))
            Mockito.`when`(soundPlayListRepository.findPlayListById(2L)).thenReturn(playlist)

            val result = service.findSoundsMissingFromPlaylist(sounds.toSet(), 2L)

            assertThat(result).containsExactly(sounds.last())
        }
}
