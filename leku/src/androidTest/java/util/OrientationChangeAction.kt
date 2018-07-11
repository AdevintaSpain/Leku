package util

import android.app.Activity
import android.content.pm.ActivityInfo
import android.support.test.espresso.UiController
import android.support.test.espresso.ViewAction
import android.util.Log
import android.view.View
import org.hamcrest.Matcher

import android.support.test.espresso.matcher.ViewMatchers.isRoot

class OrientationChangeAction private constructor(private val orientation: Int) : ViewAction {

    override fun getConstraints(): Matcher<View> {
        return isRoot()
    }

    override fun getDescription(): String {
        return "Changes orientation to $orientation"
    }

    override fun perform(uiController: UiController, view: View) {
        try {
            Thread.sleep(500)
        } catch (e: InterruptedException) {
            Log.d(OrientationChangeAction::class.java.name, e.message)
        }

        uiController.loopMainThreadUntilIdle()
        val activity = view.context as Activity
        activity.requestedOrientation = orientation
    }

    companion object {

        fun orientationLandscape(): ViewAction {
            return OrientationChangeAction(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
        }

        fun orientationPortrait(): ViewAction {
            return OrientationChangeAction(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
        }
    }
}