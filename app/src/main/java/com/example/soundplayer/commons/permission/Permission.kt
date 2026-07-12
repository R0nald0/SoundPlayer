package com.example.soundplayer.commons.permission

import android.app.Activity
import android.content.pm.PackageManager
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.core.content.ContextCompat
import com.example.soundplayer.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class Permission {
    companion object {
        fun requestWithRationaleIfNeeded(
            activity: Activity,
            permission: String,
            gerenciarPermissoes: ActivityResultLauncher<Array<String>>,
            permissions: Set<String>,
            onPositiveButton: () -> Unit,
        ) {
            if (shouldShowRequestPermissionRationale(activity, permission)) {
                MaterialAlertDialogBuilder(activity)
                    .setTitle(activity.getString(R.string.permission_required_title))
                    .setMessage(activity.getString(R.string.permission_required_audio_message))
                    .setPositiveButton(activity.getString(R.string.accept)) { dialog, _ ->
                        onPositiveButton()
                        dialog.dismiss()
                    }.setNegativeButton(activity.getString(R.string.deny)) { dialog, _ ->
                        dialog.dismiss()
                    }.show()
            } else {
                gerenciarPermissoes.launch(permissions.toTypedArray())
            }
        }

        fun checkPermissions(
            context: Activity,
            permissions: Set<String>,
        ): List<String> =
            permissions.filter {
                ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_DENIED
            }

        fun getPermissions(permission: Map<String, Boolean>): Boolean = !permission.values.contains(false)

        fun requestPermission(
            context: Activity,
            gerenciarPermissoes: ActivityResultLauncher<Array<String>>,
            permissions: Set<String>,
        ) {
            val missingPermissions = checkPermissions(context, permissions)
            if (missingPermissions.isEmpty()) return

            requestWithRationaleIfNeeded(
                activity = context,
                permission = missingPermissions.first(),
                gerenciarPermissoes = gerenciarPermissoes,
                permissions = permissions,
                onPositiveButton = {
                    gerenciarPermissoes.launch(missingPermissions.toTypedArray())
                },
            )
        }
    }
}
