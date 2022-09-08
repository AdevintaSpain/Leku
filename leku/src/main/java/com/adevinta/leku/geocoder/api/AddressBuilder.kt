package com.adevinta.leku.geocoder.api

import android.location.Address
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.Locale
import kotlin.collections.ArrayList

class AddressBuilder {

    @Throws(JSONException::class)
    fun parseArrayResult(json: String): List<Address> {
        val addresses = ArrayList<Address>()
        val root = JSONObject(json)
        val results = root.getJSONArray("results")
        for (i in 0 until results.length()) {
            addresses.add(parseAddress(results.getJSONObject(i)))
        }
        return addresses
    }

    @Throws(JSONException::class)
    fun parseResult(json: String): Address {
        val root = JSONObject(json)
        val result = root.getJSONObject("result")
        return parseAddress(result)
    }

    @Throws(JSONException::class)
    private fun parseAddress(jsonObject: JSONObject): Address {
        val location = jsonObject.getJSONObject("geometry").getJSONObject("location")
        val latitude = location.getDouble("lat")
        val longitude = location.getDouble("lng")
        val components = getAddressComponents(jsonObject.getJSONArray("address_components"))
        var postalCode: String? = ""
        var city: String? = ""
        var number: String? = ""
        var street: String? = ""

        for (component in components) {
            component.types?.let {
                if (it.contains("postal_code")) {
                    postalCode = component.name
                }
                if (it.contains("locality")) {
                    city = component.name
                }
                if (it.contains("street_number")) {
                    number = component.name
                }
                if (it.contains("route")) {
                    street = component.name
                }
            }
        }

        val fullAddress = getFullAddress(street, number)

        val address = Address(Locale.getDefault())
        address.latitude = latitude
        address.longitude = longitude
        address.postalCode = postalCode
        address.setAddressLine(0, fullAddress.toString())
        address.setAddressLine(1, postalCode)
        address.setAddressLine(2, city)
        address.locality = city
        return address
    }

    @Throws(JSONException::class)
    private fun getAddressComponents(jsonComponents: JSONArray): List<AddressComponent> {
        val components = ArrayList<AddressComponent>()
        for (i in 0 until jsonComponents.length()) {
            val component = AddressComponent()
            val jsonComponent = jsonComponents.getJSONObject(i)
            component.name = jsonComponent.getString("long_name")
            component.types = ArrayList()
            val jsonTypes = jsonComponent.getJSONArray("types")
            for (j in 0 until jsonTypes.length()) {
                component.types!!.add(jsonTypes.getString(j))
            }
            components.add(component)
        }
        return components
    }

    private fun getFullAddress(street: String?, number: String?): StringBuilder {
        val fullAddress = StringBuilder()
        fullAddress.append(street)
        if (!street.isNullOrEmpty() && !number.isNullOrEmpty()) {
            fullAddress.append(", ").append(number)
        }
        return fullAddress
    }

    private class AddressComponent {
        internal var name: String? = null
        internal var types: MutableList<String>? = null
    }
}
