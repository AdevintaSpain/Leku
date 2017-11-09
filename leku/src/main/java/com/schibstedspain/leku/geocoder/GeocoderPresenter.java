package com.schibstedspain.leku.geocoder;

import com.google.android.gms.maps.model.LatLng;
import hu.akarnokd.rxjava.interop.RxJavaInterop;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import java.util.concurrent.TimeUnit;
import pl.charmas.android.reactivelocation.ReactiveLocationProvider;

public class GeocoderPresenter {
  private static final int RETRY_COUNT = 3;

  private GeocoderViewInterface view;
  private final GeocoderViewInterface nullView = new GeocoderViewInterface.NullView();
  private CompositeDisposable compositeDisposable = new CompositeDisposable();
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
  }

  public void setUI(GeocoderViewInterface geocoderViewInterface) {
    this.view = geocoderViewInterface;
  }

  public void stop() {
    this.view = nullView;
    if (compositeDisposable != null) {
      compositeDisposable.clear();
    }
  }

  public void getLastKnownLocation() {
    Disposable disposable = RxJavaInterop.toV2Observable(
        locationProvider.getLastKnownLocation()).retry(RETRY_COUNT).subscribe(view::showLastLocation, throwable -> {
        }, view::didGetLastLocation);
    compositeDisposable.add(disposable);
  }

  public void getFromLocationName(String query) {
    view.willLoadLocation();
    Disposable disposable = geocoderRepository.getFromLocationName(query)
        .observeOn(scheduler)
        .subscribe(view::showLocations, throwable -> view.showLoadLocationError(),
            view::didLoadLocation);
    compositeDisposable.add(disposable);
  }

  public void getFromLocationName(String query, LatLng lowerLeft, LatLng upperRight) {
    view.willLoadLocation();
    Disposable disposable = geocoderRepository.getFromLocationName(query, lowerLeft, upperRight)
        .observeOn(scheduler)
        .retry(RETRY_COUNT)
        .subscribe(view::showLocations, throwable -> view.showLoadLocationError(),
            view::didLoadLocation);
    compositeDisposable.add(disposable);
  }

  public void getDebouncedFromLocationName(String query, int debounceTime) {
    view.willLoadLocation();
    Disposable disposable = geocoderRepository.getFromLocationName(query)
        .debounce(debounceTime, TimeUnit.MILLISECONDS)
        .observeOn(scheduler)
        .subscribe(view::showDebouncedLocations, throwable -> view.showLoadLocationError(),
            view::didLoadLocation);
    compositeDisposable.add(disposable);
  }

  public void getDebouncedFromLocationName(String query, LatLng lowerLeft, LatLng upperRight, int debounceTime) {
    view.willLoadLocation();
    Disposable disposable = geocoderRepository.getFromLocationName(query, lowerLeft, upperRight)
        .debounce(debounceTime, TimeUnit.MILLISECONDS)
        .observeOn(scheduler)
        .subscribe(view::showDebouncedLocations, throwable -> view.showLoadLocationError(),
            view::didLoadLocation);
    compositeDisposable.add(disposable);
  }

  public void getInfoFromLocation(LatLng latLng) {
    view.willGetLocationInfo(latLng);
    Disposable disposable = geocoderRepository.getFromLocation(latLng)
        .observeOn(scheduler)
        .retry(RETRY_COUNT)
        .subscribe(view::showLocationInfo, throwable -> view.showGetLocationInfoError(),
            view::didGetLocationInfo);
    compositeDisposable.add(disposable);
  }
}
