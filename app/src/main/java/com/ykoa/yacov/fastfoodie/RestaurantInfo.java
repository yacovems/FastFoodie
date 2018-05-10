package com.ykoa.yacov.fastfoodie;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by yacov on 3/7/2018.
 */

public class RestaurantInfo implements Parcelable{

    private String name;
    private String address;
    private String phoneNumber;
    private String cuisine;
    private String rating;
    private String cost;
    private String img;
    private String distance;
    private String reviewCount;
    private int isFavorite;
    private int isForbidden;
    private String id;
    private LatLng location;
    private String yelpWebsite;


    public RestaurantInfo() {}

    public RestaurantInfo(String name, String address, String phoneNumber,
                          String cuisine, String rating, String cost, String img,
                          String distance, String reviewCount,
                          int isFavorite, int isForbidden, String id,
                          LatLng location, String yelpWebsite) {

        this.name = name;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.cuisine = cuisine;
        this.rating = rating;
        this.cost = cost;
        this.img = img;
        this.distance = distance;
        this.reviewCount = reviewCount;
        this.isFavorite = isFavorite;
        this.isForbidden = isForbidden;
        this.id = id;
        this.location = location;
        this.yelpWebsite = yelpWebsite;
    }

    public static final Parcelable.Creator<RestaurantInfo> CREATOR =
            new Parcelable.Creator<RestaurantInfo>() {

        public RestaurantInfo createFromParcel(Parcel in) {
            return new RestaurantInfo(in);
        }

        public RestaurantInfo[] newArray(int size) {
            return new RestaurantInfo[size];
        }
    };

    @Override
    public void writeToParcel(Parcel out, int flags) {
        // TODO Auto-generated method stub
        out.writeString(name);
        out.writeString(address);
        out.writeString(phoneNumber);
        out.writeString(cuisine);
        out.writeString(rating);
        out.writeString(cost);
        out.writeString(img);
        out.writeString(distance);
        out.writeString(reviewCount);
        out.writeInt(isFavorite);
        out.writeInt(isForbidden);
        out.writeString(id);
        out.writeParcelable(location, flags);
        out.writeString(yelpWebsite);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    private RestaurantInfo(Parcel in) {
        this.name = in.readString();
        this.address = in.readString();
        this.phoneNumber = in.readString();
        this.cuisine = in. readString();
        this.rating = in.readString();
        this.cost = in.readString();
        this.img = in.readString();
        this.distance = in.readString();
        this.reviewCount = in.readString();
        this.isFavorite = in.readInt();
        this.isForbidden = in.readInt();
        this.id = in.readString();
        this.location = in.readParcelable(LatLng.class.getClassLoader());
        this.yelpWebsite = in.readString();
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getCuisine() {
        return cuisine;
    }

    public String getRating() {
        return rating;
    }

    public String getCost() {
        return cost;
    }

    public String getDistance() {return distance;}

    public String getImg() {return img;}

    public String getReviewCount() {return reviewCount;}

    public int getIsFavorite() {return isFavorite;}

    public int getIsForbidden() {return isForbidden;}

    public void setIsForbidden(int forbidden) {
        isForbidden = forbidden;
    }

    public void setIsFavorite(int isFavorite) {this.isFavorite = isFavorite;}

    public String getId() {return id;}

    public LatLng getLocation() {return location;}

    public String getWebsite() {return yelpWebsite;}
}

