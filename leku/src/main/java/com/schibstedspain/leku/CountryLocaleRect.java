package com.schibstedspain.leku;

import com.google.android.gms.maps.model.LatLng;
import java.util.Locale;

class CountryLocaleRect {
  private static final LatLng US_LOWER_LEFT = new LatLng(16.132785, -168.372760);
  private static final LatLng US_UPPER_RIGHT = new LatLng(72.344643, -47.598995);

  private static final LatLng UK_LOWER_LEFT = new LatLng(49.495463, -8.392245);
  private static final LatLng UK_UPPER_RIGHT = new LatLng(59.409006, 2.652597);

  private static final LatLng FRANCE_LOWER_LEFT = new LatLng(42.278589, -5.631326);
  private static final LatLng FRANCE_UPPER_RIGHT = new LatLng(51.419246, 9.419559);

  private static final LatLng ITALY_LOWER_LEFT = new LatLng(36.072602, 6.344287);
  private static final LatLng ITALY_UPPER_RIGHT = new LatLng(47.255500, 19.209133);

  private static final LatLng GERMANY_LOWER_LEFT = new LatLng(47.103880, 5.556203);
  private static final LatLng GERMANY_UPPER_RIGHT = new LatLng(55.204320, 15.453816);

  private static final LatLng GERMAN_LOWER_LEFT = new LatLng(45.875834, 6.235783);   // DACH Region
  private static final LatLng GERMAN_UPPER_RIGHT = new LatLng(55.130976, 16.922589); // DACH Region

  private static final LatLng UAE_LOWER_LEFT = new LatLng(22.523123, 51.513718);  // United Arab Emirates
  private static final LatLng UAE_UPPER_RIGHT = new LatLng(26.188523, 56.568692); // United Arab Emirates
  private static final String UAE_COUNTRY_CODE = "ar_ae";

  private static final LatLng INDIA_LOWER_LEFT = new LatLng(5.445640, 67.487799);
  private static final LatLng INDIA_UPPER_RIGHT = new LatLng(37.691225, 90.413055);
  private static final String INDIA_COUNTRY_CODE = "en_in";

  private static final LatLng SPAIN_LOWER_LEFT = new LatLng(26.525467, -18.910366);
  private static final LatLng SPAIN_UPPER_RIGHT = new LatLng(43.906271, 5.394197);
  private static final String SPAIN_COUNTRY_CODE = "es_es";

  private static final LatLng PAKISTAN_LOWER_LEFT = new LatLng(22.895428, 60.201233);
  private static final LatLng PAKISTAN_UPPER_RIGHT = new LatLng(37.228272, 76.918031);
  private static final String PAKISTAN_COUNTRY_CODE = "en_pk";

  static LatLng getDefaultLowerLeft() {
    return getLowerLeftFromZone(Locale.getDefault());
  }

  static LatLng getDefaultUpperRight() {
    return getUpperRightFromZone(Locale.getDefault());
  }

  static LatLng getLowerLeftFromZone(Locale locale) {
    if (Locale.US.equals(locale)) {
      return US_LOWER_LEFT;
    } else if (Locale.UK.equals(locale)) {
      return UK_LOWER_LEFT;
    } else if (Locale.FRANCE.equals(locale)) {
      return FRANCE_LOWER_LEFT;
    } else if (Locale.ITALY.equals(locale)) {
      return ITALY_LOWER_LEFT;
    } else if (Locale.GERMANY.equals(locale)) {
      return GERMANY_LOWER_LEFT;
    } else if (Locale.GERMAN.equals(locale)) {
      return GERMAN_LOWER_LEFT;
    } else if (locale.toString().equalsIgnoreCase(UAE_COUNTRY_CODE)) {
      return UAE_LOWER_LEFT;
    } else if (locale.toString().equalsIgnoreCase(INDIA_COUNTRY_CODE)) {
      return INDIA_LOWER_LEFT;
    } else if (locale.toString().equalsIgnoreCase(SPAIN_COUNTRY_CODE)) {
      return SPAIN_LOWER_LEFT;
    } else if (locale.toString().equalsIgnoreCase(PAKISTAN_COUNTRY_CODE)) {
      return PAKISTAN_LOWER_LEFT;
    }
    return null;
  }

  static LatLng getUpperRightFromZone(Locale locale) {
    if (Locale.US.equals(locale)) {
      return US_UPPER_RIGHT;
    } else if (Locale.UK.equals(locale)) {
      return UK_UPPER_RIGHT;
    } else if (Locale.FRANCE.equals(locale)) {
      return FRANCE_UPPER_RIGHT;
    } else if (Locale.ITALY.equals(locale)) {
      return ITALY_UPPER_RIGHT;
    } else if (Locale.GERMANY.equals(locale)) {
      return GERMANY_UPPER_RIGHT;
    } else if (Locale.GERMAN.equals(locale)) {
      return GERMAN_UPPER_RIGHT;
    } else if (locale.toString().equalsIgnoreCase(UAE_COUNTRY_CODE)) {
      return UAE_UPPER_RIGHT;
    } else if (locale.toString().equalsIgnoreCase(INDIA_COUNTRY_CODE)) {
      return INDIA_UPPER_RIGHT;
    } else if (locale.toString().equalsIgnoreCase(SPAIN_COUNTRY_CODE)) {
      return SPAIN_UPPER_RIGHT;
    } else if (locale.toString().equalsIgnoreCase(PAKISTAN_COUNTRY_CODE)) {
      return PAKISTAN_UPPER_RIGHT;
    }
    return null;
  }
}
