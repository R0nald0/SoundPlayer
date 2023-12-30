package com.example.soundplayer.commons.extension

import android.app.AlertDialog
import android.content.Context
import java.util.concurrent.TimeUnit

fun Context.convertMilesSecondToMinSec(duration: Long): String {
    return String.format(
        "%02d:%02d",
        TimeUnit.MILLISECONDS.toMinutes(duration) % TimeUnit.HOURS.toMinutes(1),
        TimeUnit.MILLISECONDS.toSeconds(duration) % TimeUnit.MINUTES.toSeconds(1)
    )
}

fun Context.showAlerDialog(messenger :String, onPositive :()->Unit){
    AlertDialog.Builder(this)
        .setMessage(messenger)
        .setNegativeButton("Não") { dialog, id ->
            dialog.cancel()
            dialog.dismiss()
        }
        .setPositiveButton("Sim"){dialog, id ->
            dialog.cancel()
            dialog.dismiss()
            onPositive()
        }.create().show()
}
