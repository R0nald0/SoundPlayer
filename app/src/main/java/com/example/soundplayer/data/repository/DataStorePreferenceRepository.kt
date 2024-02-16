package com.example.soundplayer.data.repository
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.example.soundplayer.commons.constants.Constants
import com.example.soundplayer.commons.constants.Constants.ID_DARKMODE_KEY
import com.example.soundplayer.commons.constants.Constants.ID_PLAYLIST_KEY
import com.example.soundplayer.commons.constants.Constants.ID_SIZE_TEXT_TITLE_MUSIC
import com.example.soundplayer.commons.constants.Constants.POSITION_KEY
import com.example.soundplayer.data.entities.UserDataPreferecence
import kotlinx.coroutines.flow.first
import java.io.IOException
import javax.inject.Inject

class DataStorePreferenceRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {


    suspend fun savePreference(playlistKeyName:Long? , positionSoundKey:Int){
        try {
            dataStore.edit { prefen ->
                prefen[Constants.ID_PLAYLIST_KEY] = playlistKeyName ?:1
                prefen[POSITION_KEY] = positionSoundKey
            }
        }catch (ioException:IOException){
            throw ioException
        }catch (exeption :Exception){
            Log.e("error", "error : ${exeption.message} ")
            throw Exception("Erro ao salvar preferencias")
        }
    }

     private suspend fun readPreferences(key: Preferences.Key<Long>):Long?{
      try {
          val data  = dataStore.data.first()
          return data[key]
      }catch (ioException:IOException){
          throw ioException
      }catch (exception :Exception){
          throw exception
      }

    }

     private suspend fun readPreferencesPos(key: Preferences.Key<Int>):Int? {
         try {
             val data = dataStore.data.first()
             return data[key]
         } catch (ioException: IOException) {
             throw ioException
         } catch (exception: Exception) {
             throw exception
         }
     }

    suspend fun readAllPreferecenceData(): UserDataPreferecence? {
           try {
              val position = readPreferencesPos(POSITION_KEY)
              val idPlay= readPreferences(ID_PLAYLIST_KEY)
              val isDarkMode= readBooleansPreference()
               val sizeTitleMusic = readSizeTitleMusis(ID_SIZE_TEXT_TITLE_MUSIC)
               if (idPlay!=null){
                   return  UserDataPreferecence(
                       idPreference = idPlay ,
                       postionPreference = position ?: 0,
                       isDarkMode =isDarkMode ?: false,
                       sizeTitleMusic = sizeTitleMusic ?: 16f
                   )
               }
               return  null
           }catch (exeption:Exception){
               Log.e("error", "error : ${exeption.message} ", )
               throw Exception("erro ao ler dados em cache")
           }
    }
    suspend fun readBooleansPreference():Boolean?{
        try {
            val data  = dataStore.data.first()
            return data[ID_DARKMODE_KEY]
        }catch (ioException:IOException){
            throw ioException
        }catch (exception :Exception){
            throw exception
        }

    }
   suspend fun savePreferenceModeUi(darkMode: Boolean) {
         try {
             dataStore.edit {prefen->
                 prefen[ID_DARKMODE_KEY] = darkMode
             }
         }catch (exception :Exception){
             throw Exception(exception.message)
         }
    }
    suspend fun saveSizeTitleMusic(sizeText: Float ){
        try {
            dataStore.edit {prefen->
                prefen[ID_SIZE_TEXT_TITLE_MUSIC] = sizeText
            }
        }catch (exception :Exception){
            throw Exception(exception.message)
        }
    }

     suspend fun readSizeTitleMusis(key: Preferences.Key<Float>):Float? {
        try {
            val data = dataStore.data.first()
            return data[key]
        } catch (ioException: IOException) {
            throw ioException
        } catch (exception: Exception) {
            throw exception
        }
    }
}