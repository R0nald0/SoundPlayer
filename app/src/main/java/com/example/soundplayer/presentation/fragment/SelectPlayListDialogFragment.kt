package com.example.soundplayer.presentation.fragment

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.soundplayer.commons.extension.exibirToast
import com.example.soundplayer.databinding.FragmentSelectPlayListDialogListDialogBinding
import com.example.soundplayer.model.DataSoundPlayListToUpdate
import com.example.soundplayer.model.Sound
import com.example.soundplayer.presentation.adapter.AdapterSelectePlayList
import com.example.soundplayer.presentation.viewmodel.PlayListViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class SelectPlayListDialogFragment : DialogFragment() {
    private lateinit var posToUpdate: List<Int>
    private  var view : View?=null
    private var _binding: FragmentSelectPlayListDialogListDialogBinding? = null
    private val playListViewModel by activityViewModels<PlayListViewModel>()
    private lateinit var  adapterPlaylistSelect  :AdapterSelectePlayList
    private lateinit var  soudsToUpdate : Set<Sound>
    private  val arsg : SelectPlayListDialogFragmentArgs by navArgs()

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSelectPlayListDialogListDialogBinding.inflate(inflater ,null, false)
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
           soudsToUpdate = arsg.soundsListToAddPlayList.listMusic.map {
                it.second
           }.toSet()
        posToUpdate = arsg.soundsListToAddPlayList.listMusic.map {
            it.first
        }
    }
    private fun observer() {
        playListViewModel.playListsWithSounds.observe(this){ listOfPlayList->
            if (listOfPlayList.isNotEmpty()){
                adapterPlaylistSelect.addPlayList(listOfPlayList)
            }else{
               requireContext().exibirToast("Você não possui playList criadas")
                dismiss()
            }
        }
    }
    private fun inicializerBinding() {
        adapterPlaylistSelect = AdapterSelectePlayList(){selectedPlayList->
            playListViewModel.updatSoundAtPlaylist(
                DataSoundPlayListToUpdate(
                    idPlayList =  selectedPlayList.idPlayList ?: 0,
                    positionSound = posToUpdate,
                    sounds = soudsToUpdate
                )
            )
            dismiss()
        }
        binding.rvPlaylistsOptions.apply {
            adapter =adapterPlaylistSelect
            layoutManager = LinearLayoutManager(context,LinearLayoutManager.VERTICAL,false)
            addItemDecoration( DividerItemDecoration(this.context,LinearLayoutManager.VERTICAL))
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

     return  MaterialAlertDialogBuilder(requireContext())
          .setView(_binding?.root)
          .show()
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}