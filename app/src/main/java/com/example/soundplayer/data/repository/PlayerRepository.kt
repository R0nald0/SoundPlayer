package com.example.soundplayer.data.repository

import android.util.Log
import androidx.annotation.OptIn
import androidx.lifecycle.MutableLiveData
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.extractor.DefaultExtractorsFactory
import androidx.media3.extractor.ts.TsExtractor
import com.example.soundplayer.commons.execptions.Failure
import com.example.soundplayer.commons.extension.convertMilesSecondToMinSec
import com.example.soundplayer.model.PlayList
import com.example.soundplayer.model.Sound
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject

class PlayerRepository @Inject constructor(
   private  val exoPlayer: ExoPlayer
) {
   private var currentItem: Int =-1
   private var playerWhenRead = true
   private var playBackPosition: Long = -0L
   private var _playlistCurrentlyPlaying :PlayList ? = null
   private var _actualSound  = MutableLiveData<Sound>()
   private var _playBackError  = MutableLiveData<Failure?>()
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
         playAllMusicFromFist(playList.listSound)
      }
      else{
         playBackPosition =0L
         _playlistCurrentlyPlaying = playList
         exoPlayer.seekTo(playList.currentMusicPosition,playBackPosition)
         currentItem = playList.currentMusicPosition
      }


      return  _playlistCurrentlyPlaying
   }

    fun playAllMusicFromFist( sounds: Set<Sound>){
      val listMediaItem =  createMediaItemList(sounds)
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
      Log.i("INFO_", "configActualSound: ${mediaItem?.mediaId}")
      if (mediaItem != null) {
         val sound =Sound(
            idSound = null,
            albumName = mediaItem.mediaMetadata.albumTitle.toString(),
            artistName = mediaItem.mediaMetadata.artist.toString(),
            path = "",
            title = mediaItem.mediaMetadata.title.toString(),
            duration = exoPlayer.duration.convertMilesSecondToMinSec(),
            insertedDate = null
         )

         _actualSound.value =  sound
      }

      exoPlayer.addListener(object : Player.Listener{
         override fun onEvents(player: Player, events: Player.Events) {
            super.onEvents(player, events)

         }

         override fun onPlayerError(error: PlaybackException) {
            super.onPlayerError(error)
            Log.e("INFO_", "onPlayerError: ${error.message} :${error.errorCode}")
            when(error.errorCode){
               PlaybackException.ERROR_CODE_PARSING_CONTAINER_UNSUPPORTED -> {
                 _playBackError.value = Failure(
                     messages = "Erro ao reproduzir mídia,formato inválido.",
                     causes =error,
                     code = 3003
                  )
                  moveToNexIfErro()
               }

               PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND ->{
                _playBackError.value =  Failure(
                     messages = "Audio nao encontrado,verifique se o arquivo não foi deletado" ,
                     causes = error,
                     code = 2005
                  )
                  moveToNexIfErro()
               }
            }
            _playBackError.value = null
         }

         private fun moveToNexIfErro() {
            exoPlayer.seekToNext()
            exoPlayer.playWhenReady
            exoPlayer.prepare()
            exoPlayer.play()
         }


          @OptIn(UnstableApi::class)
          override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
            super.onMediaMetadataChanged(mediaMetadata)


                 if (exoPlayer.duration == C.TIME_UNSET){

                }

                  val artistName =mediaMetadata.artist ?: "Desconhecido"
                  val albumName =mediaMetadata.albumTitle ?: "Desconhecido"

            val sound =Sound(
                  idSound = null,
                  artistName = artistName.toString() ,
                  albumName = albumName.toString(),
                  path = "",
                  title = mediaMetadata.displayTitle.toString(),
                  duration = exoPlayer.duration.convertMilesSecondToMinSec() ,
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