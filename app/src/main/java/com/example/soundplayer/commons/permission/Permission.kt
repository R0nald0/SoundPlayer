package com.example.soundplayer.commons.permission

import androidx.activity.result.ActivityResultLauncher

class Permission {

    companion object{
        fun getPermissions(permission: Map<String, Boolean>):Boolean{

                if (!permission.values.contains(false)) return  true
                else return false

        }
        fun  requestPermission( gerenciarPermissoes: ActivityResultLauncher<Array<String>> , listOfPermssions :Set<String>){
            gerenciarPermissoes.launch(listOfPermssions.toTypedArray())
        }
    }

}