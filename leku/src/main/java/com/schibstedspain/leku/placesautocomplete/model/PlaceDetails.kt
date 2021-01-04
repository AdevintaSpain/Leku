package com.schibstedspain.leku.placesautocomplete.model




data class PlaceDetails(
        val name: String,
        val address: ArrayList<Address>,
        val lat: Double,
        val lng: Double,
        val placeId: String,
        val url: String,
        val utcOffset: Int,
        val vicinity: String,
        val compoundPlusCode: String,
        val globalPlusCode: String
)