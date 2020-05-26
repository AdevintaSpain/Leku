package com.schibstedspain.leku.utils;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.Arrays;
import java.util.List;

import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.disposables.Disposables;
import io.reactivex.functions.Action;

public abstract class BaseObservableOnSubscribe<T> implements ObservableOnSubscribe<T> {
    private final Context ctx;
    private final List<Api<? extends Api.ApiOptions.NotRequiredOptions>> services;

    @SafeVarargs
    protected BaseObservableOnSubscribe(Context ctx, Api<? extends Api.ApiOptions.NotRequiredOptions>... services) {
        this.ctx = ctx;
        this.services = Arrays.asList(services);
    }

    @Override
    public void subscribe(ObservableEmitter<T> emitter) throws Exception {
        final GoogleApiClient apiClient = createApiClient(emitter);
        try {
            apiClient.connect();
        } catch (Throwable ex) {
            if (!emitter.isDisposed()) {
                emitter.onError(ex);
            }
        }

        emitter.setDisposable(Disposables.fromAction(new Action() {
            @Override
            public void run() throws Exception {
                onDisposed();
                apiClient.disconnect();
            }
        }));
    }

    private GoogleApiClient createApiClient(ObservableEmitter<? super T> emitter) {
        ApiClientConnectionCallbacks apiClientConnectionCallbacks = new ApiClientConnectionCallbacks(ctx, emitter);
        GoogleApiClient.Builder apiClientBuilder = new GoogleApiClient.Builder(ctx);

        for (Api<? extends Api.ApiOptions.NotRequiredOptions> service : services) {
            apiClientBuilder = apiClientBuilder.addApi(service);
        }

        apiClientBuilder = apiClientBuilder
                .addConnectionCallbacks(apiClientConnectionCallbacks)
                .addOnConnectionFailedListener(apiClientConnectionCallbacks);

        GoogleApiClient apiClient = apiClientBuilder.build();
        apiClientConnectionCallbacks.setClient(apiClient);
        return apiClient;
    }

    protected void onDisposed() {
    }

    protected abstract void onGoogleApiClientReady(Context context, GoogleApiClient googleApiClient, ObservableEmitter<? super T> emitter);

    private class ApiClientConnectionCallbacks implements
            GoogleApiClient.ConnectionCallbacks,
            GoogleApiClient.OnConnectionFailedListener {

        final private Context context;

        final private ObservableEmitter<? super T> emitter;

        private GoogleApiClient apiClient;

        private ApiClientConnectionCallbacks(Context context, ObservableEmitter<? super T> emitter) {
            this.context = context;
            this.emitter = emitter;
        }

        @Override
        public void onConnected(Bundle bundle) {
            try {
                onGoogleApiClientReady(context, apiClient, emitter);
            } catch (Throwable ex) {
                if (!emitter.isDisposed()) {
                    emitter.onError(ex);
                }
            }
        }

        @Override
        public void onConnectionSuspended(int cause) {
            if (!emitter.isDisposed()) {
                emitter.onError(new IllegalStateException());
            }
        }

        @Override
        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
            if (!emitter.isDisposed()) {
                emitter.onError(new IllegalStateException("Error connecting to GoogleApiClient"));
            }
        }

        void setClient(GoogleApiClient client) {
            this.apiClient = client;
        }
    }
}
