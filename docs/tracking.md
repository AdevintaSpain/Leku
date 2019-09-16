Optionally, you can set a tracking events listener. Implement LocationPickerTracker interface, and set it in your Application class as follows:

```kotlin
LocationPicker.tracker = <<YourOwnTracker implementing LocationPickerTracker>>()
```
Available tracking events are:

|TAG|Message|
|---|---|
|GOOGLE_API_CONNECTION_FAILED|Connection Failed|
|START_VOICE_RECOGNITION_ACTIVITY_FAILED|Start Voice Recognition Activity Failed|
|ON_LOAD_LOCATION_PICKER|Location Picker|
|ON_SEARCH_LOCATIONS|Click on search for locations|
|ON_LOCALIZED_ME|Click on localize me|
|ON_LOCALIZED_BY_POI|Long click on map|
|SIMPLE_ON_LOCALIZE_BY_POI|Click on map|
|SIMPLE_ON_LOCALIZE_BY_LEKU_POI|Click on POI|
|RESULT_OK|Return location|
|CANCEL|Return without location|