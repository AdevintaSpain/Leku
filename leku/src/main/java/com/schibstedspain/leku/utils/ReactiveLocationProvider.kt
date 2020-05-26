package com.schibstedspain.leku.utils

import android.content.Context
import android.location.Location
import androidx.annotation.RequiresPermission
import io.reactivex.rxjava3.core.Single

class ReactiveLocationProvider(val context: Context) {

    @RequiresPermission(anyOf = ["android.permission.ACCESS_COARSE_LOCATION", "android.permission.ACCESS_FINE_LOCATION"])
    fun getLastKnownLocation(): Single<Location> {
        return LastKnownLocationObservableOnSubscribe.createObservable(context).singleOrError()
    }
}
