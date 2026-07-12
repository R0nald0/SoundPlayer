package com.example.soundplayer.data.mapper

import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.example.soundplayer.commons.extension.convertMilesSecondToMinSec
import com.example.soundplayer.model.Sound

internal object SoundMediaItemMapper {
    fun toMediaItem(sound: Sound): MediaItem =
        MediaItem
            .Builder()
            .setMediaMetadata(sound.toMediaMetadata())
            .setUri(sound.uriMedia)
            .setMediaId(sound.idSound.toString())
            .build()

    fun toMediaItems(sounds: Set<Sound>): List<MediaItem> = sounds.map(::toMediaItem)

    fun fromMediaMetadata(
        mediaId: String?,
        mediaMetadata: MediaMetadata,
        durationInMillis: Long,
    ): Sound =
        Sound(
            idSound = mediaId?.toLongOrNull() ?: 0,
            artistName = (mediaMetadata.artist ?: "Desconhecido").toString(),
            albumName = (mediaMetadata.albumTitle ?: "Desconhecido").toString(),
            path = "",
            title = (mediaMetadata.displayTitle ?: mediaMetadata.title ?: "").toString(),
            duration = durationInMillis.convertMilesSecondToMinSec(),
            uriMediaAlbum = mediaMetadata.artworkUri.toString(),
            insertedDate = null,
        )

    private fun Sound.toMediaMetadata(): MediaMetadata =
        MediaMetadata
            .Builder()
            .setTitle(title)
            .setArtworkUri(uriMediaAlbum?.let(Uri::parse))
            .setDisplayTitle(title)
            .setArtist(artistName)
            .setAlbumTitle(albumName)
            .build()
}
