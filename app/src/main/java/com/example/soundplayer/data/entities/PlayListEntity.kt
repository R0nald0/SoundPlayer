package com.example.soundplayer.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.soundplayer.model.PlayList
import com.example.soundplayer.model.Sound

@Entity(
    tableName = "playList",
)
data class PlayListEntity(
    @PrimaryKey(autoGenerate = true)
    val playListId:Long?,
    @ColumnInfo(name = "current_sound_position")
    val currentSoundPosition : Int = 0,
    val title :String ,
)


fun PlayListEntity.toPlayList()= PlayList(
    idPlayList = this.playListId,
    name = this.title,
    currentMusicPosition = this.currentSoundPosition,
    listSound= mutableSetOf<Sound>(),
)