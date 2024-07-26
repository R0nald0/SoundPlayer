package com.example.soundplayer.data.repository

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.soundplayer.model.PlayList
import com.example.soundplayer.model.Sound
import javax.inject.Inject

class PlayerRepository @Inject constructor(
   private  val exoPlayer: ExoPlayer
) {
   private var currentItem: Int =-1
   private var playerWhenRead = true
   private var playBackPosition: Long = -0L
   private var _playlistCurrentlyPlaying :PlayList ? = null
   private var _actualSound  = MutableLiveData<Sound>()
   private var _playBackError  = MutableLiveData<String?>()
   private  var isPlayingObserver  = MutableLiveData<Boolean>()

   fun getPlayer() = exoPlayer
   fun getActaulSound() = _actualSound
   fun getPlayBackError() = _playBackError
   fun getAcutalPlayList() :PlayList?{
        return  _playlistCurrentlyPlaying
   }
   fun isPlaying()  = isPlayingObserver

   fun getAllMusics(playList: PlayList): PlayList? {

      if ( _playlistCurrentlyPlaying != null &&  _playlistCurrentlyPlaying!!.name != playList.name){
         exoPlayer.stop()
         exoPlayer.clearMediaItems()
         currentItem = -1
      }

      if ( playList.currentMusicPosition == currentItem){
         playBackPosition = exoPlayer.currentPosition
         currentItem = playList.currentMusicPosition
      }
      else if (!exoPlayer.isPlaying && playList.currentMusicPosition != currentItem){
         playBackPosition =0L
         _playlistCurrentlyPlaying = playList
         exoPlayer.seekTo(playList.currentMusicPosition,playBackPosition)
         currentItem = playList.currentMusicPosition
         playAllMusicFromFist(createMediaItemList(playList.listSound))
      }
      else{
         playBackPosition =0L
         _playlistCurrentlyPlaying = playList
         exoPlayer.seekTo(playList.currentMusicPosition,playBackPosition)
         currentItem = playList.currentMusicPosition
      }

      return  _playlistCurrentlyPlaying
   }

   private fun playAllMusicFromFist(listMediaItem : Set<MediaItem>){
      if (exoPlayer.mediaItemCount == 0){
         exoPlayer.addMediaItems(listMediaItem.toList())
         exoPlayer.playWhenReady = playerWhenRead
         exoPlayer.prepare()
      }else{
         exoPlayer.playWhenReady = playerWhenRead
         exoPlayer.prepare()
      }
      configActualSound()
   }

   private fun configActualSound(){

      val mediaItem   = exoPlayer.currentMediaItem
      if (mediaItem != null) {
         val sound =Sound(
            idSound = null,
            path = "",
            title = mediaItem.mediaMetadata.title.toString(),
            duration = exoPlayer.duration.toString(),
            insertedDate = null
         )
         _actualSound.value =  sound

      }

      exoPlayer.addListener(object : Player.Listener{
         override fun onEvents(player: Player, events: Player.Events) {
            super.onEvents(player, events)
            Log.d("INFO_", "onEvents: ${events}")
         }

         override fun onPlayerError(error: PlaybackException) {
            super.onPlayerError(error)
            Log.e("INFO_", "onPlayerError: ${error.message} :${error.errorCode}")
            when(error.errorCode){
               PlaybackException.ERROR_CODE_PARSING_CONTAINER_UNSUPPORTED -> {
                  _playBackError.value = "Erro ao repoduzir mídia,formato incompatível. "
                  exoPlayer.seekToNext()
                  exoPlayer.playWhenReady
                  exoPlayer.prepare()
                  exoPlayer.play()
               }
            }
            _playBackError.value = null
         }

         override fun onPlayerErrorChanged(error: PlaybackException?) {
            super.onPlayerErrorChanged(error)
            if (error != null) {
               Log.d("INFO_", "onPlayerErrorChanged: ${error.message} :${error.errorCode}")
            }
         }

         override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
            super.onMediaMetadataChanged(mediaMetadata)

            Log.d("INFO_", "onMediaMetadataChanged: ${exoPlayer.duration}")

               val sound =Sound(
                  idSound = null,
                  path = "",
                  title = mediaMetadata.displayTitle.toString(),
                  duration = exoPlayer.duration.toString(),
                  uriMediaAlbum = mediaMetadata.artworkUri,
                  insertedDate = null
               )

               _actualSound.value= sound
               currentItem = exoPlayer.currentMediaItemIndex
               _playlistCurrentlyPlaying?.let {
                  it.copy(currentMusicPosition = exoPlayer.currentMediaItemIndex)
               }

         }

         override fun onIsPlayingChanged(isPlaying: Boolean) {
            super.onIsPlayingChanged(isPlaying)
             isPlayingObserver.value = isPlaying
         }
      })
   }

   private fun createMetaData(sound :Sound): MediaMetadata {

      return MediaMetadata.Builder()
         .setTitle(sound.title)
         .setArtworkUri(sound.uriMediaAlbum)
         .setDisplayTitle(sound.title)
         .build()
   }
   private fun createMediaItemList(list: Set<Sound>):Set<MediaItem>{
      return list.map{ sound ->
          MediaItem.Builder()
            .setMediaMetadata(createMetaData(sound))
            .setUri(sound.path)
            .build()
      }.toSet()
   }
   fun destroyPlayer(){
      exoPlayer.stop()
      exoPlayer.release()
   }
   fun  addItemFromListMusic(soundsToInsertPlayList: Set<Sound>){
      if (_playlistCurrentlyPlaying != null){
         val  updateMediaItem  = mutableListOf<Sound>()
         soundsToInsertPlayList.forEachIndexed{ index, sound ->
            if (!_playlistCurrentlyPlaying?.listSound?.contains(sound)!!){
               updateMediaItem.add(sound)
               _playlistCurrentlyPlaying?.listSound?.add(soundsToInsertPlayList.elementAt(index))
            }
         }
         if (updateMediaItem.isNotEmpty()){
            val mediaItemList = createMediaItemList(updateMediaItem.toSet())
            exoPlayer.addMediaItems(mediaItemList.toList())
         }
      }
   }
    fun removeItemFromListMusic(index : Int){
        val sound = _playlistCurrentlyPlaying?.listSound?.elementAt(index)
       val isRemoved = _playlistCurrentlyPlaying?.listSound?.remove(sound)
        if (isRemoved == true){
            exoPlayer.removeMediaItem(index)
        }
    }
}