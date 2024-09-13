package com.example.soundplayer.presentation.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.soundplayer.databinding.ItemSearchPlaylistChipBinding
import com.example.soundplayer.model.PlayList

class ItemChipPlayListSearchAdapter (
    private val onClick : (PlayList)->Unit
): RecyclerView.Adapter<ItemChipPlayListSearchAdapter.ItemPlayListSearchViewHolder>() {
     var listPlayListSearched = setOf<PlayList>()

    fun getAllSearchedPlayList(list : Set<PlayList>){
         listPlayListSearched = list
        notifyDataSetChanged()
    }

    inner  class  ItemPlayListSearchViewHolder(val binding : ItemSearchPlaylistChipBinding) :RecyclerView.ViewHolder(binding.root){
        fun  bind(playList: PlayList){
            binding.chipTituloPlayList.text = playList.name
            binding.chipTituloPlayList.setOnClickListener {
                onClick(playList)
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ItemPlayListSearchViewHolder {
        val layout = LayoutInflater.from(parent.context)
       val itemSearChedlayout = ItemSearchPlaylistChipBinding.inflate(layout,parent,false)
       return ItemPlayListSearchViewHolder(itemSearChedlayout)
    }

    override fun getItemCount() = listPlayListSearched.size

    override fun onBindViewHolder(holder: ItemPlayListSearchViewHolder, position: Int) {
      val  playListSearched = listPlayListSearched.elementAt(position)
        holder.bind(playListSearched)
    }
}