package com.schibstedspain.leku.placesautocomplete

import com.schibstedspain.leku.placesautocomplete.exception.InitializationException
import com.schibstedspain.leku.placesautocomplete.listener.OnPlacesDetailsListener
import com.schibstedspain.leku.placesautocomplete.model.Address
import com.schibstedspain.leku.placesautocomplete.model.Place
import com.schibstedspain.leku.placesautocomplete.model.PlaceDetails
import android.content.Context
import android.text.TextUtils
import android.util.Log
import androidx.annotation.Nullable
import com.schibstedspain.leku.R
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.net.URLEncoder

class PlaceAPI private constructor(
    var apiKey: String?, var sessionToken: String?, var appContext: Context
) {
    /**
     * Used to get details for the places api to be showed in the auto complete list
     */
    internal fun autocomplete(input: String): ArrayList<Place>? {
        checkInitialization()
        val resultList: ArrayList<Place>? = null
        var conn: HttpURLConnection? = null
        val jsonResults = StringBuilder()
        try {
            val sb = buildApiUrl(PLACES_API_BASE + TYPE_AUTOCOMPLETE + OUT_JSON)
            sb.append("&input=" + URLEncoder.encode(input, "utf8"))
            sb.append("&components=country:in")
            val url = URL(sb.toString())
            conn = url.openConnection() as HttpURLConnection
            val inputStreamReader = InputStreamReader(conn.inputStream)
            constructData(inputStreamReader, jsonResults)
        } catch (e: Exception) {
            when (e) {
                is MalformedURLException -> logError(e, R.string.leku_error_processing_places_api)
                is IOException -> logError(e, R.string.leku_error_connecting_to_places_api)
            }
            return resultList
        } finally {
            conn?.disconnect()
        }
        return parseAutoCompleteData(jsonResults)
    }

    /**
     * Fetches the details of the place
     */
    @Nullable
    fun fetchPlaceDetails(placeId: String, listener: OnPlacesDetailsListener) {
        checkInitialization()
        Thread(Runnable {
            var conn: HttpURLConnection? = null
            val jsonResults = StringBuilder()
            try {
                val sb = buildApiUrl(PLACES_API_BASE + TYPE_DETAIL + OUT_JSON)
                sb.append("$PARAM_PLACE_ID$placeId")
                conn = URL(sb.toString()).openConnection() as? HttpURLConnection
                val inputStreamReader = InputStreamReader(conn?.inputStream)
                constructData(inputStreamReader, jsonResults)
                parseDetailsData(jsonResults, listener)
            } catch (e: Exception) {
                when (e) {
                    is JSONException -> parseDetailsError(jsonResults, listener, e)
                    is MalformedURLException -> showDetailsError(
                        R.string.leku_error_processing_places_api, listener, e
                    )
                    is IOException -> showDetailsError(
                        R.string.leku_error_connecting_to_places_api,
                        listener,
                        e
                    )
                }
            } finally {
                conn?.disconnect()
            }
        }).start()
    }

    private fun checkInitialization() {
        if (TextUtils.isEmpty(apiKey)) {
            throw InitializationException(appContext.getString(R.string.leku_error_lib_not_initialized))
        }
    }

    private fun buildApiUrl(apiUrl: String): StringBuilder {
        val sb = StringBuilder(apiUrl)
        sb.append("?key=$apiKey")
        if (!TextUtils.isEmpty(sessionToken)) {
            sb.append("&sessiontoken=$sessionToken")
        }
        return sb
    }

    private fun logError(e: Exception, resource: Int) {
        Log.e(TAG, appContext.getString(resource), e)
    }

    private fun parseAutoCompleteData(jsonResults: StringBuilder): ArrayList<Place>? {
        var resultList: ArrayList<Place>? = ArrayList()
        try {
            val jsonObj = JSONObject(jsonResults.toString())
            val predsJsonArray = jsonObj.getJSONArray("predictions")
            resultList = ArrayList(predsJsonArray.length())
            for (i in 0 until predsJsonArray.length()) {
                resultList.add(
                    Place(
                        predsJsonArray.getJSONObject(i).getString("place_id"),
                        predsJsonArray.getJSONObject(i).getString("description")
                    )
                )
            }
            return resultList
        } catch (e: JSONException) {
            val errorJson = JSONObject(jsonResults.toString())
            when {
                errorJson.has(ERROR_MESSAGE) -> Log.e(TAG, errorJson.getString(ERROR_MESSAGE))
                else -> Log.e(
                    TAG,
                    appContext.getString(R.string.leku_error_cannot_process_json_results),
                    e
                )
            }
            return resultList
        }
    }

    private fun constructData(inputStreamReader: InputStreamReader, jsonResults: StringBuilder) {
        var read: Int
        val buff = CharArray(1024)
        loop@ do {
            read = inputStreamReader.read(buff)
            when {
                read != -1 -> jsonResults.append(buff, 0, read)
                else -> break@loop
            }
        } while (true)
    }

    private fun showDetailsError(resource: Int, listener: OnPlacesDetailsListener, e: Exception) {
        logError(e, resource)
        appContext.getString(resource).let { listener.onError(it) }
    }

    private fun parseDetailsError(
            jsonResults: StringBuilder, listener: OnPlacesDetailsListener, e: Exception
    ) {
        val errorJson = JSONObject(jsonResults.toString())
        if (errorJson.has(ERROR_MESSAGE)) {
            Log.e(TAG, errorJson.getString(ERROR_MESSAGE), e)
            listener.onError(errorJson.getString(ERROR_MESSAGE))
        } else {
            Log.e(TAG, appContext.getString(R.string.leku_error_cannot_process_json_results), e)
            appContext.getString(R.string.leku_error_cannot_process_json_results)
                .let { listener.onError(it) }
        }
    }

    private fun parseDetailsData(jsonResults: StringBuilder, listener: OnPlacesDetailsListener) {
        val jsonObj = JSONObject(jsonResults.toString())
        extendlog(jsonObj.toString())
        val resultJsonObject = jsonObj.getJSONObject(RESULT)
        val addressArray = resultJsonObject.getJSONArray(ADDRESS_COMPONENTS)
        val geometry = resultJsonObject.getJSONObject(GEOMETRY)
        val location = geometry.getJSONObject(LOCATION)
        val lat = location.getDouble(LAT)
        val lng = location.getDouble(LNG)
        val placeId = resultJsonObject.getString(PLACE_ID)
        val url = resultJsonObject.getString(URL)
        val utcOffset = resultJsonObject.getInt(UTC_OFFSET)
        val vicinity = resultJsonObject.getString(VICINITY)
        var compoundPlusCode = ""
        var globalPlusCode = ""
        if (resultJsonObject.has(PLUS_CODE)) {
            val plusCode = resultJsonObject.getJSONObject(PLUS_CODE)
            compoundPlusCode = plusCode.getString(COMPOUND_CODE)
            globalPlusCode = plusCode.getString(GLOBAL_CODE)
        }
        val address = ArrayList<Address>()
        getAddress(addressArray, address)
        listener.onPlaceDetailsFetched(
            PlaceDetails(
                resultJsonObject.getString(NAME), address, lat,
                lng, placeId, url, utcOffset, vicinity, compoundPlusCode, globalPlusCode
            )
        )
    }

    private fun extendlog(veryLongString: String) {
        val maxLogSize = 1000
        for (i in 0..veryLongString.length / maxLogSize) {
            val start = i * maxLogSize
            var end = (i + 1) * maxLogSize
            end = if (end > veryLongString.length) veryLongString.length else end
            //Log.v("Json=>", veryLongString.substring(start, end))
        }
    }

    private fun getAddress(addressArray: JSONArray, address: ArrayList<Address>) {
        (0 until addressArray.length()).forEach { i ->
            val addressObject = addressArray.getJSONObject(i)
            val addressTypeArray = addressObject.getJSONArray(TYPES)
            val addressType = ArrayList<String>()
            parseAddressType(addressTypeArray, addressType, address, addressObject)
        }
    }

    private fun parseAddressType(
            addressTypeArray: JSONArray, addressType: ArrayList<String>,
            address: ArrayList<Address>, addressObject: JSONObject
    ) {
        (0 until addressTypeArray.length()).forEach { j ->
            addressType.add(
                addressTypeArray.getString(j)
            )
        }
        address.add(
            Address(
                addressObject.getString(LONG_NAME),
                addressObject.getString(SHORT_NAME),
                addressType
            )
        )
    }

    companion object {
        private val TAG = PlaceAPI::class.java.simpleName
        private const val PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place"
        private const val TYPE_AUTOCOMPLETE = "/autocomplete"
        private const val TYPE_DETAIL = "/details"
        private const val COUNTRY = "/country:in"
        private const val PARAM_PLACE_ID = "&placeid="
        private const val OUT_JSON = "/json"
        private const val LONG_NAME = "long_name"
        private const val SHORT_NAME = "short_name"
        private const val PLACE_ID = "place_id"
        private const val URL = "url"
        private const val UTC_OFFSET = "utc_offset"
        private const val VICINITY = "vicinity"
        private const val PLUS_CODE = "plus_code"
        private const val COMPOUND_CODE = "compound_code"
        private const val GLOBAL_CODE = "global_code"
        private const val NAME = "name"
        private const val TYPES = "types"
        private const val ADDRESS_COMPONENTS = "address_components"
        private const val GEOMETRY = "geometry"
        private const val LOCATION = "location"
        private const val LAT = "lat"
        private const val LNG = "lng"
        private const val RESULT = "result"
        private const val ERROR_MESSAGE = "error_message"


    }

    /**
     * The data class used as builder to allow the user to use different configs of the places API
     */
    data class Builder(
        private var apiKey: String? = null,
        private var sessionToken: String? = null
    ) {
        /**
         * Sets the api key for the PlaceAPI
         */
        fun apiKey(apiKey: String) = apply { this.apiKey = apiKey }

        /**
         * Sets a unique session token for billing in the PlaceAPI
         */
        fun sessionToken(sessionToken: String) = apply { this.sessionToken = sessionToken }

        /**
         * Builds and creates an object of the PlaceAPI
         */
        fun build(context: Context) = PlaceAPI(apiKey, sessionToken, context)
    }
}
