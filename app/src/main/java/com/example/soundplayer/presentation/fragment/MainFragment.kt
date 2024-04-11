package com.example.soundplayer.presentation.fragment

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.soundplayer.R
import com.example.soundplayer.commons.constants.Constants
import com.example.soundplayer.commons.extension.exibirToast
import com.example.soundplayer.databinding.FragmentMainBinding
import com.example.soundplayer.model.DataSoundPlayListToUpdate
import com.example.soundplayer.model.PlayList
import com.example.soundplayer.model.Sound
import com.example.soundplayer.model.SoundList
import com.example.soundplayer.presentation.MyContetntProvider
import com.example.soundplayer.presentation.adapter.PlayListAdapter
import com.example.soundplayer.presentation.adapter.SoundAdapter
import com.example.soundplayer.presentation.viewmodel.PlayListViewModel
import com.example.soundplayer.presentation.viewmodel.PreferencesViewModel
import com.example.soundplayer.presentation.viewmodel.SoundViewModel
import com.example.soundplayer.presentation.viewmodel.StatePrefre
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

//TODO bug menu so aparece ao abrir a play list
class MainFragment : Fragment() {

    private  lateinit var  binding : FragmentMainBinding

    private var listSoundFromContentProvider = setOf<Sound>()
    private lateinit var myMenuProvider: MenuProvider
    var cont = 0
    private lateinit var gerenciarPermissoes : ActivityResultLauncher<Array<String>>
    private lateinit var  adapterSound : SoundAdapter
    private lateinit var  playListAdapter: PlayListAdapter
    private val  soundViewModel by activityViewModels<SoundViewModel>()
    private val playListViewModel by activityViewModels<PlayListViewModel>()
    private val preferencesViewModel by activityViewModels<PreferencesViewModel>()
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
        myMenuProvider = MyMenuProvider()
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainBinding.inflate(inflater,container,false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initAdapter()
        getPermissions()
        observersViewModel()
        initBindigs()
        binding.toolbarSelecrionItemsMaterial.title =""

        CoroutineScope(Dispatchers.Main).launch {
            soundViewModel.readPreferences()
        }
       setupToolbar()

    }

    override fun onStart() {
        preferencesViewModel.readSizeTextMusicPreference()
        super.onStart()
    }

