package com.schibstedspain.leku.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;

import androidx.annotation.NonNull;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;

public class LastKnownLocationObservableOnSubscribe extends BaseLocationObservableOnSubscribe<Location> {

    public static Observable<Location> createObservable(Context ctx) {
        return Observable.create(new LastKnownLocationObservableOnSubscribe(ctx));
    }

    private LastKnownLocationObservableOnSubscribe(Context ctx) {
        super(ctx);
    }

    @SuppressLint("MissingPermission")
    @Override
    protected void onLocationProviderClientReady(FusedLocationProviderClient locationProviderClient,
                                                 final ObservableEmitter<? super Location> emitter) {
        locationProviderClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (emitter.isDisposed()) return;
                        if (location != null) {
                            emitter.onNext(location);
                        }
                        emitter.onComplete();
                    }
                })
                .addOnFailureListener(new BaseFailureListener<>(emitter));
    }

    public static class BaseFailureListener<T> implements OnFailureListener {

        private final ObservableEmitter<? super T> emitter;

        public BaseFailureListener(ObservableEmitter<? super T> emitter) {
            this.emitter = emitter;
        }

        @Override
        public void onFailure(@NonNull Exception exception) {
            if (emitter.isDisposed()) return;
            emitter.onError(exception);
            emitter.onComplete();
        }
    }
}
