package com.example.soundplayer.data.entities

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.example.soundplayer.model.PlaylistWithSoundDomain

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


fun PlayListWithSong.toPlaylistWithSoundDomain()= PlaylistWithSoundDomain(
    playList = this.playList.toPlayList(),
    soundOfPlayList = this.soundOfPlayList.map { soundEntity -> soundEntity.toSound() }.toMutableSet()
)