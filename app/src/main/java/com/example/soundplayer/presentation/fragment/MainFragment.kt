package com.example.soundplayer.presentation.fragment

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat.registerReceiver
import androidx.core.os.bundleOf
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.soundplayer.R
import com.example.soundplayer.SoundPlayerReceiver
import com.example.soundplayer.commons.constants.Constants
import com.example.soundplayer.commons.extension.exibirToast
import com.example.soundplayer.databinding.FragmentMainBinding
import com.example.soundplayer.model.PlayList
import com.example.soundplayer.model.Sound
import com.example.soundplayer.model.SoundList
import com.example.soundplayer.presentation.SoundViewModel
import com.example.soundplayer.presentation.adapter.PlayListAdapter
import com.example.soundplayer.presentation.adapter.SoundAdapter
import com.example.soundplayer.presentation.viewmodel.PlayListViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class MainFragment : Fragment() {

    private  val binding by lazy {
        FragmentMainBinding.inflate(layoutInflater)
    }

    private val listSoundFromContentProvider = mutableSetOf<Sound>()
    lateinit var myMenuProvider: MenuProvider
    var cont = 0
    private lateinit var gerenciarPermissoes : ActivityResultLauncher<Array<String>>
    private lateinit var  adapterSound : SoundAdapter
    private lateinit var  playListAdapter: PlayListAdapter
    private val  soundViewModel by activityViewModels<SoundViewModel>()
    private val playListViewModel by activityViewModels<PlayListViewModel>()
    private var isLoading = true
    private var positonPlayListToScrol = 0

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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // createReceiver()
        binding.btnFindSounds.setOnClickListener {
            requestPermission()
        }

        binding.fabAddPlayList.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_bottomSheetFragment)
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setupToolbar()
        initAdapter()
        getPermissions()
        observersViewModel()

        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        CoroutineScope(Dispatchers.Main).launch {
            soundViewModel.readPreferences()
        }
    }



      private fun setupToolbar() {

          //  setSupportActionBar(binding.includeSelectItem.toolbarSelecrionItemsMaterial)
          myMenuProvider = MyMenuProvider()
      }

      override fun onStart() {
          super.onStart()

          Log.i("play_", "on Start")

      }

      override fun onResume() {
          super.onResume()
      }

      private  fun observersViewModel(){

          soundViewModel.userDataPreferecence.observe(viewLifecycleOwner){userDataPreference->
              if (userDataPreference != null){
                  userDataPreference.idPreference?.let {
                      playListViewModel.findPlayListById(it)
                  }
                 positonPlayListToScrol =  userDataPreference.postionPreference
              }
          }


          soundViewModel.isPlayingObserver.observe(viewLifecycleOwner){ isPlaying ->
              if (isPlaying){
                  requireActivity().exibirToast("Esta tocando")

                    soundViewModel.currentPlayList.observe(requireActivity()){currentPlayList->
                        if (currentPlayList != null && isPlaying){
                            playListAdapter.getCurrentPlayListPlayind(
                                playList = currentPlayList,
                                playIng = isPlaying
                            )
                        }

                    }

                    soundViewModel.actualSound.observe(requireActivity()){ soundLiveData->
                        Log.i("INFO_", "Main:${soundLiveData.title} ${cont++}")
                        //TODO VERIFICAR CHAMAS MULTIPLAS
                        adapterSound.getActualSound(soundLiveData)
                        binding.rvSound.scrollToPosition(soundViewModel.currentItem)
                    }
                }
          }

          playListViewModel.playLists.observe(requireActivity()){listOfplayListObservable->
              playListAdapter.addPlayList(listOfplayListObservable)
              isLoading =false
          }

          playListViewModel.uniquePlayList.observe(viewLifecycleOwner){uniquePlayListWithSongs->
                soundViewModel.updatePlayList(uniquePlayListWithSongs.listSound)
                adapterSound.getPlayList(uniquePlayListWithSongs)
               binding.rvSound.scrollToPosition(positonPlayListToScrol)
                playListAdapter.setLastOpenPlayListBorder(uniquePlayListWithSongs.idPlayList!!)
         }

          playListViewModel.soundListBd.observe(viewLifecycleOwner){listSound->
                if (listSound.isNotEmpty()){
                    val playList =  PlayList(
                        idPlayList = null,
                        name= Constants.ALL_MUSIC_NAME,
                        listSound =  listSound.toMutableSet(),
                        currentMusicPosition = 0
                    )
                    playListViewModel.savePlayList(playList)
                }
          }
      }

      private fun getPermissions(){
           gerenciarPermissoes =  registerForActivityResult(
               ActivityResultContracts
                 .RequestMultiplePermissions()){permission: Map<String, Boolean> ->

                 if (!permission.values.contains(false)){
                      verifyPermissions(true)
                 }else{
                     verifyPermissions(false)
                 }
           }

           requestPermission()

      }

      private fun verifyPermissions(isPermitted : Boolean) {
          if (!isPermitted) {
              binding.btnFindSounds.visibility = View.VISIBLE
              binding.txvSoundNotFound.visibility = View.VISIBLE
              binding.linearMusics.visibility = View.GONE
              binding.fabAddPlayList.visibility = View.GONE
              requireActivity().exibirToast("Permissão necessaria para carragar as músicas")

          } else {
              binding.btnFindSounds.visibility = View.GONE
              binding.txvSoundNotFound.visibility = View.GONE
              binding.linearMusics.visibility = View.VISIBLE
              binding.fabAddPlayList.visibility = View.VISIBLE
              getMusicFromContentProvider()
              if (listSoundFromContentProvider.isNotEmpty()) playListViewModel.saveAllSoundsByContentProvider(
                  listSoundFromContentProvider
              )
              playListViewModel.getAllPlayList()
          }
      }

      private fun requestPermission(){
          gerenciarPermissoes.launch(listPermission.toTypedArray())
      }

      private fun initAdapter() {
          adapterSound = SoundAdapter(
             soundViewModel =  soundViewModel,
              isUpdateList = {isUpdate-> updateViewWhenMenuChange(isUpdate) },
              initNewFragment = {
                  findNavController().navigate(R.id.action_mainFragment_to_soundPlayingFragment)
              }
              )

          binding.rvSound.adapter = adapterSound
          binding.rvSound.layoutManager = LinearLayoutManager( requireActivity(),LinearLayoutManager.VERTICAL,false)

          playListAdapter = PlayListAdapter(

              onclick = { playListChoseByUser -> adapterSound.getPlayList(playListChoseByUser)},
              onDelete = {playList -> playListViewModel.deletePlayList(playList)},
              onEdit = {playList -> playListViewModel.updateNamePlayList(playList) }
          )
          binding.idRvFavoriteList.adapter = playListAdapter
          binding.idRvFavoriteList.layoutManager = LinearLayoutManager( requireActivity(),LinearLayoutManager.HORIZONTAL,false)

      }

      private fun updateViewWhenMenuChange(isUpdate: Boolean) {
          if (isUpdate) {
             requireActivity().addMenuProvider(myMenuProvider)
              binding.include.toolbarPrincipal.visibility = View.GONE
              binding.includeSelectItem.toolbarSelecrionItems.visibility = View.VISIBLE
              binding.fabAddPlayList.isVisible = false
              binding.includeSelectItem.backButton.setOnClickListener {
                  adapterSound.clearSoundListSelected()
              }
          } else {
              requireActivity().removeMenuProvider(myMenuProvider)
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
          val cursor =requireActivity().contentResolver.query(
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
                  val duarution = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                  val path  = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
                  val title = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)

                  while (cursor.moveToNext()){

                      val idMedia = cursor.getLong(id)
                      val albumidMedia = cursor.getLong(albumid)
                      val mediaUriAlbum  = ContentUris.withAppendedId(
                          Uri.parse("content://media/external/audio/albumart"),albumidMedia)

                      val mediaUri  = ContentUris.withAppendedId(
                          MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,idMedia)

                      val sound = Sound(
                          idSound = null,
                          path = cursor.getString(path),
                          duration =cursor.getInt(duarution).toString(),
                          title= cursor.getString(title),
                          uriMedia = mediaUri,
                          uriMediaAlbum = mediaUriAlbum
                      )

                      if (File(sound.path).exists()){
                          if (!listSoundFromContentProvider.contains(sound)){
                              listSoundFromContentProvider.add(sound)
                          }
                      }
                  }
                  cursor.close()
              }catch (nullPointer : NullPointerException){
                  nullPointer.printStackTrace()
                  requireActivity().exibirToast( "Null ${nullPointer.printStackTrace()}")
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
              binding.txvQuantidadeMusics.text = "total de  musicas ${listSoundFromContentProvider.size}"
          }
      }


      override fun onStop() {
          CoroutineScope(Dispatchers.Main).launch{
              soundViewModel.savePreference()
          }
          Log.i("play_", "onStop")
          super.onStop()
      }
       override fun onDestroy() {
        //   unregisterReceiver(myReceiver)
        //  soundViewModel.destroyPlayer()
           super.onDestroy()
      }

    inner class  MyMenuProvider() : MenuProvider {
        @SuppressLint("SuspiciousIndentation")
        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            menuInflater.inflate(R.menu.sound_menu,menu)
            val pairPlayList = adapterSound.getSoundSelecionados()
            if (pairPlayList.first.toInt() == 1){
                menu.removeItem(R.id.id_remove)
            }

        }

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean {

            val pairPlayList  =  adapterSound.getSoundSelecionados()

            return  when(menuItem.itemId){

                R.id.id_update->{
                    val sound = SoundList(0,pairPlayList.second)
                    val  bottomSheetFragment =SelectPlayListDialogFragment()
                    val bundle = bundleOf("listSound" to sound)
                    bottomSheetFragment.arguments = bundle
                    findNavController().navigate(R.id.action_mainFragment_to_selectPlayListDialogFragment)
                    //bottomSheetFragment.show(supportFragmentManager,"tag")
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