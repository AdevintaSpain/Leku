package com.schibsted.leku.geocoder;

import com.google.android.gms.maps.model.LatLng;
import pl.charmas.android.reactivelocation.ReactiveLocationProvider;
import rx.Scheduler;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class GeocoderPresenter {
  private final GeocoderInteractorInterface interactor;
  private GeocoderViewInterface view;
  private final GeocoderViewInterface nullView = new GeocoderViewInterface.NullView();
  private Subscription locationSubscription;
  private Subscription locationNameSubscription;
  private Subscription lastKnownLocationSubscription;
  private final Scheduler scheduler;
  private ReactiveLocationProvider locationProvider;

  public GeocoderPresenter(ReactiveLocationProvider reactiveLocationProvider, GeocoderInteractorInterface interactor) {
    this(reactiveLocationProvider, interactor, AndroidSchedulers.mainThread());
  }

  public GeocoderPresenter(ReactiveLocationProvider reactiveLocationProvider, GeocoderInteractorInterface interactor,
      Scheduler scheduler) {
    this.view = nullView;
    this.scheduler = scheduler;
    this.locationProvider = reactiveLocationProvider;
    this.interactor = interactor;
  }

  public void setUI(GeocoderViewInterface geocoderViewInterface) {
    this.view = geocoderViewInterface;
  }

  public void stop() {
    this.view = nullView;
    if (lastKnownLocationSubscription != null) {
      lastKnownLocationSubscription.unsubscribe();
    }
    if (locationNameSubscription != null) {
      locationNameSubscription.unsubscribe();
    }
    if (locationSubscription != null) {
      locationSubscription.unsubscribe();
    }
  }

  public void getLastKnownLocation() {
    lastKnownLocationSubscription =
        locationProvider.getLastKnownLocation().subscribe(view::showLastLocation, throwable -> {
        }, view::didGetLastLocation);
  }

  public void getFromLocationName(String query) {
    view.willLoadLocation();
    locationNameSubscription = interactor.getFromLocationName(query)
        .subscribeOn(Schedulers.newThread())
        .observeOn(scheduler)
        .subscribe(view::showLocations, throwable -> view.showLoadLocationError(),
            view::didLoadLocation);
  }

  public void getFromLocationName(String query, LatLng lowerLeft, LatLng upperRight) {
    view.willLoadLocation();
    locationNameSubscription = interactor.getFromLocationName(query, lowerLeft, upperRight)
        .subscribeOn(Schedulers.newThread())
        .observeOn(scheduler)
        .subscribe(view::showLocations, throwable -> view.showLoadLocationError(),
            view::didLoadLocation);
  }

  public void getInfoFromLocation(double latitude, double longitude) {
    view.willGetLocationInfo();
    locationSubscription = interactor.getFromLocation(latitude, longitude)
        .subscribeOn(Schedulers.newThread())
        .observeOn(scheduler)
        .subscribe(view::showLocationInfo, throwable -> view.showGetLocationInfoError(),
            view::didGetLocationInfo);
  }
}
