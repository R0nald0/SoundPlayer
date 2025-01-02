package com.example.soundplayer.service

import com.example.soundplayer.data.repository.SoundRepository
import com.example.soundplayer.model.Sound
import javax.inject.Inject

class SoundDomainService @Inject constructor(
    private val soundRepository: SoundRepository,
){
    suspend fun saveSound(sounds: Set<Sound>) :List<Long> {
      val linesModiefied  = sounds.map { sound->
            soundRepository.saveSound(sound)
        }
        return linesModiefied
    }
    suspend fun findAllSound() = soundRepository.findAllSound()
    suspend fun delete(sound: Sound)= soundRepository.delete(sound)
    suspend fun findSoundByTitle(title: String)= soundRepository.findSountByTitle(title)

}