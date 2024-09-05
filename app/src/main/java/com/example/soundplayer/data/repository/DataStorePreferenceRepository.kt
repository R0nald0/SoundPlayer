package com.example.soundplayer.data.repository
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.example.soundplayer.commons.constants.Constants.ID_DARK_MODE_KEY
import com.example.soundplayer.commons.constants.Constants.ID_ORDERED_SONS_PREFFERENCE
import com.example.soundplayer.commons.constants.Constants.ID_PLAYLIST_KEY
import com.example.soundplayer.commons.constants.Constants.ID_SIZE_TEXT_TITLE_MUSIC
import com.example.soundplayer.commons.constants.Constants.POSITION_KEY
import com.example.soundplayer.commons.execptions.Failure
import com.example.soundplayer.data.entities.UserDataPreferecence
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

class DataStorePreferenceRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    suspend fun savePreference(playlistKeyId:Long?, positionSoundKey:Int){
        try {
            dataStore.edit { prefen ->
                prefen[ID_PLAYLIST_KEY] = playlistKeyId ?:1
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

    suspend fun readAppAllPrefferences():UserDataPreferecence{
        try {
            val data = dataStore.data.first()
            return UserDataPreferecence(
                orderedSound = data[ID_ORDERED_SONS_PREFFERENCE] ?: 0,
                idPreference = data[ID_PLAYLIST_KEY] ?: 1L,
                postionPreference = data[POSITION_KEY] ?: 0,
                isDarkMode = data[ID_DARK_MODE_KEY] ?: 0,
                sizeTitleMusic = data[ID_SIZE_TEXT_TITLE_MUSIC] ?: 15f
            )
        }catch (ioException:IOException){
            Log.e("error", "error : ${ioException.message} ", )
            throw Failure(messages = "Não conseguimos ler as preferências",ioException,ioException.hashCode())
        }catch (exception :Exception){
            throw exception
        }
    }

    suspend fun <T>readUserPrefference(key: Preferences.Key<T>): Flow<T?> {
        try {
            val data = dataStore.data.map {
                it[key]
            }
            return data
        } catch (ioException: IOException) {
            throw ioException
        } catch (exception: Exception) {
            throw exception
        }
    }
    suspend fun <T> savePrefferenceUser(value: T,key : Preferences.Key<T>){
        try {
            dataStore.edit {preference->
                preference[key] = value
            }
        }catch (ioException: IOException) {
            throw ioException
        } catch (exception: Exception) {
            throw exception
        }
    }

}