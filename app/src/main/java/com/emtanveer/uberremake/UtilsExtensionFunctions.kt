package com.emtanveer.uberremake

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat

@TargetApi(30)
fun Activity.checkBackgroundLocationPermissionAPI30(backgroundLocationRequestCode: Int) {
    if (checkSinglePermission(Manifest.permission.ACCESS_FINE_LOCATION)) return
    AlertDialog.Builder(this)
        .setTitle("")
        .setMessage("")
        .setPositiveButton("Yes") { _,_ ->
            // this request will take user to Application's Setting page
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), backgroundLocationRequestCode)
        }
        .setNegativeButton("No") { dialog,_ ->
            dialog.dismiss()
        }
        .create()
        .show()

}

fun Context.checkSinglePermission(permission: String) : Boolean {
    return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
}
