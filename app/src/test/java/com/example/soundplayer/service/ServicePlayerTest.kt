package com.example.soundplayer.service

import com.example.soundplayer.HelperDataTest
import com.example.soundplayer.commons.constants.Constants
import com.example.soundplayer.data.entities.toPlayList
import com.example.soundplayer.data.repository.DataStorePreferenceRepository
import com.example.soundplayer.data.repository.SoundPlayListRepository
import com.example.soundplayer.data.repository.SoundRepository
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class ServicePlayerTest {
    @Mock
    lateinit var playerRepository: PlaybackController

    @Mock
    lateinit var soundPlayListRepository: SoundPlayListRepository

    @Mock
    lateinit var soundRepository: SoundRepository

    @Mock
    lateinit var dataStorePreferenceRepository: DataStorePreferenceRepository

    lateinit var playlistOrderingService: PlaylistOrderingService

    @Mock
    lateinit var deviceSoundSyncService: DeviceSoundSyncService

    lateinit var servicePlayer: ServicePlayer

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        playlistOrderingService = PlaylistOrderingService()
        servicePlayer =
            ServicePlayer(
                playerRepository = playerRepository,
                soundRepository = soundRepository,
                dataStorePreferenceRepository = dataStorePreferenceRepository,
                soundPlayListRepository = soundPlayListRepository,
                playlistOrderingService = playlistOrderingService,
                deviceSoundSyncService = deviceSoundSyncService,
            )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Given a playlist,When playPlaylist is executed,should initialize playlist e return this`() = runTest {}

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Given sort value 0, when ordernate runs, then sorts alphabetically`() =
        runTest {
            val soundList = HelperDataTest.listSound().toList()
            val soundsOrdernated = playlistOrderingService.sort(sounds = soundList, 0)

            assertThat(soundsOrdernated.first().title).isEqualTo("Minha primeira m\u00fasica")
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Given sort value 1, when ordernate runs, then sorts alphabetically descending`() =
        runTest {
            val soundList = HelperDataTest.listSound().toList()
            val soundsOrdernated = playlistOrderingService.sort(sounds = soundList, 1)

            assertThat(soundsOrdernated.first().title).isEqualTo("Minha segunda m\u00fasica")
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Given sort value 2, when ordernate runs, then sorts by insertedDate`() =
        runTest {
            val soundList = HelperDataTest.listSound().toList()
            val soundsOrdernated = playlistOrderingService.sort(sounds = soundList, 2)

            assertThat(soundsOrdernated.first().title).isEqualTo("Minha primeira m\u00fasica")
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Given a id ,When findPlayListById is executed,should find a PlayList`() =
        runTest {
            val actualPlayLis =
                HelperDataTest.listOfPlayListEtity().map {
                    it.toPlayList()
                }

            Mockito
                .`when`(
                    dataStorePreferenceRepository.readUserPreference(Constants.ID_ORDERED_SOUNDS_PREFERENCE),
                ).thenReturn(flowOf(1))
            Mockito.`when`(playerRepository.getActualPlayList()).thenReturn(actualPlayLis.first())
        }

    @After
    fun tearDown() {
    }
}
