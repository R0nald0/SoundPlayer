package com.example.soundplayer.presentation.viewmodel

import com.example.soundplayer.data.entities.UserDataPreference
import com.example.soundplayer.model.DataSoundPlayListToUpdate
import com.example.soundplayer.model.PlayList
import com.example.soundplayer.model.Sound

sealed interface SoundIntent {
    data class PlayPlayList(
        val playList: PlayList,
    ) : SoundIntent

    data object LoadActualPlayList : SoundIntent

    data object UpdateAudioFocus : SoundIntent

    data object SavePreference : SoundIntent
}

data class SoundUiState(
    val currentSound: Sound? = null,
    val currentPlayList: PlayList? = null,
    val isPlaying: Boolean = false,
    val preferences: UserDataPreference? = null,
    val error: String? = null,
)

sealed interface SoundUiEvent {
    data class PlaybackError(
        val message: String,
        val data: DataSoundPlayListToUpdate?,
    ) : SoundUiEvent
}
