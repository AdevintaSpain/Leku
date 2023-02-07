package com.adevinta.leku

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.res.Resources
import android.graphics.PorterDuff
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.speech.RecognizerIntent
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import android.widget.LinearLayout
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RawRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.adevinta.leku.geocoder.GeocoderDataSourceInterface
import com.adevinta.leku.geocoder.GeocoderPresenter
import com.adevinta.leku.geocoder.GeocoderViewInterface
import com.adevinta.leku.geocoder.GoogleGeocoderDataSource
import com.adevinta.leku.geocoder.GeocoderRepository
import com.adevinta.leku.geocoder.AndroidGeocoderDataSource
import com.adevinta.leku.geocoder.adapters.DefaultSuggestionAdapter
import com.adevinta.leku.geocoder.PlaceSuggestion
import com.adevinta.leku.geocoder.adapters.DefaultAddressAdapter
import com.adevinta.leku.geocoder.adapters.SearchViewHolder
import com.adevinta.leku.geocoder.adapters.SuggestionViewHolder
import com.adevinta.leku.geocoder.adapters.base.LekuSearchAdapter
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.MAP_TYPE_NORMAL
import com.google.android.gms.maps.GoogleMap.MAP_TYPE_SATELLITE
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.libraries.places.api.Places
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.maps.GeoApiContext
import com.adevinta.leku.geocoder.api.AddressBuilder
import com.adevinta.leku.geocoder.api.NetworkClient
import com.adevinta.leku.geocoder.api.SuggestionBuilder
import com.adevinta.leku.geocoder.places.GooglePlacesDataSource
import com.adevinta.leku.geocoder.timezone.GoogleTimeZoneDataSource
import com.adevinta.leku.locale.DefaultCountryLocaleRect
import com.adevinta.leku.locale.SearchZoneRect
import com.adevinta.leku.permissions.PermissionUtils
import com.adevinta.leku.tracker.TrackEvents
import com.adevinta.leku.utils.ReactiveLocationProvider
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import java.util.Locale
import java.util.TimeZone
import kotlin.collections.set

const val LATITUDE = "latitude"
const val LONGITUDE = "longitude"
const val ZIPCODE = "zipcode"
const val ADDRESS = "address"
const val LOCATION_ADDRESS = "location_address"
const val TRANSITION_BUNDLE = "transition_bundle"
const val LAYOUTS_TO_HIDE = "layouts_to_hide"
const val SEARCH_ZONE = "search_zone"
const val SEARCH_ZONE_RECT = "search_zone_rect"
const val SEARCH_ZONE_DEFAULT_LOCALE = "search_zone_default_locale"
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
const val MAP_STYLE = "map_style"
const val UNNAMED_ROAD_VISIBILITY = "unnamed_road_visibility"
const val WITH_LEGACY_LAYOUT = "with_legacy_layout"
const val SEARCH_BAR_HIDDEN = "search_view_hidden"
private const val GEOLOC_API_KEY = "geoloc_api_key"
private const val PLACES_API_KEY = "places_api_key"
private const val LOCATION_KEY = "location_key"
private const val LAST_LOCATION_QUERY = "last_location_query"
private const val OPTIONS_HIDE_STREET = "street"
private const val OPTIONS_HIDE_CITY = "city"
private const val OPTIONS_HIDE_ZIPCODE = "zipcode"
private const val UNNAMED_ROAD_WITH_COMMA = "Unnamed Road, "
private const val UNNAMED_ROAD_WITH_HYPHEN = "Unnamed Road - "
private const val CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000
private const val DEFAULT_ZOOM = 16
private const val WIDER_ZOOM = 6
private const val MIN_CHARACTERS = 2
private const val DEBOUNCE_TIME = 400
private const val PADDING_GOOGLE_LOGO_TOP_RIGHT = 24.0f

