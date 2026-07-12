package com.example.soundplayer.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.AudioAttributes
import androidx.media3.exoplayer.ExoPlayer
import com.example.soundplayer.data.repository.DataStorePreferenceRepository
import com.example.soundplayer.model.PlayList
import com.example.soundplayer.service.ServicePlayer
import com.example.soundplayer.service.UserPreferencesService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SoundViewModel
    @Inject
    constructor(
        private val dataStorePreferenceRepository: DataStorePreferenceRepository,
        private val userPreferencesService: UserPreferencesService,
        private val servicePlayer: ServicePlayer,
    ) : ViewModel() {
        private val tag = "SoundViewModel"

        private val _uiState = MutableStateFlow(SoundUiState())
        val uiState: StateFlow<SoundUiState> = _uiState.asStateFlow()

        private val _uiEvent = MutableSharedFlow<SoundUiEvent>()
        val uiEvent: SharedFlow<SoundUiEvent> = _uiEvent.asSharedFlow()

        // Acesso direto ao player necessário para o Media3 PlayerView
        val player: ExoPlayer get() = servicePlayer.getPlayer()

        init {
            observePlayerState()
        }

        fun onIntent(intent: SoundIntent) {
            when (intent) {
                is SoundIntent.PlayPlayList -> playPlayList(intent.playList)
                is SoundIntent.LoadActualPlayList -> loadActualPlayList()
                is SoundIntent.UpdateAudioFocus -> updateAudioFocus()
                is SoundIntent.SavePreference -> savePreference()
            }
        }

        fun currentSoundPosition(): Int = _uiState.value.currentPlayList?.currentMusicPosition ?: 0

        private fun observePlayerState() {
            viewModelScope.launch {
                servicePlayer
                    .getPlaybackState()
                    .collect { playbackState ->
                        _uiState.update {
                            it.copy(
                                currentSound = playbackState.currentSound,
                                currentPlayList = playbackState.currentPlayList,
                                isPlaying = playbackState.isPlaying,
                            )
                        }
                    }
            }
            viewModelScope.launch {
                servicePlayer
                    .getPlaybackError()
                    .collect { error ->
                        _uiEvent.emit(
                            SoundUiEvent.PlaybackError(
                                message = error.message ?: "Erro de reprodução",
                                data = error.dataSoundPlayListToUpdate,
                            ),
                        )
                    }
            }
        }

        private fun playPlayList(playList: PlayList) {
            viewModelScope.launch {
                runCatching { servicePlayer.playPlaylist(playList) }
                    .onSuccess { currentPlayList ->
                        if (currentPlayList != null) {
                            _uiState.update { it.copy(currentPlayList = currentPlayList) }
                            savePreference()
                        }
                    }.onFailure { Log.e(tag, "playPlayList: ${it.message}") }
            }
        }

        private fun loadActualPlayList() {
            _uiState.update { it.copy(currentPlayList = servicePlayer.getActualPlayList()) }
        }

        private fun updateAudioFocus() {
            player.setAudioAttributes(AudioAttributes.DEFAULT, true)
        }

        private fun savePreference() {
            viewModelScope.launch {
                val currentPlayList = _uiState.value.currentPlayList ?: return@launch
                runCatching {
                    dataStorePreferenceRepository.savePreference(
                        playlistKeyId = currentPlayList.idPlayList,
                        positionSoundKey = currentPlayList.currentMusicPosition,
                    )
                }.onSuccess { readPreferences() }
                    .onFailure { Log.e(tag, "savePreference: ${it.message}") }
            }
        }

        private fun readPreferences() {
            viewModelScope.launch {
                runCatching { userPreferencesService.readAppAllPreferences() }
                    .onSuccess { prefs -> _uiState.update { it.copy(preferences = prefs) } }
                    .onFailure { Log.e(tag, "readPreferences: ${it.message}") }
            }
        }
    }
