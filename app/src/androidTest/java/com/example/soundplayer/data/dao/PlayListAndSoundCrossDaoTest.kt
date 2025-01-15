package com.example.soundplayer.data.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.soundplayer.data.database.DatabasePlaylist
import com.example.soundplayer.data.entities.PlayListAndSoundCrossEntity
import com.example.soundplayer.data.entities.PlayListEntity
import com.example.soundplayer.data.entities.SoundEntity
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test


class PlayListAndSoundCrossDaoTest {

    private lateinit var soundDatabase: DatabasePlaylist
    private lateinit var playlistAndSoundCrossDao: PlaylistAndSoundCrossDao
    private lateinit var playListDAO: PlayListDAO
    private lateinit var soundDao: SoundDao


    @Before
    fun setUp() {
        soundDatabase = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            DatabasePlaylist::class.java,
        ).allowMainThreadQueries()
            .build()

        playlistAndSoundCrossDao = soundDatabase.playListAndSoundCrossDao()
        playListDAO = soundDatabase.playlistDao()
        soundDao = soundDatabase.soundDao()
    }

    private val playListAndSoundCrossEntities = listOf(
        PlayListAndSoundCrossEntity(playListId = 1L, soundId = 1L),
        PlayListAndSoundCrossEntity(playListId = 3L, soundId = 2L),
        PlayListAndSoundCrossEntity(playListId = 2L, soundId = 3L),
        PlayListAndSoundCrossEntity(playListId = 2L, soundId = 4L),
        PlayListAndSoundCrossEntity(playListId = 3L, soundId = 7L)
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun insertPlayListAndSoundCroos_save_PlayListCrossSound_on_database() = runTest {
        val listLongResultPlayListXSound =
            playlistAndSoundCrossDao.insertPlayListAndSoundCroos(playListAndSoundCrossEntities)

        assertThat(listLongResultPlayListXSound).isNotEmpty()
        assertThat(listLongResultPlayListXSound.size).isEqualTo(5)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun findAllPlayListWithSong_should_return_a_PlayListWithSong() = runTest {
        playlistAndSoundCrossDao.insertPlayListAndSoundCroos(playListAndSoundCrossEntities)

        val playList = PlayListEntity(
            playListId = 1,
            title = "Relaxing Music",
            currentSoundPosition = 1
        )
        val sound1 = SoundEntity(soundId = 3, title = "Upbeat Track", duration = "240")
        val sound2 = SoundEntity(soundId = 4, title = "Motivational Beat", duration = "300")


        playListDAO.createPlayList(playList)
        soundDao.saveSound(sound1)
        soundDao.saveSound(sound2)

        val result = playlistAndSoundCrossDao.findAllPlayListWithSong()

        assertThat(result).isNotEmpty()
        assertThat(result.first().playList.title).isEqualTo("Relaxing Music")
    }

    @Test
    fun deletePlayListAndSoundCross_should_delete_playlistandsoundcrossentity_by_id_playlist() =
        runTest {
            val listLongResultPlayListXSound = playlistAndSoundCrossDao
                .insertPlayListAndSoundCroos(playListAndSoundCrossEntities)
            println("values inserted = $listLongResultPlayListXSound")
            val result = playlistAndSoundCrossDao.deletePlayListAndSoundCrossByIdPlayList(3)

            assertThat(result).isEqualTo(2)

        }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun deleteItemPlayListAndSoundCroos_should_delete_sound_of_playlist() = runTest {
        val listLongResultPlayListXSound = playlistAndSoundCrossDao
            .insertPlayListAndSoundCroos(playListAndSoundCrossEntities)
        println("values inserted = $listLongResultPlayListXSound")
        val result = playlistAndSoundCrossDao.deleteItemPlayListAndSoundCroos(3, 7)
        assertThat(result).isEqualTo(1)
    }


    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun findSoundByNameAndSoundId_should_find_all_sounds_title() = runTest {
        val playList = PlayListEntity(
            playListId = 1,
            title = "Relaxing Music",
            currentSoundPosition = 1
        )
        val playList2 = PlayListEntity(
            playListId = 2,
            title = "Study Music",
            currentSoundPosition = 1
        )

        val sound1= SoundEntity(soundId = 3, title = "Upbeat Track", duration = "240")
        val sound2 = SoundEntity(soundId = 4, title = "Motivational Beat", duration = "300")

        playListDAO.createPlayList(playList)
        playListDAO.createPlayList(playList2)

        soundDao.saveSound(sound1)
        soundDao.saveSound(sound2)
        playlistAndSoundCrossDao.insertPlayListAndSoundCroos(playListAndSoundCrossEntities)

        val playListAndSoundCrossEntities = listOf(
            PlayListAndSoundCrossEntity(playListId = 2L, soundId = 3L),
            PlayListAndSoundCrossEntity(playListId = 2L, soundId = 4L),
            PlayListAndSoundCrossEntity(playListId = 1L, soundId = 3L),
        )

        playlistAndSoundCrossDao.insertPlayListAndSoundCroos(playListAndSoundCrossEntities)

      val result   =  playlistAndSoundCrossDao.findSoundByNameAndSoundId("Upbeat Track",3)
        assertThat(result.song.title).isEqualTo("Upbeat Track")
        assertThat(result.playlists.size).isEqualTo(2)
    }

    @After
    fun tearDown() {
        soundDatabase.close()
    }
}