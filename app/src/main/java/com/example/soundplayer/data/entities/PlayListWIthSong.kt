package com.example.soundplayer.data.entities

import androidx.room.Embedded

import androidx.room.Junction
import androidx.room.Relation

data class PlayListWithSong(
    @Embedded
    val playList : PlayListEntity,
    @Relation(
        parentColumn = "playListId",
        entity = SoundEntity::class,
        entityColumn = "soundId",
        associateBy = Junction(
             value =  PlayListAndSoundCrossEntity::class,
             parentColumn = "playListId",
             entityColumn = "soundId",
        )

    )
    val soundOfPlayList :List<SoundEntity>
)