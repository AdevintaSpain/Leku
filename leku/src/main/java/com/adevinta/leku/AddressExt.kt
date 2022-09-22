package com.adevinta.leku

import android.content.Context
import android.location.Address

fun Address.getFullAddressString(context: Context): String {
    if (featureName == null) {
        return context.getString(R.string.leku_unknown_location)
    }
    var fullAddress = getAddressLine(0)
    if (fullAddress.isNullOrEmpty()) {
        fullAddress = ""
        featureName?.let {
            fullAddress += it
        }
        if (subLocality != null && subLocality.isNotEmpty()) {
            fullAddress += ", $subLocality"
        }
        if (locality != null && locality.isNotEmpty()) {
            fullAddress += ", $locality"
        }
        if (countryName != null && countryName.isNotEmpty()) {
            fullAddress += ", $countryName"
        }
    }
    return fullAddress
}
