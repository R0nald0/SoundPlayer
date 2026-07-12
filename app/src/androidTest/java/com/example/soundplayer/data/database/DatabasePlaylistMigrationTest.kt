package com.example.soundplayer.data.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

class DatabasePlaylistMigrationTest {
    private lateinit var context: Context
    private lateinit var databaseName: String

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        databaseName = "playlist-migration-${System.nanoTime()}.db"
        context.deleteDatabase(databaseName)
    }

    @After
    fun tearDown() {
        context.deleteDatabase(databaseName)
    }

    @Test
    fun migration1To2PreservesPlaylistDataAndCreatesSoundIndex() =
        runTest {
            createVersion1Database()

            val database =
                Room
                    .databaseBuilder(context, DatabasePlaylist::class.java, databaseName)
                    .addMigrations(DatabasePlaylist.MIGRATION_1_2)
                    .build()

            val migratedDatabase = database.openHelper.writableDatabase
            val indexes = migratedDatabase.query("PRAGMA index_list(`PlayListAndSoundCrossEntity`)")
            val indexNames = mutableListOf<String>()
            indexes.use { cursor ->
                while (cursor.moveToNext()) {
                    indexNames += cursor.getString(cursor.getColumnIndexOrThrow("name"))
                }
            }

            val playlist = database.playlistDao().findPlayListById(1)

            assertThat(indexNames).contains("index_PlayListAndSoundCrossEntity_soundId")
            assertThat(playlist.playList.title).isEqualTo("Todas as musicas")
            assertThat(playlist.soundOfPlayList).hasSize(1)
            assertThat(playlist.soundOfPlayList.first().title).isEqualTo("Yellow")

            database.close()
        }

    private fun createVersion1Database() {
        val database = context.openOrCreateDatabase(databaseName, Context.MODE_PRIVATE, null)
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `playList` (
                `playListId` INTEGER PRIMARY KEY AUTOINCREMENT,
                `current_sound_position` INTEGER NOT NULL,
                `title` TEXT NOT NULL
            )
            """.trimIndent(),
        )
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `sound` (
                `soundId` INTEGER NOT NULL,
                `title` TEXT NOT NULL,
                `artistsName` TEXT NOT NULL,
                `albumName` TEXT NOT NULL,
                `path` TEXT NOT NULL,
                `duration` TEXT NOT NULL,
                `urlMediaImage` TEXT NOT NULL,
                `urlAlbumImage` TEXT NOT NULL,
                `insertedDate` INTEGER NOT NULL,
                PRIMARY KEY(`soundId`)
            )
            """.trimIndent(),
        )
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `PlayListAndSoundCrossEntity` (
                `playListId` INTEGER NOT NULL,
                `soundId` INTEGER NOT NULL,
                PRIMARY KEY(`playListId`, `soundId`)
            )
            """.trimIndent(),
        )
        database.execSQL(
            """
            INSERT INTO `playList` (`playListId`, `current_sound_position`, `title`)
            VALUES (1, 0, 'Todas as musicas')
            """.trimIndent(),
        )
        database.execSQL(
            """
            INSERT INTO `sound` (
                `soundId`,
                `title`,
                `artistsName`,
                `albumName`,
                `path`,
                `duration`,
                `urlMediaImage`,
                `urlAlbumImage`,
                `insertedDate`
            )
            VALUES (10, 'Yellow', 'Coldplay', 'Acoustic', '/storage/yellow.mp3', '253000', '', '', 1)
            """.trimIndent(),
        )
        database.execSQL(
            """
            INSERT INTO `PlayListAndSoundCrossEntity` (`playListId`, `soundId`)
            VALUES (1, 10)
            """.trimIndent(),
        )
        database.version = 1
        database.close()
    }
}
