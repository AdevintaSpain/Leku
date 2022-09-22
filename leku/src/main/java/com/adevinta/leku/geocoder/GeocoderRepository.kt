package com.adevinta.leku.geocoder

import android.location.Address
import com.google.android.gms.maps.model.LatLng

class GeocoderRepository(
    private val customGeocoder: GeocoderDataSourceInterface?,
    private val androidGeocoder: GeocoderDataSourceInterface,
    private val googleGeocoder: GeocoderDataSourceInterface
) {

    private val dataSources get() = listOf(customGeocoder, androidGeocoder, googleGeocoder)

    suspend fun autoCompleteFromLocationName(query: String): List<PlaceSuggestion> {
        dataSources.forEach {
            val data = it?.autoCompleteFromLocationName(query) ?: emptyList()
            if (data.isNotEmpty()) {
                return data
            }
        }
        return emptyList()
    }

    suspend fun getAddressFromPlaceId(placeId: String): Address? {
        dataSources.forEach {
            val data = it?.getAddressFromPlaceId(placeId)
            if (data != null) {
                return data
            }
        }
        return null
    }

    suspend fun getFromLocationName(query: String): List<Address> {
        dataSources.forEach {
            val data = it?.getFromLocationName(query) ?: emptyList()
            if (data.isNotEmpty()) {
                return data
            }
        }
        return emptyList()
    }

    suspend fun getFromLocationName(
        query: String,
        lowerLeft: LatLng,
        upperRight: LatLng
    ): List<Address> {
        dataSources.forEach {
            val data = it?.getFromLocationName(query, lowerLeft, upperRight) ?: emptyList()
            if (data.isNotEmpty()) {
                return data
            }
        }
        return emptyList()
    }

    suspend fun getFromLocation(latLng: LatLng): List<Address> {
        dataSources.forEach {
            val data = it?.getFromLocation(latLng.latitude, latLng.longitude) ?: emptyList()
            if (data.isNotEmpty()) {
                return data
            }
        }
        return emptyList()
    }
}
