package com.example.soundplayer.presentation.adapter

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.soundplayer.R
import com.example.soundplayer.commons.extension.convertMilesSecondToMinSec
import com.example.soundplayer.databinding.ItemSoundBinding
import com.example.soundplayer.model.PlayList
import com.example.soundplayer.model.Sound
import com.example.soundplayer.presentation.SongPlayActivity
import com.example.soundplayer.presentation.SoundViewModel

class SoundAdapter(
    private val soundViewModel: SoundViewModel,
    val isUpdateList :(Boolean)->Unit,
) :RecyclerView.Adapter<SoundAdapter.SoundViewHolder>() {
   private  var soundsPlayList: PlayList? = null
   private  var actualSound :Sound? = null
    private val soundSelecionados  = mutableSetOf<Sound>()


    @SuppressLint("NotifyDataSetChanged")
    fun getPlayList(actualPlayList: PlayList){
        soundSelecionados.clear()
        isUpdateList(false)
        soundsPlayList = actualPlayList
        notifyDataSetChanged()
    }
    @SuppressLint("NotifyDataSetChanged")
    fun getActualSound(sound :Sound){
        actualSound = sound
        notifyDataSetChanged()
    }

    fun getSoundSelecionados(): Pair<Long,MutableSet<Sound>> {
        return Pair (soundsPlayList?.idPlayList!! , soundSelecionados)
    }


    inner class  SoundViewHolder(private val binding : ItemSoundBinding): ViewHolder(binding.root){
     fun bind(soudd :Sound,position: Int){
         //TODO crirar metoddo para salvar no banco a posicao da musica atual
         val duration =  binding.root.context.convertMilesSecondToMinSec(soudd.duration.toLong())
         binding.txvDuration.text = duration
         binding.txvTitle.text =soudd.title
         binding.imageView.setImageURI(soudd.uriMediaAlbum)

         if (soundSelecionados.isNotEmpty()){

             if (soundSelecionados.contains(soudd)){
                binding.cardItemSound.setCardBackgroundColor(Color.GRAY)
             }else{
                 binding.cardItemSound.setCardBackgroundColor(ContextCompat.getColor(
                     binding.root.context,R.color.colorSurfaceVariant
                 ))
             }
         }else{
             isUpdateList(false)
             binding.cardItemSound.setCardBackgroundColor(ContextCompat.getColor(
                 binding.root.context,R.color.colorSurfaceVariant
             ))
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
             binding.lottieSoundAnimePlaying.isVisible =true
         }else{
             binding.txvTitle.setTextColor(ContextCompat.getColor(binding.root.context,R.color.my_primary))
             binding.txvTitle.isSelected = false
             binding.lottieSoundAnimePlaying.isVisible =false
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
                        binding.cardItemSound.setCardBackgroundColor(Color.GRAY)
                        soundSelecionados.add(soudd)
                    }else{
                        binding.cardItemSound.setCardBackgroundColor(ContextCompat.getColor(
                            binding.root.context,R.color.colorSurfaceVariant
                        ))
                        soundSelecionados.remove(soudd)
                        if (soundSelecionados.isEmpty())isUpdateList(false)
                    }
                    Log.i("INFO_", "bind: ${soundSelecionados.size}")
                 }

         }

         binding.idContraint.setOnLongClickListener {view->
                if (soundSelecionados.size == 0){
                    isUpdateList(true)
                    soundSelecionados.add(soudd)
                    binding.cardItemSound.setCardBackgroundColor(Color.GRAY)
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

   fun clearSoundListSelected(){
       getPlayList(soundsPlayList!!)
       Log.i("INFO_", "bind: ${soundSelecionados.size}")
  }

    private fun verifyItemContainInTosoundSelecionados(){}
}