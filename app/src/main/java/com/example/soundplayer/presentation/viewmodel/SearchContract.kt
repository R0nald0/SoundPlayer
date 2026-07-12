package com.example.soundplayer.presentation.viewmodel

import com.example.soundplayer.model.SongWithPlayListDomain

sealed interface SearchIntent {
    data class SearchByTitle(
        val title: String,
    ) : SearchIntent

    data object ClearSearch : SearchIntent
}

data class SearchUiState(
    val results: List<SongWithPlayListDomain> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)
