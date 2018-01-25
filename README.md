# Leku Home Page

<div style="display:block; height: 168px;">
* <i>The location Picker for Android</i> *

[![Build Status](https://travis-ci.org/SchibstedSpain/Leku.svg?branch=master)](https://travis-ci.org/SchibstedSpain/Leku) [ ![Bintray](https://api.bintray.com/packages/schibstedspain/maven/leku/images/download.svg) ](https://bintray.com/schibstedspain/maven/leku/_latestVersion) [ ![Leku Trending](http://starveller.sigsev.io/api/repos/SchibstedSpain/Leku/badge) ](http://starveller.sigsev.io/SchibstedSpain/Leku)

Component library for Android that uses Google Maps and returns a latitude, longitude and an address based on the location picked with the Activity provided.
</div>

<br/>
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
    implementation 'com.schibstedspain.android:leku:4.0.0'
}
```

Alternatively, if you are using a different version of Google Play Services than `11.8.0` use this instead:

```groovy
implementation ('com.schibstedspain.android:leku:4.0.0') {
    exclude group: 'com.google.android.gms'
    exclude group: 'com.android.support'
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

```java
Intent intent = new LocationPickerActivity.Builder()
    .withLocation(41.4036299, 2.1743558)
    .withGeolocApiKey("<PUT API KEY HERE>")
    .withSearchZone("es_ES")
    .shouldReturnOkOnBackPressed()
    .withStreetHidden()
    .withCityHidden()
    .withZipCodeHidden()
    .withSatelliteViewHidden()
    .build(getApplicationContext());

startActivityForResult(intent, 1);
```

And add the response code from that activity:

```java

@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == 1) {
        if(resultCode == RESULT_OK){
            double latitude = data.getDoubleExtra(LocationPickerActivity.LATITUDE, 0);
            Log.d("LATITUDE****", String.valueOf(latitude));
            double longitude = data.getDoubleExtra(LocationPickerActivity.LONGITUDE, 0);
            Log.d("LONGITUDE****", String.valueOf(longitude));
            String address = data.getStringExtra(LocationPickerActivity.LOCATION_ADDRESS);
            Log.d("ADDRESS****", String.valueOf(address));
            String postalcode = data.getStringExtra(LocationPickerActivity.ZIPCODE);
            Log.d("POSTALCODE****", String.valueOf(postalcode));
            Bundle bundle = data.getBundleExtra(LocationPickerActivity.TRANSITION_BUNDLE);
            Log.d("BUNDLE TEXT****", bundle.getString("test"));
            Address fullAddress = data.getParcelableExtra(LocationPickerActivity.ADDRESS);
            if(fullAddress != null)
              Log.d("FULL ADDRESS****", fullAddress.toString());
        }
        if (resultCode == RESULT_CANCELED) {
            //Write your code if there's no result
        }
    }
}

```

That's all folks!


Bugs and Feedback
-----------------

For bugs, questions and discussions please use the [Github Issues](https://github.com/SchibstedSpain/leku/issues).


License
-------

Copyright 2016 Schibsted Classified Media Spain S.L.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.