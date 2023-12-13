package com.example.soundplayer.di.classmodule

import android.content.Context
import android.media.MediaPlayer
import androidx.media3.exoplayer.ExoPlayer
import com.example.soundplayer.presentation.SoundPresenter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class ClassModule {
    @Singleton
    @Provides
    fun provideActualSong(mediaPlayer: MediaPlayer,exoPlayer: ExoPlayer): SoundPresenter {
        return  SoundPresenter(mediaPlayer,exoPlayer)
    }

    @Singleton
    @Provides
    fun provideExoPlayer(@ApplicationContext context :Context):ExoPlayer{
        return  ExoPlayer.Builder(context)
            .build()
    }

    @Singleton
    @Provides
    fun provideMediaPlayer():MediaPlayer{
          return  MediaPlayer()
    }
}