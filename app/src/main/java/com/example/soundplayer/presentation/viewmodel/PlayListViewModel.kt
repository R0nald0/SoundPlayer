package com.example.soundplayer.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.soundplayer.model.PlayList

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class PlayListViewModel @Inject constructor():ViewModel() {

    private val _playLists = MutableLiveData<List<PlayList>>()
     val playLists : LiveData<List<PlayList>>
         get() = _playLists
    fun savePlayList(playList: PlayList){
        playList.listSound.forEach {
            Log.i("INFO_", "verifi savePlayList:${it.title}} ")
        }
    }
    fun getAllPlayList(){

    }
}