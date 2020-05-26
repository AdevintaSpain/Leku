package com.schibstedspain.leku.geocoder

import android.annotation.SuppressLint
import android.location.Address
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.schibstedspain.leku.geocoder.places.GooglePlacesDataSource
import com.schibstedspain.leku.geocoder.timezone.GoogleTimeZoneDataSource
import com.schibstedspain.leku.utils.ReactiveLocationProvider
import hu.akarnokd.rxjava3.bridge.RxJavaBridge
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableSource
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.functions.BiFunction
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
        val disposable = RxJavaBridge.toV3Single(locationProvider.getLastKnownLocation())
                .retry(RETRY_COUNT.toLong())
                .subscribe({ view?.showLastLocation(it) },
                        { view?.didGetLastLocation() })
        compositeDisposable.add(disposable)
    }

    fun getFromLocationName(query: String) {
        view?.willLoadLocation()
        val disposable = geocoderRepository.getFromLocationName(query)
                .observeOn(scheduler)
                .subscribe({ view?.showLocations(it) },
                        { view?.showLoadLocationError() },
                        { view?.didLoadLocation() })
        compositeDisposable.add(disposable)
    }

    fun getFromLocationName(query: String, lowerLeft: LatLng, upperRight: LatLng) {
        view?.willLoadLocation()
        val disposable = Observable.zip<List<Address>, List<Address>, List<Address>>(
                geocoderRepository.getFromLocationName(query, lowerLeft, upperRight),
                getPlacesFromLocationName(query, lowerLeft, upperRight),
                BiFunction<List<Address>, List<Address>, List<Address>> {
                    geocoderList, placesList -> this.getMergedList(geocoderList, placesList)
                })
                .subscribeOn(Schedulers.io())
                .observeOn(scheduler)
                .retry(RETRY_COUNT.toLong())
                .subscribe({ view?.showLocations(it) },
                        { view?.showLoadLocationError() },
                        { view?.didLoadLocation() })
        compositeDisposable.add(disposable)
    }

    fun getDebouncedFromLocationName(query: String, debounceTime: Int) {
        view?.willLoadLocation()
        val disposable = geocoderRepository.getFromLocationName(query)
                .debounce(debounceTime.toLong(), TimeUnit.MILLISECONDS, Schedulers.io())
                .observeOn(scheduler)
                .subscribe({ view?.showDebouncedLocations(it) },
                        { view?.showLoadLocationError() },
                        { view?.didLoadLocation() })
        compositeDisposable.add(disposable)
    }

    fun getDebouncedFromLocationName(query: String, lowerLeft: LatLng, upperRight: LatLng, debounceTime: Int) {
        view?.willLoadLocation()
        val disposable = Observable.zip<List<Address>, List<Address>, List<Address>>(
                geocoderRepository.getFromLocationName(query, lowerLeft, upperRight),
                getPlacesFromLocationName(query, lowerLeft, upperRight),
                BiFunction<List<Address>, List<Address>, List<Address>> {
                    geocoderList, placesList -> this.getMergedList(geocoderList, placesList) })
                .subscribeOn(Schedulers.io())
                .debounce(debounceTime.toLong(), TimeUnit.MILLISECONDS, Schedulers.io())
                .observeOn(scheduler)
                .subscribe({ view?.showDebouncedLocations(it) },
                        { view?.showLoadLocationError() },
                        { view?.didLoadLocation() })
        compositeDisposable.add(disposable)
    }

    fun getInfoFromLocation(latLng: LatLng) {
        view?.willGetLocationInfo(latLng)
        val disposable = geocoderRepository.getFromLocation(latLng)
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

    private fun returnTimeZone(address: Address): ObservableSource<out Pair<Address, TimeZone?>>? {
        return Observable.just(
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
    ): Observable<List<Address>> {
        return if (isGooglePlacesEnabled)
            googlePlacesDataSource!!.getFromLocationName(query, LatLngBounds(lowerLeft, upperRight))
                    .flatMapIterable { addresses -> addresses }
                    .take(MAX_PLACES_RESULTS.toLong()).toList().toObservable()
                    .onErrorReturnItem(ArrayList())
        else
            Observable.just(ArrayList())
    }

    private fun getMergedList(geocoderList: List<Address>, placesList: List<Address>): List<Address> {
        val mergedList = ArrayList<Address>()
        mergedList.addAll(geocoderList)
        mergedList.addAll(placesList)
        return mergedList
    }
}
