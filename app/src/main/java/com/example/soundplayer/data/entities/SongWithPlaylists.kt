package com.example.soundplayer.data.entities

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation


data class SongWithPlaylists(
    @Embedded val song: SoundEntity,
    @Relation(
        parentColumn = "soundId",
        entityColumn = "playListId",
        associateBy = Junction(PlayListAndSoundCrossEntity::class)
    )
    val playlists: List<PlayListEntity>
)