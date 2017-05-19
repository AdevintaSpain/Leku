package com.schibstedspain.leku.timezone;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.google.maps.GeoApiContext;
import com.google.maps.TimeZoneApi;
import com.google.maps.errors.ApiException;
import com.google.maps.model.LatLng;

import java.io.IOException;
import java.util.TimeZone;

public class TimeZoneUtils {
  public static TimeZone getTimeZone(Context context, double lng, double lat) {
    String api_key = getGoogleMetadata(context);
    if (api_key == null || api_key.length() <= 0) {
      Log.d(TimeZoneUtils.class.getName(), "No Google Maps api specified");
      return null;
    }

    GeoApiContext geoContext = new GeoApiContext().setApiKey(api_key);
    try {
      return TimeZoneApi.getTimeZone(geoContext, new LatLng(lng, lat)).await();
    } catch (ApiException e) {
      Log.d(TimeZoneUtils.class.getName(), e.getMessage());
    } catch (InterruptedException e) {
      Log.d(TimeZoneUtils.class.getName(), e.getMessage());
    } catch (IOException e) {
      Log.d(TimeZoneUtils.class.getName(), e.getMessage());
    }
    return null;
  }

  private static String getGoogleMetadata(Context context) {
    try {
      ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(
          context.getPackageName(), PackageManager.GET_META_DATA);
      if (appInfo.metaData != null) {
        return appInfo.metaData.getString("com.google.android.maps.v2.API_KEY");
      }
    } catch (PackageManager.NameNotFoundException e) {
      Log.d(TimeZoneUtils.class.getName(), e.getMessage());
    }
    return null;
  }
}
