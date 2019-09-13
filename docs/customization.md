##### Theming

This library uses AppCompat, so should use Theme.AppCompat or descendant in manifest.

```xml
<item name="colorPrimary">#E91E63</item>
<item name="colorPrimaryDark">#C51162</item>
<item name="colorAccent">#FBC02D</item>
<item name="colorControlActivated">#E91E63</item>
```

> `colorControlActivated` is used to colorize Street title, if not set, it uses colorAccent by default


To customize map, use:

```kotlin
.withMapStyle(R.raw.map_style_retro)
```

> Theme creator here: https://mapstyle.withgoogle.com/

##### Layout

It's possible to hide or show some of the information shown after selecting a location.
Using tha bundle parameter **LocationPickerActivity.LAYOUTS_TO_HIDE** you can change the visibility of the street, city or the zipcode.

```kotlin
intent.putExtra(LocationPickerActivity.LAYOUTS_TO_HIDE, "street|city|zipcode")
```

##### Search Zone

By default the search will be restricted to a zone determined by your default locale. If you want to force the search zone you can do it by adding this line with the locale preferred:

```kotlin
intent.putExtra(LocationPickerActivity.SEARCH_ZONE, "es_ES")
```

##### Search Zone Rect

If you want to force the search zone you can do it by adding this line with the lower left and upper right rect locations:

```kotlin
intent.putExtra(LocationPickerActivity.SEARCH_ZONE_RECT, SearchZoneRect(LatLng(26.525467, -18.910366), LatLng(43.906271, 5.394197)))
```

##### Default Search Zone Locale

If you want to be able to search with the default device locale, you can do it by adding this line:

```kotlin
intent.putExtra(LocationPickerActivity.SEARCH_ZONE_DEFAULT_LOCALE, true)
```

Note: If you don't specify any search zone it will not search using any default search zone. It will search on all around the world.

##### Force return location on back pressed

If you want to force that when the user clicks on back button it returns the location you can use this parameter (note: is only enabled if you don't provide a location):

```kotlin
intent.putExtra(LocationPickerActivity.BACK_PRESSED_RETURN_OK, true)
```

##### Enable/Disable the Satellite view

If you want to disable the satellite view button you can use this parameter (note: the satellite view is enabled by default):

```kotlin
intent.putExtra(LocationPickerActivity.ENABLE_SATELLITE_VIEW, false)
```

##### Enable/Disable requesting location permissions

If you want to disable asking for location permissions (and prevent any location requests)

```kotlin
intent.putExtra(LocationPickerActivity.ENABLE_LOCATION_PERMISSION_REQUEST, false)
```

##### Enable/Disable voice search

Now you can hide the voice search option on the search view

```kotlin
intent.putExtra(LocationPickerActivity.ENABLE_VOICE_SEARCH, false)
```

##### Hide/Show "Unnamed Road" on Address view

Now you can hide or show the text returned by the google service with "Unnamed Road" when no road name available

```kotlin
intent.putExtra(LocationPickerActivity.UNNAMED_ROAD_VISIBILITY, false)
```