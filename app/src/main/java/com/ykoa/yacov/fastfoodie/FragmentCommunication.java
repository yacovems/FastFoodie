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

    void setRadius(int radius);
    int getRadius();
    void setRestaurantList(ArrayList<RestaurantInfo> list);
    void setTempRestaurantList(ArrayList<RestaurantInfo> list);
    ArrayList<RestaurantInfo> getRestaurantList();
    ArrayList<RestaurantInfo> getTempRestaurantList();
    void setIsInitialized(boolean changed);
    boolean getIsInitialized();
    GoogleMap getMap();
    void setMap(GoogleMap map);
    LatLng getLatLng();
    void setLatLng(LatLng position);
    void setCost(int cost);
    int getCost();
    void setRating(int rating);
    int getRating();
    HashMap<String, String> getFavorites();
    void setFavorites(HashMap<String, String> favorites);
    HashMap<String, String> getForbidden();
    void setForbidden(HashMap<String, String> forbidden);
    String getUserId();
    void updateDB(HashMap<String, Object> user);
    void updateRecyclerView();
    void updateMapView();
    void showSortButton();
    void hideSortButton();
}
