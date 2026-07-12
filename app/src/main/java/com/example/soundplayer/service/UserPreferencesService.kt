package com.example.soundplayer.service

import androidx.datastore.preferences.core.Preferences
import com.example.soundplayer.data.repository.DataStorePreferenceRepository
import javax.inject.Inject

class UserPreferencesService
    @Inject
    constructor(
        private val dataStorePreferenceRepository: DataStorePreferenceRepository,
    ) {
        suspend fun readAppAllPreferences() = dataStorePreferenceRepository.readAppAllPreferences()

        fun <T> readUserPreference(key: Preferences.Key<T>) = dataStorePreferenceRepository.readUserPreference(key)

        suspend fun <T> saveUserPreference(
            value: T,
            key: Preferences.Key<T>,
        ) = dataStorePreferenceRepository.saveUserPreference(value, key)
    }
