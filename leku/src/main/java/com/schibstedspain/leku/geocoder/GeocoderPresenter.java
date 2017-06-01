package com.schibstedspain.leku.geocoder;

import com.google.android.gms.maps.model.LatLng;
import java.util.concurrent.TimeUnit;
import pl.charmas.android.reactivelocation.ReactiveLocationProvider;
import rx.Scheduler;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class GeocoderPresenter {
  private static final int RETRY_COUNT = 3;

  private final GeocoderInteractorInterface interactor;
  private GeocoderViewInterface view;
  private final GeocoderViewInterface nullView = new GeocoderViewInterface.NullView();
  private CompositeSubscription compositeSubscription;
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
    this.compositeSubscription = new CompositeSubscription();
  }

  public void setUI(GeocoderViewInterface geocoderViewInterface) {
    this.view = geocoderViewInterface;
  }

  public void stop() {
    this.view = nullView;
    if (compositeSubscription != null) {
      compositeSubscription.clear();
    }
  }

  public void getLastKnownLocation() {
    Subscription lastKnownLocationSubscription =
        locationProvider.getLastKnownLocation().retry(RETRY_COUNT).subscribe(view::showLastLocation, throwable -> {
        }, view::didGetLastLocation);
    compositeSubscription.add(lastKnownLocationSubscription);
  }

  public void getFromLocationName(String query) {
    view.willLoadLocation();
    Subscription locationNameSubscription = interactor.getFromLocationName(query)
        .subscribeOn(Schedulers.newThread())
        .observeOn(scheduler)
        .retry(RETRY_COUNT)
        .subscribe(view::showLocations, throwable -> view.showLoadLocationError(),
            view::didLoadLocation);
    compositeSubscription.add(locationNameSubscription);
  }

  public void getFromLocationName(String query, LatLng lowerLeft, LatLng upperRight) {
    view.willLoadLocation();
    Subscription locationNameSubscription = interactor.getFromLocationName(query, lowerLeft, upperRight)
        .subscribeOn(Schedulers.newThread())
        .observeOn(scheduler)
        .retry(RETRY_COUNT)
        .subscribe(view::showLocations, throwable -> view.showLoadLocationError(),
            view::didLoadLocation);
    compositeSubscription.add(locationNameSubscription);
  }

  public void getDebouncedFromLocationName(String query, int debounceTime) {
    view.willLoadLocation();
    Subscription locationNameDebounceSubscription = interactor.getFromLocationName(query)
        .debounce(debounceTime, TimeUnit.MILLISECONDS)
        .subscribeOn(Schedulers.newThread())
        .observeOn(scheduler)
        .retry(RETRY_COUNT)
        .subscribe(view::showDebouncedLocations, throwable -> view.showLoadLocationError(),
            view::didLoadLocation);
    compositeSubscription.add(locationNameDebounceSubscription);
  }

  public void getDebouncedFromLocationName(String query, LatLng lowerLeft, LatLng upperRight, int debounceTime) {
    view.willLoadLocation();
    Subscription locationNameDebounceSubscription = interactor.getFromLocationName(query, lowerLeft, upperRight)
        .debounce(debounceTime, TimeUnit.MILLISECONDS)
        .subscribeOn(Schedulers.newThread())
        .observeOn(scheduler)
        .retry(RETRY_COUNT)
        .subscribe(view::showDebouncedLocations, throwable -> view.showLoadLocationError(),
            view::didLoadLocation);
    compositeSubscription.add(locationNameDebounceSubscription);
  }

  public void getInfoFromLocation(LatLng latLng) {
    view.willGetLocationInfo(latLng);
    Subscription locationSubscription = interactor.getFromLocation(latLng.latitude, latLng.longitude)
        .subscribeOn(Schedulers.newThread())
        .observeOn(scheduler)
        .retry(RETRY_COUNT)
        .subscribe(view::showLocationInfo, throwable -> view.showGetLocationInfoError(),
            view::didGetLocationInfo);
    compositeSubscription.add(locationSubscription);
  }
}
