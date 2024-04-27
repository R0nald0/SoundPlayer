package com.example.soundplayer.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.soundplayer.data.entities.PlayListAndSoundCrossEntity
import com.example.soundplayer.data.entities.PlayListWithSong

@Dao
interface PlaylistAndSoundCrossDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend  fun insertPlayListAndSoundCroos(playListAndSoundCrossEntity: List<PlayListAndSoundCrossEntity>):List<Long>

   @Transaction
   @Query( value ="DELETE FROM playlistandsoundcrossentity WHERE playListId = :idPlaylist")
   suspend  fun deletePlayListAndSoundCross(idPlaylist:Long) : Int

    @Transaction
    @Query( value ="DELETE FROM playlistandsoundcrossentity WHERE playListId = :idPlaylist AND soundId = :idSound")
    suspend  fun deleteItemPlayListAndSoundCroos(idPlaylist:Long , idSound : Long) : Int

    @Update
    suspend  fun updatePlayListAndSoundCross(playListAndSoundCrossEntity: List<PlayListAndSoundCrossEntity>) : Int

    @Transaction
    @Query(value = "SELECT * FROM playList")
    suspend fun findAllPlayListWithSong():List<PlayListWithSong>

    @Transaction
    @Query(value = "SELECT * FROM playList WHERE playListId = :idPlaylist")
    suspend fun findPlayListById(idPlaylist: Long) : PlayListWithSong?

    @Transaction
    @Query(value = "SELECT * FROM playlistandsoundcrossentity WHERE playListId = :idPlaylist")
    suspend fun findPlayListAcrossSoundById(idPlaylist: Long) : PlayListAndSoundCrossEntity

}