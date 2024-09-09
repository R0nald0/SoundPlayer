package com.example.soundplayer.presentation.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.soundplayer.R
import com.example.soundplayer.databinding.ItemSoundSearchLayoutBinding
import com.example.soundplayer.model.Sound

class SearchAdapter (
    private val onTap :(Sound)-> Unit
):RecyclerView.Adapter<SearchAdapter.SearchViewHolder>() {
    private var listSoundWithPlayList = emptyList<Sound>()


    fun getSoundOFSearch(list: List<Sound>){
        listSoundWithPlayList = list
        notifyDataSetChanged()
    }

    inner class  SearchViewHolder(private  val binding: ItemSoundSearchLayoutBinding) : ViewHolder(binding.root){

        fun bind(sound :Sound){
             val imageSound = sound.uriMediaAlbum ?: sound.uriMedia ?: Uri.EMPTY
            if (imageSound != null){
                binding.imgCapaSound.setImageURI(imageSound)
            }else{
                binding.imgCapaSound.setImageResource(R.drawable.transferir)
            }
            binding.txvTituloSound.text = sound.title
            binding.constraintItemSoundSearch.setOnClickListener {
                onTap(sound)
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
       val searchView  = ItemSoundSearchLayoutBinding.inflate(
           LayoutInflater.from(parent.context),
           parent,
           false
       )
       return  SearchViewHolder(searchView)
    }

    override fun getItemCount() = listSoundWithPlayList.size

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
       val soundsWithPlayList = listSoundWithPlayList[position]
        holder.bind(soundsWithPlayList)
    }
}