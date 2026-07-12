package com.example.soundplayer.presentation.service

import android.app.PendingIntent
import android.content.Intent
import androidx.annotation.OptIn
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.example.soundplayer.presentation.MainActivity
import com.example.soundplayer.service.ServicePlayer
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SoundService : MediaSessionService() {
    @Inject lateinit var servicePlayer: ServicePlayer
    private var mediaSession: MediaSession? = null

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()
        val pendingIntent =
            PendingIntent.getActivity(
                this,
                0,
                Intent(this, MainActivity::class.java),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )

        mediaSession =
            MediaSession
                .Builder(this, servicePlayer.getPlayer())
                .setSessionActivity(pendingIntent)
                .build()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        mediaSession?.let { session ->
            val soundPlayer = session.player
            if (!soundPlayer.playWhenReady ||
                soundPlayer.mediaItemCount == 0 ||
                soundPlayer.playbackState == Player.STATE_ENDED
            ) {
                stopSelf()
            }
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    override fun onDestroy() {
        servicePlayer.destroyPlayer()
        mediaSession?.release()
        mediaSession = null
        super.onDestroy()
    }
}
