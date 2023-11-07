package com.example.soundplayer

import android.media.MediaPlayer
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.soundplayer.model.Sound
import com.example.soundplayer.model.SoundList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class SoundPresenter @Inject constructor(
   private val mediaPlayer: MediaPlayer
) {

    var actualSound  = MutableLiveData<Sound>()

    fun prepareMusicStart(sound:Sound){
        try {

            mediaPlayer.apply {
                reset()
                setDataSource(sound.path)
                prepare()
                start()
            }
         getActualSound(sound)
        }catch (ex:Exception){
            ex.printStackTrace()
        }
    }

    fun getActualSound(sound: Sound){
        actualSound.postValue(sound)
        Log.i("INFO_", "getActualSound live:${actualSound.value!!.title} ")
    }

    fun stopMusic(){
       try {
           mediaPlayer.stop()
           mediaPlayer.release()
       }catch(ex :Exception){
           ex.printStackTrace()
       }
    }

    fun isPlaying():Boolean{
        return mediaPlayer.isPlaying
    }

     fun convertMilesSecondToMinSec(duration: Long): String {
        return String.format(
            "%02d:%02d",
            TimeUnit.MILLISECONDS.toMinutes(duration) % TimeUnit.HOURS.toMinutes(1),
            TimeUnit.MILLISECONDS.toSeconds(duration) % TimeUnit.MINUTES.toSeconds(1)
        )
    }

    fun currentPosition():Long{
      return  mediaPlayer.currentPosition.toLong()
    }

    fun playSound(){
        mediaPlayer.start()
    }
    fun pauseSound(){
        if (mediaPlayer.isPlaying){
            mediaPlayer.pause()
        }
    }

    fun clickUserSeekBarPosition (progress:Int){
        mediaPlayer.seekTo(progress)
    }



}