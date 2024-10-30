package com.example.soundplayer.presentation



import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.graphics.ColorUtils
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import com.example.soundplayer.R
import com.example.soundplayer.commons.extension.checkThemeMode
import com.example.soundplayer.commons.extension.exibirToast
import com.example.soundplayer.databinding.ActivityMainBinding
import com.example.soundplayer.presentation.viewmodel.PreferencesViewModel
import com.example.soundplayer.presentation.viewmodel.SoundViewModel
import com.example.soundplayer.presentation.viewmodel.StatePrefre
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val  soundViewModel by viewModels<SoundViewModel>()
    private val preferencesViewModel by viewModels<PreferencesViewModel>()
    private var isLoading = true
    private lateinit var  navController : NavController
    private lateinit var appBarConfiguration :AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen().apply {
            setKeepOnScreenCondition{
                isLoading
            }
        }
        setContentView(binding.root)
        getNavHost()
        observer()
        setupNavitionBarColor()
    }

    private fun setupNavitionBarColor() {
        val color = 0xffFF400404
        window.navigationBarColor = ColorUtils.setAlphaComponent(color.toInt(), 230)
    }

    override fun onStart() {
        preferencesViewModel.readDarkModePreference()
        super.onStart()
        soundViewModel.updateAudioFocos()
    }

    private fun observer(){
           preferencesViewModel.isDarkMode.observe(this){statePreference->
                when(statePreference){
                    is StatePrefre.Sucess<*> ->{
                       val result =  statePreference.succssResult as Int
                        when(result){
                            0 ->{ AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO) }
                            1-> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                            2 -> {
                               this.checkThemeMode()
                            }
                        }
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
        appBarConfiguration = AppBarConfiguration(navController.graph)
    }
    override fun onStop() {
        val idPlayList = soundViewModel.currentPlayList.value?.idPlayList
        if (idPlayList  !=  null){
            preferencesViewModel.savePlayListIdPlayList(idPlayList)
        }
        Log.i("INFO_", "OnStop MAin Activity $idPlayList")
        super.onStop()
    }

}