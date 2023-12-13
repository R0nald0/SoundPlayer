package com.example.soundplayer.service


import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SoundService : MediaSessionService() {
    @Inject  lateinit var  exoPlayer :ExoPlayer
    private var mediaSession :MediaSession? = null

    override fun onCreate() {
        super.onCreate()
        mediaSession =MediaSession.Builder(this,exoPlayer).build()
    }
    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
            return  mediaSession
    }


    override fun onDestroy() {
        mediaSession.run {
            exoPlayer.release()
            this?.release()
            mediaSession =null
        }
        super.onDestroy()
    }

}