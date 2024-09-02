package com.example.soundplayer.commons.permission

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class Permission {

    companion object{
        fun verifyIfPermissionHasDenied( activity :Activity,permission :String, gerenciarPermissoes: ActivityResultLauncher<Array<String>> , listPemissions:Set<String>,onPositiveBotton :()->Unit){
            if (shouldShowRequestPermissionRationale(activity ,permission)){
               MaterialAlertDialogBuilder(activity)
                   .setTitle("Permissão Necessária")
                   .setMessage("Precisamos da sua permissão para accessar as mídias de audio do aparelho")
                    .setPositiveButton("Aceitar"){diolog,n->
                         onPositiveBotton()
                    }
                    .setNegativeButton("Negar"){diolog,n->
                        diolog.dismiss()
                    }
                    .show()

            }else{
                gerenciarPermissoes.launch(listPemissions.toTypedArray())
            }
        }

        fun chekPerMission(context : Activity,listPemissions: Set<String>):List<String>{
           val lit= listPemissions.filter {
               Log.i("INFO_", "requestPermissionKeys: $it")
               ContextCompat.checkSelfPermission(context,it) == PackageManager.PERMISSION_DENIED
            }


            return lit
        }

        fun getPermissions(permission: Map<String, Boolean>):Boolean{
           // Log.i("INFO_", "requestPermissionKeys: ${permission.keys}")

            if (permission.values.contains(false)) {
                return false
            }
            return  true
        }
        fun  requestPermission( context: Activity, gerenciarPermissoes: ActivityResultLauncher<Array<String>> , listPemissions:Set<String> ){
                 val l = chekPerMission(context,listPemissions)
                 l.forEach {
                     verifyIfPermissionHasDenied(
                         activity = context,it, gerenciarPermissoes,listPemissions,
                         onPositiveBotton = {
                         gerenciarPermissoes.launch(l.toTypedArray())
                     })
                 }


        }
    }

}