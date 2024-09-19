package com.example.soundplayer.presentation.fragment

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.soundplayer.commons.extension.checkThemeMode
import com.example.soundplayer.commons.extension.exibirToast
import com.example.soundplayer.databinding.FragmentSettingsBinding
import com.example.soundplayer.presentation.viewmodel.PreferencesViewModel
import com.example.soundplayer.presentation.viewmodel.StatePrefre
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsFragment : Fragment() {
    private  lateinit var binding : FragmentSettingsBinding
    var modeUiChecked = 2
    var checkedItem = 0
    var orderChoesed = 0
    val singleItems = arrayOf("Pequena", "Média", "Grande")
    val options = arrayOf("Nome","Nome Decrescente","Inserido por ultimo")
    val modeUIOptions = arrayOf("Light","DarkMode","Mesmo que o sistema")

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
        preferencesViewModel.readAllPrefference()
        readPreference()
    }
    private fun initBinding() = binding.apply {
        settingsBackButton.setOnClickListener {
            findNavController().popBackStack()
        }

       llLayouDarkMode.setOnClickListener {
           showDialog(
               title = "Escolha o modo de visualisção",
               selectedItem = modeUiChecked,
               options = modeUIOptions,
               onSelected = {onSelected->
                   when(onSelected){
                       0 -> {
                          // AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                           preferencesViewModel.saveDarkModePrefrence(0)
                       }
                       1 -> {
                           //AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                           preferencesViewModel.saveDarkModePrefrence(1)
                       }
                       2 -> {
                          preferencesViewModel.saveDarkModePrefrence(2)
                       }
                   }

                   binding.txvOpcaoModeUi.text = modeUIOptions[onSelected]
                   modeUiChecked = onSelected
               }
           )
       }

       llLayouSizeFont.setOnClickListener {
            showDialog(
                title = "Escolha o tamanho da font do titulo da musica",
                selectedItem = checkedItem,
                options = singleItems,
                onSelected = {chosedItem->
                    when(chosedItem){
                        0 -> {preferencesViewModel.saveSizeTextMusicPrefrence(15f)}
                        1 -> {preferencesViewModel.saveSizeTextMusicPrefrence(18f)}
                        2 -> {preferencesViewModel.saveSizeTextMusicPrefrence(20f)}
                    }
                    binding.txvOpcaoSize.text = singleItems[chosedItem]
                    checkedItem = chosedItem
                }
            )
       }


        llLayoutOrderedSound.setOnClickListener {
            showDialog(
             title = "Ordenar Sons por",
             options = options ,
             selectedItem = orderChoesed,
             onSelected = {chosedItem->
                 when(chosedItem){
                     0 -> {preferencesViewModel.saveOrderedSoundPrefference(chosedItem)}
                     1 -> {preferencesViewModel.saveOrderedSoundPrefference(chosedItem)}
                     2 -> {preferencesViewModel.saveOrderedSoundPrefference(chosedItem)}
                 }

                 requireContext().exibirToast("Ás playlists que não estão tocando serão ordenados por  ${options[chosedItem]} ")
                 binding.txvOpcaoOrder.text =options[chosedItem]
                 orderChoesed = chosedItem
             }
         )

        }


    }

    private fun  readPreference(){
         preferencesViewModel.isDarkMode.observe(viewLifecycleOwner){statePreference->
              when(statePreference){
                  is StatePrefre.Sucess<*> ->{
                    val result =  statePreference.succssResult as Int
                      when(result){
                          0 ->{
                              AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                          }
                          1-> {
                              AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                          }
                          2 -> {
                               requireActivity().checkThemeMode()
                          }
                      }

                      modeUiChecked = result
                      binding.txvOpcaoModeUi.text = modeUIOptions[result]
                  }
                  is StatePrefre.Error ->{
                      requireActivity().exibirToast(statePreference.mensagem)
                  }
              }
         }
        preferencesViewModel.uiStatePreffs.observe(viewLifecycleOwner){userPref->
              orderChoesed = userPref.orderedSound
              Log.i("INFO_", "readAllPrefference: $userPref")
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
            binding.txvOpcaoOrder.text =options[orderChoesed]
        }
    }
    private fun showDialog(
          title:String,
          options: Array<String>,
          selectedItem :Int,
          onSelected : (Int)->Unit,
    ){
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setSingleChoiceItems(options, selectedItem) { dialog, chosedItem ->
                dialog.cancel()
                onSelected(chosedItem)
            }
            .show()
    }
    private fun verifySystemModeUI() :Boolean {
        return requireActivity().applicationContext.resources.configuration.uiMode and
        Configuration.UI_MODE_NIGHT_MASK ==
                Configuration.UI_MODE_NIGHT_YES
    }


}