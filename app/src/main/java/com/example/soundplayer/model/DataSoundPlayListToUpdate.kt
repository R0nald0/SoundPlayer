package com.example.soundplayer.model

data class DataSoundPlayListToUpdate(
    val idPlayList : Long,
    val positionSound:List<Int>,
    val sounds:Set<Sound>,
)
