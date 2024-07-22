package com.example.soundplayer.service

import androidx.datastore.preferences.core.Preferences
import com.example.soundplayer.data.repository.DataStorePreferenceRepository
import javax.inject.Inject

class UserPrefferencesService @Inject constructor(
    private  val dataStorePreferenceRepository: DataStorePreferenceRepository
) {
    suspend fun readAppAllPrefferences() = dataStorePreferenceRepository.readAppAllPrefferences()
    suspend fun <T>readUserPrefference(key: Preferences.Key<T>) = dataStorePreferenceRepository.readUserPrefference(key)

    suspend fun <T>savePrefferenceUser(value: T,key : Preferences.Key<T>) =
           dataStorePreferenceRepository.savePrefferenceUser(value,key)
}