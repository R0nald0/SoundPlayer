
package com.example.soundplayer.presentation.adapter

import android.annotation.SuppressLint
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.util.containsValue
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.soundplayer.R
import com.example.soundplayer.commons.constants.Constants
import com.example.soundplayer.commons.extension.showAlerDialog
import com.example.soundplayer.databinding.PlayListItemBinding
import com.example.soundplayer.databinding.UpadateNamePlaylistLayoutBinding
import com.example.soundplayer.model.PlayList
import com.example.soundplayer.model.PlaylistWithSoundDomain

class PlayListAdapter(
    val onclick :(PlayList)->Unit,
    val onDelete :(PlayList)->Unit,
    val onEdit :(PlayList)->Unit,
    val onAddSoundAllMusic:()->Unit
) :RecyclerView.Adapter<PlayListAdapter.PlayLisViewHolder>(){
    private var playlistWithSoundDomainMutableSet = mutableSetOf<PlaylistWithSoundDomain>()
    private var userPositionPreferePlayListId = -1L
    private var isPlaying = false
    private var currentPlayListPlaying : PlayList? = null

    @SuppressLint("UseSparseArrays")
    var positionPlayListClicked = SparseArray<Int>()
    @SuppressLint("NotifyDataSetChanged")
    fun addPlayList(playlistWithSoundDomains: List<PlaylistWithSoundDomain>){
        positionPlayListClicked.clear()
        playlistWithSoundDomainMutableSet = playlistWithSoundDomains.toMutableSet()
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun getCurrentPlayListPlaying(playList: PlayList){
        currentPlayListPlaying = playList
        notifyDataSetChanged()
    }
    @SuppressLint("NotifyDataSetChanged")
    fun updateAnimationWhenPlayerPause(playIng:Boolean){
        isPlaying =playIng
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setLastOpenPlayListBorder(idPlayListPrefe:Long){
        userPositionPreferePlayListId =idPlayListPrefe
        notifyDataSetChanged()
    }

    inner class  PlayLisViewHolder( val binding : PlayListItemBinding): RecyclerView.ViewHolder(binding.root){

        fun bind(actualPlayList: PlayList, position: Int){
            if(!positionPlayListClicked.containsValue(position)){
                binding.idContraintPlayList.background =
                    ContextCompat.getDrawable(binding.root.context,R.drawable.border_playlist_item_not_selected)
            }
            initBindings(actualPlayList, position)
        }

        private fun  initBindings(actualPlayList :PlayList, position: Int){

            binding.txvTitle.text = actualPlayList.name
            binding.idContraintPlayList.setOnClickListener {
                setUpBorderPlayList(position)
                verifyPlayListEmpty(actualPlayList)
            }

            binding.idContraintPlayList.setOnLongClickListener {view->
                createOptionsMenu(view,actualPlayList)
                true
            }
        }
        private fun verifyPlayListEmpty(actualPlayList: PlayList) {
            if (actualPlayList.listSound.isEmpty()) {
                Toast.makeText(binding.root.context, "PlayList vazia", Toast.LENGTH_SHORT).show()
                onclick(actualPlayList)
            } else {
                onclick(actualPlayList)
            }
        }
        @SuppressLint("NotifyDataSetChanged")
         private fun setUpBorderPlayList(position: Int) {
            positionPlayListClicked.clear()
            positionPlayListClicked.put(position, position)
            if (positionPlayListClicked.containsValue(position)) {
                binding.idContraintPlayList.background =
                    ContextCompat.getDrawable(binding.root.context, R.drawable.border_playlist_selected_item)
                notifyItemChanged(position)
            }
        }

        fun setUpImageActualPlayListPlaying(currentPlayList: PlayList,playIng: Boolean ,actualPlayList: PlayList){
            if (playIng && currentPlayList.idPlayList == actualPlayList.idPlayList ){
                binding.lottieAnimePlaying.isVisible =true
                binding.imgPlayList.isVisible =false
            }else{
                binding.lottieAnimePlaying.isVisible =false
                binding.imgPlayList.isVisible =true
            }
        }

        fun setBorderOnPlayListFirstCharging(position: Int) {
            positionPlayListClicked.clear()
            positionPlayListClicked.put(position, position)
            if (positionPlayListClicked.containsValue(position)) {
                binding.idContraintPlayList.background =
                    ContextCompat.getDrawable(binding.root.context, R.drawable.border_playlist_selected_item)
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
    override fun getItemCount() = playlistWithSoundDomainMutableSet.size
    override fun onBindViewHolder(holder: PlayLisViewHolder, position: Int) {

        val playListWithMusic = playlistWithSoundDomainMutableSet.elementAt(position)

        if (userPositionPreferePlayListId == playListWithMusic.playList.idPlayList){
            holder.setBorderOnPlayListFirstCharging(position)
        }

        currentPlayListPlaying?.let {actualPlayListPlaying->
            holder.setUpImageActualPlayListPlaying(
                currentPlayList = actualPlayListPlaying,
                playIng = isPlaying,
                actualPlayList =  playListWithMusic.playList
            )
        }

        playListWithMusic.playList.listSound.addAll(playListWithMusic.soundOfPlayList)
        holder.bind(playListWithMusic.playList,position)
    }
    private fun createOptionsMenu(view : View,playList: PlayList){

        val popupMenu  =PopupMenu(view.context,view)

        if (playList.name != Constants.ALL_MUSIC_NAME){
            popupMenu.inflate(R.menu.play_list_menu)
        }else{
            popupMenu.inflate(R.menu.allmusic_menu)
        }

        popupMenu.show()
        popupMenu.setOnMenuItemClickListener {item->

            when(item.itemId){
                R.id.id_add_music->{
                    onAddSoundAllMusic()
                    true
                }
                R.id.idEdit->{
                    val upadateNameBinding =UpadateNamePlaylistLayoutBinding.inflate(LayoutInflater.from(view.context),null, false)

                    view.context.showAlerDialog(
                        messenger = String.format(ContextCompat.getString(view.context,R.string.text_atualizar_playlist),playList.name),
                        negativeButton = view.context.getString(R.string.cancelar),
                        positiveButton = view.context.getString(R.string.editar),
                        layoutResid = upadateNameBinding.root,
                        onPositive = {
                            val namePlayList = upadateNameBinding.edtextPlaylist.text.toString()
                            if (namePlayList.isNotEmpty()){
                                val playListToUpdate = PlayList(
                                    idPlayList = playList.idPlayList,
                                    listSound = playList.listSound,
                                    name =namePlayList,
                                    currentMusicPosition = playList.currentMusicPosition
                                )
                                onEdit(playListToUpdate)
                            }else{
                                Toast.makeText(view.context, "Nome da playlist não pode ser vazio", Toast.LENGTH_SHORT).show()
                            }
                        },
                    )
                    true
                }
                R.id.id_delete ->{
                    view.context.showAlerDialog(
                        messenger = String.format(ContextCompat.getString(view.context,R.string.text_delete_playlist),playList.name),
                        negativeButton = view.context.getString(R.string.n_o),
                        positiveButton = view.context.getString(R.string.sim),
                        layoutResid = View.inflate(view.context, androidx.appcompat.R.layout.abc_action_menu_layout,null),
                        onPositive = {  onDelete(playList)}
                    )
                    true
                }
                else -> false
            }
        }
    }



}
