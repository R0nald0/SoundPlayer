package com.example.soundplayer.presentation.viewmodel


import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.exoplayer.ExoPlayer
import com.example.soundplayer.data.entities.UserDataPreferecence
import com.example.soundplayer.data.repository.DataStorePreferenceRepository
import com.example.soundplayer.data.repository.SoundPlayListRepository
import com.example.soundplayer.model.DataSoundPlayListToUpdate
import com.example.soundplayer.model.PlayList
import com.example.soundplayer.model.Sound
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SoundViewModel @Inject constructor(
    private val exoPlayer: ExoPlayer,
    private  val dataStorePreferenceRepository: DataStorePreferenceRepository,
    private val soundPlayListRepository: SoundPlayListRepository
) :ViewModel(){


     private var playerWhenRead = true
     var currentItem = -1
     private var playBackPosition = -0L

    var actualSound  = MutableLiveData<Sound>()
    var isPlayingObserver  = MutableLiveData<Boolean>()

    private var _currentPlayingPlayList = MutableLiveData<PlayList>()
    val currentPlayList :LiveData<PlayList>
           get() = _currentPlayingPlayList

    private val _userDataPreferecenceObs = MutableLiveData<UserDataPreferecence>()
    val userDataPreferecence : LiveData<UserDataPreferecence>
        get() = _userDataPreferecenceObs



    private lateinit var listMediaItem  : MutableList<MediaItem>

     fun getPlayer():ExoPlayer{
       return exoPlayer
     }

     fun updatePlayList( pairListSounWithDecisionUpdate :Pair<Boolean, DataSoundPlayListToUpdate>){
        if (currentPlayList.value != null){
            if (pairListSounWithDecisionUpdate.first)
                addItemFromListMusic(pairListSounWithDecisionUpdate.second!!)
            else removeItemFromListMusic(pairListSounWithDecisionUpdate.second!!)
        }

    }

    private fun addItemFromListMusic(dataSoundPlayListToUpdate: DataSoundPlayListToUpdate){
        if (_currentPlayingPlayList.value?.idPlayList == dataSoundPlayListToUpdate.idPlayList){
             listMediaItem.clear()
             listMediaItem.addAll(createMediaItemList(dataSoundPlayListToUpdate.sounds.toMutableSet()))
             listMediaItem.forEachIndexed { index,sound ->
                 exoPlayer.addMediaItem(
                     dataSoundPlayListToUpdate.positionSound[index],
                     sound
                     )
             }
        }
    }

     private fun removeItemFromListMusic(soundPlay: DataSoundPlayListToUpdate){

          if (_currentPlayingPlayList.value?.idPlayList == soundPlay.idPlayList){

              exoPlayer.removeMediaItem(soundPlay.positionSound.first())
              _currentPlayingPlayList.value?.listSound?.remove(soundPlay.sounds.first())
              Log.i("INFO_", "size list: ${listMediaItem.size}")
          }
    }

    fun getAllMusics(playList: PlayList) {

           if ( _currentPlayingPlayList.value != null &&  _currentPlayingPlayList.value!!.name != playList.name){
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
                _currentPlayingPlayList.value = playList
                listMediaItem= createMediaItemList(playList.listSound).toMutableList()
                exoPlayer.seekTo(playList.currentMusicPosition,playBackPosition)
                currentItem = playList.currentMusicPosition
                playAllMusicFromFist()

            }
           else{
                playBackPosition =0L
                _currentPlayingPlayList.value = playList
                listMediaItem= createMediaItemList(playList.listSound).toMutableList()
                exoPlayer.seekTo(playList.currentMusicPosition,playBackPosition)
                currentItem = playList.currentMusicPosition
           }

    }



    private fun createMediaItemList(list: MutableSet<Sound>):MutableSet<MediaItem>{
        return list.map{ sound ->
             MediaItem.Builder()
                 .setMediaMetadata(createMetaData(sound))
                 .setUri(sound.path)
                 .build()
         }.toMutableSet()
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
                actualSound.value =  sound

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
                    currentItem = exoPlayer.currentMediaItemIndex
                Log.i("INFO_", "Sound index: ${exoPlayer.currentMediaItemIndex}")

                currentPlayList.value?.listSound?.forEachIndexed { index, sound ->
// //            Log.i("INFO_", "Sound At Playlist:${index} ${exoPlayer.getMediaItemAt(index).mediaMetadata.title}")
//
//
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
    fun destroyPlayer(){
        exoPlayer.stop()
        exoPlayer.release()
    }

   suspend fun savePreference(){

            runCatching {
                if(_currentPlayingPlayList.value != null){
                    dataStorePreferenceRepository
                        .savePreference(_currentPlayingPlayList.value!!.idPlayList, positionSoundKey = currentItem)
                }
            }.fold(
                onSuccess = {
                    withContext(Dispatchers.Main){
                        readPreferences()
                    }
                },
                onFailure = {
                    Log.i("INFO_", "savePreference: erro ao salvar preferencias ${it.message}")
                }
            )

    }
   suspend  fun readPreferences(){

             runCatching {
                dataStorePreferenceRepository.readAllPreferecenceData()
             }.fold(
                 onSuccess = {readAllPreferecenceData->

                         withContext(Dispatchers.Main){
                             _userDataPreferecenceObs.value = readAllPreferecenceData
                                 ?: UserDataPreferecence(idPreference = 1, postionPreference = 1
                             )
                         }


                 },
                 onFailure = {
                     Log.i("Play_", "readPreferences: erro ao ler dados da store : ${it.message}")
                 }
             )

    }

}