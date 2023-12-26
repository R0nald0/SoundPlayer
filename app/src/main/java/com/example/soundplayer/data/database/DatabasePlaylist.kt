package com.example.soundplayer.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.soundplayer.commons.constants.Constants
import com.example.soundplayer.data.dao.PlayListDAO
import com.example.soundplayer.data.dao.PlaylistAndSoundCrossDao
import com.example.soundplayer.data.dao.SoundDao
import com.example.soundplayer.data.entities.PlayListAndSoundCrossEntity
import com.example.soundplayer.data.entities.PlayListEntity
import com.example.soundplayer.data.entities.SoundEntity


@Database(
    entities = [
         PlayListEntity::class,
         SoundEntity::class,
         PlayListAndSoundCrossEntity::class],
    version = 1
)
abstract class DatabasePlaylist : RoomDatabase() {
   abstract  fun playlistDao():PlayListDAO
   abstract  fun soundDao(): SoundDao
   abstract fun playListAndSoundCrossDao():PlaylistAndSoundCrossDao

   companion object {
       fun getInstance(context: Context): DatabasePlaylist {
           return Room.databaseBuilder(
               context,
                DatabasePlaylist::class.java,
               Constants.DATABASE_NAME
           ).build()
       }
   }
}