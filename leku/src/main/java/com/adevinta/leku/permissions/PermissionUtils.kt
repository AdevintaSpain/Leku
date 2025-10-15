package com.adevinta.leku.permissions

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object PermissionUtils {
    private fun shouldRequestPermission(
        context: Context,
        permission: String,
    ): Boolean = ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_DENIED

    fun isLocationPermissionGranted(context: Context): Boolean =
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED

    private fun requestPermission(
        activity: Activity,
        permission: String,
        requestCode: Int,
    ) {
        ActivityCompat.requestPermissions(activity, arrayOf(permission), requestCode)
    }

    fun shouldRequestLocationStoragePermission(context: Context): Boolean =
        shouldRequestPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION,
        )

    fun requestLocationPermission(activity: Activity) {
        requestPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION, 0)
    }
}
