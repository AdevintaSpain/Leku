package com.schibstedspain.leku.geocoder

import android.location.Address
import android.location.Geocoder
import com.google.android.gms.maps.model.LatLng
import io.reactivex.Observable
import java.io.IOException

class AndroidGeocoderDataSource(private val geocoder: Geocoder) : GeocoderInteractorDataSource {

    override fun getFromLocationName(query: String): Observable<List<Address>> {
        return Observable.create { emitter ->
            try {
                emitter.onNext(geocoder.getFromLocationName(query, MAX_RESULTS))
                emitter.onComplete()
            } catch (e: IOException) {
                emitter.tryOnError(e)
            }
        }
    }

    override fun getFromLocationName(query: String, lowerLeft: LatLng, upperRight: LatLng): Observable<List<Address>> {
        return Observable.create { emitter ->
            try {
                emitter.onNext(geocoder.getFromLocationName(query, MAX_RESULTS, lowerLeft.latitude,
                        lowerLeft.longitude, upperRight.latitude, upperRight.longitude))
                emitter.onComplete()
            } catch (e: IOException) {
                emitter.tryOnError(e)
            }
        }
    }

    override fun getFromLocation(latitude: Double, longitude: Double): Observable<List<Address>> {
        return Observable.create { emitter ->
            try {
                emitter.onNext(geocoder.getFromLocation(latitude, longitude, MAX_RESULTS))
                emitter.onComplete()
            } catch (e: IOException) {
                emitter.tryOnError(e)
            }
        }
    }

    companion object {
        private const val MAX_RESULTS = 5
    }
}
