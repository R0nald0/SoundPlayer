package com.example.soundplayer.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soundplayer.commons.constants.Constants
import com.example.soundplayer.data.entities.UserDataPreferecence
import com.example.soundplayer.data.repository.DataStorePreferenceRepository
import com.example.soundplayer.service.ServicePlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


@HiltViewModel
class PreferencesViewModel @Inject constructor(
    private val prefrencesRepository: DataStorePreferenceRepository,
    private val servicePlayer: ServicePlayer
) :ViewModel() {

     private val _isDarkMode =  MutableLiveData<StatePrefre>()
     var isDarkMode : LiveData<StatePrefre> = _isDarkMode

    private val  _sizeTextMusic =MutableLiveData<StatePrefre>()
    var sizeTextTitleMusic : LiveData<StatePrefre> = _sizeTextMusic

    private val  _uiStatePreffs =MutableLiveData<UserDataPreferecence>()
    var uiStatePreffs : LiveData<UserDataPreferecence> = _uiStatePreffs

  init {

  }
    fun readAllPrefference(){
        viewModelScope.launch {
            runCatching {
                prefrencesRepository.readAllPreferecenceData()
            }.fold(
                onSuccess = {userPreff->
                     if (userPreff != null){
                         _uiStatePreffs.value = userPreff!!

                     } else{
                         Log.i("INFO_", "readAllPrefference: user pref Null")
                     }
                },
                onFailure = {}
            )
        }
    }
    fun saveDarkModePrefrence(isDarkMode :Boolean){
       viewModelScope.launch {
           prefrencesRepository.savePrefferenceUser(
               value = isDarkMode,
               key = Constants.ID_DARKMODE_KEY
           )
       }
    }

     fun readDarkModePreference(){
         viewModelScope.launch {
            runCatching {
                prefrencesRepository.readBooleansPreference()
            }.fold(
                onSuccess = {result ->
                   withContext(Dispatchers.Main){
                       _isDarkMode.value = StatePrefre.Sucess(result ?: false)
                   }
                },
                onFailure = {
                    Log.i("INFO_", "preferências music sound: ${it.message}")
                    StatePrefre.Error("Erro ao ler as preferências de Mode ui")
                }
            )
         }
    }

    fun saveSizeTextMusicPrefrence(size :Float){
        viewModelScope.launch {
            prefrencesRepository.savePrefferenceUser(
                value =  size,
                key = Constants.ID_SIZE_TEXT_TITLE_MUSIC
            )
        }
    }

    fun readSizeTextMusicPreference(){
        viewModelScope.launch {
            prefrencesRepository
                .readUserPrefference(Constants.ID_SIZE_TEXT_TITLE_MUSIC)
                .catch {
                    Log.i("INFO_", "readDarkModePreference: ${it.message}")
                    StatePrefre.Error("Erro ao ler as preferências de titulo da música ")
                }
                .collect{result->
                    _sizeTextMusic.value = StatePrefre.Sucess(result ?: 16f)
                }
        }
    }

    fun  saveOrderedSoundPrefference(value : Int){
        viewModelScope.launch {
            runCatching {
                prefrencesRepository.savePrefferenceUser(
                    value = value,
                    key = Constants.ID_ORDERED_SONS_PREFFERENCE
                )
            }.fold(
                onSuccess = {
                    prefrencesRepository
                        .readUserPrefference(Constants.ID_ORDERED_SONS_PREFFERENCE)
                        .collect{

                        }
                },
                onFailure = {}
            )
        }
    }

}

sealed interface StatePrefre{
    class Sucess <T> (val succssResult : T) : StatePrefre
    class Error(val mensagem : String) :StatePrefre
}