package com.example.soundplayer.model

import android.os.Parcelable
import com.example.soundplayer.data.entities.SoundEntity
import kotlinx.parcelize.Parcelize

@Parcelize
data class Sound(
    val idSound : Long,
    val path : String ="",
    val artistName :String?,
    val albumName :String?,
    val duration:String ="",
    var title: String ="",
    var uriMedia: String? = null,
    var uriMediaAlbum: String? =null,
    val insertedDate : Long?
):Parcelable{
    constructor(soundEntity: SoundEntity):this(
        idSound = soundEntity.soundId,
        artistName = soundEntity.artistsName,
        albumName =soundEntity.albumName,
        path = soundEntity.path,
        duration = soundEntity.duration,
        title = soundEntity.title,
        uriMedia = soundEntity.urlMediaImage,
        uriMediaAlbum = soundEntity.urlAlbumImage,
        insertedDate = soundEntity.insertedDate
    )
}

fun Sound.toSoundEntity()= SoundEntity(
    soundId = this.idSound!!,
    title = this.title,
    artistsName = artistName ?: "",
    albumName = albumName ?:"",
    path = this.path,
    duration =this.duration,
    urlAlbumImage = this.uriMediaAlbum.toString(),
    urlMediaImage = this.uriMedia.toString(),
)
