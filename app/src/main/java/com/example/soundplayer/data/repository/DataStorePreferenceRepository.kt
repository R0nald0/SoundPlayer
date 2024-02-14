package com.example.soundplayer.data.repository
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import com.example.soundplayer.data.entities.UserDataPreferecence
import kotlinx.coroutines.flow.first
import java.io.IOException
import javax.inject.Inject

class DataStorePreferenceRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private val POSITION_KEY = intPreferencesKey("postionKey")
    private val ID_PLAYLIST_KEY = longPreferencesKey("playlist")
    private val ID_DARKMODE_KEY = booleanPreferencesKey("isDarkMode")

    suspend fun savePreference(playlistKeyName:Long? , positionSoundKey:Int){
        try {
            dataStore.edit { prefen ->
                prefen[ID_PLAYLIST_KEY] = playlistKeyName ?:1
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
               if (idPlay!=null){
                   return  UserDataPreferecence(
                       idPreference = idPlay ,
                       postionPreference = position ?: 0,
                       isDarkMode =isDarkMode ?: false
                   )
               }
               return  null
           }catch (exeption:Exception){
               Log.e("error", "error : ${exeption.message} ", )
               throw Exception("erro ao ler dados em cache")
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
}