package com.adevinta.leku.locale

import android.os.Parcel
import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng

data class SearchZoneRect(val lowerLeft: LatLng, val upperRight: LatLng) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readParcelable(LatLng::class.java.classLoader)!!,
        parcel.readParcelable(LatLng::class.java.classLoader)!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(lowerLeft, flags)
        parcel.writeParcelable(upperRight, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SearchZoneRect> {
        override fun createFromParcel(parcel: Parcel): SearchZoneRect {
            return SearchZoneRect(parcel)
        }

        override fun newArray(size: Int): Array<SearchZoneRect?> {
            return arrayOfNulls(size)
        }
    }
}
