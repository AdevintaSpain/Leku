package com.schibstedspain.leku.geocoder;

import android.location.Address;
import com.google.android.gms.maps.model.LatLng;
import com.schibstedspain.leku.geocoder.api.AddressBuilder;
import com.schibstedspain.leku.geocoder.api.NetworkClient;
import io.reactivex.Observable;
import java.util.List;
import java.util.Locale;
import org.json.JSONException;

public class GoogleGeocoderDataSource implements GeocoderInteractorDataSource {

  private static final String QUERY_REQUEST = "https://maps.googleapis.com/maps/api/geocode/json?address=%1$s&key=%2$s";
  private static final String QUERY_REQUEST_WITH_RECTANGLE
      = "https://maps.googleapis.com/maps/api/geocode/json?address=%1$s&key=%2$s&bounds=%3$f,%4$f|%5$f,%6$f";
  private static final String QUERY_LAT_LONG = "https://maps.googleapis.com/maps/api/geocode/json?latlng=%1$f,%2$f&key=%3$s";
  private String apiKey;
  private final NetworkClient networkClient;
  private final AddressBuilder addressBuilder;

  public GoogleGeocoderDataSource(NetworkClient networkClient, AddressBuilder addressBuilder) {
    this.networkClient = networkClient;
    this.addressBuilder = addressBuilder;
  }

  public void setApiKey(String apiKey) {
    this.apiKey = apiKey;
  }

  @Override
  public Observable<List<Address>> getFromLocationName(String query) {
    return Observable.create(subscriber -> {
      if (apiKey == null) {
        subscriber.onComplete();
        return;
      }
      try {
        String result = networkClient.requestFromLocationName(String.format(Locale.ENGLISH,
            QUERY_REQUEST, query.trim(), apiKey));
        List<Address> addresses = addressBuilder.parseResult(result);
        subscriber.onNext(addresses);
        subscriber.onComplete();
      } catch (JSONException e) {
        subscriber.onError(e);
      }
    });
  }

  @Override
  public Observable<List<Address>> getFromLocationName(String query, LatLng lowerLeft,
      LatLng upperRight) {
    return Observable.create(subscriber -> {
      if (apiKey == null) {
        subscriber.onComplete();
        return;
      }
      try {
        String result = networkClient.requestFromLocationName(String.format(Locale.ENGLISH,
            QUERY_REQUEST_WITH_RECTANGLE, query.trim(), apiKey, lowerLeft.latitude,
            lowerLeft.longitude, upperRight.latitude, upperRight.longitude));
        List<Address> addresses = addressBuilder.parseResult(result);
        subscriber.onNext(addresses);
        subscriber.onComplete();
      } catch (JSONException e) {
        subscriber.onError(e);
      }
    });
  }

  @Override
  public Observable<List<Address>> getFromLocation(double latitude, double longitude) {
    return Observable.create(subscriber -> {
      if (apiKey == null) {
        subscriber.onComplete();
        return;
      }
      try {
        String result = networkClient.requestFromLocationName(String.format(Locale.ENGLISH,
            QUERY_LAT_LONG, latitude, longitude, apiKey));
        List<Address> addresses = addressBuilder.parseResult(result);
        subscriber.onNext(addresses);
        subscriber.onComplete();
      } catch (JSONException e) {
        subscriber.onError(e);
      }
    });
  }
}
