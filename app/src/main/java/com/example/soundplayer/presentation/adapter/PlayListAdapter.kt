package com.example.soundplayer.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.soundplayer.databinding.PlayListItemBinding
import com.example.soundplayer.model.PlayList
import com.example.soundplayer.model.Sound

class PlayListAdapter(
    val onclick :(PlayList)->Unit
) :RecyclerView.Adapter<PlayListAdapter.PlayLisViewHolder>(){


    private  val playLists = mutableSetOf<PlayList>()
    fun addPlayList(playList: PlayList){

        playLists.add(playList)
        notifyDataSetChanged()
    }
    fun getActualPlayList(sound : Sound){
       // actualSound = sound
        notifyDataSetChanged()
    }


    inner class  PlayLisViewHolder(private val binding : PlayListItemBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(playList: PlayList, position: Int){
            binding.txvTitle.text = playList.name
            binding.idContraintPlayList.setOnClickListener {
               if (playList.listSound.isEmpty()){
                   Toast.makeText(binding.root.context, "PlayLis vazia", Toast.LENGTH_SHORT).show()
               }else{
                   onclick(playList)
               }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayLisViewHolder {
        val view = PlayListItemBinding.inflate(
            LayoutInflater.from(
            parent.context),
            parent,
            false
        )

        return PlayLisViewHolder(view)
    }

    override fun getItemCount() = playLists.size
    override fun onBindViewHolder(holder: PlayLisViewHolder, position: Int) {
        val playList = playLists.elementAt(position)
        holder.bind(playList,position)
    }
}