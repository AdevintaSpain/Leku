package com.schibsted.leku;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import com.schibstedspain.leku.LocationPickerActivity;
import com.schibstedspain.leku.R;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import util.PermissionGranter;

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

  @Before
  public void setup() {
    permissionGranter = new PermissionGranter();
  }

  @Test
  public void showMapWhenTheActivityStarts() throws Exception {
    launchActivityWithPermissionsGranted();

    onView(withId(R.id.map)).check(matches(isDisplayed()));
  }

  @Test
  public void showLocationInfoWhenTheActivityStartsAndHasALocationProvided() throws Exception {
    launchActivityWithPermissionsGranted();
    wait300millis();

    assertLocationInfoIsShown();
  }

  @Test
  @Ignore("it needs the map to be shown, it probably means the maps api key")
  public void showLocationInfoWhenClickingTheMapAndNewLocationIsSelected() throws Exception {
    launchActivityWithPermissionsGranted();
    onView(withId(R.id.map)).perform(click());
    wait300millis();

    assertLocationInfoIsShown();
  }

  @Test
  public void showSuggestedLocationListWhenATextSearchIsPerformed() throws Exception {
    launchActivityWithPermissionsGranted();
    wait300millis();
    onView(withId(R.id.leku_search)).perform(typeText("calle mallorca"));
    onView(withId(R.id.leku_search))
        .check(matches(hasImeAction(EditorInfo.IME_ACTION_SEARCH)))
        .perform(pressImeActionButton());
    wait300millis();

    onView(withId(R.id.resultlist)).check(matches(isDisplayed()));
  }

  @Test
  @Ignore("seems to be VERY flacky")
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
  public void notCrashWhenLaunchingActivityAndPermissionsAreNotGranted() throws Exception {
    launchActivityWithoutLocationAndPermissions();
    wait300millis();

    onView(withId(R.id.map)).check(matches(isDisplayed()));
  }

  @Test
  public void notCrashWhenPermissionsAreNotGrantedAndClickToFloatingAction() throws Exception {
    launchActivityWithoutLocationAndPermissions();
    wait300millis();

    onView(withId(R.id.btnFloatingAction)).perform(click());
  }

  @Test
  public void hideStreetTextWhenALocationIsSelectedAndStreetTextViewIsHiddenByBundle() throws Exception {
    launchActivityWithPermissionsGranted();
    wait300millis();

    onView(withId(R.id.street)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.INVISIBLE)));
  }

  @Test
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
    Context targetContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
    Intent intent = new Intent(targetContext, LocationPickerActivity.class);
    intent.putExtra(LocationPickerActivity.LATITUDE, 41.4036299);
    intent.putExtra(LocationPickerActivity.LONGITUDE, 2.1743558);
    intent.putExtra(LocationPickerActivity.LAYOUTS_TO_HIDE, "street");
    intent.putExtra(LocationPickerActivity.SEARCH_ZONE, "es_ES");
    intent.putExtra("test", "this is a test");
    activityRule.launchActivity(intent);
  }

  private void launchActivityWithoutLocation() {
    Context targetContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
    Intent intent = new Intent(targetContext, LocationPickerActivity.class);
    activityRule.launchActivity(intent);
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
