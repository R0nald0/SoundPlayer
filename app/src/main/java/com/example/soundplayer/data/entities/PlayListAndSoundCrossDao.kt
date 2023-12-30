package com.example.soundplayer.data.entities

import androidx.room.Entity

@Entity(primaryKeys = ["playListId","soundId"])
class PlayListAndSoundCrossEntity (
    val playListId :Long,
    val soundId : Long,
)