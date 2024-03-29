package com.example.soundplayer.commons.constants

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey

object Constants {
     const val DATABASE_NAME ="playlist_database"
     const val  ALL_MUSIC_NAME ="All Musics"
     const val  PREFERENCE_NAME ="settings"
      val POSITION_KEY = intPreferencesKey("postionKey")
      val ID_PLAYLIST_KEY = longPreferencesKey("playlist")
      val ID_DARKMODE_KEY = booleanPreferencesKey("isDarkMode")
      val ID_SIZE_TEXT_TITLE_MUSIC= floatPreferencesKey("sizeTextTitleMusic")
}