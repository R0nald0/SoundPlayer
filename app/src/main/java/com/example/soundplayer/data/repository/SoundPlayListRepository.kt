package com.example.soundplayer.data.repository

import android.util.Log
import com.example.soundplayer.data.dao.PlayListDAO
import com.example.soundplayer.data.dao.PlaylistAndSoundCrossDao
import com.example.soundplayer.data.dao.SoundDao
import com.example.soundplayer.data.entities.PlayListAndSoundCrossEntity
import com.example.soundplayer.data.entities.toSound
import com.example.soundplayer.model.PlayList
import com.example.soundplayer.model.Sound
import com.example.soundplayer.model.toEntity
import com.example.soundplayer.model.toSoundEntity
import java.lang.Exception
import javax.inject.Inject


class SoundPlayListRepository @Inject constructor (
    private val playListDAO: PlayListDAO,
    private val playlistAndSoundCross :PlaylistAndSoundCrossDao,
    private val soundDao: SoundDao,
){

 suspend fun savePlayList(playList: PlayList):List<Long>{
       try {
            val idPlayList =playlistAndSoundCross.createPlayList(playList.toEntity())
        val  playlistAndSoundCrossDaoList = playList.listSound.map {soundFromPlayList->
               PlayListAndSoundCrossEntity(
                   idPlayList,
                   soundFromPlayList.idSound!!
               )
           }

           return  playlistAndSoundCross.savePlayListAndSoundCroos(playlistAndSoundCrossDaoList)
       } catch (exception: Exception) {
           exception.printStackTrace()
           throw Exception("Erro ao salvar play list")
       }
    }

  suspend fun findAllPlayList():List<PlayList>{
        try {
          val listPlayListEntity  =  playlistAndSoundCross.findAllPlayList()
            val listPlayList = listPlayListEntity.map {playListWithSound->
                  PlayList(playListWithSound)
            }
            return  listPlayList
        }catch (exeption :Exception){
            throw exeption
        }
    }

   suspend fun saveSound(sound: Sound):Long{
           return  playlistAndSoundCross.createSound(sound = sound.toSoundEntity())
    }

    suspend fun sizeListSound():List<Sound>{

        return soundDao.findAllSound().map {soundEntity ->
            Sound(soundEntity)
        }
    }
}