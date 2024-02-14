package com.example.soundplayer.presentation.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.soundplayer.commons.extension.exibirToast
import com.example.soundplayer.databinding.FragmentSettingsBinding
import com.example.soundplayer.presentation.viewmodel.PreferencesViewModel
import com.example.soundplayer.presentation.viewmodel.SoundViewModel
import com.example.soundplayer.presentation.viewmodel.StatePrefre
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsFragment : Fragment() {
    private  lateinit var binding : FragmentSettingsBinding
    private val preferencesViewModel by activityViewModels<PreferencesViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentSettingsBinding.inflate(LayoutInflater.from(inflater.context))
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
    }
    fun configureModeUi(isDarkMode: Boolean){

          if (isDarkMode) AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
          else{AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)}
          preferencesViewModel.saveDarkModePrefrence(isDarkMode)
    }

    fun  readPreference(){
         preferencesViewModel.isDarkMode.observe(viewLifecycleOwner){statePreference->
              when(statePreference){
                  is StatePrefre.Sucesse->{
                     binding.switchDarkMode.isChecked =  statePreference.isDarkMode
                  }
                  is StatePrefre.Error ->{
                      requireActivity().exibirToast(statePreference.mensagem)
                  }
              }
         }
    }

}