package com.schibstedspain.leku.geocoder.timezone

import android.content.Context
import com.google.maps.GeoApiContext
import com.google.maps.TimeZoneApi
import com.google.maps.errors.ApiException
import com.google.maps.model.LatLng
import java.io.IOException
import java.util.TimeZone
import android.content.pm.PackageManager

class GoogleTimeZoneDataSource(private val geoApiContext: GeoApiContext) {

    companion object {
        fun getApiKey(context: Context): String? {
            try {
                val appInfo = context.packageManager.getApplicationInfo(
                        context.packageName, PackageManager.GET_META_DATA)
                if (appInfo.metaData != null) {
                    return appInfo.metaData.getString("com.google.android.geo.API_KEY")
                }
            } catch (ignored: PackageManager.NameNotFoundException) {
            }

            return null
        }
    }

    fun getTimeZone(latitude: Double, longitude: Double): TimeZone? {
        try {
            return TimeZoneApi.getTimeZone(geoApiContext, LatLng(latitude, longitude)).await()
        } catch (ignored: ApiException) {
        } catch (ignored: InterruptedException) {
        } catch (ignored: IOException) {
        }
        return null
    }
}
