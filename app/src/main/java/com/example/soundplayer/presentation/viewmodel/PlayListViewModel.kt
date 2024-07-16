
package com.example.soundplayer.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soundplayer.data.repository.SoundRepository
import com.example.soundplayer.model.DataSoundPlayListToUpdate
import com.example.soundplayer.model.PlayList
import com.example.soundplayer.model.PlaylistWithSoundDomain
import com.example.soundplayer.model.Sound
import com.example.soundplayer.service.ServicePlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class PlayListViewModel @Inject constructor(
    private val soundRepository: SoundRepository,
    private val servicePlayer: ServicePlayer
):ViewModel() {
    private val TAG = "INFO_"
    private val _clickedPlayList = MutableLiveData<PlayList>()
    val clickedPlayList : LiveData<PlayList> = _clickedPlayList

    private val _playListsWithSounds = MutableLiveData<List<PlaylistWithSoundDomain>>()
    val playListsWithSounds : LiveData<List<PlaylistWithSoundDomain>>
        get() = _playListsWithSounds

    private val _soundListBd = MutableLiveData<List<Sound>>()
    val soundListBd : LiveData<List<Sound>>
        get() = _soundListBd

    private val  _listSize = MutableLiveData<Int>()
    var listSize:LiveData<Int>  = _listSize

    init {
      countTotalSound()
    }

    fun savePlayList(playList: PlayList){
        viewModelScope.launch {
            val retor  = servicePlayer.createPrayList(playList)
            if (retor.isNotEmpty()){
                getAllPlayList()
            }
        }

    }
    fun getAllPlayList(){
        viewModelScope.launch {
            runCatching {
                servicePlayer.findAllPlayList()
            }.fold(
                onSuccess = {playlistWithSoundDomainsRetorno->
                    _playListsWithSounds.value = playlistWithSoundDomainsRetorno
                },
                onFailure = {
                    Log.i("INFO_", "getAllPlayList: erro ao buscar todas ás playlists ${it.message}")
                }
            )
        }
    }

  fun findAllSound(){
      viewModelScope.launch {
          runCatching {
              soundRepository.findAllSound()
          }.fold(
              onSuccess = {soundList->
                  _soundListBd.value = soundList
              },
              onFailure = {erro->
                  Log.i(TAG, "findAllSound: Erro ao buscar sound ${erro.message}")
              }
          )
      }
  }
    fun countTotalSound(){
        viewModelScope.launch {
            _listSize.value = soundRepository.findAllSound().size
        }
    }
    fun saveAllSoundsByContentProvider(sizeListAllMusic: MutableSet<Sound>){
        viewModelScope.launch {
            runCatching {
                servicePlayer.saveSoundProvideFromDb(sizeListAllMusic)
            }.fold(
                onSuccess = { listSound->
                       if (listSound?.size != 0){
                           val sounds =soundRepository.findAllSound()
                           _soundListBd.value =  sounds
                           _listSize.value = sounds.size
                       }

                    Log.i(TAG, "saveAllSoundsByContentProvider: AllMusics salva")
                },
                onFailure = {
                    Log.i(TAG, "saveAllSoundsByContentProvider: erro ao salvar allmucis")
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
                findPlayListById(idPlayList = idPlaylist)
                getAllPlayList()
            }
        }
    }

    private suspend fun verifyAndUpdateSizeListSound(idPlaylist: Long) {
        if (idPlaylist == 1L) {
            _listSize.value = soundRepository.findAllSound().size
            Log.i(TAG, "verifyAndUpdateSizeListSound: ${listSize.value}")
        }
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
                findPlayListById(idPlayList = dataSoundToUpdate.idPlayList)
                getAllPlayList()
            }

        }
    }

    fun findPlayListById(idPlayList:Long) {
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
                }
            )
        }
    }


}
