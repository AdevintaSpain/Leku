package com.schibstedspain.leku.geocoder;

import android.location.Address;
import android.location.Location;
import java.util.List;

public interface GeocoderViewInterface {
  void willLoadLocation();

  void showLocations(List<Address> addresses);

  void showDebouncedLocations(List<Address> addresses);

  void didLoadLocation();

  void showLoadLocationError();

  void showLastLocation(Location location);

  void didGetLastLocation();

  void showLocationInfo(List<Address> addresses);

  void willGetLocationInfo(double longitude, double latitude);

  void didGetLocationInfo();

  void showGetLocationInfoError();

  class NullView implements GeocoderViewInterface {

    @Override
    public void willLoadLocation() {

    }

    @Override
    public void showLocations(List<Address> addresses) {

    }

    @Override
    public void showDebouncedLocations(List<Address> addresses) {

    }

    @Override
    public void didLoadLocation() {

    }

    @Override
    public void showLoadLocationError() {

    }

    @Override
    public void showLastLocation(Location location) {

    }

    @Override
    public void didGetLastLocation() {

    }

    @Override
    public void showLocationInfo(List<Address> addresses) {

    }

    @Override
    public void willGetLocationInfo(double longitude, double latitude) {

    }

    @Override
    public void didGetLocationInfo() {

    }

    @Override
    public void showGetLocationInfoError() {

    }
  }
}
