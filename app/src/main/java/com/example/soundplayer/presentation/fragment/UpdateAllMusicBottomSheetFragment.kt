package com.example.soundplayer.presentation.fragment

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.soundplayer.R
import com.example.soundplayer.commons.extension.exibirToast
import com.example.soundplayer.commons.extension.snackBarSound
import com.example.soundplayer.databinding.FragmentBottomAllmusicLayoutBinding
import com.example.soundplayer.databinding.FragmentItemListDialogListDialogBinding
import com.example.soundplayer.model.DataSoundPlayListToUpdate
import com.example.soundplayer.model.PlayList
import com.example.soundplayer.model.Sound
import com.example.soundplayer.presentation.MyContetntProvider
import com.example.soundplayer.presentation.adapter.BottomAllMusicUpdatePlayListAdapter
import com.example.soundplayer.presentation.adapter.BottomPlayListAdapter
import com.example.soundplayer.presentation.viewmodel.PlayListViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class UpdateAllMusicBottomSheetFragment : BottomSheetDialogFragment() {

    private val binding by  lazy {
        FragmentBottomAllmusicLayoutBinding.inflate(layoutInflater)
    }
   private  lateinit var  bottomSheetAdapter  : BottomAllMusicUpdatePlayListAdapter
   private val playListViewModel by activityViewModels<PlayListViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding.btnCreatePlayList.setOnClickListener {
                  val soundSelected = bottomSheetAdapter.getSoundSelected()
                  if (soundSelected.isNotEmpty()){
                      playListViewModel.updateSoundList(sounds = soundSelected)
                      dismiss()
                  }

             //TODO verificar quando list vir vazia

        }

        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        bottomSheetAdapter = BottomAllMusicUpdatePlayListAdapter()
        binding.rvMusics.adapter = bottomSheetAdapter
        binding.rvMusics.addItemDecoration(DividerItemDecoration( binding.root.context,RecyclerView.HORIZONTAL))
        binding.rvMusics.layoutManager = LinearLayoutManager(binding.root.context,LinearLayoutManager.VERTICAL,false)
        binding.btnCreatePlayList.text = "Adicionao músicas"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        observerLiveData()
    }

    override fun onStart() {
        super.onStart()
        val listSoundFromContentProvider = MyContetntProvider
            .createData(requireContext())
            .getListOfSound(requireContext())
            .listSoundFromContentProvider

        playListViewModel.comparePlayLists(soundOfSystem = listSoundFromContentProvider,1)
    }

    private fun observerLiveData(){
         playListViewModel.soundListCompared.observe(this){soundsDatabase->
              if (soundsDatabase.isNotEmpty()){
                   binding.txvNoSounds.isVisible =false;
                   binding.llSoundsToAdd.isVisible= true
                  bottomSheetAdapter.getListSound(soundsDatabase.toSet())
              }else{
                  binding.txvNoSounds.isVisible =true;
                  binding.llSoundsToAdd.isVisible= false
              }
         }
    }

}