package com.example.soundplayer.presentation.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.soundplayer.commons.extension.exibirToast
import com.example.soundplayer.databinding.FragmentSettingsBinding
import com.example.soundplayer.presentation.viewmodel.PreferencesViewModel
import com.example.soundplayer.presentation.viewmodel.StatePrefre
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsFragment : Fragment() {
    private  lateinit var binding : FragmentSettingsBinding
    var checkedItem = 0
    val singleItems = arrayOf("Pequena", "Média", "Grande")
    private val preferencesViewModel by activityViewModels<PreferencesViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentSettingsBinding.inflate(inflater,container,false)
        return  binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initBinding()
    }

    override fun onStart() {

        super.onStart()
        preferencesViewModel.readDarkModePreference()
        readPreference()
    }
    fun initBinding() = binding.apply {
        settingsBackButton.setOnClickListener {
            findNavController().popBackStack()
        }

        switchDarkMode.setOnCheckedChangeListener { buttonView, isChecked ->
             configureModeUi(isChecked)
        }

       llLayouSizeFont.setOnClickListener {
            showDialog("Escolha o tamanho da font do titulo da musica")
       }


    }
    fun configureModeUi(isDarkMode: Boolean){

          if (isDarkMode) AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
          else{AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)}
          preferencesViewModel.saveDarkModePrefrence(isDarkMode)
    }

    fun  readPreference(){
         preferencesViewModel.isDarkMode.observe(viewLifecycleOwner){statePreference->
              when(statePreference){
                  is StatePrefre.Sucess<*> ->{
                     binding.switchDarkMode.isChecked = statePreference.succssResult as Boolean
                  }
                  is StatePrefre.Error ->{
                      requireActivity().exibirToast(statePreference.mensagem)
                  }
              }
         }

        preferencesViewModel.sizeTextTitleMusic.observe(viewLifecycleOwner){ statePreference ->
            when(statePreference){
                is StatePrefre.Sucess<*>->{
                    val result  = statePreference.succssResult as Float
                   checkedItem = when(result){
                        16f -> 0
                        18f -> 1
                        20f -> 2
                       else -> 0
                   }
                }
                is  StatePrefre.Error ->{
                    requireActivity().exibirToast(statePreference.mensagem)
                }

            }
            binding.txvOpcaoSize.text = singleItems[checkedItem]
        }
    }
    fun showDialog(title:String){



        MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setSingleChoiceItems(singleItems, checkedItem) { dialog, which ->
                binding.txvOpcaoSize.text = singleItems[which]
                dialog.cancel()
                when(which){
                    0 -> {preferencesViewModel.saveSizeTextMusicPrefrence(15f)}
                    1 -> {preferencesViewModel.saveSizeTextMusicPrefrence(18f)}
                    2 -> {preferencesViewModel.saveSizeTextMusicPrefrence(20f)}
                }
            }
            .show()
    }

}