package com.example.soundplayer.presentation.service


import android.content.Intent
import androidx.annotation.OptIn
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.navigation.NavDeepLinkBuilder
import com.example.soundplayer.R
import com.example.soundplayer.presentation.MainActivity
import com.example.soundplayer.service.ServicePlayer
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SoundService : MediaSessionService() {
    @Inject  lateinit var servicePlayer: ServicePlayer
    private var mediaSession :MediaSession? = null

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()
        val pendingIntent = NavDeepLinkBuilder(this)
            .setGraph(R.navigation.sound_navigation_graph)
            .setDestination(R.id.soundPlayingFragment)
            .setComponentName(MainActivity::class.java)
            .createPendingIntent()

        mediaSession =MediaSession.Builder(this,servicePlayer.getPlayer())
            .setSessionActivity(pendingIntent)
            .build()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        if (mediaSession != null){
            val soundPlayer = mediaSession!!.player
            if (!soundPlayer.playWhenReady
                ||soundPlayer.mediaItemCount == 0
                || soundPlayer.playbackState  == Player.STATE_ENDED
            ){
                stopSelf()
            }
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
