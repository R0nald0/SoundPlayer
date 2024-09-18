package com.example.soundplayer.commons

import com.example.soundplayer.model.Sound

sealed interface UiState  {
   data class InitialState(val dados :List<Sound>) : UiState
   data class Sucesso(val dados :List<Sound>) : UiState
   data  class Erro(val mensagem : String) : UiState
}
