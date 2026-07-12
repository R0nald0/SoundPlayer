package com.example.soundplayer.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soundplayer.commons.constants.Constants
import com.example.soundplayer.service.ServicePlayer
import com.example.soundplayer.service.UserPreferencesService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PreferencesViewModel
    @Inject
    constructor(
        private val serviceDataPreference: UserPreferencesService,
        private val servicePlayer: ServicePlayer,
    ) : ViewModel() {
        private val tag = "PreferencesViewModel"

        private val _uiState = MutableStateFlow(PreferencesUiState())
        val uiState: StateFlow<PreferencesUiState> = _uiState.asStateFlow()

        private val _uiEvent = MutableSharedFlow<PreferencesUiEvent>()
        val uiEvent: SharedFlow<PreferencesUiEvent> = _uiEvent.asSharedFlow()

        fun onIntent(intent: PreferencesIntent) {
            when (intent) {
                is PreferencesIntent.ReadAllPreferences -> readAllPreferences()
                is PreferencesIntent.ReadDarkMode -> readDarkMode()
                is PreferencesIntent.ReadTextSize -> readTextSize()
                is PreferencesIntent.SaveDarkMode -> saveDarkMode(intent.value)
                is PreferencesIntent.SaveTextSize -> saveTextSize(intent.size)
                is PreferencesIntent.SaveOrderedSound -> saveOrderedSound(intent.value)
                is PreferencesIntent.SavePlaylistId -> savePlaylistId(intent.id)
                is PreferencesIntent.SaveCurrentSoundPosition -> saveCurrentSoundPosition(intent.position)
            }
        }

        private fun readAllPreferences() {
            viewModelScope.launch {
                runCatching { serviceDataPreference.readAppAllPreferences() }
                    .onSuccess { prefs ->
                        _uiState.update { it.copy(preferences = prefs) }
                    }.onFailure { Log.e(tag, "readAllPreferences: ${it.message}") }
            }
        }

        private fun readDarkMode() {
            viewModelScope.launch {
                serviceDataPreference
                    .readUserPreference(Constants.ID_DARK_MODE_KEY)
                    .catch { error ->
                        Log.e(tag, "readDarkMode: ${error.message}")
                        _uiEvent.emit(PreferencesUiEvent.ShowError("Erro ao ler modo de visualização"))
                    }.collect { result ->
                        _uiState.update { it.copy(darkMode = result ?: 2) }
                    }
            }
        }

        private fun readTextSize() {
            viewModelScope.launch {
                serviceDataPreference
                    .readUserPreference(Constants.ID_SIZE_TEXT_TITLE_MUSIC)
                    .catch { error ->
                        Log.e(tag, "readTextSize: ${error.message}")
                        _uiEvent.emit(PreferencesUiEvent.ShowError("Erro ao ler tamanho do texto"))
                    }.collect { result ->
                        _uiState.update { it.copy(textSize = result ?: 16f) }
                    }
            }
        }

        private fun saveDarkMode(value: Int) {
            viewModelScope.launch {
                runCatching {
                    serviceDataPreference.saveUserPreference(value, Constants.ID_DARK_MODE_KEY)
                }.onSuccess { _uiState.update { it.copy(darkMode = value) } }
                    .onFailure { Log.e(tag, "saveDarkMode: ${it.message}") }
            }
        }

        private fun saveTextSize(size: Float) {
            viewModelScope.launch {
                runCatching {
                    serviceDataPreference.saveUserPreference(size, Constants.ID_SIZE_TEXT_TITLE_MUSIC)
                }.onSuccess { _uiState.update { it.copy(textSize = size) } }
                    .onFailure { Log.e(tag, "saveTextSize: ${it.message}") }
            }
        }

        private fun saveOrderedSound(value: Int) {
            viewModelScope.launch {
                runCatching {
                    serviceDataPreference.saveUserPreference(value, Constants.ID_ORDERED_SOUNDS_PREFERENCE)
                }.onSuccess {
                    _uiState.update { state ->
                        state.copy(preferences = state.preferences?.copy(orderedSound = value))
                    }
                }.onFailure { Log.e(tag, "saveOrderedSound: ${it.message}") }
            }
        }

        private fun savePlaylistId(id: Long) {
            viewModelScope.launch {
                runCatching {
                    serviceDataPreference.saveUserPreference(id, Constants.ID_PLAYLIST_KEY)
                }.onSuccess { readAllPreferences() }
                    .onFailure { Log.e(tag, "savePlaylistId: ${it.message}") }
            }
        }

        private fun saveCurrentSoundPosition(position: Int) {
            viewModelScope.launch {
                runCatching {
                    serviceDataPreference.saveUserPreference(position, Constants.POSITION_KEY)
                }.onFailure { Log.e(tag, "saveCurrentSoundPosition: ${it.message}") }
            }
        }
    }
