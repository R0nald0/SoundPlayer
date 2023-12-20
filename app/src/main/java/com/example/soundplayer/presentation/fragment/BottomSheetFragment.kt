package com.example.soundplayer.presentation.fragment

import android.os.Build
import android.os.Bundle
import android.util.Log
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.soundplayer.presentation.adapter.BottomPlayListAdapter
import com.example.soundplayer.databinding.FragmentItemListDialogListDialogBinding
import com.example.soundplayer.model.PlayList
import com.example.soundplayer.model.Sound
import com.example.soundplayer.model.SoundList
import com.example.soundplayer.presentation.viewmodel.PlayListViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class BottomSheetFragment : BottomSheetDialogFragment() {

    private val binding by  lazy {
        FragmentItemListDialogListDialogBinding.inflate(layoutInflater)
    }
   private  lateinit var  bottomSheetAdapter  : BottomPlayListAdapter
   private val playListViewModel by activityViewModels<PlayListViewModel>()

   private lateinit var listSounds : Set<Sound>
   private lateinit var createdSounds : Set<Sound>
    override fun onCreateView(

        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding.edtPlayList.clearFocus()
        binding.textInputLayoutCratePlayList.clearFocus()
        
        val bundle = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requireArguments().getParcelable("list",SoundList::class.java)
        } else {
            requireArguments().getParcelable<SoundList>("list")
        }

        if (bundle != null) {
            Log.i("INFO_", "onCreateView: ${bundle.listMusic.size}")
            listSounds = bundle.listMusic
        }

        binding.btnCreatePlayList.setOnClickListener {
             bottomSheetAdapter.getSoundSelected() //<- Todo Verificar se necessario alterar metodo para retornar lista
              val namePlayList  = binding.edtPlayList.text.toString()
              if (createdSounds.isNotEmpty() && namePlayList.isNotEmpty()){
                   val playList = PlayList(
                        idPlayList = null,
                        name = namePlayList,
                        listSound =  createdSounds.toMutableSet(),
                        currentMusicPosition = 0,
                       )
                   playListViewModel.savePlayList(playList = playList)
                   dismiss()
              }else{
                  // Todo Verificar erro quando createdSound é vazio
                  Toast.makeText(binding.root.context, "Play list vazia ou nome invalido", Toast.LENGTH_SHORT).show()
              }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {


        bottomSheetAdapter = BottomPlayListAdapter{returnedChosedSoundList->

            if (returnedChosedSoundList.isNotEmpty() ){
                 createdSounds = returnedChosedSoundList.toSet()
            }else{
                Toast.makeText(binding.root.context, "Play list vazia", Toast.LENGTH_SHORT).show()
            }
        };

        binding.rvMusics.adapter = bottomSheetAdapter
        binding.rvMusics.addItemDecoration(DividerItemDecoration( binding.root.context,RecyclerView.HORIZONTAL))
        binding.rvMusics.layoutManager = LinearLayoutManager(binding.root.context,LinearLayoutManager.VERTICAL,false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        observerViewModel()
    }

    override fun onStart() {
        super.onStart()
        playListViewModel.saveAllSoundsByContentProvider(listSounds.toMutableSet())
    }
    private fun observerViewModel(){
        playListViewModel.soundListBd.observe(this){listSoundsDatabase->
            bottomSheetAdapter.getListSound(listSoundsDatabase.toMutableSet())
            Log.i("INFO_", "observerViewModel: ${listSoundsDatabase.size}")
        }
    }


}