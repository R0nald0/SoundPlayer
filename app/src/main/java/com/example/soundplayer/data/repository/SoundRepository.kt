package com.example.soundplayer.data.repository

import com.example.soundplayer.data.dao.SoundDao
import com.example.soundplayer.data.entities.SoundEntity
import com.example.soundplayer.data.entities.toSound
import com.example.soundplayer.model.Sound
import com.example.soundplayer.model.toSoundEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SoundRepository @Inject constructor(
    private val soundDao: SoundDao
){
    suspend fun saveSound(sound: Sound):Long{
        return  soundDao.saveSound(sound = sound.toSoundEntity())
    }

    suspend fun findSountByTitle(title:String):Flow<List<Sound>>{
      return  soundDao.findSoundByName(title).map {
            it.map { it.toSound() }
        }

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