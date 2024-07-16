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
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend  fun saveSound(sound : SoundEntity):Long
    @Query(value = "SELECT * FROM sound")
    suspend  fun findAllSound():List<SoundEntity>

    @Query(value = "SELECT title FROM sound")
    suspend  fun findAllSoundTiitle():List<String>

    @Query(value = "SELECT * FROM sound WHERE soundId = :idSound")
    suspend  fun findSoundById(idSound:Long?):SoundEntity

    @Query(value = "SELECT * FROM sound WHERE sound.title = :title")
    suspend  fun findSoundByName(title:String):SoundEntity?


    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend   fun updateSound(sound: SoundEntity)
    @Delete
    suspend  fun deleteSound(sound: SoundEntity):Int
}