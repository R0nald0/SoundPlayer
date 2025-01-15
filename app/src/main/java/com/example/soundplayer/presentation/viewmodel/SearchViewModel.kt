package com.example.soundplayer.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soundplayer.model.SongWithPlayListDomain
import com.example.soundplayer.service.SoundDomainService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class  StateUi(
    val songWithPlayListDomain :SongWithPlayListDomain? = null,
    val error : String? = null
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val service: SoundDomainService
) : ViewModel() {

    private val _uiState = MutableStateFlow(StateUi())
    var uiState: StateFlow<StateUi> = _uiState.asStateFlow()

    private val _loader = MutableStateFlow(false);
    var loader: StateFlow<Boolean> = _loader;


    fun findSoundBytitle(title: String) {
        if (title.isEmpty()) {
            _uiState
                .update {
                it.copy(songWithPlayListDomain = null)
            }
            return
        }
        viewModelScope.launch {

            service.findSoundByTitle(title)
                .flowOn(Dispatchers.Main)
                .onStart {
                    _loader.value = true
                }
                .debounce(800)
                .distinctUntilChanged()
                .catch { erro ->
                    _uiState.update {
                        it.copy(error = erro.message)
                    }
                    _loader.value = false
                }
                .collect { songWithPlayListDomain ->
                    _uiState.update {
                          it.copy(songWithPlayListDomain = songWithPlayListDomain)
                     }
                    _loader.value = false
                }
        }
    }
}
