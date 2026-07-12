package com.example.soundplayer.data.repository

import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.example.soundplayer.commons.exceptions.PlaybackErrorException
import com.example.soundplayer.data.mapper.SoundMediaItemMapper
import com.example.soundplayer.model.DataSoundPlayListToUpdate
import com.example.soundplayer.model.PlayList
import com.example.soundplayer.model.Sound
import com.example.soundplayer.service.PlaybackController
import com.example.soundplayer.service.PlaybackState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class PlayerRepository
    @Inject
    constructor(
        private val exoPlayer: ExoPlayer,
    ) : PlaybackController {
        private var currentItem: Int = -1
        private var playWhenReady = true
        private var playbackPosition: Long = 0L
        private var playlistCurrentlyPlaying: PlayList? = null
        private val _playbackError = MutableSharedFlow<PlaybackErrorException>(extraBufferCapacity = 1)
        private val _playbackState = MutableStateFlow(PlaybackState())
        override val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

        private val playerListener =
            object : Player.Listener {
                override fun onPlayerError(error: PlaybackException) {
                    super.onPlayerError(error)
                    when (error.errorCode) {
                        PlaybackException.ERROR_CODE_PARSING_CONTAINER_UNSUPPORTED -> {
                            emitPlaybackError(
                                buildPlaybackError(
                                    messages = "Erro ao reproduzir m\u00eddia, formato inv\u00e1lido.",
                                    cause = error,
                                    code = 3003,
                                ),
                            )
                            skipToNextOrStopAfterError()
                        }
                        PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND -> {
                            emitPlaybackError(
                                buildPlaybackError(
                                    messages =
                                        "\u00c1udio n\u00e3o encontrado. " +
                                            "Verifique se o arquivo n\u00e3o foi exclu\u00eddo ou movido.",
                                    cause = error,
                                    code = 2005,
                                ),
                            )
                            skipToNextOrStopAfterError()
                        }
                    }
                }

                @OptIn(UnstableApi::class)
                override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
                    super.onMediaMetadataChanged(mediaMetadata)
                    val sound =
                        SoundMediaItemMapper.fromMediaMetadata(
                            mediaId = exoPlayer.currentMediaItem?.mediaId,
                            mediaMetadata = mediaMetadata,
                            durationInMillis = exoPlayer.duration,
                        )
                    currentItem = exoPlayer.currentMediaItemIndex
                    playlistCurrentlyPlaying?.currentMusicPosition = exoPlayer.currentMediaItemIndex
                    updatePlaybackState(
                        currentSound = sound,
                        currentPlayList = playlistCurrentlyPlaying,
                        currentIndex = currentItem,
                    )
                }

                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    super.onIsPlayingChanged(isPlaying)
                    updatePlaybackState(isPlaying = isPlaying)
                }
            }

        private fun buildPlaybackError(
            messages: String,
            cause: PlaybackException,
            code: Int,
        ): PlaybackErrorException {
            val sound =
                SoundMediaItemMapper.fromMediaMetadata(
                    mediaId = exoPlayer.currentMediaItem?.mediaId,
                    mediaMetadata = exoPlayer.mediaMetadata,
                    durationInMillis = exoPlayer.duration,
                )
            return PlaybackErrorException(
                messages = messages,
                causes = cause,
                code = code,
                dataSoundPlayListToUpdate =
                    DataSoundPlayListToUpdate(
                        positionSound = listOf(currentItem),
                        idPlayList = playlistCurrentlyPlaying?.idPlayList ?: 1,
                        sounds = setOf(sound),
                    ),
            )
        }

        override fun getPlayer() = exoPlayer

        override fun getPlaybackError(): SharedFlow<PlaybackErrorException> = _playbackError.asSharedFlow()

        override fun getActualPlayList(): PlayList? = playlistCurrentlyPlaying

        override fun playPlaylist(playList: PlayList): PlayList? {
            if (!playList.hasPlayablePosition()) {
                emitPlaybackInitializationError(
                    cause = IllegalArgumentException("Invalid playlist position ${playList.currentMusicPosition}"),
                )
                return null
            }

            return try {
                val currentPlaylist = playlistCurrentlyPlaying
                if (currentPlaylist != null && currentPlaylist.name != playList.name) {
                    exoPlayer.stop()
                    exoPlayer.clearMediaItems()
                    currentItem = -1
                }

                if (playList.currentMusicPosition == currentItem) {
                    playbackPosition = exoPlayer.currentPosition
                    currentItem = playList.currentMusicPosition
                    updatePlaybackState(currentPlayList = playlistCurrentlyPlaying, currentIndex = currentItem)
                } else if (!exoPlayer.isPlaying && playList.currentMusicPosition != currentItem) {
                    playbackPosition = 0L
                    playlistCurrentlyPlaying = playList
                    exoPlayer.seekTo(playList.currentMusicPosition, playbackPosition)
                    currentItem = playList.currentMusicPosition
                    updatePlaybackState(currentPlayList = playList, currentIndex = currentItem)
                    playAllMusicFromFirst(playList.listSound)
                } else {
                    playbackPosition = 0L
                    playlistCurrentlyPlaying = playList
                    exoPlayer.seekTo(playList.currentMusicPosition, playbackPosition)
                    currentItem = playList.currentMusicPosition
                    updatePlaybackState(currentPlayList = playList, currentIndex = currentItem)
                }

                playlistCurrentlyPlaying
            } catch (error: IllegalArgumentException) {
                emitPlaybackInitializationError(error)
                null
            } catch (error: IllegalStateException) {
                emitPlaybackInitializationError(error)
                null
            }
        }

        private fun emitPlaybackError(error: PlaybackErrorException) {
            _playbackError.tryEmit(error)
        }

        private fun emitPlaybackInitializationError(cause: Throwable) {
            emitPlaybackError(
                PlaybackErrorException(
                    causes = cause,
                    code = 0,
                    messages = "Erro ao inicializar a playlist",
                ),
            )
        }

        override fun playAllMusicFromFirst(sounds: Set<Sound>) {
            if (sounds.isEmpty()) return

            val mediaItems = SoundMediaItemMapper.toMediaItems(sounds)
            if (exoPlayer.mediaItemCount == 0) {
                exoPlayer.addMediaItems(mediaItems)
                exoPlayer.playWhenReady = playWhenReady
                exoPlayer.prepare()
            } else {
                exoPlayer.playWhenReady = playWhenReady
                exoPlayer.prepare()
            }
            syncCurrentSoundAndListener()
        }

        private fun skipToNextOrStopAfterError() {
            if (exoPlayer.hasNextMediaItem()) {
                exoPlayer.seekToNext()
                exoPlayer.prepare()
                exoPlayer.play()
            } else {
                exoPlayer.stop()
            }
        }

        private fun syncCurrentSoundAndListener() {
            exoPlayer.currentMediaItem?.let { mediaItem ->
                val sound =
                    SoundMediaItemMapper.fromMediaMetadata(
                        mediaId = mediaItem.mediaId,
                        mediaMetadata = mediaItem.mediaMetadata,
                        durationInMillis = exoPlayer.duration,
                    )
                updatePlaybackState(currentSound = sound)
            }
            // Remove antes de adicionar â€” garante exatamente um listener ativo
            exoPlayer.removeListener(playerListener)
            exoPlayer.addListener(playerListener)
        }

        private fun updatePlaybackState(
            currentSound: Sound? = _playbackState.value.currentSound,
            currentPlayList: PlayList? = _playbackState.value.currentPlayList,
            isPlaying: Boolean = _playbackState.value.isPlaying,
            currentIndex: Int = _playbackState.value.currentIndex,
        ) {
            _playbackState.value =
                PlaybackState(
                    currentSound = currentSound,
                    currentPlayList = currentPlayList,
                    isPlaying = isPlaying,
                    currentIndex = currentIndex,
                )
        }

        override fun destroyPlayer() {
            exoPlayer.removeListener(playerListener)
            exoPlayer.stop()
            exoPlayer.release()
        }

        override fun addItemFromListMusic(soundsToInsertPlayList: Set<Sound>) {
            val currentPlaylist = playlistCurrentlyPlaying
            if (currentPlaylist != null) {
                val updateMediaItems = mutableSetOf<Sound>()
                soundsToInsertPlayList.forEach { sound ->
                    if (!currentPlaylist.listSound.contains(sound)) {
                        updateMediaItems.add(sound)
                        currentPlaylist.listSound.add(sound)
                    }
                }
                if (updateMediaItems.isNotEmpty()) {
                    val mediaItems = SoundMediaItemMapper.toMediaItems(updateMediaItems)
                    exoPlayer.addMediaItems(mediaItems)
                }
            }
        }

        override fun removeItemFromListMusic(index: Int) {
            val playlist = playlistCurrentlyPlaying ?: return
            if (index !in 0 until playlist.listSound.size) return

            val sound = playlist.listSound.elementAt(index)
            val isRemoved = playlist.listSound.remove(sound)
            if (isRemoved) {
                exoPlayer.removeMediaItem(index)
            }
        }

        override fun reorderPlaylistWithMoves(newList: List<Sound>) {
            val currentIndex = exoPlayer.currentMediaItemIndex
            val currentItem = exoPlayer.currentMediaItem ?: return

            // Maps each sound path to its desired queue position.
            val targetOrder =
                newList
                    .mapIndexed { index, sound ->
                        sound.path to index
                    }.toMap()

            // Percorre a fila atual e gera lista de MediaItem com ordem antiga
            val currentItems = (0 until exoPlayer.mediaItemCount).map { exoPlayer.getMediaItemAt(it) }

            // Reorders MediaItems by comparing the current queue with targetOrder.
            var i = 0
            while (i < currentItems.size) {
                val mediaItem = currentItems[i]
                val uri = mediaItem.localConfiguration?.uri.toString()
                val correctIndex = targetOrder[uri]

                if (correctIndex != null && correctIndex != i) {
                    // Moves the current item to the desired position.
                    exoPlayer.moveMediaItem(i, correctIndex)
                } else {
                    i++
                }
            }

            // Keeps playback on the same media item after the reorder.
            val newIndex =
                (0 until exoPlayer.mediaItemCount)
                    .indexOfFirst {
                        exoPlayer
                            .getMediaItemAt(
                                it,
                            ).localConfiguration
                            ?.uri == currentItem.localConfiguration?.uri
                    }

            if (newIndex >= 0 && newIndex != currentIndex) {
                exoPlayer.seekTo(newIndex, C.TIME_UNSET)
            }
        }
    }

private fun PlayList.hasPlayablePosition(): Boolean =
    listSound.isNotEmpty() && currentMusicPosition in 0 until listSound.size
