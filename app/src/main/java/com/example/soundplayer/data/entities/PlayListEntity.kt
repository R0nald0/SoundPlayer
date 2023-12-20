package com.example.soundplayer.data.entities

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "playList",
)
data class PlayListEntity(
    @PrimaryKey(autoGenerate = true)
    val playListId:Long?,
    @ColumnInfo(name = "current_sound_position")
    val currentSoundPosition :Int = 0,
    @ColumnInfo(name = "title_music")
    val title :String ,
){
}