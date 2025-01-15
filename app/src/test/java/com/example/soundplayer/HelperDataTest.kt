package com.example.soundplayer

import com.example.soundplayer.data.entities.PlayListAndSoundCrossEntity
import com.example.soundplayer.data.entities.PlayListEntity
import com.example.soundplayer.data.entities.SongWithPlaylists
import com.example.soundplayer.data.entities.SoundEntity
import com.example.soundplayer.model.PlayList
import com.example.soundplayer.model.PlaylistWithSoundDomain
import com.example.soundplayer.model.Sound
import java.util.Date

object HelperDataTest {

    fun soundEntityList() = listOf(
        SoundEntity(
            soundId = 1L,
            title = "Song One",
            artistsName = "Artist One",
            albumName = "Album One",
            path = "/music/song_one.mp3",
            duration = "3:45",
            urlMediaImage = "https://example.com/song_one_image.jpg",
            urlAlbumImage = "https://example.com/album_one_image.jpg",
            insertedDate = Date().time
        ),
        SoundEntity(
            soundId = 2L,
            title = "Song Two",
            artistsName = "Artist Two",
            albumName = "Album Two",
            path = "/music/song_two.mp3",
            duration = "4:30",
            urlMediaImage = "https://example.com/song_two_image.jpg",
            urlAlbumImage = "https://example.com/album_two_image.jpg",
            insertedDate = Date().time
        ),
        SoundEntity(
            soundId = 3L,
            title = "Song Three",
            artistsName = "Artist Three",
            albumName = "Album Three",
            path = "/music/song_three.mp3",
            duration = "5:00",
            urlMediaImage = "https://example.com/song_three_image.jpg",
            urlAlbumImage = "https://example.com/album_three_image.jpg",
            insertedDate = Date().time
        ),
        SoundEntity(
            soundId = 4L,
            title = "Song Four",
            artistsName = "Artist Four",
            albumName = "Album Four",
            path = "/music/song_four.mp3",
            duration = "2:50",
            urlMediaImage = "https://example.com/song_four_image.jpg",
            urlAlbumImage = "https://example.com/album_four_image.jpg",
            insertedDate = Date().time
        ),
        SoundEntity(
            soundId = 5L,
            title = "Song Five",
            artistsName = "Artist Five",
            albumName = "Album Five",
            path = "/music/song_five.mp3",
            duration = "3:15",
            urlMediaImage = "https://example.com/song_five_image.jpg",
            urlAlbumImage = "https://example.com/album_five_image.jpg",
            insertedDate = Date().time
        )
    )
    fun playlistWithSoundDomainList()=  List(10) {
        PlaylistWithSoundDomain(
            soundOfPlayList = mutableSetOf<Sound>(),
            playList = PlayList(
                idPlayList = null,
                listSound = mutableSetOf(),
                name = "Teste",
                currentMusicPosition = 1,
            )
        )
    }
    fun listPlayListAndSoundCrossEntity() = listOf(
        PlayListAndSoundCrossEntity(1, 1),
        PlayListAndSoundCrossEntity(1, 2),
        PlayListAndSoundCrossEntity(1, 4),
        PlayListAndSoundCrossEntity(1, 5),
        PlayListAndSoundCrossEntity(1, 22),
    )
    fun listSoundWIthPlayLists() = listOf(
        SongWithPlaylists(
            song = soundEntityList().last(),
            playlists = listOfPlayListEtity()
        )
    )
    fun listSound() = mutableSetOf(
        Sound(
            idSound = 1,
            title = "Minha primeira música",
            duration = "3:40",
            albumName = "Nome alnbum",
            artistName = "Altor",
            path = "path",
            uriMediaAlbum = "",
            uriMedia = "",
            insertedDate = 2313231
        ),
        Sound(
            idSound = 2,
            title = "Minha segunda música",
            duration = "3:40",
            albumName = "Nome alnbum",
            artistName = "Altor",
            path = "path",
            uriMediaAlbum = "",
            uriMedia = "",
            insertedDate = 2313231
        )
    )

    fun listOfPlayListEtity() = listOf(
        PlayListEntity(
            playListId = 1,
            title = "First playlist",
            currentSoundPosition = 0,
        ), PlayListEntity(
            playListId = 2,
            title = "Second playlist",
            currentSoundPosition = 0,
        ), PlayListEntity(
            playListId = 3,
            title = "Third playlist",
            currentSoundPosition = 0,
        ), PlayListEntity(
            playListId = 4,
            title = "Fourth playlist",
            currentSoundPosition = 0,
        ),

        )
}