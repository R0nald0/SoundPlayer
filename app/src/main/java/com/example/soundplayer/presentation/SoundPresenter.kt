package com.example.soundplayer.presentation


import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.soundplayer.model.PlayList
import com.example.soundplayer.model.Sound
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SoundPresenter @Inject constructor(
    private val exoPlayer: ExoPlayer
) :ViewModel(){
     var playerWhenRead = true
     var currentItem = 0
     var playBackPosition = 0L

    var actualSound  = MutableLiveData<Sound>()
    var isPlayingObserver  = MutableLiveData<Boolean>()
    private var _currentPlayList : PlayList ? = null
    val currentPlayList : PlayList?
           get() = _currentPlayList
    private lateinit var listMediaItem  : List<MediaItem>

     fun getPlayer():ExoPlayer{
       return exoPlayer
     }

    fun getAllMusics(playList: PlayList) {

           if (_currentPlayList != null &&  _currentPlayList!!.name != playList.name){
               exoPlayer.stop()
               exoPlayer.clearMediaItems()
            }

            if (!exoPlayer.isPlaying || playList.currentMusicPosition !=currentItem ){
                _currentPlayList = playList
                listMediaItem= playList.listSound.map{ sound ->
                    MediaItem.Builder()
                        .setMediaMetadata(createMetaData(sound))
                        .setUri(sound.path)
                        .build()
                }

                exoPlayer.seekTo(playList.currentMusicPosition,playBackPosition)
                currentItem = playList.currentMusicPosition
            }
    }
    private fun createMetaData(sound :Sound):MediaMetadata{

        return MediaMetadata.Builder()
            .setTitle(sound.title)
            .setArtworkUri(sound.uriMediaAlbum)
            .setDisplayTitle(sound.title)
            .build()
    }
    private fun configActualSound(){
      val mediaItem   = exoPlayer.currentMediaItem
            if (mediaItem != null) {
                val sound =Sound(
                    idSound = null,
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
                        idSound = null,
                        path = "",
                        title = mediaMetadata.displayTitle.toString(),
                        duration = exoPlayer.duration.toString(),
                        uriMediaAlbum = mediaMetadata.artworkUri
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

    fun destroyPlayer(){
        exoPlayer.stop()
        exoPlayer.release()
    }

}