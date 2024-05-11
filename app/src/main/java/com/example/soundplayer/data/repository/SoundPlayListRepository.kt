package com.example.soundplayer.data.repository

import android.util.Log
import com.example.soundplayer.data.dao.PlayListDAO
import com.example.soundplayer.data.dao.PlaylistAndSoundCrossDao
import com.example.soundplayer.data.dao.SoundDao
import com.example.soundplayer.data.entities.PlayListAndSoundCrossEntity
import com.example.soundplayer.data.entities.PlayListWithSong
import com.example.soundplayer.data.entities.SoundEntity
import com.example.soundplayer.data.entities.toPlaylistWithSoundDomain
import com.example.soundplayer.data.entities.toSound
import com.example.soundplayer.model.PlayList
import com.example.soundplayer.model.PlaylistWithSoundDomain
import com.example.soundplayer.model.Sound
import com.example.soundplayer.model.toEntity
import com.example.soundplayer.model.toSoundEntity
import javax.inject.Inject


class SoundPlayListRepository @Inject constructor (
    private val playListDAO: PlayListDAO,
    private val playlistAndSoundCross :PlaylistAndSoundCrossDao,
    private val soundDao: SoundDao,
){

   suspend fun savePlayList(playList: PlayList):List<Long>{
       try {
            val list = findAllPlayListWithSong()
            list.forEach { playListDb->
                if (playListDb.playList.name == playList.name) return listOf();
            }

           val idPlayList =playListDAO.createPlayList(playList.toEntity())
           val  playlistAndSoundCrossDaoList = playList.listSound.map {soundFromPlayList->
                  PlayListAndSoundCrossEntity(
                         idPlayList,
                         soundFromPlayList.idSound!!
                     )
                 }
           return  playlistAndSoundCross.insertPlayListAndSoundCroos(playlistAndSoundCrossDaoList)

       } catch (exception: Exception) {
           exception.printStackTrace()
           throw Exception("Erro ao salvar playlist")
       }
    }

   suspend fun findAllPlayListWithSong():List<PlaylistWithSoundDomain>{
        try {
            val list =   playlistAndSoundCross.findAllPlayListWithSong()

            val  newList = list.map {playListWithSong ->
                playListWithSong.toPlaylistWithSoundDomain()
            }



            return newList

        }catch (exeption :Exception){
             exeption.printStackTrace()
            throw exeption
        }
    }

    suspend fun findSoundById(idSound :Long?):List<SoundEntity>{
    return  soundDao.findAllSound(idSound)
    }

   suspend fun saveSound(sound: Sound):Long{
       return  soundDao.createSound(sound = sound.toSoundEntity())
    }

    suspend fun sizeListSound():List<Sound>{
        return soundDao.findAllSound().map {soundEntity ->
            Sound(soundEntity)
        }
    }

   suspend fun deletePlaylist(playList: PlayList) : Int{
      try {
          val retorno = playListDAO.deletePlayList(playList = playList.toEntity())
          if (retorno != 0 && playList.listSound.isNotEmpty()){
              playlistAndSoundCross.deletePlayListAndSoundCross(playList.toEntity().playListId!!)
          }
          return retorno
      }catch (exeption : Exception){
           throw exeption;
      }
    }

    suspend fun findPlayListById(idPlayList: Long): PlayList? {
         try {
             val playListWithSong =playlistAndSoundCross.findPlayListById(idPlayList)
             if (playListWithSong != null){
                 return  PlayList(
                     idPlayList = playListWithSong.playList.playListId,
                     listSound = playListWithSong.soundOfPlayList.map { soundEntity -> soundEntity.toSound() }.toMutableSet(),
                     currentMusicPosition = playListWithSong.playList.currentSoundPosition,
                     name  =  playListWithSong.playList.title
                 )
             }
             return null
         }catch (execption :Exception){
             throw execption;
         }
    }

    suspend fun addSountToPlayList(idPlayList: Long, listToAdd :Set<Sound>):List<Long>{
        try {
            val listAcrossPlayListSound = listToAdd.map { sound ->
                PlayListAndSoundCrossEntity(
                    playListId = idPlayList,
                    soundId = sound.idSound!!
                )
            }
            return playlistAndSoundCross.insertPlayListAndSoundCroos(listAcrossPlayListSound)
        }catch (nullPointer :NullPointerException){
            nullPointer.printStackTrace()
            throw NullPointerException("erro ao adiconar musica na playlist,id da playlist não encontrado")
        }
        catch (exeption:Exception){
            throw exeption;
        }
    }

    suspend fun  removeSoundItemsFromPlayList(idPlayList: Long, soudsToRemove:Set<Sound>) :List<Int>{
         try {
            return soudsToRemove.map {sound ->
                  playlistAndSoundCross.deleteItemPlayListAndSoundCroos(idPlayList,sound.idSound!!)
             }
         }catch (exec :Exception){
             throw exec;
         }
    }

     fun compareListToUpdate(newList:MutableSet<Sound>, playList: PlayList):MutableSet<Sound> {
          val listToUpdate = mutableSetOf<Sound>()
           if (playList.listSound == newList) return listToUpdate
           if (playList.listSound != newList){
               newList.forEach { sound->
                          if (newList.contains(sound) && !playList.listSound.contains(sound)) {
                              listToUpdate.add(sound)
                          }else if(newList.contains(sound) && playList.listSound.contains(sound)){}
                 }
             }
          return listToUpdate;
    }

    suspend fun updateNamePlayList(playList: PlayList) :Int {
          try {
            return playListDAO.updatePlayList(playList.toEntity())
          } catch(exeption:Exception){
               throw  exeption;
          }
    }
}