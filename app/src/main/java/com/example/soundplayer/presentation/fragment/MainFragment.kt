package com.example.soundplayer.presentation.fragment

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.media3.common.PlaybackException
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.soundplayer.R
import com.example.soundplayer.commons.constants.Constants
import com.example.soundplayer.commons.extension.snackBarSound
import com.example.soundplayer.commons.permission.Permission
import com.example.soundplayer.databinding.FragmentMainBinding
import com.example.soundplayer.model.DataSoundPlayListToUpdate
import com.example.soundplayer.model.PlayList
import com.example.soundplayer.model.SoundList
import com.example.soundplayer.presentation.MyContetntProvider
import com.example.soundplayer.presentation.adapter.PlayListAdapter
import com.example.soundplayer.presentation.adapter.SoundAdapter
import com.example.soundplayer.presentation.viewmodel.PlayListViewModel
import com.example.soundplayer.presentation.viewmodel.PreferencesViewModel
import com.example.soundplayer.presentation.viewmodel.SoundViewModel
import com.example.soundplayer.presentation.viewmodel.StatePrefre

class MainFragment : Fragment() {

    private  lateinit var  binding : FragmentMainBinding
    private lateinit var myMenuProvider: MenuProvider
    var cont = 0
    private lateinit var gerenciarPermissoes : ActivityResultLauncher<Array<String>>
    private lateinit var  adapterSound : SoundAdapter
    private lateinit var  playListAdapter: PlayListAdapter
    private val  soundViewModel by activityViewModels<SoundViewModel>()
    private val playListViewModel by activityViewModels<PlayListViewModel>()
    private val preferencesViewModel by activityViewModels<PreferencesViewModel>()
    private var actualPlayListPLayingMain :PlayList? =null
    val listPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        setOf(
            android.Manifest.permission.READ_MEDIA_AUDIO,
            android.Manifest.permission.FOREGROUND_SERVICE,
            android.Manifest.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK,
        )
    } else {
        setOf(
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
        getPermissions()
        binding = FragmentMainBinding.inflate(inflater,container,false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observersViewModel()

        initAdapter()
        initBindigs()
        binding.toolbarSelecrionItemsMaterial.title =""
        preferencesViewModel.readAllPrefference()

        setupToolbar()
    }

    override fun onStart() {
        soundViewModel.getActualPlayList()
       preferencesViewModel.readSizeTextMusicPreference()
       // preferencesViewModel.readAllPrefference()
        val sound = soundViewModel.actualSound?.value
        if (sound != null){
            adapterSound.getActualSound(sound)
        }
        super.onStart()
    }

    private fun initBindigs() {
        binding.btnFindSounds.setOnClickListener {
            Permission.requestPermission(requireActivity(),gerenciarPermissoes,listPermission)
        }

        binding.fabAddPlayList.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_bottomSheetFragment)
        }
    }

    private fun setupToolbar() {
        val activity  = activity as AppCompatActivity
        activity.setSupportActionBar(binding.toolbarSelecrionItemsMaterial)
    }
    @SuppressLint("StringFormatMatches")
    private  fun observersViewModel(){
        playListViewModel.hasPermission.observe(viewLifecycleOwner){hasPermission ->
             if (!hasPermission) {
                 showHideViewItems(false)
                //requireActivity().exibirToast(getString(R.string.permiss_o_necessaria_para_carragar_as_m_sicas))
         } else {
            getMusicFromContentProvider()
             playListViewModel.listSize.observe(viewLifecycleOwner){sizeListSoundOnBd->
                 binding.txvQuantidadeMusics.text = getString(R.string.total_de_musicas, sizeListSoundOnBd)
                 if (sizeListSoundOnBd <= 0){
                     showHideViewItems(false)
                 }else{
                     playListViewModel.getAllPlayList()
                     showHideViewItems(true)
                 }
             }
         }

         }

        preferencesViewModel.uiStatePreffs.observe(viewLifecycleOwner){ uiStatePref->
            if (uiStatePref.idPreference != null){
                playListViewModel.findPlayListById(uiStatePref.idPreference)
            }

        }

        soundViewModel.isPlayingObserver.observe(viewLifecycleOwner){ isPlaying ->
            if (isPlaying){
                soundViewModel.currentPlayList.observe(viewLifecycleOwner){ actualPLayiingPlayList->

                    if (actualPLayiingPlayList != null){
                        preferencesViewModel.savePlayListIdPlayList(actualPLayiingPlayList.idPlayList!!)

                        playListAdapter.getCurrentPlayListPlaying(
                            playList = actualPLayiingPlayList
                        )
                        actualPlayListPLayingMain = actualPLayiingPlayList
                    }
                }

                soundViewModel.actualSound?.observe(viewLifecycleOwner){ soundLiveDataActual->
                   // Log.i("INFO_", "Main:${soundLiveDataActual.title} ${cont++}")
                    //TODO VERIFICAR CHAMAdasS MULTIPLAS
                    adapterSound.getActualSound(soundLiveDataActual)
                    val currentPositionSound = soundViewModel.getCurrentPositionSound()
                    binding.rvSound.scrollToPosition(currentPositionSound)
                    preferencesViewModel.savePlayListIdCurrenPositionSound(currentPositionSound)
                }
            }
            adapterSound.updateAnimationWithPlaying(isPlaying)
            playListAdapter.updateAnimationWhenPlayerPause(isPlaying)

        }

        playListViewModel.playListsWithSounds.observe(viewLifecycleOwner){ listOfplayListObservable->
            playListAdapter.addPlayList(listOfplayListObservable)
        }

        playListViewModel.clickedPlayList.observe(viewLifecycleOwner){touchedPlayList->
            updateViewWhenPlayListIsEmpty(touchedPlayList)
            binding.rvSound.scrollToPosition(touchedPlayList.currentMusicPosition)
            playListAdapter.setLastOpenPlayListBorder(touchedPlayList.idPlayList ?: -1)
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

        preferencesViewModel.sizeTextTitleMusic.observe(viewLifecycleOwner){ statePreference ->
            when(statePreference){
                is StatePrefre.Sucess<*>->{
                    adapterSound.sizeTitleMusic = statePreference.succssResult as Float
                }
                is  StatePrefre.Error ->{
                    requireView().snackBarSound(
                        messages = statePreference.mensagem,backGroundColor = Color.RED,
                        onClick = null,
                        actionText = null,)
                }
            }
        }

        soundViewModel.playBackError.observe(viewLifecycleOwner){playbackError->
            if (playbackError != null){
                when(playbackError.code){
                    PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND->{
                        requireView().snackBarSound(
                            messages = playbackError.message ?: "",
                            backGroundColor = Color.RED,
                            onClick = null,
                            actionText = null,)
                    }

                    PlaybackException.ERROR_CODE_PARSING_CONTAINER_UNSUPPORTED->{
                        requireView().snackBarSound(
                            messages = playbackError.message ?: "",
                            backGroundColor = Color.RED,
                            onClick = null,
                            actionText = null,)
                    }
                }

            }
        }
    }

    private fun showHideViewItems(showListSoundtItem :Boolean) {
        if (showListSoundtItem){
            binding.LineartLayoutEmptySound.isVisible = false
            binding.linearMusics.visibility = View.VISIBLE
            binding.rvSound.isVisible = true
            binding.fabAddPlayList.visibility = View.VISIBLE
            binding.txvQuantidadeMusics.visibility = View.VISIBLE
            binding.txvPlayList.visibility = View.VISIBLE
            binding.txvSounds.visibility = View.VISIBLE
        }else{
            binding.LineartLayoutEmptySound.isVisible = true
            binding.rvSound.isVisible = true
            binding.linearMusics.visibility = View.GONE
            binding.fabAddPlayList.visibility = View.GONE
            binding.txvQuantidadeMusics.visibility = View.GONE
            binding.txvPlayList.visibility = View.GONE
            binding.txvSounds.visibility = View.GONE
        }

    }

    private fun getPermissions(){
        gerenciarPermissoes =  registerForActivityResult(
            ActivityResultContracts
                .RequestMultiplePermissions()){permission: Map<String, Boolean> ->

            val isPepermited = Permission.getPermissions(permission)
            playListViewModel.verifyPermissions( isPepermited)
        }
    }

    override fun onResume() {
        val listPermissions = Permission.chekPerMission(this.requireActivity(),listPermission)
        if (listPermissions.isNotEmpty()){
            showHideViewItems(false)
        }else{
            playListViewModel.verifyPermissions( true)
        }
        super.onResume()
    }

    private fun initAdapter() {
        adapterSound = SoundAdapter(
            onDelete = {idPlayList,intSoundPair ->
                val dataSoundToUpdate = DataSoundPlayListToUpdate(
                    idPlayList, listOf(intSoundPair.first), setOf(intSoundPair.second)
                )
                playListViewModel.removePlaySoundFromPlayList(dataSoundToUpdate)
            },
            soundViewModel =  soundViewModel,
            isUpdateList = {isUpdate->
               updateViewWhenMenuChange(isUpdate)
            },
            onClickInitNewFragment = {playList->
                soundViewModel.setPlayListToPlay(playList)
                findNavController().navigate(R.id.action_mainFragment_to_soundPlayingFragment)
            }
        )

        binding.rvSound.adapter = adapterSound
        binding.rvSound.layoutManager = LinearLayoutManager( requireActivity(),LinearLayoutManager.VERTICAL,false)

        playListAdapter = PlayListAdapter(
            onclick =  { playListChoseByUser ->
                playListViewModel.findPlayListById(playListChoseByUser.idPlayList ?: 1)
            },
            onDelete = {playList ->
                playListViewModel.deletePlayList(playList)
                binding.txvNoMusicAtPlaylist.isVisible =false
                binding.rvSound.isVisible= false
            },
            onEdit =   {playList -> playListViewModel.updateNamePlayList(playList) },
            onAddSoundAllMusic = {
                findNavController().navigate(R.id.action_mainFragment_to_updateAllMusicBottomSheetFragment)
            }
        )
        binding.idRvPlaylist.adapter = playListAdapter
        binding.idRvPlaylist.layoutManager = LinearLayoutManager( requireActivity(),LinearLayoutManager.HORIZONTAL,false)

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
       val listSoundFromContentProvider = MyContetntProvider
            .createData(requireContext())
            .getListOfSound(requireContext())
            .listSoundFromContentProvider.toMutableSet()

       playListViewModel.saveAllSoundsByContentProvider(listSoundFromContentProvider)

    }

    override fun onStop() {
        super.onStop()
    }

    inner class  MyMenuProvider() : MenuProvider {
        @SuppressLint("SuspiciousIndentation")
        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            val pairPlayList = adapterSound.getSoundSelecionados()

            if (pairPlayList.second.isEmpty()){
                binding.txvTitleToolbar.text = context?.let { ContextCompat.getString(it,R.string.app_name) }
                binding.mainFragmentBackButton.isVisible =false
                menuInflater.inflate(R.menu.menu_toolbar,menu)
            }else{
                binding.txvTitleToolbar.text= context?.let { ContextCompat.getString(it,R.string.app_name) }
                binding.mainFragmentBackButton.isVisible =true
                menuInflater.inflate(R.menu.update_toolbar_menu,menu)
            }

        }

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean {

            val pairPlayList  =  adapterSound.getSoundSelecionados()

            return  when(menuItem.itemId){
                R.id.id_search_musica ->{
                    findNavController().navigate(R.id.action_mainFragment_to_searchFragment)
                    true
                }
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