package com.example.soundplayer.presentation.fragment

import android.annotation.SuppressLint
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
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.soundplayer.R
import com.example.soundplayer.commons.constants.Constants
import com.example.soundplayer.commons.extension.exibirToast
import com.example.soundplayer.commons.permission.Permission
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

class MainFragment : Fragment() {

    private  lateinit var  binding : FragmentMainBinding

    private var listSoundFromContentProvider = mutableSetOf<Sound>()
    private lateinit var myMenuProvider: MenuProvider
    var cont = 0
    private lateinit var gerenciarPermissoes : ActivityResultLauncher<Array<String>>
    private lateinit var  adapterSound : SoundAdapter
    private lateinit var  playListAdapter: PlayListAdapter
    private val  soundViewModel by activityViewModels<SoundViewModel>()
    private val playListViewModel by activityViewModels<PlayListViewModel>()
    private val preferencesViewModel by activityViewModels<PreferencesViewModel>()
    private var actulPlayListPLayingMain :PlayList? =null


    private val listPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
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
            Permission.requestPermission(gerenciarPermissoes,listPermission)
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
        soundViewModel.userDataPreferecence.observe(viewLifecycleOwner){userDataPreference->
            if (userDataPreference.idPreference != null  ){
                playListViewModel.findPlayListById(userDataPreference.idPreference)
            }
        }

        soundViewModel.isPlayingObserver.observe(viewLifecycleOwner){ isPlaying ->

            if (isPlaying){
                //TODO PlayList vindo nula
                soundViewModel.currentPlayList.observe(viewLifecycleOwner){actualPLayiingPlayList->
                    if (actualPLayiingPlayList != null){
                        playListAdapter.getCurrentPlayListPlaying(
                            playList = actualPLayiingPlayList
                        )
                        actulPlayListPLayingMain = actualPLayiingPlayList
                    }
                }

                soundViewModel.actualSound?.observe(viewLifecycleOwner){ soundLiveDataActual->
                   // Log.i("INFO_", "Main:${soundLiveDataActual.title} ${cont++}")
                    //TODO VERIFICAR CHAMAdasS MULTIPLAS
                    adapterSound.getActualSound(soundLiveDataActual)
                    binding.rvSound.scrollToPosition(soundViewModel.getCurrentPositionSound())
                }
            }
            adapterSound.updateAnimationWithPlaying(isPlaying)
            playListAdapter.updateAnimationWhenPlayerPause(isPlaying)

        }

        playListViewModel.playListsWithSounds.observe(viewLifecycleOwner){ listOfplayListObservable->
            playListAdapter.addPlayList(listOfplayListObservable)
        }

        playListViewModel.clickedPlayList.observe(viewLifecycleOwner){touchedPlayList->

            binding.rvSound.scrollToPosition(touchedPlayList.currentMusicPosition)
            playListAdapter.setLastOpenPlayListBorder(touchedPlayList.idPlayList ?: 0)
            updateViewWhenPlayListIsEmpty(touchedPlayList)
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
                    requireActivity().exibirToast(statePreference.mensagem)
                }
            }
        }
    }

    private fun getPermissions(){
        gerenciarPermissoes =  registerForActivityResult(
            ActivityResultContracts
                .RequestMultiplePermissions()){permission: Map<String, Boolean> ->

            val isPepermited  =Permission.getPermissions(permission)
            verifyPermissions(isPepermited)
        }

       Permission.requestPermission(gerenciarPermissoes,listPermission)

    }

    private fun verifyPermissions(isPermitted : Boolean) {
        if (!isPermitted) {
            binding.LineartLayoutEmptySound.isVisible= true
            binding.linearMusics.visibility = View.GONE
            binding.fabAddPlayList.visibility = View.GONE
            binding.txvQuantidadeMusics.visibility = View.GONE
            binding.txvPlayList.visibility = View.GONE
            binding.txvSounds.visibility = View.GONE
            requireActivity().exibirToast(getString(R.string.permiss_o_necessaria_para_carragar_as_m_sicas))
        } else {
            playListViewModel.listSize.observe(viewLifecycleOwner){sizeListSoundOnBd->
                binding.txvQuantidadeMusics.text = getString(R.string.total_de_musicas, sizeListSoundOnBd)

                if (sizeListSoundOnBd != 0){
                    binding.LineartLayoutEmptySound.isVisible= false
                    binding.linearMusics.visibility = View.VISIBLE
                    binding.fabAddPlayList.visibility = View.VISIBLE
                    binding.txvQuantidadeMusics.visibility = View.VISIBLE
                    binding.txvPlayList.visibility = View.VISIBLE
                    binding.txvSounds.visibility = View.VISIBLE
                }else{
                    getMusicFromContentProvider()
                    if (listSoundFromContentProvider.isNotEmpty()) playListViewModel.saveAllSoundsByContentProvider(
                        listSoundFromContentProvider
                    )
                }
                playListViewModel.getAllPlayList()
            }
        }
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
            onClickInitNewFragment = {
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
            onEdit =   {playList -> playListViewModel.updateNamePlayList(playList) }
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
        listSoundFromContentProvider = MyContetntProvider
            .createData(requireContext())
            .getListOfSound(requireContext())
            .listSoundFromContentProvider.toMutableSet()
        verifyListIsEmpty(listSoundFromContentProvider)
    }

    private fun verifyListIsEmpty(listSound :MutableSet<Sound>){
        if (listSound.isEmpty()){
            binding.linearMusics.visibility =View.GONE
            binding.txvSoundNotFound.visibility =View.VISIBLE
        }else{
            binding.linearMusics.visibility =View.VISIBLE
            binding.txvSoundNotFound.visibility =View.GONE
        }
    }

    override fun onStop() {
        CoroutineScope(Dispatchers.Main).launch{
            soundViewModel.savePreference()
        }
        super.onStop()
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