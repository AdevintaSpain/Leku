package com.schibsted.mappicker

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.location.Address
import android.location.Location
import android.os.Bundle
import android.os.StrictMode
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Toast
import com.schibstedspain.leku.LATITUDE
import com.schibstedspain.leku.LONGITUDE
import com.schibstedspain.leku.LOCATION_ADDRESS
import com.schibstedspain.leku.ADDRESS
import com.schibstedspain.leku.ZIPCODE
import com.schibstedspain.leku.TRANSITION_BUNDLE
import com.schibstedspain.leku.LEKU_POI
import com.schibstedspain.leku.TIME_ZONE_ID
import com.schibstedspain.leku.TIME_ZONE_DISPLAY_NAME
import com.schibstedspain.leku.LekuPoi
import com.schibstedspain.leku.LocationPickerActivity
import com.schibstedspain.leku.LocationPicker
import com.schibstedspain.leku.tracker.LocationPickerTracker
import com.schibstedspain.leku.tracker.TrackEvents
import java.util.UUID

private const val MAP_BUTTON_REQUEST_CODE = 1
private const val MAP_POIS_BUTTON_REQUEST_CODE = 2

class MainActivity : AppCompatActivity() {

    private val lekuPois: List<LekuPoi>
        get() {
            val pois = ArrayList<LekuPoi>()

            val locationPoi1 = Location("leku")
            locationPoi1.latitude = 41.4036339
            locationPoi1.longitude = 2.1721618
            val poi1 = LekuPoi(UUID.randomUUID().toString(), "Los bellota", locationPoi1)
            pois.add(poi1)

            val locationPoi2 = Location("leku")
            locationPoi2.latitude = 41.4023265
            locationPoi2.longitude = 2.1741417
            val poi2 = LekuPoi(UUID.randomUUID().toString(), "Starbucks", locationPoi2)
            poi2.address = "Plaça de la Sagrada Família, 19, 08013 Barcelona"
            pois.add(poi2)

            return pois
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build())
        StrictMode.setVmPolicy(StrictMode.VmPolicy.Builder().detectAll().penaltyLog().build())
        setContentView(R.layout.activity_main)
        val mapButton = findViewById<View>(R.id.map_button)
        mapButton.setOnClickListener { _ ->
            val locationPickerIntent = LocationPickerActivity.Builder()
                    .withLocation(41.4036299, 2.1743558)
                    //.withGeolocApiKey("<PUT API KEY HERE>")
                    .withSearchZone("es_ES")
                    //.shouldReturnOkOnBackPressed()
                    //.withStreetHidden()
                    //.withCityHidden()
                    //.withZipCodeHidden()
                    //.withSatelliteViewHidden()
                    //.withGooglePlacesEnabled()
                    .withGoogleTimeZoneEnabled()
                    //.withVoiceSearchHidden()
                    .build(applicationContext)

            //this is optional if you want to return RESULT_OK if you don't set the latitude/longitude and click back button
            locationPickerIntent.putExtra("test", "this is a test")

            startActivityForResult(locationPickerIntent, MAP_BUTTON_REQUEST_CODE)
        }

        val mapPoisButton = findViewById<View>(R.id.map_button_with_pois)
        mapPoisButton.setOnClickListener { _ ->
            val locationPickerIntent = LocationPickerActivity.Builder()
                    .withLocation(41.4036299, 2.1743558)
                    .withPois(lekuPois)
                    .build(applicationContext)

            startActivityForResult(locationPickerIntent, MAP_POIS_BUTTON_REQUEST_CODE)
        }

        initializeLocationPickerTracker()
    }

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
                if (timeZoneId != null) {
                    Log.d("TIME ZONE ID****", timeZoneId)
                }
                val timeZoneDisplayName = data.getStringExtra(TIME_ZONE_DISPLAY_NAME)
                if (timeZoneDisplayName != null) {
                    Log.d("TIME ZONE NAME****", timeZoneDisplayName)
                }
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

    private fun initializeLocationPickerTracker() {
        LocationPicker.setTracker(MyPickerTracker(this))
    }

    private class MyPickerTracker(private val context: Context) : LocationPickerTracker {
        override fun onEventTracked(event: TrackEvents) {
            Toast.makeText(context, "Event: " + event.eventName, Toast.LENGTH_SHORT).show()
        }
    }
}
