package com.example.soundplayer.di

import android.app.Application
import com.google.android.material.color.DynamicColors
import com.google.android.material.color.utilities.DynamicColor
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SoundBaseApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        DynamicColors.applyToActivitiesIfAvailable(this)
    }
}
