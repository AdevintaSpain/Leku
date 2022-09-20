package com.adevinta.leku.geocoder

import android.location.Address
import android.location.Geocoder
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.runInterruptible
import java.io.IOException

private const val MAX_RESULTS = 5

class AndroidGeocoderDataSource(private val geocoder: Geocoder) : GeocoderDataSourceInterface {
    override suspend fun autoCompleteFromLocationName(query: String): List<PlaceSuggestion> {
        return emptyList()
    }

    override suspend fun getAddressFromPlaceId(placeId: String): Address? {
        return null
    }

    override suspend fun getFromLocationName(query: String): List<Address> = runInterruptible {
        try {
            geocoder.getFromLocationName(query, MAX_RESULTS)
        } catch (exception: IOException) {
            emptyList()
        }
    }

    override suspend fun getFromLocationName(
        query: String,
        lowerLeft: LatLng,
        upperRight: LatLng
    ): List<Address> = runInterruptible {
        try {
            geocoder.getFromLocationName(
                query,
                MAX_RESULTS,
                lowerLeft.latitude,
                lowerLeft.longitude,
                upperRight.latitude,
                upperRight.longitude
            )
        } catch (exception: IOException) {
            emptyList()
        }
    }

    override suspend fun getFromLocation(latitude: Double, longitude: Double): List<Address> =
        runInterruptible {
            try {
                geocoder.getFromLocation(latitude, longitude, MAX_RESULTS)
            } catch (exception: IOException) {
                emptyList()
            }
        }
}
