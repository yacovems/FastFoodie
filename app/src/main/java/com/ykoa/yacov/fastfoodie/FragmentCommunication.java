package com.ykoa.yacov.fastfoodie;

import java.util.ArrayList;

/**
 * Created by Yacov on 3/10/2018.
 */

public interface FragmentCommunication {

    public void setRadius(int radius);
    public int getRadius();
    public void setRestaurantList(ArrayList<RestaurantInfo> list);
    public ArrayList<RestaurantInfo> getRestaurantList();
}
