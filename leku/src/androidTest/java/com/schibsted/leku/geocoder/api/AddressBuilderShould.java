package com.schibsted.leku.geocoder.api;

import android.location.Address;
import android.support.annotation.NonNull;
import com.schibstedspain.leku.geocoder.api.AddressBuilder;
import java.util.List;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.junit.MockitoRule;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.mockito.junit.MockitoJUnit.rule;

public class AddressBuilderShould {

  @Rule public MockitoRule mockitoRule = rule();

  private AddressBuilder addressBuilder;

  @Before
  public void setUp() {
    addressBuilder = new AddressBuilder();
  }

  @Test
  public void returnExpectedAddress_WhenJsonProvided() throws JSONException {
    String json = getJson();

    List<Address> addresses = addressBuilder.parseResult(json);

    assertEquals("Barcelona", addresses.get(0).getLocality());
    assertEquals("Carrer del Comte d'Urgell, 102", addresses.get(0).getAddressLine(0));
    assertEquals("08011", addresses.get(0).getPostalCode());
    assertTrue(Double.valueOf(41.3838035).equals(addresses.get(0).getLatitude()));
    assertTrue(Double.valueOf(2.1568617).equals(addresses.get(0).getLongitude()));

  }

  @Test
  public void returnExpectedAddress_WhenJsonWithOnlyCityProvided() throws JSONException {
    String json = getJsonForOnlyCity();

    List<Address> addresses = addressBuilder.parseResult(json);

    assertEquals("Barcelona", addresses.get(0).getLocality());
    assertEquals("", addresses.get(0).getAddressLine(0));
    assertEquals("", addresses.get(0).getPostalCode());
    assertTrue(Double.valueOf(41.3850639).equals(addresses.get(0).getLatitude()));
    assertTrue(Double.valueOf(2.1734035).equals(addresses.get(0).getLongitude()));

  }

  @NonNull
  private String getJson() {
    return "{\"results\": [{\"address_components\": [{\"long_name\": \"102\",\"short_name\": \"102\",\"types\": "
        + "[ \"street_number\"]},{\"long_name\": \"Carrer del Comte d'Urgell\",\"short_name\": \"Carrer del Comte d'Urgell\",\"types\": "
        + "[ \"route\"]},{\"long_name\": \"Barcelona\",\"short_name\": \"Barcelona\",\"types\": [ \"locality\", \"political\"]},"
        + "{\"long_name\": \"Barcelona\",\"short_name\": \"Barcelona\",\"types\": [ \"administrative_area_level_2\", \"political\"]},"
        + "{\"long_name\": \"Catalunya\",\"short_name\": \"CT\",\"types\": [ \"administrative_area_level_1\", \"political\"]},"
        + "{\"long_name\": \"Spain\",\"short_name\": \"ES\",\"types\": [ \"country\", \"political\"]},{\"long_name\": \"08011\","
        + "\"short_name\": \"08011\",\"types\": [ \"postal_code\"]}],\"formatted_address\""
        + ": \"Carrer del Comte d'Urgell, 102, 08011 Barcelona, Spain\",\"geometry\": {\"bounds\": {\"northeast\": "
        + "{ \"lat\": 41.3839416, \"lng\": 2.1570442},\"southwest\": { \"lat\": 41.3836653, \"lng\": 2.1566792}},\"location\": "
        + "{\"lat\": 41.3838035,\"lng\": 2.1568617},\"location_type\": \"ROOFTOP\",\"viewport\": {\"northeast\": "
        + "{ \"lat\": 41.3851524302915, \"lng\": 2.158210680291502},\"southwest\": { \"lat\": 41.3824544697085, "
        + "\"lng\": 2.155512719708498}}},\"partial_match\": true,\"place_id\": \"ChIJdehx-YiipBIR8hitzOckUuo\",\"types\": [\"premise\"] } "
        + "], \"status\": \"OK\"}";
  }

  @NonNull
  private String getJsonForOnlyCity() {
    return "{\"results\": [{\"address_components\": [{\"long_name\": \"Barcelona\",\"short_name\": \"Barcelona\",\"types\": "
        + "[\"locality\",\"political\"]},{\"long_name\": \"Barcelona\",\"short_name\": \"Barcelona\",\"types\": "
        + "[\"administrative_area_level_2\",\"political\"]},{\"long_name\": \"Catalonia\",\"short_name\": \"CT\",\"types\": "
        + "[\"administrative_area_level_1\",\"political\"]},{\"long_name\": \"Spain\",\"short_name\": \"ES\",\"types\": "
        + "[\"country\",\"political\"]}],\"formatted_address\": \"Barcelona, Spain\",\"geometry\": {\"bounds\": {\"northeast\": "
        + "{\"lat\": 41.4695761,\"lng\": 2.2280099},\"southwest\": {\"lat\": 41.320004,\"lng\": 2.0695258}},\"location\":"
        + " {\"lat\": 41.3850639,\"lng\": 2.1734035},\"location_type\": \"APPROXIMATE\",\"viewport\": {\"northeast\": "
        + "{\"lat\": 41.4695761,\"lng\": 2.2280099},\"southwest\": {\"lat\": 41.320004,\"lng\": 2.0695258}}},\"place_id\": "
        + "\"ChIJ5TCOcRaYpBIRCmZHTz37sEQ\",\"types\": [\"locality\",\"political\"]}],\"status\": \"OK\"}";
  }
}