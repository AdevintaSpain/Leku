package com.schibstedspain.leku

import com.google.android.gms.maps.model.LatLng
import java.util.Locale

internal object CountryLocaleRect {
    private val US_LOWER_LEFT = LatLng(16.132785, -168.372760)
    private val US_UPPER_RIGHT = LatLng(72.344643, -47.598995)

    private val UK_LOWER_LEFT = LatLng(49.495463, -8.392245)
    private val UK_UPPER_RIGHT = LatLng(59.409006, 2.652597)

    private val FRANCE_LOWER_LEFT = LatLng(42.278589, -5.631326)
    private val FRANCE_UPPER_RIGHT = LatLng(51.419246, 9.419559)

    private val ITALY_LOWER_LEFT = LatLng(36.072602, 6.344287)
    private val ITALY_UPPER_RIGHT = LatLng(47.255500, 19.209133)

    private val GERMANY_LOWER_LEFT = LatLng(47.103880, 5.556203)
    private val GERMANY_UPPER_RIGHT = LatLng(55.204320, 15.453816)

    private val GERMAN_LOWER_LEFT = LatLng(45.875834, 6.235783)   // DACH Region
    private val GERMAN_UPPER_RIGHT = LatLng(55.130976, 16.922589) // DACH Region

    private val UAE_LOWER_LEFT = LatLng(22.523123, 51.513718)  // United Arab Emirates
    private val UAE_UPPER_RIGHT = LatLng(26.188523, 56.568692) // United Arab Emirates
    private const val UAE_COUNTRY_CODE = "ar_ae"

    private val INDIA_LOWER_LEFT = LatLng(5.445640, 67.487799)
    private val INDIA_UPPER_RIGHT = LatLng(37.691225, 90.413055)
    private const val INDIA_COUNTRY_CODE = "en_in"

    private val SPAIN_LOWER_LEFT = LatLng(26.525467, -18.910366)
    private val SPAIN_UPPER_RIGHT = LatLng(43.906271, 5.394197)
    private const val SPAIN_COUNTRY_CODE = "es_es"

    private val PAKISTAN_LOWER_LEFT = LatLng(22.895428, 60.201233)
    private val PAKISTAN_UPPER_RIGHT = LatLng(37.228272, 76.918031)
    private const val PAKISTAN_COUNTRY_CODE = "en_pk"

    private val VIETNAM_LOWER_LEFT = LatLng(6.997486, 101.612789)
    private val VIETNAM_UPPER_RIGHT = LatLng(24.151926, 110.665524)
    private const val VIETNAM_COUNTRY_CODE = "vi_VN"

    val defaultLowerLeft: LatLng?
        get() = getLowerLeftFromZone(Locale.getDefault())

    val defaultUpperRight: LatLng?
        get() = getUpperRightFromZone(Locale.getDefault())

    fun getLowerLeftFromZone(locale: Locale): LatLng? {
        return when {
            Locale.US == locale -> US_LOWER_LEFT
            Locale.UK == locale -> UK_LOWER_LEFT
            Locale.FRANCE == locale -> FRANCE_LOWER_LEFT
            Locale.ITALY == locale -> ITALY_LOWER_LEFT
            Locale.GERMANY == locale -> GERMANY_LOWER_LEFT
            Locale.GERMAN == locale -> GERMAN_LOWER_LEFT
            locale.toString().equals(UAE_COUNTRY_CODE, ignoreCase = true) -> UAE_LOWER_LEFT
            locale.toString().equals(INDIA_COUNTRY_CODE, ignoreCase = true) -> INDIA_LOWER_LEFT
            locale.toString().equals(SPAIN_COUNTRY_CODE, ignoreCase = true) -> SPAIN_LOWER_LEFT
            locale.toString().equals(PAKISTAN_COUNTRY_CODE, ignoreCase = true) -> PAKISTAN_LOWER_LEFT
            locale.toString().equals(VIETNAM_COUNTRY_CODE, ignoreCase = true) -> VIETNAM_LOWER_LEFT
            else -> null
        }
    }

    fun getUpperRightFromZone(locale: Locale): LatLng? {
        return when {
            Locale.US == locale -> US_UPPER_RIGHT
            Locale.UK == locale -> UK_UPPER_RIGHT
            Locale.FRANCE == locale -> FRANCE_UPPER_RIGHT
            Locale.ITALY == locale -> ITALY_UPPER_RIGHT
            Locale.GERMANY == locale -> GERMANY_UPPER_RIGHT
            Locale.GERMAN == locale -> GERMAN_UPPER_RIGHT
            locale.toString().equals(UAE_COUNTRY_CODE, ignoreCase = true) -> UAE_UPPER_RIGHT
            locale.toString().equals(INDIA_COUNTRY_CODE, ignoreCase = true) -> INDIA_UPPER_RIGHT
            locale.toString().equals(SPAIN_COUNTRY_CODE, ignoreCase = true) -> SPAIN_UPPER_RIGHT
            locale.toString().equals(PAKISTAN_COUNTRY_CODE, ignoreCase = true) -> PAKISTAN_UPPER_RIGHT
            locale.toString().equals(VIETNAM_COUNTRY_CODE, ignoreCase = true) -> VIETNAM_UPPER_RIGHT
            else -> null
        }
    }
}
