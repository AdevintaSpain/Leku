package com.adevinta.leku.geocoder

import android.location.Address
import com.google.android.gms.maps.model.LatLng
import io.reactivex.rxjava3.core.Single

interface GeocoderDataSourceInterface {
    fun getFromLocationName(query: String): Single<List<Address>>

    fun getFromLocationName(query: String, lowerLeft: LatLng, upperRight: LatLng): Single<List<Address>>

    fun getFromLocation(latitude: Double, longitude: Double): Single<List<Address>>
}
