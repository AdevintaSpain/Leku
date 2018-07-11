package com.schibstedspain.leku

import com.schibstedspain.leku.tracker.LocationPickerTracker
import com.schibstedspain.leku.tracker.TrackEvents

object LocationPicker {

    private val EMPTY_TRACKER = EmptyLocationPickerTracker()

    private var tracker: LocationPickerTracker = EMPTY_TRACKER

    fun setTracker(tracker: LocationPickerTracker?) {
        if (tracker == null) {
            throw IllegalArgumentException("The LocationPickerTracker instance can't be null.")
        }
        LocationPicker.tracker = tracker
    }

    fun getTracker(): LocationPickerTracker {
        return tracker
    }

    fun reset() {
        tracker = EMPTY_TRACKER
    }

    class EmptyLocationPickerTracker : LocationPickerTracker {
        override fun onEventTracked(event: TrackEvents) { }
    }
}
