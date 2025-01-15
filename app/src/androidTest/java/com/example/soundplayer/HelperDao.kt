package com.example.soundplayer

import com.example.soundplayer.data.dao.PlayListDAO
import com.example.soundplayer.data.dao.SoundDao
import com.example.soundplayer.data.entities.PlayListEntity
import com.example.soundplayer.data.entities.SoundEntity

object HelperDao {
    suspend fun createAndSavedPlaylistWithSounds(playListDAO: PlayListDAO, soundDao: SoundDao) {
        val playList = PlayListEntity(
            playListId = 1,
            title = "Relaxing Music",
            currentSoundPosition = 1
        )
        val playList2 = PlayListEntity(
            playListId = 2,
            title = "Study Music",
            currentSoundPosition = 1
        )
        val sound1 = SoundEntity(soundId = 3, title = "Upbeat Track", duration = "240")
        val sound2 = SoundEntity(soundId = 4, title = "Motivational Beat", duration = "300")
        playListDAO.createPlayList(playList)
        playListDAO.createPlayList(playList2)

        soundDao.saveSound(sound1)
        soundDao.saveSound(sound2)
    }

    fun listOfPlayList() = listOf(
        PlayListEntity(
            playListId = null,
            title = "First playlist",
            currentSoundPosition = 0,
        ), PlayListEntity(
            playListId = null,
            title = "Second playlist",
            currentSoundPosition = 0,
        ), PlayListEntity(
            playListId = null,
            title = "Third playlist",
            currentSoundPosition = 0,
        ), PlayListEntity(
            playListId = null,
            title = "Fourth playlist",
            currentSoundPosition = 0,
        ),

    )
}