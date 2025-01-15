package com.example.soundplayer.service

import com.example.soundplayer.HelperDataTest
import com.example.soundplayer.data.entities.toSongWithPlayListDomain
import com.example.soundplayer.data.repository.SoundRepository
import com.example.soundplayer.model.SongWithPlayListDomain
import com.example.soundplayer.model.Sound
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class SoundDomainServiceTest {

    @Mock
    lateinit var soundRepository: SoundRepository

    private lateinit var soundDomainService: SoundDomainService

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        soundDomainService = SoundDomainService(soundRepository)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Given a Set of sound,When saveSounds is executed,should save sounds and return list of long`()= runTest {
        val soundSave = HelperDataTest.listSound().first()
        val listSounds = HelperDataTest.listSound()

        Mockito.`when`(soundRepository.saveSound(soundSave)).thenReturn(1)

        val resultLong = soundDomainService.saveSounds(listSounds)

         assertThat(resultLong.size).isEqualTo(2)


         verify(soundRepository, times(2)).saveSound(soundSave)
    }
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `When findAllSound is executed,Should find all songs`() = runTest {
        Mockito.`when`(soundRepository.findAllSound()).thenReturn(HelperDataTest.listSound().toList())

        val results = soundDomainService.findAllSound()

        assertThat(results.size).isEqualTo(2)
        assertThat(results.first()).isInstanceOf(Sound::class.java)
        verify(soundRepository, times(1)).findAllSound()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Given a sound,When delete is executed,Should remove sound and return number of afected line`() = runTest {
        val sound = HelperDataTest.listSound().first()

        Mockito.`when`(soundRepository.delete(sound)).thenReturn(1)

        val results = soundDomainService.delete(sound)

        assertThat(results).isEqualTo(1)
        verify(soundRepository, times(1)).delete(sound)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Given a  title,When findSoundByTitle is executed,Should find  Flow of SongWithPlayListDomain with this title`() = runTest {

        val flowSoundWithPlayListDomain =  flow {
            HelperDataTest.listSoundWIthPlayLists().forEach {
                emit(it.toSongWithPlayListDomain())
            }
        }
        Mockito.`when`(soundRepository.findSountByTitle(Mockito.anyString())).thenReturn(flowSoundWithPlayListDomain)

        val result = soundDomainService.findSoundByTitle("son")

        assertThat(result.toList().size).isEqualTo(5)
        assertThat(result.first()).isInstanceOf(SongWithPlayListDomain::class.java)
        assertThat(result.last().sound.title).isEqualTo("Song Five")
        verify(soundRepository, times(1)).findSountByTitle(Mockito.anyString())
    }





    @After
    fun tearDown() {
    }
}