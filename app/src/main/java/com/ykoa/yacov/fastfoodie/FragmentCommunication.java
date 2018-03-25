package com.ykoa.yacov.fastfoodie;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

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
    public HashMap<String, String> getFavorites();
    public void setFavorites(HashMap<String, String> favorites);
    public HashMap<String, String> getForbidden();
    public void setForbidden(HashMap<String, String> forbidden);
    public String getUserId();
    public void updateDB(HashMap<String, Object> user);
}
