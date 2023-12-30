package com.example.soundplayer.data.repository

import android.net.Uri
import com.example.soundplayer.data.dao.PlayListDAO
import com.example.soundplayer.data.dao.PlaylistAndSoundCrossDao
import com.example.soundplayer.data.dao.SoundDao
import com.example.soundplayer.data.entities.PlayListAndSoundCrossEntity
import com.example.soundplayer.model.PlayList
import com.example.soundplayer.model.Sound
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*

import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class SoundPlayListRepositoryTest {
    @Mock
    lateinit var playListDAO: PlayListDAO
    @Mock
    lateinit var  playlistAndSoundCross : PlaylistAndSoundCrossDao
    @Mock
    lateinit var  soundDao: SoundDao

    lateinit var  soundPlayListRepository: SoundPlayListRepository

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        soundPlayListRepository = SoundPlayListRepository(playListDAO,playlistAndSoundCross, soundDao)
    }

    @After
    fun tearDown() {
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `compare List Two to Update or delete`() = runTest {
        Mockito.`when`(playlistAndSoundCross.deleteItemPlayListAndSoundCroos(1,1)).thenReturn(
            1
        )

       val playListAcrosxx = soundPlayListRepository.compareListToUpdate(playList = playList , newList = listSount)


    }


    private val listSount = mutableSetOf(
        Sound(1,"caminho","12:3","teste1", Uri.EMPTY, Uri.EMPTY),
        Sound(1,"caminho","12:3","teste2", Uri.EMPTY, Uri.EMPTY),
        Sound(1,"caminho","12:3","teste3", Uri.EMPTY, Uri.EMPTY),
        Sound(1,"caminho","12:3","teste4", Uri.EMPTY, Uri.EMPTY),
        Sound(1,"caminho","12:3","teste5", Uri.EMPTY, Uri.EMPTY),
    )

    val playList = PlayList(1,"Teste Play",0,listSount)


    private val listPlaySound = listOf(
        PlayListAndSoundCrossEntity(1,1),
        PlayListAndSoundCrossEntity(1,2),
        PlayListAndSoundCrossEntity(1,4),
        PlayListAndSoundCrossEntity(1,5),
        PlayListAndSoundCrossEntity(1,22),
    )
}