package com.example.soundplayer.service

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

    fun playPlaylist(playerList :PlayList):PlayList?{
       return   playerRepository.getAllMusics(playerList)
    }
    suspend fun findAllPlayList() = soundPlayListRepository.findAllPlayListWithSong()

  private  fun ondenate(soundList:MutableSet<Sound>, ondanateType:Int):MutableSet<Sound>{
      return  when(ondanateType){
            0-> soundList.sortedBy { it.title }.toMutableSet()
            1->  soundList.sortedByDescending { it.title }.toMutableSet()
            2-> soundList.sortedBy { it.insertedDate }.toMutableSet()
          else -> {soundList}
      }
    }
    suspend fun findPlayListById(id: Long):PlayList?{
          val orderBy = dataStorePreferenceRepository
                 .readUserPrefference(key = Constants.ID_ORDERED_SONS_PREFFERENCE)
                 .first() ?: 0

         playerRepository.getAcutalPlayList()?.let {playlistCurrentlyPlaying ->
             if (id == playlistCurrentlyPlaying.idPlayList){
                 return playlistCurrentlyPlaying
             }
         }

        val playListById = soundPlayListRepository.findPlayListById(id)
        val listOrdered = ondenate(playListById!!.listSound,orderBy)
        return playListById.copy(listSound =listOrdered)
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
    fun getActualPlayList()= playerRepository.getAcutalPlayList()
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
            if (idPlayList == 1L){
                val soundById = soundRepository.findSoundById(idSound).toSound()
                soundRepository.delete(soundById)
            }
            playerRepository.getAcutalPlayList()?.let {playlistCurrentlyPlaying ->
                if (playlistCurrentlyPlaying.idPlayList?.equals(idPlayList) == true){
                    playerRepository.removeItemFromListMusic( indexSound)
                }
           }
        }
      return  afectedLines
    }

    suspend fun saveSoundProvideFromDb(soundsList :Set<Sound>):List<Long>?{
          if (soundsList.isEmpty()) return null
          val listSoundBd  = soundRepository.findAllSoundTiitle()

         val afactedLines = mutableListOf<Long>();
         if (listSoundBd.size != soundsList.size){
             soundsList.forEach {soundByProvider->
                 if (!listSoundBd.contains(soundByProvider.title)){
                     val result = soundRepository.saveSound(soundByProvider)
                     afactedLines.add(result)
                 }
             }
         }

        return afactedLines
    }
}