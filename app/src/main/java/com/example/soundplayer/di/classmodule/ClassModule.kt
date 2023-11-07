package com.example.soundplayer.di.classmodule

import android.media.MediaPlayer
import com.example.soundplayer.SoundPresenter
import com.example.soundplayer.model.Sound
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.time.Duration
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class ClassModule {
    @Singleton
    @Provides
    fun provideActualSong(mediaPlayer: MediaPlayer):SoundPresenter{
        return  SoundPresenter(mediaPlayer)
    }


    @Singleton
    @Provides
    fun provideMediaPlayer():MediaPlayer{
          return  MediaPlayer()
    }
}