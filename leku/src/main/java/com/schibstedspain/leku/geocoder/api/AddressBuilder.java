package com.schibstedspain.leku.geocoder.api;

import android.location.Address;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AddressBuilder {

  public List<Address> parseResult(String json) throws JSONException {
    List<Address> addresses = new ArrayList<>();
    JSONObject root = new JSONObject(json);
    JSONArray results = root.getJSONArray("results");
    for (int i = 0; i < results.length(); i++) {
      addresses.add(parseAddress(results.getJSONObject(i)));
    }
    return addresses;
  }

  private Address parseAddress(JSONObject jsonObject) throws JSONException {
    JSONObject location = jsonObject.getJSONObject("geometry").getJSONObject("location");
    double latitude = location.getDouble("lat");
    double longitude = location.getDouble("lng");

    List<AddressComponent> components = getAddressComponents(jsonObject.getJSONArray("address_components"));


    String postalCode = "";
    String city = "";
    String number = "";
    String street = "";
    for (AddressComponent component : components) {
      if (component.types.contains("postal_code")) {
        postalCode = component.name;
      }
      if (component.types.contains("locality")) {
        city = component.name;
      }
      if (component.types.contains("street_number")) {
        number = component.name;
      }
      if (component.types.contains("route")) {
        street = component.name;
      }
    }
    StringBuilder fullAddress = new StringBuilder();
    fullAddress.append(street);
    if (!street.isEmpty() && !number.isEmpty()) {
      fullAddress.append(", ").append(number);
    }
    Address address = new Address(Locale.getDefault());
    address.setLatitude(latitude);
    address.setLongitude(longitude);
    address.setPostalCode(postalCode);
    address.setAddressLine(0, fullAddress.toString());
    address.setAddressLine(1, postalCode);
    address.setAddressLine(2, city);
    address.setLocality(city);
    return address;
  }

  private List<AddressComponent> getAddressComponents(JSONArray jsonComponents) throws JSONException {
    List<AddressComponent> components = new ArrayList<>();
    for (int i = 0; i < jsonComponents.length(); i++) {
      AddressComponent component = new AddressComponent();
      JSONObject jsonComponent = jsonComponents.getJSONObject(i);
      component.name = jsonComponent.getString("long_name");
      component.types = new ArrayList<>();
      JSONArray jsonTypes = jsonComponent.getJSONArray("types");
      for (int j = 0; j < jsonTypes.length(); j++) {
        component.types.add(jsonTypes.getString(j));
      }
      components.add(component);
    }
    return components;
  }

  private static class AddressComponent {
    String name;
    List<String> types;
  }
}
