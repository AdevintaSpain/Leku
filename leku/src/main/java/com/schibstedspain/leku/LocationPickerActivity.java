package com.schibstedspain.leku;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.schibstedspain.leku.geocoder.GeocoderAPIInteractor;
import com.schibstedspain.leku.geocoder.GeocoderInteractor;
import com.schibstedspain.leku.geocoder.GeocoderPresenter;
import com.schibstedspain.leku.geocoder.GeocoderViewInterface;
import com.schibstedspain.leku.geocoder.api.AddressBuilder;
import com.schibstedspain.leku.geocoder.api.NetworkClient;
import com.schibstedspain.leku.permissions.PermissionUtils;
import com.schibstedspain.leku.tracker.TrackEvents;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import pl.charmas.android.reactivelocation.ReactiveLocationProvider;

import static com.google.android.gms.maps.GoogleMap.MAP_TYPE_NORMAL;
import static com.google.android.gms.maps.GoogleMap.MAP_TYPE_SATELLITE;

public class LocationPickerActivity extends AppCompatActivity
    implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener, LocationListener, GoogleMap.OnMapLongClickListener,
    GeocoderViewInterface, GoogleMap.OnMapClickListener {

  public static final String LATITUDE = "locationLatitude";
  public static final String LONGITUDE = "locationLongitude";
  public static final String ZIPCODE = "zipcode";
  public static final String ADDRESS = "address";
  public static final String LOCATION_ADDRESS = "location_address";
  public static final String TRANSITION_BUNDLE = "transition_bundle";
  public static final String LAYOUTS_TO_HIDE = "layouts_to_hide";
  public static final String SEARCH_ZONE = "search_zone";
  public static final String BACK_PRESSED_RETURN_OK = "back_pressed_return_ok";
  public static final String ENABLE_SATELLITE_VIEW = "enable_satellite_view";
  public static final String ENABLE_LOCATION_PERMISSION_REQUEST = "enable_location_permission_request";
  public static final String POIS_LIST = "pois_list";
  public static final String LEKU_POI = "leku_poi";
  private static final String GEOLOC_API_KEY = "geoloc_api_key";
  private static final String LOCATION_KEY = "location_key";
  private static final String LAST_LOCATION_QUERY = "last_location_query";
  private static final String OPTIONS_HIDE_STREET = "street";
  private static final String OPTIONS_HIDE_CITY = "city";
  private static final String OPTIONS_HIDE_ZIPCODE = "zipcode";
  private static final int REQUEST_PLACE_PICKER = 6655;
  private static final int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
  private static final int DEFAULT_ZOOM = 16;
  private static final int WIDER_ZOOM = 6;
  private static final int MIN_CHARACTERS = 2;

  private GoogleMap map;
  private GoogleApiClient googleApiClient;
  private Location currentLocation;
  private LekuPoi currentLekuPoi;
  private GeocoderPresenter geocoderPresenter;

  private ArrayAdapter<String> adapter;
  private EditText searchView;
  private TextView street;
  private TextView coordinates;
  private TextView longitude;
  private TextView latitude;
  private TextView city;
  private TextView zipCode;
  private FrameLayout locationInfoLayout;
  private ProgressBar progressBar;
  private ListView listResult;
  private ImageView clearSearchButton;
  private MenuItem searchOption;

  private final List<Address> locationList = new ArrayList<>();
  private List<String> locationNameList = new ArrayList<>();
  private boolean hasWiderZoom = false;
  private Bundle bundle = new Bundle();
  private Address selectedAddress;
  private boolean isLocationInformedFromBundle = false;
  private boolean isStreetVisible = true;
  private boolean isCityVisible = true;
  private boolean isZipCodeVisible = true;
  private boolean shouldReturnOkOnBackPressed = false;
  private boolean enableSatelliteView = true;
  private boolean enableLocationPermissionRequest = true;
  private String searchZone;
  private List<LekuPoi> poisList;
  private Map<String, LekuPoi> lekuPoisMarkersMap;
  private Marker currentMarker;
  private TextWatcher textWatcher;
  private String apiKey;
  private GeocoderAPIInteractor apiInteractor;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_location_picker);
    setUpMainVariables();
    setUpResultsList();
    setUpToolBar();
    updateValuesFromBundle(savedInstanceState);
    checkLocationPermission();
    setUpSearchView();
    setUpMapIfNeeded();
    setUpFloatingButtons();
    buildGoogleApiClient();
    track(TrackEvents.didLoadLocationPicker);
  }

  private void checkLocationPermission() {
    if (enableLocationPermissionRequest && PermissionUtils.shouldRequestLocationStoragePermission(getApplicationContext())) {
      PermissionUtils.requestLocationPermission(this);
    }
  }

  protected void track(TrackEvents event) {
    LocationPicker.getTracker().onEventTracked(event);
  }

  private void setUpMainVariables() {
    Geocoder geocoder = new Geocoder(this, Locale.getDefault());
    apiInteractor = new GeocoderAPIInteractor(new NetworkClient(), new AddressBuilder());
    geocoderPresenter = new GeocoderPresenter(new ReactiveLocationProvider(getApplicationContext()),
        new GeocoderInteractor(geocoder), apiInteractor);
    geocoderPresenter.setUI(this);
    progressBar = (ProgressBar) findViewById(R.id.loading_progress_bar);
    progressBar.setVisibility(View.GONE);
    locationInfoLayout = (FrameLayout) findViewById(R.id.location_info);
    longitude = (TextView) findViewById(R.id.longitude);
    latitude = (TextView) findViewById(R.id.latitude);
    street = (TextView) findViewById(R.id.street);
    coordinates = (TextView) findViewById(R.id.coordinates);
    city = (TextView) findViewById(R.id.city);
    zipCode = (TextView) findViewById(R.id.zipCode);
    clearSearchButton = (ImageView) findViewById(R.id.leku_clear_search_image);
    clearSearchButton.setOnClickListener(view -> {
      if (searchView != null) {
        searchView.setText("");
      }
    });
    locationNameList = new ArrayList<>();
  }

  private void setUpResultsList() {
    listResult = (ListView) findViewById(R.id.resultlist);
    adapter =
        new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, locationNameList);
    listResult.setAdapter(adapter);
    listResult.setOnItemClickListener((adapterView, view, i, l) -> {
      setNewLocation(locationList.get(i));
      changeListResultVisibility(View.GONE);
    });
  }

  private void setUpToolBar() {
    Toolbar toolbar = (Toolbar) findViewById(R.id.map_search_toolbar);
    setSupportActionBar(toolbar);
    if (getSupportActionBar() != null) {
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
      getSupportActionBar().setDisplayShowTitleEnabled(false);
    }
  }

  private void setUpSearchView() {
    searchView = (EditText) findViewById(R.id.leku_search);
    searchView.setOnEditorActionListener((v, actionId, event) -> {
      boolean handled = false;
      if (actionId == EditorInfo.IME_ACTION_SEARCH) {
        retrieveLocationFrom(v.getText().toString());
        closeKeyboard();
        handled = true;
      }
      return handled;
    });
    textWatcher = getSearchTextWatcher();
    searchView.addTextChangedListener(textWatcher);
  }

  private TextWatcher getSearchTextWatcher() {
    return new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

      }

      @Override
      public void onTextChanged(CharSequence charSequence, int start, int count, int after) {
        if ("".equals(charSequence.toString())) {
          adapter.clear();
          adapter.notifyDataSetChanged();
          showLocationInfoLayout();
          if (clearSearchButton != null) {
            clearSearchButton.setVisibility(View.INVISIBLE);
          }
          if (searchOption != null) {
            searchOption.setIcon(R.drawable.ic_mic);
          }
        } else {
          if (charSequence.length() > MIN_CHARACTERS && after > count) {
            retrieveLocationFrom(charSequence.toString(), 400);
          }
          if (clearSearchButton != null) {
            clearSearchButton.setVisibility(View.VISIBLE);
          }
          if (searchOption != null) {
            searchOption.setIcon(R.drawable.ic_search);
          }
        }
      }

      @Override
      public void afterTextChanged(Editable editable) {

      }
    };
  }

  private void setUpFloatingButtons() {
    FloatingActionButton btnMyLocation = (FloatingActionButton) findViewById(R.id.btnFloatingAction);
    btnMyLocation.setOnClickListener(v -> {
      geocoderPresenter.getLastKnownLocation();
      track(TrackEvents.didLocalizeMe);
    });
    FloatingActionButton btnAcceptLocation = (FloatingActionButton) findViewById(R.id.btnAccept);
    btnAcceptLocation.setOnClickListener(v -> returnCurrentPosition());

    FloatingActionButton btnSatellite = (FloatingActionButton) findViewById(R.id.btnSatellite);
    btnSatellite.setOnClickListener(view -> {
      map.setMapType(map.getMapType() == MAP_TYPE_SATELLITE ? MAP_TYPE_NORMAL : MAP_TYPE_SATELLITE);
      btnSatellite.setImageResource(map.getMapType() == MAP_TYPE_SATELLITE ? R.drawable.ic_satellite_off : R.drawable.ic_satellite_on);
    });
    btnSatellite.setVisibility(enableSatelliteView ? View.VISIBLE : View.GONE);
  }

  private void updateValuesFromBundle(Bundle savedInstanceState) {
    Bundle transitionBundle = getIntent().getExtras();
    if (transitionBundle != null) {
      getTransitionBundleParams(transitionBundle);
    }
    if (savedInstanceState != null) {
      getSavedInstanceParams(savedInstanceState);
    }
    updateAddressLayoutVisibility();
  }

  private void setUpMapIfNeeded() {
    if (map == null) {
      ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMapAsync(this);
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.toolbar_menu, menu);
    searchOption = menu.findItem(R.id.action_voice);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();
    if (id == android.R.id.home) {
      onBackPressed();
      return true;
    } else if (id == R.id.action_voice) {
      if (searchView.getText().toString().isEmpty()) {
        startVoiceRecognitionActivity();
      } else {
        retrieveLocationFrom(searchView.getText().toString());
        closeKeyboard();
      }
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    if (PermissionUtils.isLocationPermissionGranted(getApplicationContext())) {
      geocoderPresenter.getLastKnownLocation();
    }
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    switch (requestCode) {
      case REQUEST_PLACE_PICKER:
        if (resultCode == Activity.RESULT_OK) {
          ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
          searchView = (EditText) findViewById(R.id.leku_search);
          retrieveLocationFrom(matches.get(0));
        }
        break;
      default:
        break;
    }
    super.onActivityResult(requestCode, resultCode, data);
  }

  @Override
  protected void onStart() {
    super.onStart();
    googleApiClient.connect();
    geocoderPresenter.setUI(this);
  }

  @Override
  protected void onStop() {
    if (googleApiClient.isConnected()) {
      googleApiClient.disconnect();
    }
    geocoderPresenter.stop();
    super.onStop();
  }

  @Override
  protected void onResume() {
    super.onResume();
    setUpMapIfNeeded();
  }

  @Override
  protected void onDestroy() {
    if (searchView != null && textWatcher != null) {
      searchView.removeTextChangedListener(textWatcher);
    }
    if (googleApiClient != null) {
      googleApiClient.unregisterConnectionCallbacks(this);
    }
    super.onDestroy();
  }

  @Override
  public void onBackPressed() {
    if (!shouldReturnOkOnBackPressed || isLocationInformedFromBundle) {
      setResult(RESULT_CANCELED);
      track(TrackEvents.CANCEL);
      finish();
    } else {
      returnCurrentPosition();
    }
  }

  @Override
  public void onMapReady(GoogleMap googleMap) {
    if (map == null) {
      map = googleMap;
      setDefaultMapSettings();
      setCurrentPositionLocation();
      setPois();
    }
  }

  @Override
  public void onConnected(Bundle savedBundle) {
    if (currentLocation == null) {
      geocoderPresenter.getLastKnownLocation();
    }
  }

  @Override
  public void onConnectionSuspended(int i) {
    googleApiClient.connect();
  }

  @Override
  public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    if (connectionResult.hasResolution()) {
      try {
        connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
      } catch (IntentSender.SendIntentException e) {
        track(TrackEvents.googleApiConnectionFailed);
      }
    }
  }

  @Override
  public void onLocationChanged(Location location) {
    currentLocation = location;
  }

  @Override
  public void onSaveInstanceState(Bundle savedInstanceState) {
    if (currentLocation != null) {
      savedInstanceState.putParcelable(LOCATION_KEY, currentLocation);
    }
    if (searchView != null) {
      savedInstanceState.putString(LAST_LOCATION_QUERY, String.valueOf(searchView.getText()));
    }
    if (bundle.containsKey(TRANSITION_BUNDLE)) {
      savedInstanceState.putBundle(TRANSITION_BUNDLE, bundle.getBundle(TRANSITION_BUNDLE));
    }
    if (poisList != null) {
      savedInstanceState.putParcelableArrayList(POIS_LIST, new ArrayList<>(poisList));
    }
    savedInstanceState.putBoolean(ENABLE_SATELLITE_VIEW, enableSatelliteView);
    savedInstanceState.putBoolean(ENABLE_LOCATION_PERMISSION_REQUEST, enableLocationPermissionRequest);
    super.onSaveInstanceState(savedInstanceState);
  }

  @Override
  public void onRestoreInstanceState(Bundle savedInstanceState) {
    super.onRestoreInstanceState(savedInstanceState);
    String lastQuery = savedInstanceState.getString(LAST_LOCATION_QUERY, "");
    if (!"".equals(lastQuery)) {
      retrieveLocationFrom(lastQuery);
    }
    currentLocation = savedInstanceState.getParcelable(LOCATION_KEY);
    if (currentLocation != null) {
      setCurrentPositionLocation();
    }
    if (savedInstanceState.containsKey(TRANSITION_BUNDLE)) {
      bundle.putBundle(TRANSITION_BUNDLE, savedInstanceState.getBundle(TRANSITION_BUNDLE));
    }
    if (savedInstanceState.containsKey(POIS_LIST)) {
      poisList = savedInstanceState.getParcelableArrayList(POIS_LIST);
    }
    if (savedInstanceState.containsKey(ENABLE_SATELLITE_VIEW)) {
      enableSatelliteView = savedInstanceState.getBoolean(ENABLE_SATELLITE_VIEW);
    }
    if (savedInstanceState.containsKey(ENABLE_LOCATION_PERMISSION_REQUEST)) {
      enableLocationPermissionRequest = savedInstanceState.getBoolean(ENABLE_LOCATION_PERMISSION_REQUEST);
    }
  }

  @Override
  public void onMapLongClick(LatLng latLng) {
    currentLekuPoi = null;
    setNewPosition(latLng);
    track(TrackEvents.didLocalizeByPoi);
  }

  @Override
  public void onMapClick(LatLng latLng) {
    currentLekuPoi = null;
    setNewPosition(latLng);
    track(TrackEvents.simpleDidLocalizeByPoi);
  }

  private void setNewPosition(LatLng latLng) {
    if (currentLocation == null) {
      currentLocation = new Location(getString(R.string.network_resource));
    }
    currentLocation.setLatitude(latLng.latitude);
    currentLocation.setLongitude(latLng.longitude);
    setCurrentPositionLocation();
  }

  @Override
  public void willLoadLocation() {
    progressBar.setVisibility(View.VISIBLE);
    changeListResultVisibility(View.GONE);
  }

  @Override
  public void showLocations(List<Address> addresses) {
    if (addresses != null) {
      fillLocationList(addresses);
      if (addresses.isEmpty()) {
        Toast.makeText(getApplicationContext(), R.string.no_search_results, Toast.LENGTH_LONG)
            .show();
      } else {
        updateLocationNameList(addresses);
        if (hasWiderZoom) {
          searchView.setText("");
        }
        if (addresses.size() == 1) {
          setNewLocation(addresses.get(0));
        }
        adapter.notifyDataSetChanged();
      }
    }
  }

  @Override
  public void showDebouncedLocations(List<Address> addresses) {
    if (addresses != null) {
      fillLocationList(addresses);
      if (!addresses.isEmpty()) {
        updateLocationNameList(addresses);
        adapter.notifyDataSetChanged();
      }
    }
  }

  @Override
  public void didLoadLocation() {
    progressBar.setVisibility(View.GONE);

    changeListResultVisibility(locationList.size() > 1 ? View.VISIBLE : View.GONE);

    if (locationList.size() == 1 && locationList.get(0).getMaxAddressLineIndex() > 0) {
      changeLocationInfoLayoutVisibility(View.VISIBLE);
    } else {
      changeLocationInfoLayoutVisibility(View.GONE);
    }
    track(TrackEvents.didSearchLocations);
  }

  private void changeListResultVisibility(int visibility) {
    listResult.setVisibility(visibility);
  }

  private void changeLocationInfoLayoutVisibility(int visibility) {
    locationInfoLayout.setVisibility(visibility);
  }

  private void showCoordinatesLayout() {
    longitude.setVisibility(View.VISIBLE);
    latitude.setVisibility(View.VISIBLE);
    coordinates.setVisibility(View.VISIBLE);
    street.setVisibility(View.GONE);
    city.setVisibility(View.GONE);
    zipCode.setVisibility(View.GONE);
    changeLocationInfoLayoutVisibility(View.VISIBLE);
  }

  private void showAddressLayout() {
    longitude.setVisibility(View.GONE);
    latitude.setVisibility(View.GONE);
    coordinates.setVisibility(View.GONE);
    street.setVisibility(View.VISIBLE);
    city.setVisibility(View.VISIBLE);
    zipCode.setVisibility(View.VISIBLE);
    changeLocationInfoLayoutVisibility(View.VISIBLE);
  }

  private void updateAddressLayoutVisibility() {
    street.setVisibility(isStreetVisible ? View.VISIBLE : View.INVISIBLE);
    city.setVisibility(isCityVisible ? View.VISIBLE : View.INVISIBLE);
    zipCode.setVisibility(isZipCodeVisible ? View.VISIBLE : View.INVISIBLE);
    longitude.setVisibility(View.VISIBLE);
    latitude.setVisibility(View.VISIBLE);
    coordinates.setVisibility(View.VISIBLE);
  }

  @Override
  public void showLoadLocationError() {
    progressBar.setVisibility(View.GONE);
    changeListResultVisibility(View.GONE);
    Toast.makeText(this, R.string.load_location_error, Toast.LENGTH_LONG).show();
  }

  @Override
  public void willGetLocationInfo(LatLng latLng) {
    changeLocationInfoLayoutVisibility(View.VISIBLE);
    setCoordinatesInfo(latLng);
  }

  @Override
  public void showLastLocation(Location location) {
    currentLocation = location;
  }

  @Override
  public void didGetLastLocation() {
    if (currentLocation != null) {
      if (!Geocoder.isPresent()) {
        Toast.makeText(this, R.string.no_geocoder_available, Toast.LENGTH_LONG).show();
        return;
      }
      setUpMapIfNeeded();
    }
    setUpDefaultMapLocation();
  }

  @Override
  public void showLocationInfo(List<Address> addresses) {
    if (addresses != null) {
      if (addresses.size() > 0 && addresses.get(0).getMaxAddressLineIndex() > 0) {
        selectedAddress = addresses.get(0);
        setLocationInfo(selectedAddress);
      } else {
        setLocationEmpty();
      }
    }
  }

  public void setLocationEmpty() {
    this.street.setText("");
    this.city.setText("");
    this.zipCode.setText("");
    changeLocationInfoLayoutVisibility(View.VISIBLE);
  }

  @Override
  public void didGetLocationInfo() {
    showLocationInfoLayout();
  }

  @Override
  public void showGetLocationInfoError() {
    setLocationEmpty();
  }

  private void showLocationInfoLayout() {
    changeLocationInfoLayoutVisibility(View.VISIBLE);
  }

  private void getSavedInstanceParams(Bundle savedInstanceState) {
    if (savedInstanceState.containsKey(TRANSITION_BUNDLE)) {
      bundle.putBundle(TRANSITION_BUNDLE, savedInstanceState.getBundle(TRANSITION_BUNDLE));
    } else {
      bundle.putBundle(TRANSITION_BUNDLE, savedInstanceState);
    }
    if (savedInstanceState.keySet().contains(LOCATION_KEY)) {
      currentLocation = savedInstanceState.getParcelable(LOCATION_KEY);
    }
    setUpDefaultMapLocation();
    if (savedInstanceState.keySet().contains(LAYOUTS_TO_HIDE)) {
      setLayoutVisibilityFromBundle(savedInstanceState);
    }
    if (savedInstanceState.keySet().contains(GEOLOC_API_KEY)) {
      apiInteractor.setApiKey(savedInstanceState.getString(GEOLOC_API_KEY));
    }
    if (savedInstanceState.keySet().contains(SEARCH_ZONE)) {
      searchZone = savedInstanceState.getString(SEARCH_ZONE);
    }
    if (savedInstanceState.keySet().contains(ENABLE_SATELLITE_VIEW)) {
      enableSatelliteView = savedInstanceState.getBoolean(ENABLE_SATELLITE_VIEW);
    }
    if (savedInstanceState.keySet().contains(POIS_LIST)) {
      poisList = savedInstanceState.getParcelableArrayList(POIS_LIST);
    }
    if (savedInstanceState.keySet().contains(ENABLE_LOCATION_PERMISSION_REQUEST)) {
      enableLocationPermissionRequest = savedInstanceState.getBoolean(ENABLE_LOCATION_PERMISSION_REQUEST);
    }
  }

  private void getTransitionBundleParams(Bundle transitionBundle) {
    bundle.putBundle(TRANSITION_BUNDLE, transitionBundle);
    if (transitionBundle.keySet().contains(LATITUDE) && transitionBundle.keySet()
        .contains(LONGITUDE)) {
      setLocationFromBundle(transitionBundle);
    }
    if (transitionBundle.keySet().contains(LAYOUTS_TO_HIDE)) {
      setLayoutVisibilityFromBundle(transitionBundle);
    }
    if (transitionBundle.keySet().contains(SEARCH_ZONE)) {
      searchZone = transitionBundle.getString(SEARCH_ZONE);
    }
    if (transitionBundle.keySet().contains(BACK_PRESSED_RETURN_OK)) {
      shouldReturnOkOnBackPressed = transitionBundle.getBoolean(BACK_PRESSED_RETURN_OK);
    }
    if (transitionBundle.keySet().contains(ENABLE_SATELLITE_VIEW)) {
      enableSatelliteView = transitionBundle.getBoolean(ENABLE_SATELLITE_VIEW);
    }
    if (transitionBundle.keySet().contains(ENABLE_LOCATION_PERMISSION_REQUEST)) {
      enableLocationPermissionRequest = transitionBundle.getBoolean(ENABLE_LOCATION_PERMISSION_REQUEST);
    }
    if (transitionBundle.keySet().contains(POIS_LIST)) {
      poisList = transitionBundle.getParcelableArrayList(POIS_LIST);
    }
    if (transitionBundle.keySet().contains(GEOLOC_API_KEY)) {
      apiInteractor.setApiKey(transitionBundle.getString(GEOLOC_API_KEY));
    }
  }

  private void setLayoutVisibilityFromBundle(Bundle transitionBundle) {
    String options = transitionBundle.getString(LAYOUTS_TO_HIDE);
    if (options != null && options.contains(OPTIONS_HIDE_STREET)) {
      isStreetVisible = false;
    }
    if (options != null && options.contains(OPTIONS_HIDE_CITY)) {
      isCityVisible = false;
    }
    if (options != null && options.contains(OPTIONS_HIDE_ZIPCODE)) {
      isZipCodeVisible = false;
    }
  }

  private void setLocationFromBundle(Bundle transitionBundle) {
    if (currentLocation == null) {
      currentLocation = new Location(getString(R.string.network_resource));
    }
    currentLocation.setLatitude(transitionBundle.getDouble(LATITUDE));
    currentLocation.setLongitude(transitionBundle.getDouble(LONGITUDE));
    setCurrentPositionLocation();
    isLocationInformedFromBundle = true;
  }

  private void startVoiceRecognitionActivity() {
    Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
    intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.voice_search_promp));
    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,
        getString(R.string.voice_search_extra_language));

    if (checkPlayServices()) {
      try {
        startActivityForResult(intent, REQUEST_PLACE_PICKER);
      } catch (ActivityNotFoundException e) {
        track(TrackEvents.startVoiceRecognitionActivityFailed);
      }
    }
  }

  private boolean checkPlayServices() {
    GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
    int result = googleAPI.isGooglePlayServicesAvailable(getApplicationContext());
    if (result != ConnectionResult.SUCCESS) {
      if (googleAPI.isUserResolvableError(result)) {
        googleAPI.getErrorDialog(this, result, CONNECTION_FAILURE_RESOLUTION_REQUEST).show();
      }
      return false;
    }
    return true;
  }

  private void setCoordinatesInfo(LatLng latLng) {
    this.latitude.setText(getString(R.string.latitude) + ": " + latLng.latitude);
    this.longitude.setText(getString(R.string.longitude) + ": " + latLng.longitude);
    showCoordinatesLayout();
  }

  private void setLocationInfo(Address address) {
    street.setText(address.getAddressLine(0));
    city.setText(isStreetEqualsCity(address) ? "" : address.getLocality());
    zipCode.setText(address.getPostalCode());
    showAddressLayout();
  }

  private void setLocationInfo(LekuPoi poi) {
    this.currentLekuPoi = poi;
    street.setText(poi.getTitle());
    city.setText(poi.getAddress());
    zipCode.setText(null);
    showAddressLayout();
  }

  private boolean isStreetEqualsCity(Address address) {
    return address.getAddressLine(0).equals(address.getLocality());
  }

  private void setNewMapMarker(LatLng latLng) {
    if (map != null) {
      if (currentMarker != null) {
        currentMarker.remove();
      }
      CameraPosition cameraPosition =
          new CameraPosition.Builder().target(latLng)
              .zoom(getDefaultZoom())
              .build();
      hasWiderZoom = false;
      map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
      currentMarker = addMarker(latLng);
      map.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
        @Override
        public void onMarkerDragStart(Marker marker) {

        }

        @Override
        public void onMarkerDrag(Marker marker) {
        }

        @Override
        public void onMarkerDragEnd(Marker marker) {
          if (currentLocation == null) {
            currentLocation = new Location(getString(R.string.network_resource));
          }
          currentLekuPoi = null;
          currentLocation.setLongitude(marker.getPosition().longitude);
          currentLocation.setLatitude(marker.getPosition().latitude);
          setCurrentPositionLocation();
        }
      });
    }
  }

  private int getDefaultZoom() {
    int zoom;
    if (hasWiderZoom) {
      zoom = WIDER_ZOOM;
    } else {
      zoom = DEFAULT_ZOOM;
    }
    return zoom;
  }

  private void retrieveLocationFrom(String query) {
    if (searchZone != null && !searchZone.isEmpty()) {
      retrieveLocationFromZone(query, searchZone);
    } else {
      retrieveLocationFromDefaultZone(query);
    }
  }

  private void retrieveLocationFrom(String query, int debounceTime) {
    if (searchZone != null && !searchZone.isEmpty()) {
      retrieveDebouncedLocationFromZone(query, searchZone, debounceTime);
    } else {
      retrieveDebouncedLocationFromDefaultZone(query, debounceTime);
    }
  }

  private void retrieveLocationFromDefaultZone(String query) {
    if (CountryLocaleRect.getDefaultLowerLeft() != null) {
      geocoderPresenter.getFromLocationName(query, CountryLocaleRect.getDefaultLowerLeft(),
          CountryLocaleRect.getDefaultUpperRight());
    } else {
      geocoderPresenter.getFromLocationName(query);
    }
  }

  private void retrieveLocationFromZone(String query, String zoneKey) {
    Locale locale = new Locale(zoneKey);
    if (CountryLocaleRect.getLowerLeftFromZone(locale) != null) {
      geocoderPresenter.getFromLocationName(query, CountryLocaleRect.getLowerLeftFromZone(locale),
          CountryLocaleRect.getUpperRightFromZone(locale));
    } else {
      geocoderPresenter.getFromLocationName(query);
    }
  }

  private void retrieveDebouncedLocationFromDefaultZone(String query, int debounceTime) {
    if (CountryLocaleRect.getDefaultLowerLeft() != null) {
      geocoderPresenter.getDebouncedFromLocationName(query, CountryLocaleRect.getDefaultLowerLeft(),
          CountryLocaleRect.getDefaultUpperRight(), debounceTime);
    } else {
      geocoderPresenter.getDebouncedFromLocationName(query, debounceTime);
    }
  }

  private void retrieveDebouncedLocationFromZone(String query, String zoneKey, int debounceTime) {
    Locale locale = new Locale(zoneKey);
    if (CountryLocaleRect.getLowerLeftFromZone(locale) != null) {
      geocoderPresenter.getDebouncedFromLocationName(query, CountryLocaleRect.getLowerLeftFromZone(locale),
          CountryLocaleRect.getUpperRightFromZone(locale), debounceTime);
    } else {
      geocoderPresenter.getDebouncedFromLocationName(query, debounceTime);
    }
  }

  private void returnCurrentPosition() {
    if (currentLekuPoi != null) {
      Intent returnIntent = new Intent();
      returnIntent.putExtra(LATITUDE, currentLekuPoi.getLocation().getLatitude());
      returnIntent.putExtra(LONGITUDE, currentLekuPoi.getLocation().getLongitude());
      if (street != null && city != null) {
        returnIntent.putExtra(LOCATION_ADDRESS, getLocationAddress());
      }
      returnIntent.putExtra(TRANSITION_BUNDLE, bundle.getBundle(TRANSITION_BUNDLE));
      returnIntent.putExtra(LEKU_POI, currentLekuPoi);
      setResult(RESULT_OK, returnIntent);
      track(TrackEvents.RESULT_OK);
    } else if (currentLocation != null) {
      Intent returnIntent = new Intent();
      returnIntent.putExtra(LATITUDE, currentLocation.getLatitude());
      returnIntent.putExtra(LONGITUDE, currentLocation.getLongitude());
      if (street != null && city != null) {
        returnIntent.putExtra(LOCATION_ADDRESS, getLocationAddress());
      }
      if (zipCode != null) {
        returnIntent.putExtra(ZIPCODE, zipCode.getText());
      }
      returnIntent.putExtra(ADDRESS, selectedAddress);
      returnIntent.putExtra(TRANSITION_BUNDLE, bundle.getBundle(TRANSITION_BUNDLE));
      setResult(RESULT_OK, returnIntent);
      track(TrackEvents.RESULT_OK);
    } else {
      setResult(RESULT_CANCELED);
      track(TrackEvents.CANCEL);
    }
    finish();
  }

  private String getLocationAddress() {
    String locationAddress = "";
    if (street != null && !street.getText().toString().isEmpty()) {
      locationAddress = street.getText().toString();
    }
    if (city != null && !city.getText().toString().isEmpty()) {
      if (!locationAddress.isEmpty()) {
        locationAddress += ", ";
      }
      locationAddress += city.getText().toString();
    }
    return locationAddress;
  }

  private void updateLocationNameList(List<Address> addresses) {
    locationNameList.clear();
    for (Address address : addresses) {
      if (address.getFeatureName() == null) {
        locationNameList.add(getString(R.string.unknown_location));
      } else {
        locationNameList.add(getFullAddressString(address));
      }
    }
  }

  private String getFullAddressString(Address address) {
    String fullAddress = "";
    if (address.getFeatureName() != null) {
      fullAddress += address.getFeatureName();
    }
    if (address.getSubLocality() != null && !address.getSubLocality().isEmpty()) {
      fullAddress += ", " + address.getSubLocality();
    }
    if (address.getLocality() != null && !address.getLocality().isEmpty()) {
      fullAddress += ", " + address.getLocality();
    }
    if (address.getCountryName() != null && !address.getCountryName().isEmpty()) {
      fullAddress += ", " + address.getCountryName();
    }
    return fullAddress;
  }

  private void setDefaultMapSettings() {
    if (map != null) {
      map.setMapType(MAP_TYPE_NORMAL);
      map.setOnMapLongClickListener(this);
      map.setOnMapClickListener(this);
      map.getUiSettings().setCompassEnabled(false);
      map.getUiSettings().setMyLocationButtonEnabled(true);
      map.getUiSettings().setMapToolbarEnabled(false);
    }
  }

  private void setUpDefaultMapLocation() {
    if (currentLocation != null) {
      setCurrentPositionLocation();
    } else {
      searchView = (EditText) findViewById(R.id.leku_search);
      retrieveLocationFrom(Locale.getDefault().getDisplayCountry());
      hasWiderZoom = true;
    }
  }

  private void setCurrentPositionLocation() {
    if (currentLocation != null) {
      setNewMapMarker(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()));
      geocoderPresenter.getInfoFromLocation(new LatLng(currentLocation.getLatitude(),
          currentLocation.getLongitude()));
    }
  }

  private void setPois() {
    if (poisList != null && !poisList.isEmpty()) {
      lekuPoisMarkersMap = new HashMap<>();
      for (LekuPoi lekuPoi : poisList) {
        Location location = lekuPoi.getLocation();
        if (location != null && lekuPoi.getTitle() != null) {
          Marker marker = addPoiMarker(new LatLng(location.getLatitude(), location.getLongitude()),
              lekuPoi.getTitle(), lekuPoi.getAddress());
          lekuPoisMarkersMap.put(marker.getId(), lekuPoi);
        }
      }

      map.setOnMarkerClickListener(marker -> {
        LekuPoi lekuPoi = lekuPoisMarkersMap.get(marker.getId());
        if (lekuPoi != null) {
          setLocationInfo(lekuPoi);
          centerToPoi(lekuPoi);
          track(TrackEvents.simpleDidLocalizeByLekuPoi);
        }
        return true;
      });
    }
  }

  private void centerToPoi(LekuPoi lekuPoi) {
    if (map != null) {
      Location location = lekuPoi.getLocation();
      CameraPosition cameraPosition = new CameraPosition.Builder()
          .target(new LatLng(location.getLatitude(), location.getLongitude())).zoom(getDefaultZoom()).build();
      hasWiderZoom = false;
      map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }
  }

  private synchronized void buildGoogleApiClient() {
    googleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this)
        .addOnConnectionFailedListener(this)
        .addApi(LocationServices.API)
        .build();
    googleApiClient.connect();
  }

  private Marker addMarker(LatLng latLng) {
    return map.addMarker(new MarkerOptions().position(latLng).draggable(true));
  }

  private Marker addPoiMarker(LatLng latLng, String title, String address) {
    return map.addMarker(new MarkerOptions().position(latLng)
        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
        .title(title)
        .snippet(address));
  }

  private void setNewLocation(Address address) {
    this.selectedAddress = address;
    if (currentLocation == null) {
      currentLocation = new Location(getString(R.string.network_resource));
    }

    currentLocation.setLatitude(address.getLatitude());
    currentLocation.setLongitude(address.getLongitude());
    setNewMapMarker(new LatLng(address.getLatitude(), address.getLongitude()));
    setLocationInfo(address);
    searchView.setText("");
  }

  private void fillLocationList(List<Address> addresses) {
    locationList.clear();
    for (Address address : addresses) {
      locationList.add(address);
    }
  }

  private void closeKeyboard() {
    View view = this.getCurrentFocus();
    if (view != null) {
      InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
      imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
  }

  public static class Builder {
    private Double locationLatitude;
    private Double locationLongitude;
    private String locationSearchZone;
    private String layoutsToHide = "";
    private boolean enableSatelliteView = true;
    private boolean shouldReturnOkOnBackPressed = false;
    private List<LekuPoi> lekuPois;
    private String geolocApiKey = null;

    public Builder() {
    }

    public Builder withLocation(double latitude, double longitude) {
      this.locationLatitude = latitude;
      this.locationLongitude = longitude;
      return this;
    }

    public Builder withLocation(LatLng latLng) {
      if (latLng != null) {
        this.locationLatitude = latLng.latitude;
        this.locationLongitude = latLng.longitude;
      }
      return this;
    }

    public Builder withSearchZone(String searchZone) {
      this.locationSearchZone = searchZone;
      return this;
    }

    public Builder withSatelliteViewHidden() {
      this.enableSatelliteView = false;
      return this;
    }

    public Builder shouldReturnOkOnBackPressed() {
      this.shouldReturnOkOnBackPressed = true;
      return this;
    }

    public Builder withStreetHidden() {
      this.layoutsToHide = String.format("%s|%s", layoutsToHide, OPTIONS_HIDE_STREET);
      return this;
    }

    public Builder withCityHidden() {
      this.layoutsToHide = String.format("%s|%s", layoutsToHide, OPTIONS_HIDE_CITY);
      return this;
    }

    public Builder withZipCodeHidden() {
      this.layoutsToHide = String.format("%s|%s", layoutsToHide, OPTIONS_HIDE_ZIPCODE);
      return this;
    }

    public Builder withPois(List<LekuPoi> pois) {
      this.lekuPois = pois;
      return this;
    }

    public Builder withGeolocApiKey(String apiKey) {
      this.geolocApiKey = apiKey;
      return this;
    }

    public Intent build(Context context) {
      Intent intent = new Intent(context, LocationPickerActivity.class);

      if (locationLatitude != null) {
        intent.putExtra(LATITUDE, locationLatitude);
      }
      if (locationLongitude != null) {
        intent.putExtra(LONGITUDE, locationLongitude);
      }
      if (locationSearchZone != null) {
        intent.putExtra(SEARCH_ZONE, locationSearchZone);
      }
      if (!layoutsToHide.isEmpty()) {
        intent.putExtra(LAYOUTS_TO_HIDE, layoutsToHide);
      }
      intent.putExtra(BACK_PRESSED_RETURN_OK, shouldReturnOkOnBackPressed);
      intent.putExtra(ENABLE_SATELLITE_VIEW, enableSatelliteView);
      if (lekuPois != null && !lekuPois.isEmpty()) {
        intent.putExtra(POIS_LIST, new ArrayList<>(lekuPois));
      }
      if (geolocApiKey != null) {
        intent.putExtra(GEOLOC_API_KEY, geolocApiKey);
      }

      return intent;
    }
  }
}
