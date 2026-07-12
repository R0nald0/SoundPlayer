package com.example.soundplayer.service

import com.example.soundplayer.commons.constants.Constants
import com.example.soundplayer.data.repository.DataStorePreferenceRepository
import com.example.soundplayer.data.repository.SoundPlayListRepository
import com.example.soundplayer.data.repository.SoundRepository
import com.example.soundplayer.model.PlayList
import com.example.soundplayer.model.Sound
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class ServicePlayer
    @Inject
    constructor(
        private val playerRepository: PlaybackController,
        private val soundPlayListRepository: SoundPlayListRepository,
        private val soundRepository: SoundRepository,
        private val dataStorePreferenceRepository: DataStorePreferenceRepository,
        private val playlistOrderingService: PlaylistOrderingService,
        private val deviceSoundSyncService: DeviceSoundSyncService,
    ) {
        fun playAllMusicFromFirst(listMediaItem: Set<Sound>) = playerRepository.playAllMusicFromFirst(listMediaItem)

        fun playPlaylist(playerList: PlayList) = playerRepository.playPlaylist(playerList)

        suspend fun findAllPlayList() = soundPlayListRepository.findAllPlayListWithSong()

        suspend fun findPlayListById(id: Long): PlayList {
            val orderBy =
                dataStorePreferenceRepository
                    .readUserPreference(key = Constants.ID_ORDERED_SOUNDS_PREFERENCE)
                    .first() ?: 0

            playerRepository.getActualPlayList()?.let { playlistCurrentlyPlaying ->
                if (id == playlistCurrentlyPlaying.idPlayList) {
                    val listOrdered = playlistOrderingService.sort(playlistCurrentlyPlaying.listSound.toList(), orderBy)
                    val updatedPlaylist = playlistCurrentlyPlaying.copy(listSound = listOrdered.toMutableSet())
                    playerRepository.reorderPlaylistWithMoves(listOrdered)
                    return updatedPlaylist
                }
            }

            val playListById = soundPlayListRepository.findPlayListById(id)
            val listOrdered = playlistOrderingService.sort(playListById.listSound.toList(), orderBy)

            return playListById.copy(listSound = listOrdered.toMutableSet())
        }

        suspend fun createPlayList(playList: PlayList): List<Long> {
            val playListsFromDatabase = soundPlayListRepository.findAllPlayListWithSong()
            playListsFromDatabase.forEach { playListDb ->
                if (playListDb.playList.name == playList.name) return emptyList()
            }

            if (playList.listSound.isEmpty()) return emptyList()

            return soundPlayListRepository.savePlayList(playList = playList)
        }

        suspend fun updateNamePlayList(
            id: Long,
            newName: String,
        ): Int = soundPlayListRepository.updateNamePlayList(id, newName)

        suspend fun deletePlayList(playList: PlayList): Int = soundPlayListRepository.deletePlaylist(playList)

        fun getActualPlayList() = playerRepository.getActualPlayList()

        fun getPlaybackState() = playerRepository.playbackState

        fun destroyPlayer() = playerRepository.destroyPlayer()

        fun getPlayer() = playerRepository.getPlayer()

        fun getPlaybackError() = playerRepository.getPlaybackError()

        suspend fun addItemFromListMusic(
            idPlayList: Long,
            soundsToInsertPlayList: Set<Sound>,
        ): List<Long> {
            val affectedLines = soundPlayListRepository.addSoundToPlayList(idPlayList, soundsToInsertPlayList)
            if (affectedLines.isNotEmpty()) {
                playerRepository.getActualPlayList()?.let { playlistCurrentlyPlaying ->
                    if (idPlayList == playlistCurrentlyPlaying.idPlayList) {
                        playerRepository.addItemFromListMusic(soundsToInsertPlayList)
                    }
                }
            }
            return affectedLines
        }

        suspend fun removeItemFromListMusic(
            idPlayList: Long,
            idSound: Long,
            indexSound: Int,
        ): Int {
            val affectedLines = soundPlayListRepository.removeSoundItemFromPlayList(idPlayList, idSound)
            if (affectedLines != 0) {
                if (idPlayList == 1L) {
                    val soundById = soundRepository.findSoundById(idSound)
                    soundRepository.delete(soundById)
                }
                playerRepository.getActualPlayList()?.let { playlistCurrentlyPlaying ->
                    if (playlistCurrentlyPlaying.idPlayList == idPlayList) {
                        playerRepository.removeItemFromListMusic(indexSound)
                    }
                }
            }
            return affectedLines
        }

        suspend fun saveSoundProvideFromDb(soundsBySystem: Set<Sound>): List<Long>? =
            deviceSoundSyncService.saveDeviceSoundsIfDatabaseIsEmpty(soundsBySystem)

        suspend fun verifySoundsExistInSystem(soundOfSystem: Set<Sound>): List<Sound> =
            deviceSoundSyncService.removeMissingSoundsFromMainPlaylist(soundOfSystem)

        suspend fun comparePlaylistsAndReturnDifference(
            soundOfSystem: Set<Sound>,
            idPlayListToCompare: Long,
        ): Set<Sound> = deviceSoundSyncService.findSoundsMissingFromPlaylist(soundOfSystem, idPlayListToCompare)
    }
