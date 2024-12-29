package com.example.soundplayer.data.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.soundplayer.data.database.DatabasePlaylist
import com.example.soundplayer.data.entities.SoundEntity
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test


class SoundDaoTest {

    private lateinit var soundDatabase : DatabasePlaylist
    lateinit var soundDao: SoundDao

    @Before
    fun setUp() {
        soundDatabase =Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            DatabasePlaylist::class.java,
         ).allowMainThreadQueries().build()

        soundDao = soundDatabase.soundDao()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun saveSound_Shoudld_Save_a_sound_and_return_id() = runTest {
        val soundEntity = SoundEntity(
            insertedDate = 133132,
            path = "",
            title = "Minha primeira música",
            duration = "3232",
            albumName = "",
            soundId = 1,
            urlAlbumImage = "",
            urlMediaImage = "",
            artistsName = ""
        )
         val result = soundDao.saveSound(soundEntity)

        assertThat(result).isEqualTo(1)

    }

    @After
    fun tearDown() {
        soundDatabase.close()
    }
}