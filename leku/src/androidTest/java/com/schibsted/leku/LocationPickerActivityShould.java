package com.schibsted.leku;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.provider.Settings;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.filters.FlakyTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.rule.GrantPermissionRule;
import android.util.Log;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import com.schibstedspain.leku.LocationPickerActivity;
import com.schibstedspain.leku.R;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import util.PermissionGranter;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.InstrumentationRegistry.getTargetContext;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.pressImeActionButton;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasImeAction;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

public class LocationPickerActivityShould {
  private PermissionGranter permissionGranter;

  @Rule public ActivityTestRule<LocationPickerActivity> activityRule =
      new ActivityTestRule<>(LocationPickerActivity.class, true, false);

  @Rule public GrantPermissionRule runtimePermissionRule = GrantPermissionRule.grant(Manifest.permission.ACCESS_FINE_LOCATION,
      Manifest.permission.WRITE_SECURE_SETTINGS);

  @Before
  public void setup() {
    permissionGranter = new PermissionGranter();
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      getInstrumentation().getUiAutomation().executeShellCommand(
          "pm grant " + getTargetContext().getPackageName()
              + " android.permission.WRITE_SECURE_SETTINGS");
    }
  }

  private void unlockScreen() {
    final Activity activity = activityRule.getActivity();
    activity.runOnUiThread(
        () -> activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
            | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON));
  }

  private void dissableAnimationsOnTravis() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      Settings.Global.putFloat(activityRule.getActivity().getContentResolver(),
          Settings.Global.ANIMATOR_DURATION_SCALE, 0f);
    }
  }

  @Test
  @FlakyTest
  public void showMapWhenTheActivityStarts() throws Exception {
    launchActivityWithPermissionsGranted();

    onView(withId(R.id.map)).check(matches(isDisplayed()));
  }

  @Test
  @FlakyTest
  public void showLocationInfoWhenTheActivityStartsAndHasALocationProvided() throws Exception {
    launchActivityWithPermissionsGranted();
    wait300millis();
    wait300millis();

    assertLocationInfoIsShown();
  }

  @Test
  @Ignore("it needs the map to be shown, it probably means the maps api key")
  @FlakyTest
  public void showLocationInfoWhenClickingTheMapAndNewLocationIsSelected() throws Exception {
    launchActivityWithPermissionsGranted();
    onView(withId(R.id.map)).perform(click());
    wait300millis();

    assertLocationInfoIsShown();
  }

  @Test
  @FlakyTest
  public void showSuggestedLocationListWhenATextSearchIsPerformed() throws Exception {
    launchActivityWithPermissionsGranted();
    wait300millis();
    onView(withId(R.id.leku_search)).perform(typeText("calle mallorca"));
    onView(withId(R.id.leku_search))
        .check(matches(hasImeAction(EditorInfo.IME_ACTION_SEARCH)))
        .perform(pressImeActionButton());
    wait300millis();
    wait300millis();
    wait300millis();

    onView(withId(R.id.resultlist)).check(matches(isDisplayed()));
  }

  @Test
  @Ignore("seems to be VERY flacky")
  @FlakyTest
  public void notCrashWhenLaunchingActivityAndRotatingTheScreenSeveralTimes() throws Exception {
    launchActivityWithPermissionsGranted();
    wait300millis();

    activityRule.getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    assertLocationInfoIsShown();
    activityRule.getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    assertLocationInfoIsShown();
    activityRule.getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    assertLocationInfoIsShown();
    activityRule.getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    assertLocationInfoIsShown();
  }

  @Test
  @FlakyTest
  public void notCrashWhenLaunchingActivityAndPermissionsAreNotGranted() throws Exception {
    launchActivityWithoutLocationAndPermissions();
    wait300millis();

    onView(withId(R.id.map)).check(matches(isDisplayed()));
  }

  @Test
  @FlakyTest
  public void notCrashWhenPermissionsAreNotGrantedAndClickToFloatingAction() throws Exception {
    launchActivityWithoutLocationAndPermissions();
    wait300millis();

    onView(withId(R.id.btnFloatingAction)).perform(click());
  }

  @Test
  @Ignore("seems to be VERY flacky")
  @FlakyTest
  public void hideStreetTextWhenALocationIsSelectedAndStreetTextViewIsHiddenByBundle() throws Exception {
    launchActivityWithPermissionsGranted();
    wait300millis();

    onView(withId(R.id.street)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.INVISIBLE)));
  }

  @Test
  @FlakyTest
  public void showStreetAndZipCodeTextWhenALocationIsSelected() throws Exception {
    launchActivityWithPermissionsGranted();
    wait300millis();

    onView(withId(R.id.city)).check(matches(isDisplayed()));
    onView(withId(R.id.zipCode)).check(matches(isDisplayed()));
  }

  private void assertLocationInfoIsShown() {
    onView(withId(R.id.location_info)).check(matches(isDisplayed()));
    onView(withId(R.id.btnAccept)).check(matches(isDisplayed()));
    onView(withId(R.id.btnFloatingAction)).check(matches(isDisplayed()));
  }

  private void launchActivity() {
    Context targetContext = getInstrumentation().getTargetContext();
    Intent intent = new Intent(targetContext, LocationPickerActivity.class);
    intent.putExtra(LocationPickerActivity.LATITUDE, 41.4036299);
    intent.putExtra(LocationPickerActivity.LONGITUDE, 2.1743558);
    intent.putExtra(LocationPickerActivity.LAYOUTS_TO_HIDE, "street");
    intent.putExtra(LocationPickerActivity.SEARCH_ZONE, "es_ES");
    intent.putExtra("test", "this is a test");
    activityRule.launchActivity(intent);

    unlockScreen();
    dissableAnimationsOnTravis();
  }

  private void launchActivityWithoutLocation() {
    Context targetContext = getInstrumentation().getTargetContext();
    Intent intent = new Intent(targetContext, LocationPickerActivity.class);
    activityRule.launchActivity(intent);

    unlockScreen();
    dissableAnimationsOnTravis();
  }

  private void launchActivityWithPermissionsGranted() {
    launchActivity();

    permissionGranter.allowPermissionsIfNeeded(activityRule.getActivity(),
        android.Manifest.permission.ACCESS_FINE_LOCATION);
  }

  private void launchActivityWithoutLocationAndPermissions() {
    launchActivityWithoutLocation();

    permissionGranter.denyPermissionsIfNeeded(activityRule.getActivity(),
        android.Manifest.permission.ACCESS_FINE_LOCATION);
  }

  private void wait300millis() {
    try {
      Thread.sleep(300);
    } catch (InterruptedException e) {
      Log.d(LocationPickerActivityShould.class.getName(), e.getMessage());
    }
  }
}
