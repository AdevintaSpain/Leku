package com.schibstedspain.leku.geocoder

import android.location.Address
import com.google.android.gms.maps.model.LatLng
import com.schibstedspain.leku.geocoder.api.AddressBuilder
import com.schibstedspain.leku.geocoder.api.NetworkClient
import io.reactivex.Observable
import java.util.Locale
import org.json.JSONException

private const val QUERY_REQUEST = "https://maps.googleapis.com/maps/api/geocode/json?address=%1\$s&key=%2\$s"
private const val QUERY_REQUEST_WITH_RECTANGLE =
        "https://maps.googleapis.com/maps/api/geocode/json?address=%1\$s&key=%2\$s&bounds=%3\$f,%4\$f|%5\$f,%6\$f"
private const val QUERY_LAT_LONG = "https://maps.googleapis.com/maps/api/geocode/json?latlng=%1\$f,%2\$f&key=%3\$s"

class GoogleGeocoderDataSource(
    private val networkClient: NetworkClient,
    private val addressBuilder: AddressBuilder
) : GeocoderInteractorDataSource {

    private var apiKey: String? = null

    fun setApiKey(apiKey: String) {
        this.apiKey = apiKey
    }

    override fun getFromLocationName(query: String): Observable<List<Address>> {
        return Observable.create { subscriber ->
            if (apiKey == null) {
                subscriber.onComplete()
            }
            try {
                val result = networkClient.requestFromLocationName(String.format(Locale.ENGLISH,
                        QUERY_REQUEST, query.trim { it <= ' ' }, apiKey))
                val addresses = addressBuilder.parseResult(result!!)
                subscriber.onNext(addresses)
                subscriber.onComplete()
            } catch (e: JSONException) {
                subscriber.onError(e)
            }
        }
    }

    override fun getFromLocationName(query: String, lowerLeft: LatLng, upperRight: LatLng): Observable<List<Address>> {
        return Observable.create { subscriber ->
            if (apiKey == null) {
                subscriber.onComplete()
            }
            try {
                val result = networkClient.requestFromLocationName(String.format(Locale.ENGLISH,
                        QUERY_REQUEST_WITH_RECTANGLE, query.trim { it <= ' ' }, apiKey, lowerLeft.latitude,
                        lowerLeft.longitude, upperRight.latitude, upperRight.longitude))
                val addresses = addressBuilder.parseResult(result!!)
                subscriber.onNext(addresses)
                subscriber.onComplete()
            } catch (e: JSONException) {
                subscriber.onError(e)
            }
        }
    }

    override fun getFromLocation(latitude: Double, longitude: Double): Observable<List<Address>> {
        return Observable.create { subscriber ->
            if (apiKey == null) {
                subscriber.onComplete()
            }
            try {
                val result = networkClient.requestFromLocationName(String.format(Locale.ENGLISH,
                        QUERY_LAT_LONG, latitude, longitude, apiKey))
                val addresses = addressBuilder.parseResult(result!!)
                subscriber.onNext(addresses)
                subscriber.onComplete()
            } catch (e: JSONException) {
                subscriber.onError(e)
            }
        }
    }
}
