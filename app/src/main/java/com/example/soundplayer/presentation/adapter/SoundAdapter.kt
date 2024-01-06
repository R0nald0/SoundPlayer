package com.example.soundplayer.presentation.adapter

import android.content.Intent
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.soundplayer.R
import com.example.soundplayer.commons.constants.Constants
import com.example.soundplayer.commons.extension.convertMilesSecondToMinSec
import com.example.soundplayer.databinding.ItemSoundBinding
import com.example.soundplayer.model.PlayList
import com.example.soundplayer.model.Sound
import com.example.soundplayer.presentation.SongPlayActivity
import com.example.soundplayer.presentation.SoundViewModel

class SoundAdapter(
    private val soundViewModel: SoundViewModel,
    val isUpdateList :(Boolean)->Unit,
    val onRemoveSoundToPlayList :(Long,Set<Sound>)->Unit,
    val onAddInToPlayList :(Long,Set<Sound>)->Unit
) :RecyclerView.Adapter<SoundAdapter.SoundViewHolder>() {
   private  var soundsPlayList: PlayList? = null
   private  var actualSound :Sound? = null
    private val soundSelecionados  = mutableSetOf<Sound>()



    fun getPlayList(actualPlayList: PlayList){
        soundSelecionados.clear()
        isUpdateList(false)
        soundsPlayList = actualPlayList
        notifyDataSetChanged()
    }
    fun getActualSound(sound :Sound){
        actualSound = sound
        notifyDataSetChanged()
    }


    inner class  SoundViewHolder(private val binding : ItemSoundBinding): ViewHolder(binding.root){
     fun bind(soudd :Sound,position: Int){
         //TODO crirar metoddo para salvar no banco a posicao da musica atual
         val duration =  binding.root.context.convertMilesSecondToMinSec(soudd.duration.toLong())
         binding.txvDuration.text = duration
         binding.txvTitle.text =soudd.title
         binding.imageView.setImageURI(soudd.uriMediaAlbum)

         if (soundSelecionados.isNotEmpty()){
             isUpdateList(true)
             if (soundSelecionados.contains(soudd)){
                 binding.idContraint.setBackgroundColor(Color.GRAY)
             }else{
                 binding.idContraint.setBackgroundColor(ContextCompat.getColor(
                     binding.root.context,R.color.white_activity_main))
             }
         }else{
             isUpdateList(false)
             binding.idContraint.setBackgroundColor(ContextCompat.getColor(
                 binding.root.context,R.color.white_activity_main))
         }

         if (soudd.uriMediaAlbum != null){
             binding.imageView.setImageURI(soudd.uriMediaAlbum)
             if (binding.imageView.drawable == null){
                 binding.imageView.setImageResource(R.drawable.transferir)
             }
         }
         if (actualSound!=null && actualSound?.title == soudd.title){
             binding.txvTitle.setTextColor(ContextCompat.getColor(binding.root.context,R.color.red))
             binding.txvTitle.isSelected = true
         }else{
             binding.txvTitle.setTextColor(ContextCompat.getColor(binding.root.context,R.color.black))
             binding.txvTitle.isSelected = false
         }


         binding.idContraint.setOnClickListener {
                if (soundSelecionados.isEmpty()) {
                    if (soundsPlayList != null){
                        soundsPlayList!!.currentMusicPosition = position
                        val intent= Intent(binding.root.context, SongPlayActivity::class.java)
                        soundViewModel.getAllMusics(soundsPlayList!!)
                        ContextCompat.startActivity(binding.root.context,intent,null)
                    }else {
                        Toast.makeText(it.context, "PlayList null", Toast.LENGTH_SHORT).show()
                    }
                }
                 else{
                    if (!soundSelecionados.contains(soudd)){
                        binding.idContraint.setBackgroundColor(Color.GRAY)
                        soundSelecionados.add(soudd)
                        if (soundSelecionados.isEmpty())isUpdateList(true)
                    }else{
                        binding.idContraint.setBackgroundColor(ContextCompat.getColor(it.context,R.color.white_activity_main))
                        soundSelecionados.remove(soudd)
                        if (soundSelecionados.isEmpty())isUpdateList(false)
                    }
                    Log.i("INFO_", "bind: ${soundSelecionados.size}")
                 }
         }

         binding.idContraint.setOnLongClickListener {view->
             if (soundsPlayList!!.name != Constants.ALL_MUSIC_NAME){
                 isUpdateList(true)
                 soundSelecionados.add(soudd)
                 binding.idContraint.setBackgroundColor(Color.GRAY)
                 Log.i("INFO_", "bind: ${soundSelecionados.size}")
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

    override fun onBindViewHolder(holder: SoundViewHolder, position: Int) {
        val sound = soundsPlayList?.listSound?.elementAt(position)
        if (sound != null) {
            holder.bind(sound,position)
        }
    }

    private fun createOptionsMenu(view : View, playList: PlayList){

        val popupMenu  = PopupMenu(view.context,view)

        popupMenu.inflate(R.menu.sound_menu)
        if (playList.name == Constants.ALL_MUSIC_NAME){
            popupMenu.menu.removeItem(R.id.id_remove)
        }

        playList.listSound
        popupMenu.show()
        popupMenu.setOnMenuItemClickListener {

            when(it.itemId){
                R.id.id_update->{

                    //receberlista de item para adicionar
                    true
                }
                R.id.id_remove ->{
                    onRemoveSoundToPlayList
                    //receberlista de item para remover
                    Toast.makeText(view.context, "PlayList ${"remover da playList"}", Toast.LENGTH_SHORT).show()
                    true
                }
                else ->false
            }
        }
    }
   fun clearSoundListSelected(){
       getPlayList(soundsPlayList!!)
       Log.i("INFO_", "bind: ${soundSelecionados.size}")
  }
    private fun verifyItemContainInTosoundSelecionados(){}
}