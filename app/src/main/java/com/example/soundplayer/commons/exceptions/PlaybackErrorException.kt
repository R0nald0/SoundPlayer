package com.example.soundplayer.commons.exceptions

import com.example.soundplayer.model.DataSoundPlayListToUpdate

data class PlaybackErrorException(
    val messages: String,
    val causes: Throwable?,
    val dataSoundPlayListToUpdate: DataSoundPlayListToUpdate? = null,
    val code: Int,
) : Throwable(message = messages, cause = causes)
