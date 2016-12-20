package com.schibstedspain.leku;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

public class LekuPoi implements Parcelable {
  private Location location;
  private String title;
  private String address;

  public LekuPoi(String title, Location location) {
    this.location = location;
    this.title = title;
  }

  public Location getLocation() {
    return location;
  }

  public void setLocation(Location location) {
    this.location = location;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeParcelable(this.location, flags);
    dest.writeString(this.title);
    dest.writeString(this.address);
  }

  protected LekuPoi(Parcel in) {
    this.location = in.readParcelable(Location.class.getClassLoader());
    this.title = in.readString();
    this.address = in.readString();
  }

  public static final Parcelable.Creator<LekuPoi> CREATOR = new Parcelable.Creator<LekuPoi>() {
    @Override
    public LekuPoi createFromParcel(Parcel source) {
      return new LekuPoi(source);
    }

    @Override
    public LekuPoi[] newArray(int size) {
      return new LekuPoi[size];
    }
  };
}
