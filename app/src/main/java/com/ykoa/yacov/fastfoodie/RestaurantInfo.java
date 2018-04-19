package com.ykoa.yacov.fastfoodie;


import android.graphics.Bitmap;
import android.os.Parcel;
        import android.os.Parcelable;
import android.widget.ImageView;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by yacov on 3/7/2018.
 */

public class RestaurantInfo {

    private String name;
    private String address;
    private String phoneNumber;
    private String cuisine;
    private double rating;
    private String cost;
    private String img;
    private String distance;
    private String reviewCount;
    private boolean isFavorite;
    private boolean isForbidden;
    private String id;
    private LatLng location;
    private String yelpWebsite;


    public RestaurantInfo() {}

    public RestaurantInfo(String name, String address, String phoneNumber,
                          String cuisine, double rating, String cost,
                          String distance, String img, String reviewCount,
                          boolean isFavorite, boolean isForbidden, String id, LatLng location, String yelpWebsite) {

        this.name = name;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.cuisine = cuisine;
        this.rating = rating;
        this.cost = cost;
        this.distance = distance;
        this.img = img;
        this.reviewCount = reviewCount;
        this.isFavorite = isFavorite;
        this.isForbidden = isForbidden;
        this.id = id;
        this.location = location;
        this.yelpWebsite = yelpWebsite;
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

    public double getRating() {
        return rating;
    }

    public String getCost() {
        return cost;
    }

    public String getDistance() {return distance;}

    public String getImg() {return img;}

    public String getReviewCount() {return reviewCount;}

    public boolean getIsFavorite() {return isFavorite;}

    public boolean getIsForbidden() {return isForbidden;}

    public void setIsFavorite(boolean isFavorite) {this.isFavorite = isFavorite;}

    public String getId() {return id;}

    public LatLng getLocation() {return location;}

    public String getWebsite() {return yelpWebsite;}
}

