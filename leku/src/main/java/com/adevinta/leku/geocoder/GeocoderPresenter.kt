package com.adevinta.leku.geocoder

import android.annotation.SuppressLint
import android.location.Address
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.adevinta.leku.geocoder.places.GooglePlacesDataSource
import com.adevinta.leku.geocoder.timezone.GoogleTimeZoneDataSource
import com.adevinta.leku.utils.ReactiveLocationProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.TimeZone
import kotlin.collections.ArrayList

class GeocoderPresenter @JvmOverloads constructor(
    private val locationProvider: ReactiveLocationProvider,
    private val geocoderRepository: GeocoderRepository,
    private val googlePlacesDataSource: GooglePlacesDataSource? = null,
    private val googleTimeZoneDataSource: GoogleTimeZoneDataSource? = null
) {

    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private var view: GeocoderViewInterface? = null
    private val nullView = GeocoderViewInterface.NullView()
    private var isGooglePlacesEnabled = false

    init {
        this.view = nullView
    }

    fun setUI(geocoderViewInterface: GeocoderViewInterface) {
        this.view = geocoderViewInterface
    }

    fun stop() {
        this.view = nullView
        coroutineScope.cancel()
    }

    @SuppressLint("MissingPermission")
    fun getLastKnownLocation() {
        coroutineScope.launch(Dispatchers.IO) {
            val location = locationProvider.getLastKnownLocation()
            withContext(Dispatchers.Main) {
                location?.let {
                    view?.showLastLocation(it)
                }
                view?.didGetLastLocation()
            }
        }
    }

    fun getSuggestionsFromLocationName(query: String) {
        view?.willLoadLocation()
        coroutineScope.launch(Dispatchers.IO) {
            val suggestions = geocoderRepository.autoCompleteFromLocationName(query)
            withContext(Dispatchers.Main) {
                view?.showSuggestions(suggestions)
                view?.didLoadLocation()
            }
        }
    }

    fun getFromLocationName(query: String) {
        view?.willLoadLocation()
        coroutineScope.launch(Dispatchers.IO) {
            val location = geocoderRepository.getFromLocationName(query)
            withContext(Dispatchers.Main) {
                view?.showLocations(location)
                view?.didLoadLocation()
            }
        }
    }

    fun getFromLocationName(query: String, lowerLeft: LatLng, upperRight: LatLng) {
        view?.willLoadLocation()
        coroutineScope.launch(Dispatchers.IO) {
            val address = geocoderRepository.getFromLocationName(query, lowerLeft, upperRight)
            val places = getPlacesFromLocationName(query, lowerLeft, upperRight)
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

    fun getDebouncedFromLocationName(
        query: String,
        lowerLeft: LatLng,
        upperRight: LatLng,
        debounceTime: Int
    ) {
        view?.willLoadLocation()
        coroutineScope.launch(Dispatchers.IO) {
            val address = geocoderRepository.getFromLocationName(query, lowerLeft, upperRight)
            val places = getPlacesFromLocationName(query, lowerLeft, upperRight)
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
            if (addresses.isEmpty()) return@launch
            val timeZone = returnTimeZone(addresses.first())
            withContext(Dispatchers.Main) {
                view?.showLocationInfo(timeZone)
                view?.didGetLocationInfo()
            }
        }
    }

    fun getAddressFromPlaceId(placeId: String) {
        coroutineScope.launch(Dispatchers.IO) {
            val address = geocoderRepository.getAddressFromPlaceId(placeId)
            val timeZone = returnTimeZone(address)
            withContext(Dispatchers.Main) {
                if (address == null) return@withContext
                view?.showLocationInfo(timeZone)
                view?.setAddressFromSuggestion(address)
                view?.didGetLocationInfo()
            }
        }
    }

    private fun returnTimeZone(address: Address?): Pair<Address?, TimeZone?> {
        address?.let {
            return Pair(it, googleTimeZoneDataSource?.getTimeZone(it.latitude, it.longitude))
        }
        return Pair(null, null)
    }

    fun enableGooglePlaces() {
        this.isGooglePlacesEnabled = true
    }

    private fun getPlacesFromLocationName(
        query: String,
        lowerLeft: LatLng,
        upperRight: LatLng
    ): List<Address> {
        return if (isGooglePlacesEnabled && googlePlacesDataSource != null) {
            googlePlacesDataSource.getFromLocationName(query, LatLngBounds(lowerLeft, upperRight))
        } else {
            ArrayList()
        }
    }

    private fun getMergedList(
        geocoderList: List<Address>,
        placesList: List<Address>
    ): List<Address> {
        val mergedList = ArrayList<Address>()
        mergedList.addAll(geocoderList)
        mergedList.addAll(placesList)
        return mergedList
    }
}
