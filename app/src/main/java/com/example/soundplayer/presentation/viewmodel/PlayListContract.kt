package com.example.soundplayer.presentation.viewmodel

import com.example.soundplayer.model.DataSoundPlayListToUpdate
import com.example.soundplayer.model.PlayList
import com.example.soundplayer.model.PlaylistWithSoundDomain
import com.example.soundplayer.model.Sound

sealed interface PlayListIntent {
    data object LoadPlaylists : PlayListIntent

    data object CountSounds : PlayListIntent

    data object FindAllSounds : PlayListIntent

    data object ClearSelectedPlayList : PlayListIntent

    data class SavePlayList(
        val playList: PlayList,
    ) : PlayListIntent

    data class DeletePlayList(
        val playList: PlayList,
    ) : PlayListIntent

    data class RenamePlayList(
        val playList: PlayList,
    ) : PlayListIntent

    data class FindPlayListById(
        val id: Long,
    ) : PlayListIntent

    data class UpdateSoundAtPlaylist(
        val data: DataSoundPlayListToUpdate,
    ) : PlayListIntent

    data class RemoveSoundFromPlayList(
        val data: DataSoundPlayListToUpdate,
    ) : PlayListIntent

    data object LoadSoundsFromDevice : PlayListIntent

    data class SaveAllSoundsByContentProvider(
        val sounds: Set<Sound>,
    ) : PlayListIntent

    data class CompareDeviceSounds(
        val idPlayList: Long,
    ) : PlayListIntent

    data class UpdateSoundList(
        val sounds: Set<Sound>,
    ) : PlayListIntent

    data class ComparePlayLists(
        val soundsOfSystem: Set<Sound>,
        val idPlayList: Long,
    ) : PlayListIntent
}

data class PlayListUiState(
    val playlists: List<PlaylistWithSoundDomain> = emptyList(),
    val selectedPlayList: PlayList? = null,
    val soundsFromDb: Set<Sound> = emptySet(),
    val soundListSize: Int = 0,
    val comparedSounds: Set<Sound> = emptySet(),
    val isLoading: Boolean = false,
    val error: String? = null,
)

sealed interface PlayListUiEvent {
    data class ShowError(
        val message: String,
    ) : PlayListUiEvent
}
