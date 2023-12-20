package com.example.soundplayer.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.soundplayer.data.entities.PlayListEntity
import com.example.soundplayer.data.entities.PlayListWithSong

@Dao
interface PlayListDAO {
    @Insert
    fun createPlayList(playList : PlayListEntity):Long
    @Query(value = "SELECT * FROM playList")
    fun findAllPlayList():List<PlayListWithSong>
    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updatePlayList(playList: PlayListEntity)
    @Delete
    fun deletePlayList(playList: PlayListEntity)
}