package com.example.soundplayer.presentation.fragment

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.ContextThemeWrapper
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.soundplayer.R
import com.example.soundplayer.databinding.FragmentSelectPlayListDialogListDialogItemBinding
import com.example.soundplayer.databinding.FragmentSelectPlayListDialogListDialogBinding
import com.example.soundplayer.model.Sound
import com.example.soundplayer.model.SoundList
import com.example.soundplayer.presentation.adapter.AdapterSelectePlayList
import com.example.soundplayer.presentation.viewmodel.PlayListViewModel

class SelectPlayListDialogFragment : DialogFragment() {

    private var _binding: FragmentSelectPlayListDialogListDialogBinding? = null

    private val playListViewModel by activityViewModels<PlayListViewModel>()
    private lateinit var  adapterPlaylistSelect  :AdapterSelectePlayList
    private lateinit var soudsToUpdate : Set<Sound>

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentSelectPlayListDialogListDialogBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setUpBundle()
        observer()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        inicializerBinding()

    }

    private fun setUpBundle(){
        val bundle = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requireArguments().getParcelable("listSound",SoundList::class.java)
        } else {
            requireArguments().getParcelable<SoundList>("listSound")
        }

        if (bundle != null){
            soudsToUpdate = bundle.listMusic
        }else{
            Toast.makeText(this.context, "Lista de musicas não contém sons", Toast.LENGTH_SHORT).show()
            dismiss()
        }

    }
    private fun observer() {
        playListViewModel.playLists.observe(this){listOfPlayList->
            if (listOfPlayList.isNotEmpty()){
                adapterPlaylistSelect.addPlayList(listOfPlayList)
            }else{
                Toast.makeText(this.context, "você não possui playLists criada", Toast.LENGTH_SHORT).show()
                dismiss()
            }

        }
    }
    private fun inicializerBinding() {
        adapterPlaylistSelect = AdapterSelectePlayList(){selectedPlayList->
            playListViewModel.addSountToPlayList(
                idPlayList = selectedPlayList.idPlayList!!,
                listSound = soudsToUpdate
            )
            Toast.makeText(this.context, selectedPlayList.name, Toast.LENGTH_SHORT).show()
            dismiss()
        }
        binding.rvPlaylistsOptions.apply {
            adapter =adapterPlaylistSelect
            layoutManager = LinearLayoutManager(context,LinearLayoutManager.VERTICAL,false)
            addItemDecoration( DividerItemDecoration(this.context,LinearLayoutManager.VERTICAL))
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}