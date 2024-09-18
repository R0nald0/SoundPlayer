package com.example.soundplayer.model

data class SongWithPlayListDomain(
    val sound: Sound,
    val listOfPlayLists: List<PlayList>
)
