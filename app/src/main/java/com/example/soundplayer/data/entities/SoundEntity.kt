package com.example.soundplayer.data.entities

import android.net.Uri
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.soundplayer.model.Sound


@Entity(tableName = "sound")
data class SoundEntity(
    @PrimaryKey(autoGenerate = true)
    val soundId : Long?,
    val title : String ="",
    val path : String ="",
    val duration :String ="",
    val urlMediaImage :String ="",
    val urlAlbumImage :String ="",
)

fun SoundEntity.toSound() = Sound(
    idSound = this.soundId,
    path = this.path,
    duration = this.duration,
    title = this.title,
    uriMedia = Uri.parse(this.urlMediaImage),
    uriMediaAlbum = Uri.parse(this.urlAlbumImage)
)