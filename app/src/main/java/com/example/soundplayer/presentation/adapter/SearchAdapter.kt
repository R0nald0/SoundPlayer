package com.example.soundplayer.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.soundplayer.R
import com.example.soundplayer.databinding.ItemSoundSearchLayoutBinding
import com.example.soundplayer.model.PlayList
import com.example.soundplayer.model.SongWithPlayListDomain

class SearchAdapter (
    private val onTap :(SongWithPlayListDomain,PlayList)-> Unit,

):RecyclerView.Adapter<SearchAdapter.SearchViewHolder>() {
    private var listSoundWithPlayList = emptyList<SongWithPlayListDomain>()
    private lateinit var itemChipPlayListSearchAdapter : ItemChipPlayListSearchAdapter


    fun getSoundOFSearch(list: List<SongWithPlayListDomain>){
        listSoundWithPlayList = list
        notifyDataSetChanged()
    }

    inner class  SearchViewHolder(private  val binding: ItemSoundSearchLayoutBinding) : ViewHolder(binding.root){

        fun bind(sound :SongWithPlayListDomain){
             val imageSound = sound.sound.uriMediaAlbum
            if (imageSound != null) {
                binding.imgCapaSound.setImageURI(imageSound)
                if (binding.imgCapaSound.drawable == null) {
                    binding.imgCapaSound.setImageResource(R.drawable.music_player_logo_v1)
                }
            }else{
                binding.imgCapaSound.setImageResource(R.drawable.music_player_logo_v1)
            }
            binding.txvTituloSound.text = sound.sound.title
        }


        fun initItemChipPlayListSearched(songWithPlaylists: SongWithPlayListDomain){
            itemChipPlayListSearchAdapter = ItemChipPlayListSearchAdapter { playList ->
                  onTap(songWithPlaylists,playList)
            }

            binding.rvSearchPlaylist.adapter =itemChipPlayListSearchAdapter
            binding.rvSearchPlaylist.layoutManager =LinearLayoutManager(
                binding.rvSearchPlaylist.context,
                RecyclerView.HORIZONTAL,
                false
            )

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

        holder.initItemChipPlayListSearched(soundsWithPlayList)

        itemChipPlayListSearchAdapter.getAllSearchedPlayList(soundsWithPlayList.listOfPlayLists.toSet())

    }
}