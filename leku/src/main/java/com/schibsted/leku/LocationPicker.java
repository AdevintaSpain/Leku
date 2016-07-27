package com.schibsted.leku;

import com.schibsted.leku.tracker.LocationPickerTracker;

public class LocationPicker {

  private static final LocationPickerTracker EMPTY_TRACKER = eventName -> {
  };

  private static LocationPickerTracker tracker = EMPTY_TRACKER;

  public static void setTracker(LocationPickerTracker tracker) {
    if (tracker == null) {
      throw new IllegalArgumentException("The LocationPickerTracker instance can't be null.");
    }
    LocationPicker.tracker = tracker;
  }

  public static LocationPickerTracker getTracker() {
    if (tracker == null) {
      tracker = EMPTY_TRACKER;
    }
    return tracker;
  }

  public static void reset() {
    tracker = EMPTY_TRACKER;
  }
}
