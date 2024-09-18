package com.example.soundplayer.service

import androidx.lifecycle.MutableLiveData
import com.example.soundplayer.commons.constants.Constants
import com.example.soundplayer.data.entities.toSound
import com.example.soundplayer.data.repository.DataStorePreferenceRepository
import com.example.soundplayer.data.repository.PlayerRepository
import com.example.soundplayer.data.repository.SoundPlayListRepository
import com.example.soundplayer.data.repository.SoundRepository
import com.example.soundplayer.model.PlayList
import com.example.soundplayer.model.Sound
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class ServicePlayer @Inject constructor(
    private val playerRepository: PlayerRepository,
    private val soundPlayListRepository: SoundPlayListRepository,
    private val soundRepository: SoundRepository,
    private val dataStorePreferenceRepository: DataStorePreferenceRepository
) {

    fun playAllMusicFromFist(listMediaItem : Set<Sound>) = playerRepository.playAllMusicFromFist(listMediaItem)
    fun playPlaylist(playerList: PlayList): PlayList? {
        return playerRepository.getAllMusics(playerList)
    }

    suspend fun findAllPlayList() = soundPlayListRepository.findAllPlayListWithSong()

    private fun ordernate(soundList: MutableSet<Sound>, ondernateType: Int): MutableSet<Sound> {
        return when (ondernateType) {
            0 -> soundList.sortedBy { it.title }.toMutableSet()
            1 -> soundList.sortedByDescending { it.title }.toMutableSet()
            2 -> soundList.sortedBy { it.insertedDate }.toMutableSet()
            else -> {
                soundList
            }
        }
    }

    suspend  fun findPlayListById(id: Long): PlayList? {
        val orderBy = dataStorePreferenceRepository
            .readUserPrefference(key = Constants.ID_ORDERED_SONS_PREFFERENCE)
            .first() ?: 0

        playerRepository.getAcutalPlayList()?.let { playlistCurrentlyPlaying ->
            if (id == playlistCurrentlyPlaying.idPlayList) {
                return playlistCurrentlyPlaying
            }
        }

        val playListById = soundPlayListRepository.findPlayListById(id)
        val listOrdered = ordernate(playListById!!.listSound, orderBy)
        return playListById.copy(listSound = listOrdered)
    }

    suspend fun createPlayList(playList: PlayList): List<Long> {
        return soundPlayListRepository.savePlayList(playList = playList)
    }

    suspend fun updateNamePlayList(id: Long, newName: String): Int {
        return soundPlayListRepository.updateNamePlayList(id, newName)
    }

    suspend fun deletePlayList(playList: PlayList): Int {
        return soundPlayListRepository.deletePlaylist(playList)
    }

    suspend fun getActualSound(): MutableLiveData<Sound> {

        return playerRepository.getActaulSound()
    }

    fun getActualPlayList() = playerRepository.getAcutalPlayList()
    fun isiPlaying() = playerRepository.isPlaying()

    fun destroyPlayer() = playerRepository.destroyPlayer()
    fun getPlayer() = playerRepository.getPlayer()
    suspend fun getPlayBackError() = playerRepository.getPlayBackError()

    suspend fun addItemFromListMusic(
        idPlayList: Long,
        soundsToInsertPlayList: Set<Sound>
    ): List<Long> {

        val afectedLines =
            soundPlayListRepository.addSountToPlayList(idPlayList, soundsToInsertPlayList)
        if (afectedLines.isNotEmpty()) {
            playerRepository.getAcutalPlayList()?.let { playlistCurrentlyPlaying ->
                if (idPlayList == playlistCurrentlyPlaying.idPlayList) {
                    playerRepository.addItemFromListMusic(soundsToInsertPlayList)
                }
            }
        }
        return afectedLines
    }

    suspend fun removeItemFromListMusic(idPlayList: Long, idSound: Long, indexSound: Int): Int {

        val afectedLines = soundPlayListRepository.removeSoundItemFromPlayList(idPlayList, idSound)
        if (afectedLines != 0) {
            if (idPlayList == 1L) {
                val soundById = soundRepository.findSoundById(idSound).toSound()
                soundRepository.delete(soundById)
            }
            playerRepository.getAcutalPlayList()?.let { playlistCurrentlyPlaying ->
                if (playlistCurrentlyPlaying.idPlayList?.equals(idPlayList) == true) {
                    playerRepository.removeItemFromListMusic(indexSound)
                }
            }
        }
        return afectedLines
    }

    suspend fun saveSoundProvideFromDb(soundsBySystem: Set<Sound>): List<Long>? {

        if (soundsBySystem.isEmpty()) return null
        val listSoundBd = soundRepository.findAllSound()

        if (listSoundBd.isNotEmpty()) return null
        val afactedLines = mutableListOf<Long>();
        val result = verificaSeSoundContemNoDatabase(soundsBySystem)
        afactedLines.add(result)
        return afactedLines
    }


    private suspend fun verificaSeSoundContemNoDatabase(
        soundsBySystemList: Set<Sound>,
    ): Long {
        val result = soundsBySystemList.map { soundByProvider ->
            soundRepository.saveSound(soundByProvider)
        }.first()

        return result
    }

    suspend fun verifcaSeSoundContemNoSistema(soundOfSystem: Set<Sound>): List<Sound> {
        if (soundOfSystem == emptySet<Sound>()) return emptyList()
        val soundsOfDatbase = findPlayListById(1) ?: return emptyList()

        val system = soundOfSystem.map { it.title }

        val removedSounds = soundsOfDatbase.listSound.filter { soundOfDatabase ->
            if (!system.contains(soundOfDatabase.title)) {
                soundRepository.delete(soundOfDatabase)
                true
            } else {
                false
            }
        }

        return removedSounds
    }
    suspend fun comparePlayListsAndReturnDiference(soundOfSystem: Set<Sound>, idPlayListToCompare: Long):Set<Sound>{
        if (soundOfSystem == emptySet<Sound>()) return emptySet()
        val soundsOfDatbase = findPlayListById(idPlayListToCompare)?.listSound ?: return emptySet()

        val soundsId = soundsOfDatbase.map { it.idSound }
        val soundsDiff= soundOfSystem.filter { it.idSound !in soundsId }.toSet()
        return soundsDiff

    }
}