    private fun initBindigs() {
        binding.btnFindSounds.setOnClickListener {
            requestPermission()
        }

        binding.fabAddPlayList.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_bottomSheetFragment)
        }
    }

    private fun setupToolbar() {
          val activity  = activity as AppCompatActivity
          activity.setSupportActionBar(binding.toolbarSelecrionItemsMaterial)
      }


      private  fun observersViewModel(){
          playListViewModel.listSoundUpdate.observe(viewLifecycleOwner){ pairOFListSound->
              soundViewModel.updatePlayList(pairOFListSound)
          }

          soundViewModel.userDataPreferecence.observe(viewLifecycleOwner){userDataPreference->
              if (userDataPreference.idPreference != null  ){
                  playListViewModel.findPlayListById(userDataPreference.idPreference)
                  positonPlayListToScrol =  userDataPreference.postionPreference
              }
          }

          soundViewModel.isPlayingObserver.observe(viewLifecycleOwner){ isPlaying ->
              if (isPlaying){
                    soundViewModel.currentPlayList.observe(viewLifecycleOwner){currentPlayList->
                        if (currentPlayList != null && isPlaying){
                            playListAdapter.getCurrentPlayListPlayind(
                                playList = currentPlayList
                            )
                        }
                    }

                    soundViewModel.actualSound.observe(requireActivity()){ soundLiveDataActual->
                        Log.i("INFO_", "Main:${soundLiveDataActual.title} ${cont++}")
                        //TODO VERIFICAR CHAMAdasS MULTIPLAS
                        adapterSound.getActualSound(soundLiveDataActual)
                        binding.rvSound.scrollToPosition(soundViewModel.currentItem)
                    }
                }
              adapterSound.updateAnimationWithPlaying(isPlaying)
              playListAdapter.updateAnimationWhenPlayerPause(isPlaying)
          }



          playListViewModel.clickedPlayList.observe(viewLifecycleOwner){uniquePlayListWithSongs->

              binding.rvSound.scrollToPosition(positonPlayListToScrol)

              playListAdapter.setLastOpenPlayListBorder(uniquePlayListWithSongs.idPlayList ?: 0)
              updateViewWhenPlayListIsEmpty(uniquePlayListWithSongs)
         }

          playListViewModel.soundListBd.observe(viewLifecycleOwner){listSound->
                if (listSound.isNotEmpty()){

                    playListViewModel.playLists.observe(viewLifecycleOwner){listOfplayListObservable->
                        val playList =  PlayList(
                            idPlayList = 0,
                            name= Constants.ALL_MUSIC_NAME,
                            listSound =  listSoundFromContentProvider.toMutableSet(),
                            currentMusicPosition = 0
                        )
                        listOfplayListObservable.add(0,playList)

                        playListAdapter.addPlayList(listOfplayListObservable)
                    }

                   // playListViewModel.savePlayList(playList)
                }
          }

          preferencesViewModel.sizeTextTitleMusic.observe(viewLifecycleOwner){ statePreference ->
               when(statePreference){
                   is StatePrefre.Sucess<*>->{
                        adapterSound.sizeTitleMusic = statePreference.succssResult as Float
                   }
                   is  StatePrefre.Error ->{
                        requireActivity().exibirToast(statePreference.mensagem)
                   }
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
              onDelete = {idPlayList,intSoundPair ->
                 if (idPlayList == 0L){
                     requireContext().exibirToast("Audio não pode ser deletado da play list All musics")
                 }
                else {
                     val dataSoundToUpdate = DataSoundPlayListToUpdate(
                         idPlayList, listOf(intSoundPair.first), setOf(intSoundPair.second)
                     )
                     playListViewModel.removePlaySoundFromPlayList(dataSoundToUpdate)
                 }
              },
              soundViewModel =  soundViewModel,
              isUpdateList = {isUpdate->
                  updateViewWhenMenuChange(isUpdate)
                             },
              onClickInitNewFragment = {
                  findNavController().navigate(R.id.action_mainFragment_to_soundPlayingFragment)
               }
              )

          binding.rvSound.adapter = adapterSound
          binding.rvSound.layoutManager = LinearLayoutManager( requireActivity(),LinearLayoutManager.VERTICAL,false)

          playListAdapter = PlayListAdapter(
              onclick =  { playListChoseByUser ->
                           updateViewWhenPlayListIsEmpty(playListChoseByUser)
                         },
              onDelete = {playList ->
                            playListViewModel.deletePlayList(playList)
                           binding.txvNoMusicAtPlaylist.isVisible =false
                           binding.rvSound.isVisible= false
                         },
              onEdit =   {playList -> playListViewModel.updateNamePlayList(playList) }
          )
          binding.idRvFavoriteList.adapter = playListAdapter
          binding.idRvFavoriteList.layoutManager = LinearLayoutManager( requireActivity(),LinearLayoutManager.HORIZONTAL,false)

      }

    private fun updateViewWhenPlayListIsEmpty(playListChoseByUser: PlayList) {
        if (playListChoseByUser.listSound.isNotEmpty()) {
            adapterSound.getPlayList(playListChoseByUser)
            binding.rvSound.isVisible = true
            binding.txvNoMusicAtPlaylist.isVisible = false
        } else {
            binding.rvSound.isVisible = false
            binding.txvNoMusicAtPlaylist.isVisible = true
        }
    }

    private fun updateViewWhenMenuChange(isUpdate: Boolean) {

         requireActivity().removeMenuProvider(myMenuProvider)
          requireActivity().addMenuProvider(myMenuProvider)
          if (isUpdate) {
              binding.fabAddPlayList.isVisible = false
              binding.mainFragmentBackButton.setOnClickListener {
                  adapterSound.clearSoundListSelected()
              }
          } else {
              binding.fabAddPlayList.isVisible = true
          }

      }


      private fun getMusicFromContentProvider(){
        listSoundFromContentProvider  = MyContetntProvider.createData(requireContext())
              .getListOfSound(requireContext())
              .listSoundFromContentProvider
          verifyListIsEmpty()
      }

       private fun verifyListIsEmpty(){
          if (listSoundFromContentProvider.isEmpty()){
              binding.linearMusics.visibility =View.GONE
              binding.txvSoundNotFound.visibility =View.VISIBLE
          }else{
              binding.linearMusics.visibility =View.VISIBLE
              binding.txvSoundNotFound.visibility =View.GONE
              binding.txvQuantidadeMusics.text = "Total de músicas ${listSoundFromContentProvider.size}"
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
        super.onDestroy()
        playListViewModel.listSoundUpdate.removeObservers(this)
    }


   inner class  MyMenuProvider() : MenuProvider {
        @SuppressLint("SuspiciousIndentation")
        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {

            val pairPlayList = adapterSound.getSoundSelecionados()

            if (pairPlayList.second.isEmpty()){
                binding.txvTitleToolbar.text = getString(R.string.app_name)
                binding.mainFragmentBackButton.isVisible =false
                menuInflater.inflate(R.menu.menu_toolbar,menu)

            }else{
                binding.txvTitleToolbar.text= getString(R.string.selecionar_itens)
                binding.mainFragmentBackButton.isVisible =true
                menuInflater.inflate(R.menu.update_toolbar_menu,menu)
            }

        }

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean {

            val pairPlayList  =  adapterSound.getSoundSelecionados()

            return  when(menuItem.itemId){
                R.id.menu_config->{
                     findNavController().navigate(R.id.action_mainFragment_to_settingsFragment)
                    true
                }


                R.id.id_update->{
                    val sound = SoundList(0,pairPlayList.second)
                    val args  =  MainFragmentDirections.actionMainFragmentToSelectPlayListDialogFragment(sound)
                    findNavController().navigate(args)
                    true
                }

                else ->false
            }

        }
    }

}