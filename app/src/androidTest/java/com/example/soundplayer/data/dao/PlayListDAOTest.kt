package com.example.soundplayer.data.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.soundplayer.HelperDao
import com.example.soundplayer.data.database.DatabasePlaylist
import com.example.soundplayer.data.entities.PlayListEntity
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class PlayListDAOTest {

    private lateinit var databasePlayList : DatabasePlaylist
    private lateinit var playListDAO: PlayListDAO

    @Before
    fun setUp(){
        databasePlayList = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            DatabasePlaylist::class.java,
        ).allowMainThreadQueries().build()

    playListDAO = databasePlayList.playlistDao()
    }


    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun given_a_valid_playList_when_createPlayList_is_executed_it_should_save_a_play_list_and_return_long_value()= runTest {
            val playListEntity = PlayListEntity(
                playListId = null,
                title = "Fist playlist",
                currentSoundPosition = 0,
            )
        val result = playListDAO.createPlayList(playList = playListEntity)
        assertThat(result).isEqualTo(1)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun when_findAllPlayList_is_executed_should_return_a_list_of_playListWithSounds() = runTest{
        HelperDao.listOfPlayList().forEach {playListDAO.createPlayList(it)}

        val result = playListDAO.findAllPlayList()

        assertThat(result.size).isEqualTo(4)
        assertThat(result[1].playList.title).isEqualTo("Second playlist")
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun given_a_valid_playList_when_updatePlaylist_is_executed_should_update_data_in_playlist() = runTest{
        HelperDao.listOfPlayList().forEach {playListDAO.createPlayList(it)}
        val playListToUpdate = PlayListEntity(
            playListId = 4,
            title = "Minha nova  playlist",
            currentSoundPosition = 3,
        )

        val result= playListDAO.updatePlayList(playListToUpdate)
        assertThat(result).isEqualTo(1)

        val allPlayList = playListDAO.findPlayListById(4)
        assertThat(allPlayList.playList.title).isEqualTo("Minha nova  playlist")
    }

    @Test
    fun given_a_valid_string_and_id_when_is_executed_updateNamePlayList_it_should_update_name_of_playlist ()= runTest{
         HelperDao.listOfPlayList().forEach { playListDAO.createPlayList(it) }
        val result = playListDAO.updateNamePlayList(id = 10, "Nome Atualizado")
        assertThat(result).isEqualTo(1)

        val allPlayList = playListDAO.findPlayListById(3)
        assertThat(allPlayList.playList.title).isEqualTo("Nome Atualizado")
        assertThat(allPlayList.playList.currentSoundPosition).isEqualTo(0)
    }

    @Test
    fun given_a_string_and_invalid_id_when_is_executed_updateNamePlayList_it_should_return_zero ()= runTest{
        HelperDao.listOfPlayList().forEach { playListDAO.createPlayList(it) }

        val result = playListDAO.updateNamePlayList(id = 10, "Nome Atualizado")
        assertThat(result).isEqualTo(0)

    }

    @Test
    fun given_a_id_valid_when_findPlayListById_is_executed_it_should_return_a_PlayListWithSong()= runTest {
            HelperDao.listOfPlayList().forEach { playListDAO.createPlayList(it) }

        val result = playListDAO.findPlayListById(2)

        assertThat(result.playList.playListId).isEqualTo(2)
        assertThat(result.playList.title).isEqualTo("Second playlist")
    }

    @Test
    fun given_a_playlist_when_deletePlayList_is_executed_it_should_delete_this_playlist_and_return_quantity_lines_affected()= runTest {
        HelperDao.listOfPlayList().forEach { playListDAO.createPlayList(it) }
        val playListToDelete = HelperDao.listOfPlayList()[3].copy(playListId = 3)

        val result = playListDAO.deletePlayList(playListToDelete)
        assertThat(result).isEqualTo(1)

        val allPlayList = playListDAO.findAllPlayList()
        assertThat(allPlayList.size).isEqualTo(3)
    }

}