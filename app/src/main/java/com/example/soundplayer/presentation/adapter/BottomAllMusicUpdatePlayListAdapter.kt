package com.example.soundplayer.presentation.adapter

import android.annotation.SuppressLint
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.soundplayer.databinding.FragmentItemListDialogListDialogItemBinding
import com.example.soundplayer.model.Sound

class BottomAllMusicUpdatePlayListAdapter (): RecyclerView.Adapter<BottomAllMusicUpdatePlayListAdapter.ViewHolder>() {
    private val sounds  = mutableSetOf<Sound>()
    private val soundSelecionados  = mutableSetOf<Sound>()
    private val sparseBooleanArray  = SparseBooleanArray()

   fun getSoundSelected() : Set<Sound>{
        return soundSelecionados.toSet()
   }

    fun getListSound(listMusics : Set<Sound>){
        sounds.addAll(listMusics)
        notifyDataSetChanged()
    }
     inner class ViewHolder(private val binding: FragmentItemListDialogListDialogItemBinding) :RecyclerView.ViewHolder(binding.root) {
            fun bind( sound : Sound,position: Int){
                binding.txvTitleMusic.text =sound.title

                binding.checkFavorite.isChecked = sparseBooleanArray[position,false]

                binding.checkFavorite.setOnClickListener {
                   sparseBooleanArray.put(position,binding.checkFavorite.isChecked)
                    if (binding.checkFavorite.isChecked){
                        soundSelecionados.add(sound)
                    }else{
                        soundSelecionados.remove(sound)
                    }
                }
            }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            FragmentItemListDialogListDialogItemBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ), parent, false
            )
        )

    }

    override fun getItemCount() = sounds.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
         val sound = sounds.elementAt(position)
          holder.bind(sound,position)
    }


}