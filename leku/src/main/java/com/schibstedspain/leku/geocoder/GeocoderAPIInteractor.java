package com.schibstedspain.leku.geocoder;

import android.location.Address;
import android.location.Geocoder;
import com.google.android.gms.maps.model.LatLng;
import com.schibstedspain.leku.geocoder.api.NetworkClient;
import java.io.IOException;
import java.util.List;
import rx.Observable;

public class GeocoderAPIInteractor implements GeocoderInteractorInterface {

  private final static int MAX_RESULTS = 5;
  private final String apiKey;
  private final NetworkClient networkClient;
  private final Geocoder geocoder;

  public GeocoderAPIInteractor(String ApiKey, NetworkClient networkClient) {
    apiKey = ApiKey;
    this.networkClient = networkClient;
    geocoder = new Geocoder(null);
  }

  @Override
  public Observable<List<Address>> getFromLocationName(String query) {
    return Observable.create(subscriber -> {
      try {
        subscriber.onNext(geocoder.getFromLocationName(query, MAX_RESULTS));
        subscriber.onCompleted();
      } catch (IOException e) {
        subscriber.onError(e);
      }
    });
  }

  @Override
  public Observable<List<Address>> getFromLocationName(String query, LatLng lowerLeft,
      LatLng upperRight) {
    return Observable.create(subscriber -> {
      try {
        subscriber.onNext(geocoder.getFromLocationName(query, MAX_RESULTS, lowerLeft.latitude,
            lowerLeft.longitude, upperRight.latitude, upperRight.longitude));
        subscriber.onCompleted();
      } catch (IOException e) {
        subscriber.onError(e);
      }
    });
  }

  @Override
  public Observable<List<Address>> getFromLocation(double latitude, double longitude) {
    return Observable.create(subscriber -> {
      try {
        subscriber.onNext(geocoder.getFromLocation(latitude, longitude, MAX_RESULTS));
        subscriber.onCompleted();
      } catch (IOException e) {
        subscriber.onError(e);
      }
    });
  }
}
