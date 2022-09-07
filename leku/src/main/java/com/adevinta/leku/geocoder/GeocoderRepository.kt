package com.adevinta.leku.geocoder

import android.location.Address
import com.google.android.gms.maps.model.LatLng
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class GeocoderRepository(
    private val customGeocoder: GeocoderDataSourceInterface?,
    private val androidGeocoder: GeocoderDataSourceInterface,
    private val googleGeocoder: GeocoderDataSourceInterface
) {

    private val dataSources get() = listOf(customGeocoder, androidGeocoder, googleGeocoder)

    suspend fun getFromLocationName(query: String): List<Address> {
        return suspendCoroutine { continuation ->
            dataSources.forEach {
                val data = it?.getFromLocationName(query) ?: emptyList()
                if (data.isNotEmpty()) {
                    continuation.resume(data)
                    return@suspendCoroutine
                }
            }
            continuation.resume(emptyList())
        }
    }

    suspend fun getFromLocationName(
        query: String,
        lowerLeft: LatLng,
        upperRight: LatLng
    ): List<Address> {
        return suspendCoroutine { continuation ->
            dataSources.forEach {
                val data = it?.getFromLocationName(query, lowerLeft, upperRight) ?: emptyList()
                if (data.isNotEmpty()) {
                    continuation.resume(data)
                    return@suspendCoroutine
                }
            }
            continuation.resume(emptyList())
        }
    }

    suspend fun getFromLocation(latLng: LatLng): List<Address> {
        return suspendCoroutine { continuation ->
            dataSources.forEach {
                val data = it?.getFromLocation(latLng.latitude, latLng.longitude) ?: emptyList()
                if (data.isNotEmpty()) {
                    continuation.resume(data)
                    return@suspendCoroutine
                }
            }
            continuation.resume(emptyList())
        }
    }
}
