package com.schibstedspain.leku.geocoder;

import android.location.Address;
import com.google.android.gms.maps.model.LatLng;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import java.util.List;

public class GeocoderRepository {
  private static final int RETRY_COUNT = 3;

  private final GeocoderInteractorDataSource androidGeocoder;
  private final GeocoderInteractorDataSource googleGeocoder;

  public GeocoderRepository(GeocoderInteractorDataSource androidGeocoder, GeocoderInteractorDataSource googleGeocoder) {
    this.androidGeocoder = androidGeocoder;
    this.googleGeocoder = googleGeocoder;
  }

  public Observable<List<Address>> getFromLocationName(String query) {
    return androidGeocoder.getFromLocationName(query)
        .subscribeOn(Schedulers.newThread())
        .retry(RETRY_COUNT)
        .onErrorResumeNext(googleGeocoder.getFromLocationName(query))
        .onExceptionResumeNext(googleGeocoder.getFromLocationName(query));
  }

  public Observable<List<Address>> getFromLocationName(String query, LatLng lowerLeft, LatLng upperRight) {
    return androidGeocoder.getFromLocationName(query, lowerLeft, upperRight)
        .subscribeOn(Schedulers.newThread())
        .retry(RETRY_COUNT)
        .onErrorResumeNext(googleGeocoder.getFromLocationName(query, lowerLeft, upperRight))
        .onExceptionResumeNext(googleGeocoder.getFromLocationName(query, lowerLeft, upperRight));
  }

  public Observable<List<Address>> getFromLocation(LatLng latLng) {
    return androidGeocoder.getFromLocation(latLng.latitude, latLng.longitude)
        .subscribeOn(Schedulers.newThread())
        .retry(RETRY_COUNT)
        .onErrorResumeNext(googleGeocoder.getFromLocation(latLng.latitude, latLng.longitude))
        .onExceptionResumeNext(googleGeocoder.getFromLocation(latLng.latitude, latLng.longitude));
  }
}
