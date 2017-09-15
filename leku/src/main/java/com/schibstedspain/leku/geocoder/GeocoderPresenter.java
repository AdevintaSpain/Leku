package com.schibstedspain.leku.geocoder;

import com.google.android.gms.maps.model.LatLng;
import java.util.concurrent.TimeUnit;
import pl.charmas.android.reactivelocation.ReactiveLocationProvider;
import rx.Scheduler;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

public class GeocoderPresenter {

  public static final int SEARCH_DEBOUNCE_TIME = 400;
  private static final int RETRY_COUNT = 3;

  private GeocoderViewInterface view;
  private final GeocoderViewInterface nullView = new GeocoderViewInterface.NullView();
  private CompositeSubscription compositeSubscription;
  private final Scheduler scheduler;
  private ReactiveLocationProvider locationProvider;
  private GeocoderRepository geocoderRepository;

  public GeocoderPresenter(ReactiveLocationProvider reactiveLocationProvider, GeocoderRepository geocoderRepository) {
    this(reactiveLocationProvider, geocoderRepository, AndroidSchedulers.mainThread());
  }

  public GeocoderPresenter(ReactiveLocationProvider reactiveLocationProvider, GeocoderRepository geocoderRepository, Scheduler scheduler) {
    this.geocoderRepository = geocoderRepository;
    this.view = nullView;
    this.scheduler = scheduler;
    this.locationProvider = reactiveLocationProvider;
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
    Subscription locationNameSubscription = geocoderRepository.getFromLocationName(query)
        .observeOn(scheduler)
        .subscribe(view::showLocations, throwable -> view.showLoadLocationError(),
            view::didLoadLocation);
    compositeSubscription.add(locationNameSubscription);
  }

  public void getFromLocationName(String query, LatLng lowerLeft, LatLng upperRight) {
    view.willLoadLocation();
    Subscription locationNameSubscription = geocoderRepository.getFromLocationName(query, lowerLeft, upperRight)
        .observeOn(scheduler)
        .retry(RETRY_COUNT)
        .subscribe(view::showLocations, throwable -> view.showLoadLocationError(),
            view::didLoadLocation);
    compositeSubscription.add(locationNameSubscription);
  }

  public void getDebouncedFromLocationName(String query) {
    view.willLoadLocation();
    Subscription locationNameDebounceSubscription = geocoderRepository.getFromLocationName(query)
        .debounce(SEARCH_DEBOUNCE_TIME, TimeUnit.MILLISECONDS)
        .observeOn(scheduler)
        .subscribe(view::showDebouncedLocations, throwable -> view.showLoadLocationError(),
            view::didLoadLocation);
    compositeSubscription.add(locationNameDebounceSubscription);
  }

  public void getDebouncedFromLocationName(String query, LatLng lowerLeft, LatLng upperRight) {
    view.willLoadLocation();
    Subscription locationNameDebounceSubscription = geocoderRepository.getFromLocationName(query, lowerLeft, upperRight)
        .debounce(SEARCH_DEBOUNCE_TIME, TimeUnit.MILLISECONDS)
        .observeOn(scheduler)
        .subscribe(view::showDebouncedLocations, throwable -> view.showLoadLocationError(),
            view::didLoadLocation);
    compositeSubscription.add(locationNameDebounceSubscription);
  }

  public void getInfoFromLocation(LatLng latLng) {
    view.willGetLocationInfo(latLng);
    Subscription locationSubscription = geocoderRepository.getFromLocation(latLng)
        .observeOn(scheduler)
        .retry(RETRY_COUNT)
        .subscribe(view::showLocationInfo, throwable -> view.showGetLocationInfoError(),
            view::didGetLocationInfo);
    compositeSubscription.add(locationSubscription);
  }
}
