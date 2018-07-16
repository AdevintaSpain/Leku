package com.schibstedspain.leku

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.speech.RecognizerIntent
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.places.Places
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.schibstedspain.leku.geocoder.GoogleGeocoderDataSource
import com.schibstedspain.leku.geocoder.AndroidGeocoderDataSource
import com.schibstedspain.leku.geocoder.GeocoderPresenter
import com.schibstedspain.leku.geocoder.GeocoderRepository
import com.schibstedspain.leku.geocoder.GeocoderViewInterface
import com.schibstedspain.leku.geocoder.api.AddressBuilder
import com.schibstedspain.leku.geocoder.api.NetworkClient
import com.schibstedspain.leku.geocoder.places.GooglePlacesDataSource
import com.schibstedspain.leku.permissions.PermissionUtils
import com.schibstedspain.leku.tracker.TrackEvents
import pl.charmas.android.reactivelocation2.ReactiveLocationProvider

import com.google.android.gms.maps.GoogleMap.MAP_TYPE_NORMAL
import com.google.android.gms.maps.GoogleMap.MAP_TYPE_SATELLITE
import com.google.maps.GeoApiContext
import com.schibstedspain.leku.geocoder.timezone.GoogleTimeZoneDataSource
import java.util.ArrayList
import java.util.HashMap
import java.util.Locale
import java.util.TimeZone

const val LATITUDE = "latitude"
const val LONGITUDE = "longitude"
const val ZIPCODE = "zipcode"
const val ADDRESS = "address"
const val LOCATION_ADDRESS = "location_address"
const val TRANSITION_BUNDLE = "transition_bundle"
const val LAYOUTS_TO_HIDE = "layouts_to_hide"
const val SEARCH_ZONE = "search_zone"
const val BACK_PRESSED_RETURN_OK = "back_pressed_return_ok"
const val ENABLE_SATELLITE_VIEW = "enable_satellite_view"
const val ENABLE_LOCATION_PERMISSION_REQUEST = "enable_location_permission_request"
const val ENABLE_GOOGLE_PLACES = "enable_google_places"
const val ENABLE_GOOGLE_TIME_ZONE = "enable_google_time_zone"
const val POIS_LIST = "pois_list"
const val LEKU_POI = "leku_poi"
const val ENABLE_VOICE_SEARCH = "enable_voice_search"
const val TIME_ZONE_ID = "time_zone_id"
const val TIME_ZONE_DISPLAY_NAME = "time_zone_display_name"
private const val GEOLOC_API_KEY = "geoloc_api_key"
private const val LOCATION_KEY = "location_key"
private const val LAST_LOCATION_QUERY = "last_location_query"
private const val OPTIONS_HIDE_STREET = "street"
private const val OPTIONS_HIDE_CITY = "city"
private const val OPTIONS_HIDE_ZIPCODE = "zipcode"
private const val REQUEST_PLACE_PICKER = 6655
private const val CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000
private const val DEFAULT_ZOOM = 16
private const val WIDER_ZOOM = 6
private const val MIN_CHARACTERS = 2
private const val DEBOUNCE_TIME = 400

