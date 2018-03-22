package com.ykoa.yacov.fastfoodie;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

/**
 * Created by Yacov on 3/10/2018.
 */

public interface FragmentCommunication {

    public void setRadius(int radius);
    public int getRadius();
    public void setRestaurantList(ArrayList<RestaurantInfo> list);
    public ArrayList<RestaurantInfo> getRestaurantList();
    public void setHasChanged(boolean changed);
    public boolean getHasChanged();
    public GoogleMap getMap();
    public void setMap(GoogleMap map);
    public LatLng getLatLng();
    public void setLatLng(LatLng position);
    public void setCost(int cost);
    public int getCost();
    public void setRating(int rating);
    public int getRating();
}
