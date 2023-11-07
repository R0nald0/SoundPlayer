package com.example.soundplayer

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import com.example.soundplayer.constants.Constants
import com.example.soundplayer.databinding.ActivitySongPlayBinding
import com.example.soundplayer.model.Sound
import com.example.soundplayer.model.SoundList
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class SongPlayActivity (): AppCompatActivity() {
    private  val binding by lazy {
        ActivitySongPlayBinding.inflate(layoutInflater)
    }

    var x = 0.0f
    private lateinit var job: Job

    private lateinit var soundList  : SoundList
    lateinit var  actualSong : Sound
    private  var  currentPosition = 0L

    @Inject lateinit var  soundPresenter : SoundPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        val bundle = intent.extras


        bundle?.let { bundle->

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                soundList  = bundle.getParcelable(Constants.KEY_EXTRA_MUSIC,SoundList::class.java)!!
            } else {
                soundList  = bundle.getParcelable<SoundList>(Constants.KEY_EXTRA_MUSIC)!!
            }

        }

         if (!soundPresenter.isPlaying() ){
             actualSong = soundList.listMusic[soundList.currentMusic]
             initPlayer(actualSong)
         }else if ( soundPresenter.isPlaying() && soundPresenter.actualSound.value?.path == soundList.listMusic[soundList.currentMusic].path){
              actualSong = soundPresenter.actualSound.value!!
              binding.btnPlayPause.setImageResource(R.drawable.ic_pause_24)
              updateViewWhenPlaySong()
         }else{
              actualSong = soundList.listMusic[soundList.currentMusic]
              initPlayer(actualSong)
        }
        initBinding()

    }

    private fun initBinding() {
        binding.txvNameMusic.isSelected =true
       with(binding){

            binding.btnPlayPause.setOnClickListener {
                 if (soundPresenter.isPlaying()) {
                     binding.btnPlayPause.setImageResource(R.drawable.ic_play_arrow_24)
                     soundPresenter.pauseSound()
                 }
                 else {
                     soundPresenter.playSound()
                     binding.btnPlayPause.setImageResource(R.drawable.ic_pause_24)
                     updateViewWhenPlaySong()
                 }
            }

           binding.btnNext.setOnClickListener {
               nextMusic()
           }

           binding.btnPrevious.setOnClickListener {
               previousMusic()
           }
          progresSong.setOnSeekBarChangeListener(object: OnSeekBarChangeListener{
              override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                   if (fromUser){
                       soundPresenter.clickUserSeekBarPosition(progress)
                   }
              }

              override fun onStartTrackingTouch(seekBar: SeekBar?) {

              }

              override fun onStopTrackingTouch(seekBar: SeekBar?) {

              }

          })
       }
    }

    private fun updateViewWhenPlaySong(){
      job = CoroutineScope(Dispatchers.IO).launch{
          while (soundPresenter.isPlaying()){

              currentPosition    = soundPresenter.currentPosition()
              val timeCurrent    = soundPresenter.convertMilesSecondToMinSec(currentPosition)
              val totalMusicTime = soundPresenter.convertMilesSecondToMinSec(duration = actualSong.duration.toLong())

                x++

              withContext(Dispatchers.Main){
                  binding.txvNameMusic.text =actualSong.title
                  binding.txvTotalTime.text= totalMusicTime
                  binding.tvcCurrentTime.text = timeCurrent
                  binding.imvSong.rotation = x
                  binding.progresSong.max = actualSong.duration.toInt()
                  binding.progresSong.progress = currentPosition.toInt()
              }

              if (timeCurrent == totalMusicTime){
                  nextMusic()
                  x= 0.0F
              }
          }
      }
  }


    private fun initPlayer(sound: Sound){
      try {
          soundPresenter.prepareMusicStart(sound)
          binding.btnPlayPause.setImageResource(R.drawable.ic_pause_24)
          updateViewWhenPlaySong()
      }catch (ex:Exception){
          ex.printStackTrace()
      }
    }
    private fun nextMusic(){

        if (soundList.currentMusic == soundList.listMusic.lastIndex)soundList.currentMusic = 0
        else soundList.currentMusic++
        actualSong = soundList.listMusic[soundList.currentMusic]
             initPlayer(actualSong)
    }

    private fun previousMusic(){
         try {
             if (soundList.currentMusic == 0)soundList.currentMusic = soundList.listMusic.lastIndex
             else soundList.currentMusic--

             actualSong = soundList.listMusic[soundList.currentMusic]
             initPlayer(actualSong)
         }catch (ex:Exception){
             ex.printStackTrace()
             throw ex
         }
    }

    private fun stopMusic(){
        soundPresenter.stopMusic()
    }

    override fun onDestroy() {
        x= 0.0F
        super.onDestroy()
    }
}