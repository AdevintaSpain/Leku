package com.adevinta.leku.geocoder

import android.annotation.SuppressLint
import android.location.Address
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.adevinta.leku.geocoder.places.GooglePlacesDataSource
import com.adevinta.leku.geocoder.timezone.GoogleTimeZoneDataSource
import com.adevinta.leku.utils.ReactiveLocationProvider
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.MaybeSource
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import java.util.TimeZone
import kotlin.collections.ArrayList

private const val RETRY_COUNT = 3
private const val MAX_PLACES_RESULTS = 3

class GeocoderPresenter @JvmOverloads constructor(
    private val locationProvider: ReactiveLocationProvider,
    private val geocoderRepository: GeocoderRepository,
    private val googlePlacesDataSource: GooglePlacesDataSource? = null,
    private val googleTimeZoneDataSource: GoogleTimeZoneDataSource? = null,
    private val scheduler: Scheduler = AndroidSchedulers.mainThread()
) {

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
        val disposable = geocoderRepository.getFromLocationName(query)
                .subscribeOn(Schedulers.io())
                .observeOn(scheduler)
                .doFinally { view?.didLoadLocation() }
                .subscribe({ view?.showLocations(it) },
                        { view?.showLoadLocationError() })
        compositeDisposable.add(disposable)
    }

    fun getFromLocationName(query: String, lowerLeft: LatLng, upperRight: LatLng) {
        view?.willLoadLocation()
        val disposable = Single.zip(
                geocoderRepository.getFromLocationName(query, lowerLeft, upperRight),
                getPlacesFromLocationName(query, lowerLeft, upperRight)
        ) { geocoderList, placesList ->
            this.getMergedList(geocoderList, placesList)
        }
            .subscribeOn(Schedulers.io())
            .observeOn(scheduler)
            .retry(RETRY_COUNT.toLong())
            .doFinally { view?.didLoadLocation() }
            .subscribe({ view?.showLocations(it) }, { view?.showLoadLocationError() })
        compositeDisposable.add(disposable)
    }

    fun getDebouncedFromLocationName(query: String, debounceTime: Int) {
        view?.willLoadLocation()
        val disposable = geocoderRepository.getFromLocationName(query)
            .subscribeOn(Schedulers.io())
            .observeOn(scheduler)
            .toObservable()
            .debounce(debounceTime.toLong(), TimeUnit.MILLISECONDS, Schedulers.io())
            .doFinally { view?.didLoadLocation() }
            .subscribe({ view?.showDebouncedLocations(it) },
                { view?.showLoadLocationError() })
        compositeDisposable.add(disposable)
    }

    fun getDebouncedFromLocationName(query: String, lowerLeft: LatLng, upperRight: LatLng, debounceTime: Int) {
        view?.willLoadLocation()
        val disposable = Single.zip(
                geocoderRepository.getFromLocationName(query, lowerLeft, upperRight),
                getPlacesFromLocationName(query, lowerLeft, upperRight)
        ) { geocoderList, placesList -> this.getMergedList(geocoderList, placesList) }
            .subscribeOn(Schedulers.io())
            .observeOn(scheduler)
            .toObservable()
            .debounce(debounceTime.toLong(), TimeUnit.MILLISECONDS, Schedulers.io())
            .subscribe({ view?.showDebouncedLocations(it) },
                    { view?.showLoadLocationError() },
                    { view?.didLoadLocation() })
        compositeDisposable.add(disposable)
    }

    fun getInfoFromLocation(latLng: LatLng) {
        view?.willGetLocationInfo(latLng)
        val disposable = geocoderRepository.getFromLocation(latLng)
                .subscribeOn(Schedulers.io())
                .observeOn(scheduler)
                .retry(RETRY_COUNT.toLong())
                .filter { addresses -> addresses.isNotEmpty() }
                .map { addresses -> addresses[0] }
                .flatMap { address -> returnTimeZone(address) }
                .subscribe({ pair: Pair<Address, TimeZone?> -> view?.showLocationInfo(pair) },
                        { view?.showGetLocationInfoError() },
                        { view?.didGetLocationInfo() })
        compositeDisposable.add(disposable)
    }

    private fun returnTimeZone(address: Address): MaybeSource<out Pair<Address, TimeZone?>> {
        return Maybe.just(
                Pair(address, googleTimeZoneDataSource?.getTimeZone(address.latitude, address.longitude))
        ).onErrorReturn { Pair(address, null) }
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
