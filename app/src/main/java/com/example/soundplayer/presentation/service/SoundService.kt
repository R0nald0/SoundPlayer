package com.example.soundplayer.presentation.service


import android.content.Intent
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.example.soundplayer.presentation.SoundViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SoundService : MediaSessionService() {
    @Inject  lateinit var soundViewModel:SoundViewModel
    private var mediaSession :MediaSession? = null

    override fun onCreate() {
        super.onCreate()

        mediaSession =MediaSession.Builder(this,soundViewModel.getPlayer())
            .build()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        if (!soundViewModel.getPlayer().playWhenReady  || soundViewModel.getPlayer().mediaItemCount == 0){
            stopSelf()
        }
    }
    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {

            return  mediaSession
    }


    override fun onDestroy() {
        mediaSession.run {
            soundViewModel.destroyPlayer()
            this?.release()
            mediaSession =null
        }
        super.onDestroy()
    }

}