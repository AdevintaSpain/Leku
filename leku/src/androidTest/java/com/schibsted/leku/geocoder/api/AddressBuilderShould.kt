package com.schibsted.leku.geocoder.api

import com.schibstedspain.leku.geocoder.api.AddressBuilder
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertTrue
import org.json.JSONException
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.junit.MockitoJUnit.rule

class AddressBuilderShould {

    @Rule @JvmField
    var mockitoRule = rule()!!

    private var addressBuilder: AddressBuilder? = null

    private val json: String
        get() = ("{\"results\": [{\"address_components\": [{\"long_name\": \"102\",\"short_name\": \"102\",\"types\": " +
                "[ \"street_number\"]},{\"long_name\": \"Carrer del Comte d'Urgell\",\"short_name\": \"Carrer del Comte d'Urgell\",\"types\": " +
                "[ \"route\"]},{\"long_name\": \"Barcelona\",\"short_name\": \"Barcelona\",\"types\": [ \"locality\", \"political\"]}," +
                "{\"long_name\": \"Barcelona\",\"short_name\": \"Barcelona\",\"types\": [ \"administrative_area_level_2\", \"political\"]}," +
                "{\"long_name\": \"Catalunya\",\"short_name\": \"CT\",\"types\": [ \"administrative_area_level_1\", \"political\"]}," +
                "{\"long_name\": \"Spain\",\"short_name\": \"ES\",\"types\": [ \"country\", \"political\"]},{\"long_name\": \"08011\"," +
                "\"short_name\": \"08011\",\"types\": [ \"postal_code\"]}],\"formatted_address\"" +
                ": \"Carrer del Comte d'Urgell, 102, 08011 Barcelona, Spain\",\"geometry\": {\"bounds\": {\"northeast\": " +
                "{ \"lat\": 41.3839416, \"lng\": 2.1570442},\"southwest\": { \"lat\": 41.3836653, \"lng\": 2.1566792}},\"location\": " +
                "{\"lat\": 41.3838035,\"lng\": 2.1568617},\"location_type\": \"ROOFTOP\",\"viewport\": {\"northeast\": " +
                "{ \"lat\": 41.3851524302915, \"lng\": 2.158210680291502},\"southwest\": { \"lat\": 41.3824544697085, " +
                "\"lng\": 2.155512719708498}}},\"partial_match\": true,\"place_id\": \"ChIJdehx-YiipBIR8hitzOckUuo\",\"types\": [\"premise\"] } " +
                "], \"status\": \"OK\"}")

    private val jsonForOnlyCity: String
        get() = ("{\"results\": [{\"address_components\": [{\"long_name\": \"Barcelona\",\"short_name\": \"Barcelona\",\"types\": " +
                "[\"locality\",\"political\"]},{\"long_name\": \"Barcelona\",\"short_name\": \"Barcelona\",\"types\": " +
                "[\"administrative_area_level_2\",\"political\"]},{\"long_name\": \"Catalonia\",\"short_name\": \"CT\",\"types\": " +
                "[\"administrative_area_level_1\",\"political\"]},{\"long_name\": \"Spain\",\"short_name\": \"ES\",\"types\": " +
                "[\"country\",\"political\"]}],\"formatted_address\": \"Barcelona, Spain\",\"geometry\": {\"bounds\": {\"northeast\": " +
                "{\"lat\": 41.4695761,\"lng\": 2.2280099},\"southwest\": {\"lat\": 41.320004,\"lng\": 2.0695258}},\"location\":" +
                " {\"lat\": 41.3850639,\"lng\": 2.1734035},\"location_type\": \"APPROXIMATE\",\"viewport\": {\"northeast\": " +
                "{\"lat\": 41.4695761,\"lng\": 2.2280099},\"southwest\": {\"lat\": 41.320004,\"lng\": 2.0695258}}},\"place_id\": " +
                "\"ChIJ5TCOcRaYpBIRCmZHTz37sEQ\",\"types\": [\"locality\",\"political\"]}],\"status\": \"OK\"}")

    @Before
    fun setUp() {
        addressBuilder = AddressBuilder()
    }

    @Test
    @Throws(JSONException::class)
    fun returnExpectedAddressWhenJsonProvided() {
        val json = json

        val addresses = addressBuilder!!.parseResult(json)

        assertEquals("Barcelona", addresses[0].locality)
        assertEquals("Carrer del Comte d'Urgell, 102", addresses[0].getAddressLine(0))
        assertEquals("08011", addresses[0].postalCode)
        assertTrue(java.lang.Double.valueOf(41.3838035) == addresses[0].latitude)
        assertTrue(java.lang.Double.valueOf(2.1568617) == addresses[0].longitude)
    }

    @Test
    @Throws(JSONException::class)
    fun returnExpectedAddressWhenJsonWithOnlyCityProvided() {
        val json = jsonForOnlyCity

        val addresses = addressBuilder!!.parseResult(json)

        assertEquals("Barcelona", addresses[0].locality)
        assertEquals("", addresses[0].getAddressLine(0))
        assertEquals("", addresses[0].postalCode)
        assertTrue(java.lang.Double.valueOf(41.3850639) == addresses[0].latitude)
        assertTrue(java.lang.Double.valueOf(2.1734035) == addresses[0].longitude)
    }
}
