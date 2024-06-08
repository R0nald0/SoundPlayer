
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

    private val _listSoundUpdate  = MutableLiveData<Pair<Boolean,DataSoundPlayListToUpdate>>()
    var listSoundUpdate : LiveData<Pair<Boolean,DataSoundPlayListToUpdate>> = _listSoundUpdate

    private val _listSoundUpdateTest  = MutableLiveData<PlayList>()
    var listSoundUpdateTeste : LiveData<PlayList> = _listSoundUpdateTest

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
    
    fun saveAllSoundsByContentProvider(sizeListAllMusic: MutableSet<Sound>){
        viewModelScope.launch {

            var listBd = soundRepository.sizeListSound()
            if(sizeListAllMusic.size != listBd.size){
                sizeListAllMusic.forEach {sound->
                    if (!listBd.contains(sound)){
                        soundRepository.saveSound(sound)
                    }
                }
                listBd = soundRepository.sizeListSound()
            }
            withContext(Dispatchers.Main){
                _soundListBd.value = listBd
            }
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

    fun updatSoundAtPlaylist(dataSoundToUpdate: DataSoundPlayListToUpdate){
        viewModelScope.launch{
            val (idPlaylist,_,soundToUpdate) = dataSoundToUpdate
            val result =  servicePlayer.addItemFromListMusic(
                idPlayList = idPlaylist,
                soundsToInsertPlayList = soundToUpdate

            )

            if (result.isNotEmpty()) {
                _listSoundUpdate.value = Pair(true,dataSoundToUpdate)
                findPlayListById(idPlayList = idPlaylist)
                getAllPlayList()
                _listSoundUpdate.value = Pair(false , dataSoundToUpdate.copy(idPlayList = -1))
            }
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
            runCatching {
                servicePlayer.findPlayListById(idPlayList)
            }.fold(
                onSuccess = { resultPlayList ->
                    if (resultPlayList != null){
                        _clickedPlayList.value = resultPlayList!!
                    }
                }, onFailure = {
                    Log.i(TAG, "erro ao buscar  playlist com o id: $idPlayList : ${it.message} ")
                }
            )
        }
    }


}
