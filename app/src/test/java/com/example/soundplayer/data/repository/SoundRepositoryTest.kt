package com.example.soundplayer.data.repository


import com.example.soundplayer.HelperDataTest
import com.example.soundplayer.data.dao.PlaylistAndSoundCrossDao
import com.example.soundplayer.data.dao.SoundDao
import com.example.soundplayer.data.entities.SoundEntity
import com.example.soundplayer.model.Sound
import com.example.soundplayer.model.toSoundEntity
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
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
class SoundRepositoryTest {

    @Mock
    lateinit var soundDao: SoundDao

    @Mock
    lateinit var playlistAndSoundCrossDao: PlaylistAndSoundCrossDao

    private lateinit var sounRepository : SoundRepository

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
         sounRepository = SoundRepository(soundDao = soundDao, playListCrossSounddao = playlistAndSoundCrossDao)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun given_sound_when_saveSound_is_executed_should_save_sound_and_return_id() = runTest {
    val sound  =  Sound(
            idSound = 32,
            title = "Minha primeira música",
            duration = "3:40",
            albumName = "Nome alnbum",
            artistName = "Altor",
            path = "path",
            uriMediaAlbum = "",
            uriMedia = "",
            insertedDate = 2313231
        )
        val expectedId = 1L
        Mockito.`when`(soundDao.saveSound(sound.toSoundEntity())).thenReturn(expectedId)

        val result = sounRepository.saveSound(sound)

        assertThat(result).isEqualTo(1L)
        verify(soundDao, times(1)).saveSound(sound.toSoundEntity())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun given_sound_when_deleteSound_is_executed_should_delete_sound_and_return_number_of_lines_correctly_deleted() = runTest {
        val sound =HelperDataTest.listSound().first()
        Mockito.`when`(soundDao.deleteSound(sound.toSoundEntity())).thenReturn(1)
        val result = sounRepository.delete(sound)

        assertThat(result).isEqualTo(1)
        verify(soundDao, times(1)).deleteSound(sound.toSoundEntity())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Given sound title,When findSoundByName is executed,should find a flow of list of sound with title`()= runTest {
       val flowOfSoundEntity  = flow<SoundEntity> {
              HelperDataTest.soundEntityList().forEach {
                   emit(it)
              }
          }

        Mockito.`when`(soundDao.findSoundByName(Mockito.anyString())).thenReturn(flowOfSoundEntity)
        Mockito.`when`(
             playlistAndSoundCrossDao
                 .findSoundByNameAndSoundId(title =Mockito.anyString() , soundId = Mockito.anyLong())
        ).thenReturn(HelperDataTest.listSoundWIthPlayLists().last())

        val result   = sounRepository.findSountByTitle("Song").first()
        assertThat(result.sound.title).isEqualTo("Song Five")
        verify(soundDao, times(1)).findSoundByName(Mockito.anyString())
        verify(playlistAndSoundCrossDao, times(1)).findSoundByNameAndSoundId(Mockito.anyString(),Mockito.anyLong())
    }

     @OptIn(ExperimentalCoroutinesApi::class)
     @Test
     fun `Given a String,When findSoundById is executed,should find a sound `()= runTest{
            val sound = HelperDataTest.soundEntityList().first()
            Mockito.`when`(soundDao.findSoundById(Mockito.anyLong())).thenReturn(sound)

         val resultSound = sounRepository.findSoundById(1)
         assertThat(resultSound.title).isEqualTo("Song One")
         assertThat(resultSound).isInstanceOf(Sound::class.java)
         verify(soundDao, times(1)).findSoundById(Mockito.anyLong())
     }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `When findAllSound is executed,should return a list of sounds`()= runTest{
        val soundsEntity = HelperDataTest.soundEntityList()
        Mockito.`when`(soundDao.findAllSound()).thenReturn(soundsEntity)

        val resultSound = sounRepository.findAllSound()
        assertThat(resultSound.first().title).isEqualTo("Song One")
        assertThat(resultSound.first()).isInstanceOf(Sound::class.java)
        assertThat(resultSound.size).isEqualTo(5)
        assertThat(resultSound).isNotEmpty()
        verify(soundDao, times(1)).findAllSound()
    }

    @After
    fun tearDown() {}


}