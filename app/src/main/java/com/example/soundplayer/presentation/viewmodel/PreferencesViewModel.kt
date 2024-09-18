package com.example.soundplayer.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soundplayer.commons.constants.Constants
import com.example.soundplayer.commons.constants.Constants.ID_PLAYLIST_KEY
import com.example.soundplayer.commons.constants.Constants.POSITION_KEY
import com.example.soundplayer.data.entities.UserDataPreferecence
import com.example.soundplayer.service.ServicePlayer
import com.example.soundplayer.service.UserPrefferencesService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class PreferencesViewModel @Inject constructor(
    private val  serviceDataPreference: UserPrefferencesService,
    private val servicePlayer: ServicePlayer
) :ViewModel() {

     private val _isDarkMode =  MutableLiveData<StatePrefre>()
     var isDarkMode : LiveData<StatePrefre> = _isDarkMode

    private val  _sizeTextMusic =MutableLiveData<StatePrefre>()
    var sizeTextTitleMusic : LiveData<StatePrefre> = _sizeTextMusic

    private val  _uiStatePreffs =MutableLiveData<UserDataPreferecence>()
    var uiStatePreffs : LiveData<UserDataPreferecence> = _uiStatePreffs


    fun readAllPrefference(){
        viewModelScope.launch {
            runCatching {
                serviceDataPreference.readAppAllPrefferences()
            }.fold(
                onSuccess = {userPreff->
                    Log.d("INFO_", "readAllPrefference: $userPreff")
                    _uiStatePreffs.value = userPreff
                },
                onFailure = {}
            )
        }
    }
    fun saveDarkModePrefrence(valueModeUi :Int){
       viewModelScope.launch {
           serviceDataPreference.savePrefferenceUser(
               value = valueModeUi,
               key = Constants.ID_DARK_MODE_KEY
           )
       }
    }
    fun readDarkModePreference(){
         viewModelScope.launch {
             serviceDataPreference.readUserPrefference(Constants.ID_DARK_MODE_KEY).
                 catch {error->
                     Log.i("INFO_", "preferências music sound: ${error.message}")
                     StatePrefre.Error("Erro ao ler as preferências de Mode ui")
                 }
                 .collect{result ->
                     _isDarkMode.value = StatePrefre.Sucess(result ?: 2)
                 }
         }
    }
    fun saveSizeTextMusicPrefrence(size :Float){
        viewModelScope.launch {
            serviceDataPreference.savePrefferenceUser(
                value =  size,
                key = Constants.ID_SIZE_TEXT_TITLE_MUSIC
            )
        }
    }
    fun readSizeTextMusicPreference(){
        viewModelScope.launch {
            serviceDataPreference
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
    fun saveOrderedSoundPrefference(value : Int){
        viewModelScope.launch {
            runCatching {
                serviceDataPreference.savePrefferenceUser(
                    value = value,
                    key = Constants.ID_ORDERED_SONS_PREFFERENCE
                )
            }.fold(
                onSuccess = {
                    serviceDataPreference
                        .readUserPrefference(Constants.ID_ORDERED_SONS_PREFFERENCE)
                        .collect{

                        }
                },
                onFailure = {}
            )
        }
    }
    fun savePlayListIdPlayList(idPlayList :Long){
        viewModelScope.launch {
           runCatching {
               serviceDataPreference.savePrefferenceUser(idPlayList ,ID_PLAYLIST_KEY)
           }.fold(
               onSuccess = {
                   readAllPrefference()
               } ,
               onFailure ={

               }
           )
        }
    }
    fun savePlayListIdCurrenPositionSound(currentMusicPosition : Int){
        viewModelScope.launch {
           runCatching {
               serviceDataPreference.savePrefferenceUser(currentMusicPosition,POSITION_KEY)
           }.fold(
               onSuccess = {} ,
               onFailure ={

               }
           )
        }
    }

}

sealed interface StatePrefre{
    class Sucess <T> (val succssResult : T) : StatePrefre
    class Error(val mensagem : String) :StatePrefre
}