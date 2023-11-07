package com.example.soundplayer.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.io.Serializable

@Parcelize
data class Sound(
    val path : String ="",
    val  duration:String ="",
    val title: String =""
):Parcelable{
}
