package com.example.soundplayer.presentation.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.soundplayer.R
import com.example.soundplayer.commons.extension.exibirToast
import com.example.soundplayer.databinding.FragmentItemListDialogListDialogBinding
import com.example.soundplayer.model.PlayList
import com.example.soundplayer.model.Sound
import com.example.soundplayer.presentation.adapter.BottomPlayListAdapter
import com.example.soundplayer.presentation.viewmodel.PlayListViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class BottomSheetFragment : BottomSheetDialogFragment() {

    private val binding by  lazy {
        FragmentItemListDialogListDialogBinding.inflate(layoutInflater)
    }
   private  lateinit var  bottomSheetAdapter  : BottomPlayListAdapter
   private val playListViewModel by activityViewModels<PlayListViewModel>()
   private lateinit var createdSounds : Set<Sound>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding.edtPlayList.clearFocus()
        binding.textInputLayoutCratePlayList.clearFocus()
        binding.btnCreatePlayList.setOnClickListener {
              createdSounds  = bottomSheetAdapter.getSoundSelected()

              val namePlayList  = binding.edtPlayList.text.toString()
              if (namePlayList.isNotEmpty()){
                   if (createdSounds.isNotEmpty()){
                       savePlayList(namePlayList,createdSounds.toMutableSet())
                       dismiss()
                  }else{
                       requireContext().exibirToast("Você ainda não adicionao musicas a nova playlist")
                  }

              }else{
                   requireContext().exibirToast("Escolha um nome válido para sua e playlist ")
              }
        }

        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        bottomSheetAdapter = BottomPlayListAdapter()
        binding.rvMusics.adapter = bottomSheetAdapter
        binding.rvMusics.addItemDecoration(DividerItemDecoration( binding.root.context,RecyclerView.HORIZONTAL))
        binding.rvMusics.layoutManager = LinearLayoutManager(binding.root.context,LinearLayoutManager.VERTICAL,false)
        binding.btnCreatePlayList.text = getString(R.string.text_criar_playlist)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        observerLiveData()
    }

    override fun onStart() {
        super.onStart()
        playListViewModel.findAllSound()

    }


    private fun observerLiveData(){
         playListViewModel.soundListBd.observe(this){soundsDatabase->
              if (soundsDatabase.isNotEmpty()){
                  bottomSheetAdapter.getListSound(soundsDatabase.toSet())
              }
         }
    }

    private fun savePlayList(namePlayList :String, listSound : MutableSet<Sound>){
        val playList = PlayList(
            idPlayList = null,
            name = namePlayList,
            listSound = listSound,
            currentMusicPosition = 0,
        )
        playListViewModel.savePlayList(playList = playList)
    }
}