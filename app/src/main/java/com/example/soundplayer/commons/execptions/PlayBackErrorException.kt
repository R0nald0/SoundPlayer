package com.example.soundplayer.commons.execptions

import com.example.soundplayer.model.DataSoundPlayListToUpdate

data class PlayBackErrorException(
     val messages : String,
     val causes : Throwable?,
     val dataSoundPlayListToUpdate: DataSoundPlayListToUpdate? = null,
     val code : Int
    ) : Throwable(message =messages, cause = causes  )
