package com.example.soundplayer.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SoundList(
    var currentMusic : Int,
    val listMusic :Set<Sound>
):Parcelable
