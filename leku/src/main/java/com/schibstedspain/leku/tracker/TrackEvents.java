package com.schibstedspain.leku.tracker;

public enum TrackEvents {
  googleApiConnectionFailed("Connection Failed"),
  startVoiceRecognitionActivityFailed("Start Voice Recognition Activity Failed"),
  didLoadLocationPicker("Location Picker"),
  didSearchLocations("Click on search for locations"),
  didLocalizeMe("Click on localize me"),
  didLocalizeByPoi("Long click on map"),
  simpleDidLocalizeByPoi("Click on map"),
  simpleDidLocalizeByLekuPoi("Click on POI"),
  RESULT_OK("Return location"),
  CANCEL("Return without location");

  private String eventName;

  TrackEvents(String eventName) {
    this.eventName = eventName;
  }

  public String getEventName() {
    return eventName;
  }
}
