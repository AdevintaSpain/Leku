package com.adevinta.leku.geocoder

import android.location.Address
import com.google.android.gms.maps.model.LatLng
import com.adevinta.leku.geocoder.api.AddressBuilder
import com.adevinta.leku.geocoder.api.NetworkClient
import com.adevinta.leku.geocoder.api.NetworkException
import java.util.Locale
import org.json.JSONException

private const val QUERY_REQUEST = "https://maps.googleapis.com/maps/api/geocode/json?address=%1\$s&key=%2\$s"
private const val QUERY_REQUEST_WITH_RECTANGLE =
    "https://maps.googleapis.com/maps/api/geocode/json?address=%1\$s&key=%2\$s&bounds=%3\$f,%4\$f|%5\$f,%6\$f"
private const val QUERY_LAT_LONG = "https://maps.googleapis.com/maps/api/geocode/json?latlng=%1\$f,%2\$f&key=%3\$s"

class GoogleGeocoderDataSource(
    private val networkClient: NetworkClient,
    private val addressBuilder: AddressBuilder
) : GeocoderDataSourceInterface {

    private var apiKey: String? = null

    fun setApiKey(apiKey: String) {
        this.apiKey = apiKey
    }

    override fun getFromLocationName(query: String): List<Address> {
        val addresses = mutableListOf<Address>()
        if (apiKey == null) {
            return addresses
        }
        try {
            val result = networkClient.requestFromLocationName(
                String.format(Locale.ENGLISH, QUERY_REQUEST, query.trim { it <= ' ' }, apiKey)
            )
            if (result != null) {
                addresses.addAll(addressBuilder.parseResult(result))
            }
            addresses
        } catch (e: JSONException) {
            addresses
        } catch (e: NetworkException) {
            addresses
        }

        return addresses
    }

    override fun getFromLocationName(query: String, lowerLeft: LatLng, upperRight: LatLng): List<Address> {
        val addresses = mutableListOf<Address>()
        if (apiKey == null) {
            return addresses
        }
        try {
            val result = networkClient.requestFromLocationName(
                String.format(
                    Locale.ENGLISH,
                    QUERY_REQUEST_WITH_RECTANGLE,
                    query.trim { it <= ' ' },
                    apiKey,
                    lowerLeft.latitude,
                    lowerLeft.longitude,
                    upperRight.latitude,
                    upperRight.longitude
                )
            )
            if (result != null) {
                addresses.addAll(addressBuilder.parseResult(result))
            }
            addresses
        } catch (e: JSONException) {
            addresses
        } catch (e: NetworkException) {
            addresses
        }

        return addresses
    }

    override fun getFromLocation(latitude: Double, longitude: Double): List<Address> {
        val addresses = mutableListOf<Address>()
        if (apiKey == null) {
            return addresses
        }
        try {
            val result = networkClient.requestFromLocationName(
                String.format(Locale.ENGLISH, QUERY_LAT_LONG, latitude, longitude, apiKey)
            )
            if (result != null) {
                addresses.addAll(addressBuilder.parseResult(result))
            }
            addresses
        } catch (e: JSONException) {
            addresses
        } catch (e: NetworkException) {
            addresses
        }

        return addresses
    }
}
