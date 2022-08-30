package com.adevinta.mappicker

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.location.Address
import android.location.Location
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.adevinta.leku.ADDRESS
import com.adevinta.leku.LATITUDE
import com.adevinta.leku.LEKU_POI
import com.adevinta.leku.LOCATION_ADDRESS
import com.adevinta.leku.LONGITUDE
import com.adevinta.leku.LekuPoi
import com.adevinta.leku.LocationPicker
import com.adevinta.leku.LocationPickerActivity
import com.adevinta.leku.TIME_ZONE_DISPLAY_NAME
import com.adevinta.leku.TIME_ZONE_ID
import com.adevinta.leku.TRANSITION_BUNDLE
import com.adevinta.leku.ZIPCODE
import com.adevinta.leku.tracker.LocationPickerTracker
import com.adevinta.leku.tracker.TrackEvents
import java.util.UUID
import kotlin.collections.ArrayList
import kotlin.collections.List

private const val DEMO_LATITUDE = 41.4036299
private const val DEMO_LONGITUDE = 2.1743558
private const val POI1_LATITUDE = 41.4036339
private const val POI1_LONGITUDE = 2.1721618
private const val POI2_LATITUDE = 41.4023265
private const val POI2_LONGITUDE = 2.1741417

class MainActivity : AppCompatActivity() {
    lateinit var lekuActivityResultLauncher: ActivityResultLauncher<Intent>
    lateinit var mapPoisActivityResultLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build()
        )
        StrictMode.setVmPolicy(StrictMode.VmPolicy.Builder().detectAll().penaltyLog().build())

        setContent {
            MainView()
        }

        lekuActivityResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                if (result.resultCode == Activity.RESULT_OK) {
                    onResult(result.resultCode, result.data)
                } else {
                    Log.d("RESULT****", "CANCELLED")
                }
            }

        mapPoisActivityResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                if (result.resultCode == Activity.RESULT_OK) {
                    onResultWithPois(result.resultCode, result.data)
                } else {
                    Log.d("RESULT WITH POIS****", "CANCELLED")
                }
            }

        initializeLocationPickerTracker()
    }

    private fun onResult(requestCode: Int, data: Intent?) {
        Log.d("RESULT****", "OK")
        val latitude = data?.getDoubleExtra(LATITUDE, 0.0)
        Log.d("LATITUDE****", latitude.toString())
        val longitude = data?.getDoubleExtra(LONGITUDE, 0.0)
        Log.d("LONGITUDE****", longitude.toString())
        val address = data?.getStringExtra(LOCATION_ADDRESS)
        Log.d("ADDRESS****", address.toString())
        val postalcode = data?.getStringExtra(ZIPCODE)
        Log.d("POSTALCODE****", postalcode.toString())
        val bundle = data?.getBundleExtra(TRANSITION_BUNDLE)
        Log.d("BUNDLE TEXT****", bundle?.getString("test").toString())
        val fullAddress = data?.getParcelableExtra<Address>(ADDRESS)
        if (fullAddress != null) {
            Log.d("FULL ADDRESS****", fullAddress.toString())
        }
        val timeZoneId = data?.getStringExtra(TIME_ZONE_ID)
        if (timeZoneId != null) {
            Log.d("TIME ZONE ID****", timeZoneId)
        }
        val timeZoneDisplayName = data?.getStringExtra(TIME_ZONE_DISPLAY_NAME)
        if (timeZoneDisplayName != null) {
            Log.d("TIME ZONE NAME****", timeZoneDisplayName)
        }
    }

    private fun onResultWithPois(requestCode: Int, data: Intent?) {
        Log.d("RESULT WITH POIS****", "OK")
        val latitude = data?.getDoubleExtra(LATITUDE, 0.0)
        Log.d("LATITUDE****", latitude.toString())
        val longitude = data?.getDoubleExtra(LONGITUDE, 0.0)
        Log.d("LONGITUDE****", longitude.toString())
        val address = data?.getStringExtra(LOCATION_ADDRESS)
        Log.d("ADDRESS****", address.toString())
        val lekuPoi = data?.getParcelableExtra<LekuPoi>(LEKU_POI)
        Log.d("LekuPoi****", lekuPoi.toString())
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

private fun onLaunchMapPickerClicked(context: Context) {
    val activity = context as MainActivity
    val locationPickerIntent = LocationPickerActivity.Builder()
        .withLocation(DEMO_LATITUDE, DEMO_LONGITUDE)
        // .withGeolocApiKey("<PUT API KEY HERE>")
        // .withGooglePlacesApiKey("<PUT API KEY HERE>")
        .withSearchZone("es_ES")
        // .withSearchZone(SearchZoneRect(LatLng(26.525467, -18.910366), LatLng(43.906271, 5.394197)))
        .withDefaultLocaleSearchZone()
        // .shouldReturnOkOnBackPressed()
        // .withStreetHidden()
        // .withCityHidden()
        // .withZipCodeHidden()
        // .withSatelliteViewHidden()
        .withGoogleTimeZoneEnabled()
        // .withVoiceSearchHidden()
        .withUnnamedRoadHidden()
        // .withSearchBarHidden()
        .build(activity)

    // this is optional if you want to return RESULT_OK if you don't set the
    // latitude/longitude and click back button
    locationPickerIntent.putExtra("test", "this is a test")

    activity.lekuActivityResultLauncher.launch(locationPickerIntent)
}

private fun onLegacyMapClicked(context: Context) {
    val activity = context as MainActivity
    val locationPickerIntent = LocationPickerActivity.Builder()
        .withLocation(DEMO_LATITUDE, DEMO_LONGITUDE)
        .withUnnamedRoadHidden()
        .withLegacyLayout()
        .build(activity)
    activity.lekuActivityResultLauncher.launch(locationPickerIntent)
}

private val lekuPois: List<LekuPoi>
    get() {
        val pois = ArrayList<LekuPoi>()

        val locationPoi1 = Location("leku")
        locationPoi1.latitude = POI1_LATITUDE
        locationPoi1.longitude = POI1_LONGITUDE
        val poi1 = LekuPoi(UUID.randomUUID().toString(), "Los bellota", locationPoi1)
        pois.add(poi1)

        val locationPoi2 = Location("leku")
        locationPoi2.latitude = POI2_LATITUDE
        locationPoi2.longitude = POI2_LONGITUDE
        val poi2 = LekuPoi(UUID.randomUUID().toString(), "Starbucks", locationPoi2)
        poi2.address = "Plaça de la Sagrada Família, 19, 08013 Barcelona"
        pois.add(poi2)

        return pois
    }

private fun onMapPoisClicked(context: Context) {
    val activity = context as MainActivity
    val locationPickerIntent = LocationPickerActivity.Builder()
        .withLocation(DEMO_LATITUDE, DEMO_LONGITUDE)
        .withPois(lekuPois)
        .build(activity)

    activity.mapPoisActivityResultLauncher.launch(locationPickerIntent)
}

private fun onMapWithStylesClicked(context: Context) {
    val activity = context as MainActivity
    val locationPickerIntent = LocationPickerActivity.Builder()
        .withLocation(DEMO_LATITUDE, DEMO_LONGITUDE)
        .withMapStyle(R.raw.map_style_retro)
        .build(activity)
    activity.mapPoisActivityResultLauncher.launch(locationPickerIntent)
}

@Composable
@Preview(showBackground = true)
fun MainView() {
    val context = LocalContext.current

    Column(
        Modifier.padding(16.dp, 40.dp, 16.dp, 0.dp).fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Image(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            painter = painterResource(id = R.mipmap.leku_img_logo),
            contentDescription = null
        )
        Button(
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color(context.resources.getColor(R.color.leku_app_blue)),
                contentColor = Color.White
            ),
            onClick = {
                onLaunchMapPickerClicked(context)
            }
        ) {
            Text(
                stringResource(id = R.string.launch_map_picker),
                Modifier.padding(8.dp).fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
        Button(
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color(context.resources.getColor(R.color.leku_app_blue)),
                contentColor = Color.White
            ),
            onClick = {
                onLegacyMapClicked(context)
            }
        ) {
            Text(
                stringResource(id = R.string.launch_legacy_map_picker),
                Modifier.padding(8.dp).fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
        Button(
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color(context.resources.getColor(R.color.leku_app_blue)),
                contentColor = Color.White
            ),
            onClick = {
                onMapPoisClicked(context)
            }
        ) {
            Text(
                stringResource(id = R.string.launch_map_picker_with_style),
                Modifier.padding(8.dp).fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
        Button(
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color(context.resources.getColor(R.color.leku_app_blue)),
                contentColor = Color.White
            ),
            onClick = {
                onMapWithStylesClicked(context)
            }
        ) {
            Text(
                stringResource(id = R.string.launch_map_picker_with_pois),
                Modifier.padding(8.dp).fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
        Box(modifier = Modifier.fillMaxSize()) {
            Text(
                stringResource(id = R.string.leku_lib_version),
                modifier = Modifier.align(Alignment.BottomCenter).padding(0.dp, 0.dp, 0.dp, 8.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}
