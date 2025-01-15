package com.example.soundplayer.service

import com.example.soundplayer.data.repository.SoundRepository
import com.example.soundplayer.model.Sound
import javax.inject.Inject

class SoundDomainService @Inject constructor(
    private val soundRepository: SoundRepository
){
    suspend fun saveSounds(sounds: Set<Sound>) :List<Long> {
        val listOfAfectedsLines = sounds.map {
            soundRepository.saveSound(sounds.first())
         }
        return listOfAfectedsLines
    }
    suspend fun findAllSound() = soundRepository.findAllSound()
    suspend fun delete(sound: Sound)= soundRepository.delete(sound)
    suspend fun findSoundByTitle(title: String)= soundRepository.findSountByTitle(title)

}