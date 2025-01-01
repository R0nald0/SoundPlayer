package com.example.soundplayer.data.repository

import android.net.Uri
import com.example.soundplayer.data.dao.PlaylistAndSoundCrossDao
import com.example.soundplayer.data.dao.SoundDao
import com.example.soundplayer.data.entities.SoundEntity
import com.example.soundplayer.data.entities.toSound
import com.example.soundplayer.model.Sound
import com.example.soundplayer.model.toSoundEntity
import com.google.common.truth.ExpectFailure
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
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
    fun  saveSound_should_save_sound() = runTest {
        val soundEntity = SoundEntity(
            path = "",
            title = "Minha primeira música",
            duration = "3232",
            albumName = "",
            soundId = 1L,
            urlAlbumImage = "",
            urlMediaImage = "",
            artistsName = ""
        )

         Mockito.`when`(soundDao.saveSound(sound = soundEntity)).thenReturn(
           1L
       )

        val result = sounRepository.saveSound(soundEntity.toSound())

        assertThat(result).isEqualTo(1L)
        Mockito.verify(soundDao).saveSound(soundEntity)

    }
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun  deleteSound_should_delete_sound() = runTest {
        val sound =listSount.first()
        Mockito.`when`(soundDao.deleteSound(sound = sound.toSoundEntity())).thenReturn(
            1
        )

        val result = sounRepository.delete(sound)

        assertThat(result).isEqualTo(1)

    }






    @After
    fun tearDown() {
    }


    private val listSount = mutableSetOf(
        Sound(
            idSound = 1L,
            title = "Minha primeira música",
            duration = "3:40",
            albumName = "Nome alnbum",
            artistName = "Autor",
            path = "path",
            uriMediaAlbum = "",
            uriMedia = "",
            insertedDate = 2313231
        ),
        Sound(
            idSound = 2L,
            title = "Minha segunda música",
            duration = "3:40",
            albumName = "Nome alnbum",
            artistName = "Altor",
            path = "path",
            uriMediaAlbum = "",
            uriMedia = "",
            insertedDate = 2313231
        )
    )
}