package com.example.soundplayer.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
                       _isDarkMode.value = result?.let { it1 -> StatePrefre.Sucesse(it1) }
                   }
                },
                onFailure = {
                    Log.i("INFO_", "readDarkModePreference: ${it.message}")
                  StatePrefre.Error("Erro ao ler as  preferenceias  ")
                }
            )
         }
    }

}

sealed interface StatePrefre{
    class Sucesse(val isDarkMode : Boolean) : StatePrefre
    class Error(val mensagem : String) :StatePrefre
}