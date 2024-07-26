package com.example.soundplayer.data.entities

data class UserDataPreferecence(
    val idPreference : Long?,
    val postionPreference:Int,
    var isDarkMode: Int =0 ,
    val sizeTitleMusic : Float = 16f,
    val orderedSound : Int = 0
)
