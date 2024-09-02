package com.example.soundplayer.commons.extension

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.snackbar.Snackbar
import java.util.concurrent.TimeUnit

fun Context.convertMilesSecondToMinSec(duration: Long): String {
    return String.format(
        "%02d:%02d",
        TimeUnit.MILLISECONDS.toMinutes(duration) % TimeUnit.HOURS.toMinutes(1),
        TimeUnit.MILLISECONDS.toSeconds(duration) % TimeUnit.MINUTES.toSeconds(1)
    )
}

fun Context.showAlerDialog(messenger :String, positiveButton:String, negativeButton:String, layoutResid : View?, onPositive :()->Unit){
    if (layoutResid != null) {

        AlertDialog.Builder(this)
            .setView(layoutResid)
            .setMessage(messenger)
            .setNegativeButton(negativeButton) { dialog, id ->
                dialog.cancel()
                dialog.dismiss()
            }
            .setPositiveButton(positiveButton){dialog, id ->
                dialog.cancel()
                dialog.dismiss()
                onPositive()
            }.create().show()
    }
}
fun View.snackBarSound(
    messages:String,
    backGroundColor :Int =Color.GREEN,
    duration:Int = Snackbar.LENGTH_LONG,
    actionText:String? =null,
    onClick: ((View?) -> Unit)? = null
){
    Snackbar.make(
        this,messages, duration
    ).setAction(actionText,onClick)
        .setTextColor(Color.WHITE)
        .setBackgroundTint(backGroundColor)
        .show()

}

fun Context.exibirToast(messenger: String){
    Toast.makeText(this, messenger, Toast.LENGTH_LONG).show()
}



fun Activity.checkThemeMode(){
   val a =applicationContext.resources.configuration.uiMode and
            Configuration.UI_MODE_NIGHT_MASK ==
            Configuration.UI_MODE_NIGHT_YES

    if (a) AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
    else AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
}
