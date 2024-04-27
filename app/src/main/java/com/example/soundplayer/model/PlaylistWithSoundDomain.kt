package com.example.soundplayer.model

import com.example.soundplayer.data.entities.PlayListWithSong


class PlaylistWithSoundDomain (
    val playList : PlayList,
    val soundOfPlayList :MutableSet<Sound>
)

fun PlaylistWithSoundDomain.toPlaylistWithSound()=PlayListWithSong(
    playList = this.playList.toEntity(),
    soundOfPlayList = soundOfPlayList.map { sound -> sound.toSoundEntity() }
)