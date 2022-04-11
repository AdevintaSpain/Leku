package com.adevinta.leku.utils

import android.content.Context
import android.location.Location
import androidx.annotation.RequiresPermission

class ReactiveLocationProvider(val context: Context) {

    @RequiresPermission(
        anyOf = ["android.permission.ACCESS_COARSE_LOCATION", "android.permission.ACCESS_FINE_LOCATION"]
    )
    fun getLastKnownLocation(): Location {
        return LastKnownLocationObservableOnSubscribe.createObservable(context).blockingFirst()
    }
}
