package com.adevinta.leku.geocoder

import android.location.Address
import com.google.android.gms.maps.model.LatLng

interface GeocoderDataSourceInterface {
    suspend fun autoCompleteFromLocationName(query: String): List<PlaceSuggestion>

    suspend fun getAddressFromPlaceId(placeId: String): Address?

    suspend fun getFromLocationName(query: String): List<Address>

    suspend fun getFromLocationName(query: String, lowerLeft: LatLng, upperRight: LatLng): List<Address>

    suspend fun getFromLocation(latitude: Double, longitude: Double): List<Address>
}
