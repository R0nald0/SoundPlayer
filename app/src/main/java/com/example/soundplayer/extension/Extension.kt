package com.example.soundplayer.extension

import android.content.Context
import java.util.concurrent.TimeUnit

fun Context.convertMilesSecondToMinSec(duration: Long): String {
    return String.format(
        "%02d:%02d",
        TimeUnit.MILLISECONDS.toMinutes(duration) % TimeUnit.HOURS.toMinutes(1),
        TimeUnit.MILLISECONDS.toSeconds(duration) % TimeUnit.MINUTES.toSeconds(1)
    )
}
