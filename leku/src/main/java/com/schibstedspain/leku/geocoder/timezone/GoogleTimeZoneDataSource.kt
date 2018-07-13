package com.schibstedspain.leku.geocoder.timezone

import com.google.maps.GeoApiContext
import com.google.maps.TimeZoneApi
import com.google.maps.errors.ApiException
import com.google.maps.model.LatLng
import java.io.IOException
import java.util.TimeZone


class GoogleTimeZoneDataSource(private val geoApiContext: GeoApiContext) {

    fun getTimeZone(latitude: Double, longitude: Double): TimeZone? {
        try {
            return TimeZoneApi.getTimeZone(geoApiContext, LatLng(latitude, longitude)).await()
        } catch (error: ApiException) {
        } catch (ignored: InterruptedException) {
        } catch (ignored: IOException) {
        }
        return null
    }
}
