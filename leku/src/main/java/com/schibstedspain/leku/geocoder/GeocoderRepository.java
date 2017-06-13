package com.schibstedspain.leku.geocoder;

import android.location.Address;
import com.google.android.gms.maps.model.LatLng;
import java.util.List;
import rx.Observable;
import rx.schedulers.Schedulers;

public class GeocoderRepository {
  private static final int RETRY_COUNT = 3;

  private final GeocoderInteractorInterface androidGeocoder;
  private final GeocoderInteractorInterface googleGeocoder;

  public GeocoderRepository(GeocoderInteractorInterface androidGeocoder, GeocoderInteractorInterface googleGeocoder) {
    this.androidGeocoder = androidGeocoder;
    this.googleGeocoder = googleGeocoder;
  }

  public Observable<List<Address>> getFromLocationName(String query) {
    return androidGeocoder.getFromLocationName(query)
        .subscribeOn(Schedulers.newThread())
        .retry(RETRY_COUNT)
        .onErrorResumeNext(googleGeocoder.getFromLocationName(query));
  }

  public Observable<List<Address>> getFromLocationName(String query, LatLng lowerLeft, LatLng upperRight) {
    return androidGeocoder.getFromLocationName(query, lowerLeft, upperRight)
        .subscribeOn(Schedulers.newThread())
        .retry(RETRY_COUNT)
        .onErrorResumeNext(googleGeocoder.getFromLocationName(query, lowerLeft, upperRight));
  }

  public Observable<List<Address>> getFromLocation(LatLng latLng) {
    return androidGeocoder.getFromLocation(latLng.latitude, latLng.longitude)
        .subscribeOn(Schedulers.newThread())
        .retry(RETRY_COUNT)
        .onErrorResumeNext(googleGeocoder.getFromLocation(latLng.latitude, latLng.longitude));
  }
}
