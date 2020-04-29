package util

import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.test.InstrumentationRegistry.getInstrumentation
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObjectNotFoundException
import androidx.test.uiautomator.UiSelector

private const val PERMISSIONS_DIALOG_DELAY = 3000
private const val GRANT_BUTTON_INDEX = 1
private const val DENY_BUTTON_INDEX = 0

class PermissionGranter {

    fun allowPermissionsIfNeeded(activity: Activity, permissionNeeded: String) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !hasNeededPermission(activity, permissionNeeded)) {
                waitInMillis(PERMISSIONS_DIALOG_DELAY.toLong())
                val device = UiDevice.getInstance(getInstrumentation())
                val allowPermissions = device.findObject(UiSelector().clickable(true).index(GRANT_BUTTON_INDEX))
                if (allowPermissions.exists()) {
                    allowPermissions.click()
                }
            }
        } catch (e: UiObjectNotFoundException) {
            logNoPermissionDialogError()
        }
    }

    fun denyPermissionsIfNeeded(activity: Activity, permissionNeeded: String) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !hasNeededPermission(activity, permissionNeeded)) {
                waitInMillis(PERMISSIONS_DIALOG_DELAY.toLong())
                val device = UiDevice.getInstance(getInstrumentation())
                val denyPermissions = device.findObject(UiSelector().clickable(true).index(DENY_BUTTON_INDEX))
                if (denyPermissions.exists()) {
                    denyPermissions.click()
                }
            }
        } catch (e: UiObjectNotFoundException) {
            logNoPermissionDialogError()
        }
    }

    private fun hasNeededPermission(activity: Activity, permissionNeeded: String): Boolean {
        val permissionStatus = ContextCompat.checkSelfPermission(activity, permissionNeeded)
        return permissionStatus == PackageManager.PERMISSION_GRANTED
    }

    private fun waitInMillis(millis: Long) {
        try {
            Thread.sleep(millis)
        } catch (e: InterruptedException) {
            Log.d(PermissionGranter::class.java.name, "Cannot execute Thread.sleep()")
        }
    }

    private fun logNoPermissionDialogError() {
        Log.d(PermissionGranter::class.java.name, "There is no permissions dialog to interact with")
    }
}
