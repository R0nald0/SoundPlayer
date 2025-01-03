package com.example.soundplayer.presentation.adapter

import android.annotation.SuppressLint
import android.net.Uri
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.soundplayer.R
import com.example.soundplayer.databinding.FragmentItemListDialogListDialogItemBinding
import com.example.soundplayer.model.Sound

class BottomPlayListAdapter (): RecyclerView.Adapter<BottomPlayListAdapter.ViewHolder>() {
    private val sounds  = mutableSetOf<Sound>()
    private val soundSelecteds  = mutableSetOf<Sound>()
    private val sparseBooleanArray  = SparseBooleanArray()
   fun getSoundSelected() : Set<Sound>{
        return soundSelecteds.toSet()
   }
    @SuppressLint("NotifyDataSetChanged")
    fun getListSound(listMusics : Set<Sound>){
      val  sortedList = listMusics.sortedBy {
            it.title
        }
        sounds.addAll(sortedList)
        notifyDataSetChanged()
    }
     inner class ViewHolder(private val binding: FragmentItemListDialogListDialogItemBinding) :RecyclerView.ViewHolder(binding.root) {
            fun bind( sound : Sound,position: Int){
                binding.txvTitleMusic.text =sound.title
                binding.checkFavorite.isChecked = sparseBooleanArray[position,false]

                if (sound.artistName == null || sound.artistName.contains("unknown"))  binding.idNameArtist.text = ContextCompat.getString(binding.root.context,R.string.desconhecido)
                else binding.idNameArtist.text = sound.artistName

                configureImageSoundBackground(sound)
                configureClickToCheckChoseSound(position, sound)
            }

         private fun configureClickToCheckChoseSound(position: Int, sound: Sound) {
             binding.imgBack.setOnClickListener {
                 sparseBooleanArray.put(position, binding.checkFavorite.isChecked)
                 if (!binding.checkFavorite.isChecked) {
                     binding.checkFavorite.isChecked = true
                     soundSelecteds.add(sound)
                 } else {
                     binding.checkFavorite.isChecked = false
                     soundSelecteds.remove(sound)

                 }
             }

             binding.checkFavorite.setOnClickListener {
                 sparseBooleanArray.put(position, binding.checkFavorite.isChecked)
                 if (binding.checkFavorite.isChecked) {
                     soundSelecteds.add(sound)
                 } else {
                     soundSelecteds.remove(sound)
                 }
             }
         }

         private fun configureImageSoundBackground(sound: Sound) {
             if (sound.uriMediaAlbum != null) {
                 binding.imgBack.setImageURI(Uri.parse(sound.uriMediaAlbum))
                 if (binding.imgBack.drawable == null) {
                     binding.imgBack.setImageResource(R.drawable.music_player_logo_v1)
                 }
             } else {
                 binding.imgBack.setImageResource(R.drawable.music_player_logo_v1)
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