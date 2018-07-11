package com.schibstedspain.leku.geocoder

import android.location.Address
import com.google.android.gms.maps.model.LatLng
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers

private const val RETRY_COUNT = 3

class GeocoderRepository(
    private val androidGeocoder: GeocoderInteractorDataSource,
    private val googleGeocoder: GeocoderInteractorDataSource
) {

    fun getFromLocationName(query: String): Observable<List<Address>> {
        return androidGeocoder.getFromLocationName(query)
                .subscribeOn(Schedulers.newThread())
                .retry(RETRY_COUNT.toLong())
                .onErrorResumeNext(googleGeocoder.getFromLocationName(query))
    }

    fun getFromLocationName(query: String, lowerLeft: LatLng, upperRight: LatLng): Observable<List<Address>> {
        return androidGeocoder.getFromLocationName(query, lowerLeft, upperRight)
                .subscribeOn(Schedulers.newThread())
                .retry(RETRY_COUNT.toLong())
                .onErrorResumeNext(googleGeocoder.getFromLocationName(query, lowerLeft, upperRight))
    }

    fun getFromLocation(latLng: LatLng): Observable<List<Address>> {
        return androidGeocoder.getFromLocation(latLng.latitude, latLng.longitude)
                .subscribeOn(Schedulers.newThread())
                .retry(RETRY_COUNT.toLong())
                .onErrorResumeNext(googleGeocoder.getFromLocation(latLng.latitude, latLng.longitude))
    }
}
