package com.example.soundplayer.model

import android.net.Uri
import android.os.Parcelable
import com.example.soundplayer.data.entities.SoundEntity
import kotlinx.parcelize.Parcelize

@Parcelize
data class Sound(
    val idSound : Long?,
    val path : String ="",
    val duration:String ="",
    var title: String ="",
    var uriMedia: Uri? = null,
    var uriMediaAlbum: Uri? =null
):Parcelable{
    constructor(soundEntity: SoundEntity):this(
        idSound = soundEntity.soundId,
        path = soundEntity.path,
        duration = soundEntity.duration,
        title = soundEntity.title,
        uriMedia = Uri.parse(soundEntity.urlMediaImage),
        uriMediaAlbum = Uri.parse(soundEntity.urlAlbumImage)
    )
}

fun Sound.toSoundEntity()= SoundEntity(
    soundId = idSound,
    title = this.title,
    path = this.path,
    duration =this.duration,
    urlAlbumImage = this.uriMediaAlbum.toString(),
    urlMediaImage = this.uriMedia.toString()
)
