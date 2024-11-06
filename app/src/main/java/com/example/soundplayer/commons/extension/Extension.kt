package com.example.soundplayer.commons.extension

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.media3.common.MediaItem
import com.example.soundplayer.model.Sound
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import java.util.concurrent.TimeUnit


@SuppressLint("DefaultLocale")
fun Long.convertMilesSecondToMinSec(): String {
    return String.format(
        "%02d:%02d",
        TimeUnit.MILLISECONDS.toMinutes(this) % TimeUnit.HOURS.toMinutes(1),
        TimeUnit.MILLISECONDS.toSeconds(this) % TimeUnit.MINUTES.toSeconds(1)
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

fun View.showMaterialDialog(
    title:String,
    message : String? = null,
    colorTextButtonPositive :Int? = null,
    colorTextButtonNegative :Int? = null,
    positiveButtonTitle: String= "Sim",
    negativeButtonTitle: String= "Não",
    onPositiveButton:()->Unit,
    onNegativeButton:()->Unit,
){
 val dialog  =  MaterialAlertDialogBuilder(this.context)
        .setTitle(title)
        .setMessage(message)
        .setNegativeButton(negativeButtonTitle){dialog,_ ->
             onNegativeButton()
            dialog.dismiss()
        }
        .setPositiveButton(positiveButtonTitle){dialog,_->
            onPositiveButton()
            dialog.dismiss()
        }
        .show()

    if (colorTextButtonPositive != null) {
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(colorTextButtonPositive)
    }
    if (colorTextButtonNegative != null) {
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(colorTextButtonNegative)
    }
}


fun View.snackBarSound(
    messages:String,
    backGroundColor :Int =Color.GREEN,
    textColor :Int =Color.WHITE,
    duration:Int = Snackbar.LENGTH_LONG,
    animationSnackbarMode : Int = Snackbar.ANIMATION_MODE_FADE,
    actionText:String? =null,
    onClick: ((View?) -> Unit)? = null
){
    Snackbar.make(
        this,messages, duration
    ).setAction(actionText,onClick)
        .setTextColor(textColor)
        .setDuration(duration)
        .setAnimationMode(animationSnackbarMode)
        .setBackgroundTint(backGroundColor)
        .show()

}

fun  MediaItem.toSound(duration: Long,insertedDate : Long?,path:String) = Sound(
    idSound = this.mediaId.toLong(),
    artistName = mediaMetadata.artist.toString() ,
    albumName = mediaMetadata.albumTitle.toString() ,
    path = path,
    title = mediaMetadata.displayTitle.toString(),
    duration = duration.convertMilesSecondToMinSec() ,
    uriMediaAlbum = mediaMetadata.artworkUri,
    insertedDate = insertedDate
)


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

fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }
    })
}