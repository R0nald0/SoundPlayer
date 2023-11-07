package com.example.soundplayer.adapter

import android.content.Intent
import android.media.MediaPlayer
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.soundplayer.R
import com.example.soundplayer.SongPlayActivity
import com.example.soundplayer.SoundPresenter
import com.example.soundplayer.constants.Constants
import com.example.soundplayer.databinding.ItemSoundBinding
import com.example.soundplayer.model.Sound
import com.example.soundplayer.model.SoundList
import javax.inject.Inject

class SoundAdapter(

) :RecyclerView.Adapter<SoundAdapter.SoundViewHolder>() {
   private  val sounds = mutableListOf<Sound>()
   private  var actualSound :Sound? = null

    fun addSound(list: MutableList<Sound>){
        sounds.addAll(list)
        notifyDataSetChanged()
    }
    fun getActualSound(sound :Sound){
        actualSound = sound
        notifyDataSetChanged()
    }


    inner class  SoundViewHolder(private val binding : ItemSoundBinding): ViewHolder(binding.root){
     fun bind(soudd :Sound,position: Int){
         binding.txvDuration.text = soudd.duration
         binding.txvTitle.text =soudd.title


         if (actualSound!=null && actualSound?.title == soudd.title){
             binding.txvTitle.setTextColor(ContextCompat.getColor(binding.root.context,R.color.red))
         }else{
             binding.txvTitle.setTextColor(ContextCompat.getColor(binding.root.context,R.color.black))
         }

         binding.idContraint.setOnClickListener {

             val intent= Intent(binding.root.context,SongPlayActivity::class.java)
             intent.putExtra(Constants.KEY_EXTRA_MUSIC,SoundList(position,sounds))
             ContextCompat.startActivity(binding.root.context,intent,null)
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

    override fun getItemCount()= sounds.size

    override fun onBindViewHolder(holder: SoundViewHolder, position: Int) {
        val sound = sounds[position]
        holder.bind(sound,position)
    }

}