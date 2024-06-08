package com.example.soundplayer.presentation.service


import android.content.Intent
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.example.soundplayer.service.ServicePlayer
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SoundService : MediaSessionService() {
    @Inject  lateinit var servicePlayer: ServicePlayer
    private var mediaSession :MediaSession? = null

    override fun onCreate() {
        super.onCreate()

        mediaSession =MediaSession.Builder(this,servicePlayer.getPlayer())
            .build()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        if (!servicePlayer.getPlayer().playWhenReady  || servicePlayer.getPlayer().mediaItemCount == 0){
            stopSelf()
        }
    }
    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {

            return  mediaSession
    }


    override fun onDestroy() {
        mediaSession.run {
            servicePlayer.destroyPlayer()
            this?.release()
            mediaSession =null
        }
        super.onDestroy()
    }

}