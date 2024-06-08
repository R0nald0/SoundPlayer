package com.example.soundplayer.service

import com.example.soundplayer.data.repository.PlayerRepository
import com.example.soundplayer.data.repository.SoundPlayListRepository
import com.example.soundplayer.data.repository.SoundRepository
import com.example.soundplayer.model.PlayList
import com.example.soundplayer.model.Sound
import javax.inject.Inject

class ServicePlayer @Inject constructor(
    private val playerRepository: PlayerRepository,
    private val soundPlayListRepository: SoundPlayListRepository,
    private val soundRepository: SoundRepository
) {

    fun playPlaylist(playerList :PlayList,):PlayList?{
       return   playerRepository.getAllMusics(playerList)
    }
    suspend fun findAllPlayList() = soundPlayListRepository.findAllPlayListWithSong()
    suspend fun findPlayListById(id: Long):PlayList?{

         playerRepository.getAcutalPlayList()?.let {playlistCurrentlyPlaying ->
             if (id == playlistCurrentlyPlaying.idPlayList){
                 return playlistCurrentlyPlaying
             }
         }
       return soundPlayListRepository.findPlayListById(id)
    }
    suspend fun createPrayList(playList: PlayList):List<Long>{
       return soundPlayListRepository.savePlayList(playList = playList)
    }

    suspend fun updateNamePlayList(id: Long,newName: String):Int{
     return   soundPlayListRepository.updateNamePlayList(id,newName)
    }
    suspend fun deletePlayList(playList : PlayList):Int{
        return soundPlayListRepository.deletePlaylist(playList)
    }
  suspend  fun getActualSound() =playerRepository.getActaulSound()
    fun isiPlaying() =playerRepository.isPlaying()

    fun destroyPlayer() =playerRepository.destroyPlayer()
    fun getPlayer() =playerRepository.getPlayer()

    suspend fun  addItemFromListMusic(idPlayList:Long, soundsToInsertPlayList: Set<Sound>):List<Long>{

        val afectedLines  = soundPlayListRepository.addSountToPlayList(idPlayList,soundsToInsertPlayList)
        if (afectedLines.isNotEmpty()){
            playerRepository.getAcutalPlayList()?.let {playlistCurrentlyPlaying ->
                if (idPlayList == playlistCurrentlyPlaying.idPlayList){
                    playerRepository.addItemFromListMusic(soundsToInsertPlayList)
                }
            }
        }
        return  afectedLines
    }
    suspend fun removeItemFromListMusic(idPlayList : Long, idSound: Long, indexSound : Int ):Int{

        val afectedLines = soundPlayListRepository.removeSoundItemFromPlayList(idPlayList,idSound)
        if (afectedLines != 0){
            playerRepository.getAcutalPlayList()?.let {playlistCurrentlyPlaying ->
                if (playlistCurrentlyPlaying.idPlayList?.equals(idPlayList) == true){
                    playerRepository.removeItemFromListMusic( indexSound)
                }
           }

        }

      return  afectedLines
    }

}