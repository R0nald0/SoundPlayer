package com.example.soundplayer.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soundplayer.data.repository.SoundPlayListRepository
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
          val playListsRetorno =soundPlayListRepository.findAllPlayListWithSong()
              withContext(Dispatchers.Main){
                  _playLists.value = playListsRetorno
              }
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

    fun addSountToPlayList(idPlayList: Long,listSound:Set<Sound>){
         viewModelScope.launch{
            val result = soundPlayListRepository.addSountToPlayList(idPlayList,listSound)
             if (result.isNotEmpty()) {
                 getAllPlayList()
                 findPlayListById(idPlayList)
             }
         }
    }
    fun removePlaySoundFromPlayList(playListId: Long,listRemovedItems: Set<Sound>){
         viewModelScope.launch {
             val itemsRemoved  =soundPlayListRepository.removeSoundItemsFromPlayList(idPlayList = playListId,listRemovedItems)
             if (itemsRemoved.isNotEmpty()){
                 findPlayListById(idPlayList = playListId)
                 getAllPlayList()
             }

         }
    }
    fun findPlayListById(idPlayList:Long) {
        viewModelScope.launch {
             val resultPlayList = soundPlayListRepository.findPlayListById(idPlayList)
              withContext(Dispatchers.Main){
                 _uniquePlayList.postValue(resultPlayList)
             }
        }
    }
}