class LocationPickerActivity : AppCompatActivity(),
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        GoogleMap.OnMapLongClickListener,
        GeocoderViewInterface,
        GoogleMap.OnMapClickListener {

    private var map: GoogleMap? = null
    private var googleApiClient: GoogleApiClient? = null
    private var currentLocation: Location? = null
    private var currentLekuPoi: LekuPoi? = null
    private var geocoderPresenter: GeocoderPresenter? = null

    private var adapter: ArrayAdapter<String>? = null
    private var searchView: EditText? = null
    private var street: TextView? = null
    private var coordinates: TextView? = null
    private var longitude: TextView? = null
    private var latitude: TextView? = null
    private var city: TextView? = null
    private var zipCode: TextView? = null
    private var locationInfoLayout: FrameLayout? = null
    private var progressBar: ProgressBar? = null
    private var listResult: ListView? = null
    private var clearSearchButton: ImageView? = null
    private var searchOption: MenuItem? = null

    private val locationList = ArrayList<Address>()
    private var locationNameList: MutableList<String> = ArrayList()
    private var hasWiderZoom = false
    private val bundle = Bundle()
    private var selectedAddress: Address? = null
    private var isLocationInformedFromBundle = false
    private var isStreetVisible = true
    private var isCityVisible = true
    private var isZipCodeVisible = true
    private var shouldReturnOkOnBackPressed = false
    private var enableSatelliteView = true
    private var enableLocationPermissionRequest = true
    private var isGooglePlacesEnabled = false
    private var isGoogleTimeZoneEnabled = false
    private var searchZone: String? = null
    private var poisList: List<LekuPoi>? = null
    private var lekuPoisMarkersMap: MutableMap<String, LekuPoi>? = null
    private var currentMarker: Marker? = null
    private var textWatcher: TextWatcher? = null
    private var apiInteractor: GoogleGeocoderDataSource? = null
    private var isVoiceSearchEnabled = true
    private lateinit var toolbar: Toolbar
    private lateinit var timeZone: TimeZone

    private val searchTextWatcher: TextWatcher
        get() = object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            }

            override fun onTextChanged(charSequence: CharSequence, start: Int, count: Int, after: Int) {
                if ("" == charSequence.toString()) {
                    adapter!!.clear()
                    adapter!!.notifyDataSetChanged()
                    showLocationInfoLayout()
                    clearSearchButton?.visibility = View.INVISIBLE
                    searchOption?.setIcon(R.drawable.leku_ic_mic)
                    updateVoiceSearchVisibility()
                } else {
                    if (charSequence.length > MIN_CHARACTERS) {
                        retrieveLocationWithDebounceTimeFrom(charSequence.toString())
                    }
                    clearSearchButton?.visibility = View.VISIBLE
                    searchOption?.setIcon(R.drawable.leku_ic_search)
                    searchOption?.isVisible = true
                }
            }

            override fun afterTextChanged(editable: Editable) {
            }
        }

    private val defaultZoom: Int
        get() {
            return if (hasWiderZoom) {
                WIDER_ZOOM
            } else {
                DEFAULT_ZOOM
            }
        }

    private val locationAddress: String
        get() {
            var locationAddress = ""
            if (street != null && !street!!.text.toString().isEmpty()) {
                locationAddress = street!!.text.toString()
            }
            if (city != null && !city!!.text.toString().isEmpty()) {
                if (!locationAddress.isEmpty()) {
                    locationAddress += ", "
                }
                locationAddress += city!!.text.toString()
            }
            return locationAddress
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.leku_activity_location_picker)
        setUpMainVariables()
        setUpResultsList()
        setUpToolBar()
        updateValuesFromBundle(savedInstanceState)
        checkLocationPermission()
        setUpSearchView()
        setUpMapIfNeeded()
        setUpFloatingButtons()
        buildGoogleApiClient()
        track(TrackEvents.ON_LOAD_LOCATION_PICKER)
    }

    private fun checkLocationPermission() {
        if (enableLocationPermissionRequest &&
                PermissionUtils.shouldRequestLocationStoragePermission(applicationContext)) {
            PermissionUtils.requestLocationPermission(this)
        }
    }

    private fun track(event: TrackEvents) {
        LocationPicker.getTracker().onEventTracked(event)
    }

    private fun setUpMainVariables() {
        val placesDataSource = GooglePlacesDataSource(Places.getGeoDataClient(this, null))
        val geocoder = Geocoder(this, Locale.getDefault())
        apiInteractor = GoogleGeocoderDataSource(NetworkClient(), AddressBuilder())
        val geocoderRepository = GeocoderRepository(AndroidGeocoderDataSource(geocoder), apiInteractor!!)
        val timeZoneDataSource = GoogleTimeZoneDataSource(
                GeoApiContext.Builder().apiKey(GoogleTimeZoneDataSource.getApiKey(this)).build())
        geocoderPresenter = GeocoderPresenter(
                ReactiveLocationProvider(applicationContext), geocoderRepository, placesDataSource, timeZoneDataSource)
        geocoderPresenter!!.setUI(this)
        progressBar = findViewById(R.id.loading_progress_bar)
        progressBar!!.visibility = View.GONE
        locationInfoLayout = findViewById(R.id.location_info)
        longitude = findViewById(R.id.longitude)
        latitude = findViewById(R.id.latitude)
        street = findViewById(R.id.street)
        coordinates = findViewById(R.id.coordinates)
        city = findViewById(R.id.city)
        zipCode = findViewById(R.id.zipCode)
        clearSearchButton = findViewById(R.id.leku_clear_search_image)
        clearSearchButton!!.setOnClickListener { _ ->
            if (searchView != null) {
                searchView!!.setText("")
            }
        }
        locationNameList = ArrayList()
    }

    private fun setUpResultsList() {
        listResult = findViewById(R.id.resultlist)
        adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, locationNameList)
        listResult!!.adapter = adapter
        listResult!!.setOnItemClickListener { _, _, i, _ ->
            setNewLocation(locationList[i])
            changeListResultVisibility(View.GONE)
            closeKeyboard()
        }
    }

    private fun setUpToolBar() {
        toolbar = findViewById(R.id.map_search_toolbar)
        setSupportActionBar(toolbar)
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setDisplayShowTitleEnabled(false)
        }
    }

    private fun switchToolbarVisibility() {
        if (isPlayServicesAvailable()) {
            toolbar.visibility = View.VISIBLE
        } else {
            toolbar.visibility = View.GONE
        }
    }

    private fun setUpSearchView() {
        searchView = findViewById(R.id.leku_search)
        searchView!!.setOnEditorActionListener { v, actionId, _ ->
            var handled = false
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                retrieveLocationFrom(v.text.toString())
                closeKeyboard()
                handled = true
            }
            handled
        }
        textWatcher = searchTextWatcher
        searchView!!.addTextChangedListener(textWatcher)
    }

    private fun setUpFloatingButtons() {
        val btnMyLocation = findViewById<FloatingActionButton>(R.id.btnFloatingAction)
        btnMyLocation.setOnClickListener { _ ->
            checkLocationPermission()
            geocoderPresenter!!.getLastKnownLocation()
            track(TrackEvents.ON_LOCALIZED_ME)
        }
        val btnAcceptLocation = findViewById<FloatingActionButton>(R.id.btnAccept)
        btnAcceptLocation.setOnClickListener { _ -> returnCurrentPosition() }

        val btnSatellite = findViewById<FloatingActionButton>(R.id.btnSatellite)
        btnSatellite!!.setOnClickListener { _ ->
            if (map != null) {
                map!!.mapType = if (map!!.mapType == MAP_TYPE_SATELLITE) MAP_TYPE_NORMAL else MAP_TYPE_SATELLITE
                btnSatellite.setImageResource(
                        if (map!!.mapType == MAP_TYPE_SATELLITE)
                            R.drawable.leku_ic_satellite_off
                        else
                            R.drawable.leku_ic_satellite_on)
            }
        }
        btnSatellite.visibility = if (enableSatelliteView) View.VISIBLE else View.GONE
    }

    private fun updateValuesFromBundle(savedInstanceState: Bundle?) {
        val transitionBundle = intent.extras
        if (transitionBundle != null) {
            getTransitionBundleParams(transitionBundle)
        }
        if (savedInstanceState != null) {
            getSavedInstanceParams(savedInstanceState)
        }
        updateAddressLayoutVisibility()
        updateVoiceSearchVisibility()

        if (isGooglePlacesEnabled) {
            geocoderPresenter!!.enableGooglePlaces()
        }
    }

    private fun setUpMapIfNeeded() {
        if (map == null) {
            (supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment).getMapAsync(this)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.leku_toolbar_menu, menu)
        searchOption = menu.findItem(R.id.action_voice)
        updateVoiceSearchVisibility()
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            onBackPressed()
            return true
        } else if (id == R.id.action_voice) {
            if (searchView!!.text.toString().isEmpty()) {
                startVoiceRecognitionActivity()
            } else {
                retrieveLocationFrom(searchView!!.text.toString())
                closeKeyboard()
            }
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (PermissionUtils.isLocationPermissionGranted(applicationContext)) {
            geocoderPresenter!!.getLastKnownLocation()
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_PLACE_PICKER -> if (resultCode == Activity.RESULT_OK && data != null) {
                val matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                searchView = findViewById(R.id.leku_search)
                retrieveLocationFrom(matches[0])
            }
            else -> {
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onStart() {
        super.onStart()
        googleApiClient!!.connect()
        geocoderPresenter!!.setUI(this)
    }

    override fun onStop() {
        if (googleApiClient!!.isConnected) {
            googleApiClient!!.disconnect()
        }
        geocoderPresenter!!.stop()
        super.onStop()
    }

    override fun onResume() {
        super.onResume()
        setUpMapIfNeeded()
        switchToolbarVisibility()
    }

    override fun onDestroy() {
        if (searchView != null && textWatcher != null) {
            searchView!!.removeTextChangedListener(textWatcher)
        }
        if (googleApiClient != null) {
            googleApiClient!!.unregisterConnectionCallbacks(this)
        }
        super.onDestroy()
    }

    override fun onBackPressed() {
        if (!shouldReturnOkOnBackPressed || isLocationInformedFromBundle) {
            setResult(Activity.RESULT_CANCELED)
            track(TrackEvents.CANCEL)
            finish()
        } else {
            returnCurrentPosition()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        if (map == null) {
            map = googleMap
            setDefaultMapSettings()
            setCurrentPositionLocation()
            setPois()
        }
    }

    override fun onConnected(savedBundle: Bundle?) {
        if (currentLocation == null) {
            geocoderPresenter!!.getLastKnownLocation()
        }
    }

    override fun onConnectionSuspended(i: Int) {
        googleApiClient!!.connect()
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST)
            } catch (e: IntentSender.SendIntentException) {
                track(TrackEvents.GOOGLE_API_CONNECTION_FAILED)
            }
        }
    }

    override fun onLocationChanged(location: Location) {
        currentLocation = location
    }

    public override fun onSaveInstanceState(savedInstanceState: Bundle) {
        if (currentLocation != null) {
            savedInstanceState.putParcelable(LOCATION_KEY, currentLocation)
        }
        if (searchView != null) {
            savedInstanceState.putString(LAST_LOCATION_QUERY, searchView!!.text.toString())
        }
        if (bundle.containsKey(TRANSITION_BUNDLE)) {
            savedInstanceState.putBundle(TRANSITION_BUNDLE, bundle.getBundle(TRANSITION_BUNDLE))
        }
        if (poisList != null) {
            savedInstanceState.putParcelableArrayList(POIS_LIST, ArrayList(poisList!!))
        }
        savedInstanceState.putBoolean(ENABLE_SATELLITE_VIEW, enableSatelliteView)
        savedInstanceState.putBoolean(ENABLE_LOCATION_PERMISSION_REQUEST, enableLocationPermissionRequest)
        super.onSaveInstanceState(savedInstanceState)
    }

    public override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val lastQuery = savedInstanceState.getString(LAST_LOCATION_QUERY, "")
        if ("" != lastQuery) {
            retrieveLocationFrom(lastQuery)
        }
        currentLocation = savedInstanceState.getParcelable(LOCATION_KEY)
        if (currentLocation != null) {
            setCurrentPositionLocation()
        }
        if (savedInstanceState.containsKey(TRANSITION_BUNDLE)) {
            bundle.putBundle(TRANSITION_BUNDLE, savedInstanceState.getBundle(TRANSITION_BUNDLE))
        }
        if (savedInstanceState.containsKey(POIS_LIST)) {
            poisList = savedInstanceState.getParcelableArrayList(POIS_LIST)
        }
        if (savedInstanceState.containsKey(ENABLE_SATELLITE_VIEW)) {
            enableSatelliteView = savedInstanceState.getBoolean(ENABLE_SATELLITE_VIEW)
        }
        if (savedInstanceState.containsKey(ENABLE_LOCATION_PERMISSION_REQUEST)) {
            enableLocationPermissionRequest = savedInstanceState.getBoolean(ENABLE_LOCATION_PERMISSION_REQUEST)
        }
    }

    override fun onMapLongClick(latLng: LatLng) {
        currentLekuPoi = null
        setNewPosition(latLng)
        track(TrackEvents.ON_LOCALIZED_BY_POI)
    }

    override fun onMapClick(latLng: LatLng) {
        currentLekuPoi = null
        setNewPosition(latLng)
        track(TrackEvents.SIMPLE_ON_LOCALIZE_BY_POI)
    }

    private fun setNewPosition(latLng: LatLng) {
        if (currentLocation == null) {
            currentLocation = Location(getString(R.string.leku_network_resource))
        }
        currentLocation!!.latitude = latLng.latitude
        currentLocation!!.longitude = latLng.longitude
        setCurrentPositionLocation()
    }

    override fun willLoadLocation() {
        progressBar!!.visibility = View.VISIBLE
        changeListResultVisibility(View.GONE)
    }

    override fun showLocations(addresses: List<Address>) {
        fillLocationList(addresses)
        if (addresses.isEmpty()) {
            Toast.makeText(applicationContext, R.string.leku_no_search_results, Toast.LENGTH_LONG)
                    .show()
        } else {
            updateLocationNameList(addresses)
            if (hasWiderZoom) {
                searchView!!.setText("")
            }
            if (addresses.size == 1) {
                setNewLocation(addresses[0])
            }
            adapter!!.notifyDataSetChanged()
        }
    }

    override fun showDebouncedLocations(addresses: List<Address>) {
        fillLocationList(addresses)
        if (!addresses.isEmpty()) {
            updateLocationNameList(addresses)
            adapter!!.notifyDataSetChanged()
        }
    }

    override fun didLoadLocation() {
        progressBar!!.visibility = View.GONE

        changeListResultVisibility(if (locationList.size >= 1) View.VISIBLE else View.GONE)

        if (locationList.size == 1) {
            changeLocationInfoLayoutVisibility(View.VISIBLE)
        } else {
            changeLocationInfoLayoutVisibility(View.GONE)
        }
        track(TrackEvents.ON_SEARCH_LOCATIONS)
    }

    private fun changeListResultVisibility(visibility: Int) {
        listResult!!.visibility = visibility
    }

    private fun changeLocationInfoLayoutVisibility(visibility: Int) {
        locationInfoLayout!!.visibility = visibility
    }

    private fun showCoordinatesLayout() {
        longitude!!.visibility = View.VISIBLE
        latitude!!.visibility = View.VISIBLE
        coordinates!!.visibility = View.VISIBLE
        street!!.visibility = View.GONE
        city!!.visibility = View.GONE
        zipCode!!.visibility = View.GONE
        changeLocationInfoLayoutVisibility(View.VISIBLE)
    }

    private fun showAddressLayout() {
        longitude!!.visibility = View.GONE
        latitude!!.visibility = View.GONE
        coordinates!!.visibility = View.GONE
        if (isStreetVisible) {
            street!!.visibility = View.VISIBLE
        }
        if (isCityVisible) {
            city!!.visibility = View.VISIBLE
        }
        if (isZipCodeVisible) {
            zipCode!!.visibility = View.VISIBLE
        }
        changeLocationInfoLayoutVisibility(View.VISIBLE)
    }

    private fun updateAddressLayoutVisibility() {
        street!!.visibility = if (isStreetVisible) View.VISIBLE else View.INVISIBLE
        city!!.visibility = if (isCityVisible) View.VISIBLE else View.INVISIBLE
        zipCode!!.visibility = if (isZipCodeVisible) View.VISIBLE else View.INVISIBLE
        longitude!!.visibility = View.VISIBLE
        latitude!!.visibility = View.VISIBLE
        coordinates!!.visibility = View.VISIBLE
    }

    private fun updateVoiceSearchVisibility() {
        searchOption?.isVisible = isVoiceSearchEnabled
    }

    override fun showLoadLocationError() {
        progressBar!!.visibility = View.GONE
        changeListResultVisibility(View.GONE)
        Toast.makeText(this, R.string.leku_load_location_error, Toast.LENGTH_LONG).show()
    }

    override fun willGetLocationInfo(latLng: LatLng) {
        changeLocationInfoLayoutVisibility(View.VISIBLE)
        setCoordinatesInfo(latLng)
    }

    override fun showLastLocation(location: Location) {
        currentLocation = location
    }

    override fun didGetLastLocation() {
        if (currentLocation != null) {
            if (!Geocoder.isPresent()) {
                Toast.makeText(this, R.string.leku_no_geocoder_available, Toast.LENGTH_LONG).show()
                return
            }
            setUpMapIfNeeded()
        }
        setUpDefaultMapLocation()
    }

    override fun showLocationInfo(address: Pair<Address, TimeZone?>) {
        selectedAddress = address.first
        if (address.second != null) {
            timeZone = address.second!!
        }
        setLocationInfo(selectedAddress!!)
    }

    private fun setLocationEmpty() {
        this.street!!.text = ""
        this.city!!.text = ""
        this.zipCode!!.text = ""
        changeLocationInfoLayoutVisibility(View.VISIBLE)
    }

    override fun didGetLocationInfo() {
        showLocationInfoLayout()
    }

    override fun showGetLocationInfoError() {
        setLocationEmpty()
    }

    private fun showLocationInfoLayout() {
        changeLocationInfoLayoutVisibility(View.VISIBLE)
    }

    private fun getSavedInstanceParams(savedInstanceState: Bundle) {
        if (savedInstanceState.containsKey(TRANSITION_BUNDLE)) {
            bundle.putBundle(TRANSITION_BUNDLE, savedInstanceState.getBundle(TRANSITION_BUNDLE))
        } else {
            bundle.putBundle(TRANSITION_BUNDLE, savedInstanceState)
        }
        if (savedInstanceState.keySet().contains(LOCATION_KEY)) {
            currentLocation = savedInstanceState.getParcelable(LOCATION_KEY)
        }
        setUpDefaultMapLocation()
        if (savedInstanceState.keySet().contains(LAYOUTS_TO_HIDE)) {
            setLayoutVisibilityFromBundle(savedInstanceState)
        }
        if (savedInstanceState.keySet().contains(GEOLOC_API_KEY)) {
            apiInteractor!!.setApiKey(savedInstanceState.getString(GEOLOC_API_KEY, ""))
        }
        if (savedInstanceState.keySet().contains(ENABLE_GOOGLE_PLACES)) {
            isGooglePlacesEnabled = savedInstanceState.getBoolean(ENABLE_GOOGLE_PLACES, false)
        }
        if (savedInstanceState.keySet().contains(ENABLE_GOOGLE_TIME_ZONE)) {
            isGoogleTimeZoneEnabled = savedInstanceState.getBoolean(ENABLE_GOOGLE_TIME_ZONE, false)
        }
        if (savedInstanceState.keySet().contains(SEARCH_ZONE)) {
            searchZone = savedInstanceState.getString(SEARCH_ZONE)
        }
        if (savedInstanceState.keySet().contains(ENABLE_SATELLITE_VIEW)) {
            enableSatelliteView = savedInstanceState.getBoolean(ENABLE_SATELLITE_VIEW)
        }
        if (savedInstanceState.keySet().contains(POIS_LIST)) {
            poisList = savedInstanceState.getParcelableArrayList(POIS_LIST)
        }
        if (savedInstanceState.keySet().contains(ENABLE_LOCATION_PERMISSION_REQUEST)) {
            enableLocationPermissionRequest = savedInstanceState.getBoolean(ENABLE_LOCATION_PERMISSION_REQUEST)
        }
        if (savedInstanceState.keySet().contains(ENABLE_VOICE_SEARCH)) {
            isVoiceSearchEnabled = savedInstanceState.getBoolean(ENABLE_VOICE_SEARCH, true)
        }
    }

    private fun getTransitionBundleParams(transitionBundle: Bundle) {
        bundle.putBundle(TRANSITION_BUNDLE, transitionBundle)
        if (transitionBundle.keySet().contains(LATITUDE) && transitionBundle.keySet()
                        .contains(LONGITUDE)) {
            setLocationFromBundle(transitionBundle)
        }
        if (transitionBundle.keySet().contains(LAYOUTS_TO_HIDE)) {
            setLayoutVisibilityFromBundle(transitionBundle)
        }
        if (transitionBundle.keySet().contains(SEARCH_ZONE)) {
            searchZone = transitionBundle.getString(SEARCH_ZONE)
        }
        if (transitionBundle.keySet().contains(BACK_PRESSED_RETURN_OK)) {
            shouldReturnOkOnBackPressed = transitionBundle.getBoolean(BACK_PRESSED_RETURN_OK)
        }
        if (transitionBundle.keySet().contains(ENABLE_SATELLITE_VIEW)) {
            enableSatelliteView = transitionBundle.getBoolean(ENABLE_SATELLITE_VIEW)
        }
        if (transitionBundle.keySet().contains(ENABLE_LOCATION_PERMISSION_REQUEST)) {
            enableLocationPermissionRequest = transitionBundle.getBoolean(ENABLE_LOCATION_PERMISSION_REQUEST)
        }
        if (transitionBundle.keySet().contains(POIS_LIST)) {
            poisList = transitionBundle.getParcelableArrayList(POIS_LIST)
        }
        if (transitionBundle.keySet().contains(GEOLOC_API_KEY)) {
            apiInteractor!!.setApiKey(transitionBundle.getString(GEOLOC_API_KEY, ""))
        }
        if (transitionBundle.keySet().contains(ENABLE_GOOGLE_PLACES)) {
            isGooglePlacesEnabled = transitionBundle.getBoolean(ENABLE_GOOGLE_PLACES, false)
        }
        if (transitionBundle.keySet().contains(ENABLE_GOOGLE_TIME_ZONE)) {
            isGoogleTimeZoneEnabled = transitionBundle.getBoolean(ENABLE_GOOGLE_TIME_ZONE, false)
        }
        if (transitionBundle.keySet().contains(ENABLE_VOICE_SEARCH)) {
            isVoiceSearchEnabled = transitionBundle.getBoolean(ENABLE_VOICE_SEARCH, true)
        }
    }

    private fun setLayoutVisibilityFromBundle(transitionBundle: Bundle) {
        val options = transitionBundle.getString(LAYOUTS_TO_HIDE)
        if (options != null && options.contains(OPTIONS_HIDE_STREET)) {
            isStreetVisible = false
        }
        if (options != null && options.contains(OPTIONS_HIDE_CITY)) {
            isCityVisible = false
        }
        if (options != null && options.contains(OPTIONS_HIDE_ZIPCODE)) {
            isZipCodeVisible = false
        }
    }

    private fun setLocationFromBundle(transitionBundle: Bundle) {
        if (currentLocation == null) {
            currentLocation = Location(getString(R.string.leku_network_resource))
        }
        currentLocation!!.latitude = transitionBundle.getDouble(LATITUDE)
        currentLocation!!.longitude = transitionBundle.getDouble(LONGITUDE)
        setCurrentPositionLocation()
        isLocationInformedFromBundle = true
    }

    private fun startVoiceRecognitionActivity() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.leku_voice_search_promp))
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,
                getString(R.string.leku_voice_search_extra_language))

        if (isPlayServicesAvailable()) {
            try {
                startActivityForResult(intent, REQUEST_PLACE_PICKER)
            } catch (e: ActivityNotFoundException) {
                track(TrackEvents.START_VOICE_RECOGNITION_ACTIVITY_FAILED)
            }
        }
    }

    private fun isPlayServicesAvailable(): Boolean {
        val googleAPI = GoogleApiAvailability.getInstance()
        val result = googleAPI.isGooglePlayServicesAvailable(applicationContext)
        if (result != ConnectionResult.SUCCESS) {
            if (googleAPI.isUserResolvableError(result)) {
                googleAPI.getErrorDialog(this, result, CONNECTION_FAILURE_RESOLUTION_REQUEST).show()
            }
            return false
        }
        return true
    }

    private fun setCoordinatesInfo(latLng: LatLng) {
        this.latitude!!.text = String.format("%s: %s", getString(R.string.leku_latitude), latLng.latitude)
        this.longitude!!.text = String.format("%s: %s", getString(R.string.leku_longitude), latLng.longitude)
        showCoordinatesLayout()
    }

    private fun setLocationInfo(address: Address) {
        street!!.text = address.getAddressLine(0)
        city!!.text = if (isStreetEqualsCity(address)) "" else address.locality
        zipCode!!.text = address.postalCode
        showAddressLayout()
    }

    private fun setLocationInfo(poi: LekuPoi) {
        this.currentLekuPoi = poi
        street!!.text = poi.title
        city!!.text = poi.address
        zipCode!!.text = null
        showAddressLayout()
    }

    private fun isStreetEqualsCity(address: Address): Boolean {
        return address.getAddressLine(0) == address.locality
    }

    private fun setNewMapMarker(latLng: LatLng) {
        if (map != null) {
            if (currentMarker != null) {
                currentMarker!!.remove()
            }
            val cameraPosition = CameraPosition.Builder().target(latLng)
                    .zoom(defaultZoom.toFloat())
                    .build()
            hasWiderZoom = false
            map!!.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
            currentMarker = addMarker(latLng)
            map!!.setOnMarkerDragListener(object : GoogleMap.OnMarkerDragListener {
                override fun onMarkerDragStart(marker: Marker) {
                }

                override fun onMarkerDrag(marker: Marker) {}

                override fun onMarkerDragEnd(marker: Marker) {
                    if (currentLocation == null) {
                        currentLocation = Location(getString(R.string.leku_network_resource))
                    }
                    currentLekuPoi = null
                    currentLocation!!.longitude = marker.position.longitude
                    currentLocation!!.latitude = marker.position.latitude
                    setCurrentPositionLocation()
                }
            })
        }
    }

    private fun retrieveLocationFrom(query: String) {
        if (searchZone != null && !searchZone!!.isEmpty()) {
            retrieveLocationFromZone(query, searchZone!!)
        } else {
            retrieveLocationFromDefaultZone(query)
        }
    }

    private fun retrieveLocationWithDebounceTimeFrom(query: String) {
        if (searchZone != null && !searchZone!!.isEmpty()) {
            retrieveDebouncedLocationFromZone(query, searchZone!!, DEBOUNCE_TIME)
        } else {
            retrieveDebouncedLocationFromDefaultZone(query, DEBOUNCE_TIME)
        }
    }

    private fun retrieveLocationFromDefaultZone(query: String) {
        if (CountryLocaleRect.defaultLowerLeft != null) {
            geocoderPresenter!!.getFromLocationName(query, CountryLocaleRect.defaultLowerLeft!!,
                    CountryLocaleRect.defaultUpperRight!!)
        } else {
            geocoderPresenter!!.getFromLocationName(query)
        }
    }

    private fun retrieveLocationFromZone(query: String, zoneKey: String) {
        val locale = Locale(zoneKey)
        if (CountryLocaleRect.getLowerLeftFromZone(locale) != null) {
            geocoderPresenter!!.getFromLocationName(query, CountryLocaleRect.getLowerLeftFromZone(locale)!!,
                    CountryLocaleRect.getUpperRightFromZone(locale)!!)
        } else {
            geocoderPresenter!!.getFromLocationName(query)
        }
    }

    private fun retrieveDebouncedLocationFromDefaultZone(query: String, debounceTime: Int) {
        if (CountryLocaleRect.defaultLowerLeft != null) {
            geocoderPresenter!!.getDebouncedFromLocationName(query, CountryLocaleRect.defaultLowerLeft!!,
                    CountryLocaleRect.defaultUpperRight!!, debounceTime)
        } else {
            geocoderPresenter!!.getDebouncedFromLocationName(query, debounceTime)
        }
    }

    private fun retrieveDebouncedLocationFromZone(query: String, zoneKey: String, debounceTime: Int) {
        val locale = Locale(zoneKey)
        if (CountryLocaleRect.getLowerLeftFromZone(locale) != null) {
            geocoderPresenter!!.getDebouncedFromLocationName(query, CountryLocaleRect.getLowerLeftFromZone(locale)!!,
                    CountryLocaleRect.getUpperRightFromZone(locale)!!, debounceTime)
        } else {
            geocoderPresenter!!.getDebouncedFromLocationName(query, debounceTime)
        }
    }

    private fun returnCurrentPosition() {
        when {
            currentLekuPoi != null -> {
                val returnIntent = Intent()
                returnIntent.putExtra(LATITUDE, currentLekuPoi!!.location.latitude)
                returnIntent.putExtra(LONGITUDE, currentLekuPoi!!.location.longitude)
                if (street != null && city != null) {
                    returnIntent.putExtra(LOCATION_ADDRESS, locationAddress)
                }
                returnIntent.putExtra(TRANSITION_BUNDLE, bundle.getBundle(TRANSITION_BUNDLE))
                returnIntent.putExtra(LEKU_POI, currentLekuPoi)
                setResult(Activity.RESULT_OK, returnIntent)
                track(TrackEvents.RESULT_OK)
            }
            currentLocation != null -> {
                val returnIntent = Intent()
                returnIntent.putExtra(LATITUDE, currentLocation!!.latitude)
                returnIntent.putExtra(LONGITUDE, currentLocation!!.longitude)
                if (street != null && city != null) {
                    returnIntent.putExtra(LOCATION_ADDRESS, locationAddress)
                }
                if (zipCode != null) {
                    returnIntent.putExtra(ZIPCODE, zipCode!!.text)
                }
                returnIntent.putExtra(ADDRESS, selectedAddress)
                if (isGoogleTimeZoneEnabled) {
                    returnIntent.putExtra(TIME_ZONE_ID, timeZone.id)
                    returnIntent.putExtra(TIME_ZONE_DISPLAY_NAME, timeZone.displayName)
                }
                returnIntent.putExtra(TRANSITION_BUNDLE, bundle.getBundle(TRANSITION_BUNDLE))
                setResult(Activity.RESULT_OK, returnIntent)
                track(TrackEvents.RESULT_OK)
            }
            else -> {
                setResult(Activity.RESULT_CANCELED)
                track(TrackEvents.CANCEL)
            }
        }
        finish()
    }

    private fun updateLocationNameList(addresses: List<Address>) {
        locationNameList.clear()
        for (address in addresses) {
            if (address.featureName == null) {
                locationNameList.add(getString(R.string.leku_unknown_location))
            } else {
                locationNameList.add(getFullAddressString(address))
            }
        }
    }

    private fun getFullAddressString(address: Address): String {
        var fullAddress = ""
        if (address.featureName != null) {
            fullAddress += address.featureName
        }
        if (address.subLocality != null && !address.subLocality.isEmpty()) {
            fullAddress += ", " + address.subLocality
        }
        if (address.locality != null && !address.locality.isEmpty()) {
            fullAddress += ", " + address.locality
        }
        if (address.countryName != null && !address.countryName.isEmpty()) {
            fullAddress += ", " + address.countryName
        }
        return fullAddress
    }

    private fun setDefaultMapSettings() {
        if (map != null) {
            map!!.mapType = MAP_TYPE_NORMAL
            map!!.setOnMapLongClickListener(this)
            map!!.setOnMapClickListener(this)
            map!!.uiSettings.isCompassEnabled = false
            map!!.uiSettings.isMyLocationButtonEnabled = true
            map!!.uiSettings.isMapToolbarEnabled = false
        }
    }

    private fun setUpDefaultMapLocation() {
        if (currentLocation != null) {
            setCurrentPositionLocation()
        } else {
            searchView = findViewById(R.id.leku_search)
            retrieveLocationFrom(Locale.getDefault().displayCountry)
            hasWiderZoom = true
        }
    }

    private fun setCurrentPositionLocation() {
        if (currentLocation != null) {
            setNewMapMarker(LatLng(currentLocation!!.latitude, currentLocation!!.longitude))
            geocoderPresenter!!.getInfoFromLocation(LatLng(currentLocation!!.latitude,
                    currentLocation!!.longitude))
        }
    }

    private fun setPois() {
        if (poisList != null && !poisList!!.isEmpty()) {
            lekuPoisMarkersMap = HashMap()
            for (lekuPoi in poisList!!) {
                val location = lekuPoi.location
                val marker = addPoiMarker(LatLng(location.latitude, location.longitude),
                        lekuPoi.title, lekuPoi.address)
                lekuPoisMarkersMap!![marker.id] = lekuPoi
            }

            map!!.setOnMarkerClickListener { marker ->
                val lekuPoi = lekuPoisMarkersMap!![marker.id]
                if (lekuPoi != null) {
                    setLocationInfo(lekuPoi)
                    centerToPoi(lekuPoi)
                    track(TrackEvents.SIMPLE_ON_LOCALIZE_BY_LEKU_POI)
                }
                true
            }
        }
    }

    private fun centerToPoi(lekuPoi: LekuPoi) {
        if (map != null) {
            val location = lekuPoi.location
            val cameraPosition = CameraPosition.Builder()
                    .target(LatLng(location.latitude,
                            location.longitude)).zoom(defaultZoom.toFloat()).build()
            hasWiderZoom = false
            map!!.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
        }
    }

    @Synchronized
    private fun buildGoogleApiClient() {
        val googleApiClientBuilder = GoogleApiClient.Builder(this).addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)

        if (isGooglePlacesEnabled) {
            googleApiClientBuilder.addApi(Places.GEO_DATA_API)
        }

        googleApiClient = googleApiClientBuilder.build()
        googleApiClient!!.connect()
    }

    private fun addMarker(latLng: LatLng): Marker {
        return map!!.addMarker(MarkerOptions().position(latLng).draggable(true))
    }

    private fun addPoiMarker(latLng: LatLng, title: String, address: String): Marker {
        return map!!.addMarker(MarkerOptions().position(latLng)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                .title(title)
                .snippet(address))
    }

    private fun setNewLocation(address: Address) {
        this.selectedAddress = address
        if (currentLocation == null) {
            currentLocation = Location(getString(R.string.leku_network_resource))
        }

        currentLocation!!.latitude = address.latitude
        currentLocation!!.longitude = address.longitude
        setNewMapMarker(LatLng(address.latitude, address.longitude))
        setLocationInfo(address)
        searchView!!.setText("")
    }

    private fun fillLocationList(addresses: List<Address>) {
        locationList.clear()
        locationList.addAll(addresses)
    }

    private fun closeKeyboard() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    class Builder {
        private var locationLatitude: Double? = null
        private var locationLongitude: Double? = null
        private var locationSearchZone: String? = null
        private var layoutsToHide = ""
        private var enableSatelliteView = true
        private var shouldReturnOkOnBackPressed = false
        private var lekuPois: List<LekuPoi>? = null
        private var geolocApiKey: String? = null
        private var googlePlacesEnabled = false
        private var googleTimeZoneEnabled = false
        private var voiceSearchEnabled = true

        fun withLocation(latitude: Double, longitude: Double): Builder {
            this.locationLatitude = latitude
            this.locationLongitude = longitude
            return this
        }

        fun withLocation(latLng: LatLng?): Builder {
            if (latLng != null) {
                this.locationLatitude = latLng.latitude
                this.locationLongitude = latLng.longitude
            }
            return this
        }

        fun withSearchZone(searchZone: String): Builder {
            this.locationSearchZone = searchZone
            return this
        }

        fun withSatelliteViewHidden(): Builder {
            this.enableSatelliteView = false
            return this
        }

        fun shouldReturnOkOnBackPressed(): Builder {
            this.shouldReturnOkOnBackPressed = true
            return this
        }

        fun withStreetHidden(): Builder {
            this.layoutsToHide = String.format("%s|%s", layoutsToHide, OPTIONS_HIDE_STREET)
            return this
        }

        fun withCityHidden(): Builder {
            this.layoutsToHide = String.format("%s|%s", layoutsToHide, OPTIONS_HIDE_CITY)
            return this
        }

        fun withZipCodeHidden(): Builder {
            this.layoutsToHide = String.format("%s|%s", layoutsToHide, OPTIONS_HIDE_ZIPCODE)
            return this
        }

        fun withPois(pois: List<LekuPoi>): Builder {
            this.lekuPois = pois
            return this
        }

        fun withGeolocApiKey(apiKey: String): Builder {
            this.geolocApiKey = apiKey
            return this
        }

        fun withGooglePlacesEnabled(): Builder {
            this.googlePlacesEnabled = true
            return this
        }

        fun withGoogleTimeZoneEnabled(): Builder {
            this.googleTimeZoneEnabled = true
            return this
        }

        fun withVoiceSearchHidden(): Builder {
            this.voiceSearchEnabled = false
            return this
        }

        fun build(context: Context): Intent {
            val intent = Intent(context, LocationPickerActivity::class.java)

            if (locationLatitude != null) {
                intent.putExtra(LATITUDE, locationLatitude!!)
            }
            if (locationLongitude != null) {
                intent.putExtra(LONGITUDE, locationLongitude!!)
            }
            if (locationSearchZone != null) {
                intent.putExtra(SEARCH_ZONE, locationSearchZone)
            }
            if (!layoutsToHide.isEmpty()) {
                intent.putExtra(LAYOUTS_TO_HIDE, layoutsToHide)
            }
            intent.putExtra(BACK_PRESSED_RETURN_OK, shouldReturnOkOnBackPressed)
            intent.putExtra(ENABLE_SATELLITE_VIEW, enableSatelliteView)
            if (lekuPois != null && !lekuPois!!.isEmpty()) {
                intent.putExtra(POIS_LIST, ArrayList(lekuPois!!))
            }
            if (geolocApiKey != null) {
                intent.putExtra(GEOLOC_API_KEY, geolocApiKey)
            }
            intent.putExtra(ENABLE_GOOGLE_PLACES, googlePlacesEnabled)
            intent.putExtra(ENABLE_GOOGLE_TIME_ZONE, googleTimeZoneEnabled)
            intent.putExtra(ENABLE_VOICE_SEARCH, voiceSearchEnabled)

            return intent
        }
    }
}
