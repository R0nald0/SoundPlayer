package com.example.soundplayer.presentation.viewmodel


import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.soundplayer.data.entities.UserDataPreferecence
import com.example.soundplayer.data.repository.DataStorePreferenceRepository
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
    private  val dataStorePreferenceRepository: DataStorePreferenceRepository
) :ViewModel(){

     private var playerWhenRead = true
     var currentItem = 0
     private var playBackPosition = 0L

    var actualSound  = MutableLiveData<Sound>()
    var isPlayingObserver  = MutableLiveData<Boolean>()

    private var _currentPlayList = MutableLiveData<PlayList>()
    val currentPlayList :LiveData<PlayList>
           get() = _currentPlayList

    private val _userDataPreferecenceObs = MutableLiveData<UserDataPreferecence>()
    val userDataPreferecence : LiveData<UserDataPreferecence>
        get() = _userDataPreferecenceObs


    private lateinit var listMediaItem  : List<MediaItem>

     fun getPlayer():ExoPlayer{
       return exoPlayer
     }

    fun updatePlayList(listSound: MutableSet<Sound>){
        if (currentPlayList.value != null){
           val retorno  = addItemFromListMusic(listSound);
             if (retorno.isEmpty()){
                 removeItemFromListMusic(listSound)
             }
            Log.i("INFO_", "countPlayList: ${exoPlayer.mediaItemCount}")
        }
    }

    fun addItemFromListMusic(listSound:MutableSet<Sound>):MutableSet<Sound>{
        val listToUpdate = mutableSetOf<Sound>()
        if (currentPlayList.value!!.listSound != listSound){
            listSound.forEachIndexed {index , sound->
                if (listSound.contains(sound) && !currentPlayList.value!!.listSound.contains(sound)) {
                    listToUpdate.add(sound)
                    exoPlayer.addMediaItem(
                        index,
                        MediaItem.Builder()
                            .setMediaMetadata(createMetaData(sound))
                            .setUri(sound.path)
                            .build()
                    )

                }else if(listSound.contains(sound) && currentPlayList.value!!.listSound.contains(sound)){}
            }
        }
        return listToUpdate;
    }
    fun removeItemFromListMusic(listSound:MutableSet<Sound>):MutableSet<Sound>{
        val modifaildField = mutableSetOf<Sound>()
        if (currentPlayList.value!!.listSound.isNotEmpty() && listSound.isNotEmpty()){
            currentPlayList.value!!.listSound.forEachIndexed { index, sound ->
                if (!listSound.contains(sound)) {
                    exoPlayer.removeMediaItem(index);
                    modifaildField.add(sound)
                }
                Log.i("INFO_", "getMediaItemAt: ${exoPlayer.getMediaItemAt(index)}")
            }
        }

        return modifaildField
    }
    fun getAllMusics(playList: PlayList) {

           if (_currentPlayList.value != null &&  _currentPlayList.value!!.name != playList.name){
               exoPlayer.stop()
               exoPlayer.clearMediaItems()
            }

            if (!exoPlayer.isPlaying || playList.currentMusicPosition !=currentItem ){
                _currentPlayList.value = playList
                listMediaItem= createMediaItemList(playList.listSound)

                exoPlayer.seekTo(playList.currentMusicPosition,playBackPosition)
                currentItem = playList.currentMusicPosition

            }
    }

    private fun createMediaItemList(list: MutableSet<Sound>):List<MediaItem>{
        return list.map{ sound ->
             MediaItem.Builder()
                 .setMediaMetadata(createMetaData(sound))
                 .setUri(sound.path)
                 .build()
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
        if (exoPlayer.mediaItemCount == 0){
            exoPlayer.addMediaItems(listMediaItem)
            exoPlayer.playWhenReady = playerWhenRead
            exoPlayer.prepare()
        }
        configActualSound()
    }
    fun destroyPlayer(){
        exoPlayer.stop()
        exoPlayer.release()
    }

 suspend  fun savePreference(){

            runCatching {
                if(_currentPlayList.value != null){
                    dataStorePreferenceRepository
                        .savePreference(_currentPlayList.value!!.idPlayList, positionSoundKey = currentItem)
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