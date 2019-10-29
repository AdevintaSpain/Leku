### Prerequisites

minSdkVersion >= 15<br/>
Google Play Services = 16.0.0<br/>
AndroidX

### Download

Include the **jcenter** repository in your top `build.gradle`:
> Enabled by default on AndroidStudio projects
```groovy
allprojects {
    jcenter()
}
```

Include the dependency in your app `build.gradle`:

```groovy
dependencies {
    implementation 'com.schibstedspain.android:leku:6.4.0'
}
```

Alternatively, if you are using a different version of Google Play Services and AndroidX use this instead:

```groovy
implementation ('com.schibstedspain.android:leku:6.4.0') {
    exclude group: 'com.google.android.gms'
    exclude group: 'androidx.appcompat'
}
```

Note that Places on Google Play services is deprecated and this library currently uses the Compat Places SDK, that is a transition version.


For the <b>legacy versions of Leku</b> that does not use AndroidX and want to use the latest Places SDK, you could use it in this way:

```groovy
implementation ("com.google.android.libraries.places:places-compat:1.0.0")
implementation ("com.schibstedspain.android:leku:5.0.0") {
    exclude group: 'com.google.android.gms'
    exclude module: "play-services-places"
}
```

##### Troubleshoot

If you find this issue:

> Execution failed for task ':app:transformClassesWithMultidexlistForDebug'.
> com.android.build.api.transform.TransformException: Error while generating the main dex list:
>  Error while merging dex archives:
>  Program type already present: com.google.common.util.concurrent.ListenableFuture
>  Learn how to resolve the issue at https://developer.android.com/studio/build/dependencies#duplicate_classes.

The workaround for this is:

```groovy
// Add this to your app build.gradle file
configurations.all {
	// this is a workaround for the issue:
	// https://stackoverflow.com/questions/52521302/how-to-solve-program-type-already-present-com-google-common-util-concurrent-lis
	exclude group: 'com.google.guava', module: 'listenablefuture'
}
```


### Permissions

You must add the following permissions in order to use the Google Maps Android API:

* **android.permission.INTERNET**   Used by the API to download map tiles from Google Maps servers.

* **android.permission.ACCESS_NETWORK_STATE**   Allows the API to check the connection status in order to determine whether data can be downloaded.

The following permissions are not required to use Google Maps Android API v2, but are recommended.

* **android.permission.ACCESS_COARSE_LOCATION**   Allows the API to use WiFi or mobile cell data (or both) to determine the device's location. The API returns the location with an accuracy approximately equivalent to a city block.

* **android.permission.ACCESS_FINE_LOCATION**   Allows the API to determine as precise a location as possible from the available location providers, including the Global Positioning System (GPS) as well as WiFi and mobile cell data.

* **android.permission.WRITE_EXTERNAL_STORAGE**   Allows the API to cache map tile data in the device's external storage area.


```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />

<uses-feature android:name="android.hardware.location.network" android:required="false" />
<uses-feature android:name="android.hardware.location.gps" android:required="false"  />
```

You must also explicitly declare that your app uses the android.hardware.location.network or android.hardware.location.gps hardware features if your app targets Android 5.0 (API level 21) or higher and uses the ACCESS_COARSE_LOCATION or ACCESS_FINE_LOCATION permission in order to receive location updates from the network or a GPS, respectively.

**Note**: It supports runtime permissions for *Android 6 (Marshmallow)*. You don't need to do anything, it will ask for permissions if needed.


### Usage

To use the LocationPickerActivity first you need to add these lines to your AndroidManifest file:

```xml
<activity
    android:name="com.schibstedspain.leku.LocationPickerActivity"
    android:label="@string/leku_title_activity_location_picker"
    android:theme="@style/Theme.AppCompat.Light.NoActionBar"
    android:windowSoftInputMode="adjustPan"
    android:parentActivityName=".MainActivity">
    <intent-filter>
        <action android:name="android.intent.action.SEARCH" />
    </intent-filter>
    <meta-data android:name="android.app.searchable"
        android:resource="@xml/leku_searchable" />
    <meta-data
        android:name="android.support.PARENT_ACTIVITY"
        android:value=".MainActivity" />
</activity>
```

Then you have setup the call to start this activity wherever you like, always as startActivityForResult.
You can set a default location, search zone and other customizable parameters to load when you start the activity.
You only need to use the Builder setters like:

```kotlin
val locationPickerIntent = LocationPickerActivity.Builder()
    .withLocation(41.4036299, 2.1743558)
    .withGeolocApiKey("<PUT API KEY HERE>")
    .withSearchZone("es_ES")
    .withSearchZone(SearchZoneRect(LatLng(26.525467, -18.910366), LatLng(43.906271, 5.394197)))
    .withDefaultLocaleSearchZone()
    .shouldReturnOkOnBackPressed()
    .withStreetHidden()
    .withCityHidden()
    .withZipCodeHidden()
    .withSatelliteViewHidden()
    .withGooglePlacesEnabled()
    .withGoogleTimeZoneEnabled()
    .withVoiceSearchHidden()
    .withUnnamedRoadHidden()
    .build(applicationContext)

startActivityForResult(locationPickerIntent, MAP_BUTTON_REQUEST_CODE)
```

And add the response code from that activity:

```kotlin
override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    if (resultCode == Activity.RESULT_OK && data != null) {
        Log.d("RESULT****", "OK")
        if (requestCode == 1) {
            val latitude = data.getDoubleExtra(LATITUDE, 0.0)
            Log.d("LATITUDE****", latitude.toString())
            val longitude = data.getDoubleExtra(LONGITUDE, 0.0)
            Log.d("LONGITUDE****", longitude.toString())
            val address = data.getStringExtra(LOCATION_ADDRESS)
            Log.d("ADDRESS****", address.toString())
            val postalcode = data.getStringExtra(ZIPCODE)
            Log.d("POSTALCODE****", postalcode.toString())
            val bundle = data.getBundleExtra(TRANSITION_BUNDLE)
            Log.d("BUNDLE TEXT****", bundle.getString("test"))
            val fullAddress = data.getParcelableExtra<Address>(ADDRESS)
            if (fullAddress != null) {
                Log.d("FULL ADDRESS****", fullAddress.toString())
            }
            val timeZoneId = data.getStringExtra(TIME_ZONE_ID)
            Log.d("TIME ZONE ID****", timeZoneId)
            val timeZoneDisplayName = data.getStringExtra(TIME_ZONE_DISPLAY_NAME)
            Log.d("TIME ZONE NAME****", timeZoneDisplayName)
        } else if (requestCode == 2) {
            val latitude = data.getDoubleExtra(LATITUDE, 0.0)
            Log.d("LATITUDE****", latitude.toString())
            val longitude = data.getDoubleExtra(LONGITUDE, 0.0)
            Log.d("LONGITUDE****", longitude.toString())
            val address = data.getStringExtra(LOCATION_ADDRESS)
            Log.d("ADDRESS****", address.toString())
            val lekuPoi = data.getParcelableExtra<LekuPoi>(LEKU_POI)
            Log.d("LekuPoi****", lekuPoi.toString())
        }
    }
    if (resultCode == Activity.RESULT_CANCELED) {
        Log.d("RESULT****", "CANCELLED")
    }
}
```

That's all folks!
