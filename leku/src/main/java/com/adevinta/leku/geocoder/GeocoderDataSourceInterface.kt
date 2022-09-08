package com.adevinta.leku.geocoder

import android.location.Address
import com.google.android.gms.maps.model.LatLng

interface GeocoderDataSourceInterface {
    fun autoCompleteFromLocationName(query: String): List<PlaceSuggestion>

    fun getAddressFromPlaceId(placeId: String): Address?

    fun getFromLocationName(query: String): List<Address>

    fun getFromLocationName(query: String, lowerLeft: LatLng, upperRight: LatLng): List<Address>

    fun getFromLocation(latitude: Double, longitude: Double): List<Address>
}
