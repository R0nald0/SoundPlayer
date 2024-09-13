package com.example.soundplayer.data.entities

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.example.soundplayer.model.SongWithPlayListDomain


data class SongWithPlaylists(
    @Embedded val song: SoundEntity,
    @Relation(
        parentColumn = "soundId",
        entityColumn = "playListId",
        associateBy = Junction(PlayListAndSoundCrossEntity::class)
    )
    val playlists: List<PlayListEntity>
)

fun SongWithPlaylists.toSongWithPlayListDomain() = SongWithPlayListDomain(
    sound = this.song.toSound(),
    listOfPlayLists =this.playlists.map { it.toPlayList() }
)