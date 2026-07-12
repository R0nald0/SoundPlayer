package com.example.soundplayer.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.example.soundplayer.commons.constants.Constants.ID_DARK_MODE_KEY
import com.example.soundplayer.commons.constants.Constants.ID_ORDERED_SOUNDS_PREFERENCE
import com.example.soundplayer.commons.constants.Constants.ID_PLAYLIST_KEY
import com.example.soundplayer.commons.constants.Constants.ID_SIZE_TEXT_TITLE_MUSIC
import com.example.soundplayer.commons.constants.Constants.POSITION_KEY
import com.example.soundplayer.commons.exceptions.Failure
import com.example.soundplayer.data.entities.UserDataPreference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

class DataStorePreferenceRepository
    @Inject
    constructor(
        private val dataStore: DataStore<Preferences>,
    ) {
        suspend fun savePreference(
            playlistKeyId: Long?,
            positionSoundKey: Int,
        ) {
            dataStore.edit { preferences ->
                preferences[ID_PLAYLIST_KEY] = playlistKeyId ?: 1
                preferences[POSITION_KEY] = positionSoundKey
            }
        }

        suspend fun readAppAllPreferences(): UserDataPreference =
            try {
                val data = dataStore.data.first()
                UserDataPreference(
                    orderedSound = data[ID_ORDERED_SOUNDS_PREFERENCE] ?: 0,
                    playlistPreferenceId = data[ID_PLAYLIST_KEY] ?: 1L,
                    positionPreference = data[POSITION_KEY] ?: 0,
                    isDarkMode = data[ID_DARK_MODE_KEY] ?: 0,
                    sizeTitleMusic = data[ID_SIZE_TEXT_TITLE_MUSIC] ?: 15f,
                )
            } catch (ioException: IOException) {
                throw Failure(
                    messages = "N\u00e3o conseguimos ler as prefer\u00eancias",
                    causes = ioException,
                    code = ioException.hashCode(),
                )
            }

        fun <T> readUserPreference(key: Preferences.Key<T>): Flow<T?> =
            dataStore.data.map { preferences -> preferences[key] }

        suspend fun <T> saveUserPreference(
            value: T,
            key: Preferences.Key<T>,
        ) {
            dataStore.edit { preferences ->
                preferences[key] = value
            }
        }
    }
