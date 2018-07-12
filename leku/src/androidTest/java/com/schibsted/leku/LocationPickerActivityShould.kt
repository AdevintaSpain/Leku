package com.schibsted.leku

import android.Manifest
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Build
import android.provider.Settings
import android.support.test.espresso.matcher.ViewMatchers
import android.support.test.filters.FlakyTest
import android.support.test.rule.ActivityTestRule
import android.support.test.rule.GrantPermissionRule
import android.util.Log
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import com.schibstedspain.leku.LocationPickerActivity
import com.schibstedspain.leku.LATITUDE
import com.schibstedspain.leku.LONGITUDE
import com.schibstedspain.leku.LAYOUTS_TO_HIDE
import com.schibstedspain.leku.SEARCH_ZONE
import com.schibstedspain.leku.R
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import util.PermissionGranter

import android.support.test.InstrumentationRegistry.getInstrumentation
import android.support.test.InstrumentationRegistry.getTargetContext
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.action.ViewActions.pressImeActionButton
import android.support.test.espresso.action.ViewActions.typeText
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.hasImeAction
import android.support.test.espresso.matcher.ViewMatchers.isDisplayed
import android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import android.support.test.espresso.matcher.ViewMatchers.withId

class LocationPickerActivityShould {
    private var permissionGranter: PermissionGranter? = null

    @Rule @JvmField
    var activityRule = ActivityTestRule(LocationPickerActivity::class.java, true, false)

    @Rule @JvmField
    var runtimePermissionRule = GrantPermissionRule.grant(Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_SECURE_SETTINGS)!!

    @Before
    fun setup() {
        permissionGranter = PermissionGranter()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getInstrumentation().uiAutomation.executeShellCommand(
                    "pm grant " + getTargetContext().packageName + " android.permission.WRITE_SECURE_SETTINGS")
        }
    }

    private fun unlockScreen() {
        val activity = activityRule.activity
        activity.runOnUiThread {
            activity.window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                    or WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    private fun dissableAnimationsOnTravis() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Settings.Global.putFloat(activityRule.activity.contentResolver,
                    Settings.Global.ANIMATOR_DURATION_SCALE, 0f)
        }
    }

    @Test
    @FlakyTest
    @Throws(Exception::class)
    fun showMapWhenTheActivityStarts() {
        launchActivityWithPermissionsGranted()

        onView(withId(R.id.map)).check(matches(isDisplayed()))
    }

    @Test
    @FlakyTest
    @Throws(Exception::class)
    fun showLocationInfoWhenTheActivityStartsAndHasALocationProvided() {
        launchActivityWithPermissionsGranted()
        wait300millis()
        wait300millis()

        assertLocationInfoIsShown()
    }

    @Test
    @Ignore("it needs the map to be shown, it probably means the maps api key")
    @FlakyTest
    @Throws(Exception::class)
    fun showLocationInfoWhenClickingTheMapAndNewLocationIsSelected() {
        launchActivityWithPermissionsGranted()
        onView(withId(R.id.map)).perform(click())
        wait300millis()

        assertLocationInfoIsShown()
    }

    @Test
    @FlakyTest
    @Throws(Exception::class)
    fun showSuggestedLocationListWhenATextSearchIsPerformed() {
        launchActivityWithPermissionsGranted()
        wait300millis()
        onView(withId(R.id.leku_search)).perform(typeText("calle mallorca"))
        onView(withId(R.id.leku_search))
                .check(matches(hasImeAction(EditorInfo.IME_ACTION_SEARCH)))
                .perform(pressImeActionButton())
        wait300millis()
        wait300millis()
        wait300millis()

        onView(withId(R.id.resultlist)).check(matches(isDisplayed()))
    }

    @Test
    @Ignore("seems to be VERY flacky")
    @FlakyTest
    @Throws(Exception::class)
    fun notCrashWhenLaunchingActivityAndRotatingTheScreenSeveralTimes() {
        launchActivityWithPermissionsGranted()
        wait300millis()

        activityRule.activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        assertLocationInfoIsShown()
        activityRule.activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        assertLocationInfoIsShown()
        activityRule.activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        assertLocationInfoIsShown()
        activityRule.activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        assertLocationInfoIsShown()
    }

    @Test
    @FlakyTest
    @Throws(Exception::class)
    fun notCrashWhenLaunchingActivityAndPermissionsAreNotGranted() {
        launchActivityWithoutLocationAndPermissions()
        wait300millis()

        onView(withId(R.id.map)).check(matches(isDisplayed()))
    }

    @Test
    @FlakyTest
    @Throws(Exception::class)
    fun notCrashWhenPermissionsAreNotGrantedAndClickToFloatingAction() {
        launchActivityWithoutLocationAndPermissions()
        wait300millis()

        onView(withId(R.id.btnFloatingAction)).perform(click())
    }

    @Test
    @Ignore("seems to be VERY flacky")
    @FlakyTest
    @Throws(Exception::class)
    fun hideStreetTextWhenALocationIsSelectedAndStreetTextViewIsHiddenByBundle() {
        launchActivityWithPermissionsGranted()
        wait300millis()

        onView(withId(R.id.street)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.INVISIBLE)))
    }

    @Test
    @FlakyTest
    @Throws(Exception::class)
    fun showStreetAndZipCodeTextWhenALocationIsSelected() {
        launchActivityWithPermissionsGranted()
        wait300millis()

        onView(withId(R.id.city)).check(matches(isDisplayed()))
        onView(withId(R.id.zipCode)).check(matches(isDisplayed()))
    }

    private fun assertLocationInfoIsShown() {
        onView(withId(R.id.location_info)).check(matches(isDisplayed()))
        onView(withId(R.id.btnAccept)).check(matches(isDisplayed()))
        onView(withId(R.id.btnFloatingAction)).check(matches(isDisplayed()))
    }

    private fun launchActivity() {
        val targetContext = getInstrumentation().targetContext
        val intent = Intent(targetContext, LocationPickerActivity::class.java)
        intent.putExtra(LATITUDE, 41.4036299)
        intent.putExtra(LONGITUDE, 2.1743558)
        intent.putExtra(LAYOUTS_TO_HIDE, "street")
        intent.putExtra(SEARCH_ZONE, "es_ES")
        intent.putExtra("test", "this is a test")
        activityRule.launchActivity(intent)

        unlockScreen()
        dissableAnimationsOnTravis()
    }

    private fun launchActivityWithoutLocation() {
        val targetContext = getInstrumentation().targetContext
        val intent = Intent(targetContext, LocationPickerActivity::class.java)
        activityRule.launchActivity(intent)

        unlockScreen()
        dissableAnimationsOnTravis()
    }

    private fun launchActivityWithPermissionsGranted() {
        launchActivity()

        permissionGranter!!.allowPermissionsIfNeeded(activityRule.activity,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
    }

    private fun launchActivityWithoutLocationAndPermissions() {
        launchActivityWithoutLocation()

        permissionGranter!!.denyPermissionsIfNeeded(activityRule.activity,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
    }

    private fun wait300millis() {
        try {
            Thread.sleep(300)
        } catch (e: InterruptedException) {
            Log.d(LocationPickerActivityShould::class.java.name, e.message)
        }
    }
}
