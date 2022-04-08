package com.adevinta.leku.geocoder

import android.annotation.SuppressLint
import android.location.Address
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.adevinta.leku.geocoder.places.GooglePlacesDataSource
import com.adevinta.leku.geocoder.timezone.GoogleTimeZoneDataSource
import com.adevinta.leku.utils.ReactiveLocationProvider
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.TimeZone
import kotlin.collections.ArrayList

private const val RETRY_COUNT = 3
private const val MAX_PLACES_RESULTS = 3

class GeocoderPresenter @JvmOverloads constructor(
    private val locationProvider: ReactiveLocationProvider,
    private val geocoderRepository: GeocoderRepository,
    private val googlePlacesDataSource: GooglePlacesDataSource? = null,
    private val googleTimeZoneDataSource: GoogleTimeZoneDataSource? = null
) {

    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private var view: GeocoderViewInterface? = null
    private val nullView = GeocoderViewInterface.NullView()
    private val compositeDisposable = CompositeDisposable()
    private var isGooglePlacesEnabled = false

    init {
        this.view = nullView
    }

    fun setUI(geocoderViewInterface: GeocoderViewInterface) {
        this.view = geocoderViewInterface
    }

    fun stop() {
        this.view = nullView
        compositeDisposable.clear()
    }

    fun getLastKnownLocation() {
        @SuppressLint("MissingPermission")
        val disposable = locationProvider.getLastKnownLocation()
                .retry(RETRY_COUNT.toLong())
                .subscribe({ view?.showLastLocation(it) },
                        { view?.didGetLastLocation() })
        compositeDisposable.add(disposable)
    }

    fun getFromLocationName(query: String) {
        view?.willLoadLocation()
        coroutineScope.launch(Dispatchers.IO) {
            val location = geocoderRepository.getFromLocationName(query)
            withContext(Dispatchers.Main) {
                view?.didLoadLocation()
                view?.showLocations(location)
            }
        }
    }

    fun getFromLocationName(query: String, lowerLeft: LatLng, upperRight: LatLng) {
        view?.willLoadLocation()
        coroutineScope.launch(Dispatchers.IO) {
            val address = geocoderRepository.getFromLocationName(query, lowerLeft, upperRight)
            val places = getPlacesFromLocationName(query, lowerLeft, upperRight).blockingGet()
            val mergedList = getMergedList(address, places)
            withContext(Dispatchers.Main) {
                view?.showLocations(mergedList)
                view?.didLoadLocation()
            }
        }
    }

    fun getDebouncedFromLocationName(query: String, debounceTime: Int) {
        view?.willLoadLocation()
        coroutineScope.launch(Dispatchers.IO) {
            val address = geocoderRepository.getFromLocationName(query)
            withContext(Dispatchers.Main) {
                view?.showDebouncedLocations(address)
                view?.didLoadLocation()
            }
        }
    }

    fun getDebouncedFromLocationName(query: String, lowerLeft: LatLng, upperRight: LatLng, debounceTime: Int) {
        view?.willLoadLocation()
        coroutineScope.launch(Dispatchers.IO) {
            val address = geocoderRepository.getFromLocationName(query, lowerLeft, upperRight)
            val places = getPlacesFromLocationName(query, lowerLeft, upperRight).blockingGet()
            val merged = getMergedList(address, places)
            withContext(Dispatchers.Main) {
                view?.showDebouncedLocations(merged)
                view?.didLoadLocation()
            }
        }
    }

    fun getInfoFromLocation(latLng: LatLng) {
        view?.willGetLocationInfo(latLng)
        coroutineScope.launch(Dispatchers.IO) {
            val addresses = geocoderRepository.getFromLocation(latLng)
            val timeZone = returnTimeZone(addresses.first()).blockingGet()
            withContext(Dispatchers.Main) {
                view?.showLocationInfo(timeZone)
                view?.didGetLocationInfo()
            }
        }
    }

    private fun returnTimeZone(address: Address?): Single<Pair<Address?, TimeZone?>> {
        address?.let {
            return Single.fromCallable {
                Pair(it, googleTimeZoneDataSource?.getTimeZone(it.latitude, it.longitude))
            }
        }
        return Single.just(Pair(null, null))
    }

    fun enableGooglePlaces() {
        this.isGooglePlacesEnabled = true
    }

    private fun getPlacesFromLocationName(
        query: String,
        lowerLeft: LatLng,
        upperRight: LatLng
    ): Single<List<Address>> {
        return if (isGooglePlacesEnabled)
            googlePlacesDataSource!!.getFromLocationName(query, LatLngBounds(lowerLeft, upperRight))
                    .flattenAsObservable { it }
                    .take(MAX_PLACES_RESULTS.toLong()).toList()
                    .onErrorReturnItem(ArrayList())
        else
            Single.just(ArrayList())
    }

    private fun getMergedList(geocoderList: List<Address>, placesList: List<Address>): List<Address> {
        val mergedList = ArrayList<Address>()
        mergedList.addAll(geocoderList)
        mergedList.addAll(placesList)
        return mergedList
    }
}
