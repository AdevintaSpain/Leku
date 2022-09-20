package com.adevinta.leku.geocoder

import android.location.Address
import com.google.android.gms.maps.model.LatLng
import com.adevinta.leku.geocoder.api.AddressBuilder
import com.adevinta.leku.geocoder.api.NetworkClient
import com.adevinta.leku.geocoder.api.NetworkException
import com.adevinta.leku.geocoder.api.SuggestionBuilder
import java.util.Locale
import org.json.JSONException

private const val QUERY_REQUEST =
    "https://maps.googleapis.com/maps/api/geocode/json?address=%1\$s&key=%2\$s"
private const val QUERY_REQUEST_WITH_RECTANGLE =
    "https://maps.googleapis.com/maps/api/geocode/json?address=%1\$s&key=%2\$s&bounds=%3\$f,%4\$f|%5\$f,%6\$f"
private const val QUERY_LAT_LONG =
    "https://maps.googleapis.com/maps/api/geocode/json?latlng=%1\$f,%2\$f&key=%3\$s"
private const val QUERY_AUTOCOMPLETE =
    "https://maps.googleapis.com/maps/api/place/autocomplete/json?input=%1\$s&key=%2\$s"
private const val QUERY_PLACE_ID =
    "https://maps.googleapis.com/maps/api/place/details/json?place_id=%1\$s&key=%2\$s"

class GoogleGeocoderDataSource(
    private val networkClient: NetworkClient,
    private val addressBuilder: AddressBuilder,
    private val suggestionBuilder: SuggestionBuilder
) : GeocoderDataSourceInterface {

    private var geolocationApiKey: String? = null

    fun setGeolocationApiKey(apiKey: String) {
        this.geolocationApiKey = apiKey
    }

    private var placesApiKey: String? = null
    fun setPlaceApiKey(apiKey: String) {
        this.placesApiKey = apiKey
    }

    override suspend fun autoCompleteFromLocationName(query: String): List<PlaceSuggestion> {
        val suggestions = mutableListOf<PlaceSuggestion>()
        if (placesApiKey == null) {
            return suggestions
        }
        return try {
            val result = networkClient.requestFromLocationName(
                String.format(Locale.ENGLISH, QUERY_AUTOCOMPLETE, query, placesApiKey)
            )
            if (result != null) {
                suggestions.addAll(suggestionBuilder.parseResult(result))
            }
            suggestions
        } catch (e: JSONException) {
            suggestions
        } catch (e: NetworkException) {
            suggestions
        }
    }

    override suspend fun getAddressFromPlaceId(placeId: String): Address? {
        if (placesApiKey == null) {
            return null
        }
        return try {
            val result = networkClient.requestFromLocationName(
                String.format(Locale.ENGLISH, QUERY_PLACE_ID, placeId, placesApiKey)
            )
            when {
                result != null -> addressBuilder.parseResult(result)
                else -> null
            }
        } catch (e: JSONException) {
            null
        } catch (e: NetworkException) {
            null
        }
    }

    override suspend fun getFromLocationName(query: String): List<Address> {
        val addresses = mutableListOf<Address>()
        if (geolocationApiKey == null) {
            return addresses
        }
        return try {
            val result = networkClient.requestFromLocationName(
                String.format(Locale.ENGLISH, QUERY_REQUEST, query.trim { it <= ' ' }, geolocationApiKey)
            )
            if (result != null) {
                addresses.addAll(addressBuilder.parseArrayResult(result))
            }
            addresses
        } catch (e: JSONException) {
            addresses
        } catch (e: NetworkException) {
            addresses
        }
    }

    override suspend fun getFromLocationName(query: String, lowerLeft: LatLng, upperRight: LatLng): List<Address> {
        val addresses = mutableListOf<Address>()
        if (geolocationApiKey == null) {
            return addresses
        }
        return try {
            val result = networkClient.requestFromLocationName(
                String.format(
                    Locale.ENGLISH,
                    QUERY_REQUEST_WITH_RECTANGLE,
                    query.trim { it <= ' ' },
                    geolocationApiKey,
                    lowerLeft.latitude,
                    lowerLeft.longitude,
                    upperRight.latitude,
                    upperRight.longitude
                )
            )
            if (result != null) {
                addresses.addAll(addressBuilder.parseArrayResult(result))
            }
            addresses
        } catch (e: JSONException) {
            addresses
        } catch (e: NetworkException) {
            addresses
        }
    }

    override suspend fun getFromLocation(latitude: Double, longitude: Double): List<Address> {
        val addresses = mutableListOf<Address>()
        if (geolocationApiKey == null) {
            return addresses
        }
        return try {
            val result = networkClient.requestFromLocationName(
                String.format(Locale.ENGLISH, QUERY_LAT_LONG, latitude, longitude, geolocationApiKey)
            )
            if (result != null) {
                addresses.addAll(addressBuilder.parseArrayResult(result))
            }
            addresses
        } catch (e: JSONException) {
            addresses
        } catch (e: NetworkException) {
            addresses
        }
    }
}
