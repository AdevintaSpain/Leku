package com.schibsted.mappicker

import android.app.Activity
import android.content.Intent
import android.location.Address
import android.location.Location
import android.os.Bundle
import android.os.StrictMode
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Toast
import com.schibstedspain.leku.LekuPoi
import com.schibstedspain.leku.LocationPicker
import com.schibstedspain.leku.LocationPickerActivity
import com.schibstedspain.leku.tracker.LocationPickerTracker
import com.schibstedspain.leku.tracker.TrackEvents
import java.util.ArrayList
import java.util.UUID

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
    mapButton.setOnClickListener {
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
          .build(applicationContext)

      //this is optional if you want to return RESULT_OK if you don't set the latitude/longitude and click back button
      locationPickerIntent.putExtra("test", "this is a test")

      startActivityForResult(locationPickerIntent, MAP_BUTTON_REQUEST_CODE)
    }

    val mapPoisButton = findViewById<View>(R.id.map_button_with_pois)
    mapPoisButton.setOnClickListener {
      val locationPickerIntent = LocationPickerActivity.Builder()
          .withLocation(41.4036299, 2.1743558)
          .withPois(lekuPois)
          .build(applicationContext)

      startActivityForResult(locationPickerIntent, MAP_POIS_BUTTON_REQUEST_CODE)
    }

    initializeLocationPickerTracker()
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
    if (resultCode == Activity.RESULT_OK) {
      Log.d("RESULT****", "OK")
      if (requestCode == 1) {
        val latitude = data.getDoubleExtra(LocationPickerActivity.LATITUDE, 0.0)
        Log.d("LATITUDE****", latitude.toString())
        val longitude = data.getDoubleExtra(LocationPickerActivity.LONGITUDE, 0.0)
        Log.d("LONGITUDE****", longitude.toString())
        val address = data.getStringExtra(LocationPickerActivity.LOCATION_ADDRESS)
        Log.d("ADDRESS****", address.toString())
        val postalcode = data.getStringExtra(LocationPickerActivity.ZIPCODE)
        Log.d("POSTALCODE****", postalcode.toString())
        val bundle = data.getBundleExtra(LocationPickerActivity.TRANSITION_BUNDLE)
        Log.d("BUNDLE TEXT****", bundle.getString("test"))
        val fullAddress = data.getParcelableExtra<Address>(LocationPickerActivity.ADDRESS)
        if (fullAddress != null) {
          Log.d("FULL ADDRESS****", fullAddress.toString())
        }
      } else if (requestCode == 2) {
        val latitude = data.getDoubleExtra(LocationPickerActivity.LATITUDE, 0.0)
        Log.d("LATITUDE****", latitude.toString())
        val longitude = data.getDoubleExtra(LocationPickerActivity.LONGITUDE, 0.0)
        Log.d("LONGITUDE****", longitude.toString())
        val address = data.getStringExtra(LocationPickerActivity.LOCATION_ADDRESS)
        Log.d("ADDRESS****", address.toString())
        val lekuPoi = data.getParcelableExtra<LekuPoi>(LocationPickerActivity.LEKU_POI)
        Log.d("LekuPoi****", lekuPoi.toString())
      }
    }
    if (resultCode == Activity.RESULT_CANCELED) {
      Log.d("RESULT****", "CANCELLED")
    }
  }

  private fun initializeLocationPickerTracker() {
    LocationPicker.setTracker(object : LocationPickerTracker {
      override fun onEventTracked(event: TrackEvents) {
        Toast.makeText(this@MainActivity, "Event: " + event.eventName, Toast.LENGTH_SHORT)
            .show()
      }
    })
  }

  companion object {
    const val MAP_BUTTON_REQUEST_CODE = 1
    const val MAP_POIS_BUTTON_REQUEST_CODE = 2
  }
}
