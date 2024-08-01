package com.example.soundplayer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class SoundPlayerReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // This method is called when the BroadcastReceiver is receiving an Intent broadcast.
        val action = intent.action
       val resul  = when (action){
            Intent.ACTION_MEDIA_SCANNER_STARTED -> "Action media scanner"
            Intent.ACTION_MEDIA_SHARED -> "Action media shared"
            Intent.ACTION_BATTERY_LOW ->  "Action batterry low"

           else -> ""
       }
        Toast.makeText(context, resul, Toast.LENGTH_LONG).show()
    }
}