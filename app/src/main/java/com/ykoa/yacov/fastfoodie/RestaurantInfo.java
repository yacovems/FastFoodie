package com.ykoa.yacov.fastfoodie;


import android.graphics.Bitmap;
import android.os.Parcel;
        import android.os.Parcelable;
import android.widget.ImageView;

/**
 * Created by yacov on 3/7/2018.
 */

public class RestaurantInfo implements Parcelable{

    private String name;
    private String address;
    private String phoneNumber;
    private String cuisine;
    private double rating;
    private String cost;
    private String img;
    private String distance;
    private String reviewCount;


    public RestaurantInfo() {}

    public RestaurantInfo(String name, String address, String phoneNumber, String cuisine, double rating, String cost, String distance, String img, String reviewCount) {

        this.name = name;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.cuisine = cuisine;
        this.rating = rating;
        this.cost = cost;
        this.distance = distance;
        this.img = img;
        this.reviewCount = reviewCount;
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

    @Override
    public int describeContents() {
        return 0;
    }

    public RestaurantInfo(Parcel in){
        this.name = in.readString();
        this.address = in.readString();
        this.phoneNumber = in.readString();
        this.cuisine = in.readString();
        this.rating = in.readDouble();
        this.cost = in.readString();
        this.distance = in.readString();
        this.img = in.readString();
        this.img = in.readString();
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(name);
        out.writeString(address);
        out.writeString(phoneNumber);
        out.writeString(cuisine);
        out.writeDouble(rating);
        out.writeString(cost);
        out.writeString(distance);
        out.writeString(img);
        out.writeString(reviewCount);
    }

    public static final Parcelable.Creator<RestaurantInfo> CREATOR = new Parcelable.Creator<RestaurantInfo>() {
        public RestaurantInfo createFromParcel(Parcel in) {
            return new RestaurantInfo(in);
        }

        public RestaurantInfo[] newArray(int size) {
            return new RestaurantInfo[size];
        }
    };
}

