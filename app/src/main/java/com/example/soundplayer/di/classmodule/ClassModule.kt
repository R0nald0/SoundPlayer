package com.example.soundplayer.di.classmodule

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.media3.exoplayer.ExoPlayer
import com.example.soundplayer.commons.constants.Constants
import com.example.soundplayer.data.dao.PlayListDAO
import com.example.soundplayer.data.dao.PlaylistAndSoundCrossDao
import com.example.soundplayer.data.dao.SoundDao
import com.example.soundplayer.data.database.DatabasePlaylist
import com.example.soundplayer.data.repository.DataStorePreferenceRepository
import com.example.soundplayer.data.repository.PlayerRepository
import com.example.soundplayer.data.repository.SoundPlayListRepository
import com.example.soundplayer.data.repository.SoundRepository
import com.example.soundplayer.service.ServicePlayer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class ClassModule {
    @Singleton
    @Provides
    fun provideServicePlayer(
        playerRepository: PlayerRepository,
        soundRepository: SoundRepository,
        soundPlayListRepository: SoundPlayListRepository
    ) :ServicePlayer{
        return  ServicePlayer(playerRepository,soundPlayListRepository,soundRepository)
    }
    @Singleton
    @Provides
    fun providePlayerRepository(exoPlayer: ExoPlayer):PlayerRepository = PlayerRepository(exoPlayer)

    @Singleton
    @Provides
    fun provideDataStore( @ApplicationContext context :Context) :DataStore<Preferences> {
        return  PreferenceDataStoreFactory.create(
                corruptionHandler = ReplaceFileCorruptionHandler(
                    produceNewData = { emptyPreferences() }
                ),
                scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
                produceFile = { context.preferencesDataStoreFile(Constants.PREFERENCE_NAME) }
            )
    }

    @Provides
    fun provideDataStorePreferenceRespository(dataStore: DataStore<Preferences>):DataStorePreferenceRepository{
          return  DataStorePreferenceRepository(dataStore)
    }

    @Singleton
    @Provides
    fun provideExoPlayer(@ApplicationContext context : Context):ExoPlayer{
        return  ExoPlayer.Builder(context)
            .build()
    }

    @Provides
    fun providePlayListDao(databasePlaylist: DatabasePlaylist):PlayListDAO{
        return databasePlaylist.playlistDao()
    }
    @Provides
    fun provideSoundDao(databasePlaylist: DatabasePlaylist):SoundDao{
        return databasePlaylist.soundDao()
    }

    @Provides
    fun providePlaylistAndSoundCross(databasePlaylist: DatabasePlaylist):PlaylistAndSoundCrossDao{
        return  databasePlaylist.playListAndSoundCrossDao();
    }

    @Singleton
    @Provides
    fun provideSoundPlayListRepository(
        playListDAO: PlayListDAO ,
        playlistAndSoundCross : PlaylistAndSoundCrossDao
    ):SoundPlayListRepository{
         return  SoundPlayListRepository(playListDAO, playlistAndSoundCross)
    }

    @Singleton
    @Provides
    fun provideSoundRepository(soundDao: SoundDao):SoundRepository = SoundRepository(soundDao)

    @Singleton
    @Provides
    fun provideRoomDatabase(@ApplicationContext context: Context):DatabasePlaylist = DatabasePlaylist.getInstance(context)
}