package com.schibsted.leku;

import android.content.Context;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.View;
import com.schibstedspain.leku.LocationPickerActivity;
import com.schibstedspain.leku.R;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import util.PermissionGranter;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isRoot;
import static android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.allOf;
import static util.OrientationChangeAction.orientationLandscape;
import static util.OrientationChangeAction.orientationPortrait;

public class LocationPickerActivityShould {
  PermissionGranter permissionGranter;

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
    onView(withId(R.id.leku_search)).perform(setQuery("calle mallorca"));
    wait300millis();

    onView(withId(R.id.resultlist)).check(matches(isDisplayed()));
  }

  @Test
  public void notCrashWhenLaunchingActivityAndRotatingTheScreenSeveralTimes() throws Exception {
    launchActivityWithPermissionsGranted();
    wait300millis();

    onView(isRoot()).perform(orientationLandscape());
    onView(isRoot()).perform(orientationPortrait());
    onView(isRoot()).perform(orientationLandscape());
    onView(isRoot()).perform(orientationPortrait());
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

  private static ViewAction setQuery(final String text) {
    return new ViewAction() {
      @Override
      public Matcher<View> getConstraints() {
        return allOf(isDisplayed(), isAssignableFrom(SearchView.class));
      }

      @Override
      public String getDescription() {
        return "Change view text";
      }

      @Override
      public void perform(UiController uiController, View view) {
        ((SearchView) view).setQuery(text, true);
      }
    };
  }
}
