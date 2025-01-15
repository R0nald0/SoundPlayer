package com.example.soundplayer.data.repository

import com.example.soundplayer.data.dao.PlaylistAndSoundCrossDao
import com.example.soundplayer.data.dao.SoundDao
import com.example.soundplayer.data.entities.SoundEntity
import com.example.soundplayer.data.entities.toSongWithPlayListDomain
import com.example.soundplayer.data.entities.toSound
import com.example.soundplayer.model.SongWithPlayListDomain
import com.example.soundplayer.model.Sound
import com.example.soundplayer.model.toSoundEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SoundRepository @Inject constructor(
    private val soundDao: SoundDao,
    private val playListCrossSounddao: PlaylistAndSoundCrossDao
){
    suspend fun saveSound(sound: Sound) = soundDao.saveSound(sound.toSoundEntity())

    suspend fun findSountByTitle(title:String):Flow<SongWithPlayListDomain>{
      return  soundDao.findSoundByName(title).map { soundEntity ->
          playListCrossSounddao.findSoundByNameAndSoundId(title = soundEntity.title, soundId = soundEntity.soundId)
      }.map {
          it.toSongWithPlayListDomain()
      }

   /* return soundDao.findSoundByName(title).map { soundsList->
         soundsList.map {soundEntity ->
             playListCrossSounddao.findSoundByNameAndSoundId(title = soundEntity.title, soundId = soundEntity.soundId )
         }
     }.map{ it.map { it.toSongWithPlayListDomain() } }*/

    }

    suspend fun findSoundById(idSound :Long) = soundDao.findSoundById(idSound).toSound()

    suspend fun findAllSound():List<Sound>{
        return soundDao.findAllSound().map {soundEntity ->
            Sound(soundEntity)
        }
    }
    suspend fun delete(sound: Sound) = soundDao.deleteSound(sound.toSoundEntity())

}