
package com.example.soundplayer.presentation.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.soundplayer.R
import com.example.soundplayer.commons.extension.convertMilesSecondToMinSec
import com.example.soundplayer.databinding.ItemSoundBinding
import com.example.soundplayer.model.PlayList
import com.example.soundplayer.model.Sound
import com.example.soundplayer.presentation.viewmodel.SoundViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class SoundAdapter(
    private val soundViewModel: SoundViewModel,
    val isUpdateList :(Boolean)->Unit,
    val onClickInitNewFragment :()->Unit,
    val onDelete : (Long,Pair<Int,Sound>)-> Unit
) :RecyclerView.Adapter<SoundAdapter.SoundViewHolder>() {
    private  var soundsPlayList: PlayList? = null
    private  var actualSound :Sound? = null
    private val soundSelecionados  = mutableSetOf<Pair<Int,Sound>>()
    var sizeTitleMusic =16f
    private var isPlay = false

    @SuppressLint("NotifyDataSetChanged")
    fun updateAnimationWithPlaying(isPlaying : Boolean){
        isPlay = isPlaying
        notifyDataSetChanged()
    }
    @SuppressLint("NotifyDataSetChanged")
    fun getPlayList(actualPlayList: PlayList){
        soundSelecionados.clear()
        isUpdateList(false)
        soundsPlayList = actualPlayList
        notifyDataSetChanged()
    }


    @SuppressLint("NotifyDataSetChanged")
    fun getActualSound(sound :Sound){
        actualSound = sound
        notifyDataSetChanged()
    }

    fun getSoundSelecionados(): Pair<Long,MutableSet<Pair<Int,Sound>>> {
        return Pair (soundsPlayList?.idPlayList?: 1, soundSelecionados)
    }

    inner class  SoundViewHolder(private val binding : ItemSoundBinding): ViewHolder(binding.root){
        fun bind(soudd :Sound,position: Int){

            val duration =  binding.root.context.convertMilesSecondToMinSec(soudd.duration.toLong())
            binding.txvDuration.text = duration
            binding.txvTitle.text =soudd.title
            binding.txvTitle.textSize = sizeTitleMusic

            binding.idBtnOptionSound.setOnClickListener {
                createOptionsMenu(it,soudd,position)
            }

            configSelectionedItemApperence(position,soudd)
           

            if (soudd.uriMediaAlbum != null) {
                binding.imageView.setImageURI(soudd.uriMediaAlbum)
                if (binding.imageView.drawable == null) {
                    binding.imageView.setImageResource(R.drawable.transferir)
                }
            }else{
                binding.imageView.setImageResource(R.drawable.transferir)
            }

            clickItemEvent(position, soudd)

            configApperenceItemImage(soudd)
            longPressEvent(position,soudd)
        }

      fun configurAnimationWhenPlaying(isPlaying: Boolean) {
            if (isPlaying){
                binding.txvTitle.isSelected = true
                binding.lottieSoundAnimePlaying.isVisible = true
            }else{
               binding.txvTitle.isSelected = false
                binding.lottieSoundAnimePlaying.isVisible = false
            }
        }

        private fun configSelectionedItemApperence(position: Int,soudd: Sound) {
            if (soundSelecionados.isNotEmpty()) {

                if (soundSelecionados.contains(Pair(position,soudd))) {
                    binding.cardItemSound.setCardBackgroundColor(Color.GRAY)
                } else {
                    binding.cardItemSound.setCardBackgroundColor(
                        ContextCompat.getColor(
                            binding.root.context, R.color.colorNightSurfaceVariant
                        )
                    )
                }
            } else {
                isUpdateList(false)
                binding.cardItemSound.setCardBackgroundColor(
                    ContextCompat.getColor(
                        binding.root.context, R.color.colorNightSurfaceVariant
                    )
                )
            }
        }

        private fun configApperenceItemImage(soudd: Sound) {

            if (actualSound != null && actualSound?.title == soudd.title) {
                binding.txvTitle.setTextColor(
                    ContextCompat.getColor(
                        binding.root.context,
                        R.color.red
                    )
                )

                configurAnimationWhenPlaying(true)

            } else {
                binding.txvTitle.setTextColor(
                    ContextCompat.getColor(
                        binding.root.context,
                        R.color.my_primary
                    )
                )
                configurAnimationWhenPlaying(false)
            }
        }

        private fun clickItemEvent(position: Int, soudd: Sound) {
            binding.idContraint.setOnClickListener {
                if (soundSelecionados.isEmpty()) {
                    soundsPlayList?.let {nonNulPlayList->
                        nonNulPlayList.currentMusicPosition = position
                        nonNulPlayList.listSound.size
                        soundViewModel.getAllMusics(nonNulPlayList)
                        Log.i("INFO_", "clickItemEvent: ${nonNulPlayList.listSound.size}")
                        onClickInitNewFragment()
                    }?: Toast.makeText(it.context, "PlayList null", Toast.LENGTH_SHORT).show()

                } else {
                    if (!soundSelecionados.contains(Pair(position,soudd))) {
                        binding.cardItemSound.setCardBackgroundColor(Color.GRAY)
                        soundSelecionados.add(Pair(position,soudd))
                    } else {
                        binding.cardItemSound.setCardBackgroundColor(
                            ContextCompat.getColor(
                                binding.root.context, R.color.colorNightSurfaceVariant
                            )
                        )
                        soundSelecionados.remove(Pair(position,soudd))
                        if (soundSelecionados.isEmpty()) isUpdateList(false)
                    }
                }

            }
        }

        private fun longPressEvent(position: Int,soudd: Sound) {
            binding.idContraint.setOnLongClickListener { view ->
                if (soundSelecionados.size == 0) {
                    isUpdateList(true)
                    soundSelecionados.add(Pair(position,soudd))
                    binding.cardItemSound.setCardBackgroundColor(Color.GRAY)
                }
                true
            }
        }


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SoundViewHolder {
        val view = ItemSoundBinding.inflate(LayoutInflater.from(
            parent.context),
            parent, false
        )
        return SoundViewHolder(view)
    }

    override fun getItemCount(): Int {
        return soundsPlayList?.listSound?.size ?:0
    }


    override fun onBindViewHolder(holder: SoundViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val sound = soundsPlayList?.listSound?.elementAt(position)

        if (sound != null) {
            holder.bind(sound,position)

            if (actualSound != null && actualSound?.title == sound.title){
                holder.configurAnimationWhenPlaying(isPlay)
            }

        }
    }
    fun clearSoundListSelected(){
        getPlayList(soundsPlayList!!)
        soundSelecionados.clear()
    }

    private fun createOptionsMenu(view : View,sound: Sound,position : Int){

        val popupMenu  = PopupMenu(view.context,view)
        popupMenu.inflate(R.menu.unique_sound_menu)
        popupMenu.show()
        popupMenu.setOnMenuItemClickListener {
            when(it.itemId){
                R.id.id_delete->{
                    MaterialAlertDialogBuilder(view.context)
                        .setTitle("Deseja deletar ${sound.title} ?")
                        .setPositiveButton("Sim"){dialog,q->
                            onDelete(soundsPlayList?.idPlayList!!,Pair(position,sound))
                            dialog.dismiss()
                        }
                        .setNegativeButton("Cancelar"){dialog,q->
                            dialog.dismiss()
                        }
                        .show()
                    true
                }

                else -> false
            }
        }
    }


}
