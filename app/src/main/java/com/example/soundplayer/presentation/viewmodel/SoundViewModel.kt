package com.example.soundplayer.presentation.viewmodel


import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.AudioAttributes
import androidx.media3.common.Player
import com.example.soundplayer.commons.execptions.Failure
import com.example.soundplayer.data.entities.UserDataPreferecence
import com.example.soundplayer.data.repository.DataStorePreferenceRepository
import com.example.soundplayer.model.PlayList
import com.example.soundplayer.model.Sound
import com.example.soundplayer.service.ServicePlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SoundViewModel @Inject constructor(
    private  val dataStorePreferenceRepository: DataStorePreferenceRepository,
    private val servicePlayer: ServicePlayer
) :ViewModel(){

    var actualSound  : LiveData<Sound> ? = null
    lateinit var playBackError  : LiveData<Failure?>
    var isPlayingObserver  = MutableLiveData<Boolean>()

    private var _currentPlayingPlayList = MutableLiveData<PlayList>()
    val currentPlayList :LiveData<PlayList>
        get() = _currentPlayingPlayList

    private val _userDataPreferecenceObs = MutableLiveData<UserDataPreferecence>()
    val userDataPreferecence : LiveData<UserDataPreferecence>
        get() = _userDataPreferecenceObs

    lateinit var myPlayer :Player

    init {
         getPlayer()
         isPlaying()
         getActualSound()
         getActualPlayList()
         getPlayback()
    }

    fun getPlayback() {
        viewModelScope.launch {
            playBackError = servicePlayer.getPlayBackError()
        }
    }
    fun getCurrentPositionSound():Int{
      return  _currentPlayingPlayList.value?.currentMusicPosition ?: 0
    }
    fun getActualSound()
        { viewModelScope.launch { actualSound = servicePlayer.getActualSound() } }

    private fun isPlaying()  {
        viewModelScope.launch { isPlayingObserver = servicePlayer.isiPlaying() }
    }

    fun getActualPlayList() {
        viewModelScope.launch {
            _currentPlayingPlayList.value =  servicePlayer.getActualPlayList()
        }
    }
    fun getAllMusics(playList: PlayList) = viewModelScope.launch {
          val currentPlayList =  servicePlayer.playPlaylist(playList)
          if (currentPlayList != null){
              _currentPlayingPlayList.value = currentPlayList!!
              getActualSound()
              isPlaying()
          }
       }
    suspend fun savePreference(){
        runCatching {
            if(_currentPlayingPlayList.value != null){
                dataStorePreferenceRepository
                    .savePreference(
                         playlistKeyId =  _currentPlayingPlayList.value!!.idPlayList,
                        positionSoundKey = _currentPlayingPlayList.value!!.currentMusicPosition
                    )
            }
        }.fold(
            onSuccess = {
                withContext(Dispatchers.Main){
                    readPreferences()
                }
            },
            onFailure = {
                Log.i("INFO_", "savePreference: erro ao salvar preferencias ${it.message}")

            }
        )

    }
    suspend  fun readPreferences(){
        runCatching {
            dataStorePreferenceRepository.readAllPreferecenceData()
        }.fold(
            onSuccess = {readAllPreferecenceData->
                withContext(Dispatchers.Main){
                    _userDataPreferecenceObs.value = readAllPreferecenceData
                        ?: UserDataPreferecence(idPreference = 1, postionPreference = 1
                        )
                }
            },
            onFailure = {
                Log.i("Play_", "readPreferences: erro ao ler dados da store : ${it.message}")
            }
        )

    }

     fun getPlayer() {
        myPlayer = servicePlayer.getPlayer()
    }

    fun updateAudioFocos(){
        myPlayer.setAudioAttributes(
            AudioAttributes.DEFAULT,true
        )
    }

}