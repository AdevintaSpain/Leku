package com.schibstedspain.leku.geocoder

import android.location.Address
import android.location.Location

import com.google.android.gms.maps.model.LatLng
import java.util.TimeZone

interface GeocoderViewInterface {
    fun willLoadLocation()
    fun showLocations(addresses: List<Address>)
    fun showDebouncedLocations(addresses: List<Address>)
    fun didLoadLocation()
    fun showLoadLocationError()
    fun showLastLocation(location: Location)
    fun didGetLastLocation()
    fun showLocationInfo(address: Pair<Address, TimeZone?>)
    fun willGetLocationInfo(latLng: LatLng)
    fun didGetLocationInfo()
    fun showGetLocationInfoError()

    class NullView : GeocoderViewInterface {
        override fun willLoadLocation() {}
        override fun showLocations(addresses: List<Address>) {}
        override fun showDebouncedLocations(addresses: List<Address>) {}
        override fun didLoadLocation() {}
        override fun showLoadLocationError() {}
        override fun showLastLocation(location: Location) {}
        override fun didGetLastLocation() {}
        override fun showLocationInfo(address: Pair<Address, TimeZone?>) {}
        override fun willGetLocationInfo(latLng: LatLng) {}
        override fun didGetLocationInfo() {}
        override fun showGetLocationInfoError() {}
    }
}