class LocationPickerActivity :
    AppCompatActivity(),
    OnMapReadyCallback,
    GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener,
    LocationListener,
    GoogleMap.OnMapLongClickListener,
    GeocoderViewInterface,
    GoogleMap.OnMapClickListener {

    companion object {
        var customDataSource: GeocoderDataSourceInterface? = null
        var customAdapter: LekuSearchAdapter<*, *>? = null
        var currentLocationBitmapMaker: BitmapDescriptor? = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
        var otherLocationBitmapMaker: BitmapDescriptor? = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)
    }


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
    private var searchResultsList: RecyclerView? = null
    private var searchAdapter: LekuSearchAdapter<*, *>? = null
    private lateinit var linearLayoutManager: RecyclerView.LayoutManager
    private var clearSearchButton: ImageView? = null
    private var searchOption: MenuItem? = null
    private var clearLocationButton: ImageButton? = null
    private var searchEditLayout: LinearLayout? = null
    private var searchFrameLayout: FrameLayout? = null

    private val locationList = ArrayList<Address>()
    private val suggestionList = ArrayList<PlaceSuggestion>()
    private var locationNameList: MutableList<String> = ArrayList()
    private var hasWiderZoom = false
    private val bundle = Bundle()
    private var selectedAddress: Address? = null
    private var selectedSuggestion: PlaceSuggestion? = null
    private var isLocationInformedFromBundle = false
    private var isStreetVisible = true
    private var isCityVisible = true
    private var isZipCodeVisible = true
    private var shouldReturnOkOnBackPressed = false
    private var enableSatelliteView = true
    private var enableLocationPermissionRequest = true
    private var geoApiKey: String? = null
    private var googlePlacesApiKey: String? = null
    private var isGoogleTimeZoneEnabled = false
    private var searchZone: String? = null
    private var searchZoneRect: SearchZoneRect? = null
    private var isSearchZoneWithDefaultLocale = false
    private var poisList: List<LekuPoi>? = null
    private var lekuPoisMarkersMap: MutableMap<String, LekuPoi>? = null
    private var currentMarker: Marker? = null
    private var textWatcher: TextWatcher? = null
    private var googleGeocoderDataSource: GoogleGeocoderDataSource? = null
    private var isVoiceSearchEnabled = true
    private var isUnnamedRoadVisible = true
    private var mapStyle: Int? = null
    private var isLegacyLayoutEnabled = false
    private var isSearchLayoutShown = false
    private var isSearchBarHidden = false
    private var placeResolution = false

    private lateinit var toolbar: MaterialToolbar
    private lateinit var timeZone: TimeZone

    private val voiceRecognitionActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                onVoiceRecognitionActivityResult(result.data)
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
            street?.let {
                if (it.text.toString().isNotEmpty()) {
                    locationAddress = if (isUnnamedRoadVisible) {
                        it.text.toString()
                    } else {
                        removeUnnamedRoad(it.text.toString())
                    }
                }
            }
            city?.let {
                if (it.text.toString().isNotEmpty()) {
                    if (locationAddress.isNotEmpty()) {
                        locationAddress += ", "
                    }
                    locationAddress += it.text.toString()
                }
            }
            return locationAddress
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        updateValuesFromBundle(savedInstanceState)
        setUpContentView()
        setUpMainVariables()
        setUpResultsList()
        setUpToolBar()
        checkLocationPermission()
        setUpSearchView()
        setUpMapIfNeeded()
        setUpFloatingButtons()
        buildGoogleApiClient()
        track(TrackEvents.ON_LOAD_LOCATION_PICKER)
    }

    private fun setUpContentView() {
        if (isLegacyLayoutEnabled) {
            setContentView(R.layout.leku_activity_location_picker_legacy)
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                var flags: Int = window.decorView.systemUiVisibility
                flags = flags and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
                window.decorView.systemUiVisibility = flags
            }

            setContentView(R.layout.leku_activity_location_picker)
            moveGoogleLogoToTopRight()
        }
    }

    @SuppressLint("InlinedApi")
    private fun moveGoogleLogoToTopRight() {
        val contentView: View = findViewById(android.R.id.content)
        val googleLogo: View? = contentView.findViewWithTag("GoogleWatermark")
        googleLogo?.let {
            val glLayoutParams: RelativeLayout.LayoutParams =
                it.layoutParams as RelativeLayout.LayoutParams
            glLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 0)
            glLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0)
            glLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_START, 0)
            glLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE)
            glLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_END, RelativeLayout.TRUE)
            val paddingTopInPixels =
                TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    PADDING_GOOGLE_LOGO_TOP_RIGHT,
                    resources.displayMetrics
                ).toInt()
            it.setPadding(0, paddingTopInPixels, 0, 0)
            it.layoutParams = glLayoutParams
        }
    }

    private fun checkLocationPermission() {
        if (enableLocationPermissionRequest &&
            PermissionUtils.shouldRequestLocationStoragePermission(applicationContext)
        ) {
            PermissionUtils.requestLocationPermission(this)
        }
    }

    private fun track(event: TrackEvents) {
        LocationPicker.getTracker().onEventTracked(event)
    }

    private fun setUpMainVariables() {
        var placesDataSource: GooglePlacesDataSource? = null
        if (!Places.isInitialized() && !googlePlacesApiKey.isNullOrEmpty()) {
            googlePlacesApiKey?.let {
                Places.initialize(applicationContext, it)
            }
            placesDataSource = GooglePlacesDataSource(Places.createClient(this))
        }
        val geocoder = Geocoder(this, Locale.getDefault())
        if (googleGeocoderDataSource == null) {
            googleGeocoderDataSource = GoogleGeocoderDataSource(
                NetworkClient(),
                AddressBuilder(),
                SuggestionBuilder()
            )
        }
        val geocoderRepository = GeocoderRepository(
            customDataSource,
            AndroidGeocoderDataSource(geocoder),
            googleGeocoderDataSource!!
        )
        val timeZoneDataSource = GoogleTimeZoneDataSource(
            GeoApiContext.Builder().apiKey(GoogleTimeZoneDataSource.getGeoApiKey(this)).build()
        )
        geocoderPresenter = GeocoderPresenter(
            ReactiveLocationProvider(applicationContext),
            geocoderRepository,
            placesDataSource,
            timeZoneDataSource
        )
        geocoderPresenter?.setUI(this)
        progressBar = findViewById(R.id.loading_progress_bar)
        progressBar?.visibility = View.GONE
        locationInfoLayout = findViewById(R.id.location_info)
        longitude = findViewById(R.id.longitude)
        latitude = findViewById(R.id.latitude)
        street = findViewById(R.id.street)
        coordinates = findViewById(R.id.coordinates)
        city = findViewById(R.id.city)
        zipCode = findViewById(R.id.zipCode)
        clearSearchButton = findViewById(R.id.leku_clear_search_image)
        clearSearchButton?.setOnClickListener {
            searchView?.setText("")
        }
        locationNameList = ArrayList()
        clearLocationButton = findViewById(R.id.btnClearSelectedLocation)
        clearLocationButton?.setOnClickListener {
            currentLocation = null
            currentLekuPoi = null
            currentMarker?.remove()
            changeLocationInfoLayoutVisibility(View.GONE)
        }
        searchEditLayout = findViewById(R.id.leku_search_touch_zone)
        searchFrameLayout = findViewById(R.id.search_frame_layout)
    }

    private fun setUpResultsList() {
        if (isLegacyLayoutEnabled) {
            listResult = findViewById(R.id.resultlist)
            adapter =
                ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, locationNameList)
            listResult?.let {
                it.adapter = adapter
                it.setOnItemClickListener { _, _, position, _ ->
                    if (locationList[position].hasLatitude() &&
                        locationList[position].hasLongitude()
                    ) {
                        setNewLocation(locationList[position])
                        changeListResultVisibility(View.GONE)
                        closeKeyboard()
                    }
                }
            }
        } else {
            linearLayoutManager = LinearLayoutManager(this)
            when {
                placeResolution -> {
                    searchAdapter = customAdapter ?: DefaultSuggestionAdapter(
                        this,
                    )
                    searchAdapter?.onClick = {
                        setNewSuggestion(suggestionList[it])
                        changeListResultVisibility(View.GONE)
                        closeKeyboard()
                        hideSearchLayout()
                    }
                }
                else -> {
                    searchAdapter = customAdapter ?: DefaultAddressAdapter(
                        this,
                    )
                    searchAdapter?.onClick = {
                        if (locationList[it].hasLatitude() && locationList[it].hasLongitude()) {
                            setNewLocation(locationList[it])
                            changeListResultVisibility(View.GONE)
                            closeKeyboard()
                            hideSearchLayout()
                        }
                    }
                }
            }
            searchResultsList = findViewById<RecyclerView>(R.id.search_result_list).apply {
                setHasFixedSize(true)
                layoutManager = linearLayoutManager
                adapter = searchAdapter
                addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
            }
        }
    }

    private fun setUpToolBar() {
        toolbar = findViewById(R.id.map_search_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.setDisplayShowTitleEnabled(false)
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
        if (isSearchBarHidden) {
            searchEditLayout?.visibility = View.GONE
        } else {
            searchView?.setOnEditorActionListener { v, actionId, _ ->
                var handled = false
                if (actionId == EditorInfo.IME_ACTION_SEARCH && v.text.toString().isNotEmpty()) {
                    retrieveLocationFrom(v.text.toString())
                    closeKeyboard()
                    handled = true
                }
                handled
            }
            createSearchTextChangeObserver()
            if (!isLegacyLayoutEnabled) {
                searchView?.setOnFocusChangeListener { _: View?, hasFocus: Boolean ->
                    if (hasFocus) {
                        showSearchLayout()
                    }
                }
            }
        }
    }

    private fun createSearchTextChangeObserver() {
        searchView?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                onSearchTextChanged(s.toString())
            }
        })
    }

    private fun onSearchTextChanged(term: String) {
        if (term.isEmpty()) {
            if (isLegacyLayoutEnabled) {
                adapter?.let {
                    it.clear()
                    it.notifyDataSetChanged()
                }
            } else {
                searchAdapter?.notifyDataSetChanged()
            }
            showLocationInfoLayout()
            clearSearchButton?.visibility = View.INVISIBLE
            searchOption?.setIcon(R.drawable.leku_ic_mic_legacy)
            updateVoiceSearchVisibility()
        } else {
            if (term.length > MIN_CHARACTERS) {
                if (placeResolution) geocoderPresenter?.getSuggestionsFromLocationName(term)
                else retrieveLocationWithDebounceTimeFrom(term)
            }
            clearSearchButton?.visibility = View.VISIBLE
            searchOption?.setIcon(R.drawable.leku_ic_search)
            searchOption?.isVisible = true
        }
    }

    private fun showSearchLayout() {
        searchFrameLayout?.setBackgroundResource(R.color.leku_white)
        searchEditLayout?.setBackgroundResource(R.drawable.leku_search_text_with_border_background)
        searchResultsList?.visibility = View.VISIBLE
        isSearchLayoutShown = true
    }

    private fun hideSearchLayout() {
        searchFrameLayout?.setBackgroundResource(android.R.color.transparent)
        searchEditLayout?.setBackgroundResource(R.drawable.leku_search_text_background)
        searchResultsList?.visibility = View.GONE
        searchView?.clearFocus()
        isSearchLayoutShown = false
    }

    private fun setUpFloatingButtons() {
        val btnMyLocation = findViewById<FloatingActionButton>(R.id.btnMyLocation)
        btnMyLocation.setOnClickListener {
            checkLocationPermission()
            geocoderPresenter?.getLastKnownLocation()
            track(TrackEvents.ON_LOCALIZED_ME)
        }

        val btnAcceptLocation = if (isLegacyLayoutEnabled) {
            findViewById<FloatingActionButton>(R.id.btnAccept)
        } else {
            findViewById<Button>(R.id.btnAccept)
        }
        btnAcceptLocation.setOnClickListener { returnCurrentPosition() }

        val btnSatellite = findViewById<FloatingActionButton>(R.id.btnSatellite)
        btnSatellite?.setOnClickListener {
            map?.let {
                it.mapType =
                    if (it.mapType == MAP_TYPE_SATELLITE) MAP_TYPE_NORMAL else MAP_TYPE_SATELLITE
                if (isLegacyLayoutEnabled) {
                    btnSatellite.setImageResource(
                        if (it.mapType == MAP_TYPE_SATELLITE)
                            R.drawable.leku_ic_satellite_off_legacy
                        else
                            R.drawable.leku_ic_satellite_on_legacy
                    )
                } else {
                    var image = 0
                    var color = 0
                    if (it.mapType == MAP_TYPE_SATELLITE) {
                        image = R.drawable.leku_ic_maps
                        color = R.color.leku_ic_maps
                    } else {
                        image = R.drawable.leku_ic_satellite
                        color = R.color.leku_ic_satellite
                    }

                    val colorRes = try {
                        ContextCompat.getColor(btnSatellite.context, color)
                    } catch (ex: Resources.NotFoundException) {
                        0
                    }

                    btnSatellite.setImageResource(image)
                    btnSatellite.setColorFilter(colorRes, PorterDuff.Mode.SRC_ATOP)
                }
            }
        }
        if (enableSatelliteView) btnSatellite.show() else btnSatellite.hide()
    }

    private fun updateValuesFromBundle(savedInstanceState: Bundle?) {
        val transitionBundle = intent.extras
        transitionBundle?.let {
            getTransitionBundleParams(it)
        }
        savedInstanceState?.let {
            getSavedInstanceParams(it)
        }
        updateAddressLayoutVisibility()
        updateVoiceSearchVisibility()

        if (!googlePlacesApiKey.isNullOrEmpty()) {
            geocoderPresenter?.enableGooglePlaces()
        }
    }

    private fun setUpMapIfNeeded() {
        if (map == null) {
            (supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment).getMapAsync(
                this
            )
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (isLegacyLayoutEnabled) {
            val inflater = menuInflater
            inflater.inflate(R.menu.leku_toolbar_menu, menu)
            searchOption = menu.findItem(R.id.action_voice)
            updateVoiceSearchVisibility()
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                if (!isLegacyLayoutEnabled && isSearchLayoutShown) {
                    hideSearchLayout()
                } else {
                    onBackPressed()
                }
                true
            }
            R.id.action_voice -> {
                searchView?.let {
                    if (it.text.toString().isEmpty()) {
                        startVoiceRecognitionActivity()
                    } else {
                        retrieveLocationFrom(it.text.toString())
                        closeKeyboard()
                    }
                }
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (PermissionUtils.isLocationPermissionGranted(applicationContext)) {
            geocoderPresenter?.getLastKnownLocation()
        }
    }

    private fun onVoiceRecognitionActivityResult(data: Intent?) {
        data?.let {
            val matches = it.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            searchView = findViewById(R.id.leku_search)
            matches?.let {
                retrieveLocationFrom(it[0])
            }
        }
    }

    override fun onStart() {
        super.onStart()
        googleApiClient?.connect()
        geocoderPresenter?.setUI(this)
    }

    override fun onStop() {
        googleApiClient?.let {
            if (it.isConnected) {
                it.disconnect()
            }
        }
        geocoderPresenter?.stop()
        super.onStop()
    }

    override fun onResume() {
        super.onResume()
        setUpMapIfNeeded()
        switchToolbarVisibility()
    }

    override fun onDestroy() {
        currentLocationBitmapMaker = null
        otherLocationBitmapMaker = null
        textWatcher?.let {
            searchView?.removeTextChangedListener(it)
        }
        googleApiClient?.unregisterConnectionCallbacks(this)
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
            setMapStyle()
            setDefaultMapSettings()
            setCurrentPositionLocation()
            setPois()
        }
    }

    override fun onConnected(savedBundle: Bundle?) {
        if (currentLocation == null) {
            geocoderPresenter?.getLastKnownLocation()
        }
    }

    override fun onConnectionSuspended(i: Int) {
        googleApiClient?.connect()
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(
                    this,
                    CONNECTION_FAILURE_RESOLUTION_REQUEST
                )
            } catch (e: IntentSender.SendIntentException) {
                track(TrackEvents.GOOGLE_API_CONNECTION_FAILED)
            }
        }
    }

    override fun onLocationChanged(location: Location) {
        currentLocation = location
    }

    public override fun onSaveInstanceState(savedInstanceState: Bundle) {
        currentLocation?.let {
            savedInstanceState.putParcelable(LOCATION_KEY, it)
        }
        searchView?.let {
            savedInstanceState.putString(LAST_LOCATION_QUERY, it.text.toString())
        }
        if (bundle.containsKey(TRANSITION_BUNDLE)) {
            savedInstanceState.putBundle(TRANSITION_BUNDLE, bundle.getBundle(TRANSITION_BUNDLE))
        }
        poisList?.let {
            savedInstanceState.putParcelableArrayList(POIS_LIST, ArrayList(it))
        }
        savedInstanceState.putBoolean(ENABLE_SATELLITE_VIEW, enableSatelliteView)
        savedInstanceState.putBoolean(
            ENABLE_LOCATION_PERMISSION_REQUEST,
            enableLocationPermissionRequest
        )
        savedInstanceState.putBoolean(
            ENABLE_GOOGLE_PLACES,
            placeResolution
        )
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
            enableLocationPermissionRequest =
                savedInstanceState.getBoolean(ENABLE_LOCATION_PERMISSION_REQUEST)
        }
        if (savedInstanceState.containsKey(ENABLE_GOOGLE_PLACES)) {
            placeResolution =
                savedInstanceState.getBoolean(ENABLE_GOOGLE_PLACES)
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
        currentLocation?.latitude = latLng.latitude
        currentLocation?.longitude = latLng.longitude
        setCurrentPositionLocation()
    }

    override fun willLoadLocation() {
        progressBar?.visibility = View.VISIBLE
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
                searchView?.setText("")
            }
            if (addresses.size == 1) {
                setNewLocation(addresses[0])
            }
            if (isLegacyLayoutEnabled) {
                adapter?.notifyDataSetChanged()
            } else {
                searchAdapter?.notifyDataSetChanged()
            }
        }
    }

    override fun showSuggestions(suggestions: List<PlaceSuggestion>) {
        fillSuggestionList(suggestions)
        if (suggestions.isEmpty()) {
            Toast.makeText(applicationContext, R.string.leku_no_search_results, Toast.LENGTH_LONG)
                .show()
        } else {
            updateSuggestionNameList(suggestions)
            if (hasWiderZoom) {
                searchView?.setText("")
            }
            searchAdapter?.notifyDataSetChanged()
        }
    }

    override fun setAddressFromSuggestion(address: Address) {
        selectedAddress = address
        locationList.clear()
        locationList.add(address)
        currentLocation?.latitude = address.latitude
        currentLocation?.longitude = address.longitude
        setNewMapMarker(LatLng(address.latitude, address.longitude))
        setLocationInfo(address)
        searchView?.setText("")
    }

    override fun showDebouncedLocations(addresses: List<Address>) {
        fillLocationList(addresses)
        if (addresses.isNotEmpty()) {
            updateLocationNameList(addresses)
        } else {
            setNoSearchResultsOnList()
        }

        if (isLegacyLayoutEnabled) {
            adapter?.notifyDataSetChanged()
        } else {
            searchAdapter?.notifyDataSetChanged()
        }
    }

    private fun setNoSearchResultsOnList() {
        val noResultsAddress = Address(Locale.getDefault())
        locationList.add(noResultsAddress)
        locationNameList.clear()
        locationNameList.add(getString(R.string.leku_no_search_results))
    }

    override fun didLoadLocation() {
        progressBar?.visibility = View.GONE

        changeListResultVisibility(
            when {
                locationList.size >= 1 || suggestionList.size >= 1 -> View.VISIBLE
                else -> View.GONE
            }
        )

        if (locationList.size == 1) {
            changeLocationInfoLayoutVisibility(View.VISIBLE)
        } else {
            changeLocationInfoLayoutVisibility(View.GONE)
        }
        track(TrackEvents.ON_SEARCH_LOCATIONS)
    }

    private fun changeListResultVisibility(visibility: Int) {
        if (isLegacyLayoutEnabled) {
            listResult?.visibility = visibility
        } else {
            searchResultsList?.visibility = visibility
        }
    }

    private fun changeLocationInfoLayoutVisibility(visibility: Int) {
        locationInfoLayout?.visibility = visibility
    }

    private fun showCoordinatesLayout() {
        longitude?.visibility = View.VISIBLE
        latitude?.visibility = View.VISIBLE
        coordinates?.visibility = View.VISIBLE
        street?.visibility = View.GONE
        city?.visibility = View.GONE
        zipCode?.visibility = View.GONE
        changeLocationInfoLayoutVisibility(View.VISIBLE)
    }

    private fun showAddressLayout() {
        longitude?.visibility = View.GONE
        latitude?.visibility = View.GONE
        coordinates?.visibility = View.GONE
        if (isStreetVisible) {
            street?.visibility = View.VISIBLE
        }
        if (isCityVisible) {
            city?.visibility = View.VISIBLE
        }
        if (isZipCodeVisible) {
            zipCode?.visibility = View.VISIBLE
        }
        changeLocationInfoLayoutVisibility(View.VISIBLE)
    }

    private fun updateAddressLayoutVisibility() {
        street?.visibility = if (isStreetVisible) View.VISIBLE else View.INVISIBLE
        city?.visibility = if (isCityVisible) View.VISIBLE else View.INVISIBLE
        zipCode?.visibility = if (isZipCodeVisible) View.VISIBLE else View.INVISIBLE
        longitude?.visibility = View.VISIBLE
        latitude?.visibility = View.VISIBLE
        coordinates?.visibility = View.VISIBLE
    }

    private fun updateVoiceSearchVisibility() {
        searchOption?.isVisible = isVoiceSearchEnabled
    }

    override fun showLoadLocationError() {
        progressBar?.visibility = View.GONE
        changeListResultVisibility(View.GONE)
        Toast.makeText(this, R.string.leku_load_location_error, Toast.LENGTH_LONG).show()
    }

    override fun willGetLocationInfo(latLng: LatLng) {
        changeLocationInfoLayoutVisibility(View.VISIBLE)
        resetLocationAddress()
        setCoordinatesInfo(latLng)
    }

    override fun showLastLocation(location: Location) {
        currentLocation = location
        didGetLastLocation()
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

    override fun showLocationInfo(address: Pair<Address?, TimeZone?>) {
        selectedAddress = address.first
        address.second?.let {
            timeZone = it
        }
        selectedAddress?.let {
            setLocationInfo(it)
        }
    }

    private fun setLocationEmpty() {
        this.street?.text = ""
        this.city?.text = ""
        this.zipCode?.text = ""
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

        googleGeocoderDataSource = GoogleGeocoderDataSource(
            NetworkClient(),
            AddressBuilder(),
            SuggestionBuilder()
        )

        geoApiKey = if (savedInstanceState.keySet().contains(GEOLOC_API_KEY)) {
            savedInstanceState.getString(GEOLOC_API_KEY, "")
        } else {
            GoogleTimeZoneDataSource.getGeoApiKey(this)
        }

        if (geoApiKey.isNullOrEmpty().not()) {
            googleGeocoderDataSource?.setGeolocationApiKey(geoApiKey!!)
        }

        googlePlacesApiKey = if (savedInstanceState.keySet().contains(PLACES_API_KEY)) {
            savedInstanceState.getString(PLACES_API_KEY, "")
        } else {
            GoogleTimeZoneDataSource.getGeoApiKey(this)
        }

        if (googlePlacesApiKey.isNullOrEmpty().not()) {
            googleGeocoderDataSource?.setPlaceApiKey(googlePlacesApiKey!!)
        }

        if (savedInstanceState.keySet().contains(ENABLE_GOOGLE_TIME_ZONE)) {
            isGoogleTimeZoneEnabled = savedInstanceState.getBoolean(ENABLE_GOOGLE_TIME_ZONE, false)
        }
        if (savedInstanceState.keySet().contains(SEARCH_ZONE)) {
            searchZone = savedInstanceState.getString(SEARCH_ZONE)
        }
        if (savedInstanceState.keySet().contains(SEARCH_ZONE_RECT)) {
            searchZoneRect = savedInstanceState.getParcelable(SEARCH_ZONE_RECT)
        }
        if (savedInstanceState.keySet().contains(SEARCH_ZONE_DEFAULT_LOCALE)) {
            isSearchZoneWithDefaultLocale =
                savedInstanceState.getBoolean(SEARCH_ZONE_DEFAULT_LOCALE, false)
        }
        if (savedInstanceState.keySet().contains(ENABLE_SATELLITE_VIEW)) {
            enableSatelliteView = savedInstanceState.getBoolean(ENABLE_SATELLITE_VIEW)
        }
        if (savedInstanceState.keySet().contains(POIS_LIST)) {
            poisList = savedInstanceState.getParcelableArrayList(POIS_LIST)
        }
        if (savedInstanceState.keySet().contains(ENABLE_LOCATION_PERMISSION_REQUEST)) {
            enableLocationPermissionRequest =
                savedInstanceState.getBoolean(ENABLE_LOCATION_PERMISSION_REQUEST)
        }
        if (savedInstanceState.keySet().contains(ENABLE_VOICE_SEARCH)) {
            isVoiceSearchEnabled = savedInstanceState.getBoolean(ENABLE_VOICE_SEARCH, true)
        }
        if (savedInstanceState.keySet().contains(UNNAMED_ROAD_VISIBILITY)) {
            isUnnamedRoadVisible = savedInstanceState.getBoolean(UNNAMED_ROAD_VISIBILITY, true)
        }
        if (savedInstanceState.keySet().contains(MAP_STYLE)) {
            mapStyle = savedInstanceState.getInt(MAP_STYLE)
        }
        if (savedInstanceState.keySet().contains(WITH_LEGACY_LAYOUT)) {
            isLegacyLayoutEnabled = savedInstanceState.getBoolean(WITH_LEGACY_LAYOUT, false)
        }
        if (savedInstanceState.keySet().contains(SEARCH_BAR_HIDDEN)) {
            isSearchBarHidden = savedInstanceState.getBoolean(SEARCH_BAR_HIDDEN, false)
        }
        if (savedInstanceState.keySet().contains(ENABLE_GOOGLE_PLACES)) {
            placeResolution = savedInstanceState.getBoolean(ENABLE_GOOGLE_PLACES, false)
        }
    }

    private fun getTransitionBundleParams(transitionBundle: Bundle) {
        bundle.putBundle(TRANSITION_BUNDLE, transitionBundle)
        if (transitionBundle.keySet().contains(LATITUDE) && transitionBundle.keySet()
            .contains(LONGITUDE)
        ) {
            setLocationFromBundle(transitionBundle)
        }
        if (transitionBundle.keySet().contains(LAYOUTS_TO_HIDE)) {
            setLayoutVisibilityFromBundle(transitionBundle)
        }
        if (transitionBundle.keySet().contains(SEARCH_ZONE)) {
            searchZone = transitionBundle.getString(SEARCH_ZONE)
        }
        if (transitionBundle.keySet().contains(SEARCH_ZONE_RECT)) {
            searchZoneRect = transitionBundle.getParcelable(SEARCH_ZONE_RECT)
        }
        if (transitionBundle.keySet().contains(SEARCH_ZONE_DEFAULT_LOCALE)) {
            isSearchZoneWithDefaultLocale =
                transitionBundle.getBoolean(SEARCH_ZONE_DEFAULT_LOCALE, false)
        }
        if (transitionBundle.keySet().contains(BACK_PRESSED_RETURN_OK)) {
            shouldReturnOkOnBackPressed = transitionBundle.getBoolean(BACK_PRESSED_RETURN_OK)
        }
        if (transitionBundle.keySet().contains(ENABLE_SATELLITE_VIEW)) {
            enableSatelliteView = transitionBundle.getBoolean(ENABLE_SATELLITE_VIEW)
        }
        if (transitionBundle.keySet().contains(ENABLE_LOCATION_PERMISSION_REQUEST)) {
            enableLocationPermissionRequest =
                transitionBundle.getBoolean(ENABLE_LOCATION_PERMISSION_REQUEST)
        }
        if (transitionBundle.keySet().contains(POIS_LIST)) {
            poisList = transitionBundle.getParcelableArrayList(POIS_LIST)
        }

        googleGeocoderDataSource = GoogleGeocoderDataSource(
            NetworkClient(),
            AddressBuilder(),
            SuggestionBuilder()
        )

        geoApiKey = if (transitionBundle.keySet().contains(GEOLOC_API_KEY)) {
            transitionBundle.getString(GEOLOC_API_KEY, "")
        } else {
            GoogleTimeZoneDataSource.getGeoApiKey(this)
        }

        if (geoApiKey.isNullOrEmpty().not()) {
            googleGeocoderDataSource?.setGeolocationApiKey(geoApiKey!!)
        }

        googlePlacesApiKey = if (transitionBundle.keySet().contains(PLACES_API_KEY)) {
            transitionBundle.getString(PLACES_API_KEY, "")
        } else {
            GoogleTimeZoneDataSource.getGeoApiKey(this)
        }

        if (googlePlacesApiKey.isNullOrEmpty().not()) {
            googleGeocoderDataSource?.setPlaceApiKey(googlePlacesApiKey!!)
        }

        if (transitionBundle.keySet().contains(ENABLE_GOOGLE_TIME_ZONE)) {
            isGoogleTimeZoneEnabled = transitionBundle.getBoolean(ENABLE_GOOGLE_TIME_ZONE, false)
        }
        if (transitionBundle.keySet().contains(ENABLE_VOICE_SEARCH)) {
            isVoiceSearchEnabled = transitionBundle.getBoolean(ENABLE_VOICE_SEARCH, true)
        }
        if (transitionBundle.keySet().contains(UNNAMED_ROAD_VISIBILITY)) {
            isUnnamedRoadVisible = transitionBundle.getBoolean(UNNAMED_ROAD_VISIBILITY, true)
        }
        if (transitionBundle.keySet().contains(MAP_STYLE)) {
            mapStyle = transitionBundle.getInt(MAP_STYLE)
        }
        if (transitionBundle.keySet().contains(WITH_LEGACY_LAYOUT)) {
            isLegacyLayoutEnabled = transitionBundle.getBoolean(WITH_LEGACY_LAYOUT, false)
        }
        if (transitionBundle.keySet().contains(SEARCH_BAR_HIDDEN)) {
            isSearchBarHidden = transitionBundle.getBoolean(SEARCH_BAR_HIDDEN, false)
        }
        if (transitionBundle.keySet().contains(ENABLE_GOOGLE_PLACES)) {
            placeResolution = transitionBundle.getBoolean(ENABLE_GOOGLE_PLACES, false)
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
        currentLocation?.latitude = transitionBundle.getDouble(LATITUDE)
        currentLocation?.longitude = transitionBundle.getDouble(LONGITUDE)
        setCurrentPositionLocation()
        isLocationInformedFromBundle = true
    }

    private fun startVoiceRecognitionActivity() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.leku_voice_search_promp))
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE,
            getString(R.string.leku_voice_search_extra_language)
        )

        if (isPlayServicesAvailable()) {
            try {
                voiceRecognitionActivityResultLauncher.launch(intent)
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
                googleAPI.getErrorDialog(this, result, CONNECTION_FAILURE_RESOLUTION_REQUEST)
                    ?.show()
            }
            return false
        }
        return true
    }

    private fun setCoordinatesInfo(latLng: LatLng) {
        this.latitude?.text =
            String.format("%s: %s", getString(R.string.leku_latitude), latLng.latitude)
        this.longitude?.text =
            String.format("%s: %s", getString(R.string.leku_longitude), latLng.longitude)
        showCoordinatesLayout()
    }

    private fun resetLocationAddress() {
        street?.text = ""
        city?.text = ""
        zipCode?.text = ""
    }

    private fun setLocationInfo(address: Address) {
        street?.let {
            val formattedAddress = getFormattedAddress(address)
            if (isUnnamedRoadVisible) {
                it.text = formattedAddress
            } else {
                it.text = removeUnnamedRoad(formattedAddress)
            }
        }
        city?.text = if (isStreetEqualsCity(address)) "" else address.locality
        zipCode?.text = address.postalCode
        showAddressLayout()
    }

    private fun getFormattedAddress(address: Address): String {
        return if (!address.thoroughfare.isNullOrEmpty() && !address.subThoroughfare.isNullOrEmpty()) {
            getString(
                R.string.leku_formatted_address,
                address.thoroughfare,
                address.subThoroughfare
            )
        } else {
            if (address.subThoroughfare.isNullOrEmpty() && !address.thoroughfare.isNullOrEmpty()) {
                address.thoroughfare
            } else if (address.thoroughfare.isNullOrEmpty() && !address.subThoroughfare.isNullOrEmpty()) {
                address.subThoroughfare
            } else {
                address.getAddressLine(0)
            }
        }
    }

    private fun setLocationInfo(poi: LekuPoi) {
        this.currentLekuPoi = poi
        street?.text = poi.title
        city?.text = poi.address
        zipCode?.text = null
        showAddressLayout()
    }

    private fun isStreetEqualsCity(address: Address): Boolean {
        return address.getAddressLine(0) == address.locality
    }

    private fun setNewMapMarker(latLng: LatLng) {
        if (map != null) {
            currentMarker?.remove()
            val cameraPosition = CameraPosition.Builder().target(latLng)
                .zoom(defaultZoom.toFloat())
                .build()
            hasWiderZoom = false
            map?.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
            currentMarker = addMarker(latLng)
            map?.setOnMarkerDragListener(object : GoogleMap.OnMarkerDragListener {
                override fun onMarkerDragStart(marker: Marker) {
                }

                override fun onMarkerDrag(marker: Marker) {}

                override fun onMarkerDragEnd(marker: Marker) {
                    if (currentLocation == null) {
                        currentLocation = Location(getString(R.string.leku_network_resource))
                    }
                    currentLekuPoi = null
                    currentLocation?.longitude = marker.position.longitude
                    currentLocation?.latitude = marker.position.latitude
                    setCurrentPositionLocation()
                }
            })
        }
    }

    private fun retrieveLocationFrom(query: String) {
        if (searchZone != null && searchZone!!.isNotEmpty()) {
            retrieveLocationFromZone(query, searchZone!!)
        } else if (searchZoneRect != null) {
            retrieveLocationFromZone(query, searchZoneRect!!)
        } else if (isSearchZoneWithDefaultLocale) {
            retrieveLocationFromDefaultZone(query)
        } else {
            geocoderPresenter?.getFromLocationName(query)
        }
    }

    private fun retrieveLocationWithDebounceTimeFrom(query: String) {
        if (searchZone != null && searchZone!!.isNotEmpty()) {
            retrieveDebouncedLocationFromZone(query, searchZone!!, DEBOUNCE_TIME)
        } else if (searchZoneRect != null) {
            retrieveDebouncedLocationFromZone(query, searchZoneRect!!, DEBOUNCE_TIME)
        } else if (isSearchZoneWithDefaultLocale) {
            retrieveDebouncedLocationFromDefaultZone(query, DEBOUNCE_TIME)
        } else {
            geocoderPresenter?.getDebouncedFromLocationName(query, DEBOUNCE_TIME)
        }
    }

    private fun retrieveLocationFromDefaultZone(query: String) {
        geocoderPresenter?.let {
            if (DefaultCountryLocaleRect.defaultLowerLeft != null) {
                it.getFromLocationName(
                    query, DefaultCountryLocaleRect.defaultLowerLeft!!,
                    DefaultCountryLocaleRect.defaultUpperRight!!
                )
            } else {
                it.getFromLocationName(query)
            }
        }
    }

    private fun retrieveLocationFromZone(query: String, zoneKey: String) {
        geocoderPresenter?.let {
            val locale = Locale(zoneKey)
            if (DefaultCountryLocaleRect.getLowerLeftFromZone(locale) != null) {
                it.getFromLocationName(
                    query, DefaultCountryLocaleRect.getLowerLeftFromZone(locale)!!,
                    DefaultCountryLocaleRect.getUpperRightFromZone(locale)!!
                )
            } else {
                it.getFromLocationName(query)
            }
        }
    }

    private fun retrieveLocationFromZone(query: String, zoneRect: SearchZoneRect) {
        geocoderPresenter?.getFromLocationName(
            query,
            zoneRect.lowerLeft,
            zoneRect.upperRight
        )
    }

    private fun retrieveDebouncedLocationFromDefaultZone(query: String, debounceTime: Int) {
        geocoderPresenter?.let {
            if (DefaultCountryLocaleRect.defaultLowerLeft != null) {
                it.getDebouncedFromLocationName(
                    query, DefaultCountryLocaleRect.defaultLowerLeft!!,
                    DefaultCountryLocaleRect.defaultUpperRight!!, debounceTime
                )
            } else {
                it.getDebouncedFromLocationName(query, debounceTime)
            }
        }
    }

    private fun retrieveDebouncedLocationFromZone(
        query: String,
        zoneKey: String,
        debounceTime: Int
    ) {
        geocoderPresenter?.let {
            val locale = Locale(zoneKey)
            if (DefaultCountryLocaleRect.getLowerLeftFromZone(locale) != null) {
                it.getDebouncedFromLocationName(
                    query, DefaultCountryLocaleRect.getLowerLeftFromZone(locale)!!,
                    DefaultCountryLocaleRect.getUpperRightFromZone(locale)!!, debounceTime
                )
            } else {
                it.getDebouncedFromLocationName(query, debounceTime)
            }
        }
    }

    private fun retrieveDebouncedLocationFromZone(
        query: String,
        zoneRect: SearchZoneRect,
        debounceTime: Int
    ) {
        geocoderPresenter?.getDebouncedFromLocationName(
            query,
            zoneRect.lowerLeft,
            zoneRect.upperRight,
            debounceTime
        )
    }

    private fun returnCurrentPosition() {
        when {
            currentLekuPoi != null -> {
                currentLekuPoi?.let {
                    val returnIntent = Intent()
                    returnIntent.putExtra(LATITUDE, it.location.latitude)
                    returnIntent.putExtra(LONGITUDE, it.location.longitude)
                    if (street != null && city != null) {
                        returnIntent.putExtra(LOCATION_ADDRESS, locationAddress)
                    }
                    returnIntent.putExtra(TRANSITION_BUNDLE, bundle.getBundle(TRANSITION_BUNDLE))
                    returnIntent.putExtra(LEKU_POI, it)
                    setResult(Activity.RESULT_OK, returnIntent)
                    track(TrackEvents.RESULT_OK)
                }
            }
            currentLocation != null -> {
                val returnIntent = Intent()
                currentLocation?.let {
                    returnIntent.putExtra(LATITUDE, it.latitude)
                    returnIntent.putExtra(LONGITUDE, it.longitude)
                }
                if (street != null && city != null) {
                    returnIntent.putExtra(LOCATION_ADDRESS, locationAddress)
                }
                zipCode?.let {
                    returnIntent.putExtra(ZIPCODE, it.text)
                }
                returnIntent.putExtra(ADDRESS, selectedAddress)
                if (isGoogleTimeZoneEnabled && ::timeZone.isInitialized) {
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
                locationNameList.add(address.getFullAddressString(this))
            }
        }
    }

    private fun updateSuggestionNameList(suggestions: List<PlaceSuggestion>) {
        locationNameList.clear()
        for (suggestion in suggestions) {
            locationNameList.add(suggestion.description)
        }
    }

    private fun setMapStyle() {
        map?.let { googleMap ->
            mapStyle?.let { style ->
                val loadStyle = MapStyleOptions.loadRawResourceStyle(this, style)
                googleMap.setMapStyle(loadStyle)
            }
        }
    }

    private fun setDefaultMapSettings() {
        map?.let {
            it.mapType = MAP_TYPE_NORMAL
            it.setOnMapLongClickListener(this)
            it.setOnMapClickListener(this)
            it.uiSettings.isCompassEnabled = false
            it.uiSettings.isMyLocationButtonEnabled = true
            it.uiSettings.isMapToolbarEnabled = false
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
        currentLocation?.let {
            setNewMapMarker(LatLng(it.latitude, it.longitude))
            geocoderPresenter?.getInfoFromLocation(
                LatLng(
                    it.latitude,
                    it.longitude
                )
            )
        }
    }

    private fun setPois() {
        poisList?.let { pois ->
            if (pois.isNotEmpty()) {
                lekuPoisMarkersMap = HashMap()
                for (lekuPoi in pois) {
                    val location = lekuPoi.location
                    val marker = addPoiMarker(
                        LatLng(location.latitude, location.longitude),
                        lekuPoi.title, lekuPoi.address
                    )
                    lekuPoisMarkersMap?.let {
                        marker?.let { marker ->
                            it[marker.id] = lekuPoi
                        }
                    }
                }

                map?.setOnMarkerClickListener { marker ->
                    lekuPoisMarkersMap?.let { poisMarkersMap ->
                        val lekuPoi = poisMarkersMap[marker.id]
                        lekuPoi?.let {
                            setLocationInfo(it)
                            centerToPoi(it)
                            track(TrackEvents.SIMPLE_ON_LOCALIZE_BY_LEKU_POI)
                        }
                    }
                    true
                }
            }
        }
    }

    private fun centerToPoi(lekuPoi: LekuPoi) {
        map?.let {
            val location = lekuPoi.location
            val cameraPosition = CameraPosition.Builder()
                .target(
                    LatLng(
                        location.latitude,
                        location.longitude
                    )
                ).zoom(defaultZoom.toFloat()).build()
            hasWiderZoom = false
            it.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
        }
    }

    @Synchronized
    private fun buildGoogleApiClient() {
        val googleApiClientBuilder = GoogleApiClient.Builder(this).addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .addApi(LocationServices.API)

        googleApiClient = googleApiClientBuilder.build()
        googleApiClient?.connect()
    }

    private fun addMarker(latLng: LatLng): Marker? {
        map?.let {
            return it.addMarker(MarkerOptions().position(latLng)
                .icon(currentLocationBitmapMaker)
                .draggable(true))
        }
        return null
    }

    private fun addPoiMarker(latLng: LatLng, title: String, address: String): Marker? {
        map?.let {
            return it.addMarker(
                MarkerOptions().position(latLng)
                    .icon(otherLocationBitmapMaker)
                    .title(title)
                    .snippet(address)
            )
        }
        return null
    }

    private fun setNewLocation(address: Address) {
        this.selectedAddress = address
        if (currentLocation == null) {
            currentLocation = Location(getString(R.string.leku_network_resource))
        }
        currentLocation?.latitude = address.latitude
        currentLocation?.longitude = address.longitude
        setNewMapMarker(LatLng(address.latitude, address.longitude))
        setLocationInfo(address)
        searchView?.setText("")
    }

    private fun setNewSuggestion(suggestion: PlaceSuggestion) {
        this.selectedSuggestion = suggestion
        geocoderPresenter?.getAddressFromPlaceId(suggestion.placeId)
    }

    private fun fillLocationList(addresses: List<Address>) {
        locationList.clear()
        locationList.addAll(addresses)
        (searchAdapter as LekuSearchAdapter<SearchViewHolder, Address>)
            .items = locationList.toList()
    }

    private fun fillSuggestionList(suggestions: List<PlaceSuggestion>) {
        suggestionList.clear()
        suggestionList.addAll(suggestions)
        (searchAdapter as LekuSearchAdapter<SuggestionViewHolder, PlaceSuggestion>)
            ?.items = suggestionList.toList()
    }

    private fun closeKeyboard() {
        val view = this.currentFocus
        view?.let {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(it.windowToken, 0)
        }
    }

    private fun removeUnnamedRoad(str: String): String {
        return str.replace(UNNAMED_ROAD_WITH_COMMA, "")
            .replace(UNNAMED_ROAD_WITH_HYPHEN, "")
    }

    class Builder {
        private var locationLatitude: Double? = null
        private var locationLongitude: Double? = null
        private var searchZoneLocale: String? = null
        private var searchZoneRect: SearchZoneRect? = null
        private var customDataSource: GeocoderDataSourceInterface? = null
        private var customAdapter: LekuSearchAdapter<*, *>? = null
        private var searchZoneDefaultLocale = false
        private var layoutsToHide = ""
        private var enableSatelliteView = true
        private var shouldReturnOkOnBackPressed = false
        private var lekuPois: List<LekuPoi>? = null
        private var geolocApiKey: String? = null
        private var googlePlacesApiKey: String? = null
        private var googleTimeZoneEnabled = false
        private var googlePlaceEnabled = false
        private var voiceSearchEnabled = true
        private var mapStyle: Int? = null
        private var unnamedRoadVisible = true
        private var isLegacyLayoutEnabled = false
        private var isSearchBarHidden = false
        private var currentLocationBitmapMaker: BitmapDescriptor = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
        private var otherLocationBitmapMaker: BitmapDescriptor = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)

        fun setCurrentLocation(currentLocation: BitmapDescriptor): Builder {
            this.currentLocationBitmapMaker = currentLocation
            return this
        }

        fun setOtherLocation(otherLocation: BitmapDescriptor): Builder {
            this.otherLocationBitmapMaker = otherLocation
            return this
        }

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

        fun withDataSource(customDataSource: GeocoderDataSourceInterface?): Builder {
            if (customDataSource != null) {
                this.customDataSource = customDataSource
            }
            return this
        }

        fun withAdapter(customAdapter: LekuSearchAdapter<*, *>?): Builder {
            this.customAdapter = customAdapter
            return this
        }

        fun withGooglePlacesEnabled(): Builder {
            this.googlePlaceEnabled = true
            return this
        }

        fun withSearchZone(localeZone: String): Builder {
            this.searchZoneLocale = localeZone
            return this
        }

        fun withSearchZone(zoneRect: SearchZoneRect): Builder {
            this.searchZoneRect = zoneRect
            return this
        }

        fun withDefaultLocaleSearchZone(): Builder {
            this.searchZoneDefaultLocale = true
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

        fun withGooglePlacesApiKey(apiKey: String): Builder {
            this.googlePlacesApiKey = apiKey
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

        fun withUnnamedRoadHidden(): Builder {
            this.unnamedRoadVisible = false
            return this
        }

        fun withMapStyle(@RawRes mapStyle: Int): Builder {
            this.mapStyle = mapStyle
            return this
        }

        fun withLegacyLayout(): Builder {
            this.isLegacyLayoutEnabled = true
            return this
        }

        fun withSearchBarHidden(): Builder {
            this.isSearchBarHidden = true
            return this
        }

        fun build(context: Context): Intent {
            val intent = Intent(context, LocationPickerActivity::class.java)

            locationLatitude?.let {
                intent.putExtra(LATITUDE, it)
            }
            locationLongitude?.let {
                intent.putExtra(LONGITUDE, it)
            }
            searchZoneLocale?.let {
                intent.putExtra(SEARCH_ZONE, it)
            }
            searchZoneRect?.let {
                intent.putExtra(SEARCH_ZONE_RECT, searchZoneRect)
            }
            intent.putExtra(SEARCH_ZONE_DEFAULT_LOCALE, searchZoneDefaultLocale)
            if (layoutsToHide.isNotEmpty()) {
                intent.putExtra(LAYOUTS_TO_HIDE, layoutsToHide)
            }
            intent.putExtra(BACK_PRESSED_RETURN_OK, shouldReturnOkOnBackPressed)
            intent.putExtra(ENABLE_SATELLITE_VIEW, enableSatelliteView)
            lekuPois?.let {
                if (it.isNotEmpty()) {
                    intent.putExtra(POIS_LIST, ArrayList(it))
                }
            }
            geolocApiKey?.let {
                intent.putExtra(GEOLOC_API_KEY, geolocApiKey)
            }
            googlePlacesApiKey?.let {
                intent.putExtra(PLACES_API_KEY, googlePlacesApiKey)
            }
            mapStyle?.let { style -> intent.putExtra(MAP_STYLE, style) }
            intent.putExtra(ENABLE_GOOGLE_TIME_ZONE, googleTimeZoneEnabled)
            intent.putExtra(ENABLE_VOICE_SEARCH, voiceSearchEnabled)
            intent.putExtra(UNNAMED_ROAD_VISIBILITY, unnamedRoadVisible)
            intent.putExtra(WITH_LEGACY_LAYOUT, isLegacyLayoutEnabled)
            intent.putExtra(SEARCH_BAR_HIDDEN, isSearchBarHidden)
            intent.putExtra(ENABLE_GOOGLE_PLACES, googlePlaceEnabled)

            LocationPickerActivity.customDataSource = customDataSource
            LocationPickerActivity.customAdapter = customAdapter
            LocationPickerActivity.currentLocationBitmapMaker = currentLocationBitmapMaker
            LocationPickerActivity.otherLocationBitmapMaker = otherLocationBitmapMaker
            return intent
        }
    }
}
