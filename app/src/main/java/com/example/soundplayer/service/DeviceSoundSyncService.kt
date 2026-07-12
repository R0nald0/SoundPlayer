package com.example.soundplayer.service

import com.example.soundplayer.commons.constants.Constants
import com.example.soundplayer.data.repository.DataStorePreferenceRepository
import com.example.soundplayer.data.repository.SoundPlayListRepository
import com.example.soundplayer.data.repository.SoundRepository
import com.example.soundplayer.model.PlayList
import com.example.soundplayer.model.Sound
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class DeviceSoundSyncService
    @Inject
    constructor(
        private val soundRepository: SoundRepository,
        private val soundPlayListRepository: SoundPlayListRepository,
        private val dataStorePreferenceRepository: DataStorePreferenceRepository,
        private val playlistOrderingService: PlaylistOrderingService,
    ) {
        suspend fun saveDeviceSoundsIfDatabaseIsEmpty(soundsBySystem: Set<Sound>): List<Long>? {
            if (soundsBySystem.isEmpty()) return null

            val soundsFromDatabase = soundRepository.findAllSound()
            if (soundsFromDatabase.isNotEmpty()) return null

            return soundsBySystem.map { soundByProvider -> soundRepository.saveSound(soundByProvider) }
        }

        suspend fun removeMissingSoundsFromMainPlaylist(soundsOfSystem: Set<Sound>): List<Sound> {
            if (soundsOfSystem.isEmpty()) return emptyList()

            val soundsOfDatabase = findPlaylistByIdOrdered(MAIN_PLAYLIST_ID).listSound
            val systemTitles = soundsOfSystem.map { it.title }

            return soundsOfDatabase.filter { soundOfDatabase ->
                if (!systemTitles.contains(soundOfDatabase.title)) {
                    soundRepository.delete(soundOfDatabase)
                    true
                } else {
                    false
                }
            }
        }

        suspend fun findSoundsMissingFromPlaylist(
            soundsOfSystem: Set<Sound>,
            playlistIdToCompare: Long,
        ): Set<Sound> {
            if (soundsOfSystem.isEmpty()) return emptySet()

            val soundsOfDatabase = findPlaylistByIdOrdered(playlistIdToCompare).listSound
            val soundIds = soundsOfDatabase.map { it.idSound }

            return soundsOfSystem.filter { it.idSound !in soundIds }.toSet()
        }

        private suspend fun findPlaylistByIdOrdered(id: Long): PlayList {
            val orderBy =
                dataStorePreferenceRepository
                    .readUserPreference(key = Constants.ID_ORDERED_SOUNDS_PREFERENCE)
                    .first() ?: 0

            val playlist = soundPlayListRepository.findPlayListById(id)
            val orderedSounds = playlistOrderingService.sort(playlist.listSound.toList(), orderBy)

            return playlist.copy(listSound = orderedSounds.toMutableSet())
        }

        private companion object {
            const val MAIN_PLAYLIST_ID = 1L
        }
    }
