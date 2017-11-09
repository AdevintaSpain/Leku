package util;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiSelector;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import static android.support.test.InstrumentationRegistry.getInstrumentation;

public class PermissionGranter {

  private static final int PERMISSIONS_DIALOG_DELAY = 3000;
  private static final int GRANT_BUTTON_INDEX = 1;
  private static final int DENY_BUTTON_INDEX = 0;

  public void allowPermissionsIfNeeded(Activity activity, String permissionNeeded) {
    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !hasNeededPermission(activity, permissionNeeded)) {
        waitInMillis(PERMISSIONS_DIALOG_DELAY);
        UiDevice device = UiDevice.getInstance(getInstrumentation());
        UiObject allowPermissions = device.findObject(new UiSelector().clickable(true).index(GRANT_BUTTON_INDEX));
        if (allowPermissions.exists()) {
          allowPermissions.click();
        }
      }
    } catch (UiObjectNotFoundException e) {
      logNoPermissionDialogError();
    }
  }

  public void denyPermissionsIfNeeded(Activity activity, String permissionNeeded) {
    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !hasNeededPermission(activity, permissionNeeded)) {
        waitInMillis(PERMISSIONS_DIALOG_DELAY);
        UiDevice device = UiDevice.getInstance(getInstrumentation());
        UiObject denyPermissions = device.findObject(new UiSelector().clickable(true).index(DENY_BUTTON_INDEX));
        if (denyPermissions.exists()) {
          denyPermissions.click();
        }
      }
    } catch (UiObjectNotFoundException e) {
      logNoPermissionDialogError();
    }
  }

  private boolean hasNeededPermission(Activity activity, String permissionNeeded) {
    int permissionStatus = ContextCompat.checkSelfPermission(activity, permissionNeeded);
    return permissionStatus == PackageManager.PERMISSION_GRANTED;
  }

  private void waitInMillis(long millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException e) {
      Log.d(PermissionGranter.class.getName(), "Cannot execute Thread.sleep()");
    }
  }

  private void logNoPermissionDialogError() {
    Log.d(PermissionGranter.class.getName(), "There is no permissions dialog to interact with");
  }
}
