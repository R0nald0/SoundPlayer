package com.example.soundplayer.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soundplayer.model.SongWithPlayListDomain
import com.example.soundplayer.service.SoundDomainService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class SearchViewModel
    @Inject
    constructor(
        private val service: SoundDomainService,
    ) : ViewModel() {
        private val tag = "SearchViewModel"

        private val _uiState = MutableStateFlow(SearchUiState())
        val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

        private val query = MutableStateFlow("")

        init {
            observeSearchQuery()
        }

        fun onIntent(intent: SearchIntent) {
            when (intent) {
                is SearchIntent.SearchByTitle -> searchByTitle(intent.title)
                is SearchIntent.ClearSearch -> clearSearch()
            }
        }

        private fun searchByTitle(title: String) {
            if (title.isEmpty()) {
                clearSearch()
                return
            }
            query.update { title }
            _uiState.update { it.copy(results = emptyList(), isLoading = true, error = null) }
        }

        private fun clearSearch() {
            query.update { "" }
            _uiState.update { SearchUiState() }
        }

        private fun observeSearchQuery() {
            viewModelScope.launch {
                query
                    .debounce(800)
                    .distinctUntilChanged()
                    .flatMapLatest { title ->
                        flow {
                            if (title.isEmpty()) {
                                emit(SearchUiState())
                                return@flow
                            }

                            runCatching {
                                val results = mutableListOf<SongWithPlayListDomain>()
                                service
                                    .findSoundByTitle(title)
                                    .collect { result ->
                                        results += result
                                        emit(SearchUiState(results = results.toList(), isLoading = false))
                                    }

                                if (results.isEmpty()) {
                                    emit(SearchUiState(isLoading = false))
                                }
                            }.onFailure { error ->
                                Log.e(tag, "observeSearchQuery: ${error.message}")
                                emit(SearchUiState(error = error.message, isLoading = false))
                            }
                        }
                    }.collect { state -> _uiState.update { state } }
            }
        }
    }
