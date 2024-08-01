package com.example.soundplayer.presentation

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import com.example.soundplayer.commons.extension.exibirToast
import com.example.soundplayer.model.Sound
import java.io.File

object MyContetntProvider {
    lateinit var  cursor  : Cursor
    private var _listSoundFromContentProvider = mutableSetOf<Sound>()
     var listSoundFromContentProvider  : Set<Sound> = _listSoundFromContentProvider


    fun createData(context: Context) : MyContetntProvider{
        val projection = arrayOf(
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.ALBUM_ID,

            )
         cursor = context.contentResolver.query(
             MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
             projection,
             null,
             null,
             null
         )!!

        return MyContetntProvider
    }



   fun getListOfSound(context: Context):MyContetntProvider {
       if (cursor != null) {
           try {
               val id =cursor.getColumnIndexOrThrow( MediaStore.Audio.Media._ID)
               val albumid =cursor.getColumnIndexOrThrow( MediaStore.Audio.Media.ALBUM_ID)
               val duarution = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
               val path  = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
               val title = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)


               while (cursor.moveToNext()){

                   val idMedia = cursor.getLong(id)
                   val albumidMedia = cursor.getLong(albumid)
                   val mediaUriAlbum  = ContentUris.withAppendedId(
                       Uri.parse("content://media/external/audio/albumart"),albumidMedia)

                   val mediaUri  = ContentUris.withAppendedId(
                       MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,idMedia)

//                  val itemPathAlbumMedia = mediaUriAlbum.pathSegments[3].toInt()
              //    val newMediaAlbum =if(itemPathAlbumMedia == 1 || itemPathAlbumMedia ==3)  null else  mediaUriAlbum
                   val sound = Sound(
                       idSound = null,
                       path = cursor.getString(path),
                       duration =cursor.getInt(duarution).toString(),
                       title= cursor.getString(title),
                       uriMedia = mediaUri,
                       uriMediaAlbum = mediaUriAlbum,
                       insertedDate = null
                   )

                   if (File(sound.path).exists()){
                       if (!_listSoundFromContentProvider.contains(sound)){
                           _listSoundFromContentProvider.add(sound)
                       }
                   }
               }
               cursor.close()

           }catch (nullPointer : NullPointerException){
               nullPointer.printStackTrace()
               context.exibirToast( "Null ${nullPointer.printStackTrace()}")
           }catch (  illegalArgumentException: IllegalArgumentException){
               Log.e("Error", "illegalArgumentException: ${illegalArgumentException.message}", )
               illegalArgumentException.printStackTrace()
               context.exibirToast( "Null ${illegalArgumentException.printStackTrace()}")
           }
           catch ( fileNotFound : Exception){
               Log.e("Error", "FILE NOT FOUND: ${fileNotFound.message}", )
               fileNotFound.printStackTrace()
               context.exibirToast( "Null ${fileNotFound.printStackTrace()}")
           }
       }
       return MyContetntProvider
   }
}