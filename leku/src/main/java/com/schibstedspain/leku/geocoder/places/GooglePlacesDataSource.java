package com.schibstedspain.leku.geocoder.places;

import android.location.Address;
import android.util.Log;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.data.DataBufferUtils;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;
import com.google.android.gms.location.places.AutocompletePredictionBufferResponse;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBufferResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.tasks.RuntimeExecutionException;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import io.reactivex.Observable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class GooglePlacesDataSource {
  private final GeoDataClient geoDataClient;

  public GooglePlacesDataSource(GeoDataClient geoDataClient) {
    this.geoDataClient = geoDataClient;
  }

  public Observable<List<Address>> getFromLocationName(String query, LatLngBounds latLngBounds) {
    return Observable.defer(() -> {

      Task<AutocompletePredictionBufferResponse> results =
          geoDataClient.getAutocompletePredictions(query, latLngBounds, null);

      try {
        Tasks.await(results, 6, TimeUnit.SECONDS);
      } catch (ExecutionException | InterruptedException | TimeoutException ignored) {
      }

      try {
        AutocompletePredictionBufferResponse autocompletePredictions = results.getResult();

        Log.i("LEKU", "Query completed. Received " + autocompletePredictions.getCount()
            + " predictions.");

        List<AutocompletePrediction> predictionList = DataBufferUtils.freezeAndClose(autocompletePredictions);

        List<Address> addressList = new ArrayList<>();
        for (AutocompletePrediction prediction : predictionList) {
          Task<PlaceBufferResponse> placeBufferResponseTask = geoDataClient.getPlaceById(prediction.getPlaceId());
          PlaceBufferResponse placeBufferResponse = placeBufferResponseTask.getResult();
          Place place = placeBufferResponse.get(0);

          Address address = new Address(Locale.getDefault());
          address.setLatitude(place.getLatLng().latitude);
          address.setLongitude(place.getLatLng().longitude);
          address.setAddressLine(0, place.getAddress().toString());

          addressList.add(address);
        }

        return Observable.just(addressList);
      } catch (RuntimeExecutionException e) {
        Log.e("LEKU", "Error getting autocomplete prediction API call", e);
        return Observable.empty();
      }
    });
  }
}
