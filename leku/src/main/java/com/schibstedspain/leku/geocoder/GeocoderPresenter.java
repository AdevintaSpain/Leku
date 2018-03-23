package com.schibstedspain.leku.geocoder;

import android.annotation.SuppressLint;
import android.location.Address;
import android.support.annotation.NonNull;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.schibstedspain.leku.geocoder.places.GooglePlacesDataSource;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import pl.charmas.android.reactivelocation2.ReactiveLocationProvider;

public class GeocoderPresenter {
  private static final int RETRY_COUNT = 3;
  private static final int MAX_PLACES_RESULTS = 3;

  private GeocoderViewInterface view;
  private final GeocoderViewInterface nullView = new GeocoderViewInterface.NullView();
  private CompositeDisposable compositeDisposable = new CompositeDisposable();
  private final Scheduler scheduler;
  private ReactiveLocationProvider locationProvider;
  private GeocoderRepository geocoderRepository;
  private GooglePlacesDataSource googlePlacesDataSource;
  private boolean isGooglePlacesEnabled = false;

  public GeocoderPresenter(ReactiveLocationProvider reactiveLocationProvider, GeocoderRepository geocoderRepository,
      GooglePlacesDataSource placesDataSource) {
    this(reactiveLocationProvider, geocoderRepository, placesDataSource, AndroidSchedulers.mainThread());
  }

  public GeocoderPresenter(ReactiveLocationProvider reactiveLocationProvider, GeocoderRepository geocoderRepository) {
    this(reactiveLocationProvider, geocoderRepository, null, AndroidSchedulers.mainThread());
  }

  public GeocoderPresenter(ReactiveLocationProvider reactiveLocationProvider, GeocoderRepository geocoderRepository,
      GooglePlacesDataSource placesDataSource, Scheduler scheduler) {
    this.geocoderRepository = geocoderRepository;
    this.view = nullView;
    this.scheduler = scheduler;
    this.locationProvider = reactiveLocationProvider;
    this.googlePlacesDataSource = placesDataSource;
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
    @SuppressLint("MissingPermission")
    Disposable disposable = locationProvider.getLastKnownLocation()
        .retry(RETRY_COUNT)
        .subscribe(view::showLastLocation, throwable -> {
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
    Disposable disposable = Observable.zip(
        geocoderRepository.getFromLocationName(query, lowerLeft, upperRight),
        getPlacesFromLocationName(query, lowerLeft, upperRight), this::getMergedList)
        .subscribeOn(Schedulers.io())
        .observeOn(scheduler)
        .retry(RETRY_COUNT)
        .subscribe(view::showLocations, throwable -> view.showLoadLocationError(),
            view::didLoadLocation);
    compositeDisposable.add(disposable);
  }

  public void getDebouncedFromLocationName(String query, int debounceTime) {
    view.willLoadLocation();
    Disposable disposable = geocoderRepository.getFromLocationName(query)
        .debounce(debounceTime, TimeUnit.MILLISECONDS, Schedulers.io())
        .observeOn(scheduler)
        .subscribe(view::showDebouncedLocations, throwable -> view.showLoadLocationError(),
            view::didLoadLocation);
    compositeDisposable.add(disposable);
  }

  public void getDebouncedFromLocationName(String query, LatLng lowerLeft, LatLng upperRight, int debounceTime) {
    view.willLoadLocation();
    Disposable disposable = Observable.zip(
        geocoderRepository.getFromLocationName(query, lowerLeft, upperRight),
        getPlacesFromLocationName(query, lowerLeft, upperRight), this::getMergedList)
        .subscribeOn(Schedulers.io())
        .debounce(debounceTime, TimeUnit.MILLISECONDS, Schedulers.io())
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

  public void enableGooglePlaces() {
    this.isGooglePlacesEnabled = true;
  }

  private Observable<List<Address>> getPlacesFromLocationName(String query, LatLng lowerLeft, LatLng upperRight) {
    return isGooglePlacesEnabled ? googlePlacesDataSource.getFromLocationName(query, new LatLngBounds(lowerLeft, upperRight))
        .flatMapIterable(addresses -> addresses).take(MAX_PLACES_RESULTS).toList().toObservable()
        .onErrorReturnItem(new ArrayList<>()) : Observable.just(new ArrayList<>());
  }

  @NonNull
  private List<Address> getMergedList(List<Address> geocoderList, List<Address> placesList) {
    List<Address> mergedList = new ArrayList<>();
    mergedList.addAll(geocoderList);
    mergedList.addAll(placesList);
    return mergedList;
  }
}
