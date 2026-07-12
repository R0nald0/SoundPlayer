package com.example.soundplayer.service

import androidx.media3.exoplayer.ExoPlayer
import com.example.soundplayer.commons.exceptions.PlaybackErrorException
import com.example.soundplayer.model.PlayList
import com.example.soundplayer.model.Sound
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface PlaybackController {
    val playbackState: StateFlow<PlaybackState>

    fun getPlayer(): ExoPlayer

    fun getPlaybackError(): SharedFlow<PlaybackErrorException>

    fun getActualPlayList(): PlayList?

    fun playPlaylist(playList: PlayList): PlayList?

    fun playAllMusicFromFirst(sounds: Set<Sound>)

    fun destroyPlayer()

    fun addItemFromListMusic(soundsToInsertPlayList: Set<Sound>)

    fun removeItemFromListMusic(index: Int)

    fun reorderPlaylistWithMoves(newList: List<Sound>)
}
