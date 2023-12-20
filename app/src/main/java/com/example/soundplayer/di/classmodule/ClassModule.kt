package com.example.soundplayer.di.classmodule

import android.content.Context
import androidx.media3.exoplayer.ExoPlayer
import com.example.soundplayer.data.dao.PlayListDAO
import com.example.soundplayer.data.dao.PlaylistAndSoundCrossDao
import com.example.soundplayer.data.dao.SoundDao
import com.example.soundplayer.data.database.DatabasePlaylist
import com.example.soundplayer.data.repository.SoundPlayListRepository
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
    fun provideActualSong(exoPlayer: ExoPlayer): SoundPresenter {
        return  SoundPresenter(exoPlayer)
    }

    @Singleton
    @Provides
    fun provideExoPlayer(@ApplicationContext context :Context):ExoPlayer{
        return  ExoPlayer.Builder(context)
            .build()
    }

    @Provides
    fun providePlayListDao(databasePlaylist: DatabasePlaylist):PlayListDAO{
        return databasePlaylist.playlistDao()
    }
    @Provides
    fun soundDao(databasePlaylist: DatabasePlaylist):SoundDao{
        return databasePlaylist.soundDao()
    }

    @Provides
    fun providePlaylistAndSoundCross(databasePlaylist: DatabasePlaylist):PlaylistAndSoundCrossDao{
        return  databasePlaylist.playListAndSoundCrossDao();
    }
    @Provides
    fun provideSoundPlayListRepository(playListDAO: PlayListDAO ,playlistAndSoundCross: PlaylistAndSoundCrossDao,soundDao: SoundDao):SoundPlayListRepository{
         return  SoundPlayListRepository(playListDAO, playlistAndSoundCross,soundDao)
    }

    @Provides
    fun provideRoomDatabase(@ApplicationContext context: Context):DatabasePlaylist{
            return  DatabasePlaylist.getInstance(context)
    }
}