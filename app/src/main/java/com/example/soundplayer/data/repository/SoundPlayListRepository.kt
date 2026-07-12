package com.example.soundplayer.data.repository

import com.example.soundplayer.data.dao.PlayListDAO
import com.example.soundplayer.data.dao.PlaylistAndSoundCrossDao
import com.example.soundplayer.data.entities.PlayListAndSoundCrossEntity
import com.example.soundplayer.data.entities.toPlaylistWithSoundDomain
import com.example.soundplayer.data.entities.toSound
import com.example.soundplayer.model.PlayList
import com.example.soundplayer.model.PlaylistWithSoundDomain
import com.example.soundplayer.model.Sound
import com.example.soundplayer.model.toEntity
import com.example.soundplayer.model.toSoundEntity
import javax.inject.Inject

class SoundPlayListRepository
    @Inject
    constructor(
        private val playListDAO: PlayListDAO,
        private val playlistAndSoundCross: PlaylistAndSoundCrossDao,
    ) {
        suspend fun savePlayList(playList: PlayList): List<Long> =
            try {
                val idPlayList = playListDAO.createPlayList(playList.toEntity())
                addSoundToPlayList(idPlayList, playList.listSound)
            } catch (repositoryException: RepositoryException) {
                throw repositoryException
            } catch (nullPointer: NullPointerException) {
                throw RepositoryException("ID inv\u00e1lido do \u00e1udio n\u00e3o encontrado")
            }

        suspend fun findAllPlayListWithSong(): List<PlaylistWithSoundDomain> {
            val playListWithSongList = playlistAndSoundCross.findAllPlayListWithSong()
            if (playListWithSongList.isEmpty()) return emptyList()

            return playListWithSongList.map { playListWithSong ->
                playListWithSong.toPlaylistWithSoundDomain()
            }
        }

        suspend fun deletePlaylist(playList: PlayList): Int =
            try {
                val playlistId =
                    playList.idPlayList
                        ?: throw RepositoryException("Erro ao deletar ${playList.name}, ID da playlist inv\u00e1lido")

                val affected = playListDAO.deletePlayList(playList = playList.toEntity())
                if (affected != 0 && playList.listSound.isNotEmpty()) {
                    playlistAndSoundCross.deletePlayListAndSoundCrossByIdPlayList(playlistId)
                }
                affected
            } catch (repositoryException: RepositoryException) {
                throw repositoryException
            } catch (nullPointerException: NullPointerException) {
                throw RepositoryException("Erro ao deletar ${playList.name}, ID da playlist inv\u00e1lido")
            }

        suspend fun findPlayListById(idPlayList: Long): PlayList =
            try {
                val playListWithSong = playListDAO.findPlayListById(idPlayList)

                PlayList(
                    idPlayList = playListWithSong.playList.playListId,
                    listSound =
                        playListWithSong.soundOfPlayList
                            .map { soundEntity -> soundEntity.toSound() }
                            .toMutableSet(),
                    currentMusicPosition = playListWithSong.playList.currentSoundPosition,
                    name = playListWithSong.playList.title,
                )
            } catch (nullPointer: NullPointerException) {
                throw RepositoryException("Erro ao buscar playlist, ID inv\u00e1lido")
            }

        suspend fun addSoundToPlayList(
            idPlayList: Long,
            listToAdd: Set<Sound>,
        ): List<Long> =
            try {
                val listAcrossPlayListSound =
                    listToAdd.map { sound ->
                        PlayListAndSoundCrossEntity(
                            playListId = idPlayList,
                            soundId = sound.toSoundEntity().soundId,
                        )
                    }

                playlistAndSoundCross.insertPlayListAndSoundCross(listAcrossPlayListSound)
            } catch (nullPointer: NullPointerException) {
                throw RepositoryException(
                    "Erro ao adicionar m\u00fasica na playlist, ID da playlist n\u00e3o encontrado",
                )
            }

        suspend fun removeSoundItemFromPlayList(
            idPlayList: Long,
            idSound: Long,
        ): Int =
            try {
                playlistAndSoundCross.deleteItemPlayListAndSoundCross(idPlayList, idSound)
            } catch (nullPointer: NullPointerException) {
                throw RepositoryException("Erro ao remover \u00e1udio da playlist")
            }

        suspend fun updateNamePlayList(
            idPlayList: Long,
            newName: String,
        ): Int =
            try {
                playListDAO.updateNamePlayList(idPlayList, name = newName)
            } catch (nullPointer: NullPointerException) {
                throw RepositoryException("Erro ao atualizar o nome da playlist")
            }
    }

data class RepositoryException(
    override val message: String?,
) : Throwable(message = message)
