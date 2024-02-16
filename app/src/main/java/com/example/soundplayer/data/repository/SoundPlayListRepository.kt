package com.example.soundplayer.data.repository

import com.example.soundplayer.data.dao.PlayListDAO
import com.example.soundplayer.data.dao.PlaylistAndSoundCrossDao
import com.example.soundplayer.data.dao.SoundDao
import com.example.soundplayer.data.entities.PlayListAndSoundCrossEntity
import com.example.soundplayer.data.entities.toSound
import com.example.soundplayer.model.PlayList
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
            list.forEach {
                if (it.name == playList.name) return listOf();
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
           throw Exception("Erro ao salvar play list")
       }
    }

   suspend fun findAllPlayListWithSong():List<PlayList>{
        try {
            val listPlayListWithSong  =  playlistAndSoundCross.findAllPlayListWithSong()
            val listPlayList = listPlayListWithSong.map { playListWithSound->
                  PlayList(playListWithSound)
            }
            return  listPlayList
        }catch (exeption :Exception){
            throw exeption
        }
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
          var retorno = playListDAO.deletePlayList(playList = playList.toEntity())
          if (retorno != 0){
              retorno=  playlistAndSoundCross.deletePlayListAndSoundCross(playList.toEntity().playListId!!)
          }
          return retorno
      }catch (exeption : Exception){
           throw exeption;
      }
    }

    suspend fun findPlayListById(idPlayList: Long): PlayList {
         try {
             val playList =playlistAndSoundCross.findPlayListById(idPlayList)
             return  PlayList(
                 idPlayList = playList.playList.playListId,
                 listSound = playList.soundPlayList.map { soundEntity -> soundEntity.toSound() }.toMutableSet(),
                 currentMusicPosition = playList.playList.currentSoundPosition,
                 name  =  playList.playList.title
             )
         }catch (execption :Exception){
             throw execption;
         }
    }

    suspend fun updateplaylistAndSoundCrossDaoList(playList: PlayList,newList: MutableSet<Sound>): List<Long> {
         try {
             val result = playListDAO.updatePlayList(playList.toEntity())
             if ( result != 0){
                  val listUpdate =compareListToUpdate(newList, playList)
                 return if (listUpdate.isNotEmpty()){
                     val  playlistAndSoundCrossDaoList = listUpdate.map {soundFromPlayList->
                         PlayListAndSoundCrossEntity(
                             playList.idPlayList!!,
                             soundFromPlayList.idSound!!
                         )
                     }
                     playlistAndSoundCross.insertPlayListAndSoundCroos(playlistAndSoundCrossDaoList)
                 }else{
                     deleteItemPlayListSoundAcross(playList, newList)
                 }
             }
             return emptyList()

         }catch (exeption:Exception){
             exeption.printStackTrace()
             throw Exception("Erro ao Atualizar play list : ${exeption.message}")
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
        }catch (exeption:Exception){
            throw exeption;
        }
    }
    private suspend fun deleteItemPlayListSoundAcross(
        playList: PlayList,
        newList: MutableSet<Sound>
    ) :List<Long> {
        val modifaildField = mutableListOf<Long>()
        if (playList.listSound.isNotEmpty() && newList.isNotEmpty()){
            playList.listSound.forEach { sound ->
                if (!newList.contains(sound)) {
                  val modi = playlistAndSoundCross.deleteItemPlayListAndSoundCroos(
                        idPlaylist = playList.idPlayList!!,
                        idSound = sound.idSound!!
                    )
                    modifaildField.add(modi.toLong())
                }
            }
        }
        return modifaildField
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

    suspend fun compareListToUpdate(newList:MutableSet<Sound>, playList: PlayList):MutableSet<Sound> {
          val listToUpdate = mutableSetOf<Sound>()
           if (playList.listSound == newList) return  listToUpdate
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