package com.example.soundplayer.presentation.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.soundplayer.R
import com.example.soundplayer.commons.extension.convertMilesSecondToMinSec
import com.example.soundplayer.databinding.ItemSoundBinding
import com.example.soundplayer.model.PlayList
import com.example.soundplayer.model.Sound
import com.example.soundplayer.presentation.SongPlayActivity
import com.example.soundplayer.presentation.SoundPresenter

class SoundAdapter(
 private val soundPresenter: SoundPresenter
) :RecyclerView.Adapter<SoundAdapter.SoundViewHolder>() {
   private  var soundsPlayList: PlayList? = null
   private  var actualSound :Sound? = null

    fun getPlayList(actualPlayList: PlayList){
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
             ///intent.putExtra(Constants.KEY_EXTRA_MUSIC,sounds)
             if (soundsPlayList != null){
                 soundsPlayList!!.currentMusicPosition = position
                 val intent= Intent(binding.root.context, SongPlayActivity::class.java)
                 soundPresenter.getAllMusics(soundsPlayList!!)
                 ContextCompat.startActivity(binding.root.context,intent,null)
             }else {
                 Toast.makeText(it.context, "PlayList null", Toast.LENGTH_SHORT).show()
             }

         }
     }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SoundViewHolder {
        val view = ItemSoundBinding.inflate(LayoutInflater.from(
            parent.context),
            parent,
            false
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

}