package com.example.soundplayer.data.repository

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.exoplayer.ExoPlayer
import com.example.soundplayer.model.PlayList
import com.example.soundplayer.model.Sound
import javax.inject.Inject

class PlayerRepository @Inject constructor(
   private  val exoPlayer: ExoPlayer
) {

   private lateinit var listMediaItem: Set<MediaItem>
   private var currentItem: Int =-1
   private var playerWhenRead = true
   private var playBackPosition: Long = -0L
   private var playlistCurrentlyPlaying :PlayList ? = null
   private var _actualSound  = MutableLiveData<Sound>()
   private  var isPlayingObserver  = MutableLiveData<Boolean>()

    fun getPlayer() = exoPlayer
     fun getActaulSound() =_actualSound
   fun getAcutalPlayList() :PlayList?{
        return  playlistCurrentlyPlaying
   }
   fun isPlaying()  = isPlayingObserver

   fun getAllMusics(playList: PlayList): PlayList? {

      if ( playlistCurrentlyPlaying != null &&  playlistCurrentlyPlaying!!.name != playList.name){
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
         playlistCurrentlyPlaying = playList
         listMediaItem= createMediaItemList(playList.listSound)
         exoPlayer.seekTo(playList.currentMusicPosition,playBackPosition)
         currentItem = playList.currentMusicPosition
         playAllMusicFromFist()
      }
      else{
         playBackPosition =0L
         playlistCurrentlyPlaying = playList
         listMediaItem= createMediaItemList(playList.listSound)
         exoPlayer.seekTo(playList.currentMusicPosition,playBackPosition)
         currentItem = playList.currentMusicPosition
      }

      return  playlistCurrentlyPlaying
   }

   private fun playAllMusicFromFist(){
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
            duration = exoPlayer.duration.toString()
         )
         _actualSound.value =  sound

      }

      exoPlayer.addListener(object : Player.Listener{
         override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
            super.onMediaMetadataChanged(mediaMetadata)

            val sound =Sound(
               idSound = null,
               path = "",
               title = mediaMetadata.displayTitle.toString(),
               duration = exoPlayer.duration.toString(),
               uriMediaAlbum = mediaMetadata.artworkUri
            )
            _actualSound.value= sound
            currentItem = exoPlayer.currentMediaItemIndex
            playlistCurrentlyPlaying?.let {
               it.copy(currentMusicPosition = exoPlayer.currentMediaItemIndex)

            }


         }

         override fun onIsPlayingChanged(isPlaying: Boolean) {
            super.onIsPlayingChanged(isPlaying)
             isPlayingObserver.value = isPlaying
         }

         override fun onTimelineChanged(timeline: Timeline, reason: Int) {

            super.onTimelineChanged(timeline, reason)
            Log.i("INFO_", "onTimelineChanged: $reason : $timeline")

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
       val  updateMediaItem  = mutableListOf<MediaItem>()
       val mediaItemList = createMediaItemList(soundsToInsertPlayList)
          mediaItemList.forEachIndexed{ index, mediaItem ->
          if (!listMediaItem.contains(mediaItem)){
                  updateMediaItem.add(mediaItem)
                  listMediaItem.plus(mediaItem)
                  playlistCurrentlyPlaying?.listSound?.add(soundsToInsertPlayList.elementAt(index))
              }
          }
       if (updateMediaItem.isNotEmpty()){
           exoPlayer.addMediaItems(updateMediaItem)
       }
   }
    fun removeItemFromListMusic(index : Int){
        val sound = playlistCurrentlyPlaying?.listSound?.elementAt(index)
       val isRemoved = playlistCurrentlyPlaying?.listSound?.remove(sound)
        if (isRemoved == true){
            exoPlayer.removeMediaItem(index)
        }
    }
}