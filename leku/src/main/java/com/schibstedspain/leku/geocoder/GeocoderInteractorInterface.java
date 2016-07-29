package com.schibstedspain.leku.geocoder;

import android.location.Address;
import com.google.android.gms.maps.model.LatLng;
import java.util.List;
import rx.Observable;

public interface GeocoderInteractorInterface {
  Observable<List<Address>> getFromLocationName(String query);

  Observable<List<Address>> getFromLocationName(String query, LatLng lowerLeft, LatLng upperRight);

  Observable<List<Address>> getFromLocation(double latitude, double longitude);
}
