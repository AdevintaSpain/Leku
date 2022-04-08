package com.adevinta.leku.geocoder

import android.location.Address
import android.location.Geocoder
import com.google.android.gms.maps.model.LatLng
import java.io.IOException

private const val MAX_RESULTS = 5

class AndroidGeocoderDataSource(private val geocoder: Geocoder) : GeocoderDataSourceInterface {

    override fun getFromLocationName(query: String): List<Address> {
        return try {
            geocoder.getFromLocationName(query, MAX_RESULTS)
        } catch (exception: IOException) {
            emptyList()
        }
    }

    override fun getFromLocationName(query: String, lowerLeft: LatLng, upperRight: LatLng): List<Address> {
        return try {
            geocoder.getFromLocationName(
                query, MAX_RESULTS, lowerLeft.latitude, lowerLeft.longitude, upperRight.latitude, upperRight.longitude
            )
        } catch (exception: IOException) {
            emptyList()
        }
    }

    override fun getFromLocation(latitude: Double, longitude: Double): List<Address> {
        return try {
            geocoder.getFromLocation(latitude, longitude, MAX_RESULTS)
        } catch (exception: IOException) {
            emptyList()
        }
    }
}
