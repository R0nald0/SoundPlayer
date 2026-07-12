package com.example.soundplayer.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
        PlayListAndSoundCrossEntity::class,
    ],
    version = 2,
    exportSchema = false,
)
abstract class DatabasePlaylist : RoomDatabase() {
    abstract fun playlistDao(): PlayListDAO

    abstract fun soundDao(): SoundDao

    abstract fun playListAndSoundCrossDao(): PlaylistAndSoundCrossDao

    companion object {
        internal val MIGRATION_1_2 =
            object : Migration(1, 2) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL(
                        "CREATE INDEX IF NOT EXISTS `index_PlayListAndSoundCrossEntity_soundId` " +
                            "ON `PlayListAndSoundCrossEntity` (`soundId`)",
                    )
                }
            }

        fun getInstance(context: Context): DatabasePlaylist =
            Room
                .databaseBuilder(
                    context,
                    DatabasePlaylist::class.java,
                    Constants.DATABASE_NAME,
                ).addMigrations(MIGRATION_1_2)
                .build()
    }
}
