package com.example.soundplayer.service

import com.example.soundplayer.model.PlayList
import com.example.soundplayer.model.Sound

data class PlaybackState(
    val currentSound: Sound? = null,
    val currentPlayList: PlayList? = null,
    val isPlaying: Boolean = false,
    val currentIndex: Int = 0,
)
