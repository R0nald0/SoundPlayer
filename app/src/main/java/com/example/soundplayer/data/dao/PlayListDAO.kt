package com.example.soundplayer.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.soundplayer.data.entities.PlayListEntity
import com.example.soundplayer.data.entities.PlayListWithSong

@Dao
interface PlayListDAO {
    @Insert
    suspend fun createPlayList(playList: PlayListEntity): Long

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updatePlayList(playList: PlayListEntity): Int

    @Query(value = "UPDATE playList SET title = :name WHERE playListId =:id")
    suspend fun updateNamePlayList(
        id: Long,
        name: String,
    ): Int

    @Query(value = "SELECT * FROM playList WHERE playListId = :idPlaylist")
    @Transaction
    suspend fun findPlayListById(idPlaylist: Long): PlayListWithSong

    @Delete
    suspend fun deletePlayList(playList: PlayListEntity): Int
}
