package com.example.soundplayer.presentation


import android.media.MediaPlayer
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.soundplayer.model.Sound
import com.example.soundplayer.model.SoundList
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SoundPresenter @Inject constructor(
    private val mediaPlayer: MediaPlayer,
    private val exoPlayer: ExoPlayer
) :ViewModel(){
     var playerWhenRead = true
     var currentItem = 0
     var playBackPosition = 0L

    var actualSound  = MutableLiveData<Sound>()
    var currentLivePosition  = MutableLiveData<Long>()
    var isPlayingObserver  = MutableLiveData<Boolean>()
    lateinit var allMusics : SoundList
    lateinit var listMediaItem  : List<MediaItem>


     fun getPlayer():ExoPlayer{
       return exoPlayer
     }

    fun getAllMusics(sounds: SoundList) {
        if (!exoPlayer.isPlaying || sounds.currentMusic != allMusics.currentMusic){
            allMusics = sounds
            listMediaItem= sounds.listMusic.map{sound->
                MediaItem.Builder()
                    .setMediaMetadata(createMetaData(sound))
                    .setUri(sound.path)
                    .build()
            }
            exoPlayer.seekTo(sounds.currentMusic,playBackPosition)
            currentItem = sounds.currentMusic
        }

    }
    private fun createMetaData(sound :Sound):MediaMetadata{
        return MediaMetadata.Builder()
            .setTitle(sound.title)
            .build()
    }
    private fun configActualSound(){
      val mediaItem   = exoPlayer.currentMediaItem
            if (mediaItem != null) {
                val sound =Sound(
                    path = "",
                    title = mediaItem.mediaMetadata.title.toString(),
                    duration = exoPlayer.duration.toString()
                )
                actualSound.postValue( sound)
        }

        exoPlayer.addListener(object :Player.Listener{
            override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
                super.onMediaMetadataChanged(mediaMetadata)
                    val sound =Sound(
                        path = "",
                        title = mediaMetadata.title.toString(),
                        duration = exoPlayer.duration.toString()
                    )
                    actualSound.value= sound
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                super.onIsPlayingChanged(isPlaying)
                isPlayingObserver.value = isPlaying
            }
        })
    }

    fun playAllMusicFromFist(){
        exoPlayer.addMediaItems(listMediaItem)
        exoPlayer.playWhenReady = playerWhenRead
        exoPlayer.prepare()
        configActualSound()
    }
    fun isPlaying(): Boolean? {
        return isPlayingObserver.value
    }



}