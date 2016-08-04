package com.schibsted.mappicker;

import android.content.Intent;
import android.location.Address;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import com.schibstedspain.leku.LocationPicker;
import com.schibstedspain.leku.LocationPickerActivity;
import com.schibstedspain.leku.tracker.LocationPickerTracker;
import com.schibstedspain.leku.tracker.TrackEvents;

public class MainActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    StrictMode.setThreadPolicy(
        new StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build());
    StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectAll().penaltyLog().build());
    setContentView(R.layout.activity_main);
    View mapButton = findViewById(R.id.map_button);
    mapButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Intent intent = new Intent(getApplicationContext(), LocationPickerActivity.class);
        intent.putExtra(LocationPickerActivity.LATITUDE, 41.4036299);
        intent.putExtra(LocationPickerActivity.LONGITUDE, 2.1743558);
        //intent.putExtra(LocationPickerActivity.LAYOUTS_TO_HIDE, "street|city"); //this is optional if you want to hide some info
        //intent.putExtra(LocationPickerActivity.SEARCH_ZONE, "es_ES"); //this is optional if an specific search location
        intent.putExtra("test", "this is a test");
        startActivityForResult(intent, 1);
      }
    });
    initializeLocationPickerTracker();
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {

    if (requestCode == 1) {
      if (resultCode == RESULT_OK) {
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
        Log.d("FULL ADDRESS****", fullAddress.toString());
      }
      //if (resultCode == RESULT_CANCELED) {
      //Write your code if there's no result
      //}
    }
  }

  private void initializeLocationPickerTracker() {
    LocationPicker.setTracker(new LocationPickerTracker() {
      @Override
      public void onEventTracked(TrackEvents event) {
        Toast.makeText(MainActivity.this, "Event: " + event.getEventName(), Toast.LENGTH_SHORT)
            .show();
      }
    });
  }
}
