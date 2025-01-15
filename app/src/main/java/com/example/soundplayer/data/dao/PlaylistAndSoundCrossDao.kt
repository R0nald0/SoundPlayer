package com.example.soundplayer.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.soundplayer.data.entities.PlayListAndSoundCrossEntity
import com.example.soundplayer.data.entities.PlayListWithSong
import com.example.soundplayer.data.entities.SongWithPlaylists

@Dao
interface PlaylistAndSoundCrossDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend  fun insertPlayListAndSoundCroos(playListAndSoundCrossEntity: List<PlayListAndSoundCrossEntity>):List<Long>


    @Query( value ="DELETE FROM playlistandsoundcrossentity WHERE playListId = :idPlaylist")
    suspend  fun deletePlayListAndSoundCrossByIdPlayList(idPlaylist:Long) : Int

    @Transaction
    @Query( value ="DELETE FROM playlistandsoundcrossentity WHERE playListId = :idPlaylist AND soundId = :idSound")
    suspend  fun deleteItemPlayListAndSoundCroos(idPlaylist:Long , idSound : Long) : Int

    @Transaction
    @Query(value = "SELECT * FROM playList ")
    suspend fun findAllPlayListWithSong():List<PlayListWithSong>

    //Not used
    @Transaction
    @Query(value = "SELECT * FROM playlistandsoundcrossentity WHERE playListId = :idPlaylist")
    suspend fun findPlayListAcrossSoundById(idPlaylist: Long) : PlayListAndSoundCrossEntity

    @Query("""
        SELECT * FROM sound 
        INNER JOIN playlistandsoundcrossentity 
        ON sound.soundId = playlistandsoundcrossentity.soundId
        WHERE sound.title LIKE '%' || :title || '%' 
        AND playlistandsoundcrossentity.soundId = :soundId
    """)
  suspend  fun findSoundByNameAndSoundId(title: String, soundId: Long): SongWithPlaylists




}