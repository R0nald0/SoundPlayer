package com.example.soundplayer.presentation

import android.os.Build
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.graphics.ColorUtils
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.soundplayer.R
import com.example.soundplayer.commons.extension.exibirToast
import com.example.soundplayer.databinding.ActivityMainBinding
import com.example.soundplayer.presentation.viewmodel.PreferencesViewModel
import com.example.soundplayer.presentation.viewmodel.SoundViewModel
import com.example.soundplayer.presentation.viewmodel.StatePrefre
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val  soundViewModel by viewModels<SoundViewModel>()
    private val preferencesViewModel by viewModels<PreferencesViewModel>()

    private var isLoading = true

    private val listPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        listOf(
            android.Manifest.permission.READ_MEDIA_AUDIO,
            android.Manifest.permission.FOREGROUND_SERVICE,
            android.Manifest.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK,
        )
    } else {
       listOf(
           android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
           android.Manifest.permission.READ_EXTERNAL_STORAGE,
       )
    }
    private lateinit var  navController : NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen().apply {
            setKeepOnScreenCondition{
                isLoading}
        }
        setContentView(binding.root)
       getNavHost()
        observer()
        val color = 0xffFF400404
        window.navigationBarColor = ColorUtils.setAlphaComponent(color.toInt(),230)
    }

    override fun onStart() {
        preferencesViewModel.readDarkModePreference()
        super.onStart()
    }
    fun observer(){
        preferencesViewModel.isDarkMode.observe(this){statePreference->
            when(statePreference){
                is StatePrefre.Sucess<*> ->{
                     if (statePreference.succssResult as Boolean)AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                     else AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                }
                is StatePrefre.Error ->{
                   exibirToast(statePreference.mensagem)
                }
            }
            isLoading =false
        }

    }
    private fun getNavHost() {
        val navHost = supportFragmentManager.findFragmentById(R.id.myHostFragment) as NavHostFragment
         navController = navHost.navController

    }
    override fun onStop() {
    CoroutineScope(Dispatchers.Main).launch {
        soundViewModel.savePreference()
    }
        super.onStop()
    }

}