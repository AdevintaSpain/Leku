package com.schibstedspain.leku.geocoder.places

import android.annotation.SuppressLint
import android.location.Address
import com.google.android.gms.common.data.DataBufferUtils
import com.google.android.gms.location.places.AutocompletePrediction
import com.google.android.gms.location.places.GeoDataClient
import com.google.android.gms.location.places.Place
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.tasks.RuntimeExecutionException
import com.google.android.gms.tasks.Tasks
import io.reactivex.Observable
import io.reactivex.Observable.defer
import java.util.ArrayList
import java.util.Locale
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

private const val PREDICTIONS_WAITING_TIME: Long = 6
private const val PLACE_BY_ID_WAITING_TIME: Long = 3

class GooglePlacesDataSource(private val geoDataClient: GeoDataClient) {

    fun getFromLocationName(query: String, latLngBounds: LatLngBounds): Observable<List<Address>> {
        return defer {
            val results = geoDataClient.getAutocompletePredictions(query, latLngBounds, null)
            try {
                Tasks.await(results, PREDICTIONS_WAITING_TIME, TimeUnit.SECONDS)
            } catch (ignored: ExecutionException) {
            } catch (ignored: InterruptedException) {
            } catch (ignored: TimeoutException) {
            }

            try {
                val autocompletePredictions = results.result
                val predictionList = DataBufferUtils.freezeAndClose(autocompletePredictions)
                val addressList = getAddressListFromPrediction(predictionList)
                return@defer Observable.just(addressList)
            } catch (e: RuntimeExecutionException) {
                return@defer Observable.just(ArrayList<Address>())
            }
        }
    }

    private fun getAddressListFromPrediction(predictionList: List<AutocompletePrediction>): List<Address> {
        val addressList = ArrayList<Address>()
        for (prediction in predictionList) {
            val placeBufferResponseTask = geoDataClient.getPlaceById(prediction.placeId!!)
            try {
                Tasks.await(placeBufferResponseTask, PLACE_BY_ID_WAITING_TIME, TimeUnit.SECONDS)
            } catch (ignored: ExecutionException) {
            } catch (ignored: InterruptedException) {
            } catch (ignored: TimeoutException) {
            }

            val placeBufferResponse = placeBufferResponseTask.result
            @SuppressLint("RestrictedApi") val place = placeBufferResponse.get(0)
            addressList.add(mapPlaceToAddress(place))
        }
        return addressList
    }

    private fun mapPlaceToAddress(place: Place): Address {
        val address = Address(Locale.getDefault())
        address.latitude = place.latLng.latitude
        address.longitude = place.latLng.longitude
        val addressName = place.name.toString() + " - " + place.address!!.toString()
        address.setAddressLine(0, addressName)
        address.featureName = addressName
        return address
    }
}
