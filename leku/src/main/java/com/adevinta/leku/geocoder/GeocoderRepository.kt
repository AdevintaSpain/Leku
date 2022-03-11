package com.adevinta.leku.geocoder

import android.location.Address
import com.google.android.gms.maps.model.LatLng
import io.reactivex.rxjava3.core.Single

private const val RETRY_COUNT = 3

class GeocoderRepository(
    private val androidGeocoderInterface: GeocoderDataSourceInterface,
    private val googleGeocoderInterface: GeocoderDataSourceInterface
) {

    fun getFromLocationName(query: String): Single<List<Address>> {
        return androidGeocoderInterface.getFromLocationName(query)
                .retry(RETRY_COUNT.toLong())
                .onErrorResumeWith(googleGeocoderInterface.getFromLocationName(query))
    }

    fun getFromLocationName(query: String, lowerLeft: LatLng, upperRight: LatLng): Single<List<Address>> {
        return androidGeocoderInterface.getFromLocationName(query, lowerLeft, upperRight)
                .retry(RETRY_COUNT.toLong())
                .onErrorResumeWith(googleGeocoderInterface.getFromLocationName(query, lowerLeft, upperRight))
    }

    fun getFromLocation(latLng: LatLng): Single<List<Address>> {
        return androidGeocoderInterface.getFromLocation(latLng.latitude, latLng.longitude)
                .retry(RETRY_COUNT.toLong())
                .onErrorResumeWith(googleGeocoderInterface.getFromLocation(latLng.latitude, latLng.longitude))
    }
}
