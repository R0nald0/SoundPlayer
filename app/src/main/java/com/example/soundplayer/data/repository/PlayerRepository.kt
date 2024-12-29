package com.example.soundplayer.data.repository

import android.net.Uri
import androidx.annotation.OptIn
import androidx.lifecycle.MutableLiveData
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.example.soundplayer.commons.execptions.PlayBackErrorException
import com.example.soundplayer.commons.extension.convertMilesSecondToMinSec
import com.example.soundplayer.model.DataSoundPlayListToUpdate
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
   private var _playBackError  = MutableLiveData<PlayBackErrorException?>()
   private  var isPlayingObserver  = MutableLiveData<Boolean>()

   fun getPlayer() = exoPlayer
   fun getActaulSound() = _actualSound
   fun getPlayBackError() = _playBackError
   fun getAcutalPlayList() :PlayList?{
        return  _playlistCurrentlyPlaying
   }
   fun isPlaying()  = isPlayingObserver

   fun getAllMusics(playList: PlayList): PlayList? {

       try {
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
       } catch (e: Exception) {
           _playBackError.value =   PlayBackErrorException(causes = e.cause, code = 0, messages = "erro ao inicializar a playliist")
           return null
       }


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

    fun moveToNexIfErro() {
        if (exoPlayer.hasNextMediaItem()){
            exoPlayer.seekToNext()
            exoPlayer.playWhenReady
            exoPlayer.prepare()
            exoPlayer.play()
        }else{
            exoPlayer.stop()
        }

    }

    private fun configActualSound(){

      val mediaItem   = exoPlayer.currentMediaItem

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


            when(error.errorCode){
               PlaybackException.ERROR_CODE_PARSING_CONTAINER_UNSUPPORTED -> {

                   val mediaMetadata = exoPlayer.mediaMetadata
                   val mediaID = exoPlayer.currentMediaItem?.mediaId
                   val sound =Sound(
                       idSound = mediaID?.toLong(),
                       artistName = mediaMetadata.artist.toString() ,
                       albumName = mediaMetadata.albumTitle.toString() ,
                       path = "",
                       title = mediaMetadata.displayTitle.toString(),
                       duration = exoPlayer.duration.convertMilesSecondToMinSec() ,
                       uriMediaAlbum = mediaMetadata.artworkUri.toString(),
                       insertedDate = null
                   )

                 _playBackError.value = PlayBackErrorException(
                     messages = "Erro ao reproduzir mídia,formato inválido.",
                     causes =error,
                     code = 3003,
                     dataSoundPlayListToUpdate = DataSoundPlayListToUpdate(
                         positionSound = listOf(currentItem),
                         idPlayList = _playlistCurrentlyPlaying?.idPlayList ?: 1,
                         sounds = setOf(sound)
                     )
                  )

                   moveToNexIfErro()
               }

               PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND ->{
                   val mediaMetadata = exoPlayer.mediaMetadata
                   val mediaID = exoPlayer.currentMediaItem?.mediaId

                   val sound =Sound(
                       idSound = mediaID?.toLong(),
                       artistName = mediaMetadata.artist.toString() ,
                       albumName = mediaMetadata.albumTitle.toString() ,
                       path = "",
                       title = mediaMetadata.displayTitle.toString(),
                       duration = exoPlayer.duration.convertMilesSecondToMinSec() ,
                       uriMediaAlbum = mediaMetadata.artworkUri.toString(),
                       insertedDate = null
                   )


                _playBackError.value =  PlayBackErrorException(
                     messages = "Áudio não encontrado.Verifique se o arquivo não foi excluído ou movido." ,
                     causes = error,
                     code = 2005,
                     dataSoundPlayListToUpdate =  DataSoundPlayListToUpdate(
                        positionSound = listOf(currentItem),
                        idPlayList = _playlistCurrentlyPlaying?.idPlayList ?: 1,
                        sounds = setOf(sound)
                    )
                  )

                     moveToNexIfErro()
               }

            }
            _playBackError.value = null

         }

          @OptIn(UnstableApi::class)
          override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
            super.onMediaMetadataChanged(mediaMetadata)

                  val artistName =mediaMetadata.artist ?: "Desconhecido"
                  val albumName =mediaMetadata.albumTitle ?: "Desconhecido"
                  val mediaID = exoPlayer.currentMediaItem?.mediaId

            val sound =Sound(
                  idSound = mediaID?.toLong(),
                  artistName = artistName.toString() ,
                  albumName = albumName.toString(),
                  path = "",
                  title = mediaMetadata.displayTitle.toString(),
                  duration = exoPlayer.duration.convertMilesSecondToMinSec() ,
                  uriMediaAlbum = mediaMetadata.artworkUri.toString(),
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
         .setArtworkUri(Uri.parse(sound.uriMediaAlbum))
         .setDisplayTitle(sound.title)
         .setArtist(sound.artistName)
         .setAlbumTitle(sound.albumName)
         .build()
   }
   private fun createMediaItemList(list: Set<Sound>):Set<MediaItem>{
      return list.map{ sound ->
          MediaItem.Builder()
            .setMediaMetadata(createMetaData(sound))
            .setUri(sound.uriMedia)
            .setMediaId(sound.idSound.toString())
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