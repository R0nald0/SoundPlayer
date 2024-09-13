package com.example.soundplayer.presentation.fragment

import android.content.ComponentName
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.media3.common.PlaybackException
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.navigation.NavArgs
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.soundplayer.R
import com.example.soundplayer.commons.extension.snackBarSound
import com.example.soundplayer.databinding.FragmentSoundPlayingBinding
import com.example.soundplayer.presentation.service.SoundService
import com.example.soundplayer.presentation.viewmodel.SoundViewModel
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors

class SoundPlayingFragment : Fragment() {
    private val binding by lazy {
        FragmentSoundPlayingBinding.inflate(layoutInflater)
    }
    lateinit var controllerAsync: ListenableFuture<MediaController>


    private val soundViewModel by activityViewModels<SoundViewModel>()

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

    @OptIn(UnstableApi::class)
    override fun onStart() {
        super.onStart()
        if (Util.SDK_INT > 23) {
            initPlayer()
        }
        configSessionToken()
    }

    @OptIn(UnstableApi::class)
    private fun configSessionToken() {
        val sessonToken = SessionToken(
            requireActivity(),
            ComponentName(requireActivity(), SoundService::class.java)
        )
        controllerAsync = MediaController.Builder(requireActivity(), sessonToken)

            .buildAsync()
        controllerAsync.addListener({
            binding.myPlayerView.player = controllerAsync.get()
        }, MoreExecutors.directExecutor())


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack(R.id.mainFragment,false)
        }
    }

    private fun observer() {
        soundViewModel.actualSound?.observe(viewLifecycleOwner) { sound ->
            binding.txvNameMusic.isSelected = true
            binding.txvNameMusic.text = sound.title

            if (sound.uriMediaAlbum != null) {
                binding.imvSong.setImageURI(sound.uriMediaAlbum)
                if (binding.imvSong.drawable == null) {
                    binding.imvSong.setImageResource(R.drawable.transferir)
                }
            }
        }

        soundViewModel.playBackError.observe(viewLifecycleOwner) { playbackError ->
            if (playbackError != null) {
                when (playbackError.code) {
                    PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND -> {
                        requireView().snackBarSound(
                            messages = "${playbackError.message}",
                            backGroundColor = Color.RED,
                            onClick = null,
                            actionText = null,
                        )
                    }

                    PlaybackException.ERROR_CODE_PARSING_CONTAINER_UNSUPPORTED -> {
                        requireView().snackBarSound(
                            messages = "${playbackError.message}",
                            backGroundColor = Color.RED,
                            onClick = null,
                            actionText = null,
                        )
                    }
                }
            }
        }
    }

    @OptIn(UnstableApi::class)
    private fun initPlayer() {
        binding.myPlayerView.player = soundViewModel.myPlayer
    }

    override fun onStop() {
        MediaController.releaseFuture(controllerAsync)
        super.onStop()
    }

}