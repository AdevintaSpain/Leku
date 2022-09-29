package com.adevinta.leku.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class ReactiveLocationProvider(
    val context: Context
) {

    @RequiresPermission(
        anyOf = ["android.permission.ACCESS_COARSE_LOCATION", "android.permission.ACCESS_FINE_LOCATION"]
    )
    suspend fun getLastKnownLocation(): Location? = suspendCoroutine { task ->
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        ) {
            LocationServices.getFusedLocationProviderClient(context)
                .lastLocation
                .addOnCompleteListener { locTask ->
                    task.resume(locTask.result)
                }
                .addOnCanceledListener {
                    task.resume(null)
                }
                .addOnFailureListener {
                    task.resume(null)
                }
        } else {
            task.resume(null)
        }
    }
}
