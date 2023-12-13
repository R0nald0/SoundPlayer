package com.example.soundplayer.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.soundplayer.R
import com.example.soundplayer.presentation.SongPlayActivity
import com.example.soundplayer.presentation.SoundPresenter
import com.example.soundplayer.databinding.ItemSoundBinding
import com.example.soundplayer.extension.convertMilesSecondToMinSec
import com.example.soundplayer.model.Sound
import com.example.soundplayer.model.SoundList

class SoundAdapter(
 private val soundPresenter: SoundPresenter
) :RecyclerView.Adapter<SoundAdapter.SoundViewHolder>() {
   private  val sounds = mutableSetOf<Sound>()
   private  var actualSound :Sound? = null

    fun addSound(list: MutableSet<Sound>){
        sounds.addAll(list)
        notifyDataSetChanged()
    }
    fun getActualSound(sound :Sound){
        actualSound = sound
        notifyDataSetChanged()
    }


    inner class  SoundViewHolder(private val binding : ItemSoundBinding): ViewHolder(binding.root){
     fun bind(soudd :Sound,position: Int){
         val duration =  binding.root.context.convertMilesSecondToMinSec(soudd.duration.toLong())
         binding.txvDuration.text = "$duration"
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
             binding.txvTitle.setTextColor(ContextCompat.getColor(binding.root.context,R.color.white))
             binding.txvTitle.isSelected = false
         }



         binding.idContraint.setOnClickListener {
             val sounds = SoundList(position,sounds)
             val intent= Intent(binding.root.context, SongPlayActivity::class.java)
             ///intent.putExtra(Constants.KEY_EXTRA_MUSIC,sounds)
             soundPresenter.getAllMusics(sounds)
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

    override fun getItemCount(): Int {
       return sounds.size
    }

    override fun onBindViewHolder(holder: SoundViewHolder, position: Int) {
        val sound = sounds.elementAt(position)
        holder.bind(sound,position)
    }

}