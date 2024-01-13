package com.example.soundplayer.presentation

import android.content.ComponentName
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.soundplayer.R
import com.example.soundplayer.databinding.ActivitySongPlayBinding
import com.example.soundplayer.service.SoundService
import com.google.common.util.concurrent.MoreExecutors
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.log

@AndroidEntryPoint
class SongPlayActivity (): AppCompatActivity() {

    private  val binding by lazy {
        ActivitySongPlayBinding.inflate(layoutInflater)
    }

    @Inject lateinit var  soundViewModel : SoundViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initPlayer()
        observer()
    }

    private fun observer() {
       soundViewModel.actualSound.observe(this) { sound ->
           CoroutineScope(Dispatchers.Main).launch {
              // binding.txvNameMusic.isSelected = true
               binding.txvNameMusic.text = sound.title

               if (sound.uriMediaAlbum != null){
                   binding.imvSong.setImageURI(sound.uriMediaAlbum)
                   if (binding.imvSong.drawable == null){
                       binding.imvSong.setImageResource(R.drawable.transferir)
                   }
               }

           }
       }
   }


    @OptIn(UnstableApi::class)
    private fun  initPlayer(){
         soundViewModel.getPlayer().let { exoPlayer ->
             binding.myPlayerView.player = exoPlayer
             soundViewModel.playAllMusicFromFist()
         }
    }

    @OptIn(UnstableApi::class) override fun onStart() {
        super.onStart()
        if (Util.SDK_INT > 23){
            initPlayer()
        }
        val sessonToken = SessionToken(this, ComponentName(this,SoundService::class.java))
        val controllerAsync =MediaController.Builder(this,sessonToken).buildAsync()
        controllerAsync.addListener({
            binding.myPlayerView.player = controllerAsync.get()
        },MoreExecutors.directExecutor())
    }

    @OptIn(UnstableApi::class) override fun onPause() {
        super.onPause()
        if (Util.SDK_INT <= 23){
           releasePlayer()
        }

    }

    @OptIn(UnstableApi::class) override fun onResume() {
        super.onResume()
       // hideUi()
        if (Util.SDK_INT <= 23){
            initPlayer()
        }

    }

    @OptIn(UnstableApi::class) override fun onStop() {
        super.onStop()
        if (Util.SDK_INT > 23){
            //releasePlayer()
        }

    }

    private fun releasePlayer() {
//       soundPresenter.getPlayer().let { exoPlayer ->
//           soundPresenter.playerWhenRead = exoPlayer.playWhenReady
//           soundPresenter.currentItem = exoPlayer.currentMediaItemIndex
//           soundPresenter.playBackPosition = exoPlayer.currentPosition
//           exoPlayer.release()
//       }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun hideUi(){
        WindowCompat.setDecorFitsSystemWindows(window,false)
        WindowInsetsControllerCompat(window,binding.myPlayerView).let {controller->
            controller.hide(WindowInsetsCompat.Type.statusBars())
            controller.systemBarsBehavior= WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        }
    }

}