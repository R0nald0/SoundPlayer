package com.example.soundplayer.data.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.soundplayer.data.database.DatabasePlaylist
import com.example.soundplayer.data.entities.SoundEntity
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.Date


class SoundDaoTest {

    private lateinit var soundDatabase: DatabasePlaylist
    private lateinit var soundDao: SoundDao
    private val soundEntityList = listOf(
        SoundEntity(
            soundId = 1L,
            title = "Song One",
            artistsName = "Artist One",
            albumName = "Album One",
            path = "/music/song_one.mp3",
            duration = "3:45",
            urlMediaImage = "https://example.com/song_one_image.jpg",
            urlAlbumImage = "https://example.com/album_one_image.jpg",
            insertedDate = Date().time
        ),
        SoundEntity(
            soundId = 2L,
            title = "Song Two",
            artistsName = "Artist Two",
            albumName = "Album Two",
            path = "/music/song_two.mp3",
            duration = "4:30",
            urlMediaImage = "https://example.com/song_two_image.jpg",
            urlAlbumImage = "https://example.com/album_two_image.jpg",
            insertedDate = Date().time
        ),
        SoundEntity(
            soundId = 3L,
            title = "Song Three",
            artistsName = "Artist Three",
            albumName = "Album Three",
            path = "/music/song_three.mp3",
            duration = "5:00",
            urlMediaImage = "https://example.com/song_three_image.jpg",
            urlAlbumImage = "https://example.com/album_three_image.jpg",
            insertedDate = Date().time
        ),
        SoundEntity(
            soundId = 4L,
            title = "Song Four",
            artistsName = "Artist Four",
            albumName = "Album Four",
            path = "/music/song_four.mp3",
            duration = "2:50",
            urlMediaImage = "https://example.com/song_four_image.jpg",
            urlAlbumImage = "https://example.com/album_four_image.jpg",
            insertedDate = Date().time
        ),
        SoundEntity(
            soundId = 5L,
            title = "Song Five",
            artistsName = "Artist Five",
            albumName = "Album Five",
            path = "/music/song_five.mp3",
            duration = "3:15",
            urlMediaImage = "https://example.com/song_five_image.jpg",
            urlAlbumImage = "https://example.com/album_five_image.jpg",
            insertedDate = Date().time
        )
    )

    @Before
    fun setUp() {
        soundDatabase = Room.inMemoryDatabaseBuilder(
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

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun findAllSound_should_find_all_sounds() = runTest {
        soundEntityList.forEach { soundDao.saveSound(it) }
        val result = soundDao.findAllSound()
        assertThat(result).isNotEmpty()
        assertThat(result.size).isEqualTo(5)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun findSoundById_should_find_sound_by_id() = runTest {
        soundEntityList.forEach { soundDao.saveSound(it) }

        val result = soundDao.findSoundById(2)
        assertThat(result.title).isEqualTo("Song Two")
        assertThat(result.artistsName).isEqualTo("Artist Two")
        assertThat(result.path).isEqualTo("/music/song_two.mp3")
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun when_findSoundByName_is_executed_it_should_find_sounds_by_name() = runTest {
        soundEntityList.forEach { soundDao.saveSound(it) }

        val result = soundDao.findSoundByName("Song")
        val soundTest  = result.first()
        result.cancellable()
        assertThat(soundTest.title).isEqualTo("Song One")
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun given_a_valid_sound_when_updateSound_is_executed_it_should_update_data_sound() = runTest {
        soundEntityList.forEach { soundDao.saveSound(it) }
        val soundEntityUpdate = SoundEntity(
            soundId = 2,
            title = "Som Atualizado",
            duration = "3:50",
            path = "caminho/sons",
            insertedDate = 323131,
            albumName = "Meu album name",
            urlAlbumImage = "",
            urlMediaImage = "image",
            artistsName = "Sherek"
        )

        soundDao.updateSound(soundEntityUpdate)
        val result = soundDao.findSoundById(2)

        assertThat(result.title).isEqualTo("Som Atualizado")
        assertThat(result.artistsName).isEqualTo("Sherek")
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun given_a_valid_sound_when_deleteSound_is_executed_it_should_delete_sound() = runTest {
        soundEntityList.forEach { soundDao.saveSound(it) }
        val soundEntityToDelete = soundEntityList[3]

        val result = soundDao.deleteSound(soundEntityToDelete)
        assertThat(result).isEqualTo(1)

        val updatedList = soundDao.findAllSound()
        assertThat(updatedList.size).isEqualTo(4)
    }


    @After
    fun tearDown() {
        soundDatabase.close()
    }
}