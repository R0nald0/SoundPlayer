package com.example.soundplayer.commons.execptions

data class Failure(
     val messages : String,
    val causes : Throwable?,
    val code : Int
    ) : Throwable(message =messages, cause = causes  )
