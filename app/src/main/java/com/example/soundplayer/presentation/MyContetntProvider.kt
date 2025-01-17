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
            MediaStore.Audio.Artists.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.VOLUME_NAME,
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
       try {
           val id =cursor.getColumnIndexOrThrow( MediaStore.Audio.Media._ID)
           val albumid =cursor.getColumnIndexOrThrow( MediaStore.Audio.Media.ALBUM_ID)
           val duarution = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
           val path  = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
           val title = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
           val artistIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists.ARTIST)
           val albumIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
           val artistIndexMedia = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
           val volumeNameIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.VOLUME_NAME)



           while (cursor.moveToNext()){

               Log.i("INFO_", "getListOfSound: artistIndexMedia ${cursor.getString(artistIndexMedia)}")
               Log.i("INFO_", "getListOfSound: volumeNameIndex ${cursor.getString(volumeNameIndex)}")

               val idMedia = cursor.getLong(id)
               val albumidMedia = cursor.getLong(albumid)
               val mediaUriAlbum  = ContentUris.withAppendedId(
                   Uri.parse("content://media/external/audio/albumart"),albumidMedia)
              // Log.d("INFO_", "contentProvider id sound: $idMedia ")
               val mediaUri  = ContentUris.withAppendedId(
                   MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,idMedia)

//                  val itemPathAlbumMedia = mediaUriAlbum.pathSegments[3].toInt()
          //    val newMediaAlbum =if(itemPathAlbumMedia == 1 || itemPathAlbumMedia ==3)  null else  mediaUriAlbum
               val sound = Sound(
                   idSound =idMedia,
                   path = cursor.getString(path),
                   artistName = cursor.getString(artistIndex),
                   albumName = cursor.getString(albumIndex),
                   duration =cursor.getInt(duarution).toString(),
                   title= cursor.getString(title),
                   uriMedia = mediaUri.toString(),
                   uriMediaAlbum = mediaUriAlbum.toString(),
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
       }
       catch ( generalExeption : Exception){
           generalExeption.printStackTrace()
           Log.e("Error", "generalExeption: ${generalExeption.message}", )
       }
       return MyContetntProvider
   }
}