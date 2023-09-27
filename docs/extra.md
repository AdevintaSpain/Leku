If you would like to use the Geocoder presenter (MVP) used for this use case you are free to use it!
GeocoderPresenter has three methods:

* ***getLastKnownLocation:***
Which obviously returns the last known user location as a ***Location*** object.

* ***getFromLocationName(String query):***
Returns a ***List`<`Address`>`*** for the text introduced.

* ***getFromLocationName(String query, LatLng lowerLeft, LatLng upperRight):***
Returns a ***List`<`Address`>`*** for the text and the Rectangle introduced.

* ***getDebouncedFromLocationName(String query, int debounceTime):***
Returns a ***List`<`Address`>`*** for the text introduced. Useful if you want to implement your own search view with auto-complete.

* ***getDebouncedFromLocationName(String query, LatLng lowerLeft, LatLng upperRight, int debounceTime):***
Returns a ***List`<`Address`>`*** for the text and the Rectangle introduced. Useful if you want to implement your own search view with auto-complete.

* ***getInfoFromLocation(double latitude, double longitude):***
Returns a ***List`<`Address`>`*** based on a latitude and a longitude.


To use it first you need to implement the GeocoderViewInterface interface in your class like:

```kotlin
class LocationPickerActivity : AppCompatActivity(), GeocoderViewInterface {
```

Then you need to setup the presenter:

```kotlin
private val geocoderPresenter: GeocoderPresenter

override fun onCreate(savedInstanceState: Bundle?) {
  super.onCreate(savedInstanceState)
  ***
  val placesDataSource = GooglePlacesDataSource(Places.getGeoDataClient(this, null))
  val geocoder = Geocoder(this, Locale.getDefault())
  apiInteractor = GoogleGeocoderDataSource(NetworkClient(), AddressBuilder())
  val geocoderRepository = GeocoderRepository(AndroidGeocoderDataSource(geocoder), apiInteractor!!)
  val timeZoneDataSource = GoogleTimeZoneDataSource(
          GeoApiContext.Builder().apiKey(GoogleTimeZoneDataSource.getApiKey(this)).build())
  geocoderPresenter = GeocoderPresenter(
          ReactiveLocationProvider(applicationContext), geocoderRepository, placesDataSource, timeZoneDataSource)
  geocoderPresenter?.setUI(this)
  ***
}
```

And besides filling the interface methods you have to add some things to your activity/fragment lifecycle to ensure that there are no leaks.

```kotlin
override fun onStart() {
    super.onStart()
    geocoderPresenter?.setUI(this)
}

override fun onStop() {
    geocoderPresenter?.stop()
    super.onStop()
}
```

#### Transition Bundle

If you need to send and receive a param through the LocationPickerActivity you can do it.
You only need to add an "Extra" param to the intent like:

```kotlin
locationPickerIntent.putExtra("test", "this is a test")
```

And parse it on onActivityResult callback:

```kotlin
val bundle = data.getBundleExtra(LocationPickerActivity.TRANSITION_BUNDLE)
val test = bundle.getString("test")
```

#### Google Places

Leku now supports Google Places queries using the search box. If you want to enable it these are the steps you need to follow:

1. You need to replace your old `com.google.android.maps.v2.API_KEY` meta-data for the `com.google.android.geo.API_KEY`

```xml
<!-- Use this if only using Maps and not Places
    <meta-data
        android:name="com.google.android.maps.v2.API_KEY"
        android:value="@string/google_maps_key"
        />
-->

    <meta-data
        android:name="com.google.android.geo.API_KEY"
        android:value="@string/google_maps_key"/>
```

2. Enable Google Places API for Android on your [google developer console](https://console.developers.google.com/).

3. Enable it when instantiating LocationPickerActivity by adding `.withGooglePlacesEnabled()`:

```kotlin
val locationPickerIntent = LocationPickerActivity.Builder(context)
    **.withGooglePlacesEnabled()**
    .build()
```

And you are good to go. :)


#### Geocoding API Fallback

In few cases, the geocoding service from Android fails due to an issue with the NetworkLocator. The only way of fixing this is rebooting the device.

In order to cover these cases, you can instruct Leku to use the Geocoding API. To enable it, just use the method '''withGeolocApiKey''' when invoking the LocationPicker.

You should provide your Server Key as parameter. Keep in mind that the free tier only allows 2,500 requests per day. You can track how many times is it used in the Developer Console from Google.


##### Tests

Note: If you need to execute the Espresso test you will need to add the Google Maps Key into the Tests AndroidManifest.xml


Now you have all you need. :)


##### Important

Searching using the "SearchView" (geocoder) will be restricted to a zone if you are with a Locale from: US, UK, France, Italy and Spain. If not, the search will return results from all the world.


Sample usage
------------

We provide a sample project which provides runnable code samples that demonstrate their use in Android applications.
Note that you need to include your Google Play services key in the sample to be able to test it.
