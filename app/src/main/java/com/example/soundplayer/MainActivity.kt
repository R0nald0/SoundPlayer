package com.example.soundplayer

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log

import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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


    private lateinit var  adapterSound : SoundAdapter

    @Inject lateinit var  soundPresenter : SoundPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initAdapter()

       if (!checkPermission()){
           requestPermission()
           return
       }
       getMusicFromContentProvider()
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
        }else{
            Toast.makeText(this, "esta NÂO tocando", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initAdapter() {
        adapterSound = SoundAdapter()
        binding.rvSound.adapter = adapterSound
        binding.rvSound.layoutManager = LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false)
    }

    private fun checkPermission():Boolean{
        val result = ContextCompat.checkSelfPermission(
            this,android.Manifest.permission.READ_EXTERNAL_STORAGE
        )
        return result == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission(){
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,android.Manifest.permission.READ_EXTERNAL_STORAGE
               )){
            Toast.makeText(this, "Permission Requerida", Toast.LENGTH_SHORT).show()
        }else
         ActivityCompat.requestPermissions(
             this,
             listOf(android.Manifest.permission.READ_EXTERNAL_STORAGE).toTypedArray(),
            12
        )

    }

    private fun getMusicFromContentProvider(){
        val projection = arrayOf(
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DURATION
        )
        val cursor = contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            null
        )

        if (cursor != null) {
            while (cursor.moveToNext()){
                val sound = Sound(cursor.getString(1),cursor.getString(2),cursor.getString(0))
                if (File(sound.path).exists()){
                    listSound.add(sound)
                }
            }
        }
        if (listSound.isEmpty()){
            binding.rvSound.visibility =View.GONE
            binding.txvSoundNotFound.visibility =View.VISIBLE
        }else{
            binding.rvSound.visibility =View.VISIBLE
            binding.txvSoundNotFound.visibility =View.GONE
        }
    }


}