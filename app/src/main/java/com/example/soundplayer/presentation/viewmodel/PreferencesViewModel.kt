package com.example.soundplayer.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soundplayer.commons.constants.Constants
import com.example.soundplayer.data.repository.DataStorePreferenceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


@HiltViewModel
class PreferencesViewModel @Inject constructor(
    private val prefrencesRepository: DataStorePreferenceRepository
) :ViewModel() {

     private val _isDarkMode =  MutableLiveData<StatePrefre>()
     var isDarkMode : LiveData<StatePrefre> = _isDarkMode

    private val  _sizeTextMusic =MutableLiveData<StatePrefre>()
    var sizeTextTitleMusic : LiveData<StatePrefre> = _sizeTextMusic

    fun saveDarkModePrefrence(isDarkMode :Boolean){
       viewModelScope.launch {
           prefrencesRepository.savePreferenceModeUi(isDarkMode)
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
            prefrencesRepository.saveSizeTitleMusic(size)
        }
    }

    fun readSizeTextMusicPreference(){
        viewModelScope.launch {
            runCatching {
                prefrencesRepository.readSizeTitleMusis(Constants.ID_SIZE_TEXT_TITLE_MUSIC)
            }.fold(
                onSuccess = {result ->
                    withContext(Dispatchers.Main){
                        _sizeTextMusic.value = StatePrefre.Sucess(result ?: 16f)
                    }
                },
                onFailure = {
                    Log.i("INFO_", "readDarkModePreference: ${it.message}")
                    StatePrefre.Error("Erro ao ler as preferências de titulo da música ")
                }
            )
        }
    }

}

sealed interface StatePrefre{
    class Sucess <T> (val succssResult : T) : StatePrefre
    class Error(val mensagem : String) :StatePrefre
}