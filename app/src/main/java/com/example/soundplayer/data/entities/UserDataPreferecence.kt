package com.example.soundplayer.data.entities

data class UserDataPreferecence(
    val idPreference : Long?,
    val postionPreference:Int,
    var isDarkMode: Boolean =false,
    val sizeTitleMusic : Float = 16f
)
