package com.example.soundplayer.data.repository

import com.example.soundplayer.data.dao.PlaylistAndSoundCrossDao
import com.example.soundplayer.data.dao.SoundDao
import com.example.soundplayer.data.entities.SongWithPlaylists
import com.example.soundplayer.data.entities.SoundEntity
import com.example.soundplayer.data.entities.toSongWithPlayListDomain
import com.example.soundplayer.data.entities.toSound
import com.example.soundplayer.model.SongWithPlayListDomain
import com.example.soundplayer.model.Sound
import com.example.soundplayer.model.toSoundEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.reduce
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.flow.toList
import javax.inject.Inject

class SoundRepository @Inject constructor(
    private val soundDao: SoundDao,
    private val playListCrossSounddao: PlaylistAndSoundCrossDao
){
    suspend fun saveSound(sound: Sound):Long{
        return  soundDao.saveSound(sound = sound.toSoundEntity())
    }

    suspend fun findSountByTitle(title:String):Flow<List<SongWithPlayListDomain>>{
    return soundDao.findSoundByName(title).map { soundsList->
         soundsList.map {soundEntity ->
             playListCrossSounddao.findSoundByNameAndSoundId(title = soundEntity.title, soundId = soundEntity.soundId )
         }
     }.map{ it.map { it.toSongWithPlayListDomain() } }
    }

    suspend fun findSoundById(idSound :Long?):SoundEntity{
        return  soundDao.findSoundById(idSound)
    }
    suspend fun findAllSound():List<Sound>{
        return soundDao.findAllSound().map {soundEntity ->
            Sound(soundEntity)
        }
    }
    suspend fun findAllSoundTiitle():List<String> = soundDao.findAllSoundTiitle()
    suspend fun delete(sound: Sound):Int{
      return  soundDao.deleteSound(sound.toSoundEntity())
    }
}