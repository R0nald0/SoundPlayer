package com.example.soundplayer.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.example.soundplayer.data.entities.PlayListAndSoundCrossEntity
import com.example.soundplayer.data.entities.PlayListEntity
import com.example.soundplayer.data.entities.PlayListWithSong
import com.example.soundplayer.data.entities.SoundEntity

@Dao
interface PlaylistAndSoundCrossDao {
    @Insert
  suspend fun createSound(sound : SoundEntity):Long

    @Insert
   suspend  fun createPlayList(playListEntity: PlayListEntity):Long

   @Insert
   suspend  fun savePlayListAndSoundCroos(playListAndSoundCrossEntity: List<PlayListAndSoundCrossEntity>):List<Long>

  @Transaction
  @Query(value = "SELECT * FROM playList")
   suspend fun findAllPlayList():List<PlayListWithSong>

}