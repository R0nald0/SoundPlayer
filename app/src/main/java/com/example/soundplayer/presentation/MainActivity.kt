package com.example.soundplayer.presentation

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI.setupActionBarWithNavController
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.onNavDestinationSelected
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.soundplayer.R
import com.example.soundplayer.SoundPlayerReceiver
import com.example.soundplayer.commons.constants.Constants
import com.example.soundplayer.commons.extension.exibirToast
import com.example.soundplayer.databinding.ActivityMainBinding
import com.example.soundplayer.model.PlayList
import com.example.soundplayer.model.Sound
import com.example.soundplayer.model.SoundList
import com.example.soundplayer.presentation.adapter.PlayListAdapter
import com.example.soundplayer.presentation.adapter.SoundAdapter
import com.example.soundplayer.presentation.fragment.BottomSheetFragment
import com.example.soundplayer.presentation.fragment.SelectPlayListDialogFragment
import com.example.soundplayer.presentation.viewmodel.PlayListViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {


    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }




    private val  soundViewModel by viewModels<SoundViewModel>()

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
    private  lateinit var  appBarConfiguration  :AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen().apply {
            setKeepOnScreenCondition{
               Thread.sleep(1000L)
                false
            }
        }
        setContentView(binding.root)
        getNavHost()
    }

    private fun getNavHost() {
        val navHost = supportFragmentManager.findFragmentById(R.id.myHostFragment) as NavHostFragment
         navController = navHost.navController
        appBarConfiguration = AppBarConfiguration(navController.graph)


    }

    override fun onStart() {

        super.onStart()
    }

    override fun onStop() {
    CoroutineScope(Dispatchers.Main).launch {
        soundViewModel.savePreference()
    }
        super.onStop()
    }


//    private fun menuToolbar(){
//        addMenuProvider(object : MenuProvider {
//            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
//                val menu = menuInflater.inflate(R.menu.menu_toolbar,menu)
//            }
//
//            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
//                return  when(menuItem.itemId){
//                    R.id.menu_config ->{
//                        exibirToast("Clicou no config")
//                        true
//                    }
//                    else->true
//                }
//            }
//        })
//    }



}