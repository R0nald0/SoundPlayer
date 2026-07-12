package com.example.soundplayer.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soundplayer.commons.constants.Constants
import com.example.soundplayer.data.source.MediaStoreSoundDataSource
import com.example.soundplayer.model.DataSoundPlayListToUpdate
import com.example.soundplayer.model.PlayList
import com.example.soundplayer.model.Sound
import com.example.soundplayer.service.ServicePlayer
import com.example.soundplayer.service.SoundDomainService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

@HiltViewModel
class PlayListViewModel
    @Inject
    constructor(
        private val soundDomainService: SoundDomainService,
        private val servicePlayer: ServicePlayer,
        private val mediaStoreSoundDataSource: MediaStoreSoundDataSource,
    ) : ViewModel() {
        private val tag = "PlayListViewModel"
        private val playlistMutex = Mutex()

        private val _uiState = MutableStateFlow(PlayListUiState())
        val uiState: StateFlow<PlayListUiState> = _uiState.asStateFlow()

        private val _uiEvent = MutableSharedFlow<PlayListUiEvent>()
        val uiEvent: SharedFlow<PlayListUiEvent> = _uiEvent.asSharedFlow()

        fun onIntent(intent: PlayListIntent) {
            when (intent) {
                is PlayListIntent.LoadPlaylists -> loadPlaylists()
                is PlayListIntent.CountSounds -> countSounds()
                is PlayListIntent.FindAllSounds -> findAllSounds()
                is PlayListIntent.ClearSelectedPlayList -> _uiState.update { it.copy(selectedPlayList = null) }
                is PlayListIntent.SavePlayList -> savePlayList(intent.playList)
                is PlayListIntent.DeletePlayList -> deletePlayList(intent.playList)
                is PlayListIntent.RenamePlayList -> renamePlayList(intent.playList)
                is PlayListIntent.FindPlayListById -> findPlayListById(intent.id)
                is PlayListIntent.UpdateSoundAtPlaylist -> updateSoundAtPlaylist(intent.data)
                is PlayListIntent.RemoveSoundFromPlayList -> removeSoundFromPlayList(intent.data)
                is PlayListIntent.LoadSoundsFromDevice -> loadSoundsFromDevice()
                is PlayListIntent.SaveAllSoundsByContentProvider -> saveAllSoundsByContentProvider(intent.sounds)
                is PlayListIntent.CompareDeviceSounds -> compareDeviceSounds(intent.idPlayList)
                is PlayListIntent.UpdateSoundList -> updateSoundList(intent.sounds)
                is PlayListIntent.ComparePlayLists -> comparePlayLists(intent.soundsOfSystem, intent.idPlayList)
            }
        }

        private fun loadPlaylists() {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true, error = null) }
                runCatching { servicePlayer.findAllPlayList() }
                    .onSuccess { playlists ->
                        _uiState.update { it.copy(playlists = playlists, isLoading = false) }
                    }.onFailure { error ->
                        Log.e(tag, "loadPlaylists: ${error.message}")
                        _uiState.update { it.copy(isLoading = false) }
                        _uiEvent.emit(PlayListUiEvent.ShowError("Não conseguimos buscar as playlists"))
                    }
            }
        }

        private fun findAllSounds() {
            viewModelScope.launch {
                runCatching { soundDomainService.findAllSound() }
                    .onSuccess { sounds -> _uiState.update { it.copy(soundsFromDb = sounds.toSet()) } }
                    .onFailure { Log.e(tag, "findAllSounds: ${it.message}") }
            }
        }

        private fun countSounds() {
            viewModelScope.launch {
                runCatching { soundDomainService.findAllSound() }
                    .onSuccess { sounds ->
                        _uiState.update { it.copy(soundListSize = sounds.size) }
                    }.onFailure { Log.e(tag, "countSounds: ${it.message}") }
            }
        }

        private fun savePlayList(playList: PlayList) {
            viewModelScope.launch {
                runCatching { servicePlayer.createPlayList(playList) }
                    .onSuccess { ids ->
                        if (ids.isNotEmpty()) loadPlaylists()
                    }.onFailure { Log.e(tag, "savePlayList: ${it.message}") }
            }
        }

        private fun deletePlayList(playList: PlayList) {
            viewModelScope.launch {
                runCatching { servicePlayer.deletePlayList(playList) }
                    .onSuccess { affected ->
                        if (affected != 0) loadPlaylists()
                    }.onFailure { Log.e(tag, "deletePlayList: ${it.message}") }
            }
        }

        private fun renamePlayList(playList: PlayList) {
            viewModelScope.launch {
                val playlistId = playList.idPlayList
                if (playlistId == null) {
                    _uiEvent.emit(PlayListUiEvent.ShowError("Playlist inválida para renomear"))
                    return@launch
                }

                runCatching {
                    servicePlayer.updateNamePlayList(playlistId, playList.name)
                }.onSuccess { affected ->
                    if (affected != 0) loadPlaylists()
                }.onFailure { Log.e(tag, "renamePlayList: ${it.message}") }
            }
        }

        private fun findPlayListById(id: Long) {
            viewModelScope.launch {
                _uiState.update { it.copy(error = null) }
                runCatching { servicePlayer.findPlayListById(id) }
                    .onSuccess { playlist ->
                        _uiState.update { it.copy(selectedPlayList = playlist) }
                    }.onFailure { error ->
                        Log.e(tag, "findPlayListById id=$id: ${error.message}")
                        if (_uiState.value.soundListSize > 0) {
                            _uiEvent.emit(PlayListUiEvent.ShowError("Não conseguimos encontrar a playlist"))
                        }
                    }
            }
        }

        private fun updateSoundAtPlaylist(data: DataSoundPlayListToUpdate) {
            viewModelScope.launch {
                playlistMutex.withLock {
                    runCatching {
                        servicePlayer.addItemFromListMusic(
                            idPlayList = data.idPlayList,
                            soundsToInsertPlayList = data.sounds,
                        )
                    }.onSuccess { ids ->
                        if (ids.isNotEmpty()) refreshAfterUpdate(data.idPlayList)
                    }.onFailure { Log.e(tag, "updateSoundAtPlaylist: ${it.message}") }
                }
            }
        }

        private fun removeSoundFromPlayList(data: DataSoundPlayListToUpdate) {
            viewModelScope.launch {
                playlistMutex.withLock {
                    val soundId = data.sounds.firstOrNull()?.idSound
                    val position = data.positionSound.firstOrNull()
                    if (soundId == null || position == null) {
                        _uiEvent.emit(PlayListUiEvent.ShowError("Música inválida para remover da playlist"))
                        return@withLock
                    }

                    runCatching {
                        servicePlayer.removeItemFromListMusic(
                            idPlayList = data.idPlayList,
                            idSound = soundId,
                            indexSound = position,
                        )
                    }.onSuccess { affected ->
                        if (affected != 0) refreshAfterUpdate(data.idPlayList)
                    }.onFailure { Log.e(tag, "removeSoundFromPlayList: ${it.message}") }
                }
            }
        }

        private fun loadSoundsFromDevice() {
            viewModelScope.launch {
                val sounds = mediaStoreSoundDataSource.findDeviceSounds()
                if (sounds.isEmpty()) {
                    _uiState.update { it.copy(soundsFromDb = emptySet()) }
                    _uiEvent.emit(
                        PlayListUiEvent.ShowError(
                            "Não encontramos nenhum arquivo de áudio no seu aparelho. Adicione arquivos de áudio para continuar.",
                        ),
                    )
                    return@launch
                }
                saveAllSoundsByContentProvider(sounds)
            }
        }

        private fun saveAllSoundsByContentProvider(sounds: Set<Sound>) {
            if (sounds.isEmpty()) {
                _uiState.update { it.copy(soundsFromDb = emptySet()) }
                return
            }
            viewModelScope.launch {
                _uiState.update { it.copy(error = null) }
                runCatching { servicePlayer.saveSoundProvideFromDb(sounds) }
                    .onSuccess { ids ->
                        if (!ids.isNullOrEmpty()) {
                            val allSounds = soundDomainService.findAllSound()
                            val allMusicPlaylist =
                                PlayList(
                                    idPlayList = null,
                                    name = Constants.ALL_MUSIC_NAME,
                                    listSound = allSounds.toMutableSet(),
                                    currentMusicPosition = 0,
                                )
                            servicePlayer.createPlayList(allMusicPlaylist)
                            loadPlaylists()
                            _uiState.update {
                                it.copy(
                                    soundsFromDb = allSounds.toSet(),
                                    soundListSize = allSounds.size,
                                )
                            }
                        }
                    }.onFailure { error ->
                        Log.e(tag, "saveAllSoundsByContentProvider: ${error.message}")
                        _uiEvent.emit(
                            PlayListUiEvent.ShowError("Algo deu errado ao salvar as músicas. Tente novamente."),
                        )
                    }
            }
        }

        private fun updateSoundList(sounds: Set<Sound>) {
            viewModelScope.launch {
                playlistMutex.withLock {
                    runCatching { soundDomainService.saveSounds(sounds) }
                        .onSuccess { ids ->
                            if (ids.isNotEmpty()) {
                                val longList = servicePlayer.addItemFromListMusic(1, sounds)
                                if (longList.isNotEmpty()) refreshAfterUpdate(1L)
                            }
                        }.onFailure { Log.e(tag, "updateSoundList: ${it.message}") }
                }
            }
        }

        private fun comparePlayLists(
            soundsOfSystem: Set<Sound>,
            idPlayList: Long,
        ) {
            viewModelScope.launch {
                runCatching {
                    servicePlayer.comparePlaylistsAndReturnDifference(soundsOfSystem, idPlayList)
                }.onSuccess { diff ->
                    _uiState.update { it.copy(comparedSounds = diff) }
                }.onFailure { error ->
                    Log.e(tag, "comparePlayLists: ${error.message}")
                    _uiEvent.emit(PlayListUiEvent.ShowError("Não conseguimos verificar as playlists"))
                }
            }
        }

        private fun compareDeviceSounds(idPlayList: Long) {
            viewModelScope.launch {
                comparePlayLists(
                    soundsOfSystem = mediaStoreSoundDataSource.findDeviceSounds(),
                    idPlayList = idPlayList,
                )
            }
        }

        private suspend fun refreshAfterUpdate(idPlaylist: Long) {
            if (idPlaylist == 1L) {
                val size = soundDomainService.findAllSound().size
                _uiState.update { it.copy(soundListSize = size) }
            }
            runCatching { servicePlayer.findPlayListById(idPlaylist) }
                .onSuccess { playlist -> _uiState.update { it.copy(selectedPlayList = playlist) } }
                .onFailure { Log.e(tag, "refreshAfterUpdate findPlayListById: ${it.message}") }

            runCatching { servicePlayer.findAllPlayList() }
                .onSuccess { playlists -> _uiState.update { it.copy(playlists = playlists) } }
                .onFailure { Log.e(tag, "refreshAfterUpdate findAllPlayList: ${it.message}") }
        }
    }
