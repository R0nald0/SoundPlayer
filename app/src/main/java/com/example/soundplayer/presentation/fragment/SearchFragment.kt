package com.example.soundplayer.presentation.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.asLiveData
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.soundplayer.R
import com.example.soundplayer.databinding.FragmentSearchBinding
import com.example.soundplayer.model.Sound
import com.example.soundplayer.presentation.adapter.SearchAdapter
import com.example.soundplayer.presentation.viewmodel.PlayListViewModel
import com.example.soundplayer.presentation.viewmodel.SearchViewModel
import com.example.soundplayer.presentation.viewmodel.SoundViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SearchFragment : Fragment() {

    private  val binding by lazy {
        FragmentSearchBinding.inflate(layoutInflater)
    }
    private lateinit var  searchAdapter :SearchAdapter
    private  val searchSoundViewModel by viewModels<SearchViewModel>()
    private  val playerViewModel by viewModels<SoundViewModel>()
    private val playListViewModel  by viewModels<PlayListViewModel>()

    private var soundChosed : Sound? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        initObservers()
        initSearchView()
        inicializarAdapter()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    private fun initObservers() {
        playListViewModel.clickedPlayList.observe(viewLifecycleOwner){chosedPlayLis->
            chosedPlayLis.let {playList ->
                if (soundChosed != null){
                    val  position = playList.listSound.indexOf(soundChosed)
                    playList.currentMusicPosition = position
                }
                 playerViewModel.setPlayListToPlay(playList = playList)
                findNavController().navigate(R.id.soundPlayingFragment)
            }
        }

     searchSoundViewModel.loader.asLiveData().observe(viewLifecycleOwner){loader->
         if (loader){
             binding.rvItemSoundSearch.isVisible =false
             binding.txvSoundEmpyt.isVisible =false
             binding.progressLoading.isVisible =true
         }else{
             binding.rvItemSoundSearch.isVisible =true
             binding.progressLoading.isVisible =false
             binding.txvSoundEmpyt.isVisible =true
         }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            searchSoundViewModel.uiState.flowWithLifecycle(lifecycle)
                .collect{uiState ->
                    if (uiState.listSoundsSearch.isNotEmpty()){
                        binding.rvItemSoundSearch.isVisible =true
                        binding.txvSoundEmpyt.isVisible =false
                        searchAdapter.getSoundOFSearch(uiState.listSoundsSearch)
                    }else{
                        searchAdapter.getSoundOFSearch(emptyList())
                        binding.rvItemSoundSearch.isVisible =false
                        binding.txvSoundEmpyt.isVisible =true
                    }
                }
        }
    }
    private fun initSearchView() {

         binding.searchView.queryHint ="Digite o título da música"
         binding.searchView.setOnCloseListener {
               findNavController().popBackStack()
             return@setOnCloseListener true
         }
         binding.searchView.setOnQueryTextListener(object :SearchView.OnQueryTextListener {

             override fun onQueryTextChange(newText: String?): Boolean {
                 if (newText != null){
                     searchSoundViewModel.findSoundBytitle(newText)
                 }
                 return true
             }

             override fun onQueryTextSubmit(title: String?): Boolean {
                 if (title != null){
                     searchSoundViewModel.findSoundBytitle(title)
                 }
                 return true
             }
         })
    }

    private fun inicializarAdapter() {
        searchAdapter = SearchAdapter(
            onTap = {sound,playlist->
                 playListViewModel.findPlayListById(playlist.idPlayList ?: 1)
                soundChosed = sound.sound
            },

        )
        binding.rvItemSoundSearch.apply {
            adapter =searchAdapter
            layoutManager = LinearLayoutManager(requireContext(),RecyclerView.VERTICAL,false)

        }
    }



}