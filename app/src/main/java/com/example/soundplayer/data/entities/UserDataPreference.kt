package com.example.soundplayer.data.entities

data class UserDataPreference(
    val playlistPreferenceId: Long?,
    val positionPreference: Int,
    var isDarkMode: Int = 0,
    val sizeTitleMusic: Float = 16f,
    val orderedSound: Int = 0,
)
