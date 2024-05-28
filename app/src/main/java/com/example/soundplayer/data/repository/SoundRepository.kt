package com.example.soundplayer.data.repository

import com.example.soundplayer.data.dao.SoundDao
import com.example.soundplayer.data.entities.SoundEntity
import com.example.soundplayer.model.Sound
import com.example.soundplayer.model.toSoundEntity
import javax.inject.Inject

class SoundRepository @Inject constructor(
    private val soundDao: SoundDao
){
    suspend fun saveSound(sound: Sound):Long{
        return  soundDao.createSound(sound = sound.toSoundEntity())
    }

    suspend fun findSoundById(idSound :Long?):List<SoundEntity>{
        return  soundDao.findAllSound(idSound)
    }
    suspend fun sizeListSound():List<Sound>{
        return soundDao.findAllSound().map {soundEntity ->
            Sound(soundEntity)
        }
    }
    suspend fun delete(sound: Sound):Int{
      return  soundDao.deleteSound(sound.toSoundEntity())
    }
}