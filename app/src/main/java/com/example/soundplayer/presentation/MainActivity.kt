package com.example.soundplayer.presentation

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.graphics.ColorUtils
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.soundplayer.commons.extension.checkThemeMode
import com.example.soundplayer.commons.extension.exibirToast
import com.example.soundplayer.presentation.navigation.SoundPlayerNavHost
import com.example.soundplayer.presentation.theme.SoundPlayerPreferenceTheme
import com.example.soundplayer.presentation.viewmodel.PlayListViewModel
import com.example.soundplayer.presentation.viewmodel.PreferencesIntent
import com.example.soundplayer.presentation.viewmodel.PreferencesViewModel
import com.example.soundplayer.presentation.viewmodel.SearchViewModel
import com.example.soundplayer.presentation.viewmodel.SoundIntent
import com.example.soundplayer.presentation.viewmodel.SoundViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val soundViewModel by viewModels<SoundViewModel>()
    private val playListViewModel by viewModels<PlayListViewModel>()
    private val searchViewModel by viewModels<SearchViewModel>()
    private val preferencesViewModel by viewModels<PreferencesViewModel>()
    private var isLoading = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen().apply {
            setKeepOnScreenCondition { isLoading }
        }
        setupComposeContent()
        collectState()
        setupNavigationBarColor()
    }

    override fun onStart() {
        super.onStart()
        preferencesViewModel.onIntent(PreferencesIntent.ReadDarkMode)
        soundViewModel.onIntent(SoundIntent.UpdateAudioFocus)
    }

    override fun onStop() {
        super.onStop()
        soundViewModel.uiState.value.currentPlayList?.idPlayList?.let { id ->
            preferencesViewModel.onIntent(PreferencesIntent.SavePlaylistId(id))
        }
    }

    private fun collectState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    preferencesViewModel.uiState.collect { state ->
                        applyDarkMode(state.darkMode)
                        isLoading = false
                    }
                }
                launch {
                    preferencesViewModel.uiEvent.collect { event ->
                        when (event) {
                            is com.example.soundplayer.presentation.viewmodel.PreferencesUiEvent.ShowError ->
                                exibirToast(event.message)
                        }
                    }
                }
            }
        }
    }

    private fun applyDarkMode(mode: Int) {
        when (mode) {
            0 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            1 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            2 -> checkThemeMode()
        }
    }

    private fun setupComposeContent() {
        setContent {
            val preferencesState = preferencesViewModel.uiState.collectAsStateWithLifecycle()
            SoundPlayerPreferenceTheme(darkMode = preferencesState.value.darkMode) {
                SoundPlayerNavHost(
                    playListViewModel = playListViewModel,
                    soundViewModel = soundViewModel,
                    preferencesViewModel = preferencesViewModel,
                    searchViewModel = searchViewModel,
                )
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun setupNavigationBarColor() {
        val color = 0xffFF400404
        window.navigationBarColor = ColorUtils.setAlphaComponent(color.toInt(), 230)
    }
}
