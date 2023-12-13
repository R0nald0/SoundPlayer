package com.example.soundplayer.model

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Sound(
    val path : String ="",
    val  duration:String ="",
    var title: String ="",
    var uriMedia: Uri? = null,
    var uriMediaAlbum: Uri? =null
):Parcelable{
}
