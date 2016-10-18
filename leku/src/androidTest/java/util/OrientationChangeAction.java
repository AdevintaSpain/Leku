package util;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.util.Log;
import android.view.View;
import org.hamcrest.Matcher;

import static android.support.test.espresso.matcher.ViewMatchers.isRoot;

public class OrientationChangeAction implements ViewAction {
  private final int orientation;

  private OrientationChangeAction(int orientation) {
    this.orientation = orientation;
  }

  @Override
  public Matcher<View> getConstraints() {
    return isRoot();
  }

  @Override
  public String getDescription() {
    return "Changes orientation to " + orientation;
  }

  @Override
  public void perform(UiController uiController, View view) {
    try {
      Thread.sleep(500);
    } catch (InterruptedException e) {
      Log.d(OrientationChangeAction.class.getName(), e.getMessage());
    }
    uiController.loopMainThreadUntilIdle();
    final Activity activity = (Activity) view.getContext();
    activity.setRequestedOrientation(orientation);
  }

  public static ViewAction orientationLandscape() {
    return new OrientationChangeAction(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
  }

  public static ViewAction orientationPortrait() {
    return new OrientationChangeAction(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
  }
}