package com.example.soundplayer.presentation

import android.content.ContentUris
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.soundplayer.R
import com.example.soundplayer.adapter.PlayListAdapter
import com.example.soundplayer.adapter.SoundAdapter
import com.example.soundplayer.databinding.ActivityMainBinding
import com.example.soundplayer.model.PlayList
import com.example.soundplayer.model.Sound
import com.example.soundplayer.model.SoundList
import com.example.soundplayer.presentation.fragment.BottomSheetFragment
import com.example.soundplayer.presentation.viewmodel.PlayListViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import javax.inject.Inject
import kotlin.random.Random


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val listSound = mutableSetOf<Sound>()
    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private lateinit var gerenciarPermissoes : ActivityResultLauncher<String>
    private lateinit var  adapterSound : SoundAdapter
    private lateinit var  playListAdapter: PlayListAdapter
    @Inject lateinit var  soundPresenter : SoundPresenter

    private val playListViewModel by viewModels<PlayListViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        sortBackGraoundImage()

         initAdapter()
         getPermissions()
        observersViewModel()

        binding.btnFindSounds.setOnClickListener {
            requestPermission()
        }
        
        binding.fabAddPlayList.setOnClickListener { 
              if (listSound.isNotEmpty()){
                  createPlayList(listSound)
              }
        }

    }

    private fun createPlayList(listSound: MutableSet<Sound>) {
        val bottoSheet =BottomSheetFragment()

        val bundle = bundleOf("list" to SoundList(0,listSound))
        bottoSheet.arguments = bundle
        bottoSheet.show(supportFragmentManager,"tag")
    }

    private  fun observersViewModel(){
        playListViewModel.playLists.observe(this){

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
               // adapterSound.addSound(listSound)
                //TODO rever
            }
        }
        requestPermission()

    }

    fun sortBackGraoundImage(){
        val listDrawbleImage = listOf(
            R.drawable.feror, R.drawable.tree
        )
        val numberRanodom = Random.nextInt(2)
        binding.contstaintLayout.background = ContextCompat.getDrawable(this,listDrawbleImage[numberRanodom])

    }
    fun requestPermission(){
        gerenciarPermissoes.launch(
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        )
    }

    override fun onStart() {
        super.onStart()

    }

    override fun onResume() {
        super.onResume()
        if (soundPresenter.isPlaying() == true){
            soundPresenter.actualSound.observe(this){soundLiveData->
                Toast.makeText(this, "Tocando ${soundLiveData.title} ", Toast.LENGTH_SHORT).show()
                Log.i("INFO_", "Main:${soundLiveData.title} ")
                adapterSound.getActualSound(soundLiveData)
                binding.rvSound.scrollToPosition(soundPresenter.currentItem)
            }
        }
    }

    private fun initAdapter() {
        adapterSound = SoundAdapter(soundPresenter)
        binding.rvSound.adapter = adapterSound
        binding.rvSound.layoutManager = LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false)


        playListAdapter = PlayListAdapter{actualPlayList->
            adapterSound.addSound(actualPlayList.listSound)
        }
        binding.idRvFavoriteList.adapter = playListAdapter
        binding.idRvFavoriteList.layoutManager = LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,true)

    }

    private fun getMusicFromContentProvider(){
        val projection = arrayOf(
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.ALBUM_ID,
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
                val id =cursor.getColumnIndexOrThrow( MediaStore.Audio.Media._ID)
                val albumid =cursor.getColumnIndexOrThrow( MediaStore.Audio.Media.ALBUM_ID)

                while (cursor.moveToNext()){

                    val idMedia = cursor.getLong(id)
                    val albumidMedia = cursor.getLong(albumid)

                    val mediaUriAlbum  = ContentUris.withAppendedId(
                        Uri.parse("content://media/external/audio/albumart"),albumidMedia)

                    val mediaUri  = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,idMedia)


                    val sound = Sound(
                        path =  cursor.getString(1),
                        duration =  cursor.getString(2),
                        title=  cursor.getString(0),
                        uriMedia = mediaUri,
                        uriMediaAlbum = mediaUriAlbum
                    )

                    if (File(sound.path).exists()){
                        if (!listSound.contains(sound)){
                            listSound.add(sound)
                        }
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
            binding.linearMusics.visibility =View.GONE
            binding.txvSoundNotFound.visibility =View.VISIBLE
        }else{
            val playList =  PlayList("All Musiscs",listSound)
            playListAdapter.addPlayList(playList)
            binding.linearMusics.visibility =View.VISIBLE
            binding.txvSoundNotFound.visibility =View.GONE
            binding.txvQuantidadeMusics.text ="Músicas ${listSound.size}"
        }
    }


}