package com.example.soundplayer.presentation.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.soundplayer.databinding.ItemSelectPlaylistBinding
import com.example.soundplayer.model.PlayList
import com.example.soundplayer.model.PlaylistWithSoundDomain

class AdapterSelectePlayList (
     val onClick:(PlayList)->Unit
 ):RecyclerView.Adapter<AdapterSelectePlayList.ItemSelectViewHolder>(){

    private var playLists = mutableSetOf<PlaylistWithSoundDomain>()
    @SuppressLint("NotifyDataSetChanged")
    fun addPlayList(playList: List<PlaylistWithSoundDomain>){
        playLists = playList.toMutableSet()
        notifyDataSetChanged()
    }
    @SuppressLint("NotifyDataSetChanged")
    fun updateList(){
        playLists.clear()
        notifyDataSetChanged()
    }


    inner class  ItemSelectViewHolder(private val binding : ItemSelectPlaylistBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(actualPlayList: PlayList){
              binding.txNamePlayList.text = actualPlayList.name
              binding.idContraint.setOnClickListener {
                 onClick(actualPlayList)
             }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemSelectViewHolder {
        val view = ItemSelectPlaylistBinding.inflate(
            LayoutInflater.from(
                parent.context),
            parent,
            false
        )

        return ItemSelectViewHolder(view)
    }

     override fun onBindViewHolder(holder: ItemSelectViewHolder, position: Int) {
         val playList = playLists.elementAt(position)
         holder.bind(playList.playList)
     }

     override fun getItemCount() = playLists.size

}