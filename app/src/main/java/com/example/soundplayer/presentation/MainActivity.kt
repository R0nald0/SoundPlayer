package com.example.soundplayer.presentation

import android.content.ContentUris
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
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.soundplayer.R
import com.example.soundplayer.commons.constants.Constants
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
import java.io.File


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val listSoundFromContentProvider = mutableSetOf<Sound>()
    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    lateinit var myMenuProvider: MenuProvider
    var cont = 0
        private lateinit var gerenciarPermissoes : ActivityResultLauncher<String>
    private lateinit var  adapterSound : SoundAdapter
    private lateinit var  playListAdapter: PlayListAdapter
    private val  soundViewModel by viewModels<SoundViewModel>();
    private val playListViewModel by viewModels<PlayListViewModel>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setupToolbar()
        initAdapter()
        getPermissions()
        observersViewModel()

        binding.btnFindSounds.setOnClickListener {
            requestPermission()
        }
        
        binding.fabAddPlayList.setOnClickListener {
            createEditPlayListFragment(null)
        }
    }
    private fun setupToolbar() {
        setSupportActionBar(binding.includeSelectItem.toolbarSelecrionItemsMaterial)
        myMenuProvider = MyMenuProvider()
    }

    override fun onStart() {
        super.onStart()
    }

    private fun createEditPlayListFragment(playList: PlayList?) {
        val bottomSheetFragment =BottomSheetFragment()
        val bundle = bundleOf("list" to playList)
        bottomSheetFragment.arguments = bundle
        bottomSheetFragment.show(supportFragmentManager,"tag")
    }

    private  fun observersViewModel(){
        playListViewModel.playLists.observe(this){listOfplayListObservable->
             playListAdapter.addPlayList(listOfplayListObservable)
        }

       playListViewModel.uniquePlayList.observe(this){uniquePlayListWithSongs->
              soundViewModel.updatePlayList(uniquePlayListWithSongs.listSound)
              adapterSound.getPlayList(uniquePlayListWithSongs)
       }

        playListViewModel.soundListBd.observe(this){listSound->
              if (listSound.isNotEmpty()){
                  val playList =  PlayList(
                      idPlayList = null,
                      name=Constants.ALL_MUSIC_NAME,
                      listSound =  listSound.toMutableSet(),
                      currentMusicPosition = 0
                  )
                  playListViewModel.savePlayList(playList)
              }
        }

        if (soundViewModel.isPlaying() == true){
            soundViewModel.actualSound.observe(this){ soundLiveData->
                Toast.makeText(this, "Tocando ${soundLiveData.title} ", Toast.LENGTH_SHORT).show()
                Log.i("INFO_", "Main:${soundLiveData.title} ${cont++}")
                //TODO VERIFICAR CHAMAS MULTIPLAS
                adapterSound.getActualSound(soundLiveData)
                binding.rvSound.scrollToPosition(soundViewModel.currentItem)
            }
        }
    }

    private fun getPermissions(){
        gerenciarPermissoes =  registerForActivityResult(ActivityResultContracts.RequestPermission()){ isPermitted->

            if (!isPermitted){
                binding.btnFindSounds.visibility = View.VISIBLE
                binding.txvSoundNotFound.visibility = View.VISIBLE
                binding.linearMusics.visibility = View.GONE
                binding.fabAddPlayList.visibility = View.GONE
                Toast.makeText(this, "Permissão necessaria para carragar as músicas", Toast.LENGTH_SHORT).show()
            }else{
                binding.btnFindSounds.visibility = View.GONE
                binding.txvSoundNotFound.visibility = View.GONE
                binding.linearMusics.visibility = View.VISIBLE
                binding.fabAddPlayList.visibility = View.VISIBLE
                getMusicFromContentProvider()
                if (listSoundFromContentProvider.isNotEmpty()) playListViewModel.saveAllSoundsByContentProvider(listSoundFromContentProvider)
                playListViewModel.getAllPlayList()
            }
        }
        requestPermission()
    }

    private fun requestPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            gerenciarPermissoes.launch(
                android.Manifest.permission.READ_MEDIA_AUDIO
            )
        }else{
            gerenciarPermissoes.launch(
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }
    }

    private fun initAdapter() {
        adapterSound = SoundAdapter(
           soundViewModel =  soundViewModel,
            isUpdateList = {isUpdate-> updateViewWhenMenuChange(isUpdate) }
            )

        binding.rvSound.adapter = adapterSound
        binding.rvSound.layoutManager = LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false)

        playListAdapter = PlayListAdapter(
            soundViewModel = soundViewModel,
            onclick = { playListChoseByUser -> adapterSound.getPlayList(playListChoseByUser) },
            onDelete = {playList -> playListViewModel.deletePlayList(playList)},
            onEdit = {playList -> playListViewModel.updateNamePlayList(playList) }
        )
        binding.idRvFavoriteList.adapter = playListAdapter
        binding.idRvFavoriteList.layoutManager = LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false)

    }

    private fun updateViewWhenMenuChange(isUpdate: Boolean) {
        if (isUpdate) {
            addMenuProvider(myMenuProvider)
            binding.include.toolbarPrincipal.visibility = View.GONE
            binding.includeSelectItem.toolbarSelecrionItems.visibility = View.VISIBLE
            binding.fabAddPlayList.isVisible = false
            binding.includeSelectItem.backButton.setOnClickListener {
                adapterSound.clearSoundListSelected()
            }
        } else {
            removeMenuProvider(myMenuProvider)
            binding.fabAddPlayList.isVisible = true
            binding.include.toolbarPrincipal.visibility = View.VISIBLE
            binding.includeSelectItem.toolbarSelecrionItems.visibility = View.GONE
        }
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
                        idSound = null,
                        path =  cursor.getString(1),
                        duration =  cursor.getString(2),
                        title=  cursor.getString(0),
                        uriMedia = mediaUri,
                        uriMediaAlbum = mediaUriAlbum
                    )

                    if (File(sound.path).exists()){
                        if (!listSoundFromContentProvider.contains(sound)){
                            listSoundFromContentProvider.add(sound)
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
        if (listSoundFromContentProvider.isEmpty()){
            binding.linearMusics.visibility =View.GONE
            binding.txvSoundNotFound.visibility =View.VISIBLE
        }else{
            binding.linearMusics.visibility =View.VISIBLE
            binding.txvSoundNotFound.visibility =View.GONE
            binding.txvQuantidadeMusics.text ="Total de Músicas ${listSoundFromContentProvider.size}"
        }
    }

     override fun onDestroy() {
        soundViewModel.destroyPlayer()
        super.onDestroy()
    }

   inner class  MyMenuProvider() : MenuProvider {
       override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
           menuInflater.inflate(R.menu.sound_menu,menu)
         val pairPlayList = adapterSound.getSoundSelecionados()
           if (pairPlayList.first.toInt() == 1){
               menu.removeItem(R.id.id_remove)
           }

       }

       override fun onMenuItemSelected(menuItem: MenuItem): Boolean {

           val pairPlayList  =  adapterSound.getSoundSelecionados()

           return   when(menuItem.itemId){

               R.id.id_update->{
                 val sound = SoundList(0,pairPlayList.second)
                   val  bottomSheetFragment =SelectPlayListDialogFragment()
                   val bundle = bundleOf("listSound" to sound)
                   bottomSheetFragment.arguments = bundle
                   bottomSheetFragment.show(supportFragmentManager,"tag")
                   true
               }
               R.id.id_remove ->{
                   playListViewModel.removePlaySoundFromPlayList(
                        playListId = pairPlayList.first,
                        listRemovedItems =pairPlayList.second.toSet()
                   )
                   true
               }
               else ->false
           }

       }
    }



}