package com.example.soundplayer.data.entities

import androidx.room.Entity
import androidx.room.Index

@Entity(
    primaryKeys = ["playListId", "soundId"],
    indices = [
        Index(value = ["soundId"]),
    ],
)
class PlayListAndSoundCrossEntity(
    val playListId: Long,
    val soundId: Long,
)
