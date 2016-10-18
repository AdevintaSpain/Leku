package com.schibstedspain.leku.permissions;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

public class PermissionUtils {
  public static boolean shouldRequestPermission(Context context, String permission) {
    return ContextCompat.checkSelfPermission(context, permission)
        == PackageManager.PERMISSION_DENIED;
  }
  public static boolean isLocationPermissionGranted(Context context) {
    return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        == PackageManager.PERMISSION_GRANTED;
  }

  public static void requestPermission(Activity activity, String permission, int requestCode) {
    ActivityCompat.requestPermissions(activity, new String[] {permission}, requestCode);
  }

  public static boolean shouldRequestLocationStoragePermission(Context context) {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && shouldRequestPermission(context,
        Manifest.permission.ACCESS_FINE_LOCATION);
  }

  public static void requestLocationPermission(Activity activity) {
    requestPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION, 0);
  }
}
