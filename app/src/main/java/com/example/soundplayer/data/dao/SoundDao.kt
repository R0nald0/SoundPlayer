package com.example.soundplayer.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.soundplayer.data.entities.SoundEntity

@Dao
interface SoundDao {
    @Insert
    suspend  fun createSound(sound : SoundEntity):Long
    @Query(value = "SELECT * FROM sound")
    suspend  fun findAllSound():List<SoundEntity>

    @Query(value = "SELECT * FROM sound WHERE soundId = :idSound")
    suspend  fun findAllSound(idSound:Long?):List<SoundEntity>


    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend   fun updateSound(sound: SoundEntity)
    @Delete
    suspend  fun deleteSound(sound: SoundEntity):Int
}