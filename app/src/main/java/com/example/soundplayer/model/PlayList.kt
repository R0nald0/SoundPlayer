package com.example.soundplayer.model

import android.os.Parcelable
import com.example.soundplayer.data.entities.PlayListEntity
import com.example.soundplayer.data.entities.PlayListWithSong
import com.example.soundplayer.data.entities.toSound
import kotlinx.parcelize.Parcelize

@Parcelize
data class PlayList(
    val idPlayList: Long?,
    val name :String,
    var currentMusicPosition : Int,
    val listSound :MutableSet<Sound>
):Parcelable{
    constructor(playListEntity:PlayListWithSong):this(
        idPlayList =playListEntity.playList.playListId,
        name = playListEntity.playList.title,
        currentMusicPosition= playListEntity.playList.currentSoundPosition,
        listSound = playListEntity.soundPlayList.map {soundEntity ->
               soundEntity.toSound()
        }.toMutableSet()
    )
}

fun PlayList.toEntity() = PlayListEntity(
    playListId = this.idPlayList,
    currentSoundPosition = this.currentMusicPosition,
    title = this.name,
)
