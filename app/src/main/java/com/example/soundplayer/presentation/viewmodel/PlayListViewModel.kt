
package com.example.soundplayer.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soundplayer.model.DataSoundPlayListToUpdate
import com.example.soundplayer.model.PlayList
import com.example.soundplayer.model.PlaylistWithSoundDomain
import com.example.soundplayer.model.Sound
import com.example.soundplayer.service.ServicePlayer
import com.example.soundplayer.service.SoundDomainService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class PlayListViewModel @Inject constructor(
    private val soundDomainService: SoundDomainService,
    private val servicePlayer: ServicePlayer
):ViewModel() {

    private val TAG = "INFO_"
    private val _clickedPlayList = MutableLiveData<PlayList>()
    val clickedPlayList : LiveData<PlayList> = _clickedPlayList

    private val _playListsWithSounds = MutableLiveData<List<PlaylistWithSoundDomain>>()
    val playListsWithSounds : LiveData<List<PlaylistWithSoundDomain>>
        get() = _playListsWithSounds

    private val _soundListBd = MutableLiveData<Set<Sound>>()
    val soundListBd : LiveData<Set<Sound>>
        get() = _soundListBd

    private val  _listSize = MutableLiveData<Int>()
    var listSize:LiveData<Int>  = _listSize


    private val _soundListCompared = MutableLiveData<Set<Sound>>()
    val soundListCompared : LiveData<Set<Sound>> = _soundListCompared

    private val _erroMessage = MutableLiveData<String?>()
    var erroMassage :LiveData<String?> = _erroMessage


    fun savePlayList(playList: PlayList){
        viewModelScope.launch {
            val result  = servicePlayer.createPlayList(playList)
            if (result.isNotEmpty()){
                getAllPlayList()
            }
        }
    }

    fun getAllPlayList(){
        _erroMessage.value = null
        viewModelScope.launch {
            runCatching {
                servicePlayer.findAllPlayList()
            }.fold(
                onSuccess = {playlistWithSoundDomainsRetorno->
                    _playListsWithSounds.value = playlistWithSoundDomainsRetorno
                },
                onFailure = {
                    Log.e("INFO_", "getAllPlayList: erro ao buscar todas ás playlists ${it.message}")
                    _erroMessage.value = "Não conseguimos buscar as playlists"
                }
            )
        }
    }

    fun findAllSound(){
        _erroMessage.value = null
      viewModelScope.launch {
          runCatching {
              soundDomainService.findAllSound()
          }.fold(
              onSuccess = {soundList->
                  _soundListBd.value = soundList.toSet()
              },
              onFailure = {erro->
                  Log.i(TAG, "findAllSound: Erro ao buscar sound ${erro.message}")
                  _erroMessage.value = "Não conseguimos buscar os audion,tente novamente "
              }
          )
      }
  }
     fun countTotalSound(){
        viewModelScope.launch {
            _listSize.value = soundDomainService.findAllSound().size
        }
    }
    fun saveAllSoundsByContentProvider(sizeListAllMusic: Set<Sound>){
        if (sizeListAllMusic.isEmpty()){
            _soundListBd.value = emptySet()
             return
        }

        viewModelScope.launch {
            _erroMessage.value = null
            runCatching {
                servicePlayer.saveSoundProvideFromDb(sizeListAllMusic)
            }.fold(
                onSuccess = { listSound->
                       if (!listSound.isNullOrEmpty()){
                           val sounds = soundDomainService.findAllSound()
                           _soundListBd.value =  sounds.toSet()
                           _listSize.value = sounds.size
                       }
                },
                onFailure = {
                    Log.i(TAG, "saveAllSoundsByContentProvider: erro ao salvar allmusics")
                    _erroMessage.value = "Algo deu errado ao salvar a plylist,tente novamente"
                }
            )
        }
    }

    fun deletePlayList(playList: PlayList) {
        viewModelScope.launch {
            val reterno   = servicePlayer.deletePlayList(playList)
            if (reterno != 0){
                getAllPlayList()
            }
        }
    }
    fun updateNamePlayList(playList: PlayList){
        viewModelScope.launch {
            val numberModifiedField  = servicePlayer.updateNamePlayList(playList.idPlayList!!,playList.name)
            if (numberModifiedField !=0 ){
                getAllPlayList()
            }
        }
    }

    fun updateSoundAtPlaylist(dataSoundToUpdate: DataSoundPlayListToUpdate){
        viewModelScope.launch{
            val (idPlaylist,_,soundToUpdate) = dataSoundToUpdate
            val result =  servicePlayer.addItemFromListMusic(
                idPlayList = idPlaylist,
                soundsToInsertPlayList = soundToUpdate
            )

            if (result.isNotEmpty()) {
                verifyAndUpdateSizeListSound(idPlaylist)
            }
        }
    }

    private suspend fun verifyAndUpdateSizeListSound(idPlaylist: Long) {
        if (idPlaylist == 1L) {
            _listSize.value = soundDomainService.findAllSound().size
        }
        findPlayListById(idPlayList = idPlaylist)
        getAllPlayList()
    }

    fun removePlaySoundFromPlayList(dataSoundToUpdate : DataSoundPlayListToUpdate){

        viewModelScope.launch {
            val itemsRemoved  =servicePlayer.removeItemFromListMusic(
                idPlayList = dataSoundToUpdate.idPlayList,
                idSound = dataSoundToUpdate.sounds.first().idSound!!,
                indexSound = dataSoundToUpdate.positionSound.first()
            )

            if (itemsRemoved != 0){
                verifyAndUpdateSizeListSound(dataSoundToUpdate.idPlayList)
            }

        }
    }

    fun findPlayListById(idPlayList:Long) {
        _erroMessage.value = null
        if (_listSize.value == null) return
        if (_listSize.value!! <= 0) return

        viewModelScope.launch {
            runCatching {
                servicePlayer.findPlayListById(idPlayList)
            }.fold(
                onSuccess = { resultPlayList ->
                    if (resultPlayList != null){
                        _clickedPlayList.value = resultPlayList!!
                    }
                }, onFailure = {
                    Log.e(TAG, "erro ao buscar  playlist com o id: $idPlayList : ${it.message} ")
                    _erroMessage.value = "Não conseguimos encontrar á playlist"
                }
            )
        }
    }



    fun updateSoundList(sounds : Set<Sound>){
        viewModelScope.launch {
            runCatching {
                soundDomainService.saveSound(sounds)
            }.fold(
                onSuccess = {item->
                     if (item.isNotEmpty()){
                         val longList = servicePlayer.addItemFromListMusic(1, sounds)
                         if (longList.isNotEmpty()) {
                             verifyAndUpdateSizeListSound(1)
                         }
                     }
                },
                onFailure = {}
            )
        }
    }

    fun  comparePlayLists(soundOfSystem: Set<Sound>,idPlayListToCompare: Long){
         viewModelScope.launch {
             runCatching {
                 servicePlayer.comparePlayListsAndReturnDiference(soundOfSystem,idPlayListToCompare)
             }.fold(
                 onSuccess = {sounds ->
                     _soundListCompared.postValue(sounds)
                 },
                 onFailure = {error->
                     Log.e(TAG, "erro ao verificar as playlists: $ : ${error.message} ")
                     _erroMessage.value = "Não conseguimos verificar as playlists"
                 }
             )
         }
    }
}
