package com.adevinta.leku.geocoder

import android.location.Address
import com.google.android.gms.maps.model.LatLng
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class GeocoderRepository(
    private val androidGeocoder: GeocoderDataSourceInterface,
    private val googleGeocoder: GeocoderDataSourceInterface
) {

    suspend fun getFromLocationName(query: String): List<Address> {
        return suspendCoroutine { continuation ->
            val addressList = androidGeocoder.getFromLocationName(query)
            if (addressList.isNullOrEmpty()) {
                continuation.resume(googleGeocoder.getFromLocationName(query))
            } else {
                continuation.resume(addressList)
            }
        }
    }

    suspend fun getFromLocationName(query: String, lowerLeft: LatLng, upperRight: LatLng): List<Address> {
        return suspendCoroutine { continuation ->
            val addressList = androidGeocoder.getFromLocationName(query, lowerLeft, upperRight)
            if (addressList.isNullOrEmpty()) {
                continuation.resume(googleGeocoder.getFromLocationName(query, lowerLeft, upperRight))
            } else {
                continuation.resume(addressList)
            }
        }
    }

    suspend fun getFromLocation(latLng: LatLng): List<Address> {
        return suspendCoroutine { continuation ->
            val addressList = androidGeocoder.getFromLocation(latLng.latitude, latLng.longitude)
            if (addressList.isNullOrEmpty()) {
                continuation.resume(googleGeocoder.getFromLocation(latLng.latitude, latLng.longitude))
            } else {
                continuation.resume(addressList)
            }
        }
    }
}
