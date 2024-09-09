package com.example.soundplayer

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
import com.example.soundplayer.commons.extension.snackBarSound
import com.example.soundplayer.databinding.FragmentSearchBinding
import com.example.soundplayer.presentation.adapter.SearchAdapter
import com.example.soundplayer.presentation.viewmodel.SearchViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SearchFragment : Fragment() {

    private  val binding by lazy {
        FragmentSearchBinding.inflate(layoutInflater)
    }
    private lateinit var  searchAdapter :SearchAdapter
    private  val searchSoundViewModel by viewModels<SearchViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initObservers()
        initSearchView()
        inicializarAdapter()
    }

    private fun initObservers() {
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
            onTap = {sound->
                requireView().snackBarSound(
                    messages = sound.title
                )
            }
        )
        binding.rvItemSoundSearch.apply {
            adapter =searchAdapter
            layoutManager = LinearLayoutManager(requireContext(),RecyclerView.VERTICAL,false)

        }
    }


}