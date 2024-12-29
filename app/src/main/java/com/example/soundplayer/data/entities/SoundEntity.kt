package com.example.soundplayer.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.soundplayer.model.Sound
import java.util.Date


@Entity(tableName = "sound")
data class SoundEntity(
    @PrimaryKey(autoGenerate = false)
    val soundId : Long,
    val title : String ="",
    val artistsName: String ="",
    val albumName: String ="",
    val path : String ="",
    val duration :String ="",
    val urlMediaImage :String ="",
    val urlAlbumImage :String ="",
    val insertedDate : Long = Date().time
)

fun SoundEntity.toSound() = Sound(
    idSound = this.soundId,
    path = this.path,
    artistName = this.artistsName,
    albumName = this.albumName,
    duration = this.duration,
    title = this.title,
    uriMedia = this.urlMediaImage,
    uriMediaAlbum = this.urlAlbumImage,
    insertedDate = this.insertedDate
)