package com.example.soundplayer.data.repository

import android.util.Log
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


class SoundPlayListRepository @Inject constructor(
    private val playListDAO: PlayListDAO,
    private val playlistAndSoundCross: PlaylistAndSoundCrossDao,
) {

    suspend fun savePlayList(playList: PlayList): List<Long> {
       try {
           val idPlayList = playListDAO.createPlayList(playList.toEntity())
          return  addSoundToPlayList(idPlayList,playList.listSound)

       }catch (repositoryException :RepositoryException){
           repositoryException.printStackTrace()
           throw repositoryException
       }catch ( nullPointer : NullPointerException){
           nullPointer.printStackTrace()
           throw RepositoryException("Id inválido do áudio não encontrado")
       }
    }

    suspend fun findAllPlayListWithSong(): List<PlaylistWithSoundDomain> {

            val playListWithSongList = playlistAndSoundCross.findAllPlayListWithSong()

            if (playListWithSongList.isEmpty()) return emptyList()

            return playListWithSongList.map { playListWithSong ->
                playListWithSong.toPlaylistWithSoundDomain()
            }


    }

    suspend fun deletePlaylist(playList: PlayList): Int {
        try {
            val retorno = playListDAO.deletePlayList(playList = playList.toEntity())
            if (retorno != 0 && playList.listSound.isNotEmpty()) {
                playlistAndSoundCross.deletePlayListAndSoundCrossByIdPlayList(playList.toEntity().playListId!!)
            }
            return retorno
        } catch (nullPointerException: NullPointerException) {
           // Log.i("ERROR", "deletePlaylist: ${nullPointerException.message}")
            nullPointerException.printStackTrace()
            throw RepositoryException(message = "Erro ao deletar ${playList.name},Id da playList inválido");
        }
    }

    suspend fun findPlayListById(idPlayList: Long): PlayList {
        try {
            val playListWithSong = playlistAndSoundCross.findPlayListById(idPlayList)

            return PlayList(
                idPlayList = playListWithSong.playList.playListId,
                listSound = playListWithSong.soundOfPlayList.map { soundEntity -> soundEntity.toSound() }
                    .toMutableSet(),
                currentMusicPosition = playListWithSong.playList.currentSoundPosition,
                name = playListWithSong.playList.title
            )

        } catch (nullPointer: NullPointerException) {
            //Log.e("ERRO", "findPlayListById: ${nullPointer.message} ", )
            nullPointer.printStackTrace()
            throw RepositoryException(message = "Erro ao bucar playList,id inválido");
        }
    }

    suspend fun addSoundToPlayList(idPlayList: Long, listToAdd: Set<Sound>): List<Long> {
        try {
            val listAcrossPlayListSound = listToAdd.map { sound ->
                PlayListAndSoundCrossEntity(
                    playListId = idPlayList,
                    soundId = sound.toSoundEntity().soundId
                )
            }

            return playlistAndSoundCross.insertPlayListAndSoundCroos(listAcrossPlayListSound)

        } catch (nullPointer: NullPointerException) {
            nullPointer.printStackTrace()
            throw RepositoryException("erro ao adicionar música na playlist,id da playlist não encontrado")
        }
    }

    suspend fun removeSoundItemFromPlayList(idPlayList: Long, idSound: Long): Int {
        try {
            return playlistAndSoundCross.deleteItemPlayListAndSoundCroos(idPlayList, idSound)
        } catch (nullPointer:NullPointerException) {
            nullPointer.printStackTrace()
            throw RepositoryException("Erro ao remover áudio da playlist");
        }
    }

    suspend fun updateNamePlayList(idPlayList: Long, newName: String): Int {
        try {
            return playListDAO.updateNamePlayList(idPlayList, name = newName)
        } catch (nullPointer:NullPointerException) {
            nullPointer.printStackTrace()
            throw RepositoryException("Erro ao atualizar o nome da playList");
        }
    }

}

data class RepositoryException(override val message: String?) : Throwable(message = message)
