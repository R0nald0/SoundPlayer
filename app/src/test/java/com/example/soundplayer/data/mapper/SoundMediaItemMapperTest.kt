package com.example.soundplayer.data.mapper

import androidx.media3.common.MediaMetadata
import com.example.soundplayer.model.Sound
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("SoundMediaItemMapper")
class SoundMediaItemMapperTest {
    @Test
    fun `dado sound, quando toMediaItem, entao preserva id e metadados`() {
        val sound =
            Sound(
                idSound = 10L,
                artistName = "Artist",
                albumName = "Album",
                duration = "02:05",
                title = "Song",
                uriMedia = "file:///music.mp3",
                uriMediaAlbum = "file:///album.jpg",
                insertedDate = 1L,
            )

        val mediaItem = SoundMediaItemMapper.toMediaItem(sound)

        assertThat(mediaItem.mediaId).isEqualTo("10")
        assertThat(mediaItem.mediaMetadata.title.toString()).isEqualTo("Song")
        assertThat(mediaItem.mediaMetadata.displayTitle.toString()).isEqualTo("Song")
        assertThat(mediaItem.mediaMetadata.artist.toString()).isEqualTo("Artist")
        assertThat(mediaItem.mediaMetadata.albumTitle.toString()).isEqualTo("Album")
    }

    @Test
    fun `dado metadata incompleto, quando fromMediaMetadata, entao aplica defaults`() {
        val metadata =
            MediaMetadata
                .Builder()
                .setTitle("Fallback title")
                .build()

        val sound =
            SoundMediaItemMapper.fromMediaMetadata(
                mediaId = "20",
                mediaMetadata = metadata,
                durationInMillis = 125_000L,
            )

        assertThat(sound.idSound).isEqualTo(20L)
        assertThat(sound.artistName).isEqualTo("Desconhecido")
        assertThat(sound.albumName).isEqualTo("Desconhecido")
        assertThat(sound.title).isEqualTo("Fallback title")
        assertThat(sound.duration).isEqualTo("02:05")
        assertThat(sound.insertedDate).isNull()
    }
}
