package com.example.soundplayer.data.entities

import androidx.room.Embedded

import androidx.room.Junction
import androidx.room.Relation

data class PlayListWithSong(
    @Embedded
    val playList : PlayListEntity,
    @Relation(
        parentColumn = "playListId",
        entityColumn = "soundId",
        associateBy = Junction(PlayListAndSoundCrossEntity::class)

    )
    val soundOfPlayList :List<SoundEntity>
) {
}