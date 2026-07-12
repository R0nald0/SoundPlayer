package com.example.soundplayer.data.source

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import com.example.soundplayer.model.Sound
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject

class MediaStoreSoundDataSource
    @Inject
    constructor(
        @param:ApplicationContext private val context: Context,
    ) {
        fun findDeviceSounds(): Set<Sound> {
            val projection =
                arrayOf(
                    MediaStore.Audio.Media.TITLE,
                    MediaStore.Audio.Media.DATA,
                    MediaStore.Audio.Media.DURATION,
                    MediaStore.Audio.Media._ID,
                    MediaStore.Audio.Media.ALBUM_ID,
                    MediaStore.Audio.Media.ARTIST,
                    MediaStore.Audio.Media.ALBUM,
                )

            return context.contentResolver
                .query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    projection,
                    null,
                    null,
                    null,
                )?.use { cursor ->
                    val idIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                    val albumIdIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                    val durationIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                    val pathIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
                    val titleIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                    val artistIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                    val albumIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)

                    buildSet {
                        while (cursor.moveToNext()) {
                            val path = cursor.getString(pathIndex)
                            if (File(path).exists()) {
                                val mediaId = cursor.getLong(idIndex)
                                val albumId = cursor.getLong(albumIdIndex)
                                val mediaUri =
                                    ContentUris.withAppendedId(
                                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                                        mediaId,
                                    )
                                val albumUri =
                                    ContentUris.withAppendedId(
                                        Uri.parse("content://media/external/audio/albumart"),
                                        albumId,
                                    )

                                add(
                                    Sound(
                                        idSound = mediaId,
                                        path = path,
                                        artistName = cursor.getString(artistIndex),
                                        albumName = cursor.getString(albumIndex),
                                        duration = cursor.getInt(durationIndex).toString(),
                                        title = cursor.getString(titleIndex),
                                        uriMedia = mediaUri.toString(),
                                        uriMediaAlbum = albumUri.toString(),
                                        insertedDate = null,
                                    ),
                                )
                            }
                        }
                    }
                } ?: emptySet()
        }
    }
