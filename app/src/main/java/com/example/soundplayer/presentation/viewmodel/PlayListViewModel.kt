package com.example.soundplayer.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soundplayer.data.repository.SoundPlayListRepository
import com.example.soundplayer.model.DataSoundPlayListToUpdate
import com.example.soundplayer.model.PlayList
import com.example.soundplayer.model.Sound
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


@HiltViewModel
class PlayListViewModel @Inject constructor(
    private val soundPlayListRepository: SoundPlayListRepository,
):ViewModel() {
    private val _uniquePlayList = MutableLiveData<PlayList>()
    val uniquePlayList : LiveData<PlayList>
        get() = _uniquePlayList

    private val _playLists = MutableLiveData<List<PlayList>>()
     val playLists : LiveData<List<PlayList>>
         get() = _playLists

    private val _soundListBd = MutableLiveData<List<Sound>>()
    val soundListBd : LiveData<List<Sound>>
        get() = _soundListBd

    private val _listSoundUpdate  = MutableLiveData<Pair<Boolean,DataSoundPlayListToUpdate>>()
    var listSoundUpdate : LiveData<Pair<Boolean,DataSoundPlayListToUpdate>> = _listSoundUpdate


    fun savePlayList(playList: PlayList){
        viewModelScope.launch {
          val retor  = soundPlayListRepository.savePlayList(playList)
            if (retor.isNotEmpty()){
                getAllPlayList()
            }
        }

    }
    fun getAllPlayList(){
       viewModelScope.launch {
           runCatching {
               soundPlayListRepository.findAllPlayListWithSong()
           }.fold(
               onSuccess = {playListsRetorno->
                   withContext(Dispatchers.Main){
                       _playLists.value = playListsRetorno
                   }
               },
               onFailure = {
                   Log.i("INFO_", "getAllPlayList: erro ao buscar todas ás playlists ${it.message}")
               }
           )
       }
    }

    fun saveSound(sound: Sound ){
        viewModelScope.launch {
           val id  =soundPlayListRepository.saveSound(sound)
            Log.i("INFO_", "saveSound id :id music $id ")
        }
    }
    fun saveAllSoundsByContentProvider(sizeListAllMusic: MutableSet<Sound>){
        viewModelScope.launch {
           var listBd = soundPlayListRepository.sizeListSound()
            if(sizeListAllMusic.size != listBd.size){
                sizeListAllMusic.forEach {sound->
                    if (!listBd.contains(sound)){
                        soundPlayListRepository.saveSound(sound)
                    }
                }
                listBd = soundPlayListRepository.sizeListSound()
            }
            withContext(Dispatchers.Main){
                _soundListBd.value = listBd
            }
        }
    }
    fun deletePlayList(playList: PlayList) {
        viewModelScope.launch {
          val reterno   = soundPlayListRepository.deletePlaylist(playList)
          if (reterno != 0){
              getAllPlayList()
          }
        }
    }
    fun updateNamePlayList(playList: PlayList){
         viewModelScope.launch {
            val numberModifiedField  =soundPlayListRepository.updateNamePlayList(playList)
             if (numberModifiedField !=0 ){
                 getAllPlayList()
             }
         }
    }

    fun updatSoundAtPlaylist(dataSoundToUpdate: DataSoundPlayListToUpdate){
         viewModelScope.launch{
            val result = soundPlayListRepository.addSountToPlayList(dataSoundToUpdate.idPlayList,dataSoundToUpdate.sounds)
             if (result.isNotEmpty()) {
                  _listSoundUpdate.value = Pair(true,dataSoundToUpdate)
                 findPlayListById(idPlayList = dataSoundToUpdate.idPlayList)
                 getAllPlayList()
                 _listSoundUpdate.value = Pair(false , dataSoundToUpdate.copy(idPlayList = -1))
             }
         }
    }

    fun removePlaySoundFromPlayList(dataSoundToUpdate : DataSoundPlayListToUpdate){

        viewModelScope.launch {
             val itemsRemoved  =soundPlayListRepository.removeSoundItemsFromPlayList(idPlayList = dataSoundToUpdate.idPlayList,
                 setOf(dataSoundToUpdate.sounds.first())
             )

             if (itemsRemoved.isNotEmpty()){
                 _listSoundUpdate.value = Pair(false , dataSoundToUpdate)
                 findPlayListById(idPlayList = dataSoundToUpdate.idPlayList)
                 getAllPlayList()
                 _listSoundUpdate.value = Pair(false , dataSoundToUpdate.copy(idPlayList = -1))
                 //TODO refatorar
             }

         }
    }

    fun findPlayListById(idPlayList:Long) {
        viewModelScope.launch {
            val resultPlayList = soundPlayListRepository.findPlayListById(idPlayList)
             if (resultPlayList != null){
                 _uniquePlayList.value = resultPlayList!!
             }
        }
    }
}