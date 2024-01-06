package com.example.soundplayer.presentation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.soundplayer.R
import com.example.soundplayer.commons.constants.Constants
import com.example.soundplayer.commons.extension.showAlerDialog
import com.example.soundplayer.databinding.PlayListItemBinding
import com.example.soundplayer.databinding.UpadateNamePlaylistLayoutBinding
import com.example.soundplayer.model.PlayList
import com.example.soundplayer.presentation.SoundViewModel

class PlayListAdapter(
    private val soundViewModel: SoundViewModel,
    val onclick :(PlayList)->Unit,
    val onDelete :(PlayList)->Unit,
    val onEdit :(PlayList)->Unit
) :RecyclerView.Adapter<PlayListAdapter.PlayLisViewHolder>(){
    private var playLists = mutableSetOf<PlayList>()
    fun addPlayList(playList: List<PlayList>){
        playLists = playList.toMutableSet()
        notifyDataSetChanged()
    }
    fun updateList(){
        playLists.clear()
        notifyDataSetChanged()
    }


    inner class  PlayLisViewHolder(private val binding : PlayListItemBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(actualPlayList: PlayList, position: Int){

            binding.txvTitle.text = actualPlayList.name
            binding.idContraintPlayList.setOnClickListener {
                if (actualPlayList.listSound.isEmpty()){
                   Toast.makeText(binding.root.context, "PlayList vazia", Toast.LENGTH_SHORT).show()
               }else{
                   onclick(actualPlayList)
               }
            }

            binding.idContraintPlayList.setOnLongClickListener {view->
                if (actualPlayList.name != Constants.ALL_MUSIC_NAME){
                    createOptionsMenu(view,actualPlayList)
                }
                true
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

    private fun createOptionsMenu(view : View,playList: PlayList){

        val popupMenu  =PopupMenu(view.context,view)
        popupMenu.inflate(R.menu.play_list_menu)
        popupMenu.show()
        popupMenu.setOnMenuItemClickListener {
            when(it.itemId){
                R.id.idEdit->{
                    val upadateNameBinding =UpadateNamePlaylistLayoutBinding.inflate(LayoutInflater.from(view.context),null, false)

                    view.context.showAlerDialog(
                        messenger = String.format(ContextCompat.getString(view.context,R.string.atualizar_nome_playlist),playList.name),
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