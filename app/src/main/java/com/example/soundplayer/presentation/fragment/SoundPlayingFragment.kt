package com.example.soundplayer.presentation.fragment

import android.content.ComponentName
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.fragment.app.activityViewModels
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.navigation.fragment.findNavController
import com.example.soundplayer.R
import com.example.soundplayer.databinding.FragmentSoundPlayingBinding
import com.example.soundplayer.presentation.viewmodel.SoundViewModel
import com.example.soundplayer.presentation.service.SoundService
import com.google.common.util.concurrent.MoreExecutors

class SoundPlayingFragment : Fragment() {
    private val binding by lazy {
        FragmentSoundPlayingBinding.inflate(layoutInflater)
    }

    private val  soundViewModel by activityViewModels<SoundViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        observer()
        // Inflate the layout for this fragment
        return binding.root
    }

    @OptIn(UnstableApi::class) override fun onStart() {
        super.onStart()
        if (Util.SDK_INT > 23){
            initPlayer()
        }
        configSessionToken()
    }

    @OptIn(UnstableApi::class) private fun configSessionToken() {
        val sessonToken = SessionToken(
            requireActivity(),
            ComponentName(requireActivity(), SoundService::class.java)
        )
        val controllerAsync = MediaController.Builder(requireActivity(), sessonToken).buildAsync()
        controllerAsync.addListener({
            binding.myPlayerView.player = controllerAsync.get()
        }, MoreExecutors.directExecutor())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun observer() {
        soundViewModel.actualSound.observe(requireActivity()) { sound ->
            binding.txvNameMusic.isSelected = true
            binding.txvNameMusic.text = sound.title

            if (sound.uriMediaAlbum != null){
                binding.imvSong.setImageURI(sound.uriMediaAlbum)
                if (binding.imvSong.drawable == null){
                    binding.imvSong.setImageResource(R.drawable.transferir)
                }
            }
        }
    }
    @OptIn(UnstableApi::class)
    private fun  initPlayer(){
        soundViewModel.getPlayer().let { exoPlayer ->
            binding.myPlayerView.player = exoPlayer
            soundViewModel.playAllMusicFromFist()
        }
    }

}