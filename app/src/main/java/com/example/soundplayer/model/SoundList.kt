package com.example.soundplayer.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SoundList(
    var currentMusic : Int,
    val listMusic :MutableSet<Pair<Int, Sound>>
):Parcelable
