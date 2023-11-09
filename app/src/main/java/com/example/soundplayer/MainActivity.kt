package com.example.soundplayer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log

import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.soundplayer.adapter.SoundAdapter
import com.example.soundplayer.databinding.ActivityMainBinding
import com.example.soundplayer.model.Sound
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val listSound = mutableListOf<Sound>()
    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

   lateinit var gerenciarPermissoes : ActivityResultLauncher<String>
    private lateinit var  adapterSound : SoundAdapter

    @Inject lateinit var  soundPresenter : SoundPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

         initAdapter()
         getPermissions()

        binding.btnFindSounds.setOnClickListener {
            requestPermission()
        }

    }

    private fun getPermissions(){

        gerenciarPermissoes =  registerForActivityResult(ActivityResultContracts.RequestPermission()){ isPermitted->
            Log.i("INFO_", "getPermissions: permitido ? $isPermitted")
            if (!isPermitted){
                binding.btnFindSounds.visibility = View.VISIBLE
                binding.txvSoundNotFound.visibility = View.VISIBLE
                binding.rvSound.visibility = View.GONE
                Toast.makeText(this, "Permissão necessaria para carragar as músicas", Toast.LENGTH_SHORT).show()
            }else{
                binding.btnFindSounds.visibility = View.GONE
                binding.txvSoundNotFound.visibility = View.GONE
                binding.rvSound.visibility = View.VISIBLE
                getMusicFromContentProvider()
                adapterSound.addSound(listSound)
            }
        }
        requestPermission()

    }

    fun requestPermission(){
        gerenciarPermissoes.launch(
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        )
    }

    override fun onStart() {
        super.onStart()
          adapterSound.addSound(listSound)
    }

    override fun onResume() {
        super.onResume()
        if (soundPresenter.isPlaying()){
            soundPresenter.actualSound.observe(this){soundLiveData->
                Toast.makeText(this, "Tocando ${soundLiveData.title} ", Toast.LENGTH_SHORT).show()
                Log.i("INFO_", "Main:${soundLiveData.title} ")
                adapterSound.getActualSound(soundLiveData)
            }
        }
    }

    private fun initAdapter() {
        adapterSound = SoundAdapter()
        binding.rvSound.adapter = adapterSound
        binding.rvSound.layoutManager = LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false)
    }

    private fun getMusicFromContentProvider(){
        val projection = arrayOf(
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DURATION,
        )
        val cursor = contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            null
        )

        if (cursor != null) {
            try {
                while (cursor.moveToNext()){
                    val sound = Sound(
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(0)
                    )
                    if (File(sound.path).exists()){
                        listSound.add(sound)
                    }
                }
            }catch (nullPointer : NullPointerException){
                Toast.makeText(this, "Null ${nullPointer.printStackTrace()}", Toast.LENGTH_SHORT).show()
            }
        }
        verifyListIsEmpty()

    }

    private fun verifyListIsEmpty(){
        if (listSound.isEmpty()){
            binding.rvSound.visibility =View.GONE
            binding.txvSoundNotFound.visibility =View.VISIBLE
        }else{
            binding.rvSound.visibility =View.VISIBLE
            binding.txvSoundNotFound.visibility =View.GONE
        }
    }


